package com.example.scanbot.doc_code_snippet


import android.app.Activity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.sdk.ui.registerForActivityResultOk
import io.scanbot.sdk.ui_v2.common.ScanbotColor
import io.scanbot.sdk.ui_v2.common.StyledText
import io.scanbot.sdk.ui_v2.document.DocumentScannerActivity
import io.scanbot.sdk.ui_v2.document.configuration.AcknowledgementMode
import io.scanbot.sdk.ui_v2.document.configuration.DocumentScanningFlow
import io.scanbot.sdk.ui_v2.document.configuration.IntroImage
import io.scanbot.sdk.ui_v2.document.configuration.IntroListEntry
import io.scanbot.sdk.ui_v2.document.configuration.PageSnapFeedbackMode
import io.scanbot.sdk.ui_v2.document.configuration.UserGuidanceVisibility


private class MultiPageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //run this function on button click
        startScanning()
    }

    private val context = this
    private val documentScannerResult: ActivityResultLauncher<DocumentScanningFlow> =
        registerForActivityResult(DocumentScannerActivity.ResultContract(context)) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.result?.let { document ->
                    // Handle the document.
                }
            } else {
                // Indicates that the cancel button was tapped.
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

