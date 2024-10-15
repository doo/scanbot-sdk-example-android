package com.example.scanbot.doc_code_snippet.rtu_ui


import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.sdk.ui_v2.common.ScanbotColor
import io.scanbot.sdk.ui_v2.document.DocumentScannerActivity
import io.scanbot.sdk.ui_v2.document.configuration.DocumentScanningFlow


class CroppingScreenSnippet : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }

    private val context = this
    private val documentScannerResult: ActivityResultLauncher<DocumentScanningFlow> by lazy {
        registerForActivityResult(DocumentScannerActivity.ResultContract(context)) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.result?.let { document ->
                    // Handle the document.
                }
            } else {
                // Indicates that the cancel button was tapped.
            }
        }
    }

    fun startScanning() {
        // Create the default configuration object.
        val configuration = DocumentScanningFlow().apply {
            // MARK: Set the limit for the number of pages you want to scan.
            outputSettings.pagesScanLimit = 30

            // Pass the DOCUMENT_UUID here to resume an old session, or pass null to start a new session or to resume a draft session.
            documentUuid = null

            // Controls whether to resume an existing draft session or start a new one when DOCUMENT_UUID is null.
            cleanScanningSession = true

            // MARK: Configure the bottom bar and the bottom bar buttons.
            // Set the background color of the bottom bar.
            appearance.bottomBarBackgroundColor = ScanbotColor(value = "#C8193C")
            // e.g. configure .
            appearance.topBarBackgroundColor = ScanbotColor(color = Color.RED)
            // Retrieve the camera screen configuration.

            // e.g. customize a UI element's text
            localization.croppingTopBarCancelButtonTitle = "Cancel"
            screens.cropping.apply {
                // e.g disable the rotation feature.
                bottomBar.rotateButton.visible = false

                topBarConfirmButton.foreground.color =
                    ScanbotColor(color = Color.WHITE)
            }

        }

        // Start the recognizer activity.
        documentScannerResult.launch(configuration)
    }
}

