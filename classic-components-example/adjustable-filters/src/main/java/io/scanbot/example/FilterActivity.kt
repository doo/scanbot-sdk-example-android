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
import io.scanbot.example.databinding.ActivityFiltersTunesBinding
import io.scanbot.example.fragment.ErrorFragment
import io.scanbot.example.fragment.FiltersBottomSheetMenuFragment
import io.scanbot.imagefilters.LegacyFilter
import io.scanbot.imagefilters.ParametricFilter
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.docprocessing.PageProcessor
import io.scanbot.sdk.persistence.Page
import io.scanbot.sdk.persistence.PageFileStorage
import io.scanbot.sdk.process.ImageFilterType
import kotlinx.coroutines.*
import java.io.File
import kotlin.coroutines.CoroutineContext

class FilterActivity : AppCompatActivity(), FiltersListener, CoroutineScope {
    companion object {
        private const val PAGE_DATA = "PAGE_DATA"
        private const val FILTERS_MENU_TAG = "FILTERS_MENU_TAG"

        @JvmStatic
        fun newIntent(context: Context, page: Page): Intent {
            val intent = Intent(context, FilterActivity::class.java)
            intent.putExtra(PAGE_DATA, (page as Parcelable))
            return intent
        }
    }

    private lateinit var selectedPage: Page

    private var filteringState: FilteringState = FilteringState.IDLE
    private var selectedFilter: ParametricFilter? = null
    private lateinit var filtersSheetFragment: FiltersBottomSheetMenuFragment

    private lateinit var scanbotSDK: ScanbotSDK
    private lateinit var pageProcessor: PageProcessor
    private lateinit var pageFileStorage: PageFileStorage

    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    private lateinit var binding: ActivityFiltersTunesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFiltersTunesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initActionBar()

        selectedPage = intent.getParcelableExtra(PAGE_DATA)!!

        selectedPage.let { page ->
            selectedFilter = page.parametricFilters.firstOrNull() ?: LegacyFilter(ImageFilterType.NONE.code)
        }

        binding.filterValue.text = getFilterName()

        scanbotSDK = ScanbotSDK(application)
        pageFileStorage = scanbotSDK.createPageFileStorage()
        pageProcessor = scanbotSDK.createPageProcessor()

        binding.filtersInnerLayout.setOnClickListener {
            filtersSheetFragment.show(supportFragmentManager, "CHOOSE_FILTERS_DIALOG_TAG")
        }

        binding.cancel.setOnClickListener {
            removeFilteredPreview()
            finish()
        }

        binding.done.setOnClickListener {
            binding.progress.visibility = View.VISIBLE
            launch {
                selectedFilter?.let { selectedPage = PageFilterHelper.applyFilter(pageProcessor, selectedPage, it) }
                withContext(Dispatchers.Main) {
                    binding.progress.visibility = View.GONE
                    val data = Intent()
                    data.putExtra(PAGE_DATA, selectedPage as Parcelable)
                    setResult(Activity.RESULT_OK, data)
                    finish()
                }
            }
        }

        initPagePreview()
        initMenu()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
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
        selectedFilter?.let { applyFilter(it) }
    }

    inner class ImageCallback : Callback {
        override fun onSuccess() {
            binding.progress.visibility = View.GONE
        }

        override fun onError(e: java.lang.Exception?) {
            binding.progress.visibility = View.GONE
        }
    }

    override fun onFilterApplied(parametricFilter: ParametricFilter) {
        applyFilter(parametricFilter)
    }

    private fun getFilterName() = selectedFilter?.let {
        if (it is LegacyFilter)
            ImageFilterType.getByCode(it.filterType).filterName.replace("_", " ").capitalize()
        else it.javaClass.simpleName
    } ?: "None"

    override fun onBackPressed() {
        super.onBackPressed()
        removeFilteredPreview()
    }

    private fun removeFilteredPreview() {
        pageFileStorage.removeFilteredPreviewImages(selectedPage.pageId)
    }

    private fun applyFilter(parametricFilter: ParametricFilter) {
        if (!scanbotSDK.licenseInfo.isValid) {
            showLicenseDialog()
        } else {
            binding.progress.visibility = View.VISIBLE
            selectedFilter = parametricFilter
            binding.filterValue.text = getFilterName()
            if (filteringState == FilteringState.IDLE && selectedFilter != null) {
                launch {
                    filteringState = FilteringState.PROCESSING

                    try {
                        pageProcessor.generateFilteredPreview(selectedPage, selectedFilter!!)
                    }
                    catch (e: Exception) {
                        Log.e("FilterTunesActivity", "Couldn't generate preview image.", e)
                        finish()
                    }

                    withContext(Dispatchers.Main) {
                        pageFileStorage.getFilteredPreviewImageURI(selectedPage.pageId, selectedFilter!!).path?.let {
                            Picasso.get()
                                .load(File(it))
                                .memoryPolicy(MemoryPolicy.NO_CACHE)
                                .resizeDimen(R.dimen.move_preview_size, R.dimen.move_preview_size)
                                .centerInside()
                                .into(binding.image, ImageCallback())
                            binding.progress.visibility = View.GONE
                            val previousState = filteringState
                            filteringState = FilteringState.IDLE
                            if (previousState == FilteringState.PROCESSING_AND_SCHEDULED) {
                                applyFilter(selectedFilter!!)
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