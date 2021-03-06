package io.scanbot.example

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.SdkLicenseError
import io.scanbot.sdk.camera.CameraOpenCallback
import io.scanbot.sdk.camera.CameraPreviewMode
import io.scanbot.sdk.camera.FrameHandlerResult
import io.scanbot.sdk.genericdocument.GenericDocumentRecognitionResult
import io.scanbot.sdk.genericdocument.GenericDocumentRecognizer
import io.scanbot.sdk.genericdocument.GenericDocumentRecognizerFrameHandler
import io.scanbot.sdk.idcardscanner.IdCardScannerFrameHandler
import io.scanbot.sdk.idcardscanner.IdScanResult
import io.scanbot.sdk.ui.camera.*

class ScannerActivity : AppCompatActivity() {

    private val scanbotSdk = ScanbotSDK(this)
    private val documentRecognizer = scanbotSdk.genericDocumentRecognizer()

    private lateinit var cameraView: IScanbotCameraView
    private lateinit var resultTextView: TextView

    private lateinit var frameHandler: GenericDocumentRecognizerFrameHandler

    private var useFlash = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        cameraView = findViewById<ScanbotCameraXView>(R.id.cameraView)
        resultTextView = findViewById(R.id.resultTextView)
        findViewById<FinderOverlayView>(R.id.finder_overlay).setRequiredAspectRatios(listOf(FinderAspectRatio(4.0, 3.0)))

        cameraView.setPreviewMode(CameraPreviewMode.FIT_IN)

        // TODO: adjust accepted sharpness score to control the accepted blurriness of the result image
        documentRecognizer.acceptedSharpnessScore = 80f
        frameHandler = GenericDocumentRecognizerFrameHandler.attach(cameraView, documentRecognizer, true)
        frameHandler.addResultHandler(object : GenericDocumentRecognizerFrameHandler.ResultHandler {
            override fun handle(result: FrameHandlerResult<GenericDocumentRecognitionResult, SdkLicenseError>): Boolean {
                val resultText: String = when (result) {
                    is FrameHandlerResult.Success -> {
                        if (result.value.status == GenericDocumentRecognitionResult.RecognitionStatus.Success) {
                            frameHandler.isEnabled = false
                            DocumentsResultsStorage.result = result.value
                            startActivity(Intent(this@ScannerActivity, ResultActivity::class.java))
                            finish()
                        }
                        result.value.status.toString()
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