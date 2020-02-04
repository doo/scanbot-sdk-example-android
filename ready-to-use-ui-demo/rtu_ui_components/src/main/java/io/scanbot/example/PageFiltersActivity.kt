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
import com.squareup.picasso.Picasso
import io.scanbot.example.fragments.ErrorFragment
import io.scanbot.example.fragments.FiltersBottomSheetMenuFragment
import io.scanbot.example.repository.PageRepository
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.persistence.Page
import io.scanbot.sdk.ui.view.edit.CroppingActivity
import io.scanbot.sdk.ui.view.edit.configuration.CroppingConfiguration
import kotlinx.android.synthetic.main.activity_filters.*
import kotlinx.coroutines.*
import java.io.File
import kotlin.coroutines.CoroutineContext

class PageFiltersActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        const val PAGE_DATA = "PAGE_DATA"
        private const val FILTERS_MENU_TAG = "FILTERS_MENU_TAG"
        const val CROP_DEFAULT_UI_REQUEST_CODE = 9999
        const val FILTER_TUNES_UI_REQUEST_CODE = 9998

        @JvmStatic
        fun newIntent(context: Context, page: Page): Intent {
            val intent = Intent(context, PageFiltersActivity::class.java)
            intent.putExtra(PAGE_DATA, (page as Parcelable))
            return intent
        }
    }


    lateinit var selectedPage: Page
    lateinit var scanbotSDK: ScanbotSDK
    private lateinit var filtersSheetFragment: FiltersBottomSheetMenuFragment

    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filters)
        initActionBar()

        selectedPage = PageRepository.getPages().find {
            it.pageId == (intent.getParcelableExtra(PAGE_DATA) as Page).pageId
        }!!

        scanbotSDK = ScanbotSDK(application)

        action_filter.setOnClickListener {
            val intent = FilterTunesActivity.newIntent(this, selectedPage)
            startActivityForResult(intent, FILTER_TUNES_UI_REQUEST_CODE)
        }
        action_delete.setOnClickListener {
            PageRepository.removePage(this, selectedPage)
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
            croppingConfig.setPolygonColor(Color.RED)
            croppingConfig.setPolygonColorMagnetic(Color.BLUE)
            //croppingConfig.setCancelButtonTitle("Cancel")
            //croppingConfig.setDetectResetButtonHidden(true)
            // Customize further colors, text resources, behavior flags ...

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
            selectedPage = PageRepository.updatePage(data!!.getParcelableExtra(CroppingActivity.EDITED_PAGE_EXTRA))
            initPagePreview()
            return
        } else if (requestCode == FILTER_TUNES_UI_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            selectedPage = PageRepository.updatePage(data!!.getParcelableExtra(FilterTunesActivity.PAGE_DATA))
            initPagePreview()
            return
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

                val filteredPreviewFilePath = scanbotSDK.pageFileStorage().getFilteredPreviewImageURI(it.pageId, it.filter).path
                if (!File(filteredPreviewFilePath).exists()) {
                    scanbotSDK.pageProcessor().generateFilteredPreview(it, it.filter)
                }
                filteredPreviewFilePath
            }
            withContext(Dispatchers.Main) {
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
            progress.visibility = GONE
        }

        override fun onError() {
            progress.visibility = GONE
        }
    }
}