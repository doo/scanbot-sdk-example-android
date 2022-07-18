package io.scanbot.example

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import io.scanbot.check.entity.CheckDocumentLibrary.wrap
import io.scanbot.example.di.ExampleSingletonImpl
import io.scanbot.example.fragments.*
import io.scanbot.example.model.BarcodeResultBundle
import io.scanbot.example.repository.BarcodeResultRepository
import io.scanbot.example.repository.PageRepository
import io.scanbot.genericdocument.entity.DePassport
import io.scanbot.genericdocument.entity.FieldProperties
import io.scanbot.genericdocument.entity.GenericDocument
import io.scanbot.genericdocument.entity.MRZ
import io.scanbot.hicscanner.model.HealthInsuranceCardRecognitionResult
import io.scanbot.mrzscanner.model.MRZRecognitionResult
import io.scanbot.sap.Status
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.barcode.entity.BarcodeFormattedData
import io.scanbot.sdk.barcode.entity.BarcodeItem
import io.scanbot.sdk.barcode.entity.FormattedBarcodeDataMapper
import io.scanbot.sdk.camera.CameraPreviewMode
import io.scanbot.sdk.camera.ZoomRange
import io.scanbot.sdk.check.entity.CheckRecognizerResult
import io.scanbot.sdk.core.contourdetector.DetectionResult
import io.scanbot.sdk.core.contourdetector.DetectionStatus
import io.scanbot.sdk.mcrecognizer.entity.MedicalCertificateRecognizerResult
import io.scanbot.sdk.persistence.Page
import io.scanbot.sdk.process.ImageFilterType
import io.scanbot.sdk.ui.entity.workflow.Workflow
import io.scanbot.sdk.ui.entity.workflow.WorkflowStepResult
import io.scanbot.sdk.ui.registerForActivityResultOk
import io.scanbot.sdk.ui.result.ResultWrapper
import io.scanbot.sdk.ui.view.barcode.BarcodeScannerActivity
import io.scanbot.sdk.ui.view.barcode.batch.BatchBarcodeScannerActivity
import io.scanbot.sdk.ui.view.barcode.batch.configuration.BatchBarcodeScannerConfiguration
import io.scanbot.sdk.ui.view.barcode.configuration.BarcodeImageGenerationType
import io.scanbot.sdk.ui.view.barcode.configuration.BarcodeScannerConfiguration
import io.scanbot.sdk.ui.view.base.configuration.CameraOrientationMode
import io.scanbot.sdk.ui.view.camera.DocumentScannerActivity
import io.scanbot.sdk.ui.view.camera.configuration.DocumentScannerConfiguration
import io.scanbot.sdk.ui.view.check.CheckRecognizerActivity
import io.scanbot.sdk.ui.view.check.configuration.CheckRecognizerConfiguration
import io.scanbot.sdk.ui.view.edit.CroppingActivity
import io.scanbot.sdk.ui.view.edit.configuration.CroppingConfiguration
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
import io.scanbot.sdk.ui.view.multiple_objects.MultipleObjectsDetectorActivity
import io.scanbot.sdk.ui.view.multiple_objects.configuration.MultipleObjectsDetectorConfiguration
import io.scanbot.sdk.ui.view.nfc.NfcPassportScannerActivity
import io.scanbot.sdk.ui.view.nfc.PassportPhotoSaveCallback
import io.scanbot.sdk.ui.view.nfc.configuration.NfcPassportConfiguration
import io.scanbot.sdk.ui.view.nfc.entity.NfcPassportScanningResult
import io.scanbot.sdk.ui.view.workflow.WorkflowScannerActivity
import io.scanbot.sdk.ui.view.workflow.configuration.WorkflowScannerConfiguration
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var scanbotSDK: ScanbotSDK

    private val mrzDefaultUiResultLauncher: ActivityResultLauncher<MRZScannerConfiguration>
    private val textDataScannerResultLauncher: ActivityResultLauncher<TextDataScannerActivity.InputParams>
    private val licensePlateScannerResultLauncher: ActivityResultLauncher<LicensePlateScannerConfiguration>
    private val nfcPassportScannerResultLauncher: ActivityResultLauncher<NfcPassportConfiguration>
    private val mrzSnapWorkflowResultLauncher: ActivityResultLauncher<WorkflowScannerActivity.InputParams>
    private val mrzFrontBackWorkflowResultLauncher: ActivityResultLauncher<WorkflowScannerActivity.InputParams>
    private val cropResultLauncher: ActivityResultLauncher<CroppingConfiguration>
    private val barcodeResultLauncher: ActivityResultLauncher<BarcodeScannerConfiguration>
    private val batchBarcodeResultLauncher: ActivityResultLauncher<BatchBarcodeScannerActivity.InputParams>
    private val barcodeAndDocWorkflowResultLauncher: ActivityResultLauncher<WorkflowScannerActivity.InputParams>
    private val dcWorkflowResultLauncher: ActivityResultLauncher<WorkflowScannerActivity.InputParams>
    private val medicalCertificateRecognizerActivityResultLauncher: ActivityResultLauncher<MedicalCertificateRecognizerConfiguration>
    private val payformWorkflowResultLauncher: ActivityResultLauncher<WorkflowScannerActivity.InputParams>
    private val selectPictureFromGalleryResultLauncher: ActivityResultLauncher<Intent>
    private val multipleObjectsDetectorResultLauncher: ActivityResultLauncher<MultipleObjectsDetectorConfiguration>
    private val documentScannerResultLauncher: ActivityResultLauncher<DocumentScannerConfiguration>
    private val ehicScannerResultLauncher: ActivityResultLauncher<HealthInsuranceCardScannerConfiguration>
    private val genericDocumentRecognizerResultLauncher: ActivityResultLauncher<GenericDocumentRecognizerConfiguration>
    private val checkRecognizerResultLauncher: ActivityResultLauncher<CheckRecognizerConfiguration>

    private fun handleGeneriDocRecognizerResult(resultWrappers: List<ResultWrapper<GenericDocument>>) {
        // For simplicity we will take only the first document
        val firstResultWrapper = resultWrappers.first()

        // Get the ResultRepository from the ScanbotSDK instance
        // scanbotSDK was created in onCreate via ScanbotSDK(context)
        val resultRepository = scanbotSDK.resultRepositoryForClass(firstResultWrapper.clazz)

        // Receive an instance of GenericDocument class from the repository
        // This call will also remove the result from the repository (to make the memory usage less)
        val genericDocument = resultRepository.getResultAndErase(firstResultWrapper.resultId)

        Toast.makeText(this, genericDocument?.fields?.map { "${it.type.name} = ${it.value?.text}" }.toString(), Toast.LENGTH_LONG).show()
    }

    private fun showLicenseDialog() {
        if (supportFragmentManager.findFragmentByTag(ErrorFragment.NAME) == null) {
            val dialogFragment = ErrorFragment.newInstance()
            dialogFragment.show(supportFragmentManager, ErrorFragment.NAME)
        }
    }

    private fun showMrzDialog(mrzRecognitionResult: MRZRecognitionResult) {
        val dialogFragment = MRZDialogFragment.newInstance(mrzRecognitionResult)
        dialogFragment.show(supportFragmentManager, MRZDialogFragment.NAME)
    }

    private fun showNfcPassportDialog(nfcPassportScanningResult: NfcPassportScanningResult) {
        val dialogFragment = NfcPassportResultDialogFragment.newInstance(nfcPassportScanningResult)
        dialogFragment.show(supportFragmentManager, NfcPassportResultDialogFragment.NAME)
    }

    private fun showMrzImageWorkflowResult(workflow: Workflow, workflowStepResults: ArrayList<WorkflowStepResult>) {
        val dialogFragment = MRZImageResultDialogFragment.newInstance(workflow, workflowStepResults)
        dialogFragment.show(supportFragmentManager, MRZImageResultDialogFragment.NAME)
    }

    private fun showFrontBackMrzImageWorkflowResult(workflow: Workflow, workflowStepResults: ArrayList<WorkflowStepResult>) {
        val dialogFragment = MRZFrontBackImageResultDialogFragment.newInstance(workflow, workflowStepResults)
        dialogFragment.show(supportFragmentManager, MRZFrontBackImageResultDialogFragment.NAME)
    }

    private fun showBarcodeAndDocumentWorkflowResult(workflow: Workflow, workflowStepResults: List<WorkflowStepResult>) {
        val dialogFragment = BarCodeResultDialogFragment.newInstance(workflow, workflowStepResults)
        dialogFragment.show(supportFragmentManager, BarCodeResultDialogFragment.NAME)
    }

    private fun showDCWorkflowResult(workflow: Workflow, workflowStepResults: List<WorkflowStepResult>) {
        val dialogFragment = MedicalCertificateResultDialogFragment.newInstance(workflow, workflowStepResults)
        dialogFragment.show(supportFragmentManager, MedicalCertificateResultDialogFragment.NAME)
    }

    private fun showPayFormWorkflowResult(workflow: Workflow, workflowStepResults: List<WorkflowStepResult>) {
        val dialogFragment = PayFormResultDialogFragment.newInstance(workflow, workflowStepResults)
        dialogFragment.show(supportFragmentManager, PayFormResultDialogFragment.NAME)
    }

    private fun showEHICResultDialog(recognitionResult: HealthInsuranceCardRecognitionResult) {
        val dialogFragment = EHICResultDialogFragment.newInstance(recognitionResult)
        dialogFragment.show(supportFragmentManager, EHICResultDialogFragment.NAME)
    }

    private fun handleMedicalCertificateResult(resultWrapper: ResultWrapper<MedicalCertificateRecognizerResult>) {
        // Get the ResultRepository from the ScanbotSDK instance
        // scanbotSDK was created in onCreate via ScanbotSDK(context)
        val resultRepository = scanbotSDK.resultRepositoryForClass(resultWrapper.clazz)

        // Receive an instance of MedicalCertificateRecognizerResult class from the repository
        // This call will also remove the result from the repository (to make the memory usage less)
        val medicalCertificateRecognizerResult = resultRepository.getResultAndErase(resultWrapper.resultId)

        showMedicalCertificateRecognizerResult(medicalCertificateRecognizerResult!!)
    }

    private fun showMedicalCertificateRecognizerResult(recognitionResult: MedicalCertificateRecognizerResult) {
        val dialogFragment = MedicalCertificateResultDialogFragment.newInstance(recognitionResult)
        dialogFragment.show(supportFragmentManager, MedicalCertificateResultDialogFragment.NAME)
    }

    private fun handleCheckRecognizerResult(resultWrapper: ResultWrapper<CheckRecognizerResult>) {
        // Get the ResultRepository from the ScanbotSDK instance
        // scanbotSDK was created in onCreate via ScanbotSDK(context)
        val resultRepository = scanbotSDK.resultRepositoryForClass(resultWrapper.clazz)

        // Receive an instance of CheckRecognizerResult class from the repository
        // This call will also remove the result from the repository (to make the memory usage less)
        val result = resultRepository.getResultAndErase(resultWrapper.resultId)

        showCheckRecognizerResult(result!!)
    }

    private fun showCheckRecognizerResult(recognitionResult: CheckRecognizerResult) {
        recognitionResult.check?.wrap()
        Toast.makeText(this, recognitionResult.toString(), Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        if (!scanbotSDK.licenseInfo.isValid) {
            showLicenseDialog()
        }
        warning_view.visibility = if (scanbotSDK.licenseInfo.status != Status.StatusOkay) View.VISIBLE else View.GONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initDependencies()
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.doc_detection_on_image_btn).setOnClickListener {
            // select an image from photo library and run document detection on it:
            importImageWithDetect()
        }

        findViewById<View>(R.id.camera_default_ui).setOnClickListener {
            // Customize text resources, behavior and UI:
            val cameraConfiguration = DocumentScannerConfiguration()
            cameraConfiguration.setCameraPreviewMode(CameraPreviewMode.FIT_IN)
            cameraConfiguration.setIgnoreBadAspectRatio(true)
            cameraConfiguration.setBottomBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
            cameraConfiguration.setBottomBarButtonsColor(ContextCompat.getColor(this, R.color.greyColor))
            cameraConfiguration.setTopBarButtonsActiveColor(ContextCompat.getColor(this, android.R.color.white))
            cameraConfiguration.setCameraBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
            cameraConfiguration.setUserGuidanceBackgroundColor(ContextCompat.getColor(this, android.R.color.black))
            cameraConfiguration.setUserGuidanceTextColor(ContextCompat.getColor(this, android.R.color.white))
            cameraConfiguration.setMultiPageEnabled(true)
            cameraConfiguration.setAutoSnappingSensitivity(0.75f)
            cameraConfiguration.setPageCounterButtonTitle("%d Page(s)")
            cameraConfiguration.setTextHintOK("Don't move.\nCapturing document...")
            // see further customization configs ...

            documentScannerResultLauncher.launch(cameraConfiguration)
        }

        findViewById<View>(R.id.page_preview_activity).setOnClickListener {
            val intent = Intent(this@MainActivity, PagePreviewActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.mrz_camera_default_ui).setOnClickListener {
            val mrzCameraConfiguration = MRZScannerConfiguration()

            mrzCameraConfiguration.setTopBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
            mrzCameraConfiguration.setTopBarButtonsColor(ContextCompat.getColor(this, R.color.greyColor))
            mrzCameraConfiguration.setSuccessBeepEnabled(false)

            mrzDefaultUiResultLauncher.launch(mrzCameraConfiguration)
        }

        findViewById<View>(R.id.nfc_passport_default_ui).setOnClickListener {
            val nfcPassportConfiguration = NfcPassportConfiguration()

            nfcPassportConfiguration.setTopBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
            nfcPassportConfiguration.setTopBarButtonsColor(ContextCompat.getColor(this, R.color.greyColor))
            nfcPassportConfiguration.setSuccessBeepEnabled(false)

            // TODO: if you need to load an image from the NFC chip enable line below
            // nfcPassportConfiguration.setShouldSavePhotoImageInStorage(true)

            // if for some reason (e.g. security) you need to retrieve passport photo from NFC chip
            // without getting it stored on device disk, you can enable the following configuration
            class PhotoSaveCallback : PassportPhotoSaveCallback {
                // NOTE: callback implementation class must be static (in case of Java)
                // or non-inner (in case of Kotlin), have default (empty) constructor
                // and must not touch fields or methods of enclosing class/method
                override fun onImageRetrieved(photo: Bitmap?) {
                    // TODO: use photo from this callback
                }
            }
            nfcPassportConfiguration.setPassportPhotoSaveCallback(PhotoSaveCallback::class.java)

            nfcPassportScannerResultLauncher.launch(nfcPassportConfiguration)
        }

        findViewById<View>(R.id.text_data_scanner_default_ui).setOnClickListener {
            val textDataScannerConfiguration = TextDataScannerConfiguration()

            textDataScannerConfiguration.setTopBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
            textDataScannerConfiguration.setTopBarButtonsColor(ContextCompat.getColor(this, R.color.greyColor))

            val step = TextDataScannerStep(
                    stepTag = "Date",
                    title = "6-digit string",
                    guidanceText = "Scan a 6-digit string which starts with 1 or 2",
                    // For the pattern: # - digits, ? - for any character. Other characters represent themselves
                    pattern = "######",
                    // TODO: set validation string and validation callback which matches the need of the task
                    // In this example we are waiting for a string which starts with 1 or 2, and then 5 more digits
                    validationCallback = { text ->
                        text.first() in listOf('1', '2') // TODO: add additional validation for the recognized text
                    },
                    preferredZoom = 1.6f)
            val rtuInput = TextDataScannerActivity.InputParams(
                    textDataScannerConfiguration, step
            )
            textDataScannerResultLauncher.launch(rtuInput)
        }

        findViewById<View>(R.id.license_plate_scanner_default_ui).setOnClickListener {
            val licensePlateScannerConfiguration = LicensePlateScannerConfiguration()

            licensePlateScannerConfiguration.setTopBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
            licensePlateScannerConfiguration.setTopBarButtonsColor(ContextCompat.getColor(this, R.color.greyColor))

            licensePlateScannerResultLauncher.launch(licensePlateScannerConfiguration)
        }

        findViewById<View>(R.id.generic_document_default_ui).setOnClickListener {
            val genericDocumentConfiguration = GenericDocumentRecognizerConfiguration()
            genericDocumentConfiguration.setTopBarButtonsInactiveColor(ContextCompat.getColor(this, android.R.color.white))
            genericDocumentConfiguration.setTopBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
            genericDocumentConfiguration.setFieldsDisplayConfiguration(
                hashMapOf(
                    DePassport.NormalizedFieldNames.PHOTO to FieldProperties(
                        "My passport photo",
                        FieldProperties.DisplayState.AlwaysVisible
                    ),
                    MRZ.NormalizedFieldNames.CHECK_DIGIT to FieldProperties(
                        "Check digit",
                        FieldProperties.DisplayState.AlwaysVisible
                    )
                )
            )
            genericDocumentRecognizerResultLauncher.launch(genericDocumentConfiguration)
        }

        findViewById<View>(R.id.qr_camera_default_ui).setOnClickListener {
            val barcodeCameraConfiguration = BarcodeScannerConfiguration()

            barcodeCameraConfiguration.setTopBarButtonsColor(ContextCompat.getColor(this, android.R.color.white))
            barcodeCameraConfiguration.setTopBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
            barcodeCameraConfiguration.setFinderTextHint("Please align the QR-/Barcode in the frame above to scan it.")

            // Default value is 0.
            barcodeCameraConfiguration.setCameraZoomFactor(0.1f)
            // Default value is ZoomRange(0, 1).
            barcodeCameraConfiguration.setCameraZoomRange(ZoomRange(0.1f, 1f))
            barcodeCameraConfiguration.setBarcodeImageGenerationType(BarcodeImageGenerationType.NONE)

            barcodeResultLauncher.launch(barcodeCameraConfiguration)
        }

        findViewById<View>(R.id.qr_camera_default_ui_with_image).setOnClickListener {
            val barcodeCameraConfiguration = BarcodeScannerConfiguration()

            barcodeCameraConfiguration.setTopBarButtonsColor(ContextCompat.getColor(this, android.R.color.white))
            barcodeCameraConfiguration.setTopBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
            barcodeCameraConfiguration.setFinderTextHint("Please align the QR-/Barcode in the frame above to scan it.")
            barcodeCameraConfiguration.setBarcodeImageGenerationType(BarcodeImageGenerationType.VIDEO_FRAME)

            barcodeResultLauncher.launch(barcodeCameraConfiguration)
        }

        findViewById<View>(R.id.qr_camera_batch_mode).setOnClickListener {
            val barcodeCameraConfiguration = BatchBarcodeScannerConfiguration()

            barcodeCameraConfiguration.setTopBarButtonsColor(ContextCompat.getColor(this, android.R.color.white))
            barcodeCameraConfiguration.setTopBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
            barcodeCameraConfiguration.setFinderTextHint("Please align the QR-/Barcode in the frame above to scan it.")

            barcodeCameraConfiguration.setDetailsBackgroundColor(ContextCompat.getColor(this, android.R.color.white))
            barcodeCameraConfiguration.setDetailsActionColor(ContextCompat.getColor(this, android.R.color.white))
            barcodeCameraConfiguration.setDetailsBackgroundColor(ContextCompat.getColor(this, R.color.sheetColor))
            barcodeCameraConfiguration.setDetailsPrimaryColor(ContextCompat.getColor(this, android.R.color.white))
            barcodeCameraConfiguration.setBarcodesCountTextColor(ContextCompat.getColor(this, android.R.color.white))
            barcodeCameraConfiguration.setOrientationLockMode(CameraOrientationMode.PORTRAIT)

            class CustomFormattedBarcodeDataMapper : FormattedBarcodeDataMapper {
                // NOTE: callback implementation class must be static (in case of Java)
                // or non-inner (in case of Kotlin), have default (empty) constructor
                // and must not touch fields or methods of enclosing class/method
                override fun decodeFormattedData(barcodeItem: BarcodeItem): BarcodeFormattedData {
                    // TODO: use barcodeItem appropriately here as needed
                    return BarcodeFormattedData(barcodeItem.barcodeFormat.name, barcodeItem.textWithExtension)
                }
            }

            val rtuInput = BatchBarcodeScannerActivity.InputParams(
                    barcodeCameraConfiguration,
                    CustomFormattedBarcodeDataMapper::class.java
            )
            batchBarcodeResultLauncher.launch(rtuInput)
        }

        findViewById<View>(R.id.mrz_image_default_ui).setOnClickListener {
            val workflowScannerConfiguration = WorkflowScannerConfiguration()
            workflowScannerConfiguration.setIgnoreBadAspectRatio(true)
            workflowScannerConfiguration.setTopBarButtonsActiveColor(ContextCompat.getColor(this, android.R.color.white))
            workflowScannerConfiguration.setTopBarButtonsInactiveColor(ContextCompat.getColor(this, android.R.color.white))
            workflowScannerConfiguration.setTopBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
            workflowScannerConfiguration.setBottomBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))

            val rtuInput = WorkflowScannerActivity.InputParams(
                    workflowScannerConfiguration, WorkflowFactory.scanMRZAndSnap()
            )
            mrzSnapWorkflowResultLauncher.launch(rtuInput)
        }

        findViewById<View>(R.id.mrz_front_back_image_default_ui).setOnClickListener {
            val workflowScannerConfiguration = WorkflowScannerConfiguration()
            workflowScannerConfiguration.setIgnoreBadAspectRatio(true)
            workflowScannerConfiguration.setTopBarButtonsActiveColor(ContextCompat.getColor(this, android.R.color.white))
            workflowScannerConfiguration.setTopBarButtonsInactiveColor(ContextCompat.getColor(this, android.R.color.white))
            workflowScannerConfiguration.setTopBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
            workflowScannerConfiguration.setBottomBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))

            val rtuInput = WorkflowScannerActivity.InputParams(
                    workflowScannerConfiguration, WorkflowFactory.scanMRZAndFrontBackSnap()
            )
            mrzFrontBackWorkflowResultLauncher.launch(rtuInput)
        }

        findViewById<View>(R.id.barcode_and_doc_default_ui).setOnClickListener {
            val workflowScannerConfiguration = WorkflowScannerConfiguration()
            workflowScannerConfiguration.setIgnoreBadAspectRatio(true)
            workflowScannerConfiguration.setTopBarButtonsActiveColor(ContextCompat.getColor(this, android.R.color.white))
            workflowScannerConfiguration.setTopBarButtonsInactiveColor(ContextCompat.getColor(this, android.R.color.white))
            workflowScannerConfiguration.setTopBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
            workflowScannerConfiguration.setBottomBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))

            val rtuInput = WorkflowScannerActivity.InputParams(workflowScannerConfiguration, WorkflowFactory.barcodeAndDocumentImage())
            barcodeAndDocWorkflowResultLauncher.launch(rtuInput)
        }

        findViewById<View>(R.id.mc_workflow_ui).setOnClickListener {
            val workflowScannerConfiguration = WorkflowScannerConfiguration()
            workflowScannerConfiguration.setIgnoreBadAspectRatio(true)
            workflowScannerConfiguration.setTopBarButtonsActiveColor(ContextCompat.getColor(this, android.R.color.white))
            workflowScannerConfiguration.setTopBarButtonsInactiveColor(ContextCompat.getColor(this, android.R.color.white))
            workflowScannerConfiguration.setTopBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
            workflowScannerConfiguration.setBottomBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
            workflowScannerConfiguration.setCameraPreviewMode(CameraPreviewMode.FIT_IN)

            val rtuInput = WorkflowScannerActivity.InputParams(workflowScannerConfiguration, WorkflowFactory.medicalCertificate())
            dcWorkflowResultLauncher.launch(rtuInput)
        }

        payform_default_ui.setOnClickListener {
            val workflowScannerConfiguration = WorkflowScannerConfiguration()
            workflowScannerConfiguration.setIgnoreBadAspectRatio(true)
            workflowScannerConfiguration.setTopBarButtonsActiveColor(ContextCompat.getColor(this, android.R.color.white))
            workflowScannerConfiguration.setTopBarButtonsInactiveColor(ContextCompat.getColor(this, android.R.color.white))
            workflowScannerConfiguration.setTopBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
            workflowScannerConfiguration.setBottomBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
            workflowScannerConfiguration.setCameraPreviewMode(CameraPreviewMode.FIT_IN)

            val rtuInput = WorkflowScannerActivity.InputParams(workflowScannerConfiguration, WorkflowFactory.payFormWithClassicalDocPolygonDetection())
            payformWorkflowResultLauncher.launch(rtuInput)
        }

        ehic_default_ui.setOnClickListener {
            val ehicScannerConfig = HealthInsuranceCardScannerConfiguration()
            ehicScannerConfig.setTopBarButtonsColor(Color.WHITE)
            // ehicScannerConfig.setTopBarBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
            // ehicScannerConfig.setFinderTextHint("custom text")
            // ...

            ehicScannerResultLauncher.launch(ehicScannerConfig)
        }

        multiple_object_detector_ui.setOnClickListener {
            val config = MultipleObjectsDetectorConfiguration().apply {
                setCameraPreviewMode(CameraPreviewMode.FIT_IN)
                setBottomBarBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.colorPrimaryDark))
                setBottomBarButtonsColor(ContextCompat.getColor(this@MainActivity, R.color.greyColor))
                setTopBarButtonsActiveColor(ContextCompat.getColor(this@MainActivity, android.R.color.white))
                setCameraBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.colorPrimary))
                // aspect ratio below: from 2x1 (landscape-oriented object) to 1x2 (portrait)
                setAspectRatioRange(MultipleObjectsDetectorConfiguration.AspectRatioRange(0.5F, 2.0F))
                setCancelButtonTitle("Abort")
                setBatchButtonTitle("Batch")
                setBatchModeEnabled(true)
                setPageCounterButtonTitle("%d Page(s)")
            }

            multipleObjectsDetectorResultLauncher.launch(config)
        }

        check_recognizer_ui.setOnClickListener {
            val config = CheckRecognizerConfiguration().apply {
                setTopBarBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.colorPrimaryDark))
                setTopBarButtonsColor(ContextCompat.getColor(this@MainActivity, R.color.greyColor))
            }

            checkRecognizerResultLauncher.launch(config)
        }

        mc_scanner_ui.setOnClickListener {
            val config = MedicalCertificateRecognizerConfiguration().apply {
                setTopBarBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.colorPrimaryDark))
                setTopBarButtonsColor(ContextCompat.getColor(this@MainActivity, R.color.greyColor))
            }

            medicalCertificateRecognizerActivityResultLauncher.launch(config)
        }
    }

    private fun importImageWithDetect() {
        val imageIntent = Intent()
        imageIntent.type = "image/*"
        imageIntent.action = Intent.ACTION_GET_CONTENT
        imageIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, false)
        imageIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        selectPictureFromGalleryResultLauncher.launch(Intent.createChooser(imageIntent, getString(R.string.share_title)))
    }

    private fun processGalleryResult(data: Intent): List<Bitmap> {
        val imageUris = data.data?.let { listOf(it) }
                ?: (0 until data.clipData!!.itemCount).toList().map { data.clipData!!.getItemAt(it).uri }

        return imageUris.mapNotNull {
            var bitmap: Bitmap? = null
            try {
                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
            } catch (e: IOException) {
            }
            bitmap
        }
    }

    private fun initDependencies() {
        scanbotSDK = ScanbotSDK(this)
    }

    init {
        mrzDefaultUiResultLauncher =
                registerForActivityResultOk(MRZScannerActivity.ResultContract()) { resultEntity ->
                    showMrzDialog(resultEntity.result!!)
                }
        textDataScannerResultLauncher =
                registerForActivityResultOk(TextDataScannerActivity.ResultContract()) { resultEntity ->
                    val textDataScannerStepResult = resultEntity.result!!.first()
                    Toast.makeText(this@MainActivity, "Scanned: ${textDataScannerStepResult.text}", Toast.LENGTH_LONG).show()
                }

        licensePlateScannerResultLauncher =
                registerForActivityResultOk(LicensePlateScannerActivity.ResultContract()) { resultEntity ->
                    // TODO: Process data from
                    // data.getParcelableExtra(LicensePlateScannerActivity.EXTRACTED_FIELDS_EXTRA) as LicensePlateScannerResult
                    Toast.makeText(this@MainActivity, getString(R.string.license_plate_flow_finished), Toast.LENGTH_LONG).show()
                }

        nfcPassportScannerResultLauncher =
                registerForActivityResultOk(NfcPassportScannerActivity.ResultContract()) { resultEntity ->
                    showNfcPassportDialog(resultEntity.result!!)
                }

        mrzSnapWorkflowResultLauncher =
                registerForActivityResultOk(WorkflowScannerActivity.ResultContract()) { resultEntity ->
                    showMrzImageWorkflowResult(resultEntity.workflow!!, ArrayList(resultEntity.result!!))
                }

        mrzFrontBackWorkflowResultLauncher =
                registerForActivityResultOk(WorkflowScannerActivity.ResultContract()) { resultEntity ->
                    showFrontBackMrzImageWorkflowResult(resultEntity.workflow!!,
                            ArrayList(resultEntity.result!!))
                }

        cropResultLauncher =
                registerForActivityResultOk(CroppingActivity.ResultContract()) { resultEntity ->
                    PageRepository.addPage(resultEntity.result!!)

                    val intent = Intent(this, PagePreviewActivity::class.java)
                    startActivity(intent)
                }

        barcodeResultLauncher =
                registerForActivityResultOk(BarcodeScannerActivity.ResultContract()) { resultEntity ->
                    BarcodeResultRepository.barcodeResultBundle = BarcodeResultBundle(
                            resultEntity.result!!,
                            resultEntity.barcodeImagePath,
                            resultEntity.barcodePreviewFramePath
                    )

                    val intent = Intent(this, BarcodeResultActivity::class.java)
                    startActivity(intent)
                }

        batchBarcodeResultLauncher =
                registerForActivityResultOk(BatchBarcodeScannerActivity.ResultContract()) { resultEntity ->
                    BarcodeResultRepository.barcodeResultBundle = BarcodeResultBundle(resultEntity.result!!)

                    val intent = Intent(this, BarcodeResultActivity::class.java)
                    startActivity(intent)
                }

        barcodeAndDocWorkflowResultLauncher =
                registerForActivityResultOk(WorkflowScannerActivity.ResultContract()) { resultEntity ->
                    showBarcodeAndDocumentWorkflowResult(resultEntity.workflow!!, resultEntity.result!!)
                }

        dcWorkflowResultLauncher =
                registerForActivityResultOk(WorkflowScannerActivity.ResultContract()) { resultEntity ->
                    showDCWorkflowResult(resultEntity.workflow!!, resultEntity.result!!)
                }

        payformWorkflowResultLauncher =
                registerForActivityResultOk(WorkflowScannerActivity.ResultContract()) { resultEntity ->
                    showPayFormWorkflowResult(resultEntity.workflow!!, resultEntity.result!!)
                }

        selectPictureFromGalleryResultLauncher =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
                    if (activityResult.resultCode == Activity.RESULT_OK && activityResult.data != null) {
                        if (!scanbotSDK.licenseInfo.isValid) {
                            showLicenseDialog()
                        } else {
                            ProcessImageForAutoDocumentDetection(activityResult.data!!).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR)
                            // If you wish to crop selected document instead - switch to commented code below
//                            ProcessImageForCroppingUI(activityResult.data!!).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR)
                        }
                    }
                }

        multipleObjectsDetectorResultLauncher =
                registerForActivityResultOk(MultipleObjectsDetectorActivity.ResultContract()) { resultEntity ->
                    PageRepository.addPages(resultEntity.result!!)
                    val intent = Intent(this, PagePreviewActivity::class.java)
                    startActivity(intent)
                }

        documentScannerResultLauncher =
                registerForActivityResultOk(DocumentScannerActivity.ResultContract()) { resultEntity ->
                    PageRepository.addPages(resultEntity.result!!)

                    val intent = Intent(this, PagePreviewActivity::class.java)
                    startActivity(intent)
                }

        ehicScannerResultLauncher =
                registerForActivityResultOk(HealthInsuranceCardScannerActivity.ResultContract()) { resultEntity ->
                    showEHICResultDialog(resultEntity.result!!)
                }

        genericDocumentRecognizerResultLauncher =
                registerForActivityResultOk(GenericDocumentRecognizerActivity.ResultContract()) { resultEntity ->
                    handleGeneriDocRecognizerResult(resultEntity.result!!)
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

    /** Imports a selected image as original image, creates a new page and opens the Cropping UI on it. */
    internal inner class ProcessImageForCroppingUI(private var data: Intent) : AsyncTask<Void, Void, List<Page>>() {

        override fun onPreExecute() {
            super.onPreExecute()
            progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg params: Void): List<Page> {
            val processGalleryResult = processGalleryResult(data)

            val pageFileStorage = ExampleSingletonImpl(this@MainActivity).pageFileStorageInstance()

            // create a new Page object with given image as original image:

            return processGalleryResult.map {
                val pageId = pageFileStorage.add(it)
                Page(pageId)
            }
        }

        override fun onPostExecute(pages: List<Page>) {
            progressBar.visibility = View.GONE
            pages.first().also { page ->
                val editPolygonConfiguration = CroppingConfiguration(page)

                cropResultLauncher.launch(editPolygonConfiguration)
            }
        }
    }

    /** Imports a selected image as original image and performs auto document detection on it. */
    internal inner class ProcessImageForAutoDocumentDetection(private var data: Intent) : AsyncTask<Void, Void, List<Page>>() {

        override fun onPreExecute() {
            super.onPreExecute()
            progressBar.visibility = View.VISIBLE
            Toast.makeText(this@MainActivity,
                    getString(R.string.importing_and_processing), Toast.LENGTH_LONG).show()
        }

        override fun doInBackground(vararg params: Void): List<Page> {
            val processGalleryResult = processGalleryResult(data)

            val singleton = ExampleSingletonImpl(this@MainActivity)
            val pageFileStorage = singleton.pageFileStorageInstance()
            val pageProcessor = singleton.pageProcessorInstance()

            // create a new Page object with given image as original image:

            return processGalleryResult.map {
                val pageId = pageFileStorage.add(it)
                var page = Page(pageId, emptyList(), DetectionStatus.OK, ImageFilterType.NONE)

                // run auto document detection on it:
                page = pageProcessor.detectDocument(page)

                PageRepository.addPage(page)
                page
            }
        }

        override fun onPostExecute(pages: List<Page>) {
            progressBar.visibility = View.GONE
            val intent = Intent(this@MainActivity, PagePreviewActivity::class.java)
            startActivity(intent)
        }
    }
}
