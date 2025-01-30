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

import io.scanbot.sdk.documentdata.entity.*
import io.scanbot.sdk.mrz.*
import io.scanbot.sdk.ui.camera.*
import io.scanbot.sdk.ui_v2.common.*
import io.scanbot.sdk.ui_v2.mrz.*
import io.scanbot.sdk.ui_v2.mrz.configuration.*

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

        val resultLauncher: ActivityResultLauncher<MrzScannerScreenConfiguration>

        resultLauncher =
            registerForActivityResult(MrzScannerActivity.ResultContract()) { resultEntity: MrzScannerActivity.Result ->
                if (resultEntity.resultOk) {
                    Toast.makeText(context, resultEntity.result?.rawMRZ, Toast.LENGTH_LONG).show()
                }
            }

        myButton.setOnClickListener {
            val configuration = MrzScannerScreenConfiguration()
            configuration.mrzExampleOverlay = MrzFinderLayoutPreset.threeLineMrzFinderLayoutPreset().apply {
                //  this.mrzTextLine1 = "ARD<<MUSTER<<<<<<<<<<<<<<<<<<"
            }
            configuration.palette.apply { sbColorPrimary = ScanbotColor("#FFFF00") }
            configuration.viewFinder.apply {
                style = FinderStyle.finderStrokedStyle().apply {
                    strokeColor = ScanbotColor("#FFFFFF")
                    strokeWidth = 10.0
                }
            }
            configuration.actionBar.flashButton.activeBackgroundColor = ScanbotColor("#FFCE5C")
            configuration.actionBar.flashButton.activeForegroundColor = ScanbotColor("#000000")

            configuration.vibration = Vibration(true)
            configuration.sound = Sound(true)
            configuration.topUserGuidance.title.text = "Scan your ID"
            resultLauncher.launch(configuration)
        }
    }
    // @EndTag("result-api")

}

class DeprecatedApiCall : AppCompatActivity() {
    val MRZ_REQUEST_CODE_CONSTANT = 1232134
    fun deprecatedResultApi(myButton: Button) {
        // @Tag("deprecated")

        myButton.setOnClickListener {
            val mrzCameraConfiguration = MrzScannerScreenConfiguration()
            val intent =
                MrzScannerActivity.newIntent(this@DeprecatedApiCall, mrzCameraConfiguration)
            startActivityForResult(intent, MRZ_REQUEST_CODE_CONSTANT)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == MRZ_REQUEST_CODE_CONSTANT) {
            val resultEntity: MrzScannerActivity.Result =
                MrzScannerActivity.extractResult(resultCode, data)
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

fun MrzScreenConfiguration() {
    // @Tag("Mrz Scanner Screen Configuration")
    val mrzCameconfiguration = MrzScannerScreenConfiguration()
    mrzCameconfiguration.mrzExampleOverlay = MrzFinderLayoutPreset.twoLineMrzFinderLayoutPreset().apply {
      //  this.mrzTextLine1 = "ARD<<MUSTER<<<<<<<<<<<<<<<<<<"
    }
    mrzCameconfiguration.topBar.backgroundColor = ScanbotColor("#FF0000")

    mrzCameconfiguration.actionBar.zoomButton.backgroundColor = ScanbotColor("#FFCE5C")
    mrzCameconfiguration.actionBar.zoomButton.foregroundColor = ScanbotColor("#000000")

    mrzCameconfiguration.vibration = Vibration(true)
    mrzCameconfiguration.sound = Sound(true)
    mrzCameconfiguration.topUserGuidance.title.text = "Scan your ID"
    // @EndTag("Mrz Scanner Screen Configuration")
}

fun presentingMrzResultSnippet(result: MrzScannerUiResult, context: Context) {
    // @Tag("Presenting Mrz Result")
    val mrzString = result?.mrzDocument?.fields?.joinToString("\n") { "${it.type.name}: ${it.value?.text}" } ?: ""
    Toast.makeText(context, mrzString, Toast.LENGTH_LONG).show()
    // @EndTag("Presenting Mrz Result")
}

fun mrzScannerResult(result: MrzScannerUiResult) {
    // @Tag("Mrz Scanner Result")
    val mrzScanningResult = MRZ(result?.mrzDocument!!)
    val givenName: String = mrzScanningResult.givenNames.value.text
    val birthDate: String = mrzScanningResult.birthDate.value.text
    val expiryDate: String? = mrzScanningResult.expiryDate?.value?.text
    // @EndTag("Mrz Scanner Result")
}


//Classic snippets

fun getInstances(context: Context, cameraView: ScanbotCameraXView) {
    // @Tag("Get Instances")
    val scanbotSDK = ScanbotSDK(context)
    val scanner: MrzScanner = scanbotSDK.createMrzScanner()
    val frameHandler: MrzScannerFrameHandler =
        MrzScannerFrameHandler.attach(cameraView, scanner)
    // @EndTag("Get Instances")
}

fun handleResult(MrzScannerFrameHandler: MrzScannerFrameHandler){
    // @Tag("Handle Result")
    MrzScannerFrameHandler.addResultHandler { result ->
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