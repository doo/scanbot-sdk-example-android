package io.scanbot.example

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import io.scanbot.example.common.Const
import io.scanbot.example.common.showToast
import io.scanbot.example.databinding.ActivityMainBinding
import io.scanbot.sdk.ScanbotSDK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private val scanbotSdk: ScanbotSDK by lazy { ScanbotSDK(this) }

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

        lifecycleScope.launch { recognizeCheck(uri) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.checkRecognizer.setOnClickListener {
            startActivity(CheckRecognizerActivity.newIntent(this))
        }
        binding.checkRecognizerAutoSnapping.setOnClickListener {
            startActivity(AutoSnappingCheckScannerActivity.newIntent(this))
        }
        binding.checkRecognizerPickImage.setOnClickListener {
            selectGalleryImageResultLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private suspend fun recognizeCheck(uri: Uri) {
        withContext(Dispatchers.Main) { binding.progressBar.isVisible = true }

        val recognitionResult = withContext(Dispatchers.Default) {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            val checkRecognizer = scanbotSdk.createCheckScanner()
            checkRecognizer.scanFromBitmap(bitmap, 0)
        }

        withContext(Dispatchers.Main) {
            recognitionResult?.let {
                startActivity(CheckScannerResultActivity.newIntent(this@MainActivity, it))
            } ?: this@MainActivity.showToast("No  data recognized!")
        }

        withContext(Dispatchers.Main) { binding.progressBar.isVisible = false }
    }
}
