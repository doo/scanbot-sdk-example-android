package io.scanbot.example

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import io.scanbot.common.Result
import io.scanbot.common.mapSuccess
import io.scanbot.common.onSuccess
import io.scanbot.example.common.applyEdgeToEdge
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.camera.CaptureInfo
import io.scanbot.sdk.camera.FrameHandler
import io.scanbot.sdk.camera.PictureCallback
import io.scanbot.sdk.document.DocumentAutoSnappingController
import io.scanbot.sdk.document.DocumentScannerFrameHandler
import io.scanbot.sdk.documentscanner.DocumentDetectionStatus
import io.scanbot.sdk.documentscanner.DocumentScanner
import io.scanbot.sdk.image.ImageRef
import io.scanbot.sdk.imageprocessing.ScanbotSdkImageProcessor
import io.scanbot.sdk.process.ImageProcessor
import io.scanbot.sdk.ui.PolygonView
import io.scanbot.sdk.ui.camera.ScanbotCameraXView
import io.scanbot.sdk.ui.camera.ShutterButton
import io.scanbot.sdk.util.PolygonHelper

/**
Ths example uses new sdk APIs presented in Scanbot SDK v.8.x.x
Please, check the official documentation for more details:
Result API https://docs.scanbot.io/android/document-scanner-sdk/detailed-setup-guide/result-api/
ImageRef API https://docs.scanbot.io/android/document-scanner-sdk/detailed-setup-guide/image-ref-api/
 */

class MainActivity : AppCompatActivity(), DocumentScannerFrameHandler.ResultHandler {
    private lateinit var cameraView: ScanbotCameraXView
    private lateinit var polygonView: PolygonView
    private lateinit var resultView: ImageView
    private lateinit var userGuidanceHint: TextView
    private lateinit var autoSnappingToggleButton: Button
    private lateinit var shutterButton: ShutterButton

    private lateinit var documentScannerFrameHandler: DocumentScannerFrameHandler
    private lateinit var autoSnappingController: DocumentAutoSnappingController


    private var lastUserGuidanceHintTs = 0L
    private var flashEnabled = false
    private var autoSnappingEnabled = true
    private val ignoreOrientationMistmatch = true
    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY)
        super.onCreate(savedInstanceState)
        askPermission()
        setContentView(R.layout.activity_main)
        supportActionBar!!.hide()

        applyEdgeToEdge(this.findViewById(R.id.root_view))

        val scanbotSDK = ScanbotSDK(this)
        scanbotSDK.createDocumentScanner().onSuccess { documentScanner ->
            documentScanner.apply {
                // Please note: https://docs.scanbot.io/document-scanner-sdk/android/features/document-scanner/ui-components/
                setConfiguration(copyCurrentConfiguration().apply {
                    parameters.apply {
                        this.ignoreOrientationMismatch = ignoreOrientationMistmatch
                        this.acceptedSizeScore = 75
                        this.acceptedAngleScore = 60
                    }
                })
            }
            documentScannerFrameHandler =
                DocumentScannerFrameHandler.attach(cameraView, documentScanner)
            cameraView.addPictureCallback(object : PictureCallback() {
                override fun onPictureTaken(image: ImageRef, captureInfo: CaptureInfo) {
                    processPictureTaken(image, documentScanner)
                }
            })

            documentScannerFrameHandler.addResultHandler(polygonView.documentScannerResultHandler)
            documentScannerFrameHandler.addResultHandler(this@MainActivity)

            autoSnappingController =
                DocumentAutoSnappingController.attach(cameraView, documentScannerFrameHandler)

            // Please note: https://docs.scanbot.io/document-scanner-sdk/android/features/document-scanner/ui-components/#sensitivity
            autoSnappingController.setSensitivity(0.85f)
        }


        cameraView = findViewById<View>(R.id.camera) as ScanbotCameraXView

        // In this example we demonstrate how to lock the orientation of the UI (Activity)
        // as well as the orientation of the taken picture to portrait.
        cameraView.lockToPortrait(true)

        // See https://docs.scanbot.io/document-scanner-sdk/android/features/document-scanner/ui-components/#preview-mode
        //cameraView.setPreviewMode(io.scanbot.sdk.camera.CameraPreviewMode.FIT_IN)

        cameraView.setCameraOpenCallback {
            cameraView.postDelayed({
                // Shutter sound is ON by default. You can disable it:
                // cameraView.setShutterSound(false)

                cameraView.continuousFocus()
                cameraView.useFlash(flashEnabled)
            }, 700)
        }
        resultView = findViewById<View>(R.id.result) as ImageView

        polygonView = findViewById<View>(R.id.polygonView) as PolygonView
        polygonView.setFillColor(POLYGON_FILL_COLOR)
        polygonView.setFillColorOK(POLYGON_FILL_COLOR_OK)


        userGuidanceHint = findViewById(R.id.userGuidanceHint)

        shutterButton = findViewById(R.id.shutterButton)
        shutterButton.setOnClickListener { cameraView.takePicture(false) }
        shutterButton.visibility = View.VISIBLE

        findViewById<View>(R.id.flashToggle).setOnClickListener {
            flashEnabled = !flashEnabled
            cameraView.useFlash(flashEnabled)
        }

        autoSnappingToggleButton = findViewById(R.id.autoSnappingToggle)
        autoSnappingToggleButton.setOnClickListener {
            autoSnappingEnabled = !autoSnappingEnabled
            setAutoSnapEnabled(autoSnappingEnabled)
        }
        autoSnappingToggleButton.post { setAutoSnapEnabled(autoSnappingEnabled) }
    }

    private fun askPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 999)
        }
    }

    override fun handle(
        result: Result<DocumentScannerFrameHandler.DetectedFrame>,
        frame: FrameHandler.Frame
    ): Boolean {
        // Here you are continuously notified about document scanning results.
        // For example, you can show a user guidance text depending on the current scanning status.
        result.onSuccess { data ->
            userGuidanceHint.post {
                showUserGuidance(data.detectionStatus)
            }
        }

        return false // typically you need to return false
    }

    private fun showUserGuidance(result: DocumentDetectionStatus) {
        if (!autoSnappingEnabled) {
            return
        }
        if (System.currentTimeMillis() - lastUserGuidanceHintTs < 400) {
            return
        }

        // Make sure to reset the default polygon fill color (see the ignoreBadAspectRatio case).
        polygonView.setFillColor(POLYGON_FILL_COLOR)
        when (result) {
            DocumentDetectionStatus.OK -> {
                userGuidanceHint.text = "Don't move"
                userGuidanceHint.visibility = View.VISIBLE
            }

            DocumentDetectionStatus.OK_BUT_TOO_SMALL -> {
                userGuidanceHint.text = "Move closer"
                userGuidanceHint.visibility = View.VISIBLE
            }

            DocumentDetectionStatus.OK_BUT_BAD_ANGLES -> {
                userGuidanceHint.text = "Perspective"
                userGuidanceHint.visibility = View.VISIBLE
            }

            DocumentDetectionStatus.ERROR_NOTHING_DETECTED -> {
                userGuidanceHint.text = "No Document"
                userGuidanceHint.visibility = View.VISIBLE
            }

            DocumentDetectionStatus.ERROR_TOO_NOISY -> {
                userGuidanceHint.text = "Background too noisy"
                userGuidanceHint.visibility = View.VISIBLE
            }

            DocumentDetectionStatus.OK_BUT_BAD_ASPECT_RATIO -> {
                if (ignoreOrientationMistmatch) {
                    userGuidanceHint.text = "Don't move"
                    // change polygon color to "OK"
                    polygonView.setFillColor(POLYGON_FILL_COLOR_OK)
                } else {
                    userGuidanceHint.text = "Wrong aspect ratio.\n Rotate your device."
                }
                userGuidanceHint.visibility = View.VISIBLE
            }

            DocumentDetectionStatus.ERROR_TOO_DARK -> {
                userGuidanceHint.text = "Poor light"
                userGuidanceHint.visibility = View.VISIBLE
            }

            else -> userGuidanceHint.visibility = View.GONE
        }
        lastUserGuidanceHintTs = System.currentTimeMillis()
    }

    private fun processPictureTaken(image: ImageRef, documentScanner: DocumentScanner) {
        // Run document scanning on original image:
        val polygon =
            documentScanner.run(image).getOrNull()?.pointsNormalized ?: throw IllegalStateException(
                "No document detected"
            )
        val polygonCrop =
            polygon.takeIf { it.isNotEmpty() && it.size == 4 } ?: PolygonHelper.getFullPolygon()
        val documentImage = ScanbotSdkImageProcessor.create()
            .crop(image, polygonCrop)
            .mapSuccess { documentImage ->
                ScanbotSdkImageProcessor.create().resize(documentImage, 200).getOrReturn()
            }.getOrNull()

        resultView.post {
            resultView.setImageBitmap(documentImage?.toBitmap()?.getOrNull())
        }

        // continue scanning
        cameraView.postDelayed({
            cameraView.continuousFocus()
            cameraView.startPreview()
        }, 1000)
    }

    private fun setAutoSnapEnabled(enabled: Boolean) {
        autoSnappingController.isEnabled = enabled
        documentScannerFrameHandler.isEnabled = enabled
        polygonView.visibility = if (enabled) View.VISIBLE else View.GONE
        autoSnappingToggleButton.text = "Automatic ${if (enabled) "ON" else "OFF"}"
        if (enabled) {
            shutterButton.showAutoButton()
        } else {
            shutterButton.showManualButton()
            userGuidanceHint.visibility = View.GONE
        }
    }

    companion object {
        private val POLYGON_FILL_COLOR = Color.parseColor("#55ff0000")
        private val POLYGON_FILL_COLOR_OK = Color.parseColor("#4400ff00")
    }
}
