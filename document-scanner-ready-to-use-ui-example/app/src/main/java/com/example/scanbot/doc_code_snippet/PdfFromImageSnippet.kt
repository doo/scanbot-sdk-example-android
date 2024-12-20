package com.example.scanbot.doc_code_snippet


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.scanbot.utils.getUrisFromGalleryResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.scanbot.pdf.model.PdfConfiguration
import io.scanbot.sdk.ScanbotSDK


class PdfFromImageSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        importImagesFromLibrary()
    }

    private val scanbotSDK = ScanbotSDK(this@PdfFromImageSnippet)
    private val context = this

    private val pictureForDocDetectionResult =
        this.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                activityResult.data?.let { imagePickerResult ->
                    lifecycleScope.launch {
                        withContext(Dispatchers.Default) {
                            getUrisFromGalleryResult(imagePickerResult)
                                .asSequence() // process images one by one instead of collecting the whole list - less memory consumption
                                .apply {
                                    createPdfFromImages(this.toList())
                                }

                        }
                    }
                }
            }
        }

    // Create a PDF generator instance
    val pdfGenerator = scanbotSDK.createPdfGenerator()
    fun createPdfFromImages(list: List<Uri>) {
        list.forEach { imageUri ->
            // Create the default PDF generation options.
            val config = PdfConfiguration.default()
            // notify the generator that the images are encrypted with global sdk-encryption settings
            val encryptionEnabled = false
            // Join the images into a PDF file.
            val pdfFile = pdfGenerator.generateFromUris(
                imageFileUris = list.toTypedArray(),
                sourceFilesEncrypted = encryptionEnabled,
                config
            )

            if (pdfFile != null && pdfFile.exists()) {
                // Do something with the PDF file
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

