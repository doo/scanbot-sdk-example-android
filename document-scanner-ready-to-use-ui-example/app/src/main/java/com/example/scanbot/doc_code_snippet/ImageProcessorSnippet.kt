package com.example.scanbot.doc_code_snippet


import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.scanbot.utils.getUrisFromGalleryResult
import com.example.scanbot.utils.toBitmap
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.core.contourdetector.DocumentDetectionStatus
import io.scanbot.sdk.core.processor.ImageProcessor
import io.scanbot.sdk.process.model.DocumentQuality
import io.scanbot.sdk.util.isDefault
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


private class ImageProcessorSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //run this function on button click
        importImagesFromLibrary()
    }

    private val scanbotSDK = ScanbotSDK(this@ImageProcessorSnippet)
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
                                .mapNotNull { it.toBitmap(contentResolver) }.apply {
                                    startFiltering(this.toList())
                                }

                        }
                    }
                }
            }
        }

    // Create a quality analyzer instance
    val documentDetector = scanbotSDK.createContourDetector()
    fun startFiltering(list: List<Bitmap>) {
        list.forEach { image ->
            // Run detection on the picked image
            val detectionResult = documentDetector.detect(image)

            // Check the result and retrieve the detected polygon.
            if (detectionResult != null &&
                detectionResult.status == DocumentDetectionStatus.OK &&
                detectionResult.polygonF.isNotEmpty() &&
                !detectionResult.polygonF.isDefault()
            ) {
                // If the result is an acceptable polygon, we warp the image into the polygon.
                val imageProcessor = ImageProcessor(image)

                // You can crop the image using the polygon if you want.
                imageProcessor.crop(detectionResult.polygonF)

                // Retrieve the processed image.
                imageProcessor.processedImage()?.let {
                    // do something with the cropped image. eg. add it to a document save etc.
                }
            }
        }
    }

    // Print the result.
    fun printResult(quality: DocumentQuality) {
        when (quality) {
            DocumentQuality.NO_DOCUMENT ->
                print("No document was found")

            DocumentQuality.VERY_POOR ->
                print("The quality of the document is very poor")

            DocumentQuality.POOR ->
                print("The quality of the document is poor")

            DocumentQuality.REASONABLE ->
                print("The quality of the document is reasonable")

            DocumentQuality.GOOD ->
                print("The quality of the document is good")

            DocumentQuality.EXCELLENT ->
                print("The quality of the document is excellent")
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

