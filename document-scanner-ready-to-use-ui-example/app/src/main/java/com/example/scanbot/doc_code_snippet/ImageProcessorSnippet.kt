package com.example.scanbot.doc_code_snippet


import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
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
import io.scanbot.sdk.core.contourdetector.DocumentDetectionStatus
import io.scanbot.sdk.core.processor.ImageProcessor
import io.scanbot.sdk.docprocessing.Document
import io.scanbot.sdk.ui_v2.common.ScanbotColor
import io.scanbot.sdk.ui_v2.document.CroppingActivity
import io.scanbot.sdk.ui_v2.document.configuration.CroppingConfiguration
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
                                    startCropping(this.toList())
                                }

                        }
                    }
                }
            }
        }

    // Create a document detector instance
    val documentDetector = scanbotSDK.createContourDetector()
    fun startCropping(list: List<Bitmap>) {
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

