package io.scanbot.example

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.scanbot.common.onSuccess


import io.scanbot.example.common.Const
import io.scanbot.example.common.applyEdgeToEdge
import io.scanbot.example.common.showToast
import io.scanbot.example.databinding.ActivityMainBinding
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.documentscanner.DocumentStraighteningMode
import io.scanbot.sdk.documentscanner.DocumentStraighteningParameters
import io.scanbot.sdk.image.ImageRef
import io.scanbot.sdk.util.PolygonHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
Ths example uses new SDK APIs presented in Scanbot SDK v.8.x.x
Please, check the official documentation for more details:
Result API https://docs.scanbot.io/android/document-scanner-sdk/detailed-setup-guide/result-api/
ImageRef API https://docs.scanbot.io/android/document-scanner-sdk/detailed-setup-guide/image-ref-api/
 */

class MainActivity : AppCompatActivity() {

    private val scanbotSdk: ScanbotSDK by lazy { ScanbotSDK(this) }

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val requestCameraLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startActivity(Intent(this, DocumentCameraActivity::class.java))
            } else {
                this@MainActivity.showToast("Camera permission is required to run this example!")
            }
        }

    private val selectGalleryImageResultLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
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

            lifecycleScope.launch { processImage(uri) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportActionBar?.hide()
        applyEdgeToEdge(findViewById(R.id.root_view))

        binding.showDocScannerBtn.setOnClickListener {
            requestCameraLauncher.launch(Manifest.permission.CAMERA)
        }

        binding.importImage.setOnClickListener {
            selectGalleryImageResultLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    /** Imports a selected image and performs document straightening on it. */
    private suspend fun processImage(uri: Uri) {
        withContext(Dispatchers.Main) {
            binding.progressBar.visibility = View.VISIBLE
            this@MainActivity.showToast("Importing page...")
        }

        val documentImage = withContext(Dispatchers.Default) {
            // load the selected image
            val image = contentResolver.openInputStream(uri)?.use { inputStream ->
                ImageRef.fromInputStream(inputStream)
            } ?: throw IllegalStateException("Cannot open input stream from URI: $uri")

            // run document scanning on the image:
            scanbotSdk.createDocumentStraightener().getOrNull()
                ?.run(image, parameters = DocumentStraighteningParameters().apply {
                    straighteningMode = DocumentStraighteningMode.STRAIGHTEN
                    // uncomment if you want wo set specific aspect ratios for documents
                    // aspectRatios = listOf(AspectRatio(29.0, 21.0))
                })?.getOrNull()?.straightenedImage?.toBitmap()?.getOrNull()
        }

        withContext(Dispatchers.Main) {
            binding.progressBar.visibility = View.GONE

            // present straightened image:
            binding.importResultImage.setImageBitmap(documentImage)
            binding.importResultImage.visibility = View.VISIBLE
        }
    }
}
