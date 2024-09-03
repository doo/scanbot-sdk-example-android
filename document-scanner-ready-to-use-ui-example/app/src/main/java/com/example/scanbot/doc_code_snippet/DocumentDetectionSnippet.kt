package com.example.scanbot.doc_code_snippet


import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.scanbot.utils.getUrisFromGalleryResult
import com.example.scanbot.utils.toBitmap
import io.scanbot.page.PageImageSource
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.docprocessing.Document
import io.scanbot.sdk.ui_v2.common.ScanbotColor
import io.scanbot.sdk.ui_v2.document.CroppingActivity
import io.scanbot.sdk.ui_v2.document.configuration.CroppingConfiguration
import io.scanbot.sdk.util.isDefault
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


private class DocumentDetectionSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //run this function on button click
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
    val documentDetector = scanbotSDK.createContourDetector()

    fun startCropping(document: Document) {
        document.pages.forEach { page ->
            // Run detection on the created page
            val detectionResult = documentDetector.detect(page.originalImage!!)
            // Check the result and retrieve the detected polygon.
            if (detectionResult != null &&
                detectionResult.polygonF.isNotEmpty() &&
                !detectionResult.polygonF.isDefault()
            ) {
                // If the result is an acceptable polygon, we warp the image into the polygon.
                page.apply(newPolygon = detectionResult.polygonF)
                // Set the source of the page to IMPORTED if needs
                page.source = PageImageSource.IMPORTED
            }
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

