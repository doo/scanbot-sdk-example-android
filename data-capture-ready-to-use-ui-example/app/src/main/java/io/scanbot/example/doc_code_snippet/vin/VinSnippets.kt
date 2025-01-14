package io.scanbot.example.doc_code_snippet.vin

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.sdk.*
import io.scanbot.sdk.camera.*
import io.scanbot.sdk.ui.camera.*
import io.scanbot.sdk.ui.view.vin.*
import io.scanbot.sdk.ui.view.vin.configuration.VinScannerConfiguration
import io.scanbot.sdk.vin.*

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

fun startVinScannerRTUAndHandleResultSnippet(activity: AppCompatActivity, myButton: Button) {
    // @Tag("Start RTU Vin Scanner and handle the result")
    val vinScannerResultLauncher: ActivityResultLauncher<VinScannerConfiguration>

    // ...

    vinScannerResultLauncher = activity.registerForActivityResult(VinScannerActivity.ResultContract()) { resultEntity: VinScannerActivity.Result ->
        if (resultEntity.resultOk) {
            Toast.makeText(activity, resultEntity.result?.rawText, Toast.LENGTH_LONG).show()
        }
    }

    // ...

    myButton.setOnClickListener {
        val configuration = VinScannerConfiguration()
        vinScannerResultLauncher.launch(configuration)
    }
    // @EndTag("Start RTU Vin Scanner and handle the result")
}

val VIN_SCANNER_REQUEST_CODE_CONSTANT = 1000

fun startVinScannerRTUDeprecatedSnippet(activity: AppCompatActivity, myButton: Button) {
    // @Tag("(DEPRECATED) Start RTU Vin Scanner")
    myButton.setOnClickListener {
        val configuration = VinScannerConfiguration()
        val intent = VinScannerActivity.newIntent(activity, configuration)
        activity.startActivityForResult(intent, VIN_SCANNER_REQUEST_CODE_CONSTANT)
    }
    // @EndTag("(DEPRECATED) Start RTU Vin Scanner")
}

fun handleResultDeprecatedSnippet(requestCode: Int, resultCode: Int, data: Intent?, context: Context) {
    // @Tag("(DEPRECATED) Handle RTU Vin Scanner result")
    if (requestCode == VIN_SCANNER_REQUEST_CODE_CONSTANT) {
        val result: VinScannerActivity.Result = VinScannerActivity.extractResult(resultCode, data)
        if (result.resultOk) {
            Toast.makeText(context, result.result?.rawText, Toast.LENGTH_LONG).show()
        }
    }
    // @EndTag("(DEPRECATED) Handle RTU Vin Scanner result")
}

fun setVinScannerRTUConfigurationSnippet(context: Context) {
    // @Tag("Set RTU Vin Scanner configuration")
    val configuration = VinScannerConfiguration()
    configuration.setFlashEnabled(false)
    configuration.setCancelButtonTitle("Stop")
    configuration.setSignificantShakeDelay(700)
    configuration.setGuidanceText("Scan VIN code")
    configuration.setCameraOverlayColor(Color.parseColor("#DF535454"))
    // @EndTag("Set RTU Vin Scanner configuration")
}

fun handleVinScannerRTUResultSnippet(context: Context, result: VinScannerActivity.Result) {
    // @Tag("Handle RTU Vin Scanner result")
    val scannedVinString = StringBuilder()
            .append("VIN code: ${result.result?.rawText}")
            .append("\nConfidence: ${result.result?.confidence}")
            .append("\nValidation success: ${result.result?.validationSuccessful}")
            .toString()
    Toast.makeText(context, scannedVinString, Toast.LENGTH_LONG).show()
    // @EndTag("Handle RTU Vin Scanner result")
}

fun useVinScannerFrameHandlerSnippet(context: Context, cameraView: ScanbotCameraXView) {
    // @Tag("Attach Vin Scanner to ScanbotCameraXView")
    val scanbotSdk = ScanbotSDK(context)
    val vinScanner = scanbotSdk.createVinScanner()
    val vinScannerFrameHandler = VinScannerFrameHandler.attach(cameraView, vinScanner)
    // @EndTag("Attach Vin Scanner to ScanbotCameraXView")
}

fun addResultHandlerForVinScannerFrameHandlerSnippet(activity: AppCompatActivity, vinScannerFrameHandler: VinScannerFrameHandler, resultTextView: TextView) {
    // @Tag("Add a result handler for Vin Scanner Frame Handler")
    vinScannerFrameHandler.addResultHandler(VinScannerFrameHandler.ResultHandler { result ->
        val resultText: String = when (result) {
            is FrameHandlerResult.Success -> {
                if (result.value.textResult.validationSuccessful) {
                    result.value.textResult.rawText
                } else {
                    "VIN not found"
                }
            }
            is FrameHandlerResult.Failure -> "Check your setup or license"
        }

        // NOTE: 'handle' method runs in background thread - don't forget to switch to main before touching any Views
        activity.runOnUiThread { resultTextView.text = resultText }

        false
    })
    // @EndTag("Add a result handler for Vin Scanner Frame Handler")
}
