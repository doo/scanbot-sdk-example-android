package io.scanbot.example

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import io.scanbot.example.fragments.BarCodeDialogFragment
import io.scanbot.example.fragments.ErrorFragment
import io.scanbot.example.fragments.MRZDialogFragment
import io.scanbot.example.fragments.QRCodeDialogFragment
import io.scanbot.mrzscanner.model.MRZRecognitionResult
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.barcode.entity.BarcodeFormat
import io.scanbot.sdk.barcode.entity.BarcodeScanningResult
import io.scanbot.sdk.persistence.Page
import io.scanbot.sdk.process.ImageFilterType
import io.scanbot.sdk.ui.view.barcode.BarcodeScannerActivity
import io.scanbot.sdk.ui.view.barcode.configuration.BarcodeScannerConfiguration
import io.scanbot.sdk.ui.view.camera.configuration.DocumentScannerConfiguration
import io.scanbot.sdk.ui.view.edit.configuration.CroppingConfiguration
import io.scanbot.sdk.ui.view.mrz.MRZScannerActivity
import io.scanbot.sdk.ui.view.mrz.configuration.MRZScannerConfiguration
import kotlinx.android.synthetic.main.activity_default_preview.*
import net.doo.snap.blob.BlobFactory
import net.doo.snap.blob.BlobManager
import net.doo.snap.camera.CameraPreviewMode
import net.doo.snap.lib.detector.DetectionResult
import java.io.IOException


class DefaultUIPreviewActivity : AppCompatActivity() {

    private val MRZ_DEFAULT_UI_REQUEST_CODE = 909
    private val BARCODE_DEFAULT_UI_REQUEST_CODE = 910
    private val QR_CODE_DEFAULT_UI_REQUEST_CODE = 911
    private val CROP_DEFAULT_UI_REQUEST_CODE = 9999
    private val SELECT_PICTURE_FOR_CROPPING_UI_REQUEST = 8888
    private val SELECT_PICTURE_FOR_DOC_DETECTION_REQUEST = 7777

    private val CAMERA_DEFAULT_UI_REQUEST_CODE = 1111

    private lateinit var scanbotSDK: ScanbotSDK
    private lateinit var blobManager: BlobManager
    private lateinit var blobFactory: BlobFactory

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_CANCELED) {
            showLicenseDialog()
        }
        if (requestCode == MRZ_DEFAULT_UI_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            showMrzDialog(data!!.getParcelableExtra(MRZScannerActivity.EXTRACTED_FIELDS_EXTRA))
        } else if (requestCode == CROP_DEFAULT_UI_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val page = data!!.getParcelableExtra<Page>(io.scanbot.sdk.ui.view.edit.CroppingActivity.EDITED_PAGE_EXTRA)
            page.pageId
        } else if (requestCode == BARCODE_DEFAULT_UI_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val barcodeData = data!!.getParcelableExtra<BarcodeScanningResult>(BarcodeScannerActivity.SCANNED_BARCODE_EXTRA)
            showBarcodeDialog(barcodeData)
        } else if (requestCode == QR_CODE_DEFAULT_UI_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val qrCode = data!!.getParcelableExtra<BarcodeScanningResult>(BarcodeScannerActivity.SCANNED_BARCODE_EXTRA)
            showQrDialog(qrCode)
        } else if (requestCode == SELECT_PICTURE_FOR_CROPPING_UI_REQUEST) {
            if (resultCode == RESULT_OK) {
                ProcessImageForCroppingUI(data).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR)
            }
        } else if (requestCode == SELECT_PICTURE_FOR_DOC_DETECTION_REQUEST) {
            if (resultCode == RESULT_OK) {
                ProcessImageForAutoDocumentDetection(data).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR)
            }
        } else if (requestCode == CAMERA_DEFAULT_UI_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val intent = Intent(this@DefaultUIPreviewActivity, PagePreviewActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showLicenseDialog() {
        val dialogFragment = ErrorFragment.newInstanse()
        dialogFragment.show(supportFragmentManager, ErrorFragment.NAME)
    }

    private fun showMrzDialog(mrzRecognitionResult: MRZRecognitionResult) {
        val dialogFragment = MRZDialogFragment.newInstanse(mrzRecognitionResult)
        dialogFragment.show(supportFragmentManager, MRZDialogFragment.NAME)
    }

    private fun showBarcodeDialog(barcodeRecognitionResult: BarcodeScanningResult) {
        val dialogFragment = BarCodeDialogFragment.newInstanse(barcodeRecognitionResult)
        dialogFragment.show(supportFragmentManager, BarCodeDialogFragment.NAME)
    }

    private fun showQrDialog(barcodeRecognitionResult: BarcodeScanningResult) {
        val fm = supportFragmentManager
        val dialogFragment = QRCodeDialogFragment.newInstanse(barcodeRecognitionResult)
        dialogFragment.show(fm, QRCodeDialogFragment.NAME)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initDependencies()
        setContentView(R.layout.activity_default_preview)

        warning_view.visibility = if (!Application.LICENSE.isEmpty()) View.GONE else View.VISIBLE

        // select an image from photo library and run document detection on it:
        findViewById<View>(R.id.doc_detection_on_image_btn).setOnClickListener {
            val imageIntent = Intent()
            imageIntent.type = "image/*"
            imageIntent.action = Intent.ACTION_GET_CONTENT
            imageIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, false)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                imageIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
            }
            startActivityForResult(Intent.createChooser(imageIntent, "Select Image"), SELECT_PICTURE_FOR_DOC_DETECTION_REQUEST)
        }

        findViewById<View>(R.id.camera_default_ui).setOnClickListener {
            val cameraConfiguration = DocumentScannerConfiguration()
            cameraConfiguration.setCameraPreviewMode(CameraPreviewMode.FILL_IN)
            cameraConfiguration.setIgnoreBadAspectRatio(true)
            cameraConfiguration.setBottomBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
            cameraConfiguration.setBottomBarButtonsColor(ContextCompat.getColor(this, R.color.greyColor))
            cameraConfiguration.setTopBarButtonsActiveColor(ContextCompat.getColor(this, android.R.color.white))
            cameraConfiguration.setCameraBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
            cameraConfiguration.setUserGuidanceBackgroundColor(ContextCompat.getColor(this, android.R.color.black))
            cameraConfiguration.setUserGuidanceTextColor(ContextCompat.getColor(this, android.R.color.white))

            val intent = io.scanbot.sdk.ui.view.camera.DocumentScannerActivity.newIntent(this@DefaultUIPreviewActivity,
                    cameraConfiguration
            )
            startActivityForResult(intent, CAMERA_DEFAULT_UI_REQUEST_CODE)
        }

        findViewById<View>(R.id.page_preview_activity).setOnClickListener {
            val intent = Intent(this@DefaultUIPreviewActivity, PagePreviewActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.mrz_camera_default_ui).setOnClickListener {
            val mrzCameraConfiguration = MRZScannerConfiguration()

            val intent = MRZScannerActivity.newIntent(this@DefaultUIPreviewActivity, mrzCameraConfiguration)
            startActivityForResult(intent, MRZ_DEFAULT_UI_REQUEST_CODE)
        }

        findViewById<View>(R.id.barcode_camera_default_ui).setOnClickListener {
            val barcodeCameraConfiguration = BarcodeScannerConfiguration()

            barcodeCameraConfiguration.setTopBarButtonsColor(ContextCompat.getColor(this, android.R.color.white))
            barcodeCameraConfiguration.setTopBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))

            val intent = BarcodeScannerActivity.newIntent(this@DefaultUIPreviewActivity, barcodeCameraConfiguration)
            startActivityForResult(intent, BARCODE_DEFAULT_UI_REQUEST_CODE)
        }

        findViewById<View>(R.id.qr_camera_default_ui).setOnClickListener {
            val qrcodeCameraConfiguration = BarcodeScannerConfiguration()

            qrcodeCameraConfiguration.setTopBarButtonsColor(ContextCompat.getColor(this, android.R.color.white))
            qrcodeCameraConfiguration.setTopBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
            qrcodeCameraConfiguration.setBarcodeFormatsFilter(arrayListOf(BarcodeFormat.QR_CODE))

            val intent = BarcodeScannerActivity.newIntent(this@DefaultUIPreviewActivity, qrcodeCameraConfiguration)

            startActivityForResult(intent, QR_CODE_DEFAULT_UI_REQUEST_CODE)
        }

    }

    private fun processGalleryResult(data: Intent): Bitmap? {
        val imageUri = data.data
        var bitmap: Bitmap? = null
        if (imageUri != null) {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            } catch (e: IOException) {
            }
        }
        return bitmap
    }

    private fun initDependencies() {
        scanbotSDK = ScanbotSDK(this)
        blobManager = scanbotSDK.blobManager()
        blobFactory = scanbotSDK.blobFactory()
    }

    private fun extractData(result: MRZRecognitionResult): String {
        return StringBuilder()
                .append("documentCode: ").append(result.documentCodeField().value).append("\n")
                .append("First name: ").append(result.firstNameField().value).append("\n")
                .append("Last name: ").append(result.lastNameField().value).append("\n")
                .append("issuingStateOrOrganization: ").append(result.issuingStateOrOrganizationField().value).append("\n")
                .append("departmentOfIssuance: ").append(result.departmentOfIssuanceField().value).append("\n")
                .append("nationality: ").append(result.nationalityField().value).append("\n")
                .append("dateOfBirth: ").append(result.dateOfBirthField().value).append("\n")
                .append("gender: ").append(result.genderField().value).append("\n")
                .append("dateOfExpiry: ").append(result.dateOfExpiryField().value).append("\n")
                .append("personalNumber: ").append(result.personalNumberField().value).append("\n")
                .append("optional1: ").append(result.optional1Field().value).append("\n")
                .append("optional2: ").append(result.optional2Field().value).append("\n")
                .append("discreetIssuingStateOrOrganization: ").append(result.discreetIssuingStateOrOrganizationField().value).append("\n")
                .append("validCheckDigitsCount: ").append(result.validCheckDigitsCount).append("\n")
                .append("checkDigitsCount: ").append(result.checkDigitsCount).append("\n")
                .append("travelDocType: ").append(result.travelDocTypeField().value).append("\n")
                .toString()
    }

    /**
     * Imports a selected image as original image, creates a new page and opens the Cropping UI on it.
     */
    internal inner class ProcessImageForCroppingUI(private var data: Intent?) : AsyncTask<Void, Void, Page>() {

        override fun onPreExecute() {
            super.onPreExecute()
            progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg params: Void): Page {
            val processGalleryResult = processGalleryResult(data!!)

            val pageFileStorage = io.scanbot.sdk.ScanbotSDK(this@DefaultUIPreviewActivity).pageFileStorage()
            // create a new Page object with given image as original image:
            val pageId = pageFileStorage.add(processGalleryResult!!)
            return Page(pageId)
        }

        override fun onPostExecute(page: Page) {
            progressBar.visibility = View.GONE
            val editPolygonConfiguration = CroppingConfiguration()

            editPolygonConfiguration.setPage(page)
            val intent = io.scanbot.sdk.ui.view.edit.CroppingActivity.newIntent(
                    applicationContext,
                    editPolygonConfiguration
            )
            startActivityForResult(intent, CROP_DEFAULT_UI_REQUEST_CODE)
        }
    }


    /**
     * Imports a selected image as original image and performs auto document detection on it.
     */
    internal inner class ProcessImageForAutoDocumentDetection(private var data: Intent?) : AsyncTask<Void, Void, Page>() {

        override fun onPreExecute() {
            super.onPreExecute()
            progressBar.visibility = View.VISIBLE
            Toast.makeText(this@DefaultUIPreviewActivity,
                    "Importing selected image and running auto document detection...", Toast.LENGTH_LONG).show()
        }

        override fun doInBackground(vararg params: Void): Page {
            val processGalleryResult = processGalleryResult(data!!)

            val pageFileStorage = io.scanbot.sdk.ScanbotSDK(this@DefaultUIPreviewActivity).pageFileStorage()
            val pageProcessor = io.scanbot.sdk.ScanbotSDK(this@DefaultUIPreviewActivity).pageProcessor()

            // create a new Page object with given image as original image:
            val pageId = pageFileStorage.add(processGalleryResult!!)
            var page = Page(pageId, emptyList(), DetectionResult.OK, ImageFilterType.NONE)

            // run auto document detection on it:
            page = pageProcessor.detectDocument(page)

            return page
        }

        override fun onPostExecute(page: Page) {
            progressBar.visibility = View.GONE
            val intent = Intent(this@DefaultUIPreviewActivity, PagePreviewActivity::class.java)
            startActivity(intent)
        }
    }

}