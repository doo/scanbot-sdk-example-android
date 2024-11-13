package com.example.scanbot.main

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.scanbot.Const
import com.example.scanbot.preview.DocumentPreviewActivity
import com.example.scanbot.preview.SinglePagePreviewActivity
import com.example.scanbot.utils.getUrisFromGalleryResult
import com.example.scanbot.utils.toBitmap
import io.scanbot.common.AspectRatio
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.docprocessing.Document
import io.scanbot.sdk.ui_v2.common.ScanbotColor
import io.scanbot.sdk.ui_v2.common.activity.registerForActivityResultOk
import io.scanbot.sdk.ui_v2.document.DocumentScannerActivity
import io.scanbot.sdk.ui_v2.document.configuration.DocumentScanningFlow
import io.scanbot.sdk.usecases.documents.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private val scanbotSDK = ScanbotSDK(this@MainActivity)

    private lateinit var documentScannerResult: ActivityResultLauncher<DocumentScanningFlow>
    private lateinit var pictureForDocDetectionResult: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        documentScannerResult =
            registerForActivityResultOk(DocumentScannerActivity.ResultContract(this)) { activityResult ->
                if (activityResult.resultCode == Activity.RESULT_OK && activityResult.result != null) {
                    val document = activityResult.result!!
                    runPreviewScreen(document.uuid, singlePageOnly = document.pages.size == 1)
                } else processNotOkResult()
            }

        pictureForDocDetectionResult =
            this.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
                if (activityResult.resultCode == Activity.RESULT_OK) {
                    activityResult.data?.let { imagePickerResult ->
                        lifecycleScope.launch {
                            withContext(Dispatchers.Default) {
                                val document = scanbotSDK.documentApi.createDocument()
                                getUrisFromGalleryResult(imagePickerResult)
                                    .asSequence() // process images one by one instead of collecting the whole list - less memory consumption
                                    .map { it.toBitmap(contentResolver) }
                                    .forEach { bitmap ->
                                        if (bitmap == null) {
                                            Log.e("MainActivity", "Failed to load bitmap from URI")
                                            return@forEach
                                        }
                                        document.addPage(bitmap)
                                    }
                                runImagesFromGalleryScanner(document.uuid)
                            }
                        }
                    }
                }
            }

        val items = listOf(
            ViewType.Header("Document Scanning Use Cases"),
            ViewType.Option(UseCase.SINGLE_PAGE, "Single-Page Scanning"),
            ViewType.Option(UseCase.MULTIPLE_PAGE, "Multiple-Page Scanning"),
            ViewType.Option(UseCase.FINDER, "Single-Page Scanning with Finder "),
            ViewType.Option(UseCase.GALLERY, "Pick from Gallery"),
            ViewType.Support(),
        )

        val adapter = OptionAdapter(items) {
            when (it) {
                UseCase.SINGLE_PAGE -> runSinglePageScanner()
                UseCase.MULTIPLE_PAGE -> runMultiPageScanner()
                UseCase.FINDER -> runFinderPageScanner()
                UseCase.GALLERY -> importImagesFromLibrary()
            }
        }
        val recyclerView = findViewById<RecyclerView>(R.id.main_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun runSinglePageScanner() {
        val config = DocumentScanningFlow().apply {
            this.outputSettings.pagesScanLimit = 1
            this.screens.camera.cameraConfiguration.requiredAspectRatios = listOf(
                AspectRatio(width = 21.0, height = 29.7) // allow only A4 format documents to be scanned
            )
        }
        documentScannerResult.launch(config)
    }

    private fun runMultiPageScanner() {
        val config = DocumentScanningFlow().apply {
            this.screens.camera.bottomBar.shutterButton.innerColor = ScanbotColor(Color.RED)
        }
        documentScannerResult.launch(config)
    }

    private fun runImagesFromGalleryScanner(documentId: String) {
        val config = DocumentScanningFlow().apply {
            this.documentUuid = documentId
        }
        documentScannerResult.launch(config)
    }

    private fun runFinderPageScanner() {
        val a4AspectRatio = AspectRatio(width = 21.0, height = 29.7)
        val config = DocumentScanningFlow().apply {
            this.screens.camera.cameraConfiguration.requiredAspectRatios = listOf(a4AspectRatio)
            this.screens.camera.viewFinder.visible = true
            this.screens.camera.viewFinder.aspectRatio = a4AspectRatio
            this.screens.camera.cameraConfiguration.acceptedSizeScore = 0.75
        }
        documentScannerResult.launch(config)
    }

    private fun importImagesFromLibrary() {
        val imageIntent = Intent()
        imageIntent.type = "image/*"
        imageIntent.action = Intent.ACTION_GET_CONTENT
        imageIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, false)
        imageIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        imageIntent.putExtra(
            Intent.EXTRA_MIME_TYPES,
            arrayOf("image/jpeg", "image/png", "image/webp", "image/heic")
        )
        pictureForDocDetectionResult.launch(Intent.createChooser(imageIntent, "Select Picture"))
    }

    private suspend fun createDocumentFromUris(uris: List<Uri>): Document {
        return withContext(Dispatchers.Default) {
            val document = scanbotSDK.documentApi.createDocument()
            uris.forEach { imageUri ->
                document.addPage(imageUri)
            }
            document
        }
    }

    private fun runPreviewScreen(documentId: String, singlePageOnly: Boolean) {
        val clazz = if (singlePageOnly) {
            SinglePagePreviewActivity::class.java
        } else {
            DocumentPreviewActivity::class.java
        }
        val intent = Intent(this, clazz)
        intent.putExtra(Const.EXTRA_DOCUMENT_ID, documentId)
        startActivity(intent)
    }

    private fun processNotOkResult() {
        // Handle the case when the result code is not OK or document is null
        // In most cases this means that user cancelled the scanning process or Scanbot SDK license is not valid
        // For the purpose of this example this method does nothing.
    }

    // Below are additional configurations for document scanner that you might want to consider as an example.
    // These are not used in the current implementation of the example app.

    private fun runSinglePageScannerWithGuidance() {
        val config = DocumentScanningFlow().apply {
            this.outputSettings.pagesScanLimit = 1
            with(this.screens.camera.userGuidance.statesTitles) {
                badAngles = "Hold your phone parallel to the document"
                badAspectRatio = "The document is not in the correct format"
                tooDark = "Its too dark, please add more light"
                tooSmall = "Document too small, please move closer"
                tooNoisy = "Image too noisy, please move to a better lit area"
                noDocumentFound = "No document detected, please try again"
                capturing = "Hold your phone, steady, trying to scan"
            }
        }
        documentScannerResult.launch(config)
    }
}
