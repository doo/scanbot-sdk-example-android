package com.example.scanbot.doc_code_snippet.rtu_ui


import android.app.Activity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.sdk.ui_v2.common.ScanbotColor
import io.scanbot.sdk.ui_v2.document.DocumentScannerActivity
import io.scanbot.sdk.ui_v2.document.configuration.DocumentScanningFlow
import io.scanbot.sdk.ui_v2.document.configuration.PageSnapFeedbackMode
import io.scanbot.sdk.ui_v2.document.configuration.UserGuidanceVisibility


private class ScanningScreenSnippet : AppCompatActivity() {
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
            // MARK: Set the limit for the number of pages you want to scan.
            outputSettings.pagesScanLimit = 30

            // Pass the DOCUMENT_UUID here to resume an old session, or pass null to start a new session or to resume a draft session.
            documentUuid = null

            // Controls whether to resume an existing draft session or start a new one when DOCUMENT_UUID is null.
            cleanScanningSession = true

            // MARK: Configure the bottom bar and the bottom bar buttons.
            // Set the background color of the bottom bar.
            appearance.bottomBarBackgroundColor = ScanbotColor(value = "#C8193C")

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
                bottomBar.importButton.visible = true
                bottomBar.importButton.title.visible = true
                bottomBar.importButton.title.text = "Import"

                // Configure the auto/manual snap button.
                bottomBar.autoSnappingModeButton.title.visible = true
                bottomBar.autoSnappingModeButton.title.text = "Auto"
                bottomBar.manualSnappingModeButton.title.visible = true
                bottomBar.manualSnappingModeButton.title.text = "Manual"

                // Configure the torch off/on button.
                bottomBar.torchOnButton.title.visible = true
                bottomBar.torchOnButton.title.text = "On"
                bottomBar.torchOffButton.title.visible = true
                bottomBar.torchOffButton.title.text = "Off"


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

