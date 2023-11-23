package io.scanbot.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import io.scanbot.hicscanner.model.HealthInsuranceCardDetectionStatus
import io.scanbot.sdk.AspectRatio
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.camera.FrameHandlerResult
import io.scanbot.sdk.hicscanner.HealthInsuranceCardScannerFrameHandler
import io.scanbot.sdk.ui.camera.FinderOverlayView
import io.scanbot.sdk.ui.camera.ScanbotCameraXView
import io.scanbot.sdk.util.log.LoggerProvider

class EhicLiveDetectionActivity : AppCompatActivity() {
    private lateinit var cameraView: ScanbotCameraXView
    private lateinit var finderOverlay: FinderOverlayView

    private val logger = LoggerProvider.logger

    private var flashEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ehic_live_scanner)
        supportActionBar!!.hide()
        cameraView = findViewById(R.id.camera)
        cameraView.setCameraOpenCallback {
            cameraView.postDelayed({
                cameraView.useFlash(flashEnabled)
                cameraView.continuousFocus()
            }, 700)
        }

        finderOverlay = findViewById(R.id.finder_overlay)
        finderOverlay.setRequiredAspectRatios(listOf(AspectRatio(3.0, 2.0)))

        val scanbotSDK = ScanbotSDK(this)
        val healthInsuranceCardScanner = scanbotSDK.createHealthInsuranceCardScanner()
        val frameHandler = HealthInsuranceCardScannerFrameHandler.attach(cameraView, healthInsuranceCardScanner)

        frameHandler.addResultHandler { result ->
            if (result is FrameHandlerResult.Success) {
                val recognitionResult = result.value
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
            false
        }
        findViewById<View>(R.id.flash).setOnClickListener { v: View? ->
            flashEnabled = !flashEnabled
            cameraView.useFlash(flashEnabled)
        }

        Toast.makeText(this, if (scanbotSDK.licenseInfo.isValid) "License is active" else "License is expired", Toast.LENGTH_LONG).show()
    }

    companion object {
        @JvmStatic
        fun newIntent(context: Context?): Intent {
            return Intent(context, EhicLiveDetectionActivity::class.java)
        }
    }
}