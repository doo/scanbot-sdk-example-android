package io.scanbot.example.doc_code_snippet.data_extractor

/*
    NOTE: this snippet of code is to be used only as a part of the website documentation.
    This code is not intended for any use outside of the support of documentation by Scanbot SDK GmbH employees.
*/

// NOTE for maintainers: whenever changing this code,
// ensure that links using it are still pointing to valid lines!
// Pay attention to imports adding/removal/sorting!
// Page URLs using this code:
// TODO: add URLs here

import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import io.scanbot.example.*
import io.scanbot.sdk.*
import io.scanbot.sdk.documentdata.DocumentDataExtractorCommonConfiguration
import io.scanbot.sdk.documentdata.DocumentDataExtractorConfigurationBuilder
import io.scanbot.sdk.documentdata.entity.*
import io.scanbot.sdk.ui_v2.common.*
import io.scanbot.sdk.ui_v2.documentdata.*
import io.scanbot.sdk.ui_v2.documentdataextractor.configuration.*

//Rtu ui snippets
fun initializeScanbotSDK(application: Application) {
    // @Tag("InitializeScanbotSDK")
    ScanbotSDKInitializer().initialize(application)
    // @EndTag("InitializeScanbotSDK")
}

class StartDocumentDataExtractorUiSnippet : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }

    // @Tag("Launching the scanner")

    val resultLauncher: ActivityResultLauncher<DocumentDataExtractorScreenConfiguration> =
        registerForActivityResult(DocumentDataExtractorActivity.ResultContract()) { resultEntity: DocumentDataExtractorActivity.Result ->
            if (resultEntity.resultOk) {
                resultEntity.result?.document?.let { document ->
                    wrapGenericDocument(document)
                }
            }
        }

    private fun startScanning() {
        // Create the default configuration object.
        val configuration = DocumentDataExtractorScreenConfiguration()

        // Start the recognizer activity.
        resultLauncher.launch(configuration)
    }
    // @EndTag("Launching the scanner")
}

class DocumentDataExtractorPaletteSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }
    // @Tag("Palette")

    val resultLauncher: ActivityResultLauncher<DocumentDataExtractorScreenConfiguration> =
        registerForActivityResult(DocumentDataExtractorActivity.ResultContract()) { resultEntity: DocumentDataExtractorActivity.Result ->
            if (resultEntity.resultOk) {
                resultEntity.result?.document?.let { document ->
                    wrapGenericDocument(document)
                }
            }
        }

    private fun startScanning() {

        // Create the default configuration object.
        val configuration = DocumentDataExtractorScreenConfiguration()

        // Retrieve the instance of the palette from the configuration object.
        configuration.palette.apply {
            // Configure the colors.
            // The palette already has the default colors set, so you don't have to always set all the colors.
            sbColorPrimary = ScanbotColor("#C8193C")
            sbColorPrimaryDisabled = ScanbotColor("#F5F5F5")
            sbColorNegative = ScanbotColor("#FF3737")
            sbColorPositive = ScanbotColor("#4EFFB4")
            sbColorWarning = ScanbotColor("#FFCE5C")
            sbColorSecondary = ScanbotColor("#FFEDEE")
            sbColorSecondaryDisabled = ScanbotColor("#F5F5F5")
            sbColorOnPrimary = ScanbotColor("#FFFFFF")
            sbColorOnSecondary = ScanbotColor("#C8193C")
            sbColorSurface = ScanbotColor("#FFFFFF")
            sbColorOutline = ScanbotColor("#EFEFEF")
            sbColorOnSurfaceVariant = ScanbotColor("#707070")
            sbColorOnSurface = ScanbotColor("#000000")
            sbColorSurfaceLow = ScanbotColor("#26000000")
            sbColorSurfaceHigh = ScanbotColor("#7A000000")
            sbColorModalOverlay = ScanbotColor("#A3000000")
        }

        resultLauncher.launch(configuration)
    }
    // @EndTag("Palette")
}

class DocumentDataExtractorLocalizationSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }
    // @Tag("Localization")

    val resultLauncher: ActivityResultLauncher<DocumentDataExtractorScreenConfiguration> =
        registerForActivityResult(DocumentDataExtractorActivity.ResultContract()) { resultEntity: DocumentDataExtractorActivity.Result ->
            if (resultEntity.resultOk) {
                resultEntity.result?.document?.let { document ->
                    wrapGenericDocument(document)
                }
            }
        }

    private fun startScanning() {

        // Create the default configuration object.
        val configuration = DocumentDataExtractorScreenConfiguration()

        // Retrieve the instance of the localization from the configuration object.
        configuration.localization.apply {
            // Configure the strings.
            // e.g
            topUserGuidance = "top.user.guidance"
            cameraPermissionCloseButton = "camera.permission.close"
        }

        resultLauncher.launch(configuration)
    }
// @EndTag("Localization")
}

class DocumentDataExtractorIntroductionSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }
    // @Tag("Introduction")

    val resultLauncher: ActivityResultLauncher<DocumentDataExtractorScreenConfiguration> =
        registerForActivityResult(DocumentDataExtractorActivity.ResultContract()) { resultEntity: DocumentDataExtractorActivity.Result ->
            if (resultEntity.resultOk) {
                resultEntity.result?.document?.let { document ->
                    wrapGenericDocument(document)
                }
            }
        }

    private fun startScanning() {

        // Create the default configuration object.
        val configuration = DocumentDataExtractorScreenConfiguration()

        // Retrieve the instance of the intro screen from the configuration object.
        configuration.introScreen.apply {
            // Show the introduction screen automatically when the screen appears.
            showAutomatically = true

            // Configure the background color of the screen.
            backgroundColor = ScanbotColor("#FFFFFF")

            // Configure the title for the intro screen.
            title.text = "How to scan an ID document"

            // Configure the image for the introduction screen.
            // If you want to have no image...
            image = DocumentDataIntroNoImage()
            // For a custom image...
            image = DocumentDataIntroCustomImage(uri = "PathToImage")
            // Or you can also use our default image.
            image = DocumentDataIntroDefaultImage()

            // Configure the color of the handler on top.
            handlerColor = ScanbotColor("#EFEFEF")

            // Configure the color of the divider.
            dividerColor = ScanbotColor("#EFEFEF")

            // Configure the text.
            explanation.color = ScanbotColor("#000000")
            explanation.text =
                "To scan your ID, position the document within the viewfinder, ensuring it is properly aligned and all key details are clearly visible. The scanner will automatically extract essential information, such as your name, date of birth, and document number. Once the scan is complete, the scanner will close, and the extracted data will be processed accordingly.\n\nPress 'Start Scanning' to begin."

            // Configure the done button.
            // e.g the text or the background color.
            doneButton.text = "Start Scanning"
            doneButton.background.fillColor = ScanbotColor("#C8193C")
        }

        resultLauncher.launch(configuration)
    }
// @EndTag("Introduction")
}

class DocumentDataExtractorUserGuidanceSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }
    // @Tag("User guidance")

    val resultLauncher: ActivityResultLauncher<DocumentDataExtractorScreenConfiguration> =
        registerForActivityResult(DocumentDataExtractorActivity.ResultContract()) { resultEntity: DocumentDataExtractorActivity.Result ->
            if (resultEntity.resultOk) {
                resultEntity.result?.document?.let { document ->
                    wrapGenericDocument(document)
                }
            }
        }

    private fun startScanning() {

        // Create the default configuration object.
        val configuration = DocumentDataExtractorScreenConfiguration()

        // Configure user guidance's

        // Top user guidance
        // Retrieve the instance of the top user guidance from the configuration object.
        configuration.topUserGuidance.apply {
            // Show the user guidance.
            visible = true
            // Configure the title.
            title.text = "Scan your Identity Document"
            title.color = ScanbotColor("#FFFFFF")
            // Configure the background.
            background.fillColor = ScanbotColor("#7A000000")

            resultLauncher.launch(configuration)
        }
        configuration.scanStatusUserGuidance.statesTitles.noDocumentFound = "No Document Found"
        configuration.scanStatusUserGuidance.statesTitles.tooDark = "Try to move to some light"
    }
// @EndTag("User guidance")
}

class DocumentDataExtractorTopBarSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }
    // @Tag("Top bar")

    val resultLauncher: ActivityResultLauncher<DocumentDataExtractorScreenConfiguration> =
        registerForActivityResult(DocumentDataExtractorActivity.ResultContract()) { resultEntity: DocumentDataExtractorActivity.Result ->
            if (resultEntity.resultOk) {
                resultEntity.result?.document?.let { document ->
                    wrapGenericDocument(document)
                }
            }
        }

    private fun startScanning() {

        // Create the default configuration object.
        val configuration = DocumentDataExtractorScreenConfiguration()

        // Retrieve the instance of the top user guidance from the configuration object.
        configuration.topBar.apply {
            // Set the top bar mode.
            mode = TopBarMode.GRADIENT

            // Set the background color which will be used as a gradient.
            backgroundColor = ScanbotColor("#C8193C")

            // Set the status bar mode.
            statusBarMode = StatusBarMode.LIGHT

            // Configure the cancel button.
            cancelButton.text = "Cancel"
            cancelButton.foreground.color = ScanbotColor("#FFFFFF")
        }

        resultLauncher.launch(configuration)
    }
// @EndTag("Top bar")
}

class DocumentDataExtractorFinderSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }
    // @Tag("Finder overlay")

    val resultLauncher: ActivityResultLauncher<DocumentDataExtractorScreenConfiguration> =
        registerForActivityResult(DocumentDataExtractorActivity.ResultContract()) { resultEntity: DocumentDataExtractorActivity.Result ->
            if (resultEntity.resultOk) {
                resultEntity.result?.document?.let { document ->
                    wrapGenericDocument(document)
                }
            }
        }

    private fun startScanning() {

        // Create the default configuration object.
        val configuration = DocumentDataExtractorScreenConfiguration()

        // Configure finder overlay appearance
        configuration.viewFinder.apply {
            style = FinderStyle.finderStrokedStyle().apply {
                strokeColor = ScanbotColor("#FF00FF")
                cornerRadius = 0.0
            }
        }
        resultLauncher.launch(configuration)
    }
// @EndTag("Finder overlay")
}

class DocumentDataExtractorActionBarSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }
    // @Tag("Action bar")

    val resultLauncher: ActivityResultLauncher<DocumentDataExtractorScreenConfiguration> =
        registerForActivityResult(DocumentDataExtractorActivity.ResultContract()) { resultEntity: DocumentDataExtractorActivity.Result ->
            if (resultEntity.resultOk) {
                resultEntity.result?.document?.let { document ->
                    wrapGenericDocument(document)
                }
            }
        }

    private fun startScanning() {

        // Create the default configuration object.
        val configuration = DocumentDataExtractorScreenConfiguration()
        // Retrieve the instance of the action bar from the configuration object.
        configuration.actionBar.apply {

            // Show the flash button.
            flashButton.visible = true

            // Configure the inactive state of the flash button.
            flashButton.backgroundColor = ScanbotColor("#7A000000")
            flashButton.foregroundColor = ScanbotColor("#FFFFFF")

            // Configure the active state of the flash button.
            flashButton.activeBackgroundColor = ScanbotColor("#FFCE5C")
            flashButton.activeForegroundColor = ScanbotColor("#000000")

            // Show the zoom button.
            zoomButton.visible = true

            // Configure the zoom button.
            zoomButton.backgroundColor = ScanbotColor("#7A000000")
            zoomButton.foregroundColor = ScanbotColor("#FFFFFF")

            // Show the flip camera button.
            flipCameraButton.visible = true

            // Configure the flip camera button.
            flipCameraButton.backgroundColor = ScanbotColor("#7A000000")
            flipCameraButton.foregroundColor = ScanbotColor("#FFFFFF")
        }
        resultLauncher.launch(configuration)

    }
// @EndTag("Action bar")
}

class DocumentDataExtractorScanningSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }
    // @Tag("Scanning")

    val resultLauncher: ActivityResultLauncher<DocumentDataExtractorScreenConfiguration> =
        registerForActivityResult(DocumentDataExtractorActivity.ResultContract()) { resultEntity: DocumentDataExtractorActivity.Result ->
            if (resultEntity.resultOk) {
                resultEntity.result?.document?.let { document ->
                    wrapGenericDocument(document)
                }
            }
        }

    private fun startScanning() {

        // Create the default configuration object.
        val configuration = DocumentDataExtractorScreenConfiguration()
        // Initiate scanner with de id card document type to scan
        configuration.scannerConfiguration.configurations = listOf(
            DocumentDataExtractorCommonConfiguration(
                listOf(
                    DeIdCardFront.DOCUMENT_TYPE,
                    DeIdCardBack.DOCUMENT_TYPE
                )
            )
        )
        // Configure camera properties.
        // e.g
        configuration.cameraConfiguration.zoomSteps = listOf(1.0, 2.0, 5.0)
        configuration.cameraConfiguration.flashEnabled = false
        configuration.cameraConfiguration.pinchToZoomEnabled = true

        // Configure the UI elements like icons or buttons.
        // e.g The top bar introduction button.
        configuration.topBarOpenIntroScreenButton.visible = true
        configuration.topBarOpenIntroScreenButton.color = ScanbotColor("#FFFFFF")
        // Cancel button.
        configuration.topBar.cancelButton.visible = true
        configuration.topBar.cancelButton.text = "Cancel"
        configuration.topBar.cancelButton.foreground.color = ScanbotColor("#FFFFFF")
        configuration.topBar.cancelButton.background.fillColor = ScanbotColor("#00000000")

        // Configure the success overlay.
        configuration.successOverlay.iconColor = ScanbotColor("#FFFFFF")
        configuration.successOverlay.message.text = "Scanned Successfully!"
        configuration.successOverlay.message.color = ScanbotColor("#FFFFFF")

        // Configure the sound.
        configuration.sound.successBeepEnabled = true
        configuration.sound.soundType = SoundType.MODERN_BEEP
        resultLauncher.launch(configuration)

    }
// @EndTag("Scanning")
}

class ComposeSnippet : AppCompatActivity() {

    // @Tag("Compose Example")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ComposeView(this).apply {
            setContent {
                // integrate the DocumentDataExtractor scanner compose view
                DocumentDataExtractorView(
                    modifier = Modifier.fillMaxSize(),
                    configuration = getConfiguration(),
                    enableBackNavigation = false,
                    onDocumentExtracted = { document ->
                        // Handle the document.
                    },
                    onDocumentExtractorClosed = { reason ->
                        // Indicates that the cancel button was tapped.
                    }
                )
            }
        })
    }


    fun getConfiguration(): DocumentDataExtractorScreenConfiguration {
        // Create the default configuration object.
        return DocumentDataExtractorScreenConfiguration().apply {

        }
    }
    // @EndTag("Compose Example")
}