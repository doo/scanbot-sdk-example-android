package io.scanbot.example

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.squareup.picasso.Callback
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import io.scanbot.example.common.Const
import io.scanbot.example.common.applyEdgeToEdge
import io.scanbot.example.common.showToast
import io.scanbot.example.databinding.ActivityFiltersTunesBinding
import io.scanbot.example.fragment.ErrorFragment
import io.scanbot.example.fragment.FiltersBottomSheetMenuFragment
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.docprocessing.Document
import io.scanbot.sdk.docprocessing.Page
import io.scanbot.sdk.imageprocessing.ParametricFilter
import kotlinx.coroutines.*

class FilterActivity : AppCompatActivity(), FiltersListener {

    companion object {
        private const val FILTERS_MENU_TAG = "FILTERS_MENU_TAG"

        const val DOC_ID_EXTRA = "DOC_ID_EXTRA"

        @JvmStatic
        fun newIntent(context: Context, documentId: String): Intent {
            return Intent(context, FilterActivity::class.java).apply {
                putExtra(DOC_ID_EXTRA, documentId)
            }
        }
    }

    private lateinit var document: Document
    private lateinit var page: Page

    private var filteringState: FilteringState = FilteringState.IDLE
    private var selectedFilter: ParametricFilter? = null
    private lateinit var filtersSheetFragment: FiltersBottomSheetMenuFragment

    private lateinit var scanbotSdk: ScanbotSDK

    private val binding by lazy { ActivityFiltersTunesBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initActionBar()
        applyEdgeToEdge(findViewById(R.id.root_view))

        scanbotSdk = ScanbotSDK(application)

        val docId = intent.getStringExtra(DOC_ID_EXTRA)
        if (docId == null) {
            this.showToast("Document ID is missing!")
            finish()
            return
        }

        lifecycleScope.launch { loadDocument(docId) }

        binding.filtersInnerLayout.setOnClickListener {
            filtersSheetFragment.show(supportFragmentManager, "CHOOSE_FILTERS_DIALOG_TAG")
        }

        binding.cancel.setOnClickListener { finish() }

        binding.done.setOnClickListener {
            lifecycleScope.launch {
                withContext(Dispatchers.Main) {
                    val data = Intent().apply {
                        putExtra(DOC_ID_EXTRA, docId)
                    }
                    setResult(Activity.RESULT_OK, data)
                    finish()
                }
            }
        }

        initMenu()
    }

    private suspend fun loadDocument(docId: String) {
        val doc = withContext(Dispatchers.IO) { scanbotSdk.documentApi.loadDocument(docId) }
        withContext(Dispatchers.Main) {
            if (doc == null) {
                showToast("Document with given ID was not found!")
                Log.e(Const.LOG_TAG, "Document with ID $docId was not found!")
                finish()
                return@withContext
            }

            document = doc

            /** For example purposes we only expect one and only page in given document.
             * In real app there can be many. */
            val page = document.pageAtIndex(0)
            if (page == null) {
                showToast("Document has no pages!")
                Log.e(Const.LOG_TAG, "Document has no pages!")
                finish()
                return@withContext
            }

            this@FilterActivity.page = page

            /** For example purposes we only use one filter from the list here - because we also apply only one filter.
             * In real app one can use multiple filters. */
            val appliedFilter = page.filters.getOrNull(0)
            selectedFilter = appliedFilter

            binding.filterValue.text = selectedFilter.getFilterName()

            updatePagePreview()
        }
    }

    private fun updatePagePreview() {
        Picasso.get()
            .load(page.documentPreviewFileUri)
            .memoryPolicy(MemoryPolicy.NO_CACHE)
            .resizeDimen(R.dimen.move_preview_size, R.dimen.move_preview_size)
            .centerInside()
            .into(binding.image, ImageCallback())
    }

    override fun onResume() {
        super.onResume()
        if (!scanbotSdk.licenseInfo.isValid) {
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
            supportFragmentManager.beginTransaction().remove(fragment).commitNow()
        }

        filtersSheetFragment = FiltersBottomSheetMenuFragment()
    }

    override fun onFilterApplied(parametricFilter: ParametricFilter?) {
        applyFilter(parametricFilter)
    }

    private fun applyFilter(newFilter: ParametricFilter?) {
        if (!scanbotSdk.licenseInfo.isValid) {
            showLicenseDialog()
        } else if (selectedFilter != newFilter) {
            binding.progress.visibility = View.VISIBLE
            selectedFilter = newFilter
            binding.filterValue.text = selectedFilter.getFilterName()
            if (filteringState == FilteringState.IDLE) {
                lifecycleScope.launch {
                    filteringState = FilteringState.PROCESSING

                    withContext(Dispatchers.Default) {
                        // applying empty collection of filters will remove all filters
                        val filtersToApply = selectedFilter?.let { listOf(it) } ?: emptyList()
                        page.apply(newFilters = filtersToApply)
                    }

                    withContext(Dispatchers.Main) {
                        updatePagePreview()

                        binding.progress.visibility = View.GONE
                        filteringState = FilteringState.IDLE
                    }
                }
            } else {
                filteringState = FilteringState.PROCESSING_AND_SCHEDULED
            }
        }
    }

    inner class ImageCallback : Callback {
        override fun onSuccess() {
            binding.progress.visibility = View.GONE
        }

        override fun onError(e: java.lang.Exception?) {
            binding.progress.visibility = View.GONE
        }
    }
}

private enum class FilteringState {
    IDLE,
    PROCESSING,
    PROCESSING_AND_SCHEDULED,
}
