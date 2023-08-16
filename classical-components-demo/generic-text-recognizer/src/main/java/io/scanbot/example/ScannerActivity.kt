package io.scanbot.example

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.camera.CameraPreviewMode
import io.scanbot.sdk.camera.FrameHandlerResult
import io.scanbot.sdk.generictext.GenericTextRecognizer
import io.scanbot.sdk.generictext.GenericTextRecognizerFrameHandler
import io.scanbot.sdk.AspectRatio
import io.scanbot.sdk.ui.camera.IScanbotCameraView
import io.scanbot.sdk.ui.camera.ScanbotCameraXView
import io.scanbot.sdk.ui.camera.ZoomFinderOverlayView

class ScannerActivity : AppCompatActivity() {
    private lateinit var cameraView: IScanbotCameraView
    private lateinit var resultTextView: TextView
    
    private var useFlash = false

    private lateinit var textRecognizer: GenericTextRecognizer
    private lateinit var textRecognizerFrameHandler: GenericTextRecognizerFrameHandler

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
        textRecognizer = ScanbotSDK(this).createGenericTextRecognizer()

        // TODO: set validation string and validation callback which matches the need of the task
        // For the pattern: # - digits, ? - for any character. Other characters represent themselves
        // In this example we are waiting for a string which starts with 1 or 2, and then 5 more digits
        textRecognizer.setValidator("######", object : GenericTextRecognizer.GenericTextValidationCallback {
            override fun validate(text: String): Boolean {
                return text.firstOrNull() in listOf('1', '2') // TODO: add additional validation for the recognized text
            }
        })


        // TODO: If the string which is needed to scan is not clearly separated from other parts of the text
        // then enable this setting. This will only work with 'pattern' variable from the validator:
        //
        // genericTextScanner.matchSubstringForPattern = true


        // TODO: as an alternative it is possible to extract the valuable text from the raw scanned text manually
        // using a Cleaner. The effective implementation of this function might significantly improve the speed
        // of scanning
        //
        // genericTextScanner.setCleaner(object : GenericTextRecognizer.CleanRecognitionResultCallback {
        //     override fun process(rawText: String): String {
        //         return extractValuableDataFromText(rawText)
        //     }
        // })

        textRecognizerFrameHandler = GenericTextRecognizerFrameHandler.attach(cameraView, textRecognizer)
        textRecognizerFrameHandler.addResultHandler { result ->
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
