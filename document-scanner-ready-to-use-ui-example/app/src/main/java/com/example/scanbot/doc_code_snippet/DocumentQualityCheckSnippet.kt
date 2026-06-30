package com.example.scanbot.doc_code_snippet


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.scanbot.utils.getUrisFromGalleryResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.scanbot.common.onSuccess
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.docprocessing.Document
import io.scanbot.sdk.documentqualityanalyzer.DocumentQuality
import io.scanbot.sdk.documentqualityanalyzer.DocumentQualityAssessment
import io.scanbot.sdk.util.toImageRef


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
                            scanbotSDK.documentApi.createDocument().onSuccess { document ->
                                getUrisFromGalleryResult(imagePickerResult)
                                    .asSequence() // process images one by one instead of collecting the whole list - less memory consumption
                                    .map { it.toImageRef(contentResolver).getOrNull() }
                                    .forEach { image ->
                                        if (image == null) {
                                            Log.e(
                                                "QualityCheckSnippet",
                                                "Failed to load image from URI"
                                            )
                                            return@forEach
                                        }
                                        document.addPage(image)
                                    }
                                startDqa(document)
                            }
                        }
                    }
                }
            }
        }

    // @Tag("Analyze the quality of a document image")
    // Create a document detector instance
    val qualityAnalyzer = scanbotSDK.createDocumentQualityAnalyzer().getOrNull()

    fun startDqa(document: Document) {
        document.pages.forEach { page ->
            // Run quality check on the created page
            val documentQuality =
                qualityAnalyzer?.run(page.originalImageRef!!)?.getOrNull()
            // proceed the result
            if (documentQuality != null) {
                printResult(documentQuality.quality)
            }
        }
    }
    // @EndTag("Analyze the quality of a document image")

    // Print the result.
    fun printResult(quality: DocumentQualityAssessment?) {
        when (quality) {
            DocumentQualityAssessment.ACCEPTABLE ->  print("The quality of the document is good enough for processing.")
            DocumentQualityAssessment.UNACCEPTABLE ->  print("The quality of the document is not good enough for processing.")
            DocumentQualityAssessment.UNCERTAIN -> print("The quality of the document is uncertain. It may be good enough for processing, but there is a risk that the result will not be good.")
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

