package com.example.scanbot.doc_code_snippet.rtu_ui


import android.app.Activity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.sdk.documentqualityanalyzer.DocumentQuality
// @Tag("Acknowledge Screen")
import io.scanbot.sdk.ui_v2.common.ScanbotColor
import io.scanbot.sdk.ui_v2.document.DocumentScannerActivity
import io.scanbot.sdk.ui_v2.document.configuration.AcknowledgementMode
import io.scanbot.sdk.ui_v2.document.configuration.DocumentScanningFlow


class AcknowledgeScreenSnippet : AppCompatActivity() {

    private val context = this
    private val documentScannerResult: ActivityResultLauncher<DocumentScanningFlow> by lazy {
        registerForActivityResult(DocumentScannerActivity.ResultContract()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.result?.let { document ->
                    // Handle the document.
                }
            } else {
                // Indicates that the cancel button was tapped.
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }

    fun startScanning() {
        // Create the default configuration object.
        val configuration = DocumentScanningFlow().apply {

            screens.camera.acknowledgement.apply {

                // Set the acknowledgment mode
                // Modes:
                // - `ALWAYS`: Runs the quality analyzer on the captured document and always displays the acknowledgment screen.
                // - `BAD_QUALITY`: Runs the quality analyzer and displays the acknowledgment screen only if the quality is poor.
                // - `NONE`: Skips the quality check entirely.
                acknowledgementMode = AcknowledgementMode.ALWAYS

                // Set the minimum acceptable document quality.
                // Options: excellent, good, reasonable, poor, veryPoor, or noDocument.
                minimumQuality = DocumentQuality.GOOD

                // Set the background color for the acknowledgment screen.
                backgroundColor = ScanbotColor(value = "#EFEFEF")

                // You can also configure the buttons in the bottom bar of the acknowledgment screen.
                // e.g To force the user to retake, if the captured document is not OK.
                bottomBar.acceptWhenNotOkButton.visible = false

                // Hide the titles of the buttons.
                bottomBar.acceptWhenNotOkButton.title.visible = false
                bottomBar.acceptWhenOkButton.title.visible = false
                bottomBar.retakeButton.title.visible = false

                // Configure the acknowledgment screen's hint message which is shown if the least acceptable quality is not met.
                badImageHint.visible = true
            }
        }

        // Start the recognizer activity.
        documentScannerResult.launch(configuration)
    }
}
// @EndTag("Acknowledge Screen")

