package io.scanbot.example

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
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

class MainActivity : AppCompatActivity() {

    private val scanbotSdk: ScanbotSDK by lazy { ScanbotSDK(this) }

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

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

            lifecycleScope.launch { scanCheck(uri) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportActionBar?.hide()

        applyEdgeToEdge(this.findViewById(R.id.root_view))

        askPermission()

        binding.checkScanner.setOnClickListener {
            startActivity(CheckScannerActivity.newIntent(this))
        }
        binding.checkScannerAutoSnapping.setOnClickListener {
            startActivity(AutoSnappingCheckScannerActivity.newIntent(this))
        }
        binding.checkScannerPickImage.setOnClickListener {
            selectGalleryImageResultLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private suspend fun scanCheck(uri: Uri) {
        withContext(Dispatchers.Main) { binding.progressBar.isVisible = true }

        val result = withContext(Dispatchers.Default) {
            val inputStream = contentResolver.openInputStream(uri) ?: throw IllegalStateException("Cannot open input stream from URI: $uri")
            val imageRef = ImageRef.fromInputStream(inputStream)

            val scanner = scanbotSdk.createCheckScanner().getOrThrow()
            scanner.run(imageRef).getOrNull()
        }

        withContext(Dispatchers.Main) {
            result?.let {
                CheckScannerResultActivity.tempDocumentImage = it?.croppedImage?.toBitmap()?.getOrNull()
                startActivity(CheckScannerResultActivity.newIntent(this@MainActivity, it))
            } ?: this@MainActivity.showToast("No  data found!")
        }

        withContext(Dispatchers.Main) { binding.progressBar.isVisible = false }
    }

    private fun askPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 999)
        }
    }
}
