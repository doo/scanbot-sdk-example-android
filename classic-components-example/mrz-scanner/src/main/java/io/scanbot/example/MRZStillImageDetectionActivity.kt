package io.scanbot.example

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.scanbot.common.getOrNull
import io.scanbot.common.getOrThrow
import io.scanbot.example.MRZResultActivity.Companion.newIntent
import io.scanbot.example.common.Const
import io.scanbot.example.common.applyEdgeToEdge
import io.scanbot.example.common.showToast
import io.scanbot.example.databinding.ActivityMrzStillImageScanningBinding
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.docprocessing.Page
import io.scanbot.sdk.image.ImageRef
import io.scanbot.sdk.ui_v2.common.activity.registerForActivityResultOk
import io.scanbot.sdk.ui_v2.document.DocumentScannerActivity
import io.scanbot.sdk.ui_v2.document.configuration.DocumentScanningFlow
import io.scanbot.sdk.util.PolygonHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MrzStillImageScanningActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMrzStillImageScanningBinding.inflate(layoutInflater) }
    private val scanbotSdk by lazy { ScanbotSDK(this) }
    private val mrzScanner by lazy { scanbotSdk.createMrzScanner().getOrThrow().apply {
        setConfiguration(this.copyCurrentConfiguration().apply {
            // frame accumulation is not needed for still image scanning
            this.frameAccumulationConfiguration.minimumNumberOfRequiredFramesWithEqualScanningResult = 1
        })
    } }

    private lateinit var page: Page

    private lateinit var docScannerResultLauncher: ActivityResultLauncher<DocumentScanningFlow>

    private val selectGalleryImageResultLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (!scanbotSdk.licenseInfo.isValid) {
                this@MrzStillImageScanningActivity.showToast("1-minute trial license has expired!")
                Log.e(Const.LOG_TAG, "1-minute trial license has expired!")
                return@registerForActivityResult
            }

            if (uri == null) {
                showToast("Error obtaining selected image!")
                Log.e(Const.LOG_TAG, "Error obtaining selected image!")
                return@registerForActivityResult
            }

            lifecycleScope.launch {
                importImageToPage(uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportActionBar!!.hide()
        applyEdgeToEdge(findViewById(R.id.root_view))

        docScannerResultLauncher =
            registerForActivityResultOk(DocumentScannerActivity.ResultContract()) { resultEntity ->
                val document = resultEntity.result!!
                page = document.pageAtIndex(0) ?: kotlin.run {
                    Log.e(Const.LOG_TAG, "Error obtaining scanned page!")
                    this@MrzStillImageScanningActivity.showToast("Error obtaining scanned page!")
                    return@registerForActivityResultOk
                }

                binding.resultImageView.setImageBitmap(page.documentImage)
                binding.runScanningBtn.visibility = View.VISIBLE
            }
        binding.startScannerBtn.setOnClickListener {
            val config = DocumentScanningFlow().apply {
                this.outputSettings.pagesScanLimit = 1
                this.screens.camera.cameraConfiguration.autoSnappingEnabled = false
                this.screens.camera.scannerParameters.ignoreOrientationMismatch = true
            }
            docScannerResultLauncher.launch(config)
        }

        binding.importFromLibBtn.setOnClickListener { openGallery() }
        binding.runScanningBtn.setOnClickListener { runRecognition() }
    }

    private fun openGallery() {
        selectGalleryImageResultLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun runRecognition() {
        lifecycleScope.launch(Dispatchers.Default) {
            scanMrz(page)
        }
        binding.progressBar.visibility = View.VISIBLE
    }

    private suspend fun scanMrz(page: Page) {
        val result = mrzScanner.run(page.documentImageRef!!).getOrNull()

        withContext(Dispatchers.Main) {
            binding.progressBar.visibility = View.GONE
            if (result != null && result.success) {
                startActivity(newIntent(this@MrzStillImageScanningActivity, result))
            } else {
                Toast.makeText(
                    this@MrzStillImageScanningActivity,
                    "No MRZ data found!", Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private suspend fun importImageToPage(uri: Uri) {
        val page = withContext(Dispatchers.Default) {
            val inputStream = contentResolver.openInputStream(uri) ?: throw IllegalStateException("Cannot open input stream from URI: $uri")
            val image = ImageRef.fromInputStream(inputStream)

            val document = scanbotSdk.documentApi.createDocument()
            val page = document.addPage(image)

            val documentScanner = scanbotSdk.createDocumentScanner().getOrThrow()
            val contourResult =
                documentScanner.run(image).getOrNull()?.pointsNormalized ?: kotlin.run {
                    Log.e(Const.LOG_TAG, "Error finding document on page " + page.uuid)
                    PolygonHelper.getFullPolygon()
                }
            page.apply(newPolygon = contourResult)
            page
        }
        this.page = page
        withContext(Dispatchers.Main) {
            binding.progressBar.visibility = View.GONE
            binding.resultImageView.setImageBitmap(page.documentImage)
            binding.runScanningBtn.visibility = View.VISIBLE
        }
    }
}
