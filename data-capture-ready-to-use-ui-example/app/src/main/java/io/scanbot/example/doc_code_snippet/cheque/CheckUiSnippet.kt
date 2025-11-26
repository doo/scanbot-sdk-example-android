package io.scanbot.example.doc_code_snippet.cheque

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
import io.scanbot.sdk.camera.FrameHandler
import io.scanbot.sdk.check.CheckScanner
import io.scanbot.sdk.check.CheckScannerFrameHandler
import io.scanbot.sdk.check.CheckScanningResult
import io.scanbot.sdk.ui.camera.ScanbotCameraXView
import io.scanbot.sdk.ui_v2.check.CheckScannerActivity
import io.scanbot.sdk.ui_v2.check.CheckScannerView
import io.scanbot.sdk.ui_v2.check.configuration.CheckIntroCustomImage
import io.scanbot.sdk.ui_v2.check.configuration.CheckIntroDefaultImage
import io.scanbot.sdk.ui_v2.check.configuration.CheckNoImage
import io.scanbot.sdk.ui_v2.check.configuration.CheckScannerScreenConfiguration
import io.scanbot.sdk.ui_v2.common.FinderStyle
import io.scanbot.sdk.ui_v2.common.ScanbotColor
import io.scanbot.sdk.ui_v2.common.SoundType
import io.scanbot.sdk.ui_v2.common.StatusBarMode
import io.scanbot.sdk.ui_v2.common.TopBarMode

//Rtu ui snippets
fun initSdkSnippet(application: Application, licenseKey: String) {
    // @Tag("InitializeScanbotSDK")
    ScanbotSDKInitializer()
        .license(application, licenseKey)
        .prepareOCRLanguagesBlobs(true)
        .initialize(application)
    // @EndTag("InitializeScanbotSDK")
}

class StartCheckUiSnippet : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }

    // @Tag("Launching the scanner")

    val resultLauncher: ActivityResultLauncher<CheckScannerScreenConfiguration> =
        registerForActivityResult(CheckScannerActivity.ResultContract()) { resultEntity ->
            resultEntity.onSuccess { checkResult ->
                checkResult.check?.let {
                    // Here you can handle `check document` and present recognized Check information (routing number, account number, etc.)
                    wrapCheck(it)
                }
            }.onFailure {
                // Optional activity closing cause handling to understand the reason scanner result is not provided
                when (it) {
                    is Result.InvalidLicenseError -> {
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
        val configuration = CheckScannerScreenConfiguration()

        // Start the recognizer activity.
        resultLauncher.launch(configuration)
    }
    // @EndTag("Launching the scanner")
}

class CheckPaletteSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }
    // @Tag("Palette")

    val resultLauncher: ActivityResultLauncher<CheckScannerScreenConfiguration> =
        registerForActivityResult(CheckScannerActivity.ResultContract()) { resultEntity ->
            resultEntity.onSuccess { checkResult ->
                checkResult.check?.let {
                    // Here you can handle `check document` and present recognized Check information (routing number, account number, etc.)
                    wrapCheck(it)
                }
            }.onFailure {
                // Optional activity closing cause handling to understand the reason scanner result is not provided
                when (it) {
                    is Result.InvalidLicenseError -> {
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
        val configuration = CheckScannerScreenConfiguration()

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

class CheckLocalizationSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }
    // @Tag("Localization")

    val resultLauncher: ActivityResultLauncher<CheckScannerScreenConfiguration> =
        registerForActivityResult(CheckScannerActivity.ResultContract()) { resultEntity ->
            resultEntity.onSuccess { checkResult ->
                checkResult.check?.let {
                    // Here you can handle `check document` and present recognized Check information (routing number, account number, etc.)
                    wrapCheck(it)
                }
            }.onFailure {
                // Optional activity closing cause handling to understand the reason scanner result is not provided
                when (it) {
                    is Result.InvalidLicenseError -> {
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
        val configuration = CheckScannerScreenConfiguration()

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

class CheckIntroductionSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }
    // @Tag("Introduction")

    val resultLauncher: ActivityResultLauncher<CheckScannerScreenConfiguration> =
        registerForActivityResult(CheckScannerActivity.ResultContract()) { resultEntity ->
            resultEntity.onSuccess { checkResult ->
                checkResult.check?.let {
                    // Here you can handle `check document` and present recognized Check information (routing number, account number, etc.)
                    wrapCheck(it)
                }
            }.onFailure {
                // Optional activity closing cause handling to understand the reason scanner result is not provided
                when (it) {
                    is Result.InvalidLicenseError -> {
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
        val configuration = CheckScannerScreenConfiguration()

        // Retrieve the instance of the intro screen from the configuration object.
        configuration.introScreen.apply {
            // Show the introduction screen automatically when the screen appears.
            showAutomatically = true

            // Configure the background color of the screen.
            backgroundColor = ScanbotColor("#FFFFFF")

            // Configure the title for the intro screen.
            title.text = "How to scan a check"

            // Configure the image for the introduction screen.
            // If you want to have no image...
            image = CheckNoImage()
            // For a custom image...
            image = CheckIntroCustomImage(uri = "PathToImage")
            // Or you can also use our default image.
            image = CheckIntroDefaultImage()

            // Configure the color of the handler on top.
            handlerColor = ScanbotColor("#EFEFEF")

            // Configure the color of the divider.
            dividerColor = ScanbotColor("#EFEFEF")

            // Configure the text.
            explanation.color = ScanbotColor("#000000")
            explanation.text =
                "To quickly and securely input your check details, please hold your device over the check, so that the camera aligns with the document.\n\nThe scanner will guide you to the optimal scanning position. Once the scan is complete, your card details will automatically be extracted and processed.\n\nPress 'Start Scanning' to begin."

            // Configure the done button.
            // e.g the text or the background color.
            doneButton.text = "Start Scanning"
            doneButton.background.fillColor = ScanbotColor("#C8193C")
        }

        resultLauncher.launch(configuration)
    }
// @EndTag("Introduction")
}

class CheckUserGuidanceSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }
    // @Tag("User guidance")

    val resultLauncher: ActivityResultLauncher<CheckScannerScreenConfiguration> =
        registerForActivityResult(CheckScannerActivity.ResultContract()) { resultEntity ->
            resultEntity.onSuccess { checkResult ->
                checkResult.check?.let {
                    // Here you can handle `check document` and present recognized Check information (routing number, account number, etc.)
                    wrapCheck(it)
                }
            }.onFailure {
                // Optional activity closing cause handling to understand the reason scanner result is not provided
                when (it) {
                    is Result.InvalidLicenseError -> {
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
        val configuration = CheckScannerScreenConfiguration()

        // Configure user guidance's

        // Top user guidance
        // Retrieve the instance of the top user guidance from the configuration object.
        configuration.topUserGuidance.apply {
            // Show the user guidance.
            visible = true
            // Configure the title.
            title.text = "Scan your check"
            title.color = ScanbotColor("#FFFFFF")
            // Configure the background.
            background.fillColor = ScanbotColor("#7A000000")

            // Finder overlay user guidance
            // Retrieve the instance of the finder overlay user guidance from the configuration object.
            configuration.scanStatusUserGuidance.apply {
                // Show the user guidance.
                visible = true
                // Configure the title.
                title.text = "Scan the Check"
                title.color = ScanbotColor("#FFFFFF")
                // Configure the background.
                background.fillColor = ScanbotColor("#7A000000")
            }
            resultLauncher.launch(configuration)
        }
    }
// @EndTag("User guidance")
}

class CheckTopBarSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }
    // @Tag("Top bar")

    val resultLauncher: ActivityResultLauncher<CheckScannerScreenConfiguration> =
        registerForActivityResult(CheckScannerActivity.ResultContract()) { resultEntity ->
            resultEntity.onSuccess { checkResult ->
                checkResult.check?.let {
                    // Here you can handle `check document` and present recognized Check information (routing number, account number, etc.)
                    wrapCheck(it)
                }
            }.onFailure {
                // Optional activity closing cause handling to understand the reason scanner result is not provided
                when (it) {
                    is Result.InvalidLicenseError -> {
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
        val configuration = CheckScannerScreenConfiguration()

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

class CheckFinderSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }
    // @Tag("Finder overlay")

    val resultLauncher: ActivityResultLauncher<CheckScannerScreenConfiguration> =
        registerForActivityResult(CheckScannerActivity.ResultContract()) { resultEntity ->
            resultEntity.onSuccess { checkResult ->
                checkResult.check?.let {
                    // Here you can handle `check document` and present recognized Check information (routing number, account number, etc.)
                    wrapCheck(it)
                }
            }.onFailure {
                // Optional activity closing cause handling to understand the reason scanner result is not provided
                when (it) {
                    is Result.InvalidLicenseError -> {
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
        val configuration = CheckScannerScreenConfiguration()

        configuration.exampleOverlayVisible = true
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

class CheckActionBarSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }
    // @Tag("Action bar")

    val resultLauncher: ActivityResultLauncher<CheckScannerScreenConfiguration> =
        registerForActivityResult(CheckScannerActivity.ResultContract()) { resultEntity ->
            resultEntity.onSuccess { checkResult ->
                checkResult.check?.let {
                    // Here you can handle `check document` and present recognized Check information (routing number, account number, etc.)
                    wrapCheck(it)
                }
            }.onFailure {
                // Optional activity closing cause handling to understand the reason scanner result is not provided
                when (it) {
                    is Result.InvalidLicenseError -> {
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
        val configuration = CheckScannerScreenConfiguration()
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

class CheckScanningSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }
    // @Tag("Scanning")

    val resultLauncher: ActivityResultLauncher<CheckScannerScreenConfiguration> =
        registerForActivityResult(CheckScannerActivity.ResultContract()) { resultEntity ->
            resultEntity.onSuccess { checkResult ->
                checkResult.check?.let {
                    // Here you can handle `check document` and present recognized Check information (routing number, account number, etc.)
                    wrapCheck(it)
                }
            }.onFailure {
                // Optional activity closing cause handling to understand the reason scanner result is not provided
                when (it) {
                    is Result.InvalidLicenseError -> {
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
        val configuration = CheckScannerScreenConfiguration()

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
                CheckScannerView(
                    configuration = getConfiguration(),
                    onCheckScanned = { document ->
                        // Handle the document.
                    },
                    onCheckScannerClosed = { reason ->
                        // Indicates that the cancel button was tapped.
                    }
                )
            }
        })
    }


    fun getConfiguration(): CheckScannerScreenConfiguration {
        // Create the default configuration object.
        return CheckScannerScreenConfiguration().apply {

        }
    }
    // @EndTag("Compose Example")
}


//Classic snippets

fun getInstances(context: Context, cameraView: ScanbotCameraXView) {
    // @Tag("Get Instances")
    val scanbotSDK = ScanbotSDK(context)
    val checkScanner: CheckScanner = scanbotSDK.createCheckScanner().getOrThrow()
    val checkScannerFrameHandler: CheckScannerFrameHandler =
        CheckScannerFrameHandler.attach(cameraView, checkScanner)
    // @EndTag("Get Instances")
}

fun handleResult(checkScannerFrameHandler: CheckScannerFrameHandler) {
    // @Tag("Handle Result")
    checkScannerFrameHandler.addResultHandler { result, frame ->
        result.onSuccess { checkResult ->
            if (checkResult?.check != null) {
                // do something with result here
                val checkDocument = checkResult.check
                if (checkDocument != null) {
                    wrapCheck(checkDocument)
                }
            }
        }.onFailure {
            // handle error here
        }
        false
    }

    false
}
// @EndTag("Handle Result")