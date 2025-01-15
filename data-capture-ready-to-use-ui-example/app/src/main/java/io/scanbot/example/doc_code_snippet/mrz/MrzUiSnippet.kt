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
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.example.*
import io.scanbot.sdk.*
import io.scanbot.sdk.camera.*

import io.scanbot.sdk.common.*
import io.scanbot.sdk.documentdata.entity.*
import io.scanbot.sdk.mrz.*
import io.scanbot.sdk.ui.camera.*
import io.scanbot.sdk.ui.view.mrz.*
import io.scanbot.sdk.ui.view.mrz.configuration.*

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

        val mrzResult: ActivityResultLauncher<MRZScannerConfiguration>

        mrzResult =
            registerForActivityResult(MRZScannerActivity.ResultContract()) { resultEntity: MRZScannerActivity.Result ->
                if (resultEntity.resultOk) {
                    Toast.makeText(context, resultEntity.result?.rawMRZ, Toast.LENGTH_LONG).show()
                }
            }

        myButton.setOnClickListener {
            val mrzCameraConfiguration = MRZScannerConfiguration()
            mrzResult.launch(mrzCameraConfiguration)
        }
    }
    // @EndTag("result-api")

}

class DeprecatedApiCall : AppCompatActivity() {
    val MRZ_REQUEST_CODE_CONSTANT = 1232134
    fun deprecatedResultApi(myButton: Button) {
        // @Tag("deprecated")

        myButton.setOnClickListener {
            val mrzCameraConfiguration = MRZScannerConfiguration()
            val intent =
                MRZScannerActivity.newIntent(this@DeprecatedApiCall, mrzCameraConfiguration)
            startActivityForResult(intent, MRZ_REQUEST_CODE_CONSTANT)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == MRZ_REQUEST_CODE_CONSTANT) {
            val resultEntity: MRZScannerActivity.Result =
                MRZScannerActivity.extractResult(resultCode, data)
            if (resultEntity.resultOk) {
                Toast.makeText(
                    this@DeprecatedApiCall,
                    resultEntity.result?.rawMRZ,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    // @EndTag("deprecated")
}

fun mrzScannerConfiguration() {
    // @Tag("Mrz Scanner Screen Configuration")
    val mrzCameraConfiguration = MRZScannerConfiguration()
    mrzCameraConfiguration.setFinderAspectRatio(AspectRatio(8.0, 4.0))
    mrzCameraConfiguration.setCancelButtonTitle("Stop")
    mrzCameraConfiguration.setSuccessBeepEnabled(false)
    mrzCameraConfiguration.setFinderTextHint("Place the card in scanning rectangle")
    // @EndTag("Mrz Scanner Screen Configuration")
}

fun mrzScannerResult(result: MrzScannerResult) {
    // @Tag("Mrz Scanner Result")
    val mrzScanningResult = MRZ(result?.document!!)
    val givenName: String = mrzScanningResult.givenNames.value.text
    val birthDate: String = mrzScanningResult.birthDate.value.text
    val expiryDate: String? = mrzScanningResult.expiryDate?.value?.text
    // @EndTag("Mrz Scanner Result")
}


//Classic snippets

fun getInstances(context: Context, cameraView: ScanbotCameraXView) {
    // @Tag("Get Instances")
    val scanbotSDK = ScanbotSDK(context)
    val mrzScanner: MrzScanner = scanbotSDK.createMrzScanner()
    val mrzScannerFrameHandler: MrzScannerFrameHandler =
        MrzScannerFrameHandler.attach(cameraView, mrzScanner)
    // @EndTag("Get Instances")
}

fun handleResult(mrzScannerFrameHandler: MrzScannerFrameHandler){
    // @Tag("Handle Result")
    mrzScannerFrameHandler.addResultHandler { result ->
        when (result) {
            is FrameHandlerResult.Success -> {
                if (result.value.success) {
                    // do something with result here
                }
            }
            is FrameHandlerResult.Failure -> {
                // handle license error here
            }
        }
        false
    }
    // @EndTag("Handle Result")
}