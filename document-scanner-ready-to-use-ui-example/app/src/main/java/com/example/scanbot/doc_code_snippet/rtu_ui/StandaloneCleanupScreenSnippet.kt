package com.example.scanbot.doc_code_snippet.rtu_ui


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
import io.scanbot.common.onCancellation
import io.scanbot.common.onFailure
import io.scanbot.common.onSuccess
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.docprocessing.Document
import io.scanbot.sdk.ui_v2.common.ScanbotColor
import io.scanbot.sdk.ui_v2.document.DocumentCleanupActivity
import io.scanbot.sdk.ui_v2.document.configuration.DocumentCleanupStandaloneConfiguration
import io.scanbot.sdk.util.toImageRef


class StandaloneCleanupScreenSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click.
        importImagesFromLibrary()
    }

    private val scanbotSDK = ScanbotSDK(this@StandaloneCleanupScreenSnippet)
    private val context = this

    private val pictureForDocDetectionResult =
        this.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == RESULT_OK) {
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
                                                "StandaloneCleanupSnippet",
                                                "Failed to load image from URI"
                                            )
                                            return@forEach
                                        }
                                        document.addPage(image)
                                    }
                                startCleanup(document)
                            }
                        }
                    }
                }
            }
        }

    // @Tag("Using Cleanup UI")
    private val cleanupResult: ActivityResultLauncher<DocumentCleanupStandaloneConfiguration> =
        registerForActivityResult(DocumentCleanupActivity.ResultContract()) { result ->
            result.onSuccess { result ->
                // Retrieve the  document.
                val document =
                    ScanbotSDK(this@StandaloneCleanupScreenSnippet).documentApi.loadDocument(
                        documentId = result.documentUuid
                    ).onSuccess { document ->
                        val page = document.pageWithId(result.pageUuid)
                        // Proceed the page as needed.
                    }
            }.onCancellation {
                // Indicates that the cancel button was tapped. Or screen is closed by other reason.
            }.onFailure {
                when (it) {
                    is Result.InvalidLicenseError -> {
                        // indicate that the Scanbot SDK license is invalid
                    }

                    else -> {
                        // Handle other errors
                    }
                }
            }
        }

    fun startCleanup(document: Document) {
        // Retrieve the selected document page.
        val page = document.pages.getOrNull(0) ?: return
        // Create the default configuration object.
        val configuration =
            DocumentCleanupStandaloneConfiguration(
                documentUuid = document.uuid,
                pageUuid = page.uuid
            ).apply {
                // e.g disable stroke size slider.
                cleanup.toolbar.strokeSizeSlider.visible = false

                // e.g. configure various colors.
                appearance.topBarBackgroundColor = ScanbotColor(color = Color.RED)
                cleanup.topBarConfirmButton.foreground.color = ScanbotColor(color = Color.WHITE)

                // e.g. customize a UI element's text.
                localization.documentCleanupTopBarCancelButtonTitle = "Cancel"

            }

        // Start the recognizer activity.
        cleanupResult.launch(configuration)
    }
// @EndTag("Using Cleanup UI")

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

