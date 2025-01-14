package io.scanbot.example.doc_code_snippet.license_plate

import android.content.Context
import android.content.Intent
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.example.*
import io.scanbot.sdk.*
import io.scanbot.sdk.camera.*
import io.scanbot.sdk.licenseplate.*
import io.scanbot.sdk.ui.camera.*
import io.scanbot.sdk.ui.view.licenseplate.*
import io.scanbot.sdk.ui.view.licenseplate.configuration.LicensePlateScannerConfiguration
import io.scanbot.sdk.ui.view.licenseplate.entity.LicensePlateScannerResult

//Rtu ui snippets
fun initializeScanbotSDK(application: Application) {
    // @Tag("InitializeScanbotSDK")
    ScanbotSDKInitializer()
        .initialize(application)
    // @EndTag("InitializeScanbotSDK")
}


class ResultApiCall : AppCompatActivity() {

    fun resultApi(myButton: Button, context: Context) {
        // @Tag("result-api")

        val licensePlateResult: ActivityResultLauncher<LicensePlateScannerConfiguration>

        licensePlateResult =
            registerForActivityResult(LicensePlateScannerActivity.ResultContract()) { resultEntity: LicensePlateScannerActivity.Result ->
                if (resultEntity.resultOk) {
                    Toast.makeText(
                        this@ResultApiCall,
                        resultEntity.result?.rawText,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

        myButton.setOnClickListener {
            val configuration = LicensePlateScannerConfiguration()
            licensePlateResult.launch(configuration)
        }
        // @EndTag("result-api")

    }
}

class DeprecatedApiCall : AppCompatActivity() {
    val LICENSE_PLATE_REQUEST_CODE_CONSTANT = 1232134
    fun deprecatedResultApi(myButton: Button) {
        // @Tag("deprecated")
        myButton.setOnClickListener {
            val configuration = LicensePlateScannerConfiguration()
            val intent =
                LicensePlateScannerActivity.newIntent(this@DeprecatedApiCall, configuration)
            startActivityForResult(intent, LICENSE_PLATE_REQUEST_CODE_CONSTANT)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == LICENSE_PLATE_REQUEST_CODE_CONSTANT) {
            if (requestCode == LICENSE_PLATE_REQUEST_CODE_CONSTANT) {
                val result: LicensePlateScannerActivity.Result =
                    LicensePlateScannerActivity.extractResult(resultCode, data)
                if (result.resultOk) {
                    Toast.makeText(
                        this@DeprecatedApiCall,
                        result.result?.rawText,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    // @EndTag("deprecated")
}

fun licensePlateScannerConfiguration() {
    // @Tag("License Plate Scanner Configuration")
    val configuration = LicensePlateScannerConfiguration()
    configuration.setFlashEnabled(false)
    configuration.setCancelButtonTitle("Stop")
    configuration.setConfirmationDialogTitle("Double-check the result")
    configuration.setFinderTextHint("Place the plate in the scanning rectangle")
    configuration.setScanStrategy(LicensePlateScannerStrategy.ML)
    // @EndTag("License Plate Scanner Configuration")
}

fun licensePlateScannerResult(result: LicensePlateScannerResult, context: Context) {
    // @Tag("License Plate Scanner Result")
    val licensePlateString = StringBuilder()
        .append("Raw scanned text: ${result.rawText}")
        .append("\nCountry code: ${result.countryCode}")
        .append("\nLicense plate: ${result.licensePlate}")
        .append("\nConfidence level: ${result.confidence}%")
        .toString()
    Toast.makeText(context, licensePlateString, Toast.LENGTH_LONG).show()
    // @EndTag("License Plate Scanner Result")
}


//Classic snippets
fun getInstances(context: Context, cameraView: ScanbotCameraXView) {
    // @Tag("Get Instances")
    val scanbotSdk = ScanbotSDK(context)
    val licensePlateScanner = scanbotSdk.createLicensePlateScanner()
    val licensePlateScannerFrameHandler =
        LicensePlateScannerFrameHandler.attach(cameraView, licensePlateScanner)
    // @EndTag("Get Instances")
}

fun handleResult(licensePlateScannerFrameHandler: LicensePlateScannerFrameHandler) {
    // @Tag("Handle Result")
    licensePlateScannerFrameHandler.addResultHandler(LicensePlateScannerFrameHandler.ResultHandler { result ->
        val resultText: String = when (result) {
            is FrameHandlerResult.Success -> {
                if (result.value.validationSuccessful) {
                    result.value.rawText
                } else {
                    "License plate not found"
                }
            }

            is FrameHandlerResult.Failure -> "Check your setup or license"
        }

        // NOTE: 'handle' method runs in background thread - don't forget to switch to main before touching any Views
        // consume resultText here
        false
    })
    // @EndTag("Handle Result")
}