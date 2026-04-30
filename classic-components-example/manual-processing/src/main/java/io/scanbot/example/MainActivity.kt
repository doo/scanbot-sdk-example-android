package io.scanbot.example

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope


import io.scanbot.example.common.Const
import io.scanbot.example.common.applyEdgeToEdge
import io.scanbot.example.common.showToast
import io.scanbot.example.databinding.ActivityMainBinding
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.image.ImageRef
import io.scanbot.sdk.imageprocessing.ParametricFilter
import io.scanbot.sdk.util.PolygonHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
This example uses the SDK APIs introduced in Scanbot SDK v8.x.x.
Please check the official documentation for more details:
Result API https://docs.scanbot.io/android/document-scanner-sdk/detailed-setup-guide/result-api/
ImageRef API https://docs.scanbot.io/android/document-scanner-sdk/detailed-setup-guide/image-ref-api/
 */

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val scanbotSdk: ScanbotSDK by lazy { ScanbotSDK(this) }

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

        binding.scanButton.setOnClickListener {
            selectGalleryImageResultLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private suspend fun processImage(imageUri: Uri) {
        withContext(Dispatchers.Main) { binding.progressBar.visibility = View.VISIBLE }

        val image = withContext(Dispatchers.IO) {
            contentResolver.openInputStream(imageUri)?.use { inputStream ->
                ImageRef.fromInputStream(inputStream)
            }
        }
        if (image == null) {
            withContext(Dispatchers.Main) {
                binding.progressBar.visibility = View.GONE
                showToast("Error opening selected image!")
                Log.e(Const.LOG_TAG, "Cannot open input stream from URI: $imageUri")
            }
            return
        }

        val page = withContext(Dispatchers.Default) {
            val scanner = scanbotSdk.createDocumentScanner().getOrThrow()
            val detectedPolygon =
                scanner.run(image).getOrNull()?.pointsNormalized ?: PolygonHelper.getFullPolygon()
            val document = scanbotSdk.documentApi.createDocument()
                .getOrThrow() // can be handled with .getOrNull() if needed
            return@withContext document.addPage(image).getOrThrow().apply {
                apply(
                    newPolygon = detectedPolygon,
                    newFilters = listOf(ParametricFilter.grayscaleFilter())
                )
            }
        }

        withContext(Dispatchers.Main) {
            binding.progressBar.visibility = View.GONE
            binding.image.setImageURI(page.documentFileUri) // cropped image with grayscale filter applied
        }
    }
}
