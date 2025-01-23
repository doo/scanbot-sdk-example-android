package com.example.scanbot.preview

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.scanbot.Const
import com.example.scanbot.ExampleApplication
import com.example.scanbot.di.ExampleSingletonImpl
import com.example.scanbot.sharing.SaveListener
import com.example.scanbot.sharing.SharingDocumentStorage
import com.example.scanbot.usecases.GenerateJpgForSharingUseCase
import com.example.scanbot.usecases.GeneratePdfForSharingUseCase
import com.example.scanbot.usecases.GeneratePngForSharingUseCase
import com.example.scanbot.usecases.GenerateTiffForSharingUseCase
import com.example.scanbot.utils.ExampleUtils
import com.example.scanbot.utils.ExampleUtils.showEncryptedDocumentToast
import io.scanbot.sdk.imagefilters.ParametricFilter
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.docprocessing.Document
import io.scanbot.sdk.docprocessing.Page
import io.scanbot.sdk.ui_v2.common.activity.registerForActivityResultOk
import io.scanbot.sdk.ui_v2.document.CroppingActivity
import io.scanbot.sdk.ui_v2.document.configuration.CroppingConfiguration
import io.scanbot.sdk.usecases.documents.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.CoroutineContext

class SinglePagePreviewActivity : AppCompatActivity(), FiltersListener, SaveListener,
    CoroutineScope {

    private val scanbotSdk by lazy { ScanbotSDK(application) }

    private lateinit var imageView: ImageView
    private lateinit var exampleSingleton: ExampleSingletonImpl
    private lateinit var exportPdf: GeneratePdfForSharingUseCase
    private lateinit var exportJpeg: GenerateJpgForSharingUseCase
    private lateinit var exportPng: GeneratePngForSharingUseCase
    private lateinit var exportTiff: GenerateTiffForSharingUseCase
    private lateinit var document: Document
    private lateinit var page: Page

    private lateinit var filtersSheetFragment: FiltersBottomSheetMenuFragment
    private lateinit var saveSheetFragment: SaveBottomSheetMenuFragment
    private lateinit var progress: ProgressBar

    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    private val croppingResult: ActivityResultLauncher<CroppingConfiguration> =
        registerForActivityResultOk(CroppingActivity.ResultContract()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.result?.let { croppingResult ->
                    document = scanbotSdk.documentApi.loadDocument(croppingResult.documentUuid)
                        ?: throw IllegalStateException("No such document!")
                    page = document.pages.firstOrNull()
                        ?: throw IllegalStateException("No pages in document!")
                    updateImageView()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_preview)

        initActionBar()
        initMenu()

        exampleSingleton = ExampleSingletonImpl(this)
        val sharingDocumentStorage = SharingDocumentStorage(this)

        exportPdf =
            GeneratePdfForSharingUseCase(sharingDocumentStorage, exampleSingleton.pagePDFRenderer())
        exportTiff = GenerateTiffForSharingUseCase(
            sharingDocumentStorage,
            exampleSingleton.pageTIFFWriter(),
        )
        exportJpeg = GenerateJpgForSharingUseCase(sharingDocumentStorage)
        exportPng =
            GeneratePngForSharingUseCase(sharingDocumentStorage, scanbotSdk.fileIOProcessor())

        imageView = findViewById(R.id.image)
        progress = findViewById(R.id.progress_bar)

        val docId = intent.getStringExtra(Const.EXTRA_DOCUMENT_ID)
            ?: throw IllegalStateException("No document id!")
        document = scanbotSdk.documentApi.loadDocument(docId)
            ?: throw IllegalStateException("No such document!")
        page = document.pages.firstOrNull() ?: throw IllegalStateException("No pages in document!")

        val actionFilter = findViewById<TextView>(R.id.action_filter)
        actionFilter.setOnClickListener {
            val fragment = supportFragmentManager.findFragmentByTag(FILTERS_MENU_TAG)
            if (fragment == null) {
                filtersSheetFragment.show(supportFragmentManager, FILTERS_MENU_TAG)
            }
        }
        val actionCrop = findViewById<TextView>(R.id.action_crop)

        actionCrop.setOnClickListener {
            runCroppingScreen()
        }

        val actionDocQualityAnalyzer = findViewById<TextView>(R.id.action_doc_quality_analyzer)
        actionDocQualityAnalyzer.setOnClickListener {
            runDocumentQualityAnalyzer()
        }

        val export = findViewById<TextView>(R.id.action_export)
        export.setOnClickListener {
            val fragment = supportFragmentManager.findFragmentByTag(SAVE_MENU_TAG)
            if (fragment == null) {
                saveSheetFragment.show(supportFragmentManager, SAVE_MENU_TAG)
            }
        }
    }

    private fun runDocumentQualityAnalyzer() {
        if (!scanbotSdk.licenseInfo.isValid) {
            showLicenseToast()
        } else {
            progress.visibility = View.VISIBLE
            lifecycleScope.launch {
                val imageQualityResult = withContext(Dispatchers.Default) {
                    // Result is represented by `DocumentQuality` enum.
                    page.documentImage?.let {
                        exampleSingleton.pageDocQualityAnalyzer().analyzeInBitmap(it, 0)
                    }
                }
                withContext(Dispatchers.Main) {
                    imageQualityResult?.let { qualityResult ->
                        val text = "Image quality: ${qualityResult.name}"
                        Toast.makeText(this@SinglePagePreviewActivity, text, Toast.LENGTH_LONG)
                            .show()

                        updateImageView()
                        progress.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun runCroppingScreen() {
        if (!scanbotSdk.licenseInfo.isValid) {
            showLicenseToast()
            return
        }

        val configuration =
            CroppingConfiguration(documentUuid = document.uuid, pageUuid = page.uuid)
        croppingResult.launch(configuration)
    }

    private fun updateImageView() {
        imageView.setImageBitmap(page.documentImage)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    private fun initMenu() {
        val fragment = supportFragmentManager.findFragmentByTag(FILTERS_MENU_TAG)
        if (fragment != null) {
            supportFragmentManager
                .beginTransaction()
                .remove(fragment)
                .commitNow()
        }

        filtersSheetFragment = FiltersBottomSheetMenuFragment()

        val fragment2 = supportFragmentManager.findFragmentByTag(SAVE_MENU_TAG)
        if (fragment2 != null) {
            supportFragmentManager
                .beginTransaction()
                .remove(fragment2)
                .commitNow()
        }

        saveSheetFragment = SaveBottomSheetMenuFragment(true)
    }

    private fun initActionBar() {
        supportActionBar?.title = getString(R.string.scan_results)
    }

    override fun onFilterApplied(filter: ParametricFilter) {
        applyFilter(filter)
    }

    override fun savePdf() {
        saveDocumentPdf()
    }

    override fun saveTiff() {
        saveDocumentTiff()
    }

    override fun saveJpeg() {
        saveDocumentImage(true)
    }

    override fun savePng() {
        saveDocumentImage(false)
    }

    private fun applyFilter(filter: ParametricFilter) {
        if (!scanbotSdk.licenseInfo.isValid) showLicenseToast()

        progress.visibility = View.VISIBLE
        lifecycleScope.launch {
            page.apply(newFilters = listOf(filter))
            withContext(Dispatchers.Main) {
                updateImageView()
                progress.visibility = View.GONE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!scanbotSdk.licenseInfo.isValid) {
            showLicenseToast()
            return
        }
        updateImageView()
    }


    private fun showLicenseToast() {
        Toast.makeText(this@SinglePagePreviewActivity, "License is not valid!", Toast.LENGTH_LONG)
            .show()
    }

    private fun saveDocumentImage(withJpeg: Boolean) {
        if (!scanbotSdk.licenseInfo.isValid) {
            showLicenseToast()
        } else {
            progress.visibility = View.VISIBLE
            lifecycleScope.launch {
                val file: File? =
                    withContext(Dispatchers.Default) {
                        if (withJpeg) {
                            exportJpeg.generate(document).firstOrNull()
                        } else {
                            exportPng.generate(document).firstOrNull()
                        }
                    }

                withContext(Dispatchers.Main) {
                    progress.visibility = View.GONE

                    if (file != null) {
                        if (!ExampleApplication.USE_ENCRYPTION) {
                            ExampleUtils.openDocument(this@SinglePagePreviewActivity, file)
                        } else {
                            ExampleUtils.showEncryptedDocumentToast(
                                this@SinglePagePreviewActivity,
                                file,
                                exampleSingleton.fileIOProcessor()
                            )
                        }
                    }
                }
            }
        }
    }

    private fun saveDocumentPdf() {
        if (!scanbotSdk.licenseInfo.isValid) {
            showLicenseToast()
        } else {
            progress.visibility = View.VISIBLE
            lifecycleScope.launch {
                val pdfFile: File? = withContext(Dispatchers.Default) {
                    exportPdf.generate(document).firstOrNull()
                }

                withContext(Dispatchers.Main) {
                    progress.visibility = View.GONE

                    if (pdfFile != null) {
                        if (!ExampleApplication.USE_ENCRYPTION) {
                            ExampleUtils.openDocument(this@SinglePagePreviewActivity, pdfFile)
                        } else {
                            showEncryptedDocumentToast(
                                this@SinglePagePreviewActivity,
                                pdfFile,
                                exampleSingleton.fileIOProcessor()
                            )
                        }
                    }
                }
            }
        }
    }

    private fun saveDocumentTiff() {
        if (!scanbotSdk.licenseInfo.isValid) {
            showLicenseToast()
        } else {
            progress.visibility = View.VISIBLE
            lifecycleScope.launch {
                val pdfFile: File? = withContext(Dispatchers.Default) {
                    exportTiff.generate(document).firstOrNull()
                }

                withContext(Dispatchers.Main) {
                    progress.visibility = View.GONE

                    if (pdfFile != null) {
                        if (!ExampleApplication.USE_ENCRYPTION) {
                            ExampleUtils.openDocument(this@SinglePagePreviewActivity, pdfFile)
                        } else {
                            showEncryptedDocumentToast(
                                this@SinglePagePreviewActivity,
                                pdfFile,
                                exampleSingleton.fileIOProcessor()
                            )
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val FILTERS_MENU_TAG = "FILTERS_MENU_TAG"
        private const val SAVE_MENU_TAG = "SAVE_MENU_TAG"
    }
}
