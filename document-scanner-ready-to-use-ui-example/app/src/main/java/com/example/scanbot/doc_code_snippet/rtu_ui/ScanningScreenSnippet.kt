package com.example.scanbot.doc_code_snippet.rtu_ui


import android.app.Activity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.common.Result
import io.scanbot.common.onCancellation
import io.scanbot.common.onFailure
import io.scanbot.common.onSuccess
// @Tag("Scanning Screen")
import io.scanbot.sdk.ui_v2.common.ScanbotColor
import io.scanbot.sdk.ui_v2.document.DocumentScannerActivity
import io.scanbot.sdk.ui_v2.document.configuration.DocumentScanningFlow
import io.scanbot.sdk.ui_v2.document.configuration.PageSnapFeedbackMode
import io.scanbot.sdk.ui_v2.document.configuration.UserGuidanceVisibility


class ScanningScreenSnippet : AppCompatActivity() {
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
            // MARK: Set the limit for the number of pages you want to scan.
            outputSettings.pagesScanLimit = 30

            // Pass the DOCUMENT_UUID here to resume an old session, or pass null to start a new session or to resume a draft session.
            documentUuid = null

            // Controls whether to resume an existing draft session or start a new one when DOCUMENT_UUID is null.
            cleanScanningSession = true

            // MARK: Configure the tool bar and the tool bar buttons.
            // Set the background color of the tool bar.
            appearance.toolbarBackgroundColor = ScanbotColor(value = "#C8193C")

            // Retrieve the camera screen configuration.
            screens.camera.apply {
                // MARK: Configure the user guidance.
                // Configure the top user guidance.
                topUserGuidance.visible = true
                topUserGuidance.background.fillColor = ScanbotColor(value = "#4A000000")
                topUserGuidance.title.text = "Scan your document"

                // Configure the bottom user guidance.
                userGuidance.visibility = UserGuidanceVisibility.ENABLED
                userGuidance.background.fillColor = ScanbotColor(value = "#4A000000")
                userGuidance.title.text = "Please hold your device over a document"

                // Configure the the scanning assistance overlay.
                scanAssistanceOverlay.visible = true
                scanAssistanceOverlay.backgroundColor = ScanbotColor(value = "#4A000000")
                scanAssistanceOverlay.foregroundColor = ScanbotColor(value = "#FFFFFF")

                // Configure the title of the bottom user guidance for different states.
                userGuidance.statesTitles.noDocumentFound = "No Document"
                userGuidance.statesTitles.badAspectRatio = "Bad Aspect Ratio"
                userGuidance.statesTitles.badAngles = "Bad angle"
                userGuidance.statesTitles.textHintOffCenter = "The document is off center"
                userGuidance.statesTitles.tooSmall = "The document is too small"
                userGuidance.statesTitles.tooNoisy = "The document is too noisy"
                userGuidance.statesTitles.tooDark = "Need more light"
                userGuidance.statesTitles.energySaveMode = "Energy save mode is active"
                userGuidance.statesTitles.readyToCapture = "Ready to capture"
                userGuidance.statesTitles.capturing = "Capturing the document"

                // The title of the user guidance when the document ready to be captured in manual mode.
                userGuidance.statesTitles.captureManual = "The document is ready to be captured"

                // Import button is used to import image from the gallery.
                toolbar.importButton.visible = true
                toolbar.importButton.title.visible = true
                toolbar.importButton.title.text = "Import"

                // Configure the auto/manual snap button.
                toolbar.autoSnappingModeButton.title.visible = true
                toolbar.autoSnappingModeButton.title.text = "Auto"
                toolbar.manualSnappingModeButton.title.visible = true
                toolbar.manualSnappingModeButton.title.text = "Manual"

                // Configure the torch off/on button.
                toolbar.torchOnButton.title.visible = true
                toolbar.torchOnButton.title.text = "On"
                toolbar.torchOffButton.title.visible = true
                toolbar.torchOffButton.title.text = "Off"


                // MARK: Configure the document capture feedback.
                // Configure the camera blink behavior when an image is captured.
                captureFeedback.cameraBlinkEnabled = true

                // Configure the animation mode. You can choose between a checkmark animation or a document funnel animation.
                // Configure the checkmark animation. You can use the default colors or set your own desired colors for the checkmark.
                captureFeedback.snapFeedbackMode = PageSnapFeedbackMode.pageSnapCheckMarkAnimation()

                // Or you can choose the funnel animation.
                captureFeedback.snapFeedbackMode = PageSnapFeedbackMode.pageSnapFunnelAnimation()

            }


        }

        // Start the recognizer activity.
        documentScannerResult.launch(configuration)
    }
}
// @EndTag("Scanning Screen")

