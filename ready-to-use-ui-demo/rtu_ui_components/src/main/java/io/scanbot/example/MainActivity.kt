package io.scanbot.example

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import io.scanbot.example.fragments.*
import io.scanbot.example.model.BarcodeResultBundle
import io.scanbot.example.repository.BarcodeResultRepository
import io.scanbot.example.repository.PageRepository
import io.scanbot.hicscanner.model.HealthInsuranceCardRecognitionResult
import io.scanbot.mrzscanner.model.MRZRecognitionResult
import io.scanbot.sap.Status
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.barcode.entity.BarcodeScanningResult
import io.scanbot.sdk.persistence.Page
import io.scanbot.sdk.process.ImageFilterType
import io.scanbot.sdk.ui.entity.workflow.Workflow
import io.scanbot.sdk.ui.entity.workflow.WorkflowStepResult
import io.scanbot.sdk.ui.view.barcode.BarcodeScannerActivity
import io.scanbot.sdk.ui.view.barcode.BaseBarcodeScannerActivity
import io.scanbot.sdk.ui.view.barcode.configuration.BarcodeImageGenerationType
import io.scanbot.sdk.ui.view.barcode.configuration.BarcodeScannerConfiguration
import io.scanbot.sdk.ui.view.camera.DocumentScannerActivity
import io.scanbot.sdk.ui.view.camera.configuration.DocumentScannerConfiguration
import io.scanbot.sdk.ui.view.edit.configuration.CroppingConfiguration
import io.scanbot.sdk.ui.view.hic.HealthInsuranceCardScannerActivity
import io.scanbot.sdk.ui.view.hic.configuration.HealthInsuranceCardScannerConfiguration
import io.scanbot.sdk.ui.view.mrz.MRZScannerActivity
import io.scanbot.sdk.ui.view.mrz.configuration.MRZScannerConfiguration
import io.scanbot.sdk.ui.view.workflow.WorkflowScannerActivity
import io.scanbot.sdk.ui.view.workflow.configuration.WorkflowScannerConfiguration
import kotlinx.android.synthetic.main.activity_main.*
import net.doo.snap.camera.CameraPreviewMode
import net.doo.snap.lib.detector.DetectionResult
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
        private const val CROP_DEFAULT_UI_REQUEST_CODE = 9999
        private const val SELECT_PICTURE_FOR_CROPPING_UI_REQUEST = 8888
        private const val SELECT_PICTURE_FOR_DOC_DETECTION_REQUEST = 7777
        private const val CAMERA_DEFAULT_UI_REQUEST_CODE = 1111
    }

    private lateinit var scanbotSDK: ScanbotSDK

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when {
            requestCode == MRZ_DEFAULT_UI_REQUEST_CODE && resultCode == Activity.RESULT_OK -> showMrzDialog(data!!.getParcelableExtra(MRZScannerActivity.EXTRACTED_FIELDS_EXTRA))
            requestCode == MRZ_SNAP_WORKFLOW_REQUEST_CODE && resultCode == Activity.RESULT_OK -> showMrzImageWorkflowResult(data!!.getParcelableExtra(WorkflowScannerActivity.WORKFLOW_EXTRA),
                    data!!.getParcelableArrayListExtra(WorkflowScannerActivity.WORKFLOW_RESULT_EXTRA))
            requestCode == MRZ_FRONBACK_SNAP_WORKFLOW_REQUEST_CODE && resultCode == Activity.RESULT_OK -> showFrontBackMrzImageWorkflowResult(data!!.getParcelableExtra(WorkflowScannerActivity.WORKFLOW_EXTRA),
                    data!!.getParcelableArrayListExtra(WorkflowScannerActivity.WORKFLOW_RESULT_EXTRA))
            requestCode == CROP_DEFAULT_UI_REQUEST_CODE && resultCode == Activity.RESULT_OK -> {
                val page = data!!.getParcelableExtra<Page>(io.scanbot.sdk.ui.view.edit.CroppingActivity.EDITED_PAGE_EXTRA)
                page.pageId
            }
            requestCode == QR_BARCODE_DEFAULT_UI_REQUEST_CODE && resultCode == Activity.RESULT_OK -> {
                data?.getParcelableExtra<BarcodeScanningResult>(BaseBarcodeScannerActivity.SCANNED_BARCODE_EXTRA)
                        ?.let {
                            val imagePath =
                                    data.getStringExtra(BaseBarcodeScannerActivity.SCANNED_BARCODE_IMAGE_PATH_EXTRA)
                            val previewPath =
                                    data.getStringExtra(BaseBarcodeScannerActivity.SCANNED_BARCODE_PREVIEW_FRAME_PATH_EXTRA)

                            BarcodeResultRepository.barcodeResultBundle =
                                    BarcodeResultBundle(it, imagePath, previewPath)

                            val intent = Intent(this, BarcodeResultActivity::class.java)
                            startActivity(intent)
                        }
            }
            requestCode == BARCODE_AND_DOC_SCAN_WORKFLOW_REQUEST_CODE && resultCode == Activity.RESULT_OK -> showBarcodeAndDocumentWorkflowResult(data!!.getParcelableExtra(WorkflowScannerActivity.WORKFLOW_EXTRA),
                    data!!.getParcelableArrayListExtra(WorkflowScannerActivity.WORKFLOW_RESULT_EXTRA))
            requestCode == DC_SCAN_WORKFLOW_REQUEST_CODE && resultCode == Activity.RESULT_OK -> showDCWorkflowResult(data!!.getParcelableExtra(WorkflowScannerActivity.WORKFLOW_EXTRA),
                    data!!.getParcelableArrayListExtra(WorkflowScannerActivity.WORKFLOW_RESULT_EXTRA))
            requestCode == PAYFORM_SCAN_WORKFLOW_REQUEST_CODE && resultCode == Activity.RESULT_OK -> showPayFormWorkflowResult(data!!.getParcelableExtra(WorkflowScannerActivity.WORKFLOW_EXTRA),
                    data!!.getParcelableArrayListExtra(WorkflowScannerActivity.WORKFLOW_RESULT_EXTRA))
            requestCode == SELECT_PICTURE_FOR_CROPPING_UI_REQUEST && resultCode == RESULT_OK -> ProcessImageForCroppingUI(data).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR)
            requestCode == SELECT_PICTURE_FOR_DOC_DETECTION_REQUEST && resultCode == RESULT_OK -> {
                if (!scanbotSDK.licenseInfo.isValid) {
                    showLicenseDialog()
                } else {
                    ProcessImageForAutoDocumentDetection(data).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR)
                }
            }
            requestCode == CAMERA_DEFAULT_UI_REQUEST_CODE && resultCode == Activity.RESULT_OK -> {
                val pages = data!!.getParcelableArrayExtra(DocumentScannerActivity.SNAPPED_PAGE_EXTRA).toList().map {
                    it as Page
                }

                PageRepository.addPages(pages)

                val intent = Intent(this@MainActivity, PagePreviewActivity::class.java)
                startActivity(intent)
            }
            requestCode == EHIC_SCAN_REQUEST_CODE && resultCode == Activity.RESULT_OK -> {
                val hicRecognitionResult = data!!.getParcelableExtra<HealthInsuranceCardRecognitionResult>(HealthInsuranceCardScannerActivity.EXTRACTED_FIELDS_EXTRA)
                showEHICResultDialog(hicRecognitionResult)
            }
        }
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

        findViewById<View>(R.id.qr_camera_default_ui).setOnClickListener {
            val barcodeCameraConfiguration = BarcodeScannerConfiguration()

            barcodeCameraConfiguration.setTopBarButtonsColor(ContextCompat.getColor(this, android.R.color.white))
            barcodeCameraConfiguration.setTopBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
            barcodeCameraConfiguration.setFinderTextHint("Please align the QR-/Barcode in the frame above to scan it.")

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
    }

    private fun importImageWithDetect() {
        val imageIntent = Intent()
        imageIntent.type = "image/*"
        imageIntent.action = Intent.ACTION_GET_CONTENT
        imageIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            imageIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        startActivityForResult(Intent.createChooser(imageIntent, getString(R.string.share_title)), SELECT_PICTURE_FOR_DOC_DETECTION_REQUEST)
    }

    private fun processGalleryResult(data: Intent): List<Bitmap> {
        val imageUris = data.data?.let { listOf(it) }
                ?: (0 until data.clipData.itemCount).toList().map { data.clipData.getItemAt(it).uri }

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