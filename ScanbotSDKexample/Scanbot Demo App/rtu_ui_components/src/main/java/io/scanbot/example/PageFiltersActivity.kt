package io.scanbot.example

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.*
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import com.squareup.picasso.Callback
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import io.scanbot.example.fragments.ErrorFragment
import io.scanbot.example.fragments.FiltersBottomSheetMenuFragment
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.persistence.Page
import io.scanbot.sdk.process.ImageFilterType
import io.scanbot.sdk.ui.view.edit.CroppingActivity
import io.scanbot.sdk.ui.view.edit.configuration.CroppingConfiguration
import kotlinx.android.synthetic.main.activity_filters.*
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.launch
import java.io.File

class PageFiltersActivity : AppCompatActivity(), FiltersListener {

    companion object {
        const val PAGE_DATA = "PAGE_DATA"
        private const val FILTERS_MENU_TAG = "FILTERS_MENU_TAG"
        const val CROP_DEFAULT_UI_REQUEST_CODE = 9999

        @JvmStatic
        fun newIntent(context: Context, page: Page): Intent {
            val intent = Intent(context, PageFiltersActivity::class.java)
            intent.putExtra(PAGE_DATA, (page as Parcelable))
            return intent
        }
    }

    lateinit var selectedPage: Page
    var selectedFilter: ImageFilterType = ImageFilterType.NONE
    lateinit var scanbotSDK: ScanbotSDK
    private lateinit var filtersSheetFragment: FiltersBottomSheetMenuFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filters)
        initActionBar()

        selectedPage = intent.getParcelableExtra(PAGE_DATA)

        selectedPage.let {
            selectedFilter = it.filter
        }
        scanbotSDK = ScanbotSDK(application)

        action_filter.setOnClickListener {
            filtersSheetFragment.show(supportFragmentManager, "CHOOSE_FILTERS_DIALOG_TAG")
        }
        action_delete.setOnClickListener {
            scanbotSDK.pageFileStorage().remove(selectedPage.pageId)
            finish()
        }
        action_crop_and_rotate.setOnClickListener {
            val croppingConfig = CroppingConfiguration()
            croppingConfig.setPage(this.selectedPage)
            croppingConfig.setBottomBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
            croppingConfig.setTopBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))

            croppingConfig.setBottomBarButtonsColor(ContextCompat.getColor(this, R.color.greyColor))
            croppingConfig.setTopBarButtonsColor(ContextCompat.getColor(this, R.color.greyColor))

            croppingConfig.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))

            val intent = CroppingActivity.newIntent(this, croppingConfig)
            startActivityForResult(intent, CROP_DEFAULT_UI_REQUEST_CODE)
        }

        initPagePreview()
        initMenu()
    }

    override fun onResume() {
        super.onResume()
        if (!scanbotSDK.isLicenseValid) {
            showLicenseDialog()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CROP_DEFAULT_UI_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val page = data!!.getParcelableExtra<Page>(io.scanbot.sdk.ui.view.edit.CroppingActivity.EDITED_PAGE_EXTRA)
            selectedPage = page
            initPagePreview()
            return
        }
    }

    private fun showLicenseDialog() {
        if (supportFragmentManager.findFragmentByTag(ErrorFragment.NAME) == null) {
            val dialogFragment = ErrorFragment.newInstanse()
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
            GenerateFilterPreviewTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR)
        }
    }

    inner class GenerateFilterPreviewTask : AsyncTask<Void, Void, String>() {

        override fun onPreExecute() {
            super.onPreExecute()
            progress.visibility = VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): String? {
            selectedPage.let {

                val filteredPreviewFilePath = scanbotSDK.pageFileStorage().getFilteredPreviewImageURI(it.pageId, selectedFilter).path
                if (!File(filteredPreviewFilePath).exists()) {
                    scanbotSDK.pageProcessor().generateFilteredPreview(it, selectedFilter)
                }
                return filteredPreviewFilePath
            }
        }

        override fun onPostExecute(filteredPreviewFilePath: String?) {
            super.onPostExecute(filteredPreviewFilePath)

            filteredPreviewFilePath?.let {
                Picasso.with(applicationContext)
                        .load(File(filteredPreviewFilePath))
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .resizeDimen(R.dimen.move_preview_size, R.dimen.move_preview_size)
                        .centerInside()
                        .into(image, ImageCallback())
            }
        }
    }

    inner class ImageCallback : Callback {
        override fun onSuccess() {
            progress.visibility = GONE
        }

        override fun onError() {
            progress.visibility = GONE
        }

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
            progress.visibility = VISIBLE
            selectedFilter = imageFilterType
            GlobalScope.launch(Dispatchers.Default, CoroutineStart.DEFAULT, {
                scanbotSDK.pageProcessor().applyFilter(this@PageFiltersActivity.selectedPage, imageFilterType)
                scanbotSDK.pageProcessor().generateFilteredPreview(this@PageFiltersActivity.selectedPage, selectedFilter)
                Handler(Looper.getMainLooper()).post {
                    Picasso.with(applicationContext)
                            .load(File(scanbotSDK.pageFileStorage().getFilteredPreviewImageURI(this@PageFiltersActivity.selectedPage.pageId, selectedFilter).path))
                            .memoryPolicy(MemoryPolicy.NO_CACHE)
                            .resizeDimen(R.dimen.move_preview_size, R.dimen.move_preview_size)
                            .centerInside()
                            .into(image, ImageCallback())
                    progress.visibility = GONE
                }
            })
        }
    }

}