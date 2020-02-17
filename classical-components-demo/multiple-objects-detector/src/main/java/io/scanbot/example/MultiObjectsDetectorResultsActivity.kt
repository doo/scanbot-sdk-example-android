package io.scanbot.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.persistence.Page
import io.scanbot.sdk.persistence.PageFileStorage

class MultiObjectsDetectorResultsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_multiple_objects_detector_results)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val imagePaths = getImagePathsFromArgs()
        val adapter = MultipleObjectsDetectorResultsAdapter(imagePaths)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    private fun getImagePathsFromArgs(): List<String> {
        val pages = intent.getParcelableArrayListExtra<Page>(PARAMS_KEY)

        val pageFileStorage = ScanbotSDK(this).pageFileStorage()

        return pages.map { page ->
            pageFileStorage.getPreviewImageURI(page.pageId, PageFileStorage.PageFileType.UNFILTERED_DOCUMENT).path
        }
    }

    companion object {

        private const val PARAMS_KEY = "DETECTION_RESULTS"

        fun newIntent(context: Context, detectorResults: List<Page>): Intent {
            return Intent(context, MultiObjectsDetectorResultsActivity::class.java).apply {
                putParcelableArrayListExtra(PARAMS_KEY, ArrayList(detectorResults))
            }
        }
    }
}
