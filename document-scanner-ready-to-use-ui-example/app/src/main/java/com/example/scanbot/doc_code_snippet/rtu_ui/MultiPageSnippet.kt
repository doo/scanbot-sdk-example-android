package com.example.scanbot.doc_code_snippet.rtu_ui


import android.app.Activity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.common.Result
import io.scanbot.common.onCancellation
import io.scanbot.common.onFailure
import io.scanbot.common.onSuccess
// @Tag("Multi Page")
import io.scanbot.sdk.ui_v2.document.DocumentScannerActivity
import io.scanbot.sdk.ui_v2.document.configuration.AcknowledgementMode
import io.scanbot.sdk.ui_v2.document.configuration.DocumentScanningFlow


class MultiPageSnippet : AppCompatActivity() {
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
            }.onCancellation {
                // Indicates that the cancel button was tapped. Or screen is closed by other reason.
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
            outputSettings.pagesScanLimit = 0

            // Enable the acknowledgment screen.
            screens.camera.acknowledgement.acknowledgementMode = AcknowledgementMode.NONE

        }

        // Start the recognizer activity.
        documentScannerResult.launch(configuration)
    }
}
// @EndTag("Multi Page")

