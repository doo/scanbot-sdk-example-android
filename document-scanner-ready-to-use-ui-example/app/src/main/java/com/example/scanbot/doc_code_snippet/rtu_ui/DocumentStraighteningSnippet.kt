package com.example.scanbot.doc_code_snippet.rtu_ui


import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.common.Result
import io.scanbot.common.onCancellation
import io.scanbot.common.onFailure
import io.scanbot.common.onSuccess
import io.scanbot.sdk.documentscanner.DocumentStraighteningMode
import io.scanbot.sdk.documentscanner.DocumentStraighteningParameters
import io.scanbot.sdk.geometry.AspectRatio

import io.scanbot.sdk.ui_v2.document.DocumentScannerActivity
import io.scanbot.sdk.ui_v2.document.configuration.DocumentScanningFlow


class DocumentStraighteningSnippet : AppCompatActivity() {
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
                    is Result.InvalidLicenseError -> {
                        // indicate that the Scanbot SDK license is invalid
                    }

                    else -> {
                        // Handle other errors
                    }
                }
            }
        }
    }

    // @Tag("Set Document Straightening Parameters")
    fun startScanning() {
        // Create the default configuration object.
        val configuration = DocumentScanningFlow().apply {
            // Set document straightening parameters for the scanning screen.
            outputSettings.apply {
                straighteningParameters = DocumentStraighteningParameters(
                    straighteningMode = DocumentStraighteningMode.STRAIGHTEN,
                    // Expected aspect ratios for the documents. Comment if unknown.
                    aspectRatios = listOf(AspectRatio(3.0, 4.0))
                )
            }
        }
        // Start the recognizer activity.
        documentScannerResult.launch(configuration)
    }
    // @EndTag("Set Document Straightening Parameters")

}

