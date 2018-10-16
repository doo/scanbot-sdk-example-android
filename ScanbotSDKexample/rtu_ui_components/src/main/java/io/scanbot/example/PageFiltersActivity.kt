package io.scanbot.example

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.DialogFragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import com.squareup.picasso.Callback
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.persistence.Page
import io.scanbot.sdk.process.ImageFilterType
import kotlinx.android.synthetic.main.activity_filters.*
import java.io.File

class PageFiltersActivity : AppCompatActivity() {

    companion object {
        const val PAGE_DATA = "PAGE_DATA"

        @JvmStatic
        fun newIntent(context: Context, page: Page): Intent {
            val intent = Intent(context, PageFiltersActivity::class.java)
            intent.putExtra(PAGE_DATA, (page as Parcelable))
            return intent
        }
    }

    var selectedPage : Page? = null
    var selectedFilter : ImageFilterType = ImageFilterType.NONE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filters)

        selectedPage = intent.getParcelableExtra(PAGE_DATA)

        selectedPage?.let {
            selectedFilter = it.filter
        }

        chooseFiltersBtn.setOnClickListener {
            OptionsDialogFragment().show(supportFragmentManager, "CHOOSE_FILTERS_DIALOG_TAG")
        }

        done.setOnClickListener {
            ApplyFilterTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR)
        }
        cancel.setOnClickListener { finish() }

        initPagePreview()
    }

    private fun initPagePreview() {
        GenerateFilterPreviewTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR)
    }

    private fun updateFilteredPreview(filter : ImageFilterType) {
        this.selectedFilter = filter
        GenerateFilterPreviewTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR)
    }

    inner class ApplyFilterTask: AsyncTask<Void, Void, Void>() {

        override fun onPreExecute() {
            super.onPreExecute()
            progress.visibility = VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): Void? {
            selectedPage?.let {
                val scanbotSDK = ScanbotSDK(application)
                selectedPage = scanbotSDK.pageProcessor().applyFilter(it, selectedFilter)
            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)

            progress.visibility = GONE

            val resultData = Intent()
            resultData.putExtra(PAGE_DATA, selectedPage as Parcelable)
            setResult(Activity.RESULT_OK, resultData)
            finish()
        }
    }

    inner class GenerateFilterPreviewTask: AsyncTask<Void, Void, String>() {

        override fun onPreExecute() {
            super.onPreExecute()
            progress.visibility = VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): String? {
            selectedPage?.let {
                val scanbotSDK = ScanbotSDK(application)
                val filteredPreviewFilePath = scanbotSDK.pageFileStorage().getFilteredPreviewImageURI(it.pageId, selectedFilter).path
                if (!File(filteredPreviewFilePath).exists()) {
                    scanbotSDK.pageProcessor().generateFilteredPreview(it, selectedFilter)
                }
                return filteredPreviewFilePath
            }
            return null
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

    class OptionsDialogFragment : DialogFragment() {
        val options: Array<ImageFilterType> = ImageFilterType.values()

        lateinit var optionsList: RecyclerView

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val view = inflater.inflate(R.layout.options_dialog, null, false)
            optionsList = view.findViewById (R.id.options_list)
            optionsList.layoutManager = LinearLayoutManager(context)

            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE)
            return view
        }


        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)

            activity?.let { optionsList.adapter = OptionsAdapter(options, it) }
        }

        inner class OptionsAdapter(val items : Array<ImageFilterType>, val context: Context) : RecyclerView.Adapter<OptionsViewHolder>() {

            override fun getItemCount(): Int {
                return items.size
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionsViewHolder {
                return OptionsViewHolder(LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false))
            }

            override fun onBindViewHolder(holder: OptionsViewHolder, position: Int) {
                holder?.option?.text = items[position].filterName
                holder?.option.setOnClickListener {
                    (context as PageFiltersActivity).updateFilteredPreview(items[position])
                    dismissAllowingStateLoss()
                }
            }
        }
    }

    class OptionsViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        val option = view.findViewById<TextView>(android.R.id.text1)
    }
}