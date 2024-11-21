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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.core.ImageRotation
import io.scanbot.sdk.core.documentdetector.DocumentDetectionStatus
import io.scanbot.sdk.imagefilters.BrightnessFilter
import io.scanbot.sdk.imagefilters.OutputMode
import io.scanbot.sdk.imagefilters.ScanbotBinarizationFilter
import io.scanbot.sdk.process.ImageProcessor
import io.scanbot.sdk.util.isDefault


class ImageFilterSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        importImagesFromLibrary()
    }

    private val scanbotSDK = ScanbotSDK(this@ImageFilterSnippet)
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

    // Create a document detector instance
    val documentDetector = scanbotSDK.createDocumentDetector()
    fun startFiltering(list: List<Bitmap>) {
        list.forEach { image ->
            // Run detection on the picked image
            val detectionResult = documentDetector.detect(image)

            // Check the result and retrieve the detected polygon.
            if (detectionResult != null &&
                detectionResult.status == DocumentDetectionStatus.OK &&
                detectionResult.pointsNormalized.isNotEmpty() &&
                !detectionResult.pointsNormalized.isDefault()
            ) {
                // If the result is an acceptable polygon, we warp the image into the polygon.
                val imageProcessor = ImageProcessor(image)

                // Perform operations like rotating, resizing and applying filters to the image.
                // Rotate the image.
                imageProcessor.rotate(ImageRotation.CLOCKWISE_90)

                // You can crop the image using the polygon if you want.
                imageProcessor.crop(detectionResult.pointsNormalized)

                // Resize the image.
                imageProcessor.resize(700)

                // Create the instances of the filters you want to apply.
                val filter1 = ScanbotBinarizationFilter(outputMode = OutputMode.ANTIALIASED)
                val filter2 = BrightnessFilter(brightness = 0.4)
                imageProcessor.applyFilter(filter1)
                imageProcessor.applyFilter(filter2)
                // Retrieve the processed image.
                imageProcessor.processedBitmap()?.let {
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

