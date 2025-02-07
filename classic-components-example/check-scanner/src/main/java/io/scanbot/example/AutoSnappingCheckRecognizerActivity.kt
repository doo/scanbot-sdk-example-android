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
import io.scanbot.sdk.check.CheckScanner
import io.scanbot.sdk.document.DocumentScanner
import io.scanbot.sdk.document.DocumentAutoSnappingController
import io.scanbot.sdk.document.DocumentScannerFrameHandler
import io.scanbot.sdk.process.ImageProcessor
import io.scanbot.sdk.ui.PolygonView
import io.scanbot.sdk.ui.camera.ScanbotCameraXView

class AutoSnappingCheckScannerActivity : AppCompatActivity() {
    private lateinit var cameraView: ScanbotCameraXView
    private lateinit var polygonView: PolygonView
    private lateinit var resultView: TextView

    private lateinit var contourDetectorFrameHandler: DocumentScannerFrameHandler
    private lateinit var autoSnappingController: DocumentAutoSnappingController

    private lateinit var contourDetector: DocumentScanner
    private lateinit var checkRecognizer: CheckScanner

    private var flashEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_autosnapping_check_scanner)
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

        checkRecognizer = scanbotSDK.createCheckScanner()
        contourDetector = scanbotSDK.createDocumentScanner()

        polygonView = findViewById<View>(R.id.polygonView) as PolygonView

        contourDetectorFrameHandler =
            DocumentScannerFrameHandler.attach(cameraView, contourDetector)

        contourDetectorFrameHandler.setAcceptedAngleScore(60.0)
        contourDetectorFrameHandler.setAcceptedSizeScore(75.0)
        contourDetectorFrameHandler.setIgnoreBadAspectRatio(true)

        contourDetectorFrameHandler.addResultHandler(polygonView.documentScannerResultHandler)
        autoSnappingController =
            DocumentAutoSnappingController.attach(cameraView, contourDetectorFrameHandler)

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
                if (!scanbotSDK.licenseInfo.isValid) {
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

        val result = contourDetector.scanFromBitmap(originalBitmap)

        result?.pointsNormalized?.let { polygon ->
            ImageProcessor(originalBitmap).crop(polygon).processedBitmap()
                ?.let { documentImage ->
                    // documentImage will be recycled inside recognizeCheckBitmap
                    val imageCopy = Bitmap.createBitmap(documentImage)
                    val checkResult = checkRecognizer.scanFromBitmap(documentImage, 0)
                    if (checkResult?.check != null) {
                        CheckScannerResultActivity.tempDocumentImage = imageCopy
                        startActivity(CheckScannerResultActivity.newIntent(this, checkResult))
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
            return Intent(context, AutoSnappingCheckScannerActivity::class.java)
        }
    }
}
