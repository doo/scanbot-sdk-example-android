package io.scanbot.example

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import io.scanbot.ehicscanner.model.EhicDetectionStatus
import io.scanbot.example.EhicResultActivity.Companion.newIntent
import io.scanbot.example.common.Const
import io.scanbot.example.common.showToast
import io.scanbot.example.databinding.ActivityEhicStillImageDetectionBinding
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.docprocessing.Page
import io.scanbot.sdk.hicscanner.HealthInsuranceCardScanner
import io.scanbot.sdk.ui_v2.common.activity.registerForActivityResultOk
import io.scanbot.sdk.ui_v2.document.DocumentScannerActivity
import io.scanbot.sdk.ui_v2.document.configuration.AcknowledgementMode
import io.scanbot.sdk.ui_v2.document.configuration.DocumentScanningFlow
import io.scanbot.sdk.util.PolygonHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EhicStillImageDetectionActivity : AppCompatActivity() {

    private val scanbotSdk: ScanbotSDK by lazy { ScanbotSDK(this) }
    private val healthInsuranceCardScanner: HealthInsuranceCardScanner by lazy { scanbotSdk.createHealthInsuranceCardScanner() }

    private var page: Page? = null

    private val binding by lazy { ActivityEhicStillImageDetectionBinding.inflate(layoutInflater) }

    private val docScannerResultLauncher: ActivityResultLauncher<DocumentScanningFlow> by lazy {
        registerForActivityResultOk(DocumentScannerActivity.ResultContract(this)) { resultEntity ->
            val document = resultEntity.result!!
            page = document.pages.first().also { binding.resultImageView.setImageBitmap(it.documentImage) }
            binding.runRecognitionBtn.visibility = View.VISIBLE
        }
    }

    private val selectGalleryImageResultLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (!scanbotSdk.licenseInfo.isValid) {
            this@EhicStillImageDetectionActivity.showToast("1-minute trial license has expired!")
            Log.e(Const.LOG_TAG, "1-minute trial license has expired!")
            return@registerForActivityResult
        }

        if (uri == null) {
            showToast("Error obtaining selected image!")
            Log.e(Const.LOG_TAG, "Error obtaining selected image!")
            return@registerForActivityResult
        }

        binding.progress.isVisible = true
        lifecycleScope.launch { importedImageToPage(uri) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.startScannerBtn.setOnClickListener {
            val configuration = DocumentScanningFlow().apply {
                this.outputSettings.pagesScanLimit = 1
                this.screens.camera.cameraConfiguration.autoSnappingEnabled = false
                this.screens.camera.acknowledgement.acknowledgementMode = AcknowledgementMode.NONE
            }
            docScannerResultLauncher.launch(configuration)
        }

        binding.importFromLibBtn.setOnClickListener { openGallery() }
        binding.runRecognitionBtn.setOnClickListener { runRecognition() }
    }

    private fun openGallery() {
        selectGalleryImageResultLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun loadImage(imageUri: Uri): Bitmap {
        val inputStream = contentResolver.openInputStream(imageUri)
        return BitmapFactory.decodeStream(inputStream)
    }

    private fun runRecognition() {
        if (!scanbotSdk.licenseInfo.isValid) {
            showToast("1-minute trial license has expired!")
            return
        }

        page?.let { page ->
            binding.progress.isVisible = true

            lifecycleScope.launch {
                recognizeEhic(page)
            }
        }
    }

    private suspend fun recognizeEhic(page: Page) {
        val result = withContext(Dispatchers.Default) {
            val imageUri = page.documentFileUri
            val documentImage = loadImage(imageUri)
            healthInsuranceCardScanner.recognizeBitmap(documentImage, 0)
        }

        withContext(Dispatchers.Main) {
            binding.progress.isVisible = false
            if (result != null && result.status == EhicDetectionStatus.SUCCESS) {
                startActivity(newIntent(this@EhicStillImageDetectionActivity, result))
            } else {
                this@EhicStillImageDetectionActivity.showToast("No EHIC data recognized!")
            }
        }
    }

    private suspend fun importedImageToPage(uri: Uri) {
        withContext(Dispatchers.Default) {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            val detectionResult = scanbotSdk.createContourDetector().detect(bitmap)
            val document = scanbotSdk.documentApi.createDocument()
            page = document.addPage(bitmap).apply {
                apply(newPolygon = detectionResult?.polygonF ?: PolygonHelper.getFullPolygon())
            }
        }

        withContext(Dispatchers.Main) {
            binding.progress.visibility = View.GONE
            binding.runRecognitionBtn.visibility = View.VISIBLE
            page?.let { binding.resultImageView.setImageBitmap(it.documentImage) }
        }
    }
}
