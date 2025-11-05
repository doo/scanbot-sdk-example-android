package io.scanbot.example.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope


import io.scanbot.example.R
import io.scanbot.example.common.Const
import io.scanbot.example.common.applyEdgeToEdge
import io.scanbot.example.common.showToast
import io.scanbot.example.databinding.ActivityMainBinding
import io.scanbot.example.model.BarcodeResultBundle
import io.scanbot.example.repository.BarcodeResultRepository
import io.scanbot.example.repository.BarcodeTypeRepository
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.barcode.setBarcodeFormats
import io.scanbot.sdk.image.ImageRef
import io.scanbot.sdk.licensing.LicenseStatus
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

        if (scanbotSdk.licenseInfo.isValid) {
            lifecycleScope.launch { scanBarcodeAndShowResult(uri) }
        } else {
            this@MainActivity.showToast("1-minute trial license has expired!")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        applyEdgeToEdge(findViewById(R.id.root_view))

        binding.qrDemo.setOnClickListener {
            val intent = Intent(applicationContext, BarcodeScannerActivity::class.java)
            startActivity(intent)
        }

        binding.barcodeScannerViewDemo.setOnClickListener {
            val intent = Intent(applicationContext, BarcodeScannerViewActivity::class.java)
            startActivity(intent)
        }
        binding.barcodeCounterViewDemo.setOnClickListener {
            val intent = Intent(applicationContext, BarcodeScanAndCountViewActivity::class.java)
            startActivity(intent)
        }

        binding.settings.setOnClickListener {
            val intent = Intent(this@MainActivity, BarcodeTypesActivity::class.java)
            startActivity(intent)
        }

        binding.importImage.setOnClickListener {
            // select an image from photo library and run document scanning on it:
            selectGalleryImageResultLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    override fun onResume() {
        super.onResume()
        binding.warningView.isVisible = ScanbotSDK(this).licenseInfo.status == LicenseStatus.TRIAL
    }

    private suspend fun scanBarcodeAndShowResult(uri: Uri) {
        withContext(Dispatchers.Main) {
            binding.progressBar.isVisible = true
        }

        withContext(Dispatchers.Default) {
            val inputStream = contentResolver.openInputStream(uri) ?: throw IllegalStateException("Cannot open input stream from URI: $uri")
            val imageRef = ImageRef.fromInputStream(inputStream)

            val scanner = scanbotSdk.createBarcodeScanner().getOrThrow()
            scanner.setConfiguration(scanner.copyCurrentConfiguration().copy().apply {
                setBarcodeFormats(barcodeFormats = BarcodeTypeRepository.selectedTypes.toList())
            } )
            val result = scanner.run(imageRef).getOrNull()

            BarcodeResultRepository.barcodeResultBundle = result?.let { BarcodeResultBundle(it, null, null) }
        }

        withContext(Dispatchers.Main) {
            startActivity(Intent(this@MainActivity, BarcodeResultActivity::class.java))
            binding.progressBar.isVisible = false
        }
    }
}
