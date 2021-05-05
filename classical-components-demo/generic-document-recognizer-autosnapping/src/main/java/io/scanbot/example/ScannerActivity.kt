package io.scanbot.example

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.genericdocument.entity.GenericDocument
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.camera.CameraOpenCallback
import io.scanbot.sdk.camera.CameraPreviewMode
import io.scanbot.sdk.camera.PictureCallback
import io.scanbot.sdk.genericdocument.GenericDocumentAutoSnappingController
import io.scanbot.sdk.genericdocument.GenericDocumentRecognitionResult
import io.scanbot.sdk.genericdocument.GenericDocumentRecognizer
import io.scanbot.sdk.idcardscanner.IdScanResult
import io.scanbot.sdk.ui.camera.*

class ScannerActivity : AppCompatActivity() {

    private val scanbotSdk = ScanbotSDK(this)
    private val documentRecognizer = scanbotSdk.genericDocumentRecognizer()

    private lateinit var cameraView: IScanbotCameraView
    private lateinit var shutterButton: ShutterButton

    private lateinit var autoSnappingController: GenericDocumentAutoSnappingController

    private var useFlash = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        cameraView = findViewById<ScanbotCameraXView>(R.id.cameraView)
        findViewById<FinderOverlayView>(R.id.finder_overlay).setRequiredAspectRatios(listOf(FinderAspectRatio(4.0, 3.0)))

        cameraView.setPreviewMode(CameraPreviewMode.FIT_IN)

        // TODO: adjust accepted sharpness score to control the blurriness of the result image
        documentRecognizer.acceptedSharpnessScore = 80f
        autoSnappingController = GenericDocumentAutoSnappingController.attach(cameraView, documentRecognizer)

        cameraView.setCameraOpenCallback(object : CameraOpenCallback {
            override fun onCameraOpened() {
                cameraView.useFlash(useFlash)
                cameraView.continuousFocus()
            }
        })
        cameraView.addPictureCallback(object : PictureCallback() {
            override fun onPictureTaken(image: ByteArray, imageOrientation: Int) {
                processPictureTaken(image, imageOrientation)
            }
        })

        findViewById<Button>(R.id.flashButton).setOnClickListener { toggleFlash() }

        shutterButton = findViewById(R.id.shutterButton)
        shutterButton.setOnClickListener {
            cameraView.takePicture(false)
            shutterButton.isEnabled = false
        }
    }

    private fun processPictureTaken(image: ByteArray, imageOrientation: Int) {
        // pause autoSnappingController to stop detecting results on a preview during the recognition on the full-size picture
        autoSnappingController.isEnabled = false

        runOnUiThread {
            shutterButton.isEnabled = false
        }

        val bitmap = BitmapFactory.decodeByteArray(image, 0, image.size)
        val recognitionResult = documentRecognizer.scanBitmap(bitmap, orientation = imageOrientation)

        val isSuccess = recognitionResult != null && recognitionResult.status == GenericDocumentRecognitionResult.RecognitionStatus.Success

        if (isSuccess) {
            recognitionResult?.document?.let {
                proceedToResult(recognitionResult)
            }
        } else {
            runOnUiThread {
                Toast.makeText(this@ScannerActivity, "Error scanning: ${recognitionResult?.status}", Toast.LENGTH_SHORT).show()
                shutterButton.isEnabled = true
            }
            autoSnappingController.isEnabled = true
        }
    }

    private fun proceedToResult(result: GenericDocumentRecognitionResult) {
        DocumentsResultsStorage.result = result
        startActivity(Intent(this, ResultActivity::class.java))
        finish()
    }

    private fun toggleFlash() {
        useFlash = !useFlash
        cameraView.useFlash(useFlash)
    }
}
