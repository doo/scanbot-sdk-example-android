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
import io.scanbot.pdf.model.PageSize
import io.scanbot.pdf.model.PdfConfiguration
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.docprocessing.Document
import io.scanbot.sdk.ocr.OcrEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val scanbotSdk: ScanbotSDK by lazy { ScanbotSDK(this) }
    private val opticalCharacterRecognizer: OcrEngine by lazy { scanbotSdk.createOcrEngine() }


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
                recognizeTextWithoutPDF(document)
                binding.progressBar.visibility = View.GONE
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.scanButton.setOnClickListener {
            selectGalleryImageResultLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private suspend fun createDocument(uri: Uri): Document {
        return withContext(Dispatchers.IO) {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            scanbotSdk.documentApi.createDocument().apply { addPage(bitmap) }
        }
    }

    private suspend fun recognizeTextWithoutPDF(document: Document) {
        val ocrResult = withContext(Dispatchers.Default) {
            opticalCharacterRecognizer.recognizeFromUris(document.pages.map { it.documentFileUri })
        }

        withContext(Dispatchers.Main) {
            ocrResult.let {
                if (it.ocrPages.isNotEmpty()) {
                    this@MainActivity.showToast("Recognized page content: ${it.recognizedText.trimIndent()}")
                }
            }
        }
    }
}
