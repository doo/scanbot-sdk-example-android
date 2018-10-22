package io.scanbot.example

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import io.scanbot.example.fragments.ErrorFragment
import io.scanbot.example.fragments.FiltersBottomSheetMenuFragment
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.persistence.Page
import io.scanbot.sdk.persistence.PageFileStorage
import io.scanbot.sdk.process.ImageFilterType
import io.scanbot.sdk.ui.view.camera.configuration.DocumentScannerConfiguration
import kotlinx.android.synthetic.main.activity_page_preview.*
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.launch
import net.doo.snap.camera.CameraPreviewMode
import net.doo.snap.entity.SnappingDraft
import net.doo.snap.lib.detector.ContourDetector
import net.doo.snap.persistence.PageFactory
import net.doo.snap.persistence.cleanup.Cleaner
import net.doo.snap.process.DocumentProcessingResult
import net.doo.snap.process.DocumentProcessor
import net.doo.snap.process.draft.DocumentDraftExtractor
import net.doo.snap.util.bitmap.BitmapUtils
import net.doo.snap.util.thread.MimeUtils
import java.io.File
import java.io.IOException
import java.util.*


class PagePreviewActivity : AppCompatActivity(), FiltersListener {
    private lateinit var adapter: PagesAdapter
    private lateinit var recycleView: RecyclerView

    companion object {
        const val FILTER_UI_REQUEST_CODE = 7777
        private const val CAMERA_ACTIVITY: Int = 8888
        private const val FILTERS_MENU_TAG = "FILTERS_MENU_TAG"

        var selectedPage: Page? = null
    }

    private lateinit var pageFactory: PageFactory
    private lateinit var documentDraftExtractor: DocumentDraftExtractor
    private lateinit var documentProcessor: DocumentProcessor
    private lateinit var cleaner: Cleaner
    private lateinit var filtersSheetFragment: FiltersBottomSheetMenuFragment
    private lateinit var scanbotSDK: ScanbotSDK
    lateinit var progress: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_preview)
        initActionBar()
        initMenu()
        scanbotSDK = ScanbotSDK(application)

        pageFactory = scanbotSDK.pageFactory()
        documentDraftExtractor = scanbotSDK.documentDraftExtractor()
        documentProcessor = scanbotSDK.documentProcessor()
        cleaner = scanbotSDK.cleaner()

        adapter = PagesAdapter()
        adapter.setHasStableIds(true)

        recycleView = findViewById(R.id.pages_preview)
        recycleView.setHasFixedSize(true)
        recycleView.adapter = adapter

        val layoutManager = GridLayoutManager(this, 3)
        recycleView.layoutManager = layoutManager

        // initialize items only once, so we can update items from onActivityResult
        adapter.setItems(scanbotSDK.pageFileStorage().getStoredPages().map { id -> Page(id) })

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

            val intent = io.scanbot.sdk.ui.view.camera.DocumentScannerActivity.newIntent(this@PagePreviewActivity,
                    cameraConfiguration
            )
            startActivityForResult(intent, CAMERA_ACTIVITY)
        }
        findViewById<View>(R.id.action_delete_all).setOnClickListener {
            ScanbotSDK(this).pageFileStorage().removeAll()
            adapter.notifyDataSetChanged()
            action_save_document.isEnabled = false
        }
        findViewById<View>(R.id.action_filter).setOnClickListener {
            val fragment = supportFragmentManager.findFragmentByTag(FILTERS_MENU_TAG)
            if (fragment == null) {
                filtersSheetFragment.show(supportFragmentManager, FILTERS_MENU_TAG)
            }
        }

        findViewById<Button>(R.id.action_save_document).setOnClickListener {
            saveDocument()
        }

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
    }


    private fun initActionBar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar!!.title = "Scan Results"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
    }

    override fun cleanBackgroundFilter() {
        applyFilter(ImageFilterType.BACKGROUND_CLEAN)
    }

    override fun colorDocumentFilter() {
        applyFilter(ImageFilterType.COLOR_DOCUMENT)
    }

    override fun colorFilter() {
        applyFilter(ImageFilterType.COLOR_ENHANCED)
    }

    override fun grayscaleFilter() {
        applyFilter(ImageFilterType.GRAYSCALE)
    }

    override fun binarizedFilter() {
        applyFilter(ImageFilterType.BINARIZED)
    }

    override fun pureBinarizedFilter() {
        applyFilter(ImageFilterType.PURE_BINARIZED)
    }

    override fun blackAndWhiteFilter() {
        applyFilter(ImageFilterType.BLACK_AND_WHITE)
    }

    override fun noneFilter() {
        applyFilter(ImageFilterType.NONE)
    }

    private fun applyFilter(imageFilterType: ImageFilterType) {
        if (!scanbotSDK.isLicenseValid) {
            showLicenseDialog()
        } else {
            progress.visibility = View.VISIBLE
            GlobalScope.launch(Dispatchers.Default, CoroutineStart.DEFAULT, {
                adapter.items.forEach {
                    scanbotSDK.pageProcessor().applyFilter(it, imageFilterType)
                }
                Handler(Looper.getMainLooper()).post {
                    adapter.notifyDataSetChanged()
                    progress.visibility = View.GONE
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        if (!scanbotSDK.isLicenseValid) {
            showLicenseDialog()
        }
        action_save_document.isEnabled = !adapter.items.isEmpty()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILTER_UI_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val page = data!!.getParcelableExtra<Page>(PageFiltersActivity.PAGE_DATA)
            adapter.updateItem(page)
            return
        }

        if (requestCode == CAMERA_ACTIVITY) {
            adapter.setItems(scanbotSDK.pageFileStorage().getStoredPages().map { id -> Page(id) })
        }

        if (requestCode == FILTER_UI_REQUEST_CODE) {
            adapter.setItems(scanbotSDK.pageFileStorage().getStoredPages().map { id -> Page(id) })
        }
    }

    private fun showLicenseDialog() {
        if (supportFragmentManager.findFragmentByTag(ErrorFragment.NAME) == null) {
            val dialogFragment = ErrorFragment.newInstanse()
            dialogFragment.show(supportFragmentManager, ErrorFragment.NAME)
        }
    }

    private fun saveDocument() {
        if (!scanbotSDK.isLicenseValid) {
            showLicenseDialog()
        } else {
            progress.visibility = View.VISIBLE
            GlobalScope.launch(Dispatchers.Default, CoroutineStart.DEFAULT, {
                val results = ArrayList<DocumentProcessingResult>()

                val pages = mutableListOf<net.doo.snap.entity.Page>()
                adapter.items.forEach {
                    try {
                        val bitmap = loadImage(it)
                        val result = applyFilters(bitmap)

                        val page = pageFactory.buildPage(result, resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels).page
                        pages.add(pages.size, page)
                    } catch (e: IOException) {
                        throw RuntimeException(e)
                    }
                }

                val snappingDraft = SnappingDraft(*pages.toTypedArray())
                val drafts = documentDraftExtractor.extract(snappingDraft)

                for (draft in drafts) {
                    try {
                        results.add(documentProcessor.processDocument(draft))
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }

                cleaner.cleanUp()

                Handler(Looper.getMainLooper()).post {
                    progress.visibility = View.GONE

                    //open first document
                    if (results.isNotEmpty()) {
                        openDocument(results[0])
                    }
                }
            })
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


    private inner class PagesAdapter : RecyclerView.Adapter<PageViewHolder>() {

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

            val imagePath = ScanbotSDK(applicationContext).pageFileStorage().getPreviewImageURI(page.pageId, PageFileStorage.PageFileType.DOCUMENT).path
            val originalImagePath = ScanbotSDK(applicationContext).pageFileStorage().getPreviewImageURI(page.pageId, PageFileStorage.PageFileType.ORIGINAL).path
            val fileToShow = if (File(imagePath).exists()) File(imagePath) else File(originalImagePath)
            Picasso.with(applicationContext)
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

        fun updateItem(page: Page) {
            val renewedItems: MutableList<Page> = mutableListOf()
            for (item in items) {
                if (item.pageId != page.pageId) {
                    renewedItems.add(item)
                }
            }
            renewedItems.add(page)
            setItems(renewedItems)
        }
    }

    inner class PageClickListener : View.OnClickListener {
        override fun onClick(v: View?) {
            val itemPosition = recycleView.getChildLayoutPosition(v)
            selectedPage = adapter.items[itemPosition]

            val intent = PageFiltersActivity.newIntent(this@PagePreviewActivity, selectedPage!!)
            startActivityForResult(intent, FILTER_UI_REQUEST_CODE)
        }
    }

    private fun applyFilters(bitmap: Bitmap): Bitmap {
        val detector = ContourDetector()
        detector.detect(bitmap)
        val polygon = detector.polygonF

        /*
         * This operation crops original bitmap and creates a new one. Old bitmap is recycled
         * and can't be used anymore. If that's not what you need, use processImageF() instead
         */
        return detector.processImageAndRelease(bitmap, polygon, ContourDetector.IMAGE_FILTER_GRAY)
    }

    @Throws(IOException::class)
    private fun loadImage(page: Page): Bitmap {
        val imagePath = ScanbotSDK(applicationContext).pageFileStorage().getPreviewImageURI(page.pageId, PageFileStorage.PageFileType.ORIGINAL).path

        return BitmapUtils.decodeQuietly(imagePath, null) ?: throw IOException("Bitmap is null")
    }

    private fun openDocument(documentProcessingResult: DocumentProcessingResult) {
        val document = documentProcessingResult.document
        val documentFile = documentProcessingResult.documentFile

        val openIntent = Intent()
        openIntent.action = Intent.ACTION_VIEW
        openIntent.setDataAndType(
                FileProvider.getUriForFile(this, this.applicationContext.packageName + ".provider", documentFile),
                MimeUtils.getMimeByName(document.name)
        )
        openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        if (openIntent.resolveActivity(packageManager) != null) {
            startActivity(openIntent)
        } else {
            Toast.makeText(this@PagePreviewActivity, "Error while opening the document", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * View holder for page and its number.
     */
    private inner class PageViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal val imageView: ImageView = itemView.findViewById<View>(R.id.page) as ImageView

    }
}