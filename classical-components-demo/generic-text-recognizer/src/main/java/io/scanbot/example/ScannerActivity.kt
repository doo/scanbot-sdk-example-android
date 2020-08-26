package io.scanbot.example

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.SdkLicenseError
import io.scanbot.sdk.camera.CameraOpenCallback
import io.scanbot.sdk.camera.CameraPreviewMode
import io.scanbot.sdk.camera.FrameHandlerResult
import io.scanbot.sdk.entity.Language
import io.scanbot.sdk.generictext.GenericTextRecognitionResult
import io.scanbot.sdk.generictext.GenericTextRecognizer
import io.scanbot.sdk.generictext.GenericTextRecognizerFrameHandler
import io.scanbot.sdk.ui.camera.FinderOverlayView
import io.scanbot.sdk.ui.camera.IScanbotCameraView
import io.scanbot.sdk.ui.camera.ScanbotCameraXView

class ScannerActivity : AppCompatActivity() {
    private val scanbotSdk = ScanbotSDK(this)
    private val genericTextScanner = scanbotSdk.genericTextRecognizer()

    private lateinit var cameraView: IScanbotCameraView
    private lateinit var resultTextView: TextView

    private var useFlash = false

    private lateinit var genericTextRecognizerFrameHandler: GenericTextRecognizerFrameHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        cameraView = findViewById<ScanbotCameraXView>(R.id.cameraView)
        resultTextView = findViewById(R.id.resultTextView)

        val finderOverlay = findViewById<FinderOverlayView>(R.id.finder_overlay)
        // The smaller finder view brings better performance and
        finderOverlay.setFixedFinderHeight(100)
        finderOverlay.setFixedFinderWidth(400)

        cameraView.setPreviewMode(CameraPreviewMode.FIT_IN)

        // TODO: set validation string and validation callback which matches the need of the task
        // For the pattern: # - digits, ? - for any character. Other characters represent themselves
        // In this example we are waiting for a string which starts with 1 or 2, and then 5 more digits
        genericTextScanner.setValidator("######", object : GenericTextRecognizer.GenericTextValidationCallback {
            override fun validate(text: String): Boolean {
                return text.first() in listOf('1', '2') // TODO: add additional validation for the recognized text
            }
        })

        genericTextScanner.supportedLanguages = setOf(Language.ENG, Language.DEU)

        genericTextRecognizerFrameHandler = GenericTextRecognizerFrameHandler.attach(cameraView, genericTextScanner)
        genericTextRecognizerFrameHandler.addResultHandler(object : GenericTextRecognizerFrameHandler.ResultHandler {
            override fun handle(result: FrameHandlerResult<GenericTextRecognitionResult, SdkLicenseError>): Boolean {
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

                return false
            }
        })

        cameraView.setCameraOpenCallback(object : CameraOpenCallback {
            override fun onCameraOpened() {
                cameraView.useFlash(useFlash)
                cameraView.continuousFocus()
            }
        })
        findViewById<Button>(R.id.flashButton).setOnClickListener { toggleFlash() }
    }

    private fun toggleFlash() {
        useFlash = !useFlash
        cameraView.useFlash(useFlash)
    }
}
