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
import io.scanbot.sdk.SdkLicenseError
import io.scanbot.sdk.camera.CameraOpenCallback
import io.scanbot.sdk.camera.FrameHandlerResult
import io.scanbot.sdk.camera.ScanbotCameraView
import io.scanbot.sdk.core.payformscanner.DetectionResult
import io.scanbot.sdk.payformscanner.PayFormScannerFrameHandler
import io.scanbot.sdk.util.log.LoggerProvider

class PayformScannerActivity : AppCompatActivity() {
    private lateinit var cameraView: ScanbotCameraView

    private val logger = LoggerProvider.logger

    private var flashEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payform_scanner)
        supportActionBar!!.hide()

        cameraView = findViewById<View>(R.id.camera) as ScanbotCameraView
        cameraView.lockToLandscape(true)
        cameraView.setCameraOpenCallback(object : CameraOpenCallback {
            override fun onCameraOpened() {
                cameraView.postDelayed({
                    cameraView.useFlash(flashEnabled)
                    cameraView.continuousFocus()
                }, 700)
            }
        })

        val scanbotSDK = ScanbotSDK(this)
        val payFormScanner = scanbotSDK.payFormScanner()

        val payFormScannerFrameHandler = PayFormScannerFrameHandler.attach(cameraView, payFormScanner)

        payFormScannerFrameHandler.addResultHandler(object : PayFormScannerFrameHandler.ResultHandler {
            override fun handle(result: FrameHandlerResult<DetectionResult, SdkLicenseError>): Boolean {
                if (result is FrameHandlerResult.Success<*>) {
                    val detectionResult = (result as FrameHandlerResult.Success<DetectionResult?>).value

                    detectionResult?.let {
                        if (detectionResult.form?.isValid == true) {
                            val detectStart = System.currentTimeMillis()

                            try {
                                payFormScanner.recognizeForm(detectionResult.lastFrame,
                                        detectionResult.frameWidth, detectionResult.frameHeight, 0)?.let { recognitionResult ->
                                    startActivity(newIntent(this@PayformScannerActivity, recognitionResult.payformFields))
                                }
                            } finally {
                                val detectEnd = System.currentTimeMillis()
                                logger.d("PayFormScanner", "Total scanning (sec): " + (detectEnd - detectStart) / 1000f)
                            }
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
            return Intent(context, PayformScannerActivity::class.java)
        }
    }
}