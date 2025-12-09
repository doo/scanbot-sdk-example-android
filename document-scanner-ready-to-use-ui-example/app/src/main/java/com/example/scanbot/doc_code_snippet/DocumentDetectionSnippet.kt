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
import io.scanbot.page.PageImageSource
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.docprocessing.Document
import io.scanbot.sdk.util.isDefault
import io.scanbot.sdk.util.toImageRef


class DocumentDetectionSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        importImagesFromLibrary()
    }

    private val scanbotSDK = ScanbotSDK(this@DocumentDetectionSnippet)
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
                                                "StandaloneCropSnippet",
                                                "Failed to load image from URI"
                                            )
                                            return@forEach
                                        }
                                        document.addPage(image)
                                    }
                                startCropping(document)
                            }
                        }
                    }
                }
            }
        }

    // @Tag("Direct Document detection on page")
    // Create a document detector instance
    val documentScanner = scanbotSDK.createDocumentScanner().getOrNull()

    fun startCropping(document: Document) {
        document.pages.forEach { page ->
            // Run detection on the created page
            val detectionResult = documentScanner?.run(page.originalImageRef!!)?.getOrNull()
            // Check the result and retrieve the detected polygon.
            if (detectionResult != null &&
                detectionResult.pointsNormalized.isNotEmpty() &&
                !detectionResult.pointsNormalized.isDefault()
            ) {
                // If the result is an acceptable polygon, we warp the image into the polygon.
                page.apply(newPolygon = detectionResult.pointsNormalized)
                // Set the source of the page to IMPORTED if needs
                page.source = PageImageSource.IMPORTED
            }
        }
    }
    // @EndTag("Direct Document detection on page")

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

