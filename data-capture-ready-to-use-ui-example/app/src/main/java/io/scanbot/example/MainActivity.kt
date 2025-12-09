package io.scanbot.example

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import io.scanbot.common.onSuccess
import io.scanbot.example.databinding.*
import io.scanbot.example.fragments.*
import io.scanbot.example.util.*
import io.scanbot.sdk.*
import io.scanbot.sdk.check.entity.*
import io.scanbot.sdk.creditcard.entity.*
import io.scanbot.sdk.documentdata.*
import io.scanbot.sdk.documentdata.entity.*
import io.scanbot.sdk.genericdocument.*
import io.scanbot.sdk.licensing.*
import io.scanbot.sdk.ui_v2.check.*
import io.scanbot.sdk.ui_v2.check.configuration.*
import io.scanbot.sdk.ui_v2.common.*
import io.scanbot.sdk.ui_v2.common.activity.*
import io.scanbot.sdk.ui_v2.creditcard.*
import io.scanbot.sdk.ui_v2.creditcard.configuration.*
import io.scanbot.sdk.ui_v2.documentdata.*
import io.scanbot.sdk.ui_v2.documentdata.configuration.*
import io.scanbot.sdk.ui_v2.mrz.*
import io.scanbot.sdk.ui_v2.mrz.configuration.*
import io.scanbot.sdk.ui_v2.textpattern.*
import io.scanbot.sdk.ui_v2.textpattern.configuration.*
import io.scanbot.sdk.ui_v2.vin.*
import io.scanbot.sdk.ui_v2.vin.configuration.*

class MainActivity : AppCompatActivity() {

    private val scanbotSdk: ScanbotSDK by lazy { ScanbotSDK(this) }

    private val mrzDefaultUiResultLauncher: ActivityResultLauncher<MrzScannerScreenConfiguration>
    private val creditCardUiResultLauncher: ActivityResultLauncher<CreditCardScannerScreenConfiguration>
    private val textDataScannerResultLauncher: ActivityResultLauncher<TextPatternScannerScreenConfiguration>
    private val vinScannerResultLauncher: ActivityResultLauncher<VinScannerScreenConfiguration>
    private val dataExtractorResultLauncher: ActivityResultLauncher<DocumentDataExtractorScreenConfiguration>
    private val checkScannerResultLauncher: ActivityResultLauncher<CheckScannerScreenConfiguration>

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyEdgeToEdge(this.findViewById(R.id.root_view))

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
            val vinScannerConfiguration = VinScannerScreenConfiguration()


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
            val configuration = DocumentDataExtractorScreenConfiguration()
            configuration.scannerConfiguration.configurations =
                listOf(
                    DocumentDataExtractorCommonConfiguration(
                        acceptedDocumentTypes = listOf(
                            EuropeanHealthInsuranceCard.DOCUMENT_TYPE
                        )
                    ),
                    EuropeanHealthInsuranceCardConfiguration(expectedCountry = EuropeanHealthInsuranceCardIssuingCountry.GERMANY)
                )
            configuration.topBar.backgroundColor = ScanbotColor(
                ContextCompat.getColor(this, R.color.colorPrimaryDark)
            )
            dataExtractorResultLauncher.launch(configuration)
        }

        binding.checkRecognizerUi.setOnClickListener {
            val config = CheckScannerScreenConfiguration().apply {

            }

            checkScannerResultLauncher.launch(config)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!scanbotSdk.licenseInfo.isValid) {
            showLicenseDialog()
        }
        binding.warningView.visibility =
            if (scanbotSdk.licenseInfo.status != LicenseStatus.OKAY) View.VISIBLE else View.GONE
    }

    private fun handleDocumentDataExtractorResult(result: List<DocumentDataExtractorUiResult>) {
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

    private fun handleCheckScannerResult(result: CheckScannerUiResult) {
        showCheckScannerResult(result)
    }

    private fun showCheckScannerResult(recognitionResult: CheckScannerUiResult) {
        val document = recognitionResult.check?.let { Check(it) } // Convert to the document model
        Toast.makeText(this, recognitionResult.toString(), Toast.LENGTH_SHORT).show()
    }

    init {
        creditCardUiResultLauncher =
            registerForActivityResult(CreditCardScannerActivity.ResultContract()) { resultEntity ->
                resultEntity.onSuccess { result ->
                    result.creditCard?.let {
                        val creditCard = CreditCard(it)
                        val cardNumber: String = creditCard.cardNumber.value.text
                        val cardholderName: String = creditCard.cardholderName?.value?.text ?: ""
                        val expiryDate: String? = creditCard.expiryDate?.value?.text
                        Toast.makeText(
                            this@MainActivity,
                            "Card Number: $cardNumber, Cardholder Name: $cardholderName, Expiry Date: $expiryDate",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

        mrzDefaultUiResultLauncher =
            registerForActivityResultOk(MrzScannerActivity.ResultContract()) { resultEntity ->
                resultEntity.mrzDocument?.let {
                    showMrzDialog(it)
                }
            }

        textDataScannerResultLauncher =
            registerForActivityResultOk(TextPatternScannerActivity.ResultContract()) { resultEntity ->
                resultEntity.rawText.let {
                    Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                }
            }

        vinScannerResultLauncher =
            registerForActivityResultOk(VinScannerActivity.ResultContract()) { vinScanResult ->
                Toast.makeText(
                    this@MainActivity,
                    "VIN Scanned: ${vinScanResult.textResult.rawText}",
                    Toast.LENGTH_LONG
                ).show()
            }

        dataExtractorResultLauncher =
            registerForActivityResultOk(DocumentDataExtractorActivity.ResultContract()) { resultEntity ->
                handleDocumentDataExtractorResult(listOfNotNull(resultEntity))
            }

        checkScannerResultLauncher =
            registerForActivityResultOk(CheckScannerActivity.ResultContract()) { resultEntity ->
                handleCheckScannerResult(resultEntity)
            }
    }
}
