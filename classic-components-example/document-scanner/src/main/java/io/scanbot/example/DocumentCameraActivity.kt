package io.scanbot.example

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import io.scanbot.common.onSuccess


import io.scanbot.example.common.applyEdgeToEdge
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.camera.CaptureInfo
import io.scanbot.sdk.document.DocumentScannerFrameHandler
import io.scanbot.sdk.document.ui.DocumentScannerView
import io.scanbot.sdk.document.ui.IDocumentScannerViewCallback
import io.scanbot.sdk.documentscanner.DocumentDetectionStatus
import io.scanbot.sdk.documentscanner.DocumentScanner
import io.scanbot.sdk.image.ImageRef
import io.scanbot.sdk.process.ImageProcessor
import io.scanbot.sdk.ui.camera.ShutterButton
import io.scanbot.sdk.ui.view.base.configuration.CameraOrientationMode

class DocumentCameraActivity : AppCompatActivity() {

    private var lastUserGuidanceHintTs = 0L
    private var flashEnabled = false
    private var autoSnappingEnabled = true
    private val ignoreOrientationMistmatch = true

    private lateinit var documentScannerView: DocumentScannerView

    private lateinit var resultView: ImageView
    private lateinit var userGuidanceHint: TextView
    private lateinit var autoSnappingToggleButton: Button
    private lateinit var shutterButton: ShutterButton

    private lateinit var scanbotSdk: ScanbotSDK
    private lateinit var documentScanner: DocumentScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        askPermission()
        supportActionBar!!.hide()
        applyEdgeToEdge(findViewById(R.id.root_view))

        scanbotSdk = ScanbotSDK(this)
        documentScanner = scanbotSdk.createDocumentScanner().getOrThrow()

        documentScannerView = findViewById(R.id.document_scanner_view)

        resultView = findViewById<View>(R.id.result) as ImageView

        documentScannerView.polygonConfiguration.apply {
            setPolygonFillColor(POLYGON_FILL_COLOR)
            setPolygonFillColorOK(POLYGON_FILL_COLOR_OK)
        }

        documentScannerView.apply {
            initCamera()
            initScanningBehavior(
                documentScanner,
                { result, frame ->
                    // Here you are continuously notified about document scanning results.
                    // For example, you can show a user guidance text depending on the current scanning status.
                    result.onSuccess { data ->
                        userGuidanceHint.post {
                            showUserGuidance(data.detectionStatus)
                        }
                    }
                    false // typically you need to return false
                },
                object : IDocumentScannerViewCallback {
                    override fun onCameraOpen() {
                        // In this example we demonstrate how to lock the orientation of the UI (Activity)
                        // as well as the orientation of the taken picture to portrait.
                        documentScannerView.cameraConfiguration.setCameraOrientationMode(
                            CameraOrientationMode.PORTRAIT
                        )

                        documentScannerView.viewController.useFlash(flashEnabled)
                    }

                    override fun onPictureTaken(image: ImageRef, captureInfo: CaptureInfo) {
                        processPictureTaken(image, captureInfo.imageOrientation)

                        // continue scanning
                        documentScannerView.postDelayed({
                            documentScannerView.viewController.startPreview()
                        }, 1000)
                    }
                }
            )

            // See https://docs.scanbot.io/document-scanner-sdk/android/features/document-scanner/using-scanbot-camera-view/#preview-mode
            // cameraConfiguration.setCameraPreviewMode(io.scanbot.sdk.camera.CameraPreviewMode.FIT_IN)
        }

        documentScannerView.viewController.apply {
            setAcceptedAngleScore(60.0)
            setAcceptedSizeScore(75.0)
            setIgnoreOrientationMismatch(ignoreOrientationMistmatch)

            // Please note: https://docs.scanbot.io/document-scanner-sdk/android/features/document-scanner/autosnapping/#sensitivity
            setAutoSnappingSensitivity(0.85f)
        }

        userGuidanceHint = findViewById(R.id.userGuidanceHint)

        shutterButton = findViewById(R.id.shutterButton)
        shutterButton.setOnClickListener { documentScannerView.viewController.takePicture(false) }
        shutterButton.visibility = View.VISIBLE

        findViewById<View>(R.id.flashToggle).setOnClickListener {
            flashEnabled = !flashEnabled
            documentScannerView.viewController.useFlash(flashEnabled)
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

    override fun onResume() {
        super.onResume()
        documentScannerView.viewController.onResume()
    }

    override fun onPause() {
        super.onPause()
        documentScannerView.viewController.onPause()
    }

    private fun showUserGuidance(result: DocumentDetectionStatus) {
        if (!autoSnappingEnabled) {
            return
        }
        if (System.currentTimeMillis() - lastUserGuidanceHintTs < 400) {
            return
        }

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
                } else {
                    userGuidanceHint.text = "Wrong aspect ratio.\nRotate your device."
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

    private fun processPictureTaken(image: ImageRef, imageOrientation: Int) {

        // Run document scanning on original image:
        val result = documentScanner.run(image).getOrNull()
        val polygon =
            result?.pointsNormalized ?: throw IllegalStateException("No document detected")

        val documentImage =
            ImageProcessor(image).resize(200).crop(polygon).processedBitmap().getOrNull()
        resultView.post { resultView.setImageBitmap(documentImage) }
    }

    private fun setAutoSnapEnabled(enabled: Boolean) {
        documentScannerView.viewController.apply {
            autoSnappingEnabled = enabled
            isFrameProcessingEnabled = enabled
        }
        documentScannerView.polygonConfiguration.setPolygonViewVisible(enabled)

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
