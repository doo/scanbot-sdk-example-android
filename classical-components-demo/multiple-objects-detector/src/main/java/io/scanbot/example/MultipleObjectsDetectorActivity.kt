package io.scanbot.example

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.camera.CameraOpenCallback
import io.scanbot.sdk.camera.PictureCallback
import io.scanbot.sdk.camera.ScanbotCameraView
import io.scanbot.sdk.core.contourdetector.DetectionResult
import io.scanbot.sdk.multipleobjects.MultipleObjectsFrameHandler
import io.scanbot.sdk.persistence.Page
import io.scanbot.sdk.process.ImageFilterType
import io.scanbot.sdk.ui.camera.ShutterButton
import io.scanbot.sdk.ui.multipleobjects.MultiplePolygonsView

import java.util.*

class MultipleObjectsDetectorActivity : AppCompatActivity(), PictureCallback {

    private lateinit var cameraView: ScanbotCameraView
    private lateinit var progressView: ProgressBar

    private var flashEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_multiple_objects_detector)
        supportActionBar!!.hide()

        askPermission()
        setupUi()
        setupObjectDetector()
    }

    private fun askPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.CAMERA), PERMISSION_CAMERA_REQUEST_CODE)
        }
    }

    private fun setupUi() {
        progressView = findViewById(R.id.progressView)

        cameraView = findViewById(R.id.camera)
        cameraView.setCameraOpenCallback(CameraOpenCallback {
            cameraView.postDelayed({
                cameraView.continuousFocus()
                cameraView.useFlash(flashEnabled)
            }, 700)
        })
        cameraView.addPictureCallback(this)

        val shutterButton = findViewById<ShutterButton>(R.id.snap)
        shutterButton.setOnClickListener { cameraView.takePicture(false) }
        shutterButton.post { shutterButton.showAutoButton() }

        findViewById<View>(R.id.flash).setOnClickListener {
            flashEnabled = !flashEnabled
            cameraView.useFlash(flashEnabled)
        }
    }

    private fun setupObjectDetector() {
        val scanbotSdk = ScanbotSDK(this)
        val multipleObjectsDetector = scanbotSdk.multipleObjectsDetector()

        // minAspectRatio and maxAspectRatio params below specify the range of aspect ratio of desired recognized object
        // Business card standard is 8,9cm X 5,1cm (3.5" x 2") making aspect ratio to be ~ 1,74
        //multipleObjectsDetector.setParams(MultipleObjectsDetector.Params(1.6f, 1.8f))

        val businessCardsFrameHandler = MultipleObjectsFrameHandler.attach(cameraView, multipleObjectsDetector)

        val polygonView = findViewById<MultiplePolygonsView>(R.id.polygonView)
        businessCardsFrameHandler.addResultHandler(polygonView.multipleObjectDetectorHandler)
    }

    override fun onPictureTaken(image: ByteArray, imageOrientation: Int) {
        Log.i(LOG_TAG, "initial imageOrientation: $imageOrientation")
        cameraView.post { progressView.visibility = View.VISIBLE }

        val bitmap = BitmapFactory.decodeByteArray(image, 0, image.size)
        val matrix = Matrix().apply {
            setRotate(imageOrientation.toFloat(), bitmap.width / 2f, bitmap.height / 2f)
        }
        val resultBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false)

        val scanbotSDK = ScanbotSDK(this)
        val pageProcessor = scanbotSDK.pageProcessor()
        val pageFileStorage = scanbotSDK.pageFileStorage()
        val multipleObjectsDetector = scanbotSDK.multipleObjectsDetector()

        val polygons = multipleObjectsDetector.detectOnBitmap(resultBitmap)
        val detectedObjectsPages = polygons.map { polygon ->
            val pageId = pageFileStorage.add(resultBitmap)
            val page = Page(pageId, Collections.emptyList(), DetectionResult.OK, ImageFilterType.NONE)
            pageProcessor.cropAndRotate(page, 0, polygon.polygonF)
        }

        cameraView.post {
            startActivity(MultiObjectsDetectorResultsActivity.newIntent(this, detectedObjectsPages))
            progressView.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        cameraView.onResume()
    }

    override fun onPause() {
        super.onPause()
        cameraView.onPause()
    }

    private companion object {

        private const val LOG_TAG = "MultipleObjectsDetector"
        private const val PERMISSION_CAMERA_REQUEST_CODE = 999
    }
}
