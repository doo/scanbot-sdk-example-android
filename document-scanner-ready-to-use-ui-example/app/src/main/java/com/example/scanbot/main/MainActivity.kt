package com.example.scanbot.main

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.scanbot.preview.PagesPreviewActivity
import com.example.scanbot.preview.SinglePagePreviewActivity
import com.example.scanbot.utils.getUrisFromGalleryResult
import com.example.scanbot.utils.toBitmap
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.persistence.Page
import io.scanbot.sdk.persistence.PageStorageProcessor
import io.scanbot.sdk.ui.registerForActivityResultOk
import io.scanbot.sdk.ui.view.camera.DocumentScannerActivity
import io.scanbot.sdk.ui.view.camera.FinderDocumentScannerActivity
import io.scanbot.sdk.ui.view.camera.configuration.DocumentScannerConfiguration
import io.scanbot.sdk.ui.view.camera.configuration.FinderDocumentScannerConfiguration
import io.scanbot.sdk.usecases.documents.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val scanbotSDK = ScanbotSDK(this@MainActivity)
    private val pageFileStorage = scanbotSDK.createPageFileStorage()


    private val documentScannerResult: ActivityResultLauncher<DocumentScannerConfiguration> =
        registerForActivityResultOk(DocumentScannerActivity.ResultContract()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val pages = result.result ?: emptyList()
                openPreviewForPages(pages)
            }
        }

    private val finderDocumentScannerResult: ActivityResultLauncher<FinderDocumentScannerConfiguration> =
        registerForActivityResultOk(FinderDocumentScannerActivity.ResultContract()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val page = result.result
                if (page != null) {
                    runSinglePreviewScreen(page)
                }
            }
        }

    private val pictureForDocDetectionResult: ActivityResultLauncher<Intent> =
        this.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let {
                    lifecycleScope.launch(Dispatchers.Default) {
                        val uris = getUrisFromGalleryResult(it)
                        scanbotSDK.let { sdk ->
                            val pages = uris.map {
                                val bitmap = it.toBitmap(contentResolver)
                                    ?: throw IllegalArgumentException("Bitmap is null!!")
                                val page = pageFileStorage.add(
                                    bitmap,
                                    PageStorageProcessor.Configuration(
                                        createPreview = true,
                                        createDocument = true,
                                    )
                                )
                                page
                            }
                            openPreviewForPages(pages)
                        }
                    }
                }
            }
        }

    private fun openPreviewForPages(pages: List<Page>) {
        if (pages.isNotEmpty()) {
            if (pages.size > 1) {
                runPagesPreviewScreen(pages)
            } else {
                runSinglePreviewScreen(
                    pages.first()
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
        val config = DocumentScannerConfiguration()
        config.setMultiPageEnabled(false)
        config.setMultiPageButtonHidden(true)
        config.setRequiredAspectRatios(
            arrayListOf(
                io.scanbot.sdk.AspectRatio(
                    21.0,
                    29.7
                ) // allow only A4 format documents to be scanned
            )
        ) // this a A4 format
        documentScannerResult.launch(config)
    }

    private fun runMultiPageScanner() {
        val config = DocumentScannerConfiguration()
        config.setMultiPageEnabled(true)
        config.setMultiPageButtonHidden(false)
        config.setShutterButtonAutoInnerColor(Color.RED)
        config.setShutterButtonManualInnerColor(Color.RED)
        documentScannerResult.launch(config)
    }

    private fun runFinderPageScanner() {
        val config = FinderDocumentScannerConfiguration()
        config.setFinderAspectRatio(io.scanbot.sdk.AspectRatio(21.0, 29.7)) // this a A4 format
        config.setLockDocumentAspectRatioToFinder(true) // allow only documents with finder aspect ratio to be scanned
        config.setAcceptedSizeScore(0.75) // allow only documents with at least 75% of finder to be scanned
        finderDocumentScannerResult.launch(config)
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

    private fun runPagesPreviewScreen(pages: List<Page>) {
        val intent = Intent(this, PagesPreviewActivity::class.java)
        val bundle = Bundle()
        intent.putExtra("pages", ArrayList(pages.map { it.pageId }))
        startActivity(intent)
    }

    private fun runSinglePreviewScreen(page: Page) {
        val intent = Intent(this, SinglePagePreviewActivity::class.java)
        intent.putExtra("page", page.pageId)
        startActivity(intent)
    }

    private fun runSinglePageScannerWithGuidance() {
        val config = DocumentScannerConfiguration()
        config.setMultiPageEnabled(false)
        config.setMultiPageButtonHidden(true)
        config.setTextHintBadAngles("Hold your phone parallel to the document")
        config.setTextHintOK("Hold your phone, steady, trying to scan")
        config.setTextHintBadAspectRatio("The document is not in the correct format")
        config.setTextHintTooDark("Its too dark, please add more light")
        config.setTextHintTooSmall("Document too small, please move closer")
        config.setTextHintTooNoisy("Image too noisy, please move to a better lit area")
        config.setTextHintNothingDetected("No document detected, please try again")
        documentScannerResult.launch(config)
    }


    private fun runSinglePageAutoSnap() {
        val config = DocumentScannerConfiguration()
        config.setAutoSnappingButtonTitle("Auto-Snap")
        config.setAutoSnappingButtonHidden(false)
        config.setAutoSnappingEnabled(true)
        documentScannerResult.launch(config)
    }
    
}
