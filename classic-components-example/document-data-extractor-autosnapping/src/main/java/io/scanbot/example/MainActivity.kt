package io.scanbot.example

import android.Manifest
import android.content.Intent
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

            lifecycleScope.launch {
                val dataExtractor = scanbotSdk.createDocumentDataExtractor().getOrThrow()

            val result = withContext(Dispatchers.Default) {
                val inputStream = contentResolver.openInputStream(uri) ?: throw IllegalStateException("Cannot open input stream from URI: $uri")
                val imageRef = ImageRef.fromInputStream(inputStream)
                dataExtractor.run(imageRef).getOrNull()
            }

                withContext(Dispatchers.Main) {
                    DocumentsResultsStorage.result = result
                    showResult()
                }
            }
        }

    private val requestCameraLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startScannerActivity()
            } else {
                this@MainActivity.showToast("Camera permission is required to run this example!")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportActionBar?.hide()
        applyEdgeToEdge(this.findViewById(R.id.root_view))

        binding.startScannerButton.run {
            setOnClickListener { requestCameraLauncher.launch(Manifest.permission.CAMERA) }
            visibility = View.VISIBLE
        }
        binding.pickImageBtn.run {
            setOnClickListener {
                selectGalleryImageResultLauncher.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            }
        }
    }

    private fun startScannerActivity() {
        val intent = Intent(this, ScannerActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showResult() {
        val intent = Intent(this, ResultActivity::class.java)
        startActivity(intent)
        finish()
    }
}
