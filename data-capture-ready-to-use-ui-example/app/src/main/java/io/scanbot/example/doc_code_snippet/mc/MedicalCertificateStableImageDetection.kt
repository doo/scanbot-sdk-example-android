package io.scanbot.example.doc_code_snippet.mc

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.scanbot.common.onSuccess
import io.scanbot.example.util.*
import io.scanbot.sdk.*
import io.scanbot.sdk.image.ImageRef
import io.scanbot.sdk.mc.*
import io.scanbot.sdk.medicalcertificate.MedicalCertificateScanner
import io.scanbot.sdk.medicalcertificate.MedicalCertificateScanningParameters
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
class DataExtractorStableImageDetection : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        importImagesFromLibrary()
    }

    private val scanbotSDK = ScanbotSDK(this@DataExtractorStableImageDetection)
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
                                .forEach { bitmap ->
                                    if (bitmap == null) {
                                        Log.e(
                                            "Snippet",
                                            "Failed to load bitmap from URI"
                                        )
                                        return@forEach
                                    }
                                    processImage(medicalCertificateScanner, bitmap)
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

    // @Tag("Extracting medical certificate data from an image")
    // Create a medical certificate scanner instance
    val medicalCertificateScanner = scanbotSDK.createMedicalCertificateScanner().getOrThrow()

    fun processImage(
        medicalCertificateScanner: MedicalCertificateScanner,
        image: ImageRef
    ) {
        val result = medicalCertificateScanner.run(
            image,
            parameters = MedicalCertificateScanningParameters(
                shouldCropDocument = true,
                extractCroppedImage = true,
                recognizePatientInfoBox = true,
                recognizeBarcode = true
            )
        ).onSuccess {
            if (it.scanningSuccessful) {
                // Document scanning results are processed
                // processResul
            }
        }

    }
    // @EndTag("Extracting medical certificate data from an image")
}
