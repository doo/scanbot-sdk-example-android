package io.scanbot.example.doc_code_snippet.creditcard

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.scanbot.example.util.*
import io.scanbot.sdk.*
import io.scanbot.sdk.creditcard.*
import io.scanbot.sdk.image.ImageRef
import io.scanbot.sdk.ui_v2.document.utils.toImageRef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/*
    NOTE: this snippet of code is to be used only as a part of the website documentation.
    This code is not intended for any use outside of the support of documentation by Scanbot SDK GmbH employees.
*/

// NOTE for maintainers: whenever changing this code,
// ensure that links using it are still pointing to valid lines!
// Pay attention to imports adding/removal/sorting!
// Page URLs using this code:
// TODO: add URLs here

val licenseKey = "YOUR_SCANBOT_SDK_LICENSE_KEY"

// @Tag("Initialize SDK")
class ExampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize the Scanbot Scanner SDK:
        ScanbotSDKInitializer()
            .license(this, licenseKey)
            // TODO: other configuration calls
            .prepareOCRLanguagesBlobs(true)
            .initialize(this)
    }
}
// @EndTag("Initialize SDK")

fun createCreditCardScannerSnippet(context: Context) {
    // @Tag("Create Credit Card Scanner")
    val scanbotSDK = ScanbotSDK(context)
    val scanner = scanbotSDK.createCreditCardScanner()
    // @EndTag("Create Credit Card Scanner")
}

class CreditCardStableImageDetection : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        importImagesFromLibrary()
    }

    private val scanbotSDK = ScanbotSDK(this@CreditCardStableImageDetection)
    private val context = this

    private val pictureForDocDetectionResult =
        this.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                activityResult.data?.let { imagePickerResult ->
                    lifecycleScope.launch {
                        withContext(Dispatchers.Default) {
                            getUrisFromGalleryResult(imagePickerResult)
                                .asSequence() // process images one by one instead of collecting the whole list - less memory consumption
                                .map { it.toImageRef(contentResolver) }
                                .forEach { imageRef ->
                                    if (imageRef == null) {
                                        Log.e(
                                            "Snippet",
                                            "Failed to load imageRef from URI"
                                        )
                                        return@forEach
                                    }
                                    creditCardScanner?.let { processImage(it, imageRef) }
                                }

                        }
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

    // @Tag("Extracting credit card data from an image")
    // Create a data extractor  instance
    val creditCardScanner = scanbotSDK.createCreditCardScanner().getOrNull()

    private fun processImage(scanner: CreditCardScanner, image: ImageRef) {
        val result = scanner.run(image)
        // Proceed MRZ scanner result
        // processResult(result)
    }
    // @EndTag("Extracting credit card data from an image")
}


