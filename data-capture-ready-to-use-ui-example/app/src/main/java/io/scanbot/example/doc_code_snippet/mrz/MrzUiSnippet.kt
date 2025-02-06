package io.scanbot.example.doc_code_snippet.mrz

/*
    NOTE: this snippet of code is to be used only as a part of the website documentation.
    This code is not intended for any use outside of the support of documentation by Scanbot SDK GmbH employees.
*/

// NOTE for maintainers: whenever changing this code,
// ensure that links using it are still pointing to valid lines!
// Pay attention to imports adding/removal/sorting!
// Page URLs using this code:
// TODO: add URLs here

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import io.scanbot.example.*
import io.scanbot.sdk.*
import io.scanbot.sdk.camera.*

import io.scanbot.sdk.documentdata.entity.*
import io.scanbot.sdk.mrz.*
import io.scanbot.sdk.ui.camera.*
import io.scanbot.sdk.ui_v2.common.*
import io.scanbot.sdk.ui_v2.mrz.*
import io.scanbot.sdk.ui_v2.mrz.configuration.*

//Rtu ui snippets
fun initializeScanbotSDK(application: Application) {
    // @Tag("InitializeScanbotSDK")
    ScanbotSDKInitializer().initialize(application)
    // @EndTag("InitializeScanbotSDK")
}

class StartMrzUiSnippet : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }

    // @Tag("Launching the scanner")

    val resultLauncher: ActivityResultLauncher<MrzScannerScreenConfiguration> =
        registerForActivityResult(MrzScannerActivity.ResultContract()) { resultEntity: MrzScannerActivity.Result ->
            if (resultEntity.resultOk) {
                resultEntity.result?.mrzDocument?.let {
                    val mrz = MRZ(it)
                    val givenName: String = mrz.givenNames.value.text
                    val birthDate: String = mrz.birthDate.value.text
                    val expiryDate: String? = mrz.expiryDate?.value?.text
                    Toast.makeText(
                        this,
                        "Given Name: $givenName, Birth Date: $birthDate, Expiry Date: $expiryDate",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    private fun startScanning() {
        // Create the default configuration object.
        val configuration = MrzScannerScreenConfiguration()

        // Start the recognizer activity.
        resultLauncher.launch(configuration)
    }
    // @EndTag("Launching the scanner")
}

class MrzPaletteSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }
    // @Tag("Palette")

    val resultLauncher: ActivityResultLauncher<MrzScannerScreenConfiguration> =
        registerForActivityResult(MrzScannerActivity.ResultContract()) { resultEntity: MrzScannerActivity.Result ->
            if (resultEntity.resultOk) {
                resultEntity.result?.mrzDocument?.let {
                    val mrz = MRZ(it)
                    val givenName: String = mrz.givenNames.value.text
                    val birthDate: String = mrz.birthDate.value.text
                    val expiryDate: String? = mrz.expiryDate?.value?.text
                    Toast.makeText(
                        this,
                        "Given Name: $givenName, Birth Date: $birthDate, Expiry Date: $expiryDate",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    private fun startScanning() {

        // Create the default configuration object.
        val configuration = MrzScannerScreenConfiguration()

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

class MrzLocalizationSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }
    // @Tag("Localization")

    val resultLauncher: ActivityResultLauncher<MrzScannerScreenConfiguration> =
        registerForActivityResult(MrzScannerActivity.ResultContract()) { resultEntity: MrzScannerActivity.Result ->
            if (resultEntity.resultOk) {
                resultEntity.result?.mrzDocument?.let {
                    val mrz = MRZ(it)
                    val givenName: String = mrz.givenNames.value.text
                    val birthDate: String = mrz.birthDate.value.text
                    val expiryDate: String? = mrz.expiryDate?.value?.text
                    Toast.makeText(
                        this,
                        "Given Name: $givenName, Birth Date: $birthDate, Expiry Date: $expiryDate",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    private fun startScanning() {

        // Create the default configuration object.
        val configuration = MrzScannerScreenConfiguration()

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

class MrzIntroductionSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }
    // @Tag("Introduction")

    val resultLauncher: ActivityResultLauncher<MrzScannerScreenConfiguration> =
        registerForActivityResult(MrzScannerActivity.ResultContract()) { resultEntity: MrzScannerActivity.Result ->
            if (resultEntity.resultOk) {
                resultEntity.result?.mrzDocument?.let {
                    val mrz = MRZ(it)
                    val givenName: String = mrz.givenNames.value.text
                    val birthDate: String = mrz.birthDate.value.text
                    val expiryDate: String? = mrz.expiryDate?.value?.text
                    Toast.makeText(
                        this,
                        "Given Name: $givenName, Birth Date: $birthDate, Expiry Date: $expiryDate",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    private fun startScanning() {

        // Create the default configuration object.
        val configuration = MrzScannerScreenConfiguration()

        // Retrieve the instance of the intro screen from the configuration object.
        configuration.introScreen.apply {
            // Show the introduction screen automatically when the screen appears.
            showAutomatically = true

            // Configure the background color of the screen.
            backgroundColor = ScanbotColor("#FFFFFF")

            // Configure the title for the intro screen.
            title.text = "How to scan an MRZ"

            // Configure the image for the introduction screen.
            // If you want to have no image...
            image = MrzIntroNoImage()
            // For a custom image...
            image = MrzIntroCustomImage(uri = "PathToImage")
            // Or you can also use our default image.
            image = MrzIntroDefaultImage()

            // Configure the color of the handler on top.
            handlerColor = ScanbotColor("#EFEFEF")

            // Configure the color of the divider.
            dividerColor = ScanbotColor("#EFEFEF")

            // Configure the text.
            text.color = ScanbotColor("#000000")
            text.text =
                "The Machine Readable Zone (MRZ) is a special code on your ID document (such as a passport or ID card) that contains your personal information in a machine-readable format.\n\nTo scan it, simply hold your camera over the document, so that it aligns with the MRZ section. Once scanned, the data will be automatically processed, and you will be directed to the results screen.\n\nPress 'Start Scanning' to begin."

            // Configure the done button.
            // e.g the text or the background color.
            doneButton.text = "Start Scanning"
            doneButton.background.fillColor = ScanbotColor("#C8193C")
        }

        resultLauncher.launch(configuration)
    }
// @EndTag("Introduction")
}

class MrzUserGuidanceSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }
    // @Tag("User guidance")

    val resultLauncher: ActivityResultLauncher<MrzScannerScreenConfiguration> =
        registerForActivityResult(MrzScannerActivity.ResultContract()) { resultEntity: MrzScannerActivity.Result ->
            if (resultEntity.resultOk) {
                resultEntity.result?.mrzDocument?.let {
                    val mrz = MRZ(it)
                    val givenName: String = mrz.givenNames.value.text
                    val birthDate: String = mrz.birthDate.value.text
                    val expiryDate: String? = mrz.expiryDate?.value?.text
                    Toast.makeText(
                        this,
                        "Given Name: $givenName, Birth Date: $birthDate, Expiry Date: $expiryDate",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    private fun startScanning() {

        // Create the default configuration object.
        val configuration = MrzScannerScreenConfiguration()

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
                title.text = "Scan the MRZ"
                title.color = ScanbotColor("#FFFFFF")
                // Configure the background.
                background.fillColor = ScanbotColor("#7A000000")
            }
            resultLauncher.launch(configuration)
        }
    }
// @EndTag("User guidance")
}

class MrzTopBarSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }
    // @Tag("Top bar")

    val resultLauncher: ActivityResultLauncher<MrzScannerScreenConfiguration> =
        registerForActivityResult(MrzScannerActivity.ResultContract()) { resultEntity: MrzScannerActivity.Result ->
            if (resultEntity.resultOk) {
                resultEntity.result?.mrzDocument?.let {
                    val mrz = MRZ(it)
                    val givenName: String = mrz.givenNames.value.text
                    val birthDate: String = mrz.birthDate.value.text
                    val expiryDate: String? = mrz.expiryDate?.value?.text
                    Toast.makeText(
                        this,
                        "Given Name: $givenName, Birth Date: $birthDate, Expiry Date: $expiryDate",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    private fun startScanning() {

        // Create the default configuration object.
        val configuration = MrzScannerScreenConfiguration()

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

class MrzFinderSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }
    // @Tag("Finder overlay")

    val resultLauncher: ActivityResultLauncher<MrzScannerScreenConfiguration> =
        registerForActivityResult(MrzScannerActivity.ResultContract()) { resultEntity: MrzScannerActivity.Result ->
            if (resultEntity.resultOk) {
                resultEntity.result?.mrzDocument?.let {
                    val mrz = MRZ(it)
                    val givenName: String = mrz.givenNames.value.text
                    val birthDate: String = mrz.birthDate.value.text
                    val expiryDate: String? = mrz.expiryDate?.value?.text
                    Toast.makeText(
                        this,
                        "Given Name: $givenName, Birth Date: $birthDate, Expiry Date: $expiryDate",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    private fun startScanning() {

        // Create the default configuration object.
        val configuration = MrzScannerScreenConfiguration()

        // Configure the finder example overlay. You can choose between the two-line and three-line preset.
        // Each example preset has a default text for each line, but you can change it accordingly to your liking.
        // Each preset has a fixed aspect ratio adjusted to it's number of lines. To override, please use 'aspectRatio'
        // parameter in 'viewFinder' field in the main configuration object.
        // For this example we will use the three-line preset.
        configuration.mrzExampleOverlay =
            MrzFinderLayoutPreset.threeLineMrzFinderLayoutPreset().apply {
                mrzTextLine1 = "I<USA2342353464<<<<<<<<<<<<<<<"
                mrzTextLine2 = "9602300M2904076USA<<<<<<<<<<<2"
                mrzTextLine3 = "SMITH<<JACK<<<<<<<<<<<<<<<<<<<"
            }
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

class MrzActionBarSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }
    // @Tag("Action bar")

    val resultLauncher: ActivityResultLauncher<MrzScannerScreenConfiguration> =
        registerForActivityResult(MrzScannerActivity.ResultContract()) { resultEntity: MrzScannerActivity.Result ->
            if (resultEntity.resultOk) {
                resultEntity.result?.mrzDocument?.let {
                    val mrz = MRZ(it)
                    val givenName: String = mrz.givenNames.value.text
                    val birthDate: String = mrz.birthDate.value.text
                    val expiryDate: String? = mrz.expiryDate?.value?.text
                    Toast.makeText(
                        this,
                        "Given Name: $givenName, Birth Date: $birthDate, Expiry Date: $expiryDate",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    private fun startScanning() {

        // Create the default configuration object.
        val configuration = MrzScannerScreenConfiguration()
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

class MrzScanningSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }
    // @Tag("Scanning")

    val resultLauncher: ActivityResultLauncher<MrzScannerScreenConfiguration> =
        registerForActivityResult(MrzScannerActivity.ResultContract()) { resultEntity: MrzScannerActivity.Result ->
            if (resultEntity.resultOk) {
                resultEntity.result?.mrzDocument?.let {
                    val mrz = MRZ(it)
                    val givenName: String = mrz.givenNames.value.text
                    val birthDate: String = mrz.birthDate.value.text
                    val expiryDate: String? = mrz.expiryDate?.value?.text
                    Toast.makeText(
                        this,
                        "Given Name: $givenName, Birth Date: $birthDate, Expiry Date: $expiryDate",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    private fun startScanning() {

        // Create the default configuration object.
        val configuration = MrzScannerScreenConfiguration()

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