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
import io.scanbot.sdk.image.ImageRef
import io.scanbot.sdk.util.PolygonHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
Ths example uses new sdk APIs presented in Scanbot SDK v.8.x.x
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

            lifecycleScope.launch { processImageForAutoDocumentScanning(uri) }
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

    /** Imports a selected image as original image and performs auto document scanning on it. */
    private suspend fun processImageForAutoDocumentScanning(uri: Uri) {
        withContext(Dispatchers.Main) {
            binding.progressBar.visibility = View.VISIBLE
            this@MainActivity.showToast("Importing page...")
        }

        val documentImage = withContext(Dispatchers.Default) {
            // load the selected image
            val image = contentResolver.openInputStream(uri)?.use { inputStream ->
                ImageRef.fromInputStream(inputStream)
            } ?: throw IllegalStateException("Cannot open input stream from URI: $uri")


            // create a new Page object with given image as original image:
            val document = scanbotSdk.documentApi.createDocument()
                .getOrNull() //can be handled with .getOrThrow() if needed
            val page =
                document?.addPage(image)?.getOrNull() //can be handled with .getOrThrow() if needed

            // run document scanning on the image:
            scanbotSdk.createDocumentScanner().onSuccess { scanner ->
                val result = scanner.run(image).getOrReturn()
                // set the result to page:
                page?.apply(newPolygon = result.pointsNormalized?.takeIf { poly -> poly.isNotEmpty() }
                    ?: PolygonHelper.getFullPolygon())
            }
            page?.documentImage
        }

        withContext(Dispatchers.Main) {
            binding.progressBar.visibility = View.GONE

            // present cropped page image:
            binding.importResultImage.setImageBitmap(documentImage)
            binding.importResultImage.visibility = View.VISIBLE
        }
    }
}
