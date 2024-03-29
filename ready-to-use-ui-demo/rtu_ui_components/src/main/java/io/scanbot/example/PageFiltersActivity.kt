package io.scanbot.example

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.squareup.picasso.Callback
import com.squareup.picasso.MemoryPolicy
import io.scanbot.example.databinding.ActivityFiltersBinding
import io.scanbot.example.di.ExampleSingleton
import io.scanbot.example.di.ExampleSingletonImpl
import io.scanbot.example.fragments.ErrorFragment
import io.scanbot.example.fragments.FiltersBottomSheetMenuFragment
import io.scanbot.example.repository.PageRepository
import io.scanbot.example.util.PicassoHelper
import io.scanbot.imagefilters.ParametricFilter
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.persistence.Page
import io.scanbot.sdk.persistence.PageFileStorage
import io.scanbot.sdk.process.ImageFilterType
import io.scanbot.sdk.ui.registerForActivityResultOk
import io.scanbot.sdk.ui.view.edit.CroppingActivity
import io.scanbot.sdk.ui.view.edit.configuration.CroppingConfiguration
import kotlinx.coroutines.*
import java.io.File
import java.lang.Exception
import kotlin.coroutines.CoroutineContext

class PageFiltersActivity : AppCompatActivity(), CoroutineScope, FiltersListener {

    companion object {
        const val PAGE_DATA = "PAGE_DATA"
        private const val FILTERS_MENU_TAG = "FILTERS_MENU_TAG"

        @JvmStatic
        fun newIntent(context: Context, page: Page): Intent {
            val intent = Intent(context, PageFiltersActivity::class.java)
            intent.putExtra(PAGE_DATA, (page as Parcelable))
            return intent
        }
    }

    private lateinit var binding: ActivityFiltersBinding

    lateinit var selectedPage: Page
    lateinit var scanbotSDK: ScanbotSDK
    lateinit var singletonInstance: ExampleSingleton

    private var selectedFilter: ParametricFilter? = null
    private lateinit var filtersSheetFragment: FiltersBottomSheetMenuFragment

    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    private val croppingActivityResultLauncher = registerForActivityResultOk(CroppingActivity.ResultContract()) { resultEntity->
        selectedPage = PageRepository.updatePage(resultEntity.result!!)
        initPagePreview()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFiltersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initActionBar()

        val page = intent.getParcelableExtra(PAGE_DATA) as? Page ?: throw IllegalStateException("No page to filter provided!")

        selectedPage = PageRepository.getPages().find {
            it.pageId == page.pageId
        }!!

        selectedPage.let {
            selectedFilter = it.parametricFilters.firstOrNull()
        }

        scanbotSDK = ScanbotSDK(application)
        singletonInstance = ExampleSingletonImpl(this@PageFiltersActivity)

        binding.actionFilter.setOnClickListener {
            filtersSheetFragment.show(supportFragmentManager, "CHOOSE_FILTERS_DIALOG_TAG")
        }
        binding.actionDelete.setOnClickListener {
            PageRepository.removePage(this, selectedPage)
            finish()
        }

        binding.actionCropAndRotate.setOnClickListener {
            val croppingConfig = CroppingConfiguration(this.selectedPage)
            croppingConfig.setBottomBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
            croppingConfig.setTopBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
            croppingConfig.setBottomBarButtonsColor(ContextCompat.getColor(this, R.color.greyColor))
            croppingConfig.setTopBarButtonsColor(ContextCompat.getColor(this, R.color.greyColor))
            croppingConfig.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
            croppingConfig.setPolygonColor(Color.RED)
            croppingConfig.setPolygonColorMagnetic(Color.BLUE)
            croppingConfig.setHintTitle("Please, select the bounds of the document")
            croppingConfig.setHintTitleColor(Color.WHITE)
            //croppingConfig.setCancelButtonTitle("Cancel")
            //croppingConfig.setDetectResetButtonHidden(true)
            // Customize further colors, text resources, behavior flags ...

            croppingActivityResultLauncher.launch(croppingConfig)
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

    fun initActionBar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar!!.title = getString(R.string.page_title)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
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

    override fun onBackPressed() {
        val data = Intent()
        data.putExtra(PAGE_DATA, selectedPage as Parcelable)
        setResult(Activity.RESULT_OK, data)
        super.onBackPressed()
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
        if (!scanbotSDK.licenseInfo.isValid) {
            showLicenseDialog()
        } else {
            generateFilteredPreview()
        }
    }

    private fun generateFilteredPreview() {
        binding.progress.visibility = View.VISIBLE
        launch {
            val path = selectedPage.let { page ->
                if (selectedFilter == null) {
                    singletonInstance.pageFileStorageInstance().getPreviewImageURI(page.pageId, PageFileStorage.PageFileType.DOCUMENT).path
                } else {
                    val filteredPreviewFilePath = singletonInstance.pageFileStorageInstance()
                        .getFilteredPreviewImageURI(
                            page.pageId,
                            page.parametricFilters.first()
                        ).path
                    if (!File(filteredPreviewFilePath).exists()) {
                        singletonInstance.pageProcessorInstance()
                            .generateFilteredPreview(page, selectedFilter!!)
                    }
                    filteredPreviewFilePath
                }
            }
            withContext(Dispatchers.Main) {
                path?.let {
                    PicassoHelper.with(applicationContext)
                            .load(File(it))
                            .memoryPolicy(MemoryPolicy.NO_CACHE)
                            .resizeDimen(R.dimen.move_preview_size, R.dimen.move_preview_size)
                            .centerInside()
                            .into(binding.image, ImageCallback())
                }
            }
        }
    }

    override fun onFilterApplied(parametricFilter: ParametricFilter) {
        applyFilter(parametricFilter)
    }

    private fun applyFilter(parametricFilter: ParametricFilter) {
        if (!scanbotSDK.licenseInfo.isValid) {
            showLicenseDialog()
        } else {
            binding.progress.visibility = View.VISIBLE
            selectedFilter = parametricFilter
            launch {
                selectedPage = PageRepository.applyFilter(this@PageFiltersActivity, selectedPage, parametricFilter)
                withContext(Dispatchers.Main) {
                    val pageFileStorageInstance = singletonInstance.pageFileStorageInstance()
                    PicassoHelper.with(applicationContext)
                            .load(File(pageFileStorageInstance.getFilteredPreviewImageURI(this@PageFiltersActivity.selectedPage.pageId, parametricFilter).path))
                            .memoryPolicy(MemoryPolicy.NO_CACHE)
                            .resizeDimen(R.dimen.move_preview_size, R.dimen.move_preview_size)
                            .centerInside()
                            .into(binding.image, ImageCallback())
                    binding.progress.visibility = GONE
                }
            }
        }
    }

    inner class ImageCallback : Callback {
        override fun onSuccess() {
            binding.progress.visibility = GONE
        }

        override fun onError(e: Exception?) {
            binding.progress.visibility = GONE
        }
    }
}
