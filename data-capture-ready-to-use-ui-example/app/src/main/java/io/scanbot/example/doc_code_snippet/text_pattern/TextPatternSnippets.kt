package io.scanbot.example.doc_code_snippet.text_pattern

import android.app.Application
import android.content.Context
import android.content.Intent
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import io.scanbot.example.R
import io.scanbot.sdk.*
import io.scanbot.sdk.camera.*
import io.scanbot.sdk.textpattern.*
import io.scanbot.sdk.textpattern.ui.*
import io.scanbot.sdk.ui.camera.*
import io.scanbot.sdk.ui.view.textpattern.*
import io.scanbot.sdk.ui.view.textpattern.configuration.TextPatternScannerConfiguration
import io.scanbot.sdk.ui.view.textpattern.entity.*

/*
    NOTE: this snippet of code is to be used only as a part of the website documentation.
    This code is not intended for any use outside of the support of documentation by Scanbot SDK GmbH employees.
*/

// NOTE for maintainers: whenever changing this code,
// ensure that links using it are still pointing to valid lines!
// Pay attention to imports adding/removal/sorting!
// Page URLs using this code:
// TODO: add URLs here

fun initSdkSnippet(application: Application, licenseKey: String) {
    // @Tag("Initialize SDK")
    ScanbotSDKInitializer()
            .license(application, licenseKey)
            .prepareOCRLanguagesBlobs(true)
            //...
            .initialize(application)
    // @EndTag("Initialize SDK")
}

fun startTextPatternScannerRTUAndHandleResultSnippet(activity: AppCompatActivity, myButton: Button) {
    // @Tag("Start RTU Text Pattern Scanner and handle the result")
    val textPatternResult: ActivityResultLauncher<TextPatternScannerConfiguration>

    // ...

    textPatternResult = activity.registerForActivityResult(TextPatternScannerActivity.ResultContract()) { result ->
        if (result.resultOk) {
            // TODO: here you can add the result handling
        }
    }

    // ...

    myButton.setOnClickListener {
        val step = TextPatternScannerStep(
                stepTag = "One-line text",
                title = "One-line text scanning",
                guidanceText = "Scan any one-line text",
                // You may set a pattern for the expected text or use validation callback for that
                // For the pattern: # - digits, ? - for any character. Other characters represent themselves
                // pattern = "######",
                // TODO: set validation string and validation callback which matches the need of the task
                // For example we may be waiting for a string which starts with 1 or 2, and then 5 more digits
                // validationCallback = object : TextDataScannerStep.GenericTextValidationCallback {
                //     override fun validate(text: String): Boolean {
                //         return text.first() in listOf('1', '2') // TODO: add additional validation for the recognized text
                //     }
                // },
                // preferredZoom = 1.6f
                // You may also set a cleaner callback to clean the recognized text before validation
                // For example, we may want to remove all whitespaces from the recognized text or apply the regex
                // cleanRecognitionResultCallback = ...
        )

        val textPatternScannerConfiguration = TextPatternScannerConfiguration(step)
        textPatternResult.launch(textPatternScannerConfiguration)
    }
    // @EndTag("Start RTU Text Pattern Scanner and handle the result")
}

val GTR_REQUEST_CODE_CONSTANT = 1000

fun startTextPatternScannerRTUDeprecatedSnippet(activity: AppCompatActivity, myButton: Button) {
    // @Tag("(DEPRECATED) Start RTU Text Pattern Scanner")
    myButton.setOnClickListener {
        val step = TextPatternScannerStep(
                stepTag = "One-line text",
                title = "One-line text scanning",
                guidanceText = "Scan any one-line text",
        )
        val configuration = TextPatternScannerConfiguration(step)
        val intent = TextPatternScannerActivity.newIntent(activity, configuration)
        activity.startActivityForResult(intent, GTR_REQUEST_CODE_CONSTANT)
    }
    // @EndTag("(DEPRECATED) Start RTU Text Pattern Scanner")
}

fun handleResultDeprecatedSnippet(requestCode: Int, resultCode: Int, data: Intent?) {
    // @Tag("(DEPRECATED) Handle RTU Text Pattern Scanner result")
    if (requestCode == GTR_REQUEST_CODE_CONSTANT) {
        val resultEntity: TextPatternScannerActivity.Result = TextPatternScannerActivity.extractResult(resultCode, data)
        if (resultEntity.resultOk) {
            // TODO: here you can add the result handling
        }
    }
    // @EndTag("(DEPRECATED) Handle RTU Text Pattern Scanner result")
}

fun setTextPatternScannerRTUConfigurationSnippet(context: Context) {
    // @Tag("Set RTU Text Pattern Scanner configuration")
    val step = TextPatternScannerStep(
            stepTag = "One-line text",
            title = "One-line text scanning",
            guidanceText = "Scan any one-line text",
    )
    val configuration = TextPatternScannerConfiguration(step)
    configuration.setTopBarBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
    configuration.setTopBarButtonsColor(ContextCompat.getColor(context, R.color.greyColor))
    // @EndTag("Set RTU Text Pattern Scanner configuration")
}

fun handleTextPatternScannerRTUResultSnippet(context: Context, result: TextPatternScannerActivity.Result) {
    // @Tag("Handle RTU Text Pattern Scanner result")
    if (result.resultOk) {
        Toast.makeText(context, result.result!!.first().text, Toast.LENGTH_LONG).show()
    }
    // @EndTag("Handle RTU Text Pattern Scanner result")
}

fun useTextPatternScannerFrameHandlerSnippet(context: Context, cameraView: ScanbotCameraXView) {
    // @Tag("Attach Text Pattern Scanner to ScanbotCameraXView")
    val scanbotSdk = ScanbotSDK(context)
    val patternScanner = scanbotSdk.createTextPatternScanner()
    val patternScannerFrameHandler = TextPatternScannerFrameHandler.attach(cameraView, patternScanner)
    // @EndTag("Attach Text Pattern Scanner to ScanbotCameraXView")
}

fun setupValidatorSnippet(patternScanner: TextPatternScanner) {
    // @Tag("Setup Text Pattern Scanner validator")
    // will pass all the strings in the format "0123 123456"
    patternScanner.configuration.validator = PatternContentValidator(pattern = "#### ######")
    // @EndTag("Setup Text Pattern Scanner validator")
}

fun addResultHandlerForTextPatternScannerFrameHandlerSnippet(activity: AppCompatActivity, patternScannerFrameHandler: TextPatternScannerFrameHandler) {
    // @Tag("Add a result handler for Text Pattern Scanner Frame Handler")
    patternScannerFrameHandler.addResultHandler(object : TextPatternScannerFrameHandler.ResultHandler {
        override fun handle(result: FrameHandlerResult<TextPatternScannerResult, SdkLicenseError>): Boolean {
            if (result is FrameHandlerResult.Success && result.value.validationSuccessful) {
                // NOTE: 'handle' method runs in background thread - don't forget to switch to main before touching any Views
                activity.runOnUiThread {
                    // proceedToResult(result.value.rawText)
                }
                return true
            }
            return false
        }
    })
    // @EndTag("Add a result handler for Text Pattern Scanner Frame Handler")
}

fun scannerCleanerAndValidatorSnippet(textPatternScanner: TextPatternScanner) {
    // @Tag("Set Text Pattern Scanner cleaner and validator")
    textPatternScanner.configuration = textPatternScanner.configuration.apply {
        // These parameters allow customizing the performance and quality of recognition. The default values mean that,
        // to return a result from the recognizer, it is required that 2 of the 3 latest scanned frames contain
        // the same recognized result
        this.maximumNumberOfAccumulatedFrames = 3
        this.minimumNumberOfRequiredFramesWithEqualScanningResult = 3

        this.ocrResolutionLimit = 500
        this.validator = CustomContentValidator(
                // Set which symbols are supported by recognizer
                allowedCharacters = "0123456789",
                callback = object : ContentValidationCallback {
                    // CUSTOM CLEANER FUNCTION.
                    // If the string you intend on scanning is not clearly separated from other parts of the text
                    // then enable this setting. This will only work with 'pattern' variable from the validator:
                    override fun clean(rawText: String): String {
                        // clean rawText here
                        return rawText
                    }
                    // CUSTOM VALIDATION FUNCTION in addition to a pattern:
                    override fun validate(text: String): Boolean {
                        return text.first() in listOf('1', '2') // TODO: add additional validation for the recognized text
                    }
                })
    }
    // @EndTag("Set Text Pattern Scanner cleaner and validator")
}

fun bindWordboxPreviewViewSnippet(activity: AppCompatActivity, cameraView: ScanbotCameraXView, wordboxPreviewView: WordboxPreviewView) {
    // @Tag("Bind WordboxPreviewView with Text Pattern Scanner result")
    cameraView.addFrameHandler(object : FrameHandler() {
        override fun handleFrame(previewFrame: FrameHandler.Frame): Boolean {
            wordboxPreviewView.frameWidth = previewFrame.width
            wordboxPreviewView.frameHeight = previewFrame.height
            wordboxPreviewView.frameOrientation = previewFrame.frameOrientation
            return false
        }
    })
    // @EndTag("Bind WordboxPreviewView with Text Pattern Scanner result")
}

fun updateWordboxPreviewViewSnippet(activity: AppCompatActivity, cameraView: ScanbotCameraXView, textPatternScanner: TextPatternScanner, wordboxPreviewView: WordboxPreviewView) {
    // @Tag("Update WordboxPreviewView with Text Pattern Scanner result")
    val genericTextRecognizerFrameHandler = TextPatternScannerFrameHandler.attach(cameraView, textPatternScanner)
    genericTextRecognizerFrameHandler.addResultHandler { result ->
        activity.runOnUiThread {
            // `wordboxPreviewView.updateCharacters(...)` triggers the update of the UI, so it should be called from the UI thread
            wordboxPreviewView.updateCharacters(
                    when (result) {
                        is FrameHandlerResult.Success -> {
                            result.value.wordBoxes
                        }
                        else -> listOf()
                    }
            )
        }

        false
    }
    // @EndTag("Update WordboxPreviewView with Text Pattern Scanner result")
}