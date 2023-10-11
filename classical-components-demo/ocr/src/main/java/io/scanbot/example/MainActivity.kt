package io.scanbot.example

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import io.scanbot.example.common.ImportImageContract
import io.scanbot.pdf.model.PageSize
import io.scanbot.pdf.model.PdfConfig
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.core.contourdetector.DetectionStatus
import io.scanbot.sdk.docprocessing.PageProcessor
import io.scanbot.sdk.ocr.OpticalCharacterRecognizer
import io.scanbot.sdk.persistence.Page
import io.scanbot.sdk.persistence.PageFileStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

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
        val galleryImageLauncher =
            registerForActivityResult(ImportImageContract(this)) { resultEntity ->
                lifecycleScope.launch(Dispatchers.Default) {
                    resultEntity?.let { recognizeTextWithoutPDF(it) }
                    // Alternative OCR examples - PDF + OCR (sandwiched PDF):
                    //new RecognizeTextWithPDF(imageUri).execute();
                    withContext(Dispatchers.Main) { progressView.visibility = View.VISIBLE }
                }
            }
        findViewById<View>(R.id.scanButton).setOnClickListener { v: View? ->
            galleryImageLauncher.launch(
                Unit
            )
        }
        progressView = findViewById(R.id.progressBar)
    }

    private fun initDependencies() {
        val scanbotSDK = ScanbotSDK(this)
        opticalCharacterRecognizer = scanbotSDK.createOcrRecognizer()
        pageFileStorage = scanbotSDK.createPageFileStorage()
        pageProcessor = scanbotSDK.createPageProcessor()
    }

    private fun askPermission() {
        if (checkPermissionNotGranted(Manifest.permission.READ_EXTERNAL_STORAGE) ||
            checkPermissionNotGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), 999
            )
        }
    }

    private fun checkPermissionNotGranted(permission: String) =
        ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED


    private suspend fun recognizeTextWithoutPDF(bitmap: Bitmap) {

        val ocrResult = try {
            val newPageId = pageFileStorage.add(bitmap)
            val page = Page(newPageId, emptyList(), DetectionStatus.OK)

            val processedPage = pageProcessor.detectDocument(page)

            val pages = listOf(processedPage)

            opticalCharacterRecognizer.recognizeTextFromPages(pages)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        withContext(Dispatchers.Main) {
            progressView.visibility = View.GONE
            ocrResult.let {
                if (it.ocrPages.isNotEmpty()) {
                    Toast.makeText(
                        this@MainActivity,
                        """
                            Recognized page content:
                            ${it.recognizedText}
                            """.trimIndent(),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private suspend fun recognizeTextWithPDF(bitmap: Bitmap) {

        val ocrResult = try {
            val newPageId = pageFileStorage.add(bitmap)
            val page =
                Page(newPageId, emptyList(), DetectionStatus.OK)

            val processedPage = pageProcessor.detectDocument(page)

            val pages = listOf(processedPage)

            opticalCharacterRecognizer.recognizeTextWithPdfFromPages(
                pages,
                PdfConfig.defaultConfig().copy(pageSize = PageSize.A4)
            )
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        withContext(Dispatchers.Main) {
            progressView.visibility = View.GONE

            ocrResult?.sandwichedPdfDocumentFile?.let { file ->
                Toast.makeText(
                    this@MainActivity,
                    """
                            See PDF file:
                            ${file.path}
                            """.trimIndent(),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

}
