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


import io.scanbot.example.common.applyEdgeToEdge
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.camera.CameraPreviewMode
import io.scanbot.sdk.camera.CaptureInfo
import io.scanbot.sdk.camera.FrameHandlerResult
import io.scanbot.sdk.camera.PictureCallback
import io.scanbot.sdk.document.DocumentAutoSnappingController
import io.scanbot.sdk.document.DocumentScannerFrameHandler
import io.scanbot.sdk.image.ImageRef
import io.scanbot.sdk.ui.PolygonView
import io.scanbot.sdk.ui.camera.ScanbotCameraXView

class AutoSnappingCheckScannerActivity : AppCompatActivity() {
    private lateinit var scanbotSDK: ScanbotSDK
    private lateinit var cameraView: ScanbotCameraXView
    private lateinit var polygonView: PolygonView
    private lateinit var resultView: TextView

    private lateinit var frameHandler: DocumentScannerFrameHandler
    private lateinit var autoSnappingController: DocumentAutoSnappingController

    private var flashEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_autosnapping_check_scanner)
        supportActionBar!!.hide()
        applyEdgeToEdge(this.findViewById(R.id.root_view))

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
        scanbotSDK = ScanbotSDK(this)

        val documentScanner = scanbotSDK.createDocumentScanner().getOrThrow()

        polygonView = findViewById<View>(R.id.polygonView) as PolygonView

        frameHandler =
            DocumentScannerFrameHandler.attach(cameraView, documentScanner)

        documentScanner.setConfiguration(documentScanner.copyCurrentConfiguration().apply {
            parameters.apply {
                this.ignoreOrientationMismatch = true
                this.acceptedSizeScore = 75
                this.acceptedAngleScore = 60
            }
        })

        frameHandler.addResultHandler(polygonView.documentScannerResultHandler)
        autoSnappingController =
            DocumentAutoSnappingController.attach(cameraView, frameHandler)

        // Please note: https://docs.scanbot.io/document-scanner-sdk/android/features/document-scanner/ui-components/#sensitivity
        autoSnappingController.setSensitivity(0.85f)

        cameraView.addPictureCallback(object : PictureCallback() {
            override fun onPictureTaken(image: ImageRef, captureInfo: CaptureInfo) {
                frameHandler.isEnabled = false
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

        frameHandler.addResultHandler {
            if (it !is FrameHandlerResult.Success) {
                if (!scanbotSDK.licenseInfo.isValid) {
                    frameHandler.isEnabled = false
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

    private fun processPictureTaken(image: ImageRef) {

        val checkScanner = scanbotSDK.createCheckScanner().getOrThrow()
        val checkResult = checkScanner.run(image).getOrNull()
        if (checkResult?.check != null) {
            CheckScannerResultActivity.tempDocumentImage = checkResult.croppedImage?.toBitmap()?.getOrNull()
            startActivity(CheckScannerResultActivity.newIntent(this, checkResult))
        } else {
            runOnUiThread {
                Toast.makeText(
                    this,
                    "Check is not found - please, try agian",
                    Toast.LENGTH_LONG
                ).show()
            }
        }


        // continue scanning
        cameraView.postDelayed({
            cameraView.continuousFocus()
            cameraView.startPreview()
            frameHandler.isEnabled = true
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
