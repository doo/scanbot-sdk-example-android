package io.scanbot.example

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.example.common.applyEdgeToEdge
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.camera.CameraPreviewMode
import io.scanbot.sdk.camera.FrameHandlerResult
import io.scanbot.sdk.common.AspectRatio
import io.scanbot.sdk.documentdata.DocumentDataExtractionMode
import io.scanbot.sdk.documentdata.DocumentDataExtractionStatus
import io.scanbot.sdk.documentdata.DocumentDataExtractor
import io.scanbot.sdk.documentdata.DocumentDataExtractorFrameHandler
import io.scanbot.sdk.ui.camera.*

class ScannerActivity : AppCompatActivity() {
    private lateinit var cameraView: IScanbotCameraView
    private lateinit var resultTextView: TextView

    private lateinit var frameHandler: DocumentDataExtractorFrameHandler

    private lateinit var dataExtractor: DocumentDataExtractor

    private var useFlash = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)
        supportActionBar!!.hide()
        applyEdgeToEdge(this.findViewById(R.id.root_view))

        cameraView = findViewById<ScanbotCameraXView>(R.id.cameraView)
        resultTextView = findViewById(R.id.resultTextView)
        findViewById<FinderOverlayView>(R.id.finder_overlay).setRequiredAspectRatios(listOf(
            AspectRatio(4.0, 3.0)
        ))

        cameraView.setPreviewMode(CameraPreviewMode.FIT_IN)

        val scanbotSdk = ScanbotSDK(this)
        dataExtractor = scanbotSdk.createDocumentDataExtractor()

        frameHandler = DocumentDataExtractorFrameHandler.attach(cameraView, dataExtractor, DocumentDataExtractionMode.LIVE)

        frameHandler.addResultHandler { result ->
            val resultText: String = when (result) {
                is FrameHandlerResult.Success -> {
                    if (result.value.status == DocumentDataExtractionStatus.SUCCESS) {
                            frameHandler.isEnabled = false
                            DocumentsResultsStorage.result = result.value
                            startActivity(Intent(this@ScannerActivity, ResultActivity::class.java))
                            finish()
                    }
                    result.value.status.toString()
                }
                is FrameHandlerResult.Failure -> "Check your setup or license"
            }

            runOnUiThread { resultTextView.text =  resultText}

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
