package com.example.scanbot.doc_code_snippet


import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
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
import java.io.File
import io.scanbot.sdk.ScanbotSDK


class ImportDocumentFromPdfSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        importPdfFromLibrary()
    }

    private val scanbotSDK = ScanbotSDK(this@ImportDocumentFromPdfSnippet)
    private val context = this

    private val pdfForImportResult =
        this.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                activityResult.data?.let { imagePickerResult ->
                    lifecycleScope.launch {
                        withContext(Dispatchers.Default) {
                            getUrisFromGalleryResult(imagePickerResult)
                                .asSequence() // process images one by one instead of collecting the whole list - less memory consumption
                                .apply {
                                    createDocumentFromPdf(this.toList())
                                }

                        }
                    }
                }
            }
        }

    // Create instance of PdfImagesExtractor
    val extractor = scanbotSDK.createPdfImagesExtractor()
    fun createDocumentFromPdf(list: List<Uri>) {
        list.forEach { pdfUri ->
            extractor.imageUrlsFromPdf(
                pdfFile = pdfUri.toFile(),
                outputDir = File("path/to/output/folder"),
                prefix = "image_"
            ).apply {
                val document = scanbotSDK.documentApi.createDocument()
                this.forEach { imageUri ->
                    val bitmap = BitmapFactory.decodeFile(imageUri.toFile().absolutePath)
                    if (bitmap == null) {
                        Log.e(
                            "Snippet",
                            "Failed to load bitmap from URI"
                        )
                        return@forEach
                    }
                    document.addPage(bitmap)
                }
            }
        }
    }

    private fun importPdfFromLibrary() {
        val pdfIntent = Intent()
        pdfIntent.type = "application/pdf"
        pdfIntent.action = Intent.ACTION_GET_CONTENT
        pdfIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, false)
        pdfIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        pdfForImportResult.launch(Intent.createChooser(pdfIntent, "Select PDF"))
    }

}

