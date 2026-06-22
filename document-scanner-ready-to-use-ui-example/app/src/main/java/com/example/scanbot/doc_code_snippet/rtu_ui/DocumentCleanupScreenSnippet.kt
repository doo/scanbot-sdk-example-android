package com.example.scanbot.doc_code_snippet.rtu_ui


import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import io.scanbot.common.onCancellation
import io.scanbot.common.onFailure
import io.scanbot.common.onSuccess
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
            screens.review.apply {
                enabled = true
                bottomBar.documentCleanupButton.visible = true
                // Optionally style the button.
                bottomBar.documentCleanupButton.title.color =
                    ScanbotColor(color = Color.White)
            }

            // Retrieve the cleanup screen configuration from the main configuration object.
            screens.cleanup.apply {

                // Customize the top bar.
                topBarTitle.text = "Clean up the page"
                topBarBackButton.text = "Cancel"
                topBarConfirmButton.text = "Done"

                // Background color of the canvas behind the image.
                backgroundColor = ScanbotColor(value = "#222222")

                // Configure the bottom bar buttons (undo / redo / reset).
                bottomBar.undoButton.title.text = "Undo"
                bottomBar.redoButton.title.text = "Redo"
                bottomBar.resetButton.title.text = "Reset"

                // Configure the stroke-size scrollbar.
                bottomBar.strokeSizeScrollbar.apply {
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
                introduction.visible = true

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
