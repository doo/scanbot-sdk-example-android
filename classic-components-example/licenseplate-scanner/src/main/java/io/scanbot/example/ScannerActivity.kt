package io.scanbot.example

import android.os.Bundle
import android.util.TypedValue
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.camera.CameraPreviewMode
import io.scanbot.sdk.camera.FrameHandlerResult
import io.scanbot.sdk.common.AspectRatio
import io.scanbot.sdk.licenseplate.LicensePlateScanner
import io.scanbot.sdk.licenseplate.LicensePlateScannerFrameHandler
import io.scanbot.sdk.ui.camera.IScanbotCameraView
import io.scanbot.sdk.ui.camera.ScanbotCameraXView
import io.scanbot.sdk.ui.camera.ZoomFinderOverlayView

class ScannerActivity : AppCompatActivity() {
    private lateinit var licensePlateScanner: LicensePlateScanner

    private lateinit var cameraView: IScanbotCameraView
    private lateinit var resultTextView: TextView

    private var useFlash = false

    private lateinit var licensePlateScannerFrameHandler: LicensePlateScannerFrameHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)
        licensePlateScanner = ScanbotSDK(this).createLicensePlateScanner()

        cameraView = findViewById<ScanbotCameraXView>(R.id.cameraView)
        resultTextView = findViewById(R.id.resultTextView)

        val zoomFinderOverlay = findViewById<ZoomFinderOverlayView>(R.id.finder_overlay)
        // The smaller finder view brings better performance and allows user to detect text more precise
        zoomFinderOverlay.setRequiredAspectRatios(listOf(AspectRatio(4.0, 1.0)))
        zoomFinderOverlay.zoomLevel = 1.4f
        zoomFinderOverlay.setFixedFinderHeight(
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        70f, resources.displayMetrics).toInt()
        )

        cameraView.setPreviewMode(CameraPreviewMode.FIT_IN)

        licensePlateScannerFrameHandler = LicensePlateScannerFrameHandler.attach(cameraView, licensePlateScanner)

        licensePlateScannerFrameHandler.addResultHandler { result ->
            val resultText: String = when (result) {
                is FrameHandlerResult.Success -> {
                    if (result.value.validationSuccessful) {
                        result.value.rawText
                    } else {
                        "License plate not found"
                    }
                }
                is FrameHandlerResult.Failure -> "Check your setup or license"
            }

            // NOTE: 'handle' method runs in background thread - don't forget to switch to main before touching any Views
            runOnUiThread { resultTextView.text = resultText }

            false
        }

        cameraView.setCameraOpenCallback {
            cameraView.useFlash(useFlash)
            cameraView.continuousFocus()
        }
        findViewById<Button>(R.id.flashButton).setOnClickListener { toggleFlash() }
    }

    private fun toggleFlash() {
        useFlash = !useFlash
        cameraView.useFlash(useFlash)
    }
}
