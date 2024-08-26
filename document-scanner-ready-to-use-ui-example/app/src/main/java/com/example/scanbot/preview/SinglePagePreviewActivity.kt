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
import io.scanbot.imagefilters.ParametricFilter
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.persistence.Page
import io.scanbot.sdk.persistence.PageFileStorage
import io.scanbot.sdk.ui.registerForActivityResultOk
import io.scanbot.sdk.ui.view.edit.CroppingActivity
import io.scanbot.sdk.ui.view.edit.configuration.CroppingConfiguration
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

    private lateinit var imageView: ImageView
    private lateinit var exampleSingleton: ExampleSingletonImpl
    private lateinit var exportPdf: GeneratePdfForSharingUseCase
    private lateinit var exportJpeg: GenerateJpgForSharingUseCase
    private lateinit var exportPng: GeneratePngForSharingUseCase
    private lateinit var exportTiff: GenerateTiffForSharingUseCase
    private lateinit var page: Page

    private lateinit var filtersSheetFragment: FiltersBottomSheetMenuFragment
    private lateinit var saveSheetFragment: SaveBottomSheetMenuFragment
    private lateinit var scanbotSDK: ScanbotSDK
    private lateinit var progress: ProgressBar

    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    private val croppingResult: ActivityResultLauncher<CroppingConfiguration> =
        registerForActivityResultOk(CroppingActivity.ResultContract()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (result.result != null) {
                    page = result.result!!
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
        exportPdf = GeneratePdfForSharingUseCase(
            sharingDocumentStorage,
            exampleSingleton.pagePDFRenderer()
        )
        exportTiff = GenerateTiffForSharingUseCase(
            sharingDocumentStorage,
            exampleSingleton.pageFileStorageInstance(),
            exampleSingleton.pageTIFFWriter()
        )
        exportJpeg = GenerateJpgForSharingUseCase(
            sharingDocumentStorage,
            exampleSingleton.pageFileStorageInstance()
        )
        exportPng = GeneratePngForSharingUseCase(
            sharingDocumentStorage,
            exampleSingleton.pageFileStorageInstance()
        )

        scanbotSDK = ScanbotSDK(application)

        imageView = findViewById(R.id.image)

        val pagesId =
            intent.getStringExtra("page")
                ?: throw IllegalStateException("No page id")
        page = Page(pageId = pagesId)

        val actionFilter = findViewById<TextView>(R.id.action_filter)
        actionFilter.setOnClickListener {
            val fragment = supportFragmentManager.findFragmentByTag(FILTERS_MENU_TAG)
            if (fragment == null) {
                filtersSheetFragment.show(supportFragmentManager, FILTERS_MENU_TAG)
            }
        }
        val actionCrop = findViewById<TextView>(R.id.action_crop)

        actionCrop.setOnClickListener {
            runCroppingUseCase()
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
        if (!scanbotSDK.licenseInfo.isValid) {
            showLicenseToast()
        } else {
            progress.visibility = View.VISIBLE
            launch {
                val image = withContext(Dispatchers.Default) {
                    exampleSingleton.pageFileStorageInstance()
                        .getImage(page.pageId, PageFileStorage.PageFileType.DOCUMENT)
                }
                withContext(Dispatchers.Main) {
                    image?.let {

                        // Result is represented by `DocumentQualityResult` enum.
                        val analyzerResult =
                            exampleSingleton.pageDocQualityAnalyzer().analyzeInBitmap(image, 0)
                        val text = "Image quality: ${analyzerResult?.name ?: "UNKNOWN"}"
                        Toast.makeText(this@SinglePagePreviewActivity, text, Toast.LENGTH_LONG)
                            .show()

                        updateImageView()
                        progress.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun runCroppingUseCase() {
        if (!scanbotSDK.licenseInfo.isValid) {
            showLicenseToast()
            return
        }
        val configuration = CroppingConfiguration(page)
        // apply styling parameters by configuration.setColor() methods
        croppingResult.launch(configuration)
    }

    private fun updateImageView() {
        val image = exampleSingleton.pageFileStorageInstance()
            .getImage(page.pageId, PageFileStorage.PageFileType.DOCUMENT)
        progress = findViewById(R.id.progressBar)
        imageView.setImageBitmap(image)
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
        if (!scanbotSDK.licenseInfo.isValid) {
            showLicenseToast()
        } else {
            progress.visibility = View.VISIBLE
            launch {
                page = exampleSingleton.pageProcessorInstance().applyFilter(page, filter)
                withContext(Dispatchers.Main) {
                    updateImageView()
                    progress.visibility = View.GONE
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!scanbotSDK.licenseInfo.isValid) {
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
        if (!scanbotSDK.licenseInfo.isValid) {
            showLicenseToast()
        } else {
            progress.visibility = View.VISIBLE
            lifecycleScope.launch {
                val pdfFile: File? =
                    withContext(Dispatchers.Default) {
                        if (withJpeg) {
                            exportJpeg.generate(listOf(page.pageId)).firstOrNull()
                        } else {
                            exportPng.generate(listOf(page.pageId)).firstOrNull()
                        }
                    }

                withContext(Dispatchers.Main) {
                    progress.visibility = View.GONE

                    //open first document
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

    private fun saveDocumentPdf() {
        if (!scanbotSDK.licenseInfo.isValid) {
            showLicenseToast()
        } else {
            progress.visibility = View.VISIBLE
            lifecycleScope.launch {
                val pdfFile: File? = withContext(Dispatchers.Default) {
                    exportPdf.generate(listOf(page.pageId)).firstOrNull()
                }

                withContext(Dispatchers.Main) {
                    progress.visibility = View.GONE

                    //open first document
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
        if (!scanbotSDK.licenseInfo.isValid) {
            showLicenseToast()
        } else {
            progress.visibility = View.VISIBLE
            lifecycleScope.launch {
                val pdfFile: File? = withContext(Dispatchers.Default) {
                    exportTiff.generate(listOf(page.pageId)).firstOrNull()
                }

                withContext(Dispatchers.Main) {
                    progress.visibility = View.GONE

                    //open first document
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
