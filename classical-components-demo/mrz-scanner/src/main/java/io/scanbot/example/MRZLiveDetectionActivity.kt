package io.scanbot.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import io.scanbot.mrzscanner.model.MRZRecognitionResult
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.SdkLicenseError
import io.scanbot.sdk.camera.CameraOpenCallback
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
        cameraView.setCameraOpenCallback(CameraOpenCallback {
            cameraView.postDelayed({
                cameraView.useFlash(flashEnabled)
                cameraView.continuousFocus()
            }, 700)
        })
        val scanbotSDK = ScanbotSDK(this)

        val mrzScanner = scanbotSDK.mrzScanner()
        val mrzScannerFrameHandler = MRZScannerFrameHandler.attach(cameraView, mrzScanner)

        mrzScannerFrameHandler.addResultHandler(object : MRZScannerFrameHandler.ResultHandler {
            override fun handle(result: FrameHandlerResult<MRZRecognitionResult, SdkLicenseError>): Boolean {
                if (result is FrameHandlerResult.Success<*>) {
                    val mrzRecognitionResult = (result as FrameHandlerResult.Success<*>).value as MRZRecognitionResult?
                    if (mrzRecognitionResult != null && mrzRecognitionResult.recognitionSuccessful) {
                        val detectStart = System.currentTimeMillis()
                        try {
                            startActivity(MRZResultActivity.newIntent(this@MRZLiveDetectionActivity, mrzRecognitionResult))
                        } finally {
                            val detectEnd = System.currentTimeMillis()
                            logger.d("MRZScanner", "Total scanning (sec): " + (detectEnd - detectStart) / 1000f)
                        }
                    }
                }
                return false
            }
        })

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