package io.scanbot.example

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.camera.CameraPreviewMode
import io.scanbot.sdk.camera.FrameHandlerResult
import io.scanbot.sdk.common.AspectRatio
import io.scanbot.sdk.textpattern.ContentValidationCallback
import io.scanbot.sdk.textpattern.CustomContentValidator
import io.scanbot.sdk.textpattern.TextPatternScanner
import io.scanbot.sdk.textpattern.TextPatternScannerFrameHandler
import io.scanbot.sdk.ui.camera.IScanbotCameraView
import io.scanbot.sdk.ui.camera.ScanbotCameraXView
import io.scanbot.sdk.ui.camera.ZoomFinderOverlayView

class ScannerActivity : AppCompatActivity() {
    private lateinit var cameraView: IScanbotCameraView
    private lateinit var resultTextView: TextView
    
    private var useFlash = false

    private lateinit var patternScanner: TextPatternScanner
    private lateinit var patternScannerFrameHandler: TextPatternScannerFrameHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        cameraView = findViewById<ScanbotCameraXView>(R.id.cameraView)
        resultTextView = findViewById(R.id.resultTextView)

        val zoomFinderOverlay = findViewById<ZoomFinderOverlayView>(R.id.finder_overlay)
        // The smaller finder view brings better performance and allows user to detect text more precise
        zoomFinderOverlay.setRequiredAspectRatios(listOf(AspectRatio(4.0, 1.0)))
        zoomFinderOverlay.zoomLevel = 1.8f

        cameraView.setPreviewMode(CameraPreviewMode.FIT_IN)
        patternScanner = ScanbotSDK(this).createTextPatternScanner()

        // TODO: set validation string and validation callback which matches the need of the task
        // For the pattern: # - digits, ? - for any character. Other characters represent themselves
        // In this example we are waiting for a string which starts with 1 or 2, and then 5 more digits
        patternScanner.configuration = patternScanner.configuration.copy(
            // validator = PresetContentValidator(preset = ValidatorPreset.VEHICLE_IDENTIFICATION_NUMBER),
            // validator = PatternContentValidator(pattern = "######"),
            validator = CustomContentValidator(callback =  object : ContentValidationCallback {
                override fun clean(rawText: String): String {
                    return rawText.replace(" ", "_")
                }

                override fun validate(text: String): Boolean {
                    return text.firstOrNull() in listOf('1', '2') // TODO: add additional validation for the recognized text
                }
            })
        )

        patternScannerFrameHandler = TextPatternScannerFrameHandler.attach(cameraView, patternScanner)
        patternScannerFrameHandler.addResultHandler { result ->
            val resultText: String = when (result) {
                is FrameHandlerResult.Success -> {
                    when {
                        result.value.validationSuccessful -> {
                            result.value.rawText
                            // TODO: you can open the screen with a result as soon as
                        }
                        else -> ""
                    }
                }
                is FrameHandlerResult.Failure -> "Check your setup or license"
            }

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
