package com.example.scanbot.doc_code_snippet.rtu_ui


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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.scanbot.common.Result
import io.scanbot.common.onFailure
import io.scanbot.common.onSuccess
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.docprocessing.Document
import io.scanbot.sdk.ui_v2.common.ScanbotColor
import io.scanbot.sdk.ui_v2.document.CroppingActivity
import io.scanbot.sdk.ui_v2.document.DocumentScannerActivity
import io.scanbot.sdk.ui_v2.document.configuration.CroppingConfiguration
import io.scanbot.sdk.ui_v2.document.configuration.DocumentScanningFlow
import io.scanbot.sdk.util.toImageRef


class StandaloneCropScreenSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click.
        importImagesFromLibrary()
    }

    private val scanbotSDK = ScanbotSDK(this@StandaloneCropScreenSnippet)
    private val context = this

    private val pictureForDocDetectionResult =
        this.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                activityResult.data?.let { imagePickerResult ->
                    lifecycleScope.launch {
                        withContext(Dispatchers.Default) {
                            scanbotSDK.documentApi.createDocument().onSuccess { document ->
                                getUrisFromGalleryResult(imagePickerResult)
                                    // Process images one by one instead of collecting the whole list - less memory consumption.
                                    .asSequence()
                                    .map { it.toImageRef(contentResolver).getOrNull() }
                                    .forEach { image ->
                                        if (image == null) {
                                            Log.e(
                                                "StandaloneCropSnippet",
                                                "Failed to load image from URI"
                                            )
                                            return@forEach
                                        }
                                        document.addPage(image)
                                    }
                                startCropping(document)
                            }
                        }
                    }
                }
            }
        }

    // @Tag("Using Cropping UI")
    private val croppingResult: ActivityResultLauncher<CroppingConfiguration> =
        registerForActivityResult(CroppingActivity.ResultContract()) { result ->
            result.onSuccess { result ->
                // Retrieve the cropped document.
                val document =
                    ScanbotSDK(this@StandaloneCropScreenSnippet).documentApi.loadDocument(
                        documentId = result.documentUuid
                    ).onSuccess { document ->
                        val page = document.pageWithId(result.pageUuid)
                        // Proceed the page as needed.
                    }
            }.onFailure {
                when (it) {
                    is io.scanbot.common.Result.InvalidLicenseError -> {
                        // indicate that the Scanbot SDK license is invalid
                    }

                    is Result.OperationCanceledError -> {
                        // Indicates that the cancel button was tapped. or screen is closed by other reason.
                    }

                    else -> {
                        // Handle other errors
                    }
                }
            }
        }

    fun startCropping(document: Document) {
        // Retrieve the selected document page.
        val page = document.pages.getOrNull(0) ?: return
        // Create the default configuration object.
        val configuration =
            CroppingConfiguration(documentUuid = document.uuid, pageUuid = page.uuid).apply {
                // e.g disable the rotation feature.
                cropping.bottomBar.rotateButton.visible = false

                // e.g. configure various colors.
                appearance.topBarBackgroundColor = ScanbotColor(color = Color.RED)
                cropping.topBarConfirmButton.foreground.color = ScanbotColor(color = Color.WHITE)

                // e.g. customize a UI element's text.
                localization.croppingTopBarCancelButtonTitle = "Cancel"

            }

        // Start the recognizer activity.
        croppingResult.launch(configuration)
    }
// @EndTag("Using Cropping UI")

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

