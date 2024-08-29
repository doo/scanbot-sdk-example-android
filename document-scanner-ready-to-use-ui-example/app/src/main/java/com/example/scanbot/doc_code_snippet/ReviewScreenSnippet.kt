package com.example.scanbot.doc_code_snippet


import android.app.Activity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import io.scanbot.sdk.ui_v2.common.ScanbotColor
import io.scanbot.sdk.ui_v2.document.DocumentScannerActivity
import io.scanbot.sdk.ui_v2.document.configuration.DocumentScanningFlow


private class ReviewScreenSnippet : AppCompatActivity() {
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
            // Retrieve the instance of the review configuration from the main configuration object.
            screens.review.apply {

                // Enable / Disable the review screen.
                enabled = true

                // Hide the zoom button.
                zoomButton.visible = false

                // Hide the add button.
                bottomBar.addButton.visible = false
            }
            // Retrieve the instance of the reorder pages configuration from the main configuration object.
            screens.reorderPages.apply {

                // Hide the guidance view.
                guidance.visible = false

                // Set the title for the reorder screen.
                topBarTitle.text = "Reorder Pages Screen"
            }
            // Retrieve the instance of the cropping configuration from the main configuration object.
            screens.cropping.apply {

                // Hide the reset button.
                bottomBar.resetButton.visible = false
            }
            // Retrieve the retake button configuration from the main configuration object.
            screens.review.apply {

                // Show the retake button.
                bottomBar.retakeButton.visible = true

                // Configure the retake title color.
                bottomBar.retakeButton.title.color = ScanbotColor(color = Color.Black)
            }
        }

        // Start the recognizer activity.
        documentScannerResult.launch(configuration)
    }
}

