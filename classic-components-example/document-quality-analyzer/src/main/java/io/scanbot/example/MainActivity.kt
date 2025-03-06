package io.scanbot.example

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import io.scanbot.example.common.Const
import io.scanbot.example.common.applyEdgeToEdge
import io.scanbot.example.common.showToast
import io.scanbot.example.databinding.ActivityMainBinding
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.process.DocumentQualityAnalyzer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private val scanbotSdk: ScanbotSDK by lazy { ScanbotSDK(this) }
    private val documentQualityAnalyzer: DocumentQualityAnalyzer by lazy { scanbotSdk.createDocumentQualityAnalyzer() }

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

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

        lifecycleScope.launch { estimateOnStillImage(uri) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportActionBar!!.hide()
        applyEdgeToEdge(findViewById(R.id.root_view))

        binding.galleryButton.setOnClickListener {
            selectGalleryImageResultLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.stillImageClose.setOnClickListener { this@MainActivity.finish() }
    }

    override fun onResume() {
        super.onResume()
        if (scanbotSdk.licenseInfo.isValid.not()) showToast("License expired!")
        // TODO: use License fragment!
    }

    private suspend fun estimateOnStillImage(imageUri: Uri) {
        val bitmap = withContext(Dispatchers.IO) {
            val inputStream = contentResolver.openInputStream(imageUri)
            BitmapFactory.decodeStream(inputStream)
        }

        withContext(Dispatchers.Main) {
            binding.stillImageImageView.setImageBitmap(bitmap)
        }

        val result = withContext(Dispatchers.Default) { documentQualityAnalyzer.analyzeOnBitmap(bitmap, 0) }

        withContext(Dispatchers.Main) {
            binding.stillImageQualityCaption.text = "Image quality: ${result?.quality?.name ?: "UNKNOWN"}"
        }
    }
}
