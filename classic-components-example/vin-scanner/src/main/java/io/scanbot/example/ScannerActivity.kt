package io.scanbot.example

import android.os.Bundle
import android.util.TypedValue
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.common.AspectRatio
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.camera.CameraPreviewMode
import io.scanbot.sdk.camera.FrameHandlerResult
import io.scanbot.sdk.ui.camera.FinderOverlayView
import io.scanbot.sdk.ui.camera.IScanbotCameraView
import io.scanbot.sdk.ui.camera.ScanbotCameraXView
import io.scanbot.sdk.vin.VinScanner
import io.scanbot.sdk.vin.VinScannerFrameHandler

class ScannerActivity : AppCompatActivity() {
    private lateinit var vinScanner: VinScanner

    private lateinit var cameraView: IScanbotCameraView
    private lateinit var resultTextView: TextView

    private var useFlash = false

    private lateinit var vinScannerFrameHandler: VinScannerFrameHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)
        vinScanner = ScanbotSDK(this).createVinScanner()

        cameraView = findViewById<ScanbotCameraXView>(R.id.cameraView)
        resultTextView = findViewById(R.id.resultTextView)

        val finderOverlay = findViewById<FinderOverlayView>(R.id.finder_overlay)
        // The smaller finder view brings better performance and allows user to detect VIN more precise
        finderOverlay.setRequiredAspectRatios(listOf(AspectRatio(9.0, 1.0)))
        finderOverlay.setFixedFinderHeight(
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        50f, resources.displayMetrics).toInt()
        )

        cameraView.setPreviewMode(CameraPreviewMode.FIT_IN)

        vinScannerFrameHandler = VinScannerFrameHandler.attach(cameraView, vinScanner)

        vinScannerFrameHandler.addResultHandler { result ->
            val resultText: String = when (result) {
                is FrameHandlerResult.Success -> {
                    if (result.value.validationSuccessful) {
                        "VIN scanned:\n${result.value.rawText}"
                    } else {
                        "VIN not validated"
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
