package io.scanbot.example

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import io.scanbot.common.Result
import io.scanbot.common.mapSuccess
import io.scanbot.common.onSuccess
import io.scanbot.example.common.applyEdgeToEdge
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.camera.*
import io.scanbot.sdk.document.DocumentAutoSnappingController
import io.scanbot.sdk.document.DocumentScannerFrameHandler
import io.scanbot.sdk.documentscanner.DocumentDetectionResult
import io.scanbot.sdk.documentscanner.DocumentDetectionStatus
import io.scanbot.sdk.documentscanner.DocumentScanner
import io.scanbot.sdk.documentscanner.DocumentScannerParameters
import io.scanbot.sdk.documentscanner.DocumentScanningResult
import io.scanbot.sdk.geometry.AspectRatio
import io.scanbot.sdk.image.ImageRef
import io.scanbot.sdk.imageprocessing.ScanbotSdkImageProcessor
import io.scanbot.sdk.process.ImageProcessor
import io.scanbot.sdk.ui.camera.AdaptiveFinderOverlayView
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
    private lateinit var resultView: ImageView
    private lateinit var userGuidanceHint: TextView
    private lateinit var shutterButton: ShutterButton

    private lateinit var scanbotSDK: ScanbotSDK
    private lateinit var scanner: DocumentScanner

    private var flashEnabled = false
    private var lastUserGuidanceHintTs = 0L
    private val requiredPageAspectRatios = listOf(AspectRatio(4.0, 3.0))

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY)
        super.onCreate(savedInstanceState)

        scanbotSDK = ScanbotSDK(this)
        scanner = scanbotSDK.createDocumentScanner().getOrThrow()

        askPermission()
        setContentView(R.layout.activity_main)
        supportActionBar!!.hide()
        applyEdgeToEdge(this.findViewById(R.id.root_view))

        cameraView = findViewById<View>(R.id.camera) as ScanbotCameraXView
        cameraView.setPreviewMode(CameraPreviewMode.FILL_IN)

        // Lock the orientation of the UI (Activity) as well as the orientation of the taken picture to portrait.
        cameraView.lockToPortrait(true)
        cameraView.setCameraOpenCallback {
            cameraView.postDelayed({
                // Shutter sound is ON by default. You can disable it:
                // cameraView.setShutterSound(false)

                cameraView.continuousFocus()
                cameraView.useFlash(flashEnabled)
            }, 700)
        }
        resultView = findViewById<View>(R.id.result) as ImageView

        val frameHandler = DocumentScannerFrameHandler.attach(cameraView, scanner)
        // frameHandler.setAcceptedSizeScore(70)

        val finderOverlayView = findViewById<View>(R.id.finder_overlay) as AdaptiveFinderOverlayView
        finderOverlayView.setRequiredAspectRatios(requiredPageAspectRatios)
        scanner.setConfiguration(scanner.copyCurrentConfiguration().apply {
            parameters.apply {
                this.aspectRatios = requiredPageAspectRatios
                this.ignoreOrientationMismatch = true
            }
        })
        frameHandler.addResultHandler(finderOverlayView.documentScannerFrameHandler)
        frameHandler.addResultHandler(this)

        DocumentAutoSnappingController.attach(cameraView, frameHandler).apply {
            // setSensitivity(0.4f)
        }

        cameraView.addPictureCallback(object : PictureCallback() {
            override fun onPictureTaken(image: ImageRef, captureInfo: CaptureInfo) {
                processPictureTaken(image, captureInfo.imageOrientation)
            }
        })
        userGuidanceHint = findViewById(R.id.userGuidanceHint)

        shutterButton = findViewById(R.id.shutterButton)
        shutterButton.setOnClickListener { cameraView.takePicture(false) }
        shutterButton.visibility = View.VISIBLE
        shutterButton.post { shutterButton.showAutoButton() }

        findViewById<View>(R.id.flashToggle).setOnClickListener {
            flashEnabled = !flashEnabled
            cameraView.useFlash(flashEnabled)
        }
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
        result: Result<DocumentDetectionResult>,
        frame: FrameHandler.Frame
    ): Boolean {
        // Here you are continuously notified about document scanning results.
        // For example, you can show a user guidance text depending on the current scanning status.
        result.onSuccess { value ->
            userGuidanceHint.post {
                showUserGuidance(value.status)
            }
        }

        return false // typically you need to return false
    }

    private fun showUserGuidance(result: DocumentDetectionStatus) {
        val autoSnappingEnabled = true
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

            DocumentDetectionStatus.OK_BUT_OFF_CENTER -> {
                userGuidanceHint.text = "Move to the center"
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

            DocumentDetectionStatus.ERROR_TOO_DARK -> {
                userGuidanceHint.text = "Poor light"
                userGuidanceHint.visibility = View.VISIBLE
            }

            else -> userGuidanceHint.visibility = View.GONE
        }
        lastUserGuidanceHintTs = System.currentTimeMillis()
    }

    private fun processPictureTaken(image: ImageRef, imageOrientation: Int) {

        scanner.setConfiguration(scanner.copyCurrentConfiguration().apply {
            parameters.apply {
                this.aspectRatios = requiredPageAspectRatios
                this.ignoreOrientationMismatch = true
            }
        })
        val image = scanner.run(image).mapSuccess { result ->
            val polygon = result.pointsNormalized
            polygon.takeIf { it.isNotEmpty() && it.size == 4 } ?: PolygonHelper.getFullPolygon()
        }.mapSuccess { polygonCrop ->
            var documentImage = ScanbotSdkImageProcessor.create()
                .crop(image, polygonCrop)
                .getOrReturn()
            ScanbotSdkImageProcessor.create().resize(documentImage, 200).getOrReturn()
        }.onSuccess { documentImage ->
            resultView.post {
                resultView.setImageBitmap(documentImage.toBitmap().getOrNull())
                documentImage.close()
                cameraView.continuousFocus()
                cameraView.startPreview()
            }
        }
    }
}
