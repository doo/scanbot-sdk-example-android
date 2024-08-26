package com.example.scanbot.preview

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.scanbot.ExampleApplication
import com.example.scanbot.di.ExampleSingletonImpl
import com.example.scanbot.sharing.SaveListener
import com.example.scanbot.sharing.SharingDocumentStorage
import com.example.scanbot.usecases.GeneratePdfForSharingUseCase
import com.example.scanbot.usecases.GenerateTiffForSharingUseCase
import com.example.scanbot.utils.ExampleUtils
import io.scanbot.imagefilters.ParametricFilter
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.persistence.Page
import io.scanbot.sdk.persistence.PageFileStorage
import io.scanbot.sdk.persistence.cleanup.Cleaner
import io.scanbot.sdk.usecases.documents.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.CoroutineContext

class PagesPreviewActivity : AppCompatActivity(), FiltersListener, SaveListener, CoroutineScope {

    private lateinit var adapter: PagesAdapter
    private lateinit var recycleView: RecyclerView
    private lateinit var exampleSingleton: ExampleSingletonImpl
    private lateinit var exportPdf: GeneratePdfForSharingUseCase
    private lateinit var exportTiff: GenerateTiffForSharingUseCase

    private lateinit var cleaner: Cleaner

    private lateinit var filtersSheetFragment: FiltersBottomSheetMenuFragment
    private lateinit var saveSheetFragment: SaveBottomSheetMenuFragment
    private lateinit var scanbotSDK: ScanbotSDK
    private lateinit var progress: ProgressBar

    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pages_preview)
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
        scanbotSDK = ScanbotSDK(application)
        cleaner = scanbotSDK.createCleaner()


        adapter = PagesAdapter(ExampleSingletonImpl(this).pageFileStorageInstance())
        adapter.setHasStableIds(true)

        recycleView = findViewById(R.id.pages_preview)
        recycleView.setHasFixedSize(true)
        recycleView.adapter = adapter

        val layoutManager = GridLayoutManager(this, 3)
        recycleView.layoutManager = layoutManager
        val pages =
            intent.getStringArrayListExtra("pages")?.map { Page(pageId = it) }
                ?: emptyList()
        // initialize items only once, so we can update items from onActivityResult
        adapter.setItems(pages)

        progress = findViewById(R.id.progressBar)
        val actionFilter = findViewById<TextView>(R.id.action_filter)
        actionFilter.setOnClickListener {
            val fragment = supportFragmentManager.findFragmentByTag(FILTERS_MENU_TAG)
            if (fragment == null) {
                filtersSheetFragment.show(supportFragmentManager, FILTERS_MENU_TAG)
            }
        }
        val export = findViewById<TextView>(R.id.action_export)
        export.setOnClickListener {
            val fragment = supportFragmentManager.findFragmentByTag(SAVE_MENU_TAG)
            if (fragment == null) {
                saveSheetFragment.show(supportFragmentManager, SAVE_MENU_TAG)
            }
        }
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

        saveSheetFragment = SaveBottomSheetMenuFragment()
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

    private fun applyFilter(filter: ParametricFilter) {
        if (!scanbotSDK.licenseInfo.isValid) {
            showLicenseToast()
        } else {
            progress.visibility = View.VISIBLE
            launch {
                adapter.items.forEach { page ->
                    withContext(Dispatchers.Default) {
                        exampleSingleton.pageProcessorInstance().applyFilter(page, filter)
                    }
                }

                withContext(Dispatchers.Main) {
                    adapter.notifyDataSetChanged()
                    progress.visibility = View.GONE
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!scanbotSDK.licenseInfo.isValid) {
            showLicenseToast()
        }
    }


    private fun showLicenseToast() {
        Toast.makeText(this@PagesPreviewActivity, "License is not valid!", Toast.LENGTH_LONG).show()
    }

    private fun saveDocumentPdf() {
        if (!scanbotSDK.licenseInfo.isValid) {
            showLicenseToast()
        } else {
            progress.visibility = View.VISIBLE
            lifecycleScope.launch {
                val pdfFile: File? =
                    withContext(Dispatchers.Default) {
                        exportPdf.generate(adapter.items.map { it.pageId }).firstOrNull()
                    }

                withContext(Dispatchers.Main) {
                    progress.visibility = View.GONE

                    //open first document
                    if (pdfFile != null) {
                        if (!ExampleApplication.USE_ENCRYPTION) {
                            ExampleUtils.openDocument(this@PagesPreviewActivity, pdfFile)
                        } else {
                            showEncryptedDocumentToast(pdfFile)
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
                val pdfFile: File? =
                    withContext(Dispatchers.Default) {
                        exportTiff.generate(adapter.items.map { it.pageId }).firstOrNull()
                    }

                withContext(Dispatchers.Main) {
                    progress.visibility = View.GONE

                    //open first document
                    if (pdfFile != null) {
                        if (!ExampleApplication.USE_ENCRYPTION) {
                            ExampleUtils.openDocument(this@PagesPreviewActivity, pdfFile)
                        } else {
                            showEncryptedDocumentToast(pdfFile)
                        }
                    }
                }
            }
        }
    }

    private inner class PagesAdapter(val pageFileStorage: PageFileStorage) :
        RecyclerView.Adapter<PageViewHolder>() {
        val items: MutableList<Page> = mutableListOf()
        fun setItems(pages: List<Page>) {
            items.clear()
            items.addAll(pages)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_page, parent, false)
            return PageViewHolder(view)
        }

        override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
            val page = items[position]

            val imagePath = pageFileStorage.getPreviewImageURI(
                page.pageId,
                PageFileStorage.PageFileType.DOCUMENT
            ).path
            val originalImagePath = pageFileStorage.getPreviewImageURI(
                page.pageId,
                PageFileStorage.PageFileType.ORIGINAL
            ).path
            val fileToShow =
                if (File(imagePath).exists()) File(imagePath) else File(originalImagePath)
            BitmapFactory.decodeFile(fileToShow.path)?.let {
                holder.imageView.setImageBitmap(it)
            }
        }

        override fun getItemId(position: Int): Long {
            return items[position].hashCode().toLong()
        }

        override fun getItemCount(): Int {
            return items.size
        }
    }

    private fun showEncryptedDocumentToast(pdfFile: File) {
        // TODO: scanbotSDK.fileIOProcessor().openFileInputStream() you need to use this call to unencrypt the pdf file
        Toast.makeText(this@PagesPreviewActivity, "Encrypted document saved!", Toast.LENGTH_LONG)
            .show()
    }

    /**
     * View holder for page and its number.
     */
    private inner class PageViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val imageView: ImageView = itemView.findViewById<View>(R.id.page) as ImageView

    }

    companion object {
        private const val FILTERS_MENU_TAG = "FILTERS_MENU_TAG"
        private const val SAVE_MENU_TAG = "SAVE_MENU_TAG"
    }
}
