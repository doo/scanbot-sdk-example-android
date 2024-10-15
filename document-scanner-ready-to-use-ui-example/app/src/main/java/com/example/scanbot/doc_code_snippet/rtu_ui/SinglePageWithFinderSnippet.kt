package com.example.scanbot.doc_code_snippet.rtu_ui


import android.app.Activity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.sdk.ui_v2.common.AspectRatio
import io.scanbot.sdk.ui_v2.common.FinderStyle
import io.scanbot.sdk.ui_v2.common.ScanbotColor
import io.scanbot.sdk.ui_v2.document.DocumentScannerActivity
import io.scanbot.sdk.ui_v2.document.configuration.AcknowledgementMode
import io.scanbot.sdk.ui_v2.document.configuration.DocumentScanningFlow


class SinglePageWithFinderSnippet : AppCompatActivity() {
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
            // Set the visibility of the view finder.
            screens.camera.viewFinder.visible = true


            screens.camera.viewFinder.apply {
                // Create the instance of the style, either `FinderCorneredStyle` or `FinderStrokedStyle`.
                // Set the configured style.
                style = FinderStyle.finderCorneredStyle().apply {
                    this.strokeColor = ScanbotColor(value = "#FFFFFFFF")
                    this.strokeWidth = 3.0
                    this.cornerRadius = 10.0
                }

                // Set the desired aspect ratio of the view finder.
                aspectRatio = AspectRatio(width = 1.0, height = 1.0)

                // Set the overlay color.
                overlayColor = ScanbotColor(value = "#26000000")

            }

            // Set the page limit.
            outputSettings.pagesScanLimit = 1

            // Enable the tutorial screen.
            screens.camera.introduction.showAutomatically = true

            // Disable the acknowledgment screen.
            screens.camera.acknowledgement.acknowledgementMode = AcknowledgementMode.NONE

            // Disable the review screen.
            screens.review.enabled = false
        }

        // Start the recognizer activity.
        documentScannerResult.launch(configuration)
    }
}

