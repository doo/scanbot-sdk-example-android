package io.scanbot.example

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.persistence.Page
import io.scanbot.sdk.persistence.PageFileStorage
import io.scanbot.sdk.ui.view.edit.configuration.CroppingConfiguration
import java.io.File


class PagePreviewActivity : AppCompatActivity() {
    private lateinit var adapter: PagesAdapter
    private lateinit var recycleView: RecyclerView

    companion object {
        val CROP_DEFAULT_UI_REQUEST_CODE = 9999
        val FILTER_UI_REQUEST_CODE = 7777

        var selectedPage: Page? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_preview)

        adapter = PagesAdapter()
        adapter.setHasStableIds(true)

        recycleView = findViewById(R.id.pages_preview)
        recycleView.setHasFixedSize(true)
        recycleView.adapter = adapter

        val layoutManager = GridLayoutManager(this, 3)
        recycleView.layoutManager = layoutManager

        // initialize items only once, so we can update items from onActivityResult
        adapter.setItems(ScanbotSDK(this).pageFileStorage().getStoredPages().map { id -> Page(id) })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CROP_DEFAULT_UI_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val page = data!!.getParcelableExtra<Page>(io.scanbot.sdk.ui.view.edit.CroppingActivity.EDITED_PAGE_EXTRA)
            adapter.updateItem(page)
            return
        }

        if (requestCode == FILTER_UI_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val page = data!!.getParcelableExtra<Page>(PageFiltersActivity.PAGE_DATA)
            adapter.updateItem(page)
            return
        }
    }

    private inner class PagesAdapter : RecyclerView.Adapter<PageViewHolder>() {

        val items: MutableList<Page> = mutableListOf()
        private val mOnClickListener: View.OnClickListener = MyOnClickListener()
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

    inner class MyOnClickListener : View.OnClickListener {
        override fun onClick(v: View?) {
            val itemPosition = recycleView.getChildLayoutPosition(v)
            selectedPage = adapter.items[itemPosition]
            OptionsDialogFragment().show(supportFragmentManager, "OPTIONS_DIALOG_TAG")
        }
    }

    /**
     * View holder for page and its number.
     */
    private inner class PageViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal val imageView: ImageView

        init {
            imageView = itemView.findViewById<View>(R.id.page) as ImageView
        }
    }

    class OptionsDialogFragment : DialogFragment() {
        private val options: List<String> = arrayListOf<String>(
                "Open Cropping UI",
                "Open Filter UI",
                "Rotate page left",
                "Rotate page right",
                "Delete page"
        )

        private lateinit var optionsList: RecyclerView

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val view = inflater.inflate(R.layout.options_dialog, null, false)
            optionsList = view.findViewById(R.id.options_list)
            optionsList.layoutManager = LinearLayoutManager(context)

            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE)
            return view
        }


        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)

            optionsList.adapter = OptionsAdapter(options, context, this)
        }
    }

    class OptionsAdapter(private val items: List<String>, val context: Context?,
                         private val dialogFragment: OptionsDialogFragment) : RecyclerView.Adapter<OptionsViewHolder>() {

        override fun getItemCount(): Int {
            return items.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionsViewHolder {
            return OptionsViewHolder(LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false))
        }

        override fun onBindViewHolder(holder: OptionsViewHolder, position: Int) {
            holder.option?.text = items[position]
            holder.option.setOnClickListener {
                selectedPage?.let {
                    when(position) {
                        0 -> {
                            val croppingConfig = CroppingConfiguration()
                            croppingConfig.setPage(it)
                            if (context != null) {
                                val intent = io.scanbot.sdk.ui.view.edit.CroppingActivity.newIntent(context, croppingConfig)
                                (context as Activity).startActivityForResult(intent, CROP_DEFAULT_UI_REQUEST_CODE)
                            }
                        }
                        1 -> {
                            if (context != null) {
                                val intent = PageFiltersActivity.newIntent(context, it)
                                (context as Activity).startActivityForResult(intent, FILTER_UI_REQUEST_CODE)
                            }
                        }
                        2, 3 -> {
                            if (context != null) {
                                // TODO perform as a task and refresh images view when done.
                                val times = if(position == 2) 1 else -1
                                ScanbotSDK(context).pageProcessor().rotate(it, times)
                            }
                        }
                        4 -> {
                            if (context != null) {
                                ScanbotSDK(context).pageFileStorage().remove(it.pageId)
                                // TODO refresh images view
                            }
                        }
                        else -> {
                            //
                            if (context != null) {
                                Toast.makeText(context, "Option not implemented!", Toast.LENGTH_LONG).show()
                            }
                        }

                    }
                }
                dialogFragment.dismiss()
            }
        }
    }

    class OptionsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val option = view.findViewById<TextView>(android.R.id.text1)
    }
}