package io.scanbot.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import io.scanbot.example.common.ImportImageContract
import io.scanbot.example.common.showToast
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.util.PolygonHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var progressBar: View
    private lateinit var importResultImage: ImageView

    private val scanbotSdk: ScanbotSDK by lazy { ScanbotSDK(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val galleryImageLauncher = registerForActivityResult(ImportImageContract(this)) { resultEntity ->
            lifecycleScope.launch(Dispatchers.Default) {
                val activity = this@MainActivity
                val sdk = ScanbotSDK(activity)
                if (!sdk.licenseInfo.isValid) {
                    withContext(Dispatchers.Main) {
                        activity.showToast("Scanbot SDK license (1-minute trial) has expired.")
                    }
                } else {
                    resultEntity?.let { bitmap ->
                        processImageForAutoDocumentDetection(bitmap)
                    }
                }
            }
        }

        progressBar = findViewById(R.id.progress_bar)
        importResultImage = findViewById(R.id.import_result)

        findViewById<View>(R.id.show_dialog_btn).setOnClickListener {
            val cameraPermission = ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.CAMERA)
            if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity, Manifest.permission.CAMERA)) {
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.CAMERA),
                        PERMISSIONS_REQUEST_CAMERA,
                    )
                }
            } else {
                openCamera()
            }
        }

        findViewById<View>(R.id.import_image).setOnClickListener {
            galleryImageLauncher.launch(Unit)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CAMERA) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            }
        }
    }

    private fun openCamera() {
        startActivity(Intent(this, DocumentCameraActivity::class.java))
    }

    /** Imports a selected image as original image and performs auto document detection on it. */
    private suspend fun processImageForAutoDocumentDetection(bitmap: Bitmap) {
        withContext(Dispatchers.Main) {
            progressBar.visibility = View.VISIBLE
            this@MainActivity.showToast("Importing page...")
        }

        val page = withContext(Dispatchers.Default) {
            // create a new Page object with given image as original image:
            val document = scanbotSdk.documentApi.createDocument()
            val page = document.addPage(bitmap)

            // run contour detection on the image:
            val detectionResult = scanbotSdk.createContourDetector().detect(bitmap)
            // set the result to page:
            page.apply(newPolygon = detectionResult?.polygonF ?: PolygonHelper.getFullPolygon())
            page
        }

        withContext(Dispatchers.Main) {
            progressBar.visibility = View.GONE

            // show cropped page image:
            importResultImage.setImageBitmap(page.documentImage)
            importResultImage.visibility = View.VISIBLE
        }
    }

    companion object {
        private const val PERMISSIONS_REQUEST_CAMERA = 314
    }
}
