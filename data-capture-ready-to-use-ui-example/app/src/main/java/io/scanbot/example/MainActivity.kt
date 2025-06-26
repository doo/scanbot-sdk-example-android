package io.scanbot.example

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import io.scanbot.example.databinding.*
import io.scanbot.example.fragments.*
import io.scanbot.sap.*
import io.scanbot.sdk.*
import io.scanbot.sdk.check.*
import io.scanbot.sdk.check.entity.*
import io.scanbot.sdk.creditcard.entity.*
import io.scanbot.sdk.ehicscanner.*
import io.scanbot.sdk.genericdocument.entity.*
import io.scanbot.sdk.mc.*
import io.scanbot.sdk.ui.*
import io.scanbot.sdk.ui.view.check.*
import io.scanbot.sdk.ui.view.check.configuration.CheckScannerConfiguration
import io.scanbot.sdk.ui.view.hic.*
import io.scanbot.sdk.ui.view.hic.configuration.*
import io.scanbot.sdk.ui.view.mc.*
import io.scanbot.sdk.ui.view.mc.configuration.*
import io.scanbot.sdk.ui.view.vin.*
import io.scanbot.sdk.ui.view.vin.configuration.*
import io.scanbot.sdk.ui_v2.common.*
import io.scanbot.sdk.ui_v2.common.activity.*
import io.scanbot.sdk.ui_v2.creditcard.*
import io.scanbot.sdk.ui_v2.creditcard.configuration.*
import io.scanbot.sdk.ui_v2.documentdata.*
import io.scanbot.sdk.ui_v2.documentdataextractor.configuration.*
import io.scanbot.sdk.ui_v2.mrz.*
import io.scanbot.sdk.ui_v2.mrz.configuration.*
import io.scanbot.sdk.ui_v2.textpattern.*
import io.scanbot.sdk.ui_v2.textpattern.configuration.*

class MainActivity : AppCompatActivity() {

    private val scanbotSdk: ScanbotSDK by lazy { ScanbotSDK(this) }

    private val mrzDefaultUiResultLauncher: ActivityResultLauncher<MrzScannerScreenConfiguration>
    private val creditCardUiResultLauncher: ActivityResultLauncher<CreditCardScannerScreenConfiguration>
    private val textDataScannerResultLauncher: ActivityResultLauncher<TextPatternScannerScreenConfiguration>
    private val vinScannerResultLauncher: ActivityResultLauncher<VinScannerConfiguration>
    private val medicalCertificateScannerActivityResultLauncher: ActivityResultLauncher<MedicalCertificateScannerConfiguration>
    private val ehicScannerResultLauncher: ActivityResultLauncher<HealthInsuranceCardScannerConfiguration>
    private val dataExtractorResultLauncher: ActivityResultLauncher<DocumentDataExtractorScreenConfiguration>
    private val checkScannerResultLauncher: ActivityResultLauncher<CheckScannerConfiguration>

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        findViewById<View>(R.id.mrz_camera_default_ui).setOnClickListener {
            val mrzCameraConfiguration = MrzScannerScreenConfiguration()
            mrzCameraConfiguration.cameraConfiguration.apply {
                flashEnabled = false
            }
            mrzCameraConfiguration.mrzExampleOverlay =
                MrzFinderLayoutPreset.threeLineMrzFinderLayoutPreset()
            mrzCameraConfiguration.topBar.backgroundColor = ScanbotColor(Color.BLACK)

            mrzDefaultUiResultLauncher.launch(mrzCameraConfiguration)
        }

        findViewById<View>(R.id.text_pattern_scanner_default_ui).setOnClickListener {
            // Create the default configuration object.
            val textPatternScannerConfiguration = TextPatternScannerScreenConfiguration()
            // Configure what string should be passed as successfully scanned text.
            /*   textPatternScannerConfiguration.scannerConfiguration.validator = CustomContentValidator().apply {
                        val pattern = Pattern.compile("^[0-9]{4}$") // e.g. 4 digits
                        this.callback = object : ContentValidationCallback {
                            override fun clean(rawText: String): String {
                                return rawText.replace(" ", "")
                            }

                            override fun validate(text: String): Boolean {
                                val matcher = pattern.matcher(text)
                                return matcher.find()
                            }
                        }
                    }*/
            textPatternScannerConfiguration.topBar.backgroundColor = ScanbotColor(Color.BLACK)
            textDataScannerResultLauncher.launch(textPatternScannerConfiguration)
        }

        findViewById<View>(R.id.vin_scanner_default_ui).setOnClickListener {
            val vinScannerConfiguration = VinScannerConfiguration()

            vinScannerConfiguration.setTopBarBackgroundColor(
                ContextCompat.getColor(this, R.color.colorPrimaryDark)
            )
            vinScannerConfiguration.setTopBarButtonsColor(
                ContextCompat.getColor(this, R.color.greyColor)
            )

            vinScannerResultLauncher.launch(vinScannerConfiguration)
        }

        findViewById<View>(R.id.credit_card_scanner_default_ui).setOnClickListener {
            val creditCardScannerConfiguration = CreditCardScannerScreenConfiguration()
            creditCardScannerConfiguration.topBar.backgroundColor = ScanbotColor(Color.BLACK)
            creditCardUiResultLauncher.launch(creditCardScannerConfiguration)
        }

        findViewById<View>(R.id.data_extractor_default_ui).setOnClickListener {
            val configuration = DocumentDataExtractorScreenConfiguration()

            configuration.topBar.backgroundColor = ScanbotColor(
                ContextCompat.getColor(this, R.color.colorPrimaryDark)
            )
            dataExtractorResultLauncher.launch(configuration)
        }

        binding.ehicDefaultUi.setOnClickListener {
            val ehicScannerConfig = HealthInsuranceCardScannerConfiguration()
            ehicScannerConfig.setTopBarButtonsColor(Color.WHITE)
            ehicScannerConfig.setRecognizerParameters(
                EuropeanHealthInsuranceCardRecognizerConfiguration(
                    // Add your parameters here if needed
                )
            )
            // ehicScannerConfig.setTopBarBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
            // ehicScannerConfig.setFinderTextHint("custom text")
            // ...

            ehicScannerResultLauncher.launch(ehicScannerConfig)
        }

        binding.checkRecognizerUi.setOnClickListener {
            val config = CheckScannerConfiguration().apply {
                setTopBarBackgroundColor(
                    ContextCompat.getColor(
                        this@MainActivity,
                        R.color.colorPrimaryDark
                    )
                )
                setTopBarButtonsColor(ContextCompat.getColor(this@MainActivity, R.color.greyColor))
            }

            checkScannerResultLauncher.launch(config)
        }

        binding.mcScannerUi.setOnClickListener {
            val config = MedicalCertificateScannerConfiguration().apply {
                setTopBarBackgroundColor(
                    ContextCompat.getColor(
                        this@MainActivity,
                        R.color.colorPrimaryDark
                    )
                )
                setTopBarButtonsColor(ContextCompat.getColor(this@MainActivity, R.color.greyColor))
            }

            medicalCertificateScannerActivityResultLauncher.launch(config)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!scanbotSdk.licenseInfo.isValid) {
            showLicenseDialog()
        }
        binding.warningView.visibility =
            if (scanbotSdk.licenseInfo.status != Status.StatusOkay) View.VISIBLE else View.GONE
    }

    private fun handleGenericDocScannerResult(result: List<DocumentDataExtractorUiResult>) {
        result
        Toast.makeText(
            this,
            result.joinToString {
                it?.document?.fields?.joinToString { "${it.type.name} = ${it.value?.text}" } ?: ""
            },
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showLicenseDialog() {
        if (supportFragmentManager.findFragmentByTag(ErrorFragment.NAME) == null) {
            val dialogFragment = ErrorFragment.newInstance()
            dialogFragment.show(supportFragmentManager, ErrorFragment.NAME)
        }
    }

    private fun showMrzDialog(genericDocument: GenericDocument) {
        val dialogFragment = MRZDialogFragment.newInstance(genericDocument)
        dialogFragment.show(supportFragmentManager, MRZDialogFragment.NAME)
    }

    private fun showEHICResultDialog(recognitionResult: EuropeanHealthInsuranceCardRecognitionResult) {
        val dialogFragment = EHICResultDialogFragment.newInstance(recognitionResult)
        dialogFragment.show(supportFragmentManager, EHICResultDialogFragment.NAME)
    }

    private fun handleMedicalCertificateResult(resultWrapper: MedicalCertificateScanningResult) {


        showMedicalCertificateScannerResult(resultWrapper!!)
    }

    private fun showMedicalCertificateScannerResult(recognitionResult: MedicalCertificateScanningResult) {
        val dialogFragment = MedicalCertificateResultDialogFragment.newInstance(recognitionResult)
        dialogFragment.show(supportFragmentManager, MedicalCertificateResultDialogFragment.NAME)
    }

    private fun handleCheckScannerResult(result: CheckScanningResult) {
        showCheckScannerResult(result)
    }

    private fun showCheckScannerResult(recognitionResult: CheckScanningResult) {
        val document = recognitionResult.check?.let { Check(it) } // Convert to the document model
        Toast.makeText(this, recognitionResult.toString(), Toast.LENGTH_SHORT).show()
    }

    init {
        creditCardUiResultLauncher =
            registerForActivityResult(CreditCardScannerActivity.ResultContract()) { resultEntity: CreditCardScannerActivity.Result ->
                if (resultEntity.resultOk) {
                    resultEntity.result?.creditCard?.let {
                        val creditCard = CreditCard(it)
                        val cardNumber: String = creditCard.cardNumber.value.text
                        val cardholderName: String = creditCard.cardholderName?.value?.text ?: ""
                        val expiryDate: String? = creditCard.expiryDate?.value?.text
                        Toast.makeText(
                            this,
                            "Card Number: $cardNumber, Cardholder Name: $cardholderName, Expiry Date: $expiryDate",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

        mrzDefaultUiResultLauncher =
            registerForActivityResultOk(MrzScannerActivity.ResultContract()) { resultEntity ->
                if (resultEntity.resultOk) {
                    resultEntity.result?.mrzDocument?.let {
                        showMrzDialog(it)
                    }
                }
            }

        textDataScannerResultLauncher =
            registerForActivityResult(TextPatternScannerActivity.ResultContract()) { resultEntity: TextPatternScannerActivity.Result ->
                if (resultEntity.resultOk) {
                    resultEntity.result?.rawText?.let {
                        Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                    }
                }
            }

        vinScannerResultLauncher =
            registerForActivityResultOk(VinScannerActivity.ResultContract()) { resultEntity ->
                val vinScanResult = resultEntity.result!!
                Toast.makeText(
                    this@MainActivity,
                    "VIN Scanned: ${vinScanResult.textResult.rawText}",
                    Toast.LENGTH_LONG
                ).show()
            }

        ehicScannerResultLauncher =
            registerForActivityResultOk(HealthInsuranceCardScannerActivity.ResultContract()) { resultEntity ->
                showEHICResultDialog(resultEntity.result!!)
            }

        dataExtractorResultLauncher =
            registerForActivityResultOk(DocumentDataExtractorActivity.ResultContract()) { resultEntity ->
                handleGenericDocScannerResult(listOfNotNull(resultEntity.result))
            }

        medicalCertificateScannerActivityResultLauncher =
            registerForActivityResultOk(MedicalCertificateScannerActivity.ResultContract()) { resultEntity ->
                handleMedicalCertificateResult(resultEntity.result!!)
            }

        checkScannerResultLauncher =
            registerForActivityResultOk(CheckScannerActivity.ResultContract()) { resultEntity ->
                handleCheckScannerResult(resultEntity.result!!)
            }
    }
}
