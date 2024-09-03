package io.scanbot.example

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import io.scanbot.example.common.showToast
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.core.contourdetector.DocumentDetectionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SelectImageActivity : AppCompatActivity() {

    private lateinit var progressView: View

    private val selectGalleryImageResultLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                lifecycleScope.launch {
                    val documentId = createAndDetectDocumentPage(it)

                    if (documentId != null) {
                        filterActivityResultLauncher.launch(FilterActivity.newIntent(this@SelectImageActivity, documentId))
                    } else {
                        Log.e(LOG_TAG, "Error creating document with page!")
                        showToast("Error creating document with page!")
                    }
                }
            }
        }

    private val filterActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val documentId = result.data?.getStringExtra(FilterActivity.DOC_ID_EXTRA)

                if (documentId == null) {
                    Log.e(LOG_TAG, "Error obtaining documentId from filter screen!")
                    showToast("Error obtaining documentId from filter screen!")
                    return@registerForActivityResult
                }

                val page = ScanbotSDK(this).documentApi.loadDocument(documentId, false)?.pages?.firstOrNull()
                if (page == null) {
                    Log.e(LOG_TAG, "Error loading document with page!")
                    showToast("Error loading document with page!")
                    return@registerForActivityResult
                }

                val filter = page.filters.getOrNull(0)

                showToast("Filter was applied to page: ${filter.getFilterName()}")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_import_image)

        progressView = findViewById(R.id.progressBar)

        askPermission()

        findViewById<View>(R.id.import_from_lib_btn).setOnClickListener {
            selectGalleryImageResultLauncher.launch("image/*")
        }
    }

    private fun askPermission() {
        if (checkPermissionNotGranted(Manifest.permission.READ_EXTERNAL_STORAGE) ||
            checkPermissionNotGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                ), 999
            )
        }
    }

    private fun checkPermissionNotGranted(permission: String) =
        ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED

    private suspend fun createAndDetectDocumentPage(imageUri: Uri): String? {
        val bitmap = withContext(Dispatchers.IO) {
            MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        }

        if (bitmap == null) {
            Log.e(LOG_TAG, "Error loading image (bitmap is `null`)!")
            showToast("Error loading image!")
            return null
        }

        val sdk = ScanbotSDK(this)
        if (sdk.licenseInfo.isValid.not()) {
            Log.e(LOG_TAG, "License is not valid!")
            showToast("License is not valid!")
            return null
        }

        progressView.visibility = View.VISIBLE
        val resultDocument = withContext(Dispatchers.Default) {
            val contourResult = sdk.createContourDetector().detect(bitmap)

            if (contourResult == null) {
                Log.e(LOG_TAG, "Error detecting document (result is `null`)!")
                showToast("Error detecting document!")
                return@withContext null
            }
            Log.d(LOG_TAG, "Doc detected: ${contourResult.status}")

            /** We allow all `OK_*` [statuses][DocumentDetectionStatus] just for purpose of this example.
             * Otherwise it is a good practice to differentiate between statuses and handle them accordingly.
             */
            val isDetectionOk = contourResult.status.name.startsWith("OK", true)
            if (isDetectionOk.not()) {
                Log.e(LOG_TAG, "Bad document photo - detection status was ${contourResult.status.name}!")
                showToast("Bad document photo - status ${contourResult.status.name}!")
                return@withContext null
            }

            val document = sdk.documentApi.createDocument()
            val page = document.addPage(bitmap)
            Log.d(LOG_TAG, "Page added: ${page.uuid}")
            page.apply(newPolygon = contourResult.polygonF)
            document
        }

        progressView.visibility = View.GONE
        Log.d(LOG_TAG, "Document created: ${resultDocument?.uuid}")

        return resultDocument?.uuid
    }

    companion object {
        private const val LOG_TAG = "SelectImageActivity"
    }
}
