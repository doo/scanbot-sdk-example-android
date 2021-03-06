package io.scanbot.example

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
import io.scanbot.sdk.barcode.entity.BarcodeScanningResult
import io.scanbot.sdk.barcode.entity.FormattedBarcodeDataMapper
import io.scanbot.sdk.camera.CameraPreviewMode
import io.scanbot.sdk.camera.ZoomRange
import io.scanbot.sdk.core.contourdetector.DetectionResult
import io.scanbot.sdk.generictext.GenericTextRecognizer
import io.scanbot.sdk.persistence.Page
import io.scanbot.sdk.process.ImageFilterType
import io.scanbot.sdk.ui.entity.workflow.Workflow
import io.scanbot.sdk.ui.entity.workflow.WorkflowStepResult
import io.scanbot.sdk.ui.result.ResultWrapper
import io.scanbot.sdk.ui.view.barcode.BarcodeScannerActivity
import io.scanbot.sdk.ui.view.barcode.batch.BatchBarcodeScannerActivity
import io.scanbot.sdk.ui.view.barcode.batch.configuration.BatchBarcodeScannerConfiguration
import io.scanbot.sdk.ui.view.barcode.configuration.BarcodeImageGenerationType
import io.scanbot.sdk.ui.view.barcode.configuration.BarcodeScannerConfiguration
import io.scanbot.sdk.ui.view.base.configuration.CameraOrientationMode
import io.scanbot.sdk.ui.view.camera.DocumentScannerActivity
import io.scanbot.sdk.ui.view.camera.configuration.DocumentScannerConfiguration
import io.scanbot.sdk.ui.view.edit.configuration.CroppingConfiguration
import io.scanbot.sdk.ui.view.genericdocument.GenericDocumentRecognizerActivity
import io.scanbot.sdk.ui.view.genericdocument.configuration.GenericDocumentRecognizerConfiguration
import io.scanbot.sdk.ui.view.generictext.TextDataScannerActivity
import io.scanbot.sdk.ui.view.generictext.configuration.TextDataScannerConfiguration
import io.scanbot.sdk.ui.view.generictext.entity.TextDataScannerStep
import io.scanbot.sdk.ui.view.generictext.entity.TextDataScannerStepResult
import io.scanbot.sdk.ui.view.hic.HealthInsuranceCardScannerActivity
import io.scanbot.sdk.ui.view.hic.configuration.HealthInsuranceCardScannerConfiguration
import io.scanbot.sdk.ui.view.idcard.IdCardScannerActivity
import io.scanbot.sdk.ui.view.idcard.configuration.IdCardScannerConfiguration
import io.scanbot.sdk.ui.view.licenseplate.LicensePlateScannerActivity
import io.scanbot.sdk.ui.view.licenseplate.configuration.LicensePlateScannerConfiguration
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

    companion object {
        private const val MRZ_DEFAULT_UI_REQUEST_CODE = 909
        private const val QR_BARCODE_DEFAULT_UI_REQUEST_CODE = 910
        private const val MRZ_SNAP_WORKFLOW_REQUEST_CODE = 912
        private const val MRZ_FRONBACK_SNAP_WORKFLOW_REQUEST_CODE = 913
        private const val DC_SCAN_WORKFLOW_REQUEST_CODE = 914
        private const val BARCODE_AND_DOC_SCAN_WORKFLOW_REQUEST_CODE = 915
        private const val PAYFORM_SCAN_WORKFLOW_REQUEST_CODE = 916
        private const val EHIC_SCAN_REQUEST_CODE = 917
        private const val MULTIPLE_OBJECT_DETECTOR_REQUEST_CODE = 919
        private const val ID_CARD_DEFAULT_UI = 920
        private const val PASSPORT_NFC_MRZ_DEFAULT_UI = 921
        private const val TEXT_DATA_SCANNER_DEFAULT_UI = 922
        private const val LICENSE_PLATE_SCANNER_DEFAULT_UI = 923
        private const val GENERIC_DOCUMENT_RECOGNIZER_DEFAULT_UI = 924
        private const val CROP_DEFAULT_UI_REQUEST_CODE = 9999
        private const val SELECT_PICTURE_FOR_CROPPING_UI_REQUEST = 8888
        private const val SELECT_PICTURE_FOR_DOC_DETECTION_REQUEST = 7777
        private const val CAMERA_DEFAULT_UI_REQUEST_CODE = 1111

        private const val LOG_TAG = "RTU_DEMO_MAIN_ACTIVITY"
    }

    private lateinit var scanbotSDK: ScanbotSDK

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            Log.i(LOG_TAG, "resultCode is not OK when returning from activity with requestCode $requestCode. onActivityResult will do nothing now.")
            return
        }

        if (data == null) {
            Log.w(LOG_TAG, "No data while returning from activity with requestCode $requestCode. onActivityResult will do nothing now.")
            return
        }

        when (requestCode) {
            MRZ_DEFAULT_UI_REQUEST_CODE -> showMrzDialog(data.getParcelableExtra(MRZScannerActivity.EXTRACTED_FIELDS_EXTRA))
            PASSPORT_NFC_MRZ_DEFAULT_UI -> showNfcPassportDialog(data.getParcelableExtra(NfcPassportScannerActivity.EXTRACTED_FIELDS_EXTRA))
            MRZ_SNAP_WORKFLOW_REQUEST_CODE -> showMrzImageWorkflowResult(data.getParcelableExtra(WorkflowScannerActivity.WORKFLOW_EXTRA),
                    data.getParcelableArrayListExtra(WorkflowScannerActivity.WORKFLOW_RESULT_EXTRA))
            MRZ_FRONBACK_SNAP_WORKFLOW_REQUEST_CODE -> showFrontBackMrzImageWorkflowResult(data.getParcelableExtra(WorkflowScannerActivity.WORKFLOW_EXTRA),
                    data.getParcelableArrayListExtra(WorkflowScannerActivity.WORKFLOW_RESULT_EXTRA))
            CROP_DEFAULT_UI_REQUEST_CODE -> {
                val page = data.getParcelableExtra<Page>(io.scanbot.sdk.ui.view.edit.CroppingActivity.EDITED_PAGE_EXTRA)
                page.pageId
            }
            QR_BARCODE_DEFAULT_UI_REQUEST_CODE -> {
                data.getParcelableExtra<BarcodeScanningResult>(BarcodeScannerActivity.SCANNED_BARCODE_EXTRA)
                        ?.let {
                            val imagePath =
                                    data.getStringExtra(BarcodeScannerActivity.SCANNED_BARCODE_IMAGE_PATH_EXTRA)
                            val previewPath =
                                    data.getStringExtra(BarcodeScannerActivity.SCANNED_BARCODE_PREVIEW_FRAME_PATH_EXTRA)

                            BarcodeResultRepository.barcodeResultBundle =
                                    BarcodeResultBundle(it, imagePath, previewPath)

                            val intent = Intent(this, BarcodeResultActivity::class.java)
                            startActivity(intent)
                        }
            }
            BARCODE_AND_DOC_SCAN_WORKFLOW_REQUEST_CODE -> showBarcodeAndDocumentWorkflowResult(data.getParcelableExtra(WorkflowScannerActivity.WORKFLOW_EXTRA),
                    data.getParcelableArrayListExtra(WorkflowScannerActivity.WORKFLOW_RESULT_EXTRA))
            DC_SCAN_WORKFLOW_REQUEST_CODE -> showDCWorkflowResult(data.getParcelableExtra(WorkflowScannerActivity.WORKFLOW_EXTRA),
                    data.getParcelableArrayListExtra(WorkflowScannerActivity.WORKFLOW_RESULT_EXTRA))
            PAYFORM_SCAN_WORKFLOW_REQUEST_CODE -> showPayFormWorkflowResult(data.getParcelableExtra(WorkflowScannerActivity.WORKFLOW_EXTRA),
                    data.getParcelableArrayListExtra(WorkflowScannerActivity.WORKFLOW_RESULT_EXTRA))
            SELECT_PICTURE_FOR_CROPPING_UI_REQUEST -> ProcessImageForCroppingUI(data).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR)
            SELECT_PICTURE_FOR_DOC_DETECTION_REQUEST -> {
                if (!scanbotSDK.licenseInfo.isValid) {
                    showLicenseDialog()
                } else {
                    ProcessImageForAutoDocumentDetection(data).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR)
                }
            }
            MULTIPLE_OBJECT_DETECTOR_REQUEST_CODE -> {
                PageRepository.addPages(MultipleObjectsDetectorActivity.parseActivityResult(data))
                val intent = Intent(this, PagePreviewActivity::class.java)
                startActivity(intent)
            }
            CAMERA_DEFAULT_UI_REQUEST_CODE -> extractPagesToRepoStartPreview(data, DocumentScannerActivity.SNAPPED_PAGE_EXTRA)
            EHIC_SCAN_REQUEST_CODE -> {
                val hicRecognitionResult = data.getParcelableExtra<HealthInsuranceCardRecognitionResult>(HealthInsuranceCardScannerActivity.EXTRACTED_FIELDS_EXTRA)
                showEHICResultDialog(hicRecognitionResult)
            }
            ID_CARD_DEFAULT_UI -> {
                // TODO: Process data from
                // data.getParcelableExtra(IdCardScannerActivity.EXTRACTED_FIELDS_EXTRA) as IdCardScanningResult
                // Can be GermanyPassportCard or GermanyIdCard
                Toast.makeText(this@MainActivity, getString(R.string.id_card_flow_finished), Toast.LENGTH_LONG).show()
            }
            GENERIC_DOCUMENT_RECOGNIZER_DEFAULT_UI -> {
                // Get the ResultWrapper object from the intent
                val resultWrappers
                        = data.getParcelableArrayListExtra<ResultWrapper<GenericDocument>>(GenericDocumentRecognizerActivity.EXTRACTED_FIELDS_EXTRA)

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
            LICENSE_PLATE_SCANNER_DEFAULT_UI -> {
                // TODO: Process data from
                // data.getParcelableExtra(LicensePlateScannerActivity.EXTRACTED_FIELDS_EXTRA) as LicensePlateScannerResult
                Toast.makeText(this@MainActivity, getString(R.string.license_plate_flow_finished), Toast.LENGTH_LONG).show()
            }
            TEXT_DATA_SCANNER_DEFAULT_UI -> {
                val result = data.getParcelableArrayExtra(TextDataScannerActivity.EXTRACTED_FIELDS_EXTRA)
                val textDataScannerStepResult = result!!.first() as TextDataScannerStepResult
                Toast.makeText(this@MainActivity, "Scanned: ${textDataScannerStepResult.text}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun extractPagesToRepoStartPreview(data: Intent, snappedResultsKey: String) {
        val pages = data.getParcelableArrayExtra(snappedResultsKey).toList().map {
            it as Page
        }

        PageRepository.addPages(pages)

        val intent = Intent(this, PagePreviewActivity::class.java)
        startActivity(intent)
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

    private fun showBarcodeAndDocumentWorkflowResult(workflow: Workflow, workflowStepResults: ArrayList<WorkflowStepResult>) {
        val dialogFragment = BarCodeResultDialogFragment.newInstance(workflow, workflowStepResults)
        dialogFragment.show(supportFragmentManager, BarCodeResultDialogFragment.NAME)
    }

    private fun showDCWorkflowResult(workflow: Workflow, workflowStepResults: ArrayList<WorkflowStepResult>) {
        val dialogFragment = DCResultDialogFragment.newInstance(workflow, workflowStepResults)
        dialogFragment.show(supportFragmentManager, DCResultDialogFragment.NAME)
    }

    private fun showPayFormWorkflowResult(workflow: Workflow, workflowStepResults: ArrayList<WorkflowStepResult>) {
        val dialogFragment = PayFormResultDialogFragment.newInstance(workflow, workflowStepResults)
        dialogFragment.show(supportFragmentManager, PayFormResultDialogFragment.NAME)
    }

    private fun showEHICResultDialog(recognitionResult: HealthInsuranceCardRecognitionResult) {
        val dialogFragment = EHICResultDialogFragment.newInstance(recognitionResult)
        dialogFragment.show(supportFragmentManager, EHICResultDialogFragment.NAME)
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

            val intent = DocumentScannerActivity.newIntent(this@MainActivity,
                    cameraConfiguration
            )
            startActivityForResult(intent, CAMERA_DEFAULT_UI_REQUEST_CODE)
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

            val intent = MRZScannerActivity.newIntent(this@MainActivity, mrzCameraConfiguration)
            startActivityForResult(intent, MRZ_DEFAULT_UI_REQUEST_CODE)
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

            val intent = NfcPassportScannerActivity.newIntent(this@MainActivity, nfcPassportConfiguration)
            startActivityForResult(intent, PASSPORT_NFC_MRZ_DEFAULT_UI)
        }

        findViewById<View>(R.id.text_data_scanner_default_ui).setOnClickListener {
            val textDataScannerConfiguration = TextDataScannerConfiguration()

            textDataScannerConfiguration.setTopBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
            textDataScannerConfiguration.setTopBarButtonsColor(ContextCompat.getColor(this, R.color.greyColor))

            val intent = TextDataScannerActivity.newIntent(this@MainActivity, textDataScannerConfiguration,
                    step = TextDataScannerStep(
                            stepTag = "Date",
                            title = "6-digit string",
                            guidanceText = "Scan a 6-digit string which starts with 1 or 2",
                            // For the pattern: # - digits, ? - for any character. Other characters represent themselves
                            pattern = "######",
                            // TODO: set validation string and validation callback which matches the need of the task
                            // In this example we are waiting for a string which starts with 1 or 2, and then 5 more digits
                            validationCallback = object : GenericTextRecognizer.GenericTextValidationCallback {
                                override fun validate(text: String): Boolean {
                                    return text.first() in listOf('1', '2') // TODO: add additional validation for the recognized text
                                }
                            },
                            preferredZoom = 1.6f))

            startActivityForResult(intent, TEXT_DATA_SCANNER_DEFAULT_UI)
        }

        findViewById<View>(R.id.license_plate_scanner_default_ui).setOnClickListener {
            val licensePlateScannerConfiguration = LicensePlateScannerConfiguration()

            licensePlateScannerConfiguration.setTopBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
            licensePlateScannerConfiguration.setTopBarButtonsColor(ContextCompat.getColor(this, R.color.greyColor))

            val intent = LicensePlateScannerActivity.newIntent(this@MainActivity, licensePlateScannerConfiguration)

            startActivityForResult(intent, LICENSE_PLATE_SCANNER_DEFAULT_UI)
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
            val intent = GenericDocumentRecognizerActivity.newIntent(this, genericDocumentConfiguration)
            startActivityForResult(intent, GENERIC_DOCUMENT_RECOGNIZER_DEFAULT_UI)
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

            val intent = BarcodeScannerActivity.newIntent(this@MainActivity, barcodeCameraConfiguration)
            startActivityForResult(intent, QR_BARCODE_DEFAULT_UI_REQUEST_CODE)
        }

        findViewById<View>(R.id.qr_camera_default_ui_with_image).setOnClickListener {
            val barcodeCameraConfiguration = BarcodeScannerConfiguration()

            barcodeCameraConfiguration.setTopBarButtonsColor(ContextCompat.getColor(this, android.R.color.white))
            barcodeCameraConfiguration.setTopBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
            barcodeCameraConfiguration.setFinderTextHint("Please align the QR-/Barcode in the frame above to scan it.")

            barcodeCameraConfiguration.setBarcodeImageGenerationType(BarcodeImageGenerationType.VIDEO_FRAME)

            val intent = BarcodeScannerActivity.newIntent(this@MainActivity, barcodeCameraConfiguration)
            startActivityForResult(intent, QR_BARCODE_DEFAULT_UI_REQUEST_CODE)
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
                    return BarcodeFormattedData(barcodeItem.barcodeFormat.name, barcodeItem.text)
                }
            }

            val intent = BatchBarcodeScannerActivity.newIntent(this@MainActivity, barcodeCameraConfiguration, CustomFormattedBarcodeDataMapper::class.java)
            startActivityForResult(intent, QR_BARCODE_DEFAULT_UI_REQUEST_CODE)
        }

        findViewById<View>(R.id.mrz_image_default_ui).setOnClickListener {
            val workflowScannerConfiguration = WorkflowScannerConfiguration()
            workflowScannerConfiguration.setIgnoreBadAspectRatio(true)
            workflowScannerConfiguration.setTopBarButtonsActiveColor(ContextCompat.getColor(this, android.R.color.white))
            workflowScannerConfiguration.setTopBarButtonsInactiveColor(ContextCompat.getColor(this, android.R.color.white))
            workflowScannerConfiguration.setTopBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
            workflowScannerConfiguration.setBottomBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))

            val intent = WorkflowScannerActivity.newIntent(this@MainActivity,
                    workflowScannerConfiguration,
                    WorkflowFactory.scanMRZAndSnap()
            )
            startActivityForResult(intent, MRZ_SNAP_WORKFLOW_REQUEST_CODE)
        }

        findViewById<View>(R.id.mrz_front_back_image_default_ui).setOnClickListener {
            val workflowScannerConfiguration = WorkflowScannerConfiguration()
            workflowScannerConfiguration.setIgnoreBadAspectRatio(true)
            workflowScannerConfiguration.setTopBarButtonsActiveColor(ContextCompat.getColor(this, android.R.color.white))
            workflowScannerConfiguration.setTopBarButtonsInactiveColor(ContextCompat.getColor(this, android.R.color.white))
            workflowScannerConfiguration.setTopBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
            workflowScannerConfiguration.setBottomBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))

            val intent = WorkflowScannerActivity.newIntent(this@MainActivity,
                    workflowScannerConfiguration,
                    WorkflowFactory.scanMRZAndFrontBackSnap()
            )
            startActivityForResult(intent, MRZ_FRONBACK_SNAP_WORKFLOW_REQUEST_CODE)
        }

        findViewById<View>(R.id.barcode_and_doc_default_ui).setOnClickListener {
            val workflowScannerConfiguration = WorkflowScannerConfiguration()
            workflowScannerConfiguration.setIgnoreBadAspectRatio(true)
            workflowScannerConfiguration.setTopBarButtonsActiveColor(ContextCompat.getColor(this, android.R.color.white))
            workflowScannerConfiguration.setTopBarButtonsInactiveColor(ContextCompat.getColor(this, android.R.color.white))
            workflowScannerConfiguration.setTopBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
            workflowScannerConfiguration.setBottomBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))

            val intent = WorkflowScannerActivity.newIntent(this@MainActivity,
                    workflowScannerConfiguration,
                    WorkflowFactory.barcodeAndDocumentImage()
            )
            startActivityForResult(intent, BARCODE_AND_DOC_SCAN_WORKFLOW_REQUEST_CODE)
        }

        findViewById<View>(R.id.dc_default_ui).setOnClickListener {
            val workflowScannerConfiguration = WorkflowScannerConfiguration()
            workflowScannerConfiguration.setIgnoreBadAspectRatio(true)
            workflowScannerConfiguration.setTopBarButtonsActiveColor(ContextCompat.getColor(this, android.R.color.white))
            workflowScannerConfiguration.setTopBarButtonsInactiveColor(ContextCompat.getColor(this, android.R.color.white))
            workflowScannerConfiguration.setTopBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
            workflowScannerConfiguration.setBottomBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
            workflowScannerConfiguration.setCameraPreviewMode(CameraPreviewMode.FIT_IN)

            val intent = WorkflowScannerActivity.newIntent(this@MainActivity,
                    workflowScannerConfiguration,
                    WorkflowFactory.disabilityCertificate()
            )
            startActivityForResult(intent, DC_SCAN_WORKFLOW_REQUEST_CODE)
        }

        id_card_default_ui.setOnClickListener {
            val idCardScannerConfiguration = IdCardScannerConfiguration()
            idCardScannerConfiguration.setTopBarButtonsInactiveColor(ContextCompat.getColor(this, android.R.color.white))
            idCardScannerConfiguration.setTopBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))

            // It is required to enable saving photos from the Id Card manually
            // Then they will be saved in %SDK_FILES_DIRECTORY%/id_scan_results/
            // The Uris to images can be get from IdCardScanningResult.photo and IdCardScanningResult.signature
            // only if the following options are enabled:
            //
            // idCardScannerConfiguration.setShouldSavePhotoImageInStorage(true)
            // idCardScannerConfiguration.setShouldSaveSignatureImageInStorage(true)

            val intent = IdCardScannerActivity.newIntent(this@MainActivity, idCardScannerConfiguration)
            startActivityForResult(intent, ID_CARD_DEFAULT_UI)
        }

        payform_default_ui.setOnClickListener {
            val workflowScannerConfiguration = WorkflowScannerConfiguration()
            workflowScannerConfiguration.setIgnoreBadAspectRatio(true)
            workflowScannerConfiguration.setTopBarButtonsActiveColor(ContextCompat.getColor(this, android.R.color.white))
            workflowScannerConfiguration.setTopBarButtonsInactiveColor(ContextCompat.getColor(this, android.R.color.white))
            workflowScannerConfiguration.setTopBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
            workflowScannerConfiguration.setBottomBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
            workflowScannerConfiguration.setCameraPreviewMode(CameraPreviewMode.FIT_IN)

            val intent = WorkflowScannerActivity.newIntent(this@MainActivity,
                    workflowScannerConfiguration,
                    WorkflowFactory.payFormWithClassicalDocPolygonDetection()
            )
            startActivityForResult(intent, PAYFORM_SCAN_WORKFLOW_REQUEST_CODE)
        }

        ehic_default_ui.setOnClickListener {
            val ehicScannerConfig = HealthInsuranceCardScannerConfiguration()
            ehicScannerConfig.setTopBarButtonsColor(Color.WHITE)
            // ehicScannerConfig.setTopBarBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
            // ehicScannerConfig.setFinderTextHint("custom text")
            // ...

            val intent = HealthInsuranceCardScannerActivity.newIntent(this@MainActivity, ehicScannerConfig)
            startActivityForResult(intent, EHIC_SCAN_REQUEST_CODE)
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

            val intent = MultipleObjectsDetectorActivity.newIntent(this@MainActivity, config)
            startActivityForResult(intent, MULTIPLE_OBJECT_DETECTOR_REQUEST_CODE)
        }
    }

    private fun importImageWithDetect() {
        val imageIntent = Intent()
        imageIntent.type = "image/*"
        imageIntent.action = Intent.ACTION_GET_CONTENT
        imageIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, false)
        imageIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(Intent.createChooser(imageIntent, getString(R.string.share_title)), SELECT_PICTURE_FOR_DOC_DETECTION_REQUEST)
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

    /**
     * Imports a selected image as original image, creates a new page and opens the Cropping UI on it.
     */
    internal inner class ProcessImageForCroppingUI(private var data: Intent?) : AsyncTask<Void, Void, List<Page>>() {

        override fun onPreExecute() {
            super.onPreExecute()
            progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg params: Void): List<Page> {
            val processGalleryResult = processGalleryResult(data!!)

            val pageFileStorage = io.scanbot.sdk.ScanbotSDK(this@MainActivity).pageFileStorage()

            // create a new Page object with given image as original image:

            return processGalleryResult.map {
                val pageId = pageFileStorage.add(it)
                Page(pageId)
            }
        }

        override fun onPostExecute(pages: List<Page>) {
            progressBar.visibility = View.GONE
            pages.first().also { page ->
                val editPolygonConfiguration = CroppingConfiguration()

                editPolygonConfiguration.setPage(page)

                val intent = io.scanbot.sdk.ui.view.edit.CroppingActivity.newIntent(
                        applicationContext,
                        editPolygonConfiguration
                )
                startActivityForResult(intent, CROP_DEFAULT_UI_REQUEST_CODE)
            }
        }
    }


    /**
     * Imports a selected image as original image and performs auto document detection on it.
     */
    internal inner class ProcessImageForAutoDocumentDetection(private var data: Intent?) : AsyncTask<Void, Void, List<Page>>() {

        override fun onPreExecute() {
            super.onPreExecute()
            progressBar.visibility = View.VISIBLE
            Toast.makeText(this@MainActivity,
                    getString(R.string.importing_and_processing), Toast.LENGTH_LONG).show()
        }

        override fun doInBackground(vararg params: Void): List<Page> {
            val processGalleryResult = processGalleryResult(data!!)

            val pageFileStorage = io.scanbot.sdk.ScanbotSDK(this@MainActivity).pageFileStorage()
            val pageProcessor = io.scanbot.sdk.ScanbotSDK(this@MainActivity).pageProcessor()

            // create a new Page object with given image as original image:

            return processGalleryResult.map {
                val pageId = pageFileStorage.add(it)
                var page = Page(pageId, emptyList(), DetectionResult.OK, ImageFilterType.NONE)

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
