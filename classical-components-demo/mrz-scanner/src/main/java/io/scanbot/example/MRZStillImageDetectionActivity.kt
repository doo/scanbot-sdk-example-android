package io.scanbot.example

import android.graphics.Bitmap
import android.graphics.PointF
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.scanbot.example.MRZResultActivity.Companion.newIntent
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.camera.CameraPreviewMode
import io.scanbot.sdk.common.ImportImageContract
import io.scanbot.sdk.core.contourdetector.DetectionStatus
import io.scanbot.sdk.docprocessing.PageProcessor
import io.scanbot.sdk.mrzscanner.MRZScanner
import io.scanbot.sdk.persistence.Page
import io.scanbot.sdk.persistence.PageFileStorage
import io.scanbot.sdk.persistence.PageFileStorage.PageFileType
import io.scanbot.sdk.process.ImageFilterType
import io.scanbot.sdk.ui.registerForActivityResultOk
import io.scanbot.sdk.ui.view.base.configuration.CameraOrientationMode
import io.scanbot.sdk.ui.view.camera.DocumentScannerActivity
import io.scanbot.sdk.ui.view.camera.configuration.DocumentScannerConfiguration
import io.scanbot.sdk.ui.view.edit.CroppingActivity
import io.scanbot.sdk.ui.view.edit.configuration.CroppingConfiguration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class MRZStillImageDetectionActivity : AppCompatActivity() {
    private lateinit var resultImageView: ImageView
    private lateinit var cropBtn: Button
    private lateinit var runRecognitionBtn: Button
    private lateinit var progressView: View

    private var page: Page? = null

    private lateinit var mrzScanner: MRZScanner
    private lateinit var pageFileStorage: PageFileStorage
    private lateinit var pageProcessor: PageProcessor

    private val docScannerResultLauncher =
        registerForActivityResultOk(DocumentScannerActivity.ResultContract()) { resultEntity ->
            val parcelablePages = resultEntity.result!!
            page = parcelablePages[0]
            displayPreviewImage()
            cropBtn.visibility = View.VISIBLE
            runRecognitionBtn.visibility = View.VISIBLE
        }

    private val croppingResultLauncher =
        registerForActivityResultOk(CroppingActivity.ResultContract()) { resultEntity ->
            page = resultEntity.result!!
            displayPreviewImage()
        }

    private val chooseFromGalleryResultLauncher = registerForActivityResult(
        ImportImageContract(this)
    ) { resultEntity ->
        lifecycleScope.launch(Dispatchers.Default) {
            lifecycleScope.launch(Dispatchers.Default) {
                resultEntity?.let {
                    importImageToPage(it)
                    withContext(Dispatchers.Main){
                        progressView.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mrz_still_image_detection)
        resultImageView = findViewById(R.id.resultImageView)
        progressView = findViewById(R.id.progressBar)

        val scanbotSDK = ScanbotSDK(this)
        mrzScanner = scanbotSDK.createMrzScanner()
        pageFileStorage = scanbotSDK.createPageFileStorage()
        pageProcessor = scanbotSDK.createPageProcessor()

        findViewById<View>(R.id.start_scanner_btn).setOnClickListener {
            val configuration = DocumentScannerConfiguration()
            configuration.setMultiPageEnabled(false)
            configuration.setMultiPageButtonHidden(true)
            configuration.setAutoSnappingEnabled(false)
            configuration.setCameraPreviewMode(CameraPreviewMode.FIT_IN)
            configuration.setOrientationLockMode(CameraOrientationMode.PORTRAIT)
            configuration.setIgnoreBadAspectRatio(true)
            docScannerResultLauncher.launch(configuration)
        }

        findViewById<View>(R.id.import_from_lib_btn).setOnClickListener { openGallery() }

        cropBtn = findViewById(R.id.crop_btn)
        cropBtn.setOnClickListener {
            val configuration = CroppingConfiguration(page!!)
            croppingResultLauncher.launch(configuration)
        }

        runRecognitionBtn = findViewById(R.id.run_recognition_btn)
        runRecognitionBtn.setOnClickListener { runRecognition() }
    }

    private fun openGallery() {
        chooseFromGalleryResultLauncher.launch(Unit)
    }

    private fun displayPreviewImage() {
        val imageUri = pageFileStorage.getPreviewImageURI(page!!.pageId, PageFileType.DOCUMENT)
        resultImageView.setImageBitmap(loadImage(imageUri))
    }

    private fun loadImage(imageUri: Uri): Bitmap? {
        return MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
    }

    private fun runRecognition() {
        lifecycleScope.launch(Dispatchers.Default) {
            recognizeMrz(page!!)
        }
        progressView.visibility = View.VISIBLE
    }

    private suspend fun recognizeMrz(page: Page) {
        val imageUri = pageFileStorage.getImageURI(page.pageId, PageFileType.DOCUMENT)
        val documentImage = loadImage(imageUri)
        val mrzRecognitionResult = mrzScanner.recognizeMRZBitmap(documentImage, 0)


        withContext(Dispatchers.Main) {
            progressView.visibility = View.GONE
            if (mrzRecognitionResult != null && mrzRecognitionResult.recognitionSuccessful) {
                startActivity(newIntent(this@MRZStillImageDetectionActivity, mrzRecognitionResult))
            } else {
                Toast.makeText(
                    this@MRZStillImageDetectionActivity,
                    "No MRZ data recognized!", Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private suspend fun importImageToPage(bitmap: Bitmap) {
        val pageId = pageFileStorage.add(bitmap)
        val emptyPolygon = emptyList<PointF>()
        val newPage = Page(pageId, emptyPolygon, DetectionStatus.OK, ImageFilterType.NONE)
        val result = try {
            pageProcessor.detectDocument(newPage)
        } catch (ex: IOException) {
            Log.e("ImportImageToPageTask", "Error detecting document on page " + newPage.pageId)
            null
        }

        withContext(Dispatchers.Main) {
            progressView.visibility = View.GONE
            if (result != null) {
                page = result as Page?
                displayPreviewImage()
                cropBtn.visibility = View.VISIBLE
                runRecognitionBtn.visibility = View.VISIBLE
            }
        }
    }
}
