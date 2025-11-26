package com.example.scanbot.doc_code_snippet.rtu_ui


import android.app.Activity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.common.Result
import io.scanbot.common.onFailure
import io.scanbot.common.onSuccess
// @Tag("Introduction")
import io.scanbot.sdk.ui_v2.common.ScanbotColor
import io.scanbot.sdk.ui_v2.common.StyledText
import io.scanbot.sdk.ui_v2.document.DocumentScannerActivity
import io.scanbot.sdk.ui_v2.document.configuration.DocumentScanningFlow
import io.scanbot.sdk.ui_v2.document.configuration.IntroImage
import io.scanbot.sdk.ui_v2.document.configuration.IntroListEntry


class IntroductionSnippet : AppCompatActivity() {
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
            }.onFailure {
                when (it) {
                    is io.scanbot.common.Result.InvalidLicenseError -> {
                        // indicate that the Scanbot SDK license is invalid
                    }

                    is Result.OperationCanceledError -> {
                        // Indicates that the cancel button was tapped. or screen is closed by other reason.
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

            // Retrieve the instance of the introduction configuration from the main configuration object.
            screens.camera.introduction.apply {

                // Show the introduction screen automatically when the screen appears.
                showAutomatically = true

                // Create a new introduction item.
                val firstExampleEntry = IntroListEntry()

                // Configure the introduction image to be shown.
                firstExampleEntry.image = IntroImage.receiptsIntroImage()

                // Configure the text.
                firstExampleEntry.text = StyledText(
                    text = "Some text explaining how to scan a receipt",
                    color = ScanbotColor(value = "#000000")
                )

                // Create a second introduction item.
                val secondExampleEntry = IntroListEntry()

                // Configure the introduction image to be shown.
                secondExampleEntry.image = IntroImage.checkIntroImage()

                // Configure the text.
                secondExampleEntry.text =
                    StyledText(
                        text = "Some text explaining how to scan a check",
                        color = ScanbotColor(value = "#000000")
                    )

                // Set the items into the configuration.
                items = listOf(firstExampleEntry, secondExampleEntry)

                // Set a screen title.
                title = StyledText(
                    text = "Introduction",
                    color = ScanbotColor(value = "#000000")
                )

            }
        }

        // Start the recognizer activity.
        documentScannerResult.launch(configuration)
    }
}
// @EndTag("Introduction")

