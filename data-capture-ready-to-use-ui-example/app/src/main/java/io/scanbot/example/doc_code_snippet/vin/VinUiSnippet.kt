package io.scanbot.example.doc_code_snippet.vin

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
import io.scanbot.sdk.ui_v2.common.*
import io.scanbot.sdk.ui_v2.textpattern.TextPatternScannerActivity
import io.scanbot.sdk.ui_v2.textpattern.configuration.TextPatternScannerScreenConfiguration
import io.scanbot.sdk.ui_v2.vin.VinScannerActivity
import io.scanbot.sdk.ui_v2.vin.VinScannerView
import io.scanbot.sdk.ui_v2.vin.configuration.VinIntroCustomImage
import io.scanbot.sdk.ui_v2.vin.configuration.VinIntroDefaultImage
import io.scanbot.sdk.ui_v2.vin.configuration.VinIntroNoImage
import io.scanbot.sdk.ui_v2.vin.configuration.VinScannerScreenConfiguration
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

class StartVinUiSnippet : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }

    // @Tag("Launching the scanner")

    val resultLauncher: ActivityResultLauncher<VinScannerScreenConfiguration> =
        registerForActivityResult(VinScannerActivity.ResultContract()) { resultEntity ->
            resultEntity.onSuccess { result ->
                Toast.makeText(this@StartVinUiSnippet, result.textResult.rawText, Toast.LENGTH_LONG)
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
        val configuration = VinScannerScreenConfiguration()
        // Start the recognizer activity.
        resultLauncher.launch(configuration)
    }
    // @EndTag("Launching the scanner")
}

class VinPaletteSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }
    // @Tag("Palette")

    val resultLauncher: ActivityResultLauncher<VinScannerScreenConfiguration> =
        registerForActivityResult(VinScannerActivity.ResultContract()) { resultEntity ->
            resultEntity.onSuccess { result ->
                Toast.makeText(this@VinPaletteSnippet, result.textResult.rawText, Toast.LENGTH_LONG)
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
        val configuration = VinScannerScreenConfiguration()

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

class VinLocalizationSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }
    // @Tag("Localization")

    val resultLauncher: ActivityResultLauncher<VinScannerScreenConfiguration> =
        registerForActivityResult(VinScannerActivity.ResultContract()) { resultEntity ->
            resultEntity.onSuccess { result ->
                Toast.makeText(this@VinLocalizationSnippet, result.textResult.rawText, Toast.LENGTH_LONG)
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
        val configuration = VinScannerScreenConfiguration()

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

class VinIntroductionSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }
    // @Tag("Introduction")

    val resultLauncher: ActivityResultLauncher<VinScannerScreenConfiguration> =
        registerForActivityResult(VinScannerActivity.ResultContract()) { resultEntity ->
            resultEntity.onSuccess { result ->
                Toast.makeText(this@VinIntroductionSnippet, result.textResult.rawText, Toast.LENGTH_LONG)
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
        val configuration = VinScannerScreenConfiguration()

        // Retrieve the instance of the intro screen from the configuration object.
        configuration.introScreen.apply {
            // Show the introduction screen automatically when the screen appears.
            showAutomatically = true

            // Configure the background color of the screen.
            backgroundColor = ScanbotColor("#FFFFFF")

            // Configure the title for the intro screen.
            title.text = "How to scan a VIN"

            // Configure the image for the introduction screen.
            // If you want to have no image...
            image = VinIntroNoImage()
            // For a custom image...
            image = VinIntroCustomImage(uri = "PathToImage")
            // Or you can also use our default image.
            image = VinIntroDefaultImage()

            // Configure the color of the handler on top.
            handlerColor = ScanbotColor("#EFEFEF")

            // Configure the color of the divider.
            dividerColor = ScanbotColor("#EFEFEF")

            // Configure the text.
            explanation.color = ScanbotColor("#000000")
            explanation.text = "The VIN (Vehicle Identification Number) is a unique code you'll find on your windshield or inside the driver's door.\n\nTo read the VIN, hold your camera over it. Make sure it's aligned in the frame. Your VIN will be automatically extracted.\n\nTap 'Start Scanning' to begin."

            // Configure the done button.
            // e.g the text or the background color.
            doneButton.text = "Start Scanning"
            doneButton.background.fillColor = ScanbotColor("#C8193C")
        }

        resultLauncher.launch(configuration)
    }
// @EndTag("Introduction")
}

class VinUserGuidanceSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }
    // @Tag("User guidance")

    val resultLauncher: ActivityResultLauncher<VinScannerScreenConfiguration> =
        registerForActivityResult(VinScannerActivity.ResultContract()) { resultEntity ->
            resultEntity.onSuccess { result ->
                Toast.makeText(this@VinUserGuidanceSnippet, result.textResult.rawText, Toast.LENGTH_LONG)
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
        val configuration = VinScannerScreenConfiguration()

        // Configure user guidance's

        // Top user guidance
        // Retrieve the instance of the top user guidance from the configuration object.
        configuration.topUserGuidance.apply {
            // Show the user guidance.
            visible = true
            // Configure the title.
            title.text = "Scan your VIN"
            title.color = ScanbotColor("#FFFFFF")
            // Configure the background.
            background.fillColor = ScanbotColor("#7A000000")

            // Finder overlay user guidance
            // Retrieve the instance of the finder overlay user guidance from the configuration object.
            configuration.finderViewUserGuidance.apply {
                // Show the user guidance.
                visible = true
                // Configure the title.
                title.text = "Point the view finder towards the VIN"
                title.color = ScanbotColor("#FFFFFF")
                // Configure the background.
                background.fillColor = ScanbotColor("#7A000000")
            }
            resultLauncher.launch(configuration)
        }
    }
// @EndTag("User guidance")
}

class VinTopBarSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }
    // @Tag("Top bar")

    val resultLauncher: ActivityResultLauncher<VinScannerScreenConfiguration> =
        registerForActivityResult(VinScannerActivity.ResultContract()) { resultEntity ->
            resultEntity.onSuccess { result ->
                Toast.makeText(this@VinTopBarSnippet, result.textResult.rawText, Toast.LENGTH_LONG)
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
        val configuration = VinScannerScreenConfiguration()

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

class VinFinderSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }
    // @Tag("Finder overlay")

    val resultLauncher: ActivityResultLauncher<VinScannerScreenConfiguration> =
        registerForActivityResult(VinScannerActivity.ResultContract()) { resultEntity ->
            resultEntity.onSuccess { result ->
                Toast.makeText(this@VinFinderSnippet, result.textResult.rawText, Toast.LENGTH_LONG)
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
        val configuration = VinScannerScreenConfiguration()

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

class VinActionBarSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }
    // @Tag("Action bar")

    val resultLauncher: ActivityResultLauncher<VinScannerScreenConfiguration> =
        registerForActivityResult(VinScannerActivity.ResultContract()) { resultEntity ->
            resultEntity.onSuccess { result ->
                Toast.makeText(this@VinActionBarSnippet, result.textResult.rawText, Toast.LENGTH_LONG)
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
        val configuration = VinScannerScreenConfiguration()
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

class VinScanningSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }
    // @Tag("Scanning")

    val resultLauncher: ActivityResultLauncher<VinScannerScreenConfiguration> =
        registerForActivityResult(VinScannerActivity.ResultContract()) { resultEntity ->
            resultEntity.onSuccess { result ->
                Toast.makeText(this@VinScanningSnippet, result.textResult.rawText, Toast.LENGTH_LONG)
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
        val configuration = VinScannerScreenConfiguration()

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
                VinScannerView(
                    configuration = getConfiguration(),
                    onVinScanned = { document ->
                        // Handle the document.
                    },
                    onVinScannerClosed = { reason ->
                        // Indicates that the cancel button was tapped.
                    }
                )
            }
        })
    }


    fun getConfiguration(): VinScannerScreenConfiguration {
        // Create the default configuration object.
        return VinScannerScreenConfiguration().apply {

        }
    }
    // @EndTag("Compose Example")
}

