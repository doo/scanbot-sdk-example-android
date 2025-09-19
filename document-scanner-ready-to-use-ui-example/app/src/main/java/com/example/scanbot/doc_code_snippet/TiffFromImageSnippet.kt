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
import java.io.File
import io.scanbot.common.getOrNull
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.tiffgeneration.TiffGeneratorParameters


class TiffFromImageSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        importImagesFromLibrary()
    }

    private val scanbotSDK = ScanbotSDK(this@TiffFromImageSnippet)
    private val context = this

    private val pictureForDocDetectionResult =
        this.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                activityResult.data?.let { imagePickerResult ->
                    lifecycleScope.launch {
                        withContext(Dispatchers.Default) {
                            getUrisFromGalleryResult(imagePickerResult).asSequence() // process images one by one instead of collecting the whole list - less memory consumption
                                .apply {
                                    createTiffFromImages(this.toList())
                                }
                        }
                    }
                }
            }
        }

    // @Tag("Creating a TIFF from images")
    // Create tiff generator instance
    val tiffGenerator = scanbotSDK.createTiffGeneratorManager()
    fun createTiffFromImages(list: List<Uri>) {
        // Create the default Tiff generation options.
        val config = TiffGeneratorParameters(
            dpi = 200,
            compression = TiffGeneratorParameters.defaultCompression,  // Use default compression (LZW)
            userFields = arrayListOf()
        )
        // notify the generator that the images are encrypted with global sdk-encryption settings
        val encryptionEnabled = false
        // Render the images to a Tiff file.
        val file = File("path/to/tiff/file")
        val created = tiffGenerator.generateFromUris(
            sourceImages = list.toTypedArray(),
            sourceFilesEncrypted = encryptionEnabled,
            targetFile = file,
            parameters = config
        ).getOrNull()

        if (created != null && file.exists()) {
            // Do something with the Tiff file
        }
    }
    // @EndTag("Creating a TIFF from images")

    private fun importImagesFromLibrary() {
        val imageIntent = Intent()
        imageIntent.type = "image/*"
        imageIntent.action = Intent.ACTION_GET_CONTENT
        imageIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, false)
        imageIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        imageIntent.putExtra(
            Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png", "image/webp", "image/heic")
        )
        pictureForDocDetectionResult.launch(Intent.createChooser(imageIntent, "Select Picture"))
    }

}

