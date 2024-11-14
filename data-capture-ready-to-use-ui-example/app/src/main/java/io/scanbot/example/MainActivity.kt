package io.scanbot.example

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import io.scanbot.check.entity.Check
import io.scanbot.example.databinding.ActivityMainBinding
import io.scanbot.example.fragments.EHICResultDialogFragment
import io.scanbot.example.fragments.ErrorFragment
import io.scanbot.example.fragments.MRZDialogFragment
import io.scanbot.example.fragments.MedicalCertificateResultDialogFragment
import io.scanbot.genericdocument.GenericDocumentRecognitionResult
import io.scanbot.genericdocument.entity.DePassport
import io.scanbot.genericdocument.entity.FieldProperties
import io.scanbot.genericdocument.entity.GenericDocument
import io.scanbot.genericdocument.entity.MRZ
import io.scanbot.sap.Status
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.checkrecognizer.CheckRecognitionResult
import io.scanbot.sdk.ehicscanner.EuropeanHealthInsuranceCardRecognitionResult
import io.scanbot.sdk.ehicscanner.EuropeanHealthInsuranceCardRecognizerConfiguration
import io.scanbot.sdk.mcscanner.MedicalCertificateRecognitionResult
import io.scanbot.sdk.mrzscanner.MrzScannerResult
import io.scanbot.sdk.ui.registerForActivityResultOk
import io.scanbot.sdk.ui.result.ResultWrapper
import io.scanbot.sdk.ui.view.check.CheckRecognizerActivity
import io.scanbot.sdk.ui.view.check.configuration.CheckRecognizerConfiguration
import io.scanbot.sdk.ui.view.genericdocument.GenericDocumentRecognizerActivity
import io.scanbot.sdk.ui.view.genericdocument.configuration.GenericDocumentRecognizerConfiguration
import io.scanbot.sdk.ui.view.generictext.TextDataScannerActivity
import io.scanbot.sdk.ui.view.generictext.configuration.TextDataScannerConfiguration
import io.scanbot.sdk.ui.view.generictext.entity.TextDataScannerStep
import io.scanbot.sdk.ui.view.hic.HealthInsuranceCardScannerActivity
import io.scanbot.sdk.ui.view.hic.configuration.HealthInsuranceCardScannerConfiguration
import io.scanbot.sdk.ui.view.licenseplate.LicensePlateScannerActivity
import io.scanbot.sdk.ui.view.licenseplate.configuration.LicensePlateScannerConfiguration
import io.scanbot.sdk.ui.view.mc.MedicalCertificateRecognizerActivity
import io.scanbot.sdk.ui.view.mc.configuration.MedicalCertificateRecognizerConfiguration
import io.scanbot.sdk.ui.view.mrz.MRZScannerActivity
import io.scanbot.sdk.ui.view.mrz.configuration.MRZScannerConfiguration
import io.scanbot.sdk.ui.view.vin.VinScannerActivity
import io.scanbot.sdk.ui.view.vin.configuration.VinScannerConfiguration

class MainActivity : AppCompatActivity() {

    private val scanbotSdk: ScanbotSDK by lazy { ScanbotSDK(this) }

    private val mrzDefaultUiResultLauncher: ActivityResultLauncher<MRZScannerConfiguration>
    private val textDataScannerResultLauncher: ActivityResultLauncher<TextDataScannerConfiguration>
    private val vinScannerResultLauncher: ActivityResultLauncher<VinScannerConfiguration>
    private val licensePlateScannerResultLauncher: ActivityResultLauncher<LicensePlateScannerConfiguration>
    private val medicalCertificateRecognizerActivityResultLauncher: ActivityResultLauncher<MedicalCertificateRecognizerConfiguration>
    private val ehicScannerResultLauncher: ActivityResultLauncher<HealthInsuranceCardScannerConfiguration>
    private val genericDocumentRecognizerResultLauncher: ActivityResultLauncher<GenericDocumentRecognizerConfiguration>
    private val checkRecognizerResultLauncher: ActivityResultLauncher<CheckRecognizerConfiguration>

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        findViewById<View>(R.id.mrz_camera_default_ui).setOnClickListener {
            val mrzCameraConfiguration = MRZScannerConfiguration()

            mrzCameraConfiguration.setTopBarBackgroundColor(
                ContextCompat.getColor(this, R.color.colorPrimaryDark)
            )
            mrzCameraConfiguration.setTopBarButtonsColor(
                ContextCompat.getColor(this, R.color.greyColor)
            )
            mrzCameraConfiguration.setSuccessBeepEnabled(false)

            mrzDefaultUiResultLauncher.launch(mrzCameraConfiguration)
        }

        findViewById<View>(R.id.text_data_scanner_default_ui).setOnClickListener {
            val step = TextDataScannerStep(
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

            val textDataScannerConfiguration = TextDataScannerConfiguration(step)

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

        findViewById<View>(R.id.license_plate_scanner_default_ui).setOnClickListener {
            val licensePlateScannerConfiguration = LicensePlateScannerConfiguration()

            licensePlateScannerConfiguration.setTopBarBackgroundColor(
                ContextCompat.getColor(this, R.color.colorPrimaryDark)
            )
            licensePlateScannerConfiguration.setTopBarButtonsColor(
                ContextCompat.getColor(this, R.color.greyColor)
            )

            licensePlateScannerResultLauncher.launch(licensePlateScannerConfiguration)
        }

        findViewById<View>(R.id.generic_document_default_ui).setOnClickListener {
            val genericDocumentConfiguration = GenericDocumentRecognizerConfiguration()
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
            genericDocumentRecognizerResultLauncher.launch(genericDocumentConfiguration)
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
            val config = CheckRecognizerConfiguration().apply {
                setTopBarBackgroundColor(
                    ContextCompat.getColor(
                        this@MainActivity,
                        R.color.colorPrimaryDark
                    )
                )
                setTopBarButtonsColor(ContextCompat.getColor(this@MainActivity, R.color.greyColor))
            }

            checkRecognizerResultLauncher.launch(config)
        }

        binding.mcScannerUi.setOnClickListener {
            val config = MedicalCertificateRecognizerConfiguration().apply {
                setTopBarBackgroundColor(
                    ContextCompat.getColor(
                        this@MainActivity,
                        R.color.colorPrimaryDark
                    )
                )
                setTopBarButtonsColor(ContextCompat.getColor(this@MainActivity, R.color.greyColor))
            }

            medicalCertificateRecognizerActivityResultLauncher.launch(config)
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

    private fun handleGenericDocRecognizerResult(result: List<GenericDocumentRecognitionResult>) {
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

    private fun showMrzDialog(mrzRecognitionResult: MrzScannerResult) {
        val dialogFragment = MRZDialogFragment.newInstance(mrzRecognitionResult)
        dialogFragment.show(supportFragmentManager, MRZDialogFragment.NAME)
    }

    private fun showEHICResultDialog(recognitionResult: EuropeanHealthInsuranceCardRecognitionResult) {
        val dialogFragment = EHICResultDialogFragment.newInstance(recognitionResult)
        dialogFragment.show(supportFragmentManager, EHICResultDialogFragment.NAME)
    }

    private fun handleMedicalCertificateResult(resultWrapper: MedicalCertificateRecognitionResult) {


        showMedicalCertificateRecognizerResult(resultWrapper!!)
    }

    private fun showMedicalCertificateRecognizerResult(recognitionResult: MedicalCertificateRecognitionResult) {
        val dialogFragment = MedicalCertificateResultDialogFragment.newInstance(recognitionResult)
        dialogFragment.show(supportFragmentManager, MedicalCertificateResultDialogFragment.NAME)
    }

    private fun handleCheckRecognizerResult(result: CheckRecognitionResult) {
        showCheckRecognizerResult(result)
    }

    private fun showCheckRecognizerResult(recognitionResult: CheckRecognitionResult) {
        val document = recognitionResult.check?.let { Check(it) } // Convert to the document model
        Toast.makeText(this, recognitionResult.toString(), Toast.LENGTH_SHORT).show()
    }

    init {
        mrzDefaultUiResultLauncher =
            registerForActivityResultOk(MRZScannerActivity.ResultContract()) { resultEntity ->
                showMrzDialog(resultEntity.result!!)
            }

        textDataScannerResultLauncher =
            registerForActivityResultOk(TextDataScannerActivity.ResultContract()) { resultEntity ->
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

        licensePlateScannerResultLauncher =
            registerForActivityResultOk(LicensePlateScannerActivity.ResultContract()) { resultEntity ->
                // TODO: Process data from
                // data.getParcelableExtra(LicensePlateScannerActivity.EXTRACTED_FIELDS_EXTRA) as LicensePlateScannerResult
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.license_plate_flow_finished),
                    Toast.LENGTH_LONG
                ).show()
            }

        ehicScannerResultLauncher =
            registerForActivityResultOk(HealthInsuranceCardScannerActivity.ResultContract()) { resultEntity ->
                showEHICResultDialog(resultEntity.result!!)
            }

        genericDocumentRecognizerResultLauncher =
            registerForActivityResultOk(GenericDocumentRecognizerActivity.ResultContract()) { resultEntity ->
                handleGenericDocRecognizerResult(resultEntity.result!!)
            }

        medicalCertificateRecognizerActivityResultLauncher =
            registerForActivityResultOk(MedicalCertificateRecognizerActivity.ResultContract()) { resultEntity ->
                handleMedicalCertificateResult(resultEntity.result!!)
            }

        checkRecognizerResultLauncher =
            registerForActivityResultOk(CheckRecognizerActivity.ResultContract()) { resultEntity ->
                handleCheckRecognizerResult(resultEntity.result!!)
            }
    }
}
