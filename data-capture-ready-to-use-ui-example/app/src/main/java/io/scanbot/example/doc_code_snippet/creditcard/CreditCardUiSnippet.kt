package io.scanbot.example.doc_code_snippet.creditcard

import android.content.Context
import android.content.Intent
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.example.*
import io.scanbot.sdk.*
import io.scanbot.sdk.camera.*
import io.scanbot.sdk.creditcard.*
import io.scanbot.sdk.creditcard.entity.*

import io.scanbot.sdk.ui.camera.*
import io.scanbot.sdk.ui_v2.common.*
import io.scanbot.sdk.ui_v2.creditcard.*
import io.scanbot.sdk.ui_v2.creditcard.configuration.*

//Rtu ui snippets
fun initializeScanbotSDK(application: Application) {
    // @Tag("InitializeScanbotSDK")
    ScanbotSDKInitializer()
        .initialize(application)
    // @EndTag("InitializeScanbotSDK")
}


class CreditCardUiSnippet : AppCompatActivity() {

    fun resultApi(myButton: Button, context: Context) {
        // @Tag("result-api")

        val resultLauncher: ActivityResultLauncher<CreditCardScannerScreenConfiguration>

        resultLauncher =
            registerForActivityResult(CreditCardScannerActivity.ResultContract()) { resultEntity: CreditCardScannerActivity.Result ->
                if (resultEntity.resultOk) {
                    Toast.makeText(context, resultEntity.result?.creditCard?.fields?.joinToString { it.value?.text ?:"" }, Toast.LENGTH_LONG).show()
                }
            }

        myButton.setOnClickListener {
            val configuration = CreditCardScannerScreenConfiguration()
            configuration.exampleOverlayVisible = true
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
            configuration.topUserGuidance.title.text = "Scan your Credit Card"
            resultLauncher.launch(configuration)
        }
    }
    // @EndTag("result-api")

}

class DeprecatedApiCall : AppCompatActivity() {
    val CREDIT_CARD_REQUEST_CODE_CONSTANT = 1232134
    fun deprecatedResultApi(myButton: Button) {
        // @Tag("deprecated")
        myButton.setOnClickListener {
            val configuration = CreditCardScannerScreenConfiguration()
            val intent =
                CreditCardScannerActivity.newIntent(this@DeprecatedApiCall, configuration)
            startActivityForResult(intent, CREDIT_CARD_REQUEST_CODE_CONSTANT)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CREDIT_CARD_REQUEST_CODE_CONSTANT) {
            val resultEntity: CreditCardScannerActivity.Result =
                CreditCardScannerActivity.extractResult(resultCode, data)
            if (resultEntity.resultOk) {
                Toast.makeText(
                    this@DeprecatedApiCall,
                    resultEntity.result?.creditCard?.fields?.joinToString { it.value?.text ?:"" },
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    // @Tag("deprecated")
}

fun CreditCardScreenConfiguration() {
    // @Tag("Credit Card Scanner Screen Configuration")
    val configuration = CreditCardScannerScreenConfiguration()
    configuration.exampleOverlayVisible = true
    configuration.topBar.backgroundColor = ScanbotColor("#FF0000")

    configuration.actionBar.zoomButton.backgroundColor = ScanbotColor("#FFCE5C")
    configuration.actionBar.zoomButton.foregroundColor = ScanbotColor("#000000")

    configuration.vibration = Vibration(true)
    configuration.sound = Sound(true)
    configuration.topUserGuidance.title.text = "Scan your ID"
    // @EndTag("Credit Card Scanner Screen Configuration")
}

fun CreditCardScannerResult(result: CreditCardScannerUiResult) {
    // @Tag("Credit Card Scanner Result")
    val scanningResult = CreditCard(result?.creditCard!!)
    val cardNumber: String = scanningResult.cardNumber.value.text
    val expiryDate: String? = scanningResult.expiryDate?.value?.text
    val name: String? = scanningResult.cardholderName?.value?.text
    // @EndTag("Credit Card Scanner Result")
}


//Classic snippets

fun getInstances(context: Context, cameraView: ScanbotCameraXView) {
    // @Tag("Get Instances")
    val scanbotSDK = ScanbotSDK(context)
    val creditCardScanner: CreditCardScanner = scanbotSDK.createCreditCardScanner()
    val frameHandler: CreditCardScannerFrameHandler =
        CreditCardScannerFrameHandler.attach(cameraView, creditCardScanner)
    // @EndTag("Get Instances")
}

fun handleResult(frameHandler: CreditCardScannerFrameHandler){
    // @Tag("Handle Result")
    frameHandler.addResultHandler { result ->
        when (result) {
            is FrameHandlerResult.Success -> {
                if (result.value.scanningStatus == CreditCardScanningStatus.SUCCESS) {
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