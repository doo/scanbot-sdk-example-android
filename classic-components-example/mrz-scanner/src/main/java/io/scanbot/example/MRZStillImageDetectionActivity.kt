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
import io.scanbot.example.MRZResultActivity.Companion.newIntent
import io.scanbot.example.common.Const
import io.scanbot.example.common.showToast
import io.scanbot.example.databinding.ActivityMrzStillImageDetectionBinding
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.docprocessing.Page
import io.scanbot.sdk.ui_v2.common.activity.registerForActivityResultOk
import io.scanbot.sdk.ui_v2.document.DocumentScannerActivity
import io.scanbot.sdk.ui_v2.document.configuration.DocumentScanningFlow
import io.scanbot.sdk.util.PolygonHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MrzStillImageDetectionActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMrzStillImageDetectionBinding.inflate(layoutInflater) }
    private val scanbotSdk by lazy { ScanbotSDK(this) }
    private val mrzScanner by lazy { scanbotSdk.createMrzScanner() }

    private lateinit var page: Page

    private lateinit var docScannerResultLauncher: ActivityResultLauncher<DocumentScanningFlow>

    private val selectGalleryImageResultLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (!scanbotSdk.licenseInfo.isValid) {
                this@MrzStillImageDetectionActivity.showToast("1-minute trial license has expired!")
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
        docScannerResultLauncher =
            registerForActivityResultOk(DocumentScannerActivity.ResultContract(this@MrzStillImageDetectionActivity)) { resultEntity ->
                val document = resultEntity.result!!
                page = document.pageAtIndex(0) ?: kotlin.run {
                    Log.e(Const.LOG_TAG, "Error obtaining scanned page!")
                    this@MrzStillImageDetectionActivity.showToast("Error obtaining scanned page!")
                    return@registerForActivityResultOk
                }

                binding.resultImageView.setImageBitmap(page.documentImage)
                binding.cropBtn.visibility = View.VISIBLE
                binding.runRecognitionBtn.visibility = View.VISIBLE
            }
        binding.startScannerBtn.setOnClickListener {
            val config = DocumentScanningFlow().apply {
                this.outputSettings.pagesScanLimit = 1
                this.screens.camera.cameraConfiguration.autoSnappingEnabled = false
                this.screens.camera.cameraConfiguration.ignoreBadAspectRatio = true
            }
            docScannerResultLauncher.launch(config)
        }

        binding.importFromLibBtn.setOnClickListener { openGallery() }
        binding.runRecognitionBtn.setOnClickListener { runRecognition() }
    }

    private fun openGallery() {
        selectGalleryImageResultLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun runRecognition() {
        lifecycleScope.launch(Dispatchers.Default) {
            recognizeMrz(page)
        }
        binding.progressBar.visibility = View.VISIBLE
    }

    private suspend fun recognizeMrz(page: Page) {
        val mrzRecognitionResult = mrzScanner.recognizeMRZBitmap(page.documentImage, 0)

        withContext(Dispatchers.Main) {
            binding.progressBar.visibility = View.GONE
            if (mrzRecognitionResult != null && mrzRecognitionResult.recognitionSuccessful) {
                startActivity(newIntent(this@MrzStillImageDetectionActivity, mrzRecognitionResult))
            } else {
                Toast.makeText(
                    this@MrzStillImageDetectionActivity,
                    "No MRZ data recognized!", Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private suspend fun importImageToPage(uri: Uri) {
        val page = withContext(Dispatchers.Default) {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            val document = scanbotSdk.documentApi.createDocument()
            val page = document.addPage(bitmap)

            val contourResult =
                scanbotSdk.createContourDetector().detect(bitmap)?.polygonF ?: kotlin.run {
                    Log.e(Const.LOG_TAG, "Error detecting document on page " + page.uuid)
                    PolygonHelper.getFullPolygon()
                }
            page.apply(newPolygon = contourResult)
            page
        }
        this.page = page
        withContext(Dispatchers.Main) {
            binding.progressBar.visibility = View.GONE
            binding.resultImageView.setImageBitmap(page.documentImage)
            binding.cropBtn.visibility = View.VISIBLE
            binding.runRecognitionBtn.visibility = View.VISIBLE
        }
    }
}
