package com.example.scanbot.doc_code_snippet


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import androidx.lifecycle.lifecycleScope
import com.example.scanbot.utils.getUrisFromGalleryResult
import com.example.scanbot.utils.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.docprocessing.Document
import io.scanbot.sdk.imagefilters.ScanbotBinarizationFilter
import io.scanbot.sdk.tiff.model.CompressionMode
import io.scanbot.sdk.tiff.model.TiffGeneratorParameters


class TiffFromDocumentSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        importImagesFromLibrary()
    }

    private val scanbotSDK = ScanbotSDK(this@TiffFromDocumentSnippet)
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
                            createTiffFromDocument(document)
                        }
                    }
                }
            }
        }

    // @Tag("Creating a TIFF from a Document")
    // Create tiff generator instance
    val tiffGenerator = scanbotSDK.createTiffGenerator()

    fun createTiffFromDocument(document: Document) {
        val config = TiffGeneratorParameters(
            dpi = 200,
            compression = TiffGeneratorParameters.defaultCompression, // default compression is `CompressionMode.LZW`
            userFields = arrayListOf()
        )
        val tiffFile = document.tiffUri.toFile()
        val tiffGenerated = tiffGenerator.generateFromDocument(
            document,
            tiffFile,
            config
        )
        val file = tiffFile
        if (tiffGenerated && file.exists()) {
            // Do something with the Tiff file
        } else {
            Log.e("TiffFromDocumentSnippet", "Failed to create Tiff")
        }
    }
    // @EndTag("Creating a TIFF from a Document")

    // @Tag("Creating a binarized TIFF from a Document")
    fun createBinarizedTiffFromDocument(document: Document) {
        val config = TiffGeneratorParameters(
            binarizationFilter = ScanbotBinarizationFilter(),
            dpi = 200,
            compression = TiffGeneratorParameters.binaryDocumentOptimizedCompression, // compression is `CompressionMode.CCITT_T6`
            userFields = arrayListOf()
        )
        val tiffFile = document.tiffUri.toFile()
        val tiffGenerated = tiffGenerator.generateFromDocument(
            document,
            tiffFile,
            config
        )
        val file = tiffFile
        if (tiffGenerated && file.exists()) {
            // Do something with the Tiff file
        } else {
            Log.e("TiffFromDocumentSnippet", "Failed to create Tiff")
        }
    }
    // @EndTag("Creating a binarized TIFF from a Document")


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

