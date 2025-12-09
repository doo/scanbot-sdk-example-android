package com.example.scanbot.doc_code_snippet.rtu_ui


import android.app.Activity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.common.Result
import io.scanbot.common.onCancellation
import io.scanbot.common.onFailure
import io.scanbot.common.onSuccess
import io.scanbot.sdk.ui_v2.document.DocumentScannerActivity
import io.scanbot.sdk.ui_v2.document.configuration.DocumentScanningFlow


class LocalizationSnippet : AppCompatActivity() {
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

            // Configure the strings.
            localization.cameraTopBarTitle = "document.camera.title"
            localization.reviewScreenSubmitButtonTitle = "review.submit.title"
            localization.cameraUserGuidanceNoDocumentFound = "camera.userGuidance.noDocumentFound"
            localization.cameraUserGuidanceTooDark = "camera.userGuidance.tooDark"

        }

        // Start the recognizer activity.
        documentScannerResult.launch(configuration)
    }
}

