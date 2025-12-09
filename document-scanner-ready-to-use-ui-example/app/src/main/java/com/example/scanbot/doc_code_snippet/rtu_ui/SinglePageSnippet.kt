package com.example.scanbot.doc_code_snippet.rtu_ui


import android.app.Activity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.common.Result
import io.scanbot.common.onFailure
import io.scanbot.common.onSuccess
// @Tag("Single Page")
import io.scanbot.sdk.ui_v2.document.DocumentScannerActivity
import io.scanbot.sdk.ui_v2.document.configuration.AcknowledgementMode
import io.scanbot.sdk.ui_v2.document.configuration.DocumentScanningFlow


class SinglePageSnippet : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }

    private val context = this
    private val documentScannerResult: ActivityResultLauncher<DocumentScanningFlow> by lazy {
        registerForActivityResult(DocumentScannerActivity.ResultContract()) { result ->
            result.onSuccess { document ->
                // Handle the scanned document.
            }.onFailure {
                when (it) {
                    is io.scanbot.common.Result.InvalidLicenseError -> {
                        // indicate that the Scanbot SDK license is invalid
                    }

                    else -> {
                        // Handle other errors
                    }
                }
            }
        }
    }

    fun startScanning() {
        // Create the default configuration object.
        val configuration = DocumentScanningFlow().apply {

            // Set the page limit.
            outputSettings.pagesScanLimit = 1

            // Disable the tutorial screen.
            screens.camera.introduction.showAutomatically = false

            // Enable the acknowledgment screen.
            screens.camera.acknowledgement.acknowledgementMode = AcknowledgementMode.ALWAYS

            // Disable the review screen.
            screens.review.enabled = false

        }

        // Start the recognizer activity.
        documentScannerResult.launch(configuration)
    }
}
// @EndTag("Single Page")

