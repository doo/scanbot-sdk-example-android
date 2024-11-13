package com.example.scanbot.doc_code_snippet


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.scanbot.utils.getUrisFromGalleryResult
import com.example.scanbot.utils.toBitmap
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.docprocessing.Document
import io.scanbot.sdk.process.DocumentQuality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class DocumentQualityCheckSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        importImagesFromLibrary()
    }

    private val scanbotSDK = ScanbotSDK(this@DocumentQualityCheckSnippet)
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
                            startCropping(document)
                        }
                    }
                }
            }
        }

    // Create a document detector instance
    val qualityAnalyzer = scanbotSDK.createDocumentQualityAnalyzer()

    fun startCropping(document: Document) {
        document.pages.forEach { page ->
            // Run quality check on the created page
            val documentQuality =
                qualityAnalyzer.analyzeInBitmap(page.originalImage!!, orientation = 0)
            // proceed the result
            if (documentQuality != null) {
                printResult(documentQuality.quality)
            }
        }
    }

    // Print the result.
    fun printResult(quality: DocumentQuality?) {
        when (quality) {
            DocumentQuality.VERY_POOR -> print("The quality of the document is very poor")
            DocumentQuality.POOR -> print("The quality of the document is poor")
            DocumentQuality.REASONABLE -> print("The quality of the document is reasonable")
            DocumentQuality.GOOD -> print("The quality of the document is good")
            DocumentQuality.EXCELLENT -> print("The quality of the document is excellent")
            else -> print("No document was found")
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

