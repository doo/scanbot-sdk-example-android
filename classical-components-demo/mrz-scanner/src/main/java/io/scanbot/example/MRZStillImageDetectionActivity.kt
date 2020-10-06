package io.scanbot.example

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PointF
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.example.MRZResultActivity.Companion.newIntent
import io.scanbot.mrzscanner.model.MRZRecognitionResult
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.camera.CameraPreviewMode
import io.scanbot.sdk.core.contourdetector.DetectionResult
import io.scanbot.sdk.mrzscanner.MRZScanner
import io.scanbot.sdk.persistence.Page
import io.scanbot.sdk.persistence.PageFileStorage.PageFileType
import io.scanbot.sdk.process.ImageFilterType
import io.scanbot.sdk.ui.view.base.configuration.CameraOrientationMode
import io.scanbot.sdk.ui.view.camera.DocumentScannerActivity
import io.scanbot.sdk.ui.view.camera.configuration.DocumentScannerConfiguration
import io.scanbot.sdk.ui.view.edit.CroppingActivity
import io.scanbot.sdk.ui.view.edit.configuration.CroppingConfiguration
import io.scanbot.sdk.util.FileChooserUtils
import io.scanbot.sdk.util.bitmap.BitmapUtils.decodeQuietly
import java.io.IOException

class MRZStillImageDetectionActivity : AppCompatActivity() {
    private lateinit var resultImageView: ImageView
    private lateinit var cropBtn: Button
    private lateinit var runRecognitionBtn: Button
    private lateinit var progressView: View

    private var page: Page? = null

    private lateinit var scanbotSDK: ScanbotSDK
    private lateinit var mrzScanner: MRZScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mrz_still_image_detection)
        resultImageView = findViewById(R.id.resultImageView)
        progressView = findViewById(R.id.progressBar)
        scanbotSDK = ScanbotSDK(this)
        mrzScanner = scanbotSDK.mrzScanner()

        findViewById<View>(R.id.start_scanner_btn).setOnClickListener {
            val configuration = DocumentScannerConfiguration()
            configuration.setMultiPageEnabled(false)
            configuration.setMultiPageButtonHidden(true)
            configuration.setAutoSnappingEnabled(false)
            configuration.setCameraPreviewMode(CameraPreviewMode.FIT_IN)
            configuration.setOrientationLockMode(CameraOrientationMode.PORTRAIT)
            configuration.setIgnoreBadAspectRatio(true)
            val intent = DocumentScannerActivity.newIntent(this@MRZStillImageDetectionActivity, configuration)
            startActivityForResult(intent, DOCUMENT_SCANNER_REQUEST_CODE)
        }

        findViewById<View>(R.id.import_from_lib_btn).setOnClickListener { openGallery() }

        cropBtn = findViewById(R.id.crop_btn)
        cropBtn.setOnClickListener {
            val configuration = CroppingConfiguration()
            configuration.setPage(page!!)
            val intent = CroppingActivity.newIntent(this@MRZStillImageDetectionActivity, configuration)
            startActivityForResult(intent, CROP_REQUEST_CODE)
        }

        runRecognitionBtn = findViewById(R.id.run_recognition_btn)
        runRecognitionBtn.setOnClickListener { runRecognition() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.let {
            if (requestCode == DOCUMENT_SCANNER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
                val parcelablePages = data.getParcelableArrayExtra(DocumentScannerActivity.SNAPPED_PAGE_EXTRA)
                page = parcelablePages[0] as Page
                displayPreviewImage()
                cropBtn.visibility = View.VISIBLE
                runRecognitionBtn.visibility = View.VISIBLE
                return
            }
            if (requestCode == PHOTOLIB_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
                val imageUri = data.data
                ImportImageToPageTask(imageUri!!).execute()
                progressView.visibility = View.VISIBLE
                return
            }
            if (requestCode == CROP_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
                page = data.getParcelableExtra(CroppingActivity.EDITED_PAGE_EXTRA)
                displayPreviewImage()
                return
            }
        }
    }

    private fun openGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        startActivityForResult(Intent.createChooser(intent, "Select picture"), PHOTOLIB_REQUEST_CODE)
    }

    private fun displayPreviewImage() {
        val imageUri = scanbotSDK.pageFileStorage().getPreviewImageURI(page!!.pageId, PageFileType.DOCUMENT)
        resultImageView.setImageBitmap(loadImage(imageUri))
    }

    private fun loadImage(imageUri: Uri): Bitmap? {
        return MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
    }

    private fun runRecognition() {
        RecognizeMrzTask(page!!).execute()
        progressView.visibility = View.VISIBLE
    }

    private inner class RecognizeMrzTask(private val page: Page) : AsyncTask<Any?, Any?, Any?>() {
        override fun doInBackground(objects: Array<Any?>): Any? {
            val imageUri = scanbotSDK.pageFileStorage().getImageURI(page.pageId, PageFileType.DOCUMENT)
            val documentImage = loadImage(imageUri)
            return mrzScanner.recognizeMRZBitmap(documentImage, 0)
        }

        override fun onPostExecute(o: Any?) {
            super.onPostExecute(o)
            progressView.visibility = View.GONE
            val mrzRecognitionResult = o as MRZRecognitionResult?
            if (mrzRecognitionResult != null && mrzRecognitionResult.recognitionSuccessful) {
                startActivity(newIntent(this@MRZStillImageDetectionActivity, mrzRecognitionResult))
            } else {
                Toast.makeText(this@MRZStillImageDetectionActivity,
                        "No MRZ data recognized!", Toast.LENGTH_LONG).show()
            }
        }

    }

    private inner class ImportImageToPageTask(private val imageUri: Uri) : AsyncTask<Any?, Any?, Any?>() {
        override fun doInBackground(objects: Array<Any?>): Any? {
            val pageId = scanbotSDK.pageFileStorage().add(loadImage(imageUri)!!)
            val emptyPolygon = emptyList<PointF>()
            val newPage = Page(pageId, emptyPolygon, DetectionResult.OK, ImageFilterType.NONE)
            return try {
                scanbotSDK.pageProcessor().detectDocument(newPage)
            } catch (ex: IOException) {
                Log.e("ImportImageToPageTask", "Error detecting document on page " + newPage.pageId)
                null
            }
        }

        override fun onPostExecute(result: Any?) {
            super.onPostExecute(result)
            progressView.visibility = View.GONE
            if (result != null) {
                page = result as Page?
                displayPreviewImage()
                cropBtn.visibility = View.VISIBLE
                runRecognitionBtn.visibility = View.VISIBLE
            }
        }

    }

    companion object {
        private const val DOCUMENT_SCANNER_REQUEST_CODE = 4711
        private const val PHOTOLIB_REQUEST_CODE = 5711
        private const val CROP_REQUEST_CODE = 6711
    }
}