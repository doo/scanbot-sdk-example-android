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
import com.example.scanbot.utils.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.scanbot.pdf.model.PageDirection
import io.scanbot.pdf.model.PageFit
import io.scanbot.pdf.model.PageSize
import io.scanbot.pdf.model.PdfAttributes
import io.scanbot.pdf.model.PdfConfiguration
import io.scanbot.pdf.model.ResamplingMethod
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.docprocessing.Document


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
                            val document = scanbotSDK.documentApi.createDocument()
                            getUrisFromGalleryResult(imagePickerResult)
                                .asSequence() // process images one by one instead of collecting the whole list - less memory consumption
                                .map { it.toBitmap(contentResolver) }
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
        val pdfGenerated = pdfGenerator.generateFromDocument(
            document,
            config
        )
        val pdfFile = document.pdfUri.toFile()
        if (pdfGenerated && pdfFile.exists()) {
            // Do something with the PDF file
        } else {
            Log.e("PdfFromDocumentSnippet", "Failed to create PDF")
        }
    }


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

