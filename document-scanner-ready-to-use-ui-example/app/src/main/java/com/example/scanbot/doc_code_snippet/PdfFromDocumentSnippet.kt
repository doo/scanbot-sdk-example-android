package com.example.scanbot.doc_code_snippet


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import androidx.lifecycle.lifecycleScope
import com.example.scanbot.utils.getUrisFromGalleryResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.scanbot.common.Result
import io.scanbot.common.onSuccess
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.docprocessing.Document
import io.scanbot.sdk.pdfgeneration.PageDirection
import io.scanbot.sdk.pdfgeneration.PageFit
import io.scanbot.sdk.pdfgeneration.PageSize
import io.scanbot.sdk.pdfgeneration.PdfAttributes
import io.scanbot.sdk.pdfgeneration.PdfConfiguration
import io.scanbot.sdk.pdfgeneration.ResamplingMethod
import io.scanbot.sdk.ui_v2.document.utils.toImageRef


class PdfFromDocumentSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        importImagesFromLibrary()
    }

    private val scanbotSDK = ScanbotSDK(this@PdfFromDocumentSnippet)
    private val context = this

    private val pictureForDocDetectionResult =
        this.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                activityResult.data?.let { imagePickerResult ->
                    lifecycleScope.launch {
                        withContext(Dispatchers.Default) {
                            scanbotSDK.documentApi.createDocument().onSuccess { document ->
                                getUrisFromGalleryResult(imagePickerResult)
                                    .asSequence() // process images one by one instead of collecting the whole list - less memory consumption
                                    .map { it.toImageRef(contentResolver)?.getOrNull() }
                                    .forEach { bitmap ->
                                        if (bitmap == null) {
                                            Log.e(
                                                "StandaloneCropSnippet",
                                                "Failed to load bitmap from URI"
                                            )
                                            return@forEach
                                        }
                                        document.addPage(bitmap)
                                    }
                                createPdfFromImages(document)
                            }
                        }
                    }
                }
            }
        }

    // @Tag("Creating a PDF from an Document")
    // Create instance of PdfRenderer
    val pdfGenerator = scanbotSDK.createPdfGenerator()

    fun createPdfFromImages(document: Document) {
        val config = PdfConfiguration(
            attributes = PdfAttributes(
                author = "",
                title = "",
                subject = "",
                keywords = "",
                creator = ""
            ),
            pageSize = PageSize.A4,
            pageDirection = PageDirection.AUTO,
            dpi = 200,
            jpegQuality = 100,
            pageFit = PageFit.NONE,
            resamplingMethod = ResamplingMethod.NONE,
        )
        val result = pdfGenerator.generate(
            document,
            config
        )
        val pdfFile = document.pdfUri.toFile()
        if (result is Result.Success && pdfFile.exists()) {
            // Do something with the PDF file
        } else {
            Log.e("PdfFromDocumentSnippet", "Failed to create PDF")
        }
    }
    // @EndTag("Creating a PDF from an Document")


    private fun importImagesFromLibrary() {
        val imageIntent = Intent()
        imageIntent.type = "image/*"
        imageIntent.action = Intent.ACTION_GET_CONTENT
        imageIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, false)
        imageIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        imageIntent.putExtra(
            Intent.EXTRA_MIME_TYPES,
            arrayOf("image/jpeg", "image/png", "image/webp", "image/heic")
        )
        pictureForDocDetectionResult.launch(Intent.createChooser(imageIntent, "Select Picture"))
    }

}

