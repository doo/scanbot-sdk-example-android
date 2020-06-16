package io.scanbot.example

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.core.contourdetector.DetectionResult
import io.scanbot.sdk.docprocessing.PageProcessor
import io.scanbot.sdk.entity.Language
import io.scanbot.sdk.ocr.OpticalCharacterRecognizer
import io.scanbot.sdk.ocr.process.OcrResult
import io.scanbot.sdk.persistence.Page
import io.scanbot.sdk.persistence.PageFileStorage
import io.scanbot.sdk.process.ImageFilterType
import io.scanbot.sdk.process.PDFPageSize
import io.scanbot.sdk.util.FileChooserUtils
import io.scanbot.sdk.util.bitmap.BitmapUtils.decodeQuietly
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var progressView: View

    private lateinit var opticalCharacterRecognizer: OpticalCharacterRecognizer
    private lateinit var pageFileStorage: PageFileStorage
    private lateinit var pageProcessor: PageProcessor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        askPermission()
        initDependencies()

        findViewById<View>(R.id.scanButton).setOnClickListener { v: View? -> openGallery() }
        progressView = findViewById(R.id.progressBar)
    }

    private fun openGallery() {
        val intent = Intent().apply {
            type = IMAGE_TYPE
            action = Intent.ACTION_GET_CONTENT
            putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        }

        startActivityForResult(Intent.createChooser(intent, "Select picture"), SELECT_PICTURE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode != SELECT_PICTURE_REQUEST || resultCode != Activity.RESULT_OK) {
            return
        }
        val imageUri = intent?.data ?: return

        RecognizeTextWithoutPDFTask(imageUri).execute()

        // Alternative OCR examples - PDF + OCR (sandwiched PDF):
        //new RecognizeTextWithPDFTask(imageUri).execute();
        progressView.visibility = View.VISIBLE
    }

    private fun initDependencies() {
        val scanbotSDK = ScanbotSDK(this)
        opticalCharacterRecognizer = scanbotSDK.ocrRecognizer()
        pageFileStorage = scanbotSDK.pageFileStorage
        pageProcessor = scanbotSDK.pageProcessor()
    }

    private fun askPermission() {
        if (checkPermissionNotGranted(Manifest.permission.READ_EXTERNAL_STORAGE) ||
                checkPermissionNotGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), 999)
        }
    }

    private fun checkPermissionNotGranted(permission: String) =
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED

    /*
    This AsyncTask is used here only for the sake of example. Please, try to avoid usage of
    AsyncTasks in your application
     */
    private inner class RecognizeTextWithoutPDFTask(private val imageUri: Uri) : AsyncTask<Void, Void, OcrResult?>() {
        override fun doInBackground(vararg voids: Void): OcrResult? {
            return try {
                val bitmap = loadImage()

                val newPageId = pageFileStorage.add(bitmap)
                val page = Page(newPageId, emptyList(), DetectionResult.OK, ImageFilterType.BINARIZED)

                val processedPage = pageProcessor.detectDocument(page)

                val pages = listOf(processedPage)
                val languages = setOf(Language.ENG)

                opticalCharacterRecognizer.recognizeTextFromPages(pages, languages)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }

        @Throws(IOException::class)
        private fun loadImage(): Bitmap {
            val imagePath = FileChooserUtils.getPath(this@MainActivity, imageUri)
            return decodeQuietly(imagePath, null) ?: throw IOException("Bitmap is null")
        }

        override fun onPostExecute(ocrResult: OcrResult?) {
            progressView.visibility = View.GONE

            ocrResult?.let {
                if (it.ocrPages.isNotEmpty()) {
                    Toast.makeText(this@MainActivity,
                            """
                            Recognized page content:
                            ${it.recognizedText}
                            """.trimIndent(),
                            Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    /*
    This AsyncTask is used here only for the sake of example. Please, try to avoid usage of
    AsyncTasks in your application
     */
    private inner class RecognizeTextWithPDFTask private constructor(private val imageUri: Uri) : AsyncTask<Void, Void, OcrResult?>() {
        override fun doInBackground(vararg voids: Void): OcrResult? {
            return try {
                val bitmap = loadImage()

                val newPageId = pageFileStorage.add(bitmap)
                val page = Page(newPageId, emptyList(), DetectionResult.OK, ImageFilterType.BINARIZED)

                val processedPage = pageProcessor.detectDocument(page)

                val pages = listOf(processedPage)
                val languages = setOf(Language.ENG)

                opticalCharacterRecognizer.recognizeTextWithPdfFromPages(pages, PDFPageSize.A4, languages)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }

        @Throws(IOException::class)
        private fun loadImage(): Bitmap {
            val imagePath = FileChooserUtils.getPath(this@MainActivity, imageUri)
            return decodeQuietly(imagePath, null) ?: throw IOException("Bitmap is null")
        }

        override fun onPostExecute(ocrResult: OcrResult?) {
            progressView.visibility = View.GONE

            ocrResult?.sandwichedPdfDocumentFile?.let { file ->
                Toast.makeText(this@MainActivity,
                        """
                            See PDF file:
                            ${file.path}
                            """.trimIndent(),
                        Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        private const val SELECT_PICTURE_REQUEST = 100
        private const val IMAGE_TYPE = "image/*"
    }
}