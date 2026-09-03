package com.example.scanbot.doc_code_snippet.rtu_ui


import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import io.scanbot.common.onCancellation
import io.scanbot.common.onFailure
import io.scanbot.common.onSuccess
import io.scanbot.sdk.imageprocessing.DocumentCleanupConfiguration
// @Tag("Document Cleanup Screen")
import io.scanbot.sdk.ui_v2.common.ScanbotColor
import io.scanbot.sdk.ui_v2.document.DocumentScannerActivity
import io.scanbot.sdk.ui_v2.document.configuration.DocumentScanningFlow


/**
 * Configures and launches the Scanbot Document Scanner RTU UI v2 with a customized
 * Document Cleanup screen. The cleanup screen is opened from the Review screen via the
 * `documentCleanupButton` and lets the user manually erase parts of a scanned page.
 */
class DocumentCleanupScreenSnippet : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on a button click.
        startScanning()
    }

    private val documentScannerResult: ActivityResultLauncher<DocumentScanningFlow> by lazy {
        registerForActivityResult(DocumentScannerActivity.ResultContract()) { result ->
            result.onSuccess { document ->
                // Handle the scanned document. Cleanup edits are persisted on the page.
            }.onCancellation {
                // The user closed the scanner without confirming.
            }.onFailure {
                when (it) {
                    is io.scanbot.common.Result.InvalidLicenseError -> {
                        // Indicates that the Scanbot SDK license is invalid.
                    }

                    else -> {
                        // Handle other errors.
                    }
                }
            }
        }
    }

    fun startScanning() {
        // Create the default configuration object.
        val configuration = DocumentScanningFlow().apply {

            // The Document Cleanup screen is reachable from the Review screen via the
            // `documentCleanupButton`. Make sure the review screen is enabled and the button
            // is visible.
            // NOTE: toolBar only shows 5 items minimum, and dynamically hides the rest in a popup menu.
            // On a standard smartphone device, the cleanup button is usually reachable only from the 'More' menu.
            screens.review.apply {
                enabled = true

                toolBar.documentCleanupButton.barButton.visible = true

                // Optionally style the button.
                toolBar.documentCleanupButton.barButton.title.color =
                    ScanbotColor(color = Color.White)
                // OR - optionally style the relevant popup-menu item.
                toolBar.documentCleanupButton.popupMenuItem.title.color =
                    ScanbotColor(color = Color.Black)
            }

            // Retrieve the cleanup screen configuration from the main configuration object.
            screens.cleanup.apply {
                this.engineConfiguration = DocumentCleanupConfiguration(
                    keepText = false,  // If true, the cleanup tool will not allow erasing text. But it takes some time to process OCR on the image initially,
                    maxUndoRedoStackSize = 4, // The maximum number of undo/redo operations that can be performed. Make it less to save up memory
                    maxCleanupResolution = 1000,  // Downscale stroke area to this value in pixels to speed up the cleanup process. The smaller tha value the faster but quality will be lower too.
                )
                // Customize the top bar.
                topBarTitle.text = "Clean up the page"
                topBarBackButton.text = "Cancel"
                topBarConfirmButton.text = "Done"

                // Background color of the canvas behind the image.
                backgroundColor = ScanbotColor(value = "#222222")

                // Configure the tool bar buttons (undo / redo / reset).
                toolBar.undoButton.title.text = "Undo"
                toolBar.redoButton.title.text = "Redo"
                toolBar.resetButton.title.text = "Reset"

                // Configure the stroke-size slider.
                toolBar.strokeSizeSlider.apply {
                    visible = true
                    minStrokeSize = 1
                    maxStrokeSize = 50
                    title.text = "Brush size"
                }

                // Customize the stroke-size indicator (the round preview of the current brush).
                strokeSizeIndicator.apply {
                    backgroundColor = ScanbotColor(value = "#00C853")
                    borderColor = ScanbotColor(value = "#FFFFFF")
                    opacity = 0.9
                }

                // Optional: show an introduction screen the first time the user opens cleanup.
                introduction.showAutomatically = true

                // Customize the alert dialogs shown for Reset and for cancelling with unsaved
                // changes.
                resetAllEditsAlertDialog.title.text = "Reset all edits?"
                resetAllEditsAlertDialog.subtitle.text =
                    "This will revert all cleanup operations on this page."

                discardChangesAlertDialog.title.text = "Discard changes?"
                discardChangesAlertDialog.subtitle.text =
                    "Your cleanup edits on this page will be lost."
            }

            // Optionally customize text shown by the cleanup screen via the shared localization
            // object (useful when not using string resources).
            localization.documentCleanupScreenTitle = "Clean up the page"
            localization.documentCleanupUndoButtonTitle = "Undo"
            localization.documentCleanupRedoButtonTitle = "Redo"
            localization.documentCleanupResetButtonTitle = "Reset"
        }

        // Start the scanner activity.
        documentScannerResult.launch(configuration)
    }
}
// @EndTag("Document Cleanup Screen")
