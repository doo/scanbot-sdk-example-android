package io.scanbot.example

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.camera.CameraPreviewMode
import io.scanbot.sdk.camera.CaptureInfo
import io.scanbot.sdk.camera.FrameHandlerResult
import io.scanbot.sdk.camera.PictureCallback
import io.scanbot.sdk.check.CheckRecognizer
import io.scanbot.sdk.contourdetector.ContourDetectorFrameHandler
import io.scanbot.sdk.contourdetector.DocumentAutoSnappingController
import io.scanbot.sdk.core.contourdetector.ContourDetector
import io.scanbot.sdk.process.CropOperation
import io.scanbot.sdk.process.ImageProcessor
import io.scanbot.sdk.ui.PolygonView
import io.scanbot.sdk.ui.camera.ScanbotCameraXView

class AutoSnappingCheckRecognizerActivity : AppCompatActivity() {
    private lateinit var cameraView: ScanbotCameraXView
    private lateinit var polygonView: PolygonView
    private lateinit var resultView: TextView

    private lateinit var contourDetectorFrameHandler: ContourDetectorFrameHandler
    private lateinit var autoSnappingController: DocumentAutoSnappingController

    private lateinit var contourDetector: ContourDetector
    private lateinit var checkRecognizer: CheckRecognizer
    private lateinit var imageProcessor: ImageProcessor

    private var flashEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_autosnapping_check_recognizer)
        cameraView = findViewById<ScanbotCameraXView>(R.id.camera).also { cameraView ->
            cameraView.setPreviewMode(CameraPreviewMode.FIT_IN)
            cameraView.setCameraOpenCallback {
                cameraView.postDelayed({
                    cameraView.useFlash(flashEnabled)
                    cameraView.continuousFocus()
                }, 700)
            }
        }

        resultView = findViewById<View>(R.id.result) as TextView
        val scanbotSDK = ScanbotSDK(this)

        checkRecognizer = scanbotSDK.createCheckRecognizer()
        contourDetector = scanbotSDK.createContourDetector()
        imageProcessor = scanbotSDK.imageProcessor()

        polygonView = findViewById<View>(R.id.polygonView) as PolygonView

        contourDetectorFrameHandler = ContourDetectorFrameHandler.attach(cameraView, contourDetector)

        contourDetectorFrameHandler.setAcceptedAngleScore(60.0)
        contourDetectorFrameHandler.setAcceptedSizeScore(75.0)

        contourDetectorFrameHandler.addResultHandler(polygonView.contourDetectorResultHandler)
        autoSnappingController = DocumentAutoSnappingController.attach(cameraView, contourDetectorFrameHandler)
        autoSnappingController.setIgnoreBadAspectRatio(true)

        // Please note: https://docs.scanbot.io/document-scanner-sdk/android/features/document-scanner/ui-components/#sensitivity
        autoSnappingController.setSensitivity(0.85f)

        cameraView.addPictureCallback(object : PictureCallback() {
            override fun onPictureTaken(image: ByteArray, captureInfo: CaptureInfo) {
                contourDetectorFrameHandler.isEnabled = false
                processPictureTaken(image)
                runOnUiThread {
                    polygonView.visibility = View.GONE
                }
            }
        })

        findViewById<View>(R.id.flash).setOnClickListener {
            flashEnabled = !flashEnabled
            cameraView.useFlash(flashEnabled)
        }

        contourDetectorFrameHandler.addResultHandler {
            if (it !is FrameHandlerResult.Success) {
                if (!scanbotSDK.isLicenseActive) {
                    contourDetectorFrameHandler.isEnabled = false
                    runOnUiThread {
                        Toast.makeText(
                            this,
                            "License is expired",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    }
                }
            }
            false
        }
    }

    private fun processPictureTaken(image: ByteArray) {
        val options = BitmapFactory.Options()
        val originalBitmap = BitmapFactory.decodeByteArray(image, 0, image.size, options)

        val result = contourDetector.detect(originalBitmap)

        result?.polygonF?.let { polygon ->
            imageProcessor.processBitmap(
                originalBitmap,
                listOf(CropOperation(polygon))
            )?.let { documentImage ->
                // documentImage will be recycled inside recognizeCheckBitmap
                val imageCopy = Bitmap.createBitmap(documentImage)
                val checkResult = checkRecognizer.recognizeBitmap(documentImage, 0)
                if (checkResult?.check != null) {
                    CheckRecognizerResultActivity.tempDocumentImage = imageCopy
                    startActivity(CheckRecognizerResultActivity.newIntent(this, checkResult))
                } else {
                    runOnUiThread {
                        Toast.makeText(
                            this,
                            "Check is not recognized - please, try agian",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        // continue scanning
        cameraView.postDelayed({
            cameraView.continuousFocus()
            cameraView.startPreview()
            contourDetectorFrameHandler.isEnabled = true
            polygonView.visibility = View.VISIBLE
        }, 1000)
    }

    companion object {
        @JvmStatic
        fun newIntent(context: Context?): Intent {
            return Intent(context, AutoSnappingCheckRecognizerActivity::class.java)
        }
    }
}
