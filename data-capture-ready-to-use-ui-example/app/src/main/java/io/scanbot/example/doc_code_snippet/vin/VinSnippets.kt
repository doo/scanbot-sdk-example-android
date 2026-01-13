package io.scanbot.example.doc_code_snippet.vin

import android.app.Application
import android.content.Context
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.common.*
import io.scanbot.sdk.*
import io.scanbot.sdk.ui.camera.*
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

// @Tag("Initialize SDK")
fun initSdkSnippet(application: Application, licenseKey: String) {
    ScanbotSDKInitializer()
        .license(application, licenseKey)
        .prepareOCRLanguagesBlobs(true)
        //...
        .initialize(application)
}
// @EndTag("Initialize SDK")

// @Tag("Attach Vin Scanner to ScanbotCameraXView")
fun useVinScannerFrameHandlerSnippet(context: Context, cameraView: ScanbotCameraXView) {
    val scanbotSdk = ScanbotSDK(context)
    val vinScanner = scanbotSdk.createVinScanner().getOrThrow()
    val vinScannerFrameHandler = VinScannerFrameHandler.attach(cameraView, vinScanner)
}
// @EndTag("Attach Vin Scanner to ScanbotCameraXView")

// @Tag("Add a result handler for Vin Scanner Frame Handler")
fun addResultHandlerForVinScannerFrameHandlerSnippet(
    activity: AppCompatActivity,
    vinScannerFrameHandler: VinScannerFrameHandler,
    resultTextView: TextView
) {
    vinScannerFrameHandler.addResultHandler(VinScannerFrameHandler.ResultHandler { result, frame ->
        val resultText: String = result.mapSuccess { value ->
            if (value.textResult.validationSuccessful) {
                value.textResult.rawText
            } else {
                "VIN not found"
            }
        }.getOrNull() ?: "Error during VIN recognition"

        // NOTE: 'handle' method runs in background thread - don't forget to switch to main before touching any Views
        activity.runOnUiThread { resultTextView.text = resultText }

        false
    })
}
// @EndTag("Add a result handler for Vin Scanner Frame Handler")
