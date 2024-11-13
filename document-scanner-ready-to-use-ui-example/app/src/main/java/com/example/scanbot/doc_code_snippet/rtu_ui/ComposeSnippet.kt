package com.example.scanbot.doc_code_snippet.rtu_ui


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import io.scanbot.common.AspectRatio
import io.scanbot.sdk.ui_v2.common.FinderStyle
import io.scanbot.sdk.ui_v2.common.ScanbotColor
import io.scanbot.sdk.ui_v2.document.DocumentScannerView
import io.scanbot.sdk.ui_v2.document.configuration.AcknowledgementMode
import io.scanbot.sdk.ui_v2.document.configuration.DocumentScanningFlow


class ComposeSnippet : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ComposeView(this).apply {
            setContent {
                DocumentScannerView(
                    configuration = getConfiguration(),
                    onDocumentSubmitted = { document ->
                        // Handle the document.
                    },
                    onDocumentScannerClosed = { reason ->
                        // Indicates that the cancel button was tapped.
                    }
                )
            }
        })
    }


    fun getConfiguration(): DocumentScanningFlow {
        // Create the default configuration object.
        return DocumentScanningFlow().apply {
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
    }
}

