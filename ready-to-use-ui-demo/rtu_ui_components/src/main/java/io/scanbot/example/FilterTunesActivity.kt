package io.scanbot.example

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.*
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.squareup.picasso.Callback
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import io.scanbot.example.fragments.ErrorFragment
import io.scanbot.example.fragments.FiltersBottomSheetMenuFragment
import io.scanbot.example.repository.PageRepository
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.persistence.Page
import io.scanbot.sdk.process.ImageFilterTuneType
import io.scanbot.sdk.process.ImageFilterType
import io.scanbot.sdk.process.TuneOperation
import kotlinx.android.synthetic.main.activity_filters.image
import kotlinx.android.synthetic.main.activity_filters.progress
import kotlinx.android.synthetic.main.activity_filters_tunes.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.CoroutineContext

class FilterTunesActivity : AppCompatActivity(), FiltersListener, CoroutineScope {
    companion object {
        const val PAGE_DATA = "PAGE_DATA"
        private const val FILTERS_MENU_TAG = "FILTERS_MENU_TAG"

        @JvmStatic
        fun newIntent(context: Context, page: Page): Intent {
            val intent = Intent(context, FilterTunesActivity::class.java)
            intent.putExtra(PAGE_DATA, (page as Parcelable))
            return intent
        }
    }


    lateinit var selectedPage: Page
    var selectedFilter: ImageFilterType = ImageFilterType.NONE
    var tunes: LinkedHashMap<ImageFilterTuneType, TuneOperation> = linkedMapOf()
    lateinit var scanbotSDK: ScanbotSDK
    private lateinit var filtersSheetFragment: FiltersBottomSheetMenuFragment

    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filters_tunes)
        initActionBar()

        selectedPage = PageRepository.getPages().find {
            it.pageId == (intent.getParcelableExtra(PAGE_DATA) as Page).pageId
        }!!

        selectedPage.let {
            selectedFilter = it.filter
            tunes = LinkedHashMap(it.tunes.groupBy { it.tuneType }.mapValues { it.value.first() })
        }

        filter_value.text = getFilterName()

        scanbotSDK = ScanbotSDK(application)

        filters_inner_layout.setOnClickListener {
            filtersSheetFragment.show(supportFragmentManager, "CHOOSE_FILTERS_DIALOG_TAG")
        }

        cancel.setOnClickListener {
            removeFilteredPreview()
            finish()
        }

        done.setOnClickListener {
            progress.visibility = View.VISIBLE
            launch {
                selectedPage = PageRepository.applyFilter(this@FilterTunesActivity, selectedPage, selectedFilter, tunes.values.toList())
                PageRepository.generatePreview(this@FilterTunesActivity, selectedPage, selectedFilter, tunes.values.toList())
                Handler(Looper.getMainLooper()).post {
                    progress.visibility = View.GONE
                    val data = Intent()
                    data.putExtra(PAGE_DATA, selectedPage as Parcelable)
                    setResult(Activity.RESULT_OK, data)
                    finish()
                }
            }

        }

        initPagePreview()
        initMenu()
        initTunes()
    }

    private fun initTunes() {
        val listener = object : TuneValueChangedListener {
            override fun tuneValueChanged(tuneType: ImageFilterTuneType, value: Float) {
                tunes[tuneType] = TuneOperation(tuneType, value)
                applyFilter(selectedFilter)
            }
        }

        light_tunes.addView(initTuneView(listener, ImageFilterTuneType.BRIGHTNESS))
        light_tunes.addView(initTuneView(listener, ImageFilterTuneType.SHADOW))
        light_tunes.addView(initTuneView(listener, ImageFilterTuneType.HIGHLIGHTS))
        light_tunes.addView(initTuneView(listener, ImageFilterTuneType.COMBINED_WHITE_BLACK_POINT))
        light_tunes.addView(initTuneView(listener, ImageFilterTuneType.CONTRAST))

        color_tunes.addView(initTuneView(listener, ImageFilterTuneType.SATURATION))
        color_tunes.addView(initTuneView(listener, ImageFilterTuneType.SATURATION_LOWER_BALANCE))
        color_tunes.addView(initTuneView(listener, ImageFilterTuneType.SATURATION_UPPER_BALANCE))
        color_tunes.addView(initTuneView(listener, ImageFilterTuneType.VIBRANCE))
        color_tunes.addView(initTuneView(listener, ImageFilterTuneType.TINT))
        color_tunes.addView(initTuneView(listener, ImageFilterTuneType.TEMPERATURE))
    }

    private fun initTuneView(listener: TuneValueChangedListener, filter: ImageFilterTuneType) : View {
        return TuneView(this).also { it.initForTune(filter, listener, tunes.get(filter)) }
    }

    override fun onResume() {
        super.onResume()
        if (!scanbotSDK.isLicenseValid) {
            showLicenseDialog()
        }
    }

    private fun showLicenseDialog() {
        if (supportFragmentManager.findFragmentByTag(ErrorFragment.NAME) == null) {
            val dialogFragment = ErrorFragment.newInstance()
            dialogFragment.show(supportFragmentManager, ErrorFragment.NAME)
        }
    }

    fun initActionBar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
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

    private fun initPagePreview() {
        if (!scanbotSDK.isLicenseValid) {
            showLicenseDialog()
        } else {
            generateFilteredPreview()
        }
    }

    private fun generateFilteredPreview() {
        progress.visibility = View.VISIBLE
        launch {
            val path = selectedPage.let {

                val filteredPreviewFilePath = scanbotSDK.pageFileStorage().getFilteredPreviewImageURI(it.pageId, selectedFilter).path
                if (!File(filteredPreviewFilePath).exists()) {
                    scanbotSDK.pageProcessor().generateFilteredPreview(it, selectedFilter)
                }
                filteredPreviewFilePath
            }
            Handler(Looper.getMainLooper()).post {
                path?.let {
                    Picasso.with(applicationContext)
                            .load(File(it))
                            .memoryPolicy(MemoryPolicy.NO_CACHE)
                            .resizeDimen(R.dimen.move_preview_size, R.dimen.move_preview_size)
                            .centerInside()
                            .into(image, ImageCallback())
                }
            }
        }
    }


    inner class ImageCallback : Callback {
        override fun onSuccess() {
            progress.visibility = View.GONE
        }

        override fun onError() {
            progress.visibility = View.GONE
        }
    }

    override fun lowLightBinarizationFilter() {
        applyFilter(ImageFilterType.LOW_LIGHT_BINARIZATION)
    }

    override fun edgeHighlightFilter() {
        applyFilter(ImageFilterType.EDGE_HIGHLIGHT)
    }

    override fun deepBinarizationFilter() {
        applyFilter(ImageFilterType.DEEP_BINARIZATION)
    }

    override fun otsuBinarizationFilter() {
        applyFilter(ImageFilterType.OTSU_BINARIZATION)
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

    private fun getFilterName() = selectedFilter.filterName.replace("_", " ").capitalize()

    override fun onBackPressed() {
        super.onBackPressed()
        removeFilteredPreview()
    }

    private fun removeFilteredPreview() {
        scanbotSDK.pageFileStorage().removeFilteredPreviewImages(selectedPage.pageId)
    }

    private fun applyFilter(imageFilterType: ImageFilterType) {
        if (!scanbotSDK.isLicenseValid) {
            showLicenseDialog()
        } else {
            progress.visibility = View.VISIBLE
            selectedFilter = imageFilterType
            filter_value.text = getFilterName()
            launch {
                PageRepository.generatePreview(this@FilterTunesActivity, selectedPage, selectedFilter, tunes.values.toList())
                Handler(Looper.getMainLooper()).post {
                    Picasso.with(applicationContext)
                            .load(File(scanbotSDK.pageFileStorage().getFilteredPreviewImageURI(this@FilterTunesActivity.selectedPage.pageId, selectedFilter).path))
                            .memoryPolicy(MemoryPolicy.NO_CACHE)
                            .resizeDimen(R.dimen.move_preview_size, R.dimen.move_preview_size)
                            .centerInside()
                            .into(image, ImageCallback())
                    progress.visibility = View.GONE
                }
            }
        }
    }
}