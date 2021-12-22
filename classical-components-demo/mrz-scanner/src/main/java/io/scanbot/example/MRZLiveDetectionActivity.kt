package io.scanbot.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.camera.FrameHandlerResult
import io.scanbot.sdk.camera.ScanbotCameraView
import io.scanbot.sdk.mrzscanner.MRZScannerFrameHandler
import io.scanbot.sdk.util.log.LoggerProvider

class MRZLiveDetectionActivity : AppCompatActivity() {
    private val logger = LoggerProvider.logger

    private lateinit var cameraView: ScanbotCameraView

    private var flashEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mrz_live_scanner)
        supportActionBar!!.hide()
        cameraView = findViewById<View>(R.id.camera) as ScanbotCameraView

        cameraView.setCameraOpenCallback {
            cameraView.postDelayed({
                cameraView.useFlash(flashEnabled)
                cameraView.continuousFocus()
            }, 700)
        }
        val scanbotSDK = ScanbotSDK(this)

        val mrzScanner = scanbotSDK.createMrzScanner()
        val mrzScannerFrameHandler = MRZScannerFrameHandler.attach(cameraView, mrzScanner)

        mrzScannerFrameHandler.addResultHandler { result ->
            if (result is FrameHandlerResult.Success) {
                val mrzRecognitionResult = result.value

                // It is recommended to use a frame accumulation as well and expect at least 2 of 4 frames to be equal
                if (mrzRecognitionResult.recognitionSuccessful &&
                        mrzRecognitionResult.checkDigitsCount == mrzRecognitionResult.validCheckDigitsCount) {
                    startActivity(MRZResultActivity.newIntent(this, mrzRecognitionResult))
                }
            }
            false
        }

        findViewById<View>(R.id.flash).setOnClickListener { v: View? ->
            flashEnabled = !flashEnabled
            cameraView.useFlash(flashEnabled)
        }
        Toast.makeText(this, if (scanbotSDK.isLicenseActive) "License is active" else "License is expired", Toast.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        cameraView.onResume()
    }

    override fun onPause() {
        super.onPause()
        cameraView.onPause()
    }

    companion object {
        fun newIntent(context: Context?): Intent {
            return Intent(context, MRZLiveDetectionActivity::class.java)
        }
    }
}