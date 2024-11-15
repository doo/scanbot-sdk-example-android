package io.scanbot.example

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.scanbot.example.common.Const
import io.scanbot.example.common.showToast
import io.scanbot.example.databinding.ActivityMainBinding
import io.scanbot.sdk.ScanbotSDK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val scanbotSdk: ScanbotSDK by lazy { ScanbotSDK(this) }

    private val selectGalleryImageResultLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (!scanbotSdk.licenseInfo.isValid) {
            this@MainActivity.showToast("1-minute trial license has expired!")
            Log.e(Const.LOG_TAG, "1-minute trial license has expired!")
            return@registerForActivityResult
        }

        if (uri == null) {
            showToast("Error obtaining selected image!")
            Log.e(Const.LOG_TAG, "Error obtaining selected image!")
            return@registerForActivityResult
        }

        lifecycleScope.launch {
            val documentId = createAndDetectDocumentPage(uri)

            if (documentId != null) {
                filterActivityResultLauncher.launch(FilterActivity.newIntent(this@MainActivity, documentId))
            } else {
                Log.e(Const.LOG_TAG, "Error creating document with page!")
                showToast("Error creating document with page!")
            }
        }
    }

    private val filterActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == RESULT_OK) {
                val documentId = result.data?.getStringExtra(FilterActivity.DOC_ID_EXTRA)

                if (documentId == null) {
                    Log.e(Const.LOG_TAG, "Error obtaining documentId from filter screen!")
                    showToast("Error obtaining documentId from filter screen!")
                    return@registerForActivityResult
                }

                val page = ScanbotSDK(this).documentApi.loadDocument(documentId)?.pages?.firstOrNull()
                if (page == null) {
                    Log.e(Const.LOG_TAG, "Error loading document with page!")
                    showToast("Error loading document with page!")
                    return@registerForActivityResult
                }

                val filter = page.filters.getOrNull(0)

                showToast("Filter was applied to page: ${filter.getFilterName()}")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.importFromLibBtn.setOnClickListener {
            selectGalleryImageResultLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private suspend fun createAndDetectDocumentPage(imageUri: Uri): String? {
        val bitmap = withContext(Dispatchers.IO) {
            val inputStream = contentResolver.openInputStream(imageUri)
            BitmapFactory.decodeStream(inputStream)
        }

        if (bitmap == null) {
            Log.e(Const.LOG_TAG, "Error loading image (bitmap is `null`)!")
            showToast("Error loading image!")
            return null
        }

        val sdk = ScanbotSDK(this)
        if (sdk.licenseInfo.isValid.not()) {
            Log.e(Const.LOG_TAG, "License is not valid!")
            showToast("License is not valid!")
            return null
        }

        binding.progressBar.visibility = View.VISIBLE
        val resultDocument = withContext(Dispatchers.Default) {
            val contourResult = sdk.createDocumentDetector().detect(bitmap)

            if (contourResult == null) {
                Log.e(Const.LOG_TAG, "Error detecting document (result is `null`)!")
                showToast("Error detecting document!")
                return@withContext null
            }
            Log.d(Const.LOG_TAG, "Doc detected: ${contourResult.status}")

            /** We allow all `OK_*` [statuses][DocumentDetectionStatus] just for purpose of this example.
             * Otherwise it is a good practice to differentiate between statuses and handle them accordingly.
             */
            val isDetectionOk = contourResult.status.name.startsWith("OK", true)
            if (isDetectionOk.not()) {
                Log.e(Const.LOG_TAG, "Bad document photo - detection status was ${contourResult.status.name}!")
                showToast("Bad document photo - status ${contourResult.status.name}!")
                return@withContext null
            }

            val document = sdk.documentApi.createDocument()
            val page = document.addPage(bitmap)
            Log.d(Const.LOG_TAG, "Page added: ${page.uuid}")
            page.apply(newPolygon = contourResult.pointsNormalized)
            document
        }

        binding.progressBar.visibility = View.GONE
        Log.d(Const.LOG_TAG, "Document created: ${resultDocument?.uuid}")

        return resultDocument?.uuid
    }
}
