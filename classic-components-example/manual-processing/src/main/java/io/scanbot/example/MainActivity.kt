package io.scanbot.example

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
import io.scanbot.sdk.imagefilters.ParametricFilter
import io.scanbot.sdk.util.PolygonHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val scanbotSdk: ScanbotSDK by lazy { ScanbotSDK(this) }

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

        lifecycleScope.launch { processImage(uri) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.scanButton.setOnClickListener {
            selectGalleryImageResultLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private suspend fun processImage(imageUri: Uri) {
        withContext(Dispatchers.Main) { binding.progressBar.visibility = View.VISIBLE }

        val page = withContext(Dispatchers.Default) {
            val inputStream = contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val detectedPolygon = scanbotSdk.createContourDetector().detect(bitmap)?.polygonF ?: PolygonHelper.getFullPolygon()
            val document = scanbotSdk.documentApi.createDocument()
            return@withContext document.addPage(bitmap).apply {
                apply(newPolygon = detectedPolygon, newFilters = listOf(ParametricFilter.grayscaleFilter()))
            }
        }

        withContext(Dispatchers.Main) {
            binding.progressBar.visibility = View.GONE
            binding.image.setImageURI(page.documentFileUri) // cropped image with grayscale filter applied
        }
    }
}
