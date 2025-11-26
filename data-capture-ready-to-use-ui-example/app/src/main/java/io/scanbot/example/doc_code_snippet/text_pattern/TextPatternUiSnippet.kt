package io.scanbot.example.doc_code_snippet.text_pattern

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
import androidx.compose.ui.platform.ComposeView
import io.scanbot.common.Result
import io.scanbot.common.onFailure
import io.scanbot.common.onSuccess
import io.scanbot.example.*
import io.scanbot.sdk.*
import io.scanbot.sdk.textpattern.*
import io.scanbot.sdk.ui_v2.common.*
import io.scanbot.sdk.ui_v2.textpattern.*
import io.scanbot.sdk.ui_v2.textpattern.configuration.*
import java.util.regex.Pattern


//Rtu ui snippets
fun initSdkSnippet(application: Application, licenseKey: String) {
    // @Tag("InitializeScanbotSDK")
    ScanbotSDKInitializer()
        .license(application, licenseKey)
        .prepareOCRLanguagesBlobs(true)
        .initialize(application)
    // @EndTag("InitializeScanbotSDK")
}

class StartTextPatternUiSnippet : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }

    // @Tag("Launching the scanner")

    val resultLauncher: ActivityResultLauncher<TextPatternScannerScreenConfiguration> =
        registerForActivityResult(TextPatternScannerActivity.ResultContract()) { resultEntity ->
            resultEntity.onSuccess { result ->
                Toast.makeText(this@StartTextPatternUiSnippet, result.rawText, Toast.LENGTH_LONG)
                    .show()
            }.onFailure {
                // Optional activity closing cause handling to understand the reason scanner result is not provided
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

    private fun startScanning() {
        // Create the default configuration object.
        val configuration = TextPatternScannerScreenConfiguration()
        // Configure what string should be passed as successfully scanned text.
        configuration.scannerConfiguration.validator = CustomContentValidator().apply {
            val pattern = Pattern.compile("^[0-9]{4}$") // e.g. 4 digits
            this.callback = object : ContentValidationCallback {
                override fun clean(rawText: String): String {
                    return rawText.replace(" ", "")
                }

                override fun validate(text: String): Boolean {
                    val matcher = pattern.matcher(text)
                    return matcher.find()
                }
            }
        }
        // Start the recognizer activity.
        resultLauncher.launch(configuration)
    }
    // @EndTag("Launching the scanner")
}

class TextPatternPaletteSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }
    // @Tag("Palette")

    val resultLauncher: ActivityResultLauncher<TextPatternScannerScreenConfiguration> =
        registerForActivityResult(TextPatternScannerActivity.ResultContract()) { resultEntity ->
            resultEntity.onSuccess { result ->
                Toast.makeText(this@TextPatternPaletteSnippet, result.rawText, Toast.LENGTH_LONG)
                    .show()
            }.onFailure {
                // Optional activity closing cause handling to understand the reason scanner result is not provided
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

    private fun startScanning() {

        // Create the default configuration object.
        val configuration = TextPatternScannerScreenConfiguration()

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

class TextPatternLocalizationSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }
    // @Tag("Localization")

    val resultLauncher: ActivityResultLauncher<TextPatternScannerScreenConfiguration> =
        registerForActivityResult(TextPatternScannerActivity.ResultContract()) { resultEntity ->
            resultEntity.onSuccess { result ->
                Toast.makeText(this@TextPatternLocalizationSnippet, result.rawText, Toast.LENGTH_LONG)
                    .show()
            }.onFailure {
                // Optional activity closing cause handling to understand the reason scanner result is not provided
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

    private fun startScanning() {

        // Create the default configuration object.
        val configuration = TextPatternScannerScreenConfiguration()

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

class TextPatternIntroductionSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }
    // @Tag("Introduction")

    val resultLauncher: ActivityResultLauncher<TextPatternScannerScreenConfiguration> =
        registerForActivityResult(TextPatternScannerActivity.ResultContract()) { resultEntity ->
            resultEntity.onSuccess { result ->
                Toast.makeText(this@TextPatternIntroductionSnippet, result.rawText, Toast.LENGTH_LONG)
                    .show()
            }.onFailure {
                // Optional activity closing cause handling to understand the reason scanner result is not provided
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

    private fun startScanning() {

        // Create the default configuration object.
        val configuration = TextPatternScannerScreenConfiguration()

        // Retrieve the instance of the intro screen from the configuration object.
        configuration.introScreen.apply {
            // Show the introduction screen automatically when the screen appears.
            showAutomatically = true

            // Configure the background color of the screen.
            backgroundColor = ScanbotColor("#FFFFFF")

            // Configure the title for the intro screen.
            title.text = "How to scan an text"

            // Configure the image for the introduction screen.
            // If you want to have no image...
            image = TextPatternIntroNoImage()
            // For a custom image...
            image = TextPatternIntroCustomImage(uri = "PathToImage")
            // Or you can also use our default image.
            image = TextPatternIntroMeterDevice()

            // Configure the color of the handler on top.
            handlerColor = ScanbotColor("#EFEFEF")

            // Configure the color of the divider.
            dividerColor = ScanbotColor("#EFEFEF")

            // Configure the text.
            explanation.color = ScanbotColor("#000000")
            explanation.text =
                "To scan a single line of text, please hold your device so that the camera viewfinder clearly captures the text you want to scan. Please ensure the text is properly aligned. Once the scan is complete, the text will be automatically extracted.\n\nPress 'Start Scanning' to begin."

            // Configure the done button.
            // e.g the text or the background color.
            doneButton.text = "Start Scanning"
            doneButton.background.fillColor = ScanbotColor("#C8193C")
        }

        resultLauncher.launch(configuration)
    }
// @EndTag("Introduction")
}

class TextPatternUserGuidanceSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }
    // @Tag("User guidance")

    val resultLauncher: ActivityResultLauncher<TextPatternScannerScreenConfiguration> =
        registerForActivityResult(TextPatternScannerActivity.ResultContract()) { resultEntity ->
            resultEntity.onSuccess { result ->
                Toast.makeText(this@TextPatternUserGuidanceSnippet, result.rawText, Toast.LENGTH_LONG)
                    .show()
            }.onFailure {
                // Optional activity closing cause handling to understand the reason scanner result is not provided
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

    private fun startScanning() {

        // Create the default configuration object.
        val configuration = TextPatternScannerScreenConfiguration()

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

            // Finder overlay user guidance
            // Retrieve the instance of the finder overlay user guidance from the configuration object.
            configuration.finderViewUserGuidance.apply {
                // Show the user guidance.
                visible = true
                // Configure the title.
                title.text = "DD.MM.YY"
                title.color = ScanbotColor("#FFFFFF")
                // Configure the background.
                background.fillColor = ScanbotColor("#7A000000")
            }
            resultLauncher.launch(configuration)
        }
    }
// @EndTag("User guidance")
}

class TextPatternTopBarSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }
    // @Tag("Top bar")

    val resultLauncher: ActivityResultLauncher<TextPatternScannerScreenConfiguration> =
        registerForActivityResult(TextPatternScannerActivity.ResultContract()) { resultEntity ->
            resultEntity.onSuccess { result ->
                Toast.makeText(this@TextPatternTopBarSnippet, result.rawText, Toast.LENGTH_LONG)
                    .show()
            }.onFailure {
                // Optional activity closing cause handling to understand the reason scanner result is not provided
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

    private fun startScanning() {

        // Create the default configuration object.
        val configuration = TextPatternScannerScreenConfiguration()

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

class TextPatternFinderSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }
    // @Tag("Finder overlay")

    val resultLauncher: ActivityResultLauncher<TextPatternScannerScreenConfiguration> =
        registerForActivityResult(TextPatternScannerActivity.ResultContract()) { resultEntity ->
            resultEntity.onSuccess { result ->
                Toast.makeText(this@TextPatternFinderSnippet, result.rawText, Toast.LENGTH_LONG)
                    .show()
            }.onFailure {
                // Optional activity closing cause handling to understand the reason scanner result is not provided
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

    private fun startScanning() {

        // Create the default configuration object.
        val configuration = TextPatternScannerScreenConfiguration()

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

class TextPatternActionBarSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }
    // @Tag("Action bar")

    val resultLauncher: ActivityResultLauncher<TextPatternScannerScreenConfiguration> =
        registerForActivityResult(TextPatternScannerActivity.ResultContract()) { resultEntity ->
            resultEntity.onSuccess { result ->
                Toast.makeText(this@TextPatternActionBarSnippet, result.rawText, Toast.LENGTH_LONG)
                    .show()
            }.onFailure {
                // Optional activity closing cause handling to understand the reason scanner result is not provided
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

    private fun startScanning() {

        // Create the default configuration object.
        val configuration = TextPatternScannerScreenConfiguration()
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

class TextPatternScanningSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }
    // @Tag("Scanning")

    val resultLauncher: ActivityResultLauncher<TextPatternScannerScreenConfiguration> =
        registerForActivityResult(TextPatternScannerActivity.ResultContract()) { resultEntity ->
            resultEntity.onSuccess { result ->
                Toast.makeText(this@TextPatternScanningSnippet, result.rawText, Toast.LENGTH_LONG)
                    .show()
            }.onFailure {
                // Optional activity closing cause handling to understand the reason scanner result is not provided
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

    private fun startScanning() {

        // Create the default configuration object.
        val configuration = TextPatternScannerScreenConfiguration()

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

        // Configure what string should be passed as successfully scanned text.
        configuration.scannerConfiguration.validator = CustomContentValidator().apply {
            val pattern = Pattern.compile("^[0-9]{4}$") // e.g. 4 digits
            this.callback = object : ContentValidationCallback {
                override fun clean(rawText: String): String {
                    return rawText.replace(" ", "")
                }

                override fun validate(text: String): Boolean {
                    val matcher = pattern.matcher(text)
                    return matcher.find()
                }
            }
        }
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
                TextPatternScannerView(
                    configuration = getConfiguration(),
                    onTextPatternScanned = { document ->
                        // Handle the document.
                    },
                    onTextPatternScannerClosed = { reason ->
                        // Indicates that the cancel button was tapped.
                    }
                )
            }
        })
    }


    fun getConfiguration(): TextPatternScannerScreenConfiguration {
        // Create the default configuration object.
        return TextPatternScannerScreenConfiguration().apply {

        }
    }
    // @EndTag("Compose Example")
}

