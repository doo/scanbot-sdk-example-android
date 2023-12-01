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
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.SdkLicenseError
import io.scanbot.sdk.camera.CaptureInfo
import io.scanbot.sdk.camera.FrameHandlerResult
import io.scanbot.sdk.camera.PictureCallback
import io.scanbot.sdk.contourdetector.ContourDetectorFrameHandler
import io.scanbot.sdk.contourdetector.ContourDetectorFrameHandler.DetectedFrame
import io.scanbot.sdk.contourdetector.DocumentAutoSnappingController
import io.scanbot.sdk.core.contourdetector.ContourDetector
import io.scanbot.sdk.core.contourdetector.DetectionStatus
import io.scanbot.sdk.process.CropOperation
import io.scanbot.sdk.process.ImageProcessor
import io.scanbot.sdk.ui.PolygonView
import io.scanbot.sdk.ui.camera.ScanbotCameraXView
import io.scanbot.sdk.ui.camera.ShutterButton

class MainActivity : AppCompatActivity(), ContourDetectorFrameHandler.ResultHandler {
    private lateinit var cameraView: ScanbotCameraXView
    private lateinit var polygonView: PolygonView
    private lateinit var resultView: ImageView
    private lateinit var userGuidanceHint: TextView
    private lateinit var autoSnappingToggleButton: Button
    private lateinit var shutterButton: ShutterButton

    private lateinit var contourDetectorFrameHandler: ContourDetectorFrameHandler
    private lateinit var autoSnappingController: DocumentAutoSnappingController

    private lateinit var scanbotSDK: ScanbotSDK
    private lateinit var contourDetector: ContourDetector
    private lateinit var imageProcessor: ImageProcessor

    private var lastUserGuidanceHintTs = 0L
    private var flashEnabled = false
    private var autoSnappingEnabled = true
    private val ignoreBadAspectRatio = true

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY)
        super.onCreate(savedInstanceState)
        askPermission()
        setContentView(R.layout.activity_main)
        supportActionBar!!.hide()

        scanbotSDK = ScanbotSDK(this)
        contourDetector = scanbotSDK.createContourDetector()
        imageProcessor = scanbotSDK.imageProcessor()

        cameraView = findViewById<View>(R.id.camera) as ScanbotCameraXView

        // In this example we demonstrate how to lock the orientation of the UI (Activity)
        // as well as the orientation of the taken picture to portrait.
        cameraView.lockToPortrait(true)

        // See https://docs.scanbot.io/document-scanner-sdk/android/features/document-scanner/ui-components/#preview-mode
        //cameraView.setPreviewMode(io.scanbot.sdk.camera.CameraPreviewMode.FIT_IN);

        cameraView.setCameraOpenCallback {
            cameraView.postDelayed({
                // Shutter sound is ON by default. You can disable it:
                // cameraView.setShutterSound(false);

                cameraView.continuousFocus()
                cameraView.useFlash(flashEnabled)
            }, 700)
        }
        resultView = findViewById<View>(R.id.result) as ImageView

        polygonView = findViewById<View>(R.id.polygonView) as PolygonView
        polygonView.setFillColor(POLYGON_FILL_COLOR)
        polygonView.setFillColorOK(POLYGON_FILL_COLOR_OK)

        contourDetectorFrameHandler = ContourDetectorFrameHandler.attach(cameraView, contourDetector)

        // Please note: https://docs.scanbot.io/document-scanner-sdk/android/features/document-scanner/ui-components/#contour-detection-parameters
        contourDetectorFrameHandler.setAcceptedAngleScore(60.0)
        contourDetectorFrameHandler.setAcceptedSizeScore(75.0)
        contourDetectorFrameHandler.addResultHandler(polygonView.contourDetectorResultHandler)
        contourDetectorFrameHandler.addResultHandler(this)
        contourDetectorFrameHandler.setIgnoreBadAspectRatio(ignoreBadAspectRatio)

        autoSnappingController = DocumentAutoSnappingController.attach(cameraView, contourDetectorFrameHandler)

        // Please note: https://docs.scanbot.io/document-scanner-sdk/android/features/document-scanner/ui-components/#sensitivity
        autoSnappingController.setSensitivity(0.85f)

        cameraView.addPictureCallback(object : PictureCallback() {
            override fun onPictureTaken(image: ByteArray, captureInfo: CaptureInfo) {
                processPictureTaken(image, captureInfo.imageOrientation)
            }
        })
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 999)
        }
    }

    override fun handle(result: FrameHandlerResult<DetectedFrame, SdkLicenseError>): Boolean {
        // Here you are continuously notified about contour detection results.
        // For example, you can show a user guidance text depending on the current detection status.
        userGuidanceHint.post {
            if (result is FrameHandlerResult.Success<*>) {
                showUserGuidance((result as FrameHandlerResult.Success<DetectedFrame>).value.detectionStatus)
            }
        }
        return false // typically you need to return false
    }

    private fun showUserGuidance(result: DetectionStatus) {
        if (!autoSnappingEnabled) {
            return
        }
        if (System.currentTimeMillis() - lastUserGuidanceHintTs < 400) {
            return
        }

        // Make sure to reset the default polygon fill color (see the ignoreBadAspectRatio case).
        polygonView.setFillColor(POLYGON_FILL_COLOR)
        when (result) {
            DetectionStatus.OK -> {
                userGuidanceHint.text = "Don't move"
                userGuidanceHint.visibility = View.VISIBLE
            }
            DetectionStatus.OK_BUT_TOO_SMALL -> {
                userGuidanceHint.text = "Move closer"
                userGuidanceHint.visibility = View.VISIBLE
            }
            DetectionStatus.OK_BUT_BAD_ANGLES -> {
                userGuidanceHint.text = "Perspective"
                userGuidanceHint.visibility = View.VISIBLE
            }
            DetectionStatus.ERROR_NOTHING_DETECTED -> {
                userGuidanceHint.text = "No Document"
                userGuidanceHint.visibility = View.VISIBLE
            }
            DetectionStatus.ERROR_TOO_NOISY -> {
                userGuidanceHint.text = "Background too noisy"
                userGuidanceHint.visibility = View.VISIBLE
            }
            DetectionStatus.OK_BUT_BAD_ASPECT_RATIO -> {
                if (ignoreBadAspectRatio) {
                    userGuidanceHint.text = "Don't move"
                    // change polygon color to "OK"
                    polygonView.setFillColor(POLYGON_FILL_COLOR_OK)
                } else {
                    userGuidanceHint.text = "Wrong aspect ratio.\n Rotate your device."
                }
                userGuidanceHint.visibility = View.VISIBLE
            }
            DetectionStatus.ERROR_TOO_DARK -> {
                userGuidanceHint.text = "Poor light"
                userGuidanceHint.visibility = View.VISIBLE
            }
            else -> userGuidanceHint.visibility = View.GONE
        }
        lastUserGuidanceHintTs = System.currentTimeMillis()
    }

    private fun processPictureTaken(image: ByteArray, imageOrientation: Int) {
        // Here we get the full image from the camera.
        // Please see https://docs.scanbot.io/document-scanner-sdk/android/features/document-scanner/ui-components/#handling-camera-picture
        // This is just a demo showing the detected document image as a downscaled(!) preview image.

        // Decode Bitmap from bytes of original image:
        val options = BitmapFactory.Options()
        // Please note: In this simple demo we downscale the original image to 1/8 for the preview!
        options.inSampleSize = 8
        // Typically you will need the full resolution of the original image! So please change the "inSampleSize" value to 1!
        //options.inSampleSize = 1;
        var originalBitmap = BitmapFactory.decodeByteArray(image, 0, image.size, options)

        // Rotate the original image based on the imageOrientation value.
        // Required for some Android devices like Samsung!
        if (imageOrientation > 0) {
            val matrix = Matrix()
            matrix.setRotate(imageOrientation.toFloat(), originalBitmap.width / 2f, originalBitmap.height / 2f)
            originalBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, false)
        }
        // Run document detection on original image:
        val detectedPolygon = contourDetector.detect(originalBitmap)!!.polygonF

        val documentImage = imageProcessor.processBitmap(originalBitmap, CropOperation(detectedPolygon))
        resultView.post { resultView.setImageBitmap(documentImage) }

        // continue scanning
        cameraView.postDelayed({
            cameraView.continuousFocus()
            cameraView.startPreview()
        }, 1000)
    }

    private fun setAutoSnapEnabled(enabled: Boolean) {
        autoSnappingController.isEnabled = enabled
        contourDetectorFrameHandler.isEnabled = enabled
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