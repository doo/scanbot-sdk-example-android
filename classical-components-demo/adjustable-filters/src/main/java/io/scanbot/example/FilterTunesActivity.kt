package io.scanbot.example

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.squareup.picasso.Callback
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import io.scanbot.example.fragment.ErrorFragment
import io.scanbot.example.fragment.FiltersBottomSheetMenuFragment
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.docprocessing.PageProcessor
import io.scanbot.sdk.persistence.Page
import io.scanbot.sdk.persistence.PageFileStorage
import io.scanbot.sdk.process.ImageFilterTuneType
import io.scanbot.sdk.process.ImageFilterType
import io.scanbot.sdk.process.TuneOperation
import kotlinx.android.synthetic.main.activity_filters_tunes.*
import kotlinx.coroutines.*
import java.io.File
import kotlin.coroutines.CoroutineContext

class FilterTunesActivity : AppCompatActivity(), FiltersListener, CoroutineScope {
    companion object {
        private const val PAGE_DATA = "PAGE_DATA"
        private const val FILTERS_MENU_TAG = "FILTERS_MENU_TAG"

        @JvmStatic
        fun newIntent(context: Context, page: Page): Intent {
            val intent = Intent(context, FilterTunesActivity::class.java)
            intent.putExtra(PAGE_DATA, (page as Parcelable))
            return intent
        }
    }

    private lateinit var selectedPage: Page

    private var filteringState: FilteringState = FilteringState.IDLE
    private var selectedFilter: ImageFilterType = ImageFilterType.NONE
    private var tunes: LinkedHashMap<ImageFilterTuneType, TuneOperation> = linkedMapOf()
    private lateinit var filtersSheetFragment: FiltersBottomSheetMenuFragment

    private lateinit var scanbotSDK: ScanbotSDK
    private lateinit var pageProcessor: PageProcessor
    private lateinit var pageFileStorage: PageFileStorage

    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filters_tunes)
        initActionBar()

        selectedPage = intent.getParcelableExtra(PAGE_DATA) ?: return

        selectedFilter = selectedPage.filter
        updateCheckboxForType(selectedPage.filter)
        tunes = LinkedHashMap(selectedPage.tunes.groupBy { it.tuneType }.mapValues { it.value.first() })

        filter_value.text = getFilterName()

        scanbotSDK = ScanbotSDK(application)
        pageFileStorage = scanbotSDK.createPageFileStorage()
        pageProcessor = scanbotSDK.createPageProcessor()

        filters_inner_layout.setOnClickListener {
            filtersSheetFragment.show(supportFragmentManager, "CHOOSE_FILTERS_DIALOG_TAG")
        }

        cancel.setOnClickListener {
            removeFilteredPreview()
            finish()
        }

        base_filter_first_switch.setOnCheckedChangeListener { _, _ ->
            applyFilter(selectedFilter)
        }

        done.setOnClickListener {
            progress.visibility = View.VISIBLE
            launch {
                val list = tunes.values.toList()
                val filterOrder = if (base_filter_first_switch.isChecked) 0 else list.size
                selectedPage = PageFilterHelper.applyFilter(pageProcessor, selectedPage, selectedFilter, list, filterOrder)
                withContext(Dispatchers.Main) {
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

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
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

    private fun initTuneView(listener: TuneValueChangedListener, filter: ImageFilterTuneType): View {
        return TuneView(this).also { it.initForTune(filter, listener, tunes[filter]) }
    }

    override fun onResume() {
        super.onResume()
        if (!scanbotSDK.licenseInfo.isValid) {
            showLicenseDialog()
        }
    }

    private fun showLicenseDialog() {
        if (supportFragmentManager.findFragmentByTag(ErrorFragment.NAME) == null) {
            val dialogFragment = ErrorFragment.newInstance()
            dialogFragment.show(supportFragmentManager, ErrorFragment.NAME)
        }
    }

    private fun initActionBar() {
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
        applyFilter(selectedFilter)
    }

    inner class ImageCallback : Callback {
        override fun onSuccess() {
            progress.visibility = View.GONE
        }

        override fun onError(e: java.lang.Exception?) {
            progress.visibility = View.GONE
        }
    }

    override fun onFilterApplied(filterType: ImageFilterType) {
        applyFilter(filterType)
        updateCheckboxForType(filterType)
    }

    private fun getFilterName() = selectedFilter.filterName.replace("_", " ").capitalize()

    override fun onBackPressed() {
        super.onBackPressed()
        removeFilteredPreview()
    }

    private fun removeFilteredPreview() {
        pageFileStorage.removeFilteredPreviewImages(selectedPage.pageId)
    }

    private fun updateCheckboxForType(filterType: ImageFilterType) {
        base_filter_first_switch.setOnCheckedChangeListener(null)
        base_filter_first_switch.isChecked =
                when (filterType) {
                    ImageFilterType.COLOR_DOCUMENT,
                    ImageFilterType.COLOR_ENHANCED,
                    ImageFilterType.GRAYSCALE -> true
                    else -> false
                }
        base_filter_first_switch.setOnCheckedChangeListener { _, _ ->
            applyFilter(selectedFilter)
        }
    }

    private fun applyFilter(imageFilterType: ImageFilterType) {
        if (!scanbotSDK.licenseInfo.isValid) {
            showLicenseDialog()
        } else {
            progress.visibility = View.VISIBLE
            selectedFilter = imageFilterType
            filter_value.text = getFilterName()
            if (filteringState == FilteringState.IDLE) {
                launch {
                    filteringState = FilteringState.PROCESSING
                    val tunesList = tunes.values.toList()

                    try {
                        pageProcessor.generateFilteredPreview(selectedPage, selectedFilter,
                                tunesList, if (base_filter_first_switch.isChecked) 0 else tunesList.size)
                    }
                    catch (e: Exception) {
                        Log.e("FilterTunesActivity", "Couldn't generate preview image.", e)
                        finish()
                    }

                    withContext(Dispatchers.Main) {
                        pageFileStorage.getFilteredPreviewImageURI(selectedPage.pageId, selectedFilter).path?.let {
                            Picasso.get()
                                .load(File(it))
                                .memoryPolicy(MemoryPolicy.NO_CACHE)
                                .resizeDimen(R.dimen.move_preview_size, R.dimen.move_preview_size)
                                .centerInside()
                                .into(image, ImageCallback())
                            progress.visibility = View.GONE
                            val previousState = filteringState
                            filteringState = FilteringState.IDLE
                            if (previousState == FilteringState.PROCESSING_AND_SCHEDULED) {
                                applyFilter(selectedFilter)
                            }
                        }
                    }
                }
            } else {
                filteringState = FilteringState.PROCESSING_AND_SCHEDULED
            }
        }
    }
}

private enum class FilteringState {
    IDLE,
    PROCESSING,
    PROCESSING_AND_SCHEDULED
}