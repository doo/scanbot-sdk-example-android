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
import io.scanbot.genericdocument.entity.*
import io.scanbot.sap.*
import io.scanbot.sdk.*
import io.scanbot.sdk.check.*
import io.scanbot.sdk.check.entity.*
import io.scanbot.sdk.documentdata.*
import io.scanbot.sdk.documentdata.entity.*
import io.scanbot.sdk.ehicscanner.*
import io.scanbot.sdk.genericdocument.entity.*
import io.scanbot.sdk.mc.*
import io.scanbot.sdk.ui.registerForActivityResultOk
import io.scanbot.sdk.ui.view.check.*
import io.scanbot.sdk.ui.view.check.configuration.CheckScannerConfiguration
import io.scanbot.sdk.ui.view.documentdata.*
import io.scanbot.sdk.ui.view.documentdata.configuration.DocumentDataExtractorConfiguration
import io.scanbot.sdk.ui.view.hic.*
import io.scanbot.sdk.ui.view.hic.configuration.*
import io.scanbot.sdk.ui.view.mc.*
import io.scanbot.sdk.ui.view.mc.configuration.*
import io.scanbot.sdk.ui.view.textpattern.*
import io.scanbot.sdk.ui.view.textpattern.configuration.*
import io.scanbot.sdk.ui.view.textpattern.entity.*
import io.scanbot.sdk.ui.view.vin.*
import io.scanbot.sdk.ui.view.vin.configuration.*
import io.scanbot.sdk.ui_v2.common.activity.*
import io.scanbot.sdk.ui_v2.mrz.*
import io.scanbot.sdk.ui_v2.mrz.configuration.*

class MainActivity : AppCompatActivity() {

    private val scanbotSdk: ScanbotSDK by lazy { ScanbotSDK(this) }

    private val mrzDefaultUiResultLauncher: ActivityResultLauncher<MrzScannerScreenConfiguration>
    private val textDataScannerResultLauncher: ActivityResultLauncher<TextPatternScannerConfiguration>
    private val vinScannerResultLauncher: ActivityResultLauncher<VinScannerConfiguration>
    private val medicalCertificateScannerActivityResultLauncher: ActivityResultLauncher<MedicalCertificateScannerConfiguration>
    private val ehicScannerResultLauncher: ActivityResultLauncher<HealthInsuranceCardScannerConfiguration>
    private val dataExtractorResultLauncher: ActivityResultLauncher<DocumentDataExtractorConfiguration>
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
            mrzCameraConfiguration.mrzExampleOverlay = MrzFinderLayoutPreset.threeLineMrzFinderLayoutPreset()
            mrzDefaultUiResultLauncher.launch(mrzCameraConfiguration)
        }

        findViewById<View>(R.id.text_data_scanner_default_ui).setOnClickListener {
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

            val textDataScannerConfiguration = TextPatternScannerConfiguration(step)

            textDataScannerConfiguration.setTopBarBackgroundColor(
                ContextCompat.getColor(this, R.color.colorPrimaryDark)
            )
            textDataScannerConfiguration.setTopBarButtonsColor(
                ContextCompat.getColor(this, R.color.greyColor)
            )

            textDataScannerResultLauncher.launch(textDataScannerConfiguration)
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

        findViewById<View>(R.id.generic_document_default_ui).setOnClickListener {
            val genericDocumentConfiguration = DocumentDataExtractorConfiguration()
            genericDocumentConfiguration.setTopBarButtonsInactiveColor(
                ContextCompat.getColor(this, android.R.color.white)
            )
            genericDocumentConfiguration.setTopBarBackgroundColor(
                ContextCompat.getColor(this, R.color.colorPrimaryDark)
            )
            genericDocumentConfiguration.setFieldsDisplayConfiguration(
                hashMapOf(
                    DePassport.NormalizedFieldNames.PHOTO to FieldProperties(
                        "My passport photo",
                        FieldProperties.DisplayState.AlwaysVisible
                    ),
                    MRZ.NormalizedFieldNames.CHECK_DIGIT_GENERAL to FieldProperties(
                        "Check digit general",
                        FieldProperties.DisplayState.AlwaysVisible
                    )
                )
            )
            dataExtractorResultLauncher.launch(genericDocumentConfiguration)
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

    private fun handleGenericDocScannerResult(result: List<DocumentDataExtractionResult>) {
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
        mrzDefaultUiResultLauncher =
            registerForActivityResultOk(MrzScannerActivity.ResultContract()) { resultEntity ->
                resultEntity.result?.mrzDocument?.let { showMrzDialog(it) }
            }

        textDataScannerResultLauncher =
            registerForActivityResultOk(TextPatternScannerActivity.ResultContract()) { resultEntity ->
                val textDataScannerStepResult = resultEntity.result!!.first()
                Toast.makeText(
                    this@MainActivity,
                    "Scanned: ${textDataScannerStepResult.text}",
                    Toast.LENGTH_LONG
                ).show()
            }

        vinScannerResultLauncher =
            registerForActivityResultOk(VinScannerActivity.ResultContract()) { resultEntity ->
                val vinScanResult = resultEntity.result!!
                Toast.makeText(
                    this@MainActivity,
                    "VIN Scanned: ${vinScanResult.rawText}",
                    Toast.LENGTH_LONG
                ).show()
            }

        ehicScannerResultLauncher =
            registerForActivityResultOk(HealthInsuranceCardScannerActivity.ResultContract()) { resultEntity ->
                showEHICResultDialog(resultEntity.result!!)
            }

        dataExtractorResultLauncher =
            registerForActivityResultOk(DocumentDataExtractorActivity.ResultContract()) { resultEntity ->
                handleGenericDocScannerResult(resultEntity.result!!)
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
