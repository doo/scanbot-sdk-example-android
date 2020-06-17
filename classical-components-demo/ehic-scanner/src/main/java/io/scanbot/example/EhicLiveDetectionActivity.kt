package io.scanbot.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import io.scanbot.hicscanner.model.HealthInsuranceCardDetectionStatus
import io.scanbot.hicscanner.model.HealthInsuranceCardRecognitionResult
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.SdkLicenseError
import io.scanbot.sdk.camera.CameraOpenCallback
import io.scanbot.sdk.camera.FrameHandlerResult
import io.scanbot.sdk.camera.ScanbotCameraView
import io.scanbot.sdk.hicscanner.HealthInsuranceCardScannerFrameHandler
import io.scanbot.sdk.util.log.LoggerProvider

class EhicLiveDetectionActivity : AppCompatActivity() {
    private lateinit var cameraView: ScanbotCameraView

    private val logger = LoggerProvider.logger

    private var flashEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ehic_live_scanner)
        supportActionBar!!.hide()
        cameraView = findViewById(R.id.camera)
        cameraView.setCameraOpenCallback(CameraOpenCallback {
            cameraView.postDelayed(Runnable {
                cameraView.useFlash(flashEnabled)
                cameraView.continuousFocus()
            }, 700)
        })

        val scanbotSDK = ScanbotSDK(this)
        val healthInsuranceCardScanner = scanbotSDK.healthInsuranceCardScanner()
        val frameHandler = HealthInsuranceCardScannerFrameHandler.attach(cameraView, healthInsuranceCardScanner)

        frameHandler.addResultHandler(object : HealthInsuranceCardScannerFrameHandler.ResultHandler {
            override fun handle(result: FrameHandlerResult<HealthInsuranceCardRecognitionResult?, SdkLicenseError>): Boolean {
                if (result is FrameHandlerResult.Success<*>) {
                    val recognitionResult = (result as FrameHandlerResult.Success<*>).value as HealthInsuranceCardRecognitionResult?
                    if (recognitionResult != null && recognitionResult.status == HealthInsuranceCardDetectionStatus.SUCCESS) {
                        val detectStart = System.currentTimeMillis()
                        try {
                            startActivity(EhicResultActivity.newIntent(this@EhicLiveDetectionActivity, recognitionResult))
                        } finally {
                            val detectEnd = System.currentTimeMillis()
                            logger.d("EHICScanner", "Total scanning (sec): " + (detectEnd - detectStart) / 1000f)
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
        @JvmStatic
        fun newIntent(context: Context?): Intent {
            return Intent(context, EhicLiveDetectionActivity::class.java)
        }
    }
}