package io.scanbot.example

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.MemoryPolicy
import io.scanbot.example.di.ExampleSingletonImpl
import io.scanbot.example.fragments.ErrorFragment
import io.scanbot.example.fragments.FiltersBottomSheetMenuFragment
import io.scanbot.example.fragments.SaveBottomSheetMenuFragment
import io.scanbot.example.repository.PageRepository
import io.scanbot.example.util.PicassoHelper
import io.scanbot.example.util.SharingCopier
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.camera.CameraPreviewMode
import io.scanbot.sdk.ocr.OpticalCharacterRecognizer
import io.scanbot.sdk.persistence.Page
import io.scanbot.sdk.persistence.PageFileStorage
import io.scanbot.sdk.persistence.cleanup.Cleaner
import io.scanbot.sdk.process.ImageFilterType
import io.scanbot.sdk.process.PDFPageSize
import io.scanbot.sdk.process.PDFRenderer
import io.scanbot.sdk.ui.registerForActivityResultOk
import io.scanbot.sdk.ui.view.camera.DocumentScannerActivity
import io.scanbot.sdk.ui.view.camera.configuration.DocumentScannerConfiguration
import io.scanbot.sdk.util.thread.MimeUtils
import kotlinx.android.synthetic.main.activity_page_preview.*
import kotlinx.coroutines.*
import java.io.File
import kotlin.coroutines.CoroutineContext


class PagePreviewActivity : AppCompatActivity(), FiltersListener, SaveListener, CoroutineScope {

    private lateinit var adapter: PagesAdapter
    private lateinit var recycleView: RecyclerView

    companion object {
        private const val FILTERS_MENU_TAG = "FILTERS_MENU_TAG"
        private const val SAVE_MENU_TAG = "SAVE_MENU_TAG"

        var selectedPage: Page? = null
    }

    private lateinit var cleaner: Cleaner
    private lateinit var textRecognition: OpticalCharacterRecognizer
    private lateinit var pdfRenderer: PDFRenderer
    private lateinit var filtersSheetFragment: FiltersBottomSheetMenuFragment
    private lateinit var saveSheetFragment: SaveBottomSheetMenuFragment
    private lateinit var scanbotSDK: ScanbotSDK
    lateinit var progress: ProgressBar

    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    private val documentScannerResultLauncher =
            registerForActivityResultOk(DocumentScannerActivity.ResultContract()) { resultEntity ->
                PageRepository.addPages(resultEntity.result!!)
                adapter.setItems(PageRepository.getPages())
                checkVisibility()
            }

    private val filtersActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
                adapter.setItems(PageRepository.getPages())
                adapter.notifyDataSetChanged()
            }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_preview)
        initActionBar()
        initMenu()
        scanbotSDK = ScanbotSDK(application)

        cleaner = scanbotSDK.createCleaner()
        textRecognition = scanbotSDK.createOcrRecognizer()
        pdfRenderer = scanbotSDK.createPdfRenderer()

        adapter = PagesAdapter(ExampleSingletonImpl(this).pageFileStorageInstance())
        adapter.setHasStableIds(true)

        recycleView = findViewById(R.id.pages_preview)
        recycleView.setHasFixedSize(true)
        recycleView.adapter = adapter

        val layoutManager = GridLayoutManager(this, 3)
        recycleView.layoutManager = layoutManager

        // initialize items only once, so we can update items from onActivityResult
        adapter.setItems(PageRepository.getPages())

        progress = findViewById(R.id.progressBar)
        findViewById<View>(R.id.action_add_page).setOnClickListener {
            val cameraConfiguration = DocumentScannerConfiguration()
            cameraConfiguration.setCameraPreviewMode(CameraPreviewMode.FILL_IN)
            cameraConfiguration.setIgnoreBadAspectRatio(true)
            cameraConfiguration.setBottomBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
            cameraConfiguration.setBottomBarButtonsColor(ContextCompat.getColor(this, R.color.greyColor))
            cameraConfiguration.setTopBarButtonsActiveColor(ContextCompat.getColor(this, android.R.color.white))
            cameraConfiguration.setCameraBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
            cameraConfiguration.setUserGuidanceBackgroundColor(ContextCompat.getColor(this, android.R.color.black))
            cameraConfiguration.setUserGuidanceTextColor(ContextCompat.getColor(this, android.R.color.white))

            documentScannerResultLauncher.launch(cameraConfiguration)
        }
        action_delete_all.setOnClickListener {
            PageRepository.clearPages(this)
            adapter.notifyDataSetChanged()
            action_save_document.isEnabled = false
            action_delete_all.isEnabled = false
            action_filter.isEnabled = false
        }
        action_filter.setOnClickListener {
            val fragment = supportFragmentManager.findFragmentByTag(FILTERS_MENU_TAG)
            if (fragment == null) {
                filtersSheetFragment.show(supportFragmentManager, FILTERS_MENU_TAG)
            }
        }

        action_save_document.setOnClickListener {
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
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar!!.title = getString(R.string.scan_results)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
    }

    override fun onFilterApplied(filterType: ImageFilterType) {
        applyFilter(filterType)
    }

    override fun saveWithOcr() {
        saveDocument(true)
    }

    override fun saveWithOutOcr() {
        saveDocument(false)
    }

    private fun applyFilter(imageFilterType: ImageFilterType) {
        if (!scanbotSDK.licenseInfo.isValid) {
            showLicenseDialog()
        } else {
            progress.visibility = View.VISIBLE
            launch {
                PageRepository.applyFilter(this@PagePreviewActivity, imageFilterType)
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
            showLicenseDialog()
        }
        checkVisibility()
    }

    private fun checkVisibility() {
        action_save_document.isEnabled = adapter.items.isNotEmpty()
        action_delete_all.isEnabled = adapter.items.isNotEmpty()
        action_filter.isEnabled = adapter.items.isNotEmpty()
    }

    private fun showLicenseDialog() {
        if (supportFragmentManager.findFragmentByTag(ErrorFragment.NAME) == null) {
            val dialogFragment = ErrorFragment.newInstance()
            dialogFragment.show(supportFragmentManager, ErrorFragment.NAME)
        }
    }

    private fun saveDocument(withOcr: Boolean) {
        if (!scanbotSDK.licenseInfo.isValid) {
            showLicenseDialog()
        } else {
            progress.visibility = View.VISIBLE
            launch {
                var pdfFile: File? =
                        if (withOcr) {
                            textRecognition.recognizeTextWithPdfFromPages(adapter.items, PDFPageSize.AUTO, textRecognition.getInstalledLanguages()).sandwichedPdfDocumentFile
                        } else {
                            pdfRenderer.renderDocumentFromPages(adapter.items, PDFPageSize.AUTO)
                        }
                if (pdfFile != null) {
                    pdfFile = SharingCopier.moveFile(this@PagePreviewActivity, pdfFile)
                }

                withContext(Dispatchers.Main) {
                    progress.visibility = View.GONE

                    //open first document
                    if (pdfFile != null) {
                        if (!Application.USE_ENCRYPTION) {
                            openDocument(pdfFile)
                        } else {
                            showEncryptedDocumentToast(pdfFile)
                        }
                    }
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                super.onBackPressed()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private inner class PagesAdapter(val pageFileStorage: PageFileStorage) : RecyclerView.Adapter<PageViewHolder>() {
        val items: MutableList<Page> = mutableListOf()
        private val mOnClickListener: View.OnClickListener = PageClickListener()
        fun setItems(pages: List<Page>) {
            items.clear()
            items.addAll(pages)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_page, parent, false)
            view.setOnClickListener(mOnClickListener)
            return PageViewHolder(view)
        }

        override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
            val page = items[position]

            val imagePath = pageFileStorage.getPreviewImageURI(page.pageId, PageFileStorage.PageFileType.DOCUMENT).path
            val originalImagePath = pageFileStorage.getPreviewImageURI(page.pageId, PageFileStorage.PageFileType.ORIGINAL).path
            val fileToShow = if (File(imagePath).exists()) File(imagePath) else File(originalImagePath)
            PicassoHelper.with(applicationContext)
                    .load(fileToShow)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .resizeDimen(R.dimen.move_preview_size, R.dimen.move_preview_size)
                    .centerInside()
                    .into(holder.imageView)

        }

        override fun getItemId(position: Int): Long {
            return items[position].hashCode().toLong()
        }

        override fun getItemCount(): Int {
            return items.size
        }
    }

    inner class PageClickListener : View.OnClickListener {
        override fun onClick(v: View) {
            val itemPosition = recycleView.getChildLayoutPosition(v)
            selectedPage = adapter.items[itemPosition]

            val intent = PageFiltersActivity.newIntent(this@PagePreviewActivity, selectedPage!!)
            filtersActivityResultLauncher.launch(intent)
        }
    }

    private fun openDocument(pdfFile: File) {
        val uriForFile = androidx.core.content.FileProvider.getUriForFile(this@PagePreviewActivity,
                this@PagePreviewActivity.applicationContext.packageName + ".provider", pdfFile)
        val openIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uriForFile)
            type = MimeUtils.getMimeByName(pdfFile.name)
        }
        openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        if (openIntent.resolveActivity(packageManager) != null) {
            val chooser = Intent.createChooser(openIntent, pdfFile.name)
            val resInfoList = this.packageManager.queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY)

            for (resolveInfo in resInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                grantUriPermission(packageName, uriForFile, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(chooser)

        } else {
            Toast.makeText(this@PagePreviewActivity, getString(R.string.error_openning_document), Toast.LENGTH_LONG).show()
        }
    }

    private fun showEncryptedDocumentToast(pdfFile: File) {
        // TODO: scanbotSDK.fileIOProcessor().openFileInputStream() you need to use this call to unencrypt the pdf file
        Toast.makeText(this@PagePreviewActivity, getString(R.string.encrypted_document_saved, pdfFile.toString()), Toast.LENGTH_LONG).show()
    }

    /**
     * View holder for page and its number.
     */
    private inner class PageViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal val imageView: ImageView = itemView.findViewById<View>(R.id.page) as ImageView

    }
}
