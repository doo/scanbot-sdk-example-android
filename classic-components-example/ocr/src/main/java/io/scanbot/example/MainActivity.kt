package io.scanbot.example


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
import io.scanbot.sdk.docprocessing.Document
import io.scanbot.sdk.image.ImageRef
import io.scanbot.sdk.ocr.OcrEngineManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
    private val opticalCharacterRecognizer: OcrEngineManager by lazy { scanbotSdk.createOcrEngineManager() }


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

            binding.progressBar.visibility = View.VISIBLE
            lifecycleScope.launch {
                val document = createDocument(uri)
                document?.let { recognizeTextWithoutPDF(it) }
                binding.progressBar.visibility = View.GONE
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportActionBar!!.hide()
        applyEdgeToEdge(findViewById(R.id.root_view))

        binding.scanButton.setOnClickListener {
            selectGalleryImageResultLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private suspend fun createDocument(uri: Uri): Document? {
        return withContext(Dispatchers.IO) {

            val image = contentResolver.openInputStream(uri)?.use { inputStream ->
                ImageRef.fromInputStream(inputStream)
            }
            scanbotSdk.documentApi.createDocument().getOrNull()
                ?.apply { image?.let { addPage(it) } }
        }
    }

    private suspend fun recognizeTextWithoutPDF(document: Document) {
        withContext(Dispatchers.Default) {
            opticalCharacterRecognizer.recognizeFromUris(document.pages.map { it.documentFileUri })
                .onSuccess { ocrResult ->
                    runBlocking(Dispatchers.Main) {
                        ocrResult.let {
                            if (it.ocrPages.isNotEmpty()) {
                                this@MainActivity.showToast("Recognized page content: ${it.recognizedText.trimIndent()}")
                            }
                        }
                    }
                }
        }
    }
}
