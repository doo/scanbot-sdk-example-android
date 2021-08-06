package io.scanbot.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import io.scanbot.example.PayformResultActivity.Companion.newIntent
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.camera.FrameHandlerResult
import io.scanbot.sdk.camera.ScanbotCameraView
import io.scanbot.sdk.payformscanner.PayFormScannerFrameHandler

class PayformScannerActivity : AppCompatActivity() {
    private lateinit var cameraView: ScanbotCameraView
    private var flashEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payform_scanner)
        supportActionBar!!.hide()

        cameraView = findViewById<View>(R.id.camera) as ScanbotCameraView
        cameraView.lockToLandscape(true)
        cameraView.setCameraOpenCallback {
            cameraView.postDelayed({
                cameraView.useFlash(flashEnabled)
                cameraView.continuousFocus()
            }, 700)
        }

        val scanbotSDK = ScanbotSDK(this)
        val payFormScanner = scanbotSDK.createPayFormScanner()

        val payFormScannerFrameHandler = PayFormScannerFrameHandler.attach(cameraView, payFormScanner)

        payFormScannerFrameHandler.addResultHandler { result ->
            if (result is FrameHandlerResult.Success) {
                val detectionResult = result.value
                if (detectionResult.form?.isValid == true) {
                    payFormScanner.recognizeForm(
                        detectionResult.lastFrame,
                        detectionResult.frameWidth, detectionResult.frameHeight, 0
                    )?.let { recognitionResult ->
                        startActivity(newIntent(this@PayformScannerActivity, recognitionResult.payformFields))
                    }
                }
            }
            false
        }

        findViewById<View>(R.id.flash).setOnClickListener { v: View? ->
            flashEnabled = !flashEnabled
            cameraView.useFlash(flashEnabled)
        }

        Toast.makeText(this, if (scanbotSDK.licenseInfo.isValid) "License is active" else "License is expired", Toast.LENGTH_LONG).show()
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
            return Intent(context, PayformScannerActivity::class.java)
        }
    }
}