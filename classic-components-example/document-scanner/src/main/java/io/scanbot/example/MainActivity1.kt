package io.scanbot.example

import android.Manifest
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.scanbot.example.common.Const
import io.scanbot.example.common.showToast
import io.scanbot.example.databinding.ActivityMainBinding
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.util.PolygonHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private val scanbotSdk: ScanbotSDK by lazy { ScanbotSDK(this) }

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val requestCameraLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            startActivity(Intent(this, DocumentCameraActivity::class.java))
        } else {
            this@MainActivity.showToast("Camera permission is required to run this example!")
        }
    }

    private val selectGalleryImageResultLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (!scanbotSdk.licenseInfo.isValid) {
            this@MainActivity.showToast("1-minute trial license has expired!")
            Log.e(Const.LOG_TAG, "1-minute trial license has expired!")
            return@registerForActivityResult
        }

        if (uri == null) {
            showToast("Error obtaining selected image!")
            Log.e(Const.LOG_TAG, "Error obtaining selected image!")
            return@registerForActivityResult
        }

        lifecycleScope.launch { processImageForAutoDocumentDetection(uri) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.showDocScannerBtn.setOnClickListener {
            requestCameraLauncher.launch(Manifest.permission.CAMERA)
        }

        binding.importImage.setOnClickListener {
            selectGalleryImageResultLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    /** Imports a selected image as original image and performs auto document detection on it. */
    private suspend fun processImageForAutoDocumentDetection(uri: Uri) {
        withContext(Dispatchers.Main) {
            binding.progressBar.visibility = View.VISIBLE
            this@MainActivity.showToast("Importing page...")
        }

        val page = withContext(Dispatchers.Default) {
            // load the selected image
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            // create a new Page object with given image as original image:
            val document = scanbotSdk.documentApi.createDocument()
            val page = document.addPage(bitmap)

            // run contour detection on the image:
            val detectionResult = scanbotSdk.createDocumentScanner().scanFromBitmap(bitmap)
            // set the result to page:
            page.apply(newPolygon = detectionResult?.pointsNormalized ?: PolygonHelper.getFullPolygon())
            page
        }

        withContext(Dispatchers.Main) {
            binding.progressBar.visibility = View.GONE

            // present cropped page image:
            binding.importResultImage.setImageBitmap(page.documentImage)
            binding.importResultImage.visibility = View.VISIBLE
        }
    }
}
