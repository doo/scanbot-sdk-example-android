package io.scanbot.example

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.camera.*
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope {

    private lateinit var resultCaption: TextView
    private lateinit var cameraView: ScanbotCameraView
    private lateinit var stillImageResultsRoot: ViewGroup
    private lateinit var stillImageImageView: ImageView
    private lateinit var stillImageBlurCaption: TextView

    private var flashEnabled = false

    private val blurEstimator = ScanbotSDK(this).blurEstimator()

    private val parentJob = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + parentJob

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY)
        super.onCreate(savedInstanceState)
        supportActionBar!!.hide()
        setContentView(R.layout.activity_main)
        askPermission()

        resultCaption = findViewById(R.id.blur_estimated_result)

        cameraView = findViewById(R.id.camera_view)
        cameraView.setCameraOpenCallback(object : CameraOpenCallback {
            override fun onCameraOpened() {
                cameraView.postDelayed({
                    cameraView.useFlash(false)
                    cameraView.continuousFocus()
                }, 700)
            }
        })
        cameraView.addFrameHandler(blurFrameHandler)

        stillImageResultsRoot = findViewById(R.id.still_image_root)
        stillImageImageView = findViewById(R.id.still_image_image_view)
        stillImageBlurCaption = findViewById(R.id.still_image_blur_caption)

        findViewById<Button>(R.id.flash_button).setOnClickListener { toggleFlash() }
        findViewById<Button>(R.id.gallery_button).setOnClickListener { openGallery() }
        findViewById<Button>(R.id.still_image_close).setOnClickListener { closeStillImageResults() }
    }

    private val blurFrameHandler = object : FrameHandler {

        var isEnabled = AtomicBoolean(true)

        @Synchronized
        override fun handleFrame(previewFrame: FrameHandler.Frame): Boolean {
            if (isEnabled.get()) {
                val blurValue = blurEstimator.estimate(
                    previewFrame.frame, previewFrame.width, previewFrame.height,
                    previewFrame.frameOrientation
                )

                runOnUiThread {
                    resultCaption.text = String.format(BLURRINESS_CAPTION_FORMAT, blurValue)
                }
            }

            return false
        }
    }

    private fun estimateOnStillImage(imageUri: Uri) {
        cameraView.stopPreview()
        blurFrameHandler.isEnabled.set(false)
        stillImageResultsRoot.visibility = View.VISIBLE
        calculateForImage(imageUri)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PHOTOLIB_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val imageUri = data!!.data
            estimateOnStillImage(imageUri)
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

    private fun toggleFlash() {
        flashEnabled = !flashEnabled
        cameraView.useFlash(flashEnabled)
    }

    private fun closeStillImageResults() {
        stillImageImageView.setImageBitmap(null)
        stillImageBlurCaption.text = ""
        cameraView.startPreview()
        blurFrameHandler.isEnabled.set(true)
        stillImageResultsRoot.visibility = View.GONE
    }

    private fun openGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        startActivityForResult(
            Intent.createChooser(intent, "Select picture"), PHOTOLIB_REQUEST_CODE
        )
    }

    private fun errorToast() {
        runOnUiThread {
            Toast.makeText(this, "Error detecting blur", Toast.LENGTH_SHORT).show()
        }
    }

    private fun askPermission() {
        if (checkPermissionNotGranted(Manifest.permission.READ_EXTERNAL_STORAGE) ||
                checkPermissionNotGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                checkPermissionNotGranted(Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA), 999)
        }
    }

    private fun calculateForImage(imageUri: Uri) {
        launch {
            val bitmap: Bitmap =
                MediaStore.Images.Media.getBitmap(contentResolver, imageUri)

            withContext(Dispatchers.Main) {
                stillImageImageView.setImageBitmap(bitmap)
            }
            val result = blurEstimator.estimateInBitmap(bitmap, 0)
            withContext(Dispatchers.Main) {
                stillImageBlurCaption.text = String.format(BLURRINESS_CAPTION_FORMAT, result)
            }
        }
    }

    override fun onDestroy() {
        parentJob.cancel()
        super.onDestroy()
    }

    private fun checkPermissionNotGranted(permission: String) =
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED

    private companion object {
        private const val PHOTOLIB_REQUEST_CODE = 5712
        private const val BLURRINESS_CAPTION_FORMAT = "Image blurriness: %.2f"
    }
}
