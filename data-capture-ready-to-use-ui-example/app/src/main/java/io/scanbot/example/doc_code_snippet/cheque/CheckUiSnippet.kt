package io.scanbot.example.doc_code_snippet.cheque

import android.content.Context
import android.content.Intent
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.example.*
import io.scanbot.sdk.*
import io.scanbot.sdk.camera.*
import io.scanbot.sdk.check.*
import io.scanbot.sdk.check.entity.RootDocumentType.*

import io.scanbot.sdk.mrz.*
import io.scanbot.sdk.ui.camera.*
import io.scanbot.sdk.ui.view.check.*
import io.scanbot.sdk.ui.view.check.configuration.CheckScannerConfiguration

//Rtu ui snippets
fun initializeScanbotSDK(application: Application) {
    // @Tag("InitializeScanbotSDK")
    ScanbotSDKInitializer()
        .initialize(application)
    // @EndTag("InitializeScanbotSDK")
}


class ResultApiCall : AppCompatActivity() {

    fun resultApi(myButton: Button) {
        // @Tag("result-api")

        val resultLauncher: ActivityResultLauncher<CheckScannerConfiguration>

        resultLauncher =
            registerForActivityResult(CheckScannerActivity.ResultContract()) { resultEntity: CheckScannerActivity.Result ->
                if (resultEntity.resultOk) {
                    // `resultEntity` contains `ResultWrapper<CheckRecognizerResult>` which wraps the result and points to the specific result in one of the `ResultRepository`s (which contains cached recognition results)
                    val resultWrapper = resultEntity.result!!
                    val checkDocument = resultWrapper.check
                    if (checkDocument != null) {
                        wrapCheck(checkDocument)
                    }
                    // Here you can handle `checkDocument` and present recognized Check information (routing number, account number, etc.)
                }
            }

        myButton.setOnClickListener {
            val configuration = CheckScannerConfiguration()
            resultLauncher.launch(configuration)
        }
    }
    // @EndTag("result-api")

}

class DeprecatedApiCall : AppCompatActivity() {
    val CHECK_REQUEST_CODE_CONSTANT = 1232134
    fun deprecatedResultApi(myButton: Button) {
        // @Tag("deprecated")

        myButton.setOnClickListener {
            val configuration = CheckScannerConfiguration()
            val intent = CheckScannerActivity.newIntent(this@DeprecatedApiCall, configuration)
            startActivityForResult(intent, CHECK_REQUEST_CODE_CONSTANT)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CHECK_REQUEST_CODE_CONSTANT) {
            val resultEntity: CheckScannerActivity.Result =
                CheckScannerActivity.extractResult(resultCode, data)
            if (resultEntity.resultOk) {
                // `resultEntity` contains `ResultWrapper<CheckRecognizerResult>` which wraps the result and points to the specific result in one of the `ResultRepository`s (which contain cached recognition results)
                val resultWrapper = resultEntity.result!!
                val checkDocument = resultWrapper.check
                if (checkDocument != null) {
                    wrapCheck(checkDocument)
                }
                // Here you can handle `checkDocument` and present recognized Check information (routing number, account number, etc.)
            }
        }
    }
    // @EndTag("deprecated")
}

fun checkScannerConfiguration() {
    // @Tag("Check Scanner Configuration")
    val configuration = CheckScannerConfiguration()
    configuration.setCaptureHighResolutionImage(false)
    configuration.setAcceptedCheckStandards(
        arrayListOf(
            USACheck,
            KWTCheck,
            AUSCheck,
            FRACheck,
            INDCheck,
            ISRCheck
        )
    )

    // @EndTag("Check Scanner Configuration")
}

fun checkScannerResult(result: CheckScanningResult) {
    // @Tag("Check Scanner Result")
    val document = result.check
    val scanningStatus = result.status
    val documentDetectionResult = result.documentDetectionResult
    val croppedImage = result.croppedImage
    // @EndTag("Check Scanner Result")
}


//Classic snippets

fun getInstances(context: Context, cameraView: ScanbotCameraXView) {
    // @Tag("Get Instances")
    val scanbotSDK = ScanbotSDK(context)
    val checkScanner: CheckScanner = scanbotSDK.createCheckScanner()
    val checkScannerFrameHandler: CheckScannerFrameHandler =
        CheckScannerFrameHandler.attach(cameraView, checkScanner)
    // @EndTag("Get Instances")
}

fun handleResult(checkScannerFrameHandler: CheckScannerFrameHandler) {
    // @Tag("Handle Result")
    checkScannerFrameHandler.addResultHandler(object : CheckScannerFrameHandler.ResultHandler {
        override fun handle(result: FrameHandlerResult<CheckScanningResult, SdkLicenseError>): Boolean {
            when (result) {
                is FrameHandlerResult.Success -> {
                    val checkResult: CheckScanningResult? =
                        (result as FrameHandlerResult.Success<CheckScanningResult?>).value
                    if (checkResult?.check != null) {
                        // do something with result here
                        val checkDocument = checkResult.check
                        if (checkDocument != null) {
                            wrapCheck(checkDocument)
                        }
                    }
                }

                is FrameHandlerResult.Failure -> {
                } // handle license error here
            }

            return false
        }
    })
    // @EndTag("Handle Result")
}