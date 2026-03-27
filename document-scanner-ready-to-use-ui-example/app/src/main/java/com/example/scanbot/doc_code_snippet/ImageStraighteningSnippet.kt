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
import io.scanbot.common.onFailure
import io.scanbot.common.onSuccess
import io.scanbot.page.PageImageSource
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.docprocessing.Document
import io.scanbot.sdk.documentscanner.DocumentStraighteningMode
import io.scanbot.sdk.documentscanner.DocumentStraighteningParameters
import io.scanbot.sdk.geometry.AspectRatio
import io.scanbot.sdk.image.ImageRef
import io.scanbot.sdk.util.isDefault
import io.scanbot.sdk.util.toImageRef


class ImageStraighteningSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        importImagesFromLibrary()
    }

    private val scanbotSDK = ScanbotSDK(this@ImageStraighteningSnippet)
    private val context = this

    private val pictureForDocDetectionResult =
        this.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == RESULT_OK) {
                activityResult.data?.let { imagePickerResult ->
                    lifecycleScope.launch {
                        withContext(Dispatchers.Default) {
                            getUrisFromGalleryResult(imagePickerResult)
                                .asSequence() // process images one by one instead of collecting the whole list - less memory consumption
                                .map { it.toImageRef(contentResolver).getOrNull() }
                                .forEach { image ->
                                    if (image == null) {
                                        Log.e(
                                            "StraighteningSnippet",
                                            "Failed to load image from URI"
                                        )
                                        return@forEach
                                    }
                                    startStraightening(image)
                                }
                        }
                    }
                }
            }
        }

    // @Tag("Direct Document straightening on image")
    fun startStraightening(imageRef: ImageRef) {
       scanbotSDK.createDocumentEnhancer().onSuccess { enhancer ->
           val params =   DocumentStraighteningParameters(
               straighteningMode = DocumentStraighteningMode.STRAIGHTEN,
               // Expected aspect ratios for the documents. Comment if unknown.
               aspectRatios = listOf(AspectRatio(3.0, 4.0))
           )
           enhancer.straighten(imageRef, params).onSuccess { straightenedImage ->
               // straightenedImage is an ImageRef of the straightened image, you can display it in the UI or save it to storage
           }
       }
    }
    // @EndTag("Direct Document straightening on image")

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

