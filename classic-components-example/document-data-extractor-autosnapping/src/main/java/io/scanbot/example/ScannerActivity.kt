package io.scanbot.example

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.camera.CameraPreviewMode
import io.scanbot.sdk.camera.CaptureInfo
import io.scanbot.sdk.camera.PictureCallback
import io.scanbot.sdk.common.AspectRatio
import io.scanbot.sdk.documentdata.DocumentDataAutoSnappingController
import io.scanbot.sdk.documentdata.DocumentDataExtractionResult
import io.scanbot.sdk.documentdata.DocumentDataExtractionStatus
import io.scanbot.sdk.documentdata.DocumentDataExtractor
import io.scanbot.sdk.ui.camera.FinderOverlayView
import io.scanbot.sdk.ui.camera.IScanbotCameraView
import io.scanbot.sdk.ui.camera.ScanbotCameraXView
import io.scanbot.sdk.ui.camera.ShutterButton

class ScannerActivity : AppCompatActivity() {
    private lateinit var dataExtractor: DocumentDataExtractor

    private lateinit var cameraView: IScanbotCameraView
    private lateinit var shutterButton: ShutterButton

    private lateinit var autoSnappingController: DocumentDataAutoSnappingController

    private var useFlash = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        val scanbotSdk = ScanbotSDK(this)
        dataExtractor = scanbotSdk.createDocumentDataExtractor()

        cameraView = findViewById<ScanbotCameraXView>(R.id.cameraView)
        findViewById<FinderOverlayView>(R.id.finder_overlay).setRequiredAspectRatios(listOf(
            AspectRatio(4.0, 3.0)
        ))

        cameraView.setPreviewMode(CameraPreviewMode.FIT_IN)

        autoSnappingController = DocumentDataAutoSnappingController.attach(cameraView, dataExtractor)

        cameraView.setCameraOpenCallback {
            cameraView.useFlash(useFlash)
            cameraView.continuousFocus()
        }
        cameraView.addPictureCallback(object : PictureCallback() {
            override fun onPictureTaken(image: ByteArray, captureInfo: CaptureInfo) {
                processPictureTaken(image, captureInfo.imageOrientation)
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
        val recognitionResult = dataExtractor.extractFromBitmap(bitmap, orientation = imageOrientation)

        val isSuccess = recognitionResult != null && recognitionResult.status == DocumentDataExtractionStatus.SUCCESS

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

    private fun proceedToResult(result: DocumentDataExtractionResult) {
        DocumentsResultsStorage.result = result
        startActivity(Intent(this, ResultActivity::class.java))
        finish()
    }

    private fun toggleFlash() {
        useFlash = !useFlash
        cameraView.useFlash(useFlash)
    }
}
