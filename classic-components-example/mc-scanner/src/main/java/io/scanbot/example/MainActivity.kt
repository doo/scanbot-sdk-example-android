package io.scanbot.example

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import io.scanbot.common.getOrNull
import io.scanbot.common.getOrThrow
import io.scanbot.example.common.Const
import io.scanbot.example.common.applyEdgeToEdge
import io.scanbot.example.common.showToast
import io.scanbot.example.databinding.ActivityMainBinding
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.image.ImageRef
import io.scanbot.sdk.medicalcertificate.MedicalCertificateScanningParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

            lifecycleScope.launch { processImportedImage(uri) }
        }

    private suspend fun processImportedImage(uri: Uri) {
        withContext(Dispatchers.Main) {
            binding.progressBar.isVisible = true
        }

        val result = withContext(Dispatchers.Default) {
            val inputStream = contentResolver.openInputStream(uri)
                ?: throw IllegalStateException("Cannot open input stream from URI: $uri")
            val imageRef = ImageRef.fromInputStream(inputStream)

            val scanner = scanbotSdk.createMedicalCertificateScanner().getOrThrow()

            scanner.run(
                imageRef,
                MedicalCertificateScanningParameters(true, true, true)
            ).getOrNull()
        }

        withContext(Dispatchers.Main) {
            result?.let {
                startActivity(MedicalCertificateResultActivity.newIntent(this@MainActivity, it))
            } ?: this@MainActivity.showToast("Nothing found on image")

            binding.progressBar.isVisible = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportActionBar?.hide()
        applyEdgeToEdge(findViewById(R.id.root_view))

        binding.scannerBtn.setOnClickListener {
            startActivity(
                MedicalCertificateScannerActivity.newIntent(
                    this
                )
            )
        }

        binding.manualScannerBtn.setOnClickListener {
            startActivity(ManualMedicalCertificateScannerActivity.newIntent(this))
        }

        binding.pickImageBtn.setOnClickListener {
            selectGalleryImageResultLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }
}
