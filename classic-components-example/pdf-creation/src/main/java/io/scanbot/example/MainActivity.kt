package io.scanbot.example

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.lifecycle.lifecycleScope
import io.scanbot.example.common.Const
import io.scanbot.example.common.showToast
import io.scanbot.example.databinding.ActivityMainBinding
import io.scanbot.pdf.model.PageSize
import io.scanbot.pdf.model.PdfConfig
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.imagefilters.ParametricFilter
import io.scanbot.sdk.process.PDFRenderer
import io.scanbot.sdk.util.PolygonHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : AppCompatActivity() {

    private val scanbotSdk: ScanbotSDK by lazy { ScanbotSDK(this) }
    private val pdfRenderer: PDFRenderer by lazy { scanbotSdk.createPdfRenderer() }

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val selectGalleryImageResultLauncher =
        // limit to 5 images for example purposes
        registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(5)) { uris ->
            if (uris.isEmpty()) {
                this@MainActivity.showToast("No images were selected!")
                Log.w(Const.LOG_TAG, "No images were selected!")
                return@registerForActivityResult
            }

            if (!scanbotSdk.licenseInfo.isValid) {
                this@MainActivity.showToast("Scanbot SDK license (1-minute trial) has expired!")
                Log.w(Const.LOG_TAG, "Scanbot SDK license (1-minute trial) has expired!")
                return@registerForActivityResult
            }

            lifecycleScope.launch { processDocument(uris, isGrayscaleChecked) }
        }

    private val isGrayscaleChecked: Boolean
        get() = binding.grayscaleCheckBox.isChecked

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.scanButton.setOnClickListener {
            selectGalleryImageResultLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private suspend fun processDocument(uris: List<Uri>, applyGrayscale: Boolean) {
        val filters = if (applyGrayscale) listOf(ParametricFilter.grayscaleFilter()) else emptyList()
        withContext(Dispatchers.Main) { binding.progressBar.visibility = View.VISIBLE }

        val renderedPdfFile = withContext(Dispatchers.Default) {
            val contourDetector = scanbotSdk.createContourDetector()
            val document = scanbotSdk.documentApi.createDocument()
            uris.asSequence().forEach { uri ->
                val inputStream = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val pageDetected = contourDetector.detect(bitmap)?.polygonF ?: PolygonHelper.getFullPolygon()
                document.addPage(bitmap).apply(newPolygon = pageDetected, newFilters = filters)
            }

            pdfRenderer.render(document, PdfConfig.defaultConfig().copy(pageSize = PageSize.A4))
            document.pdfUri.toFile()
        }

        withContext(Dispatchers.Main) {
            binding.progressBar.visibility = View.GONE
            openPdfDocument(renderedPdfFile)
        }
    }

    private fun openPdfDocument(file: File) {
        val uri = FileProvider.getUriForFile(this, "${this.packageName}.provider", file)

        val openIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        if (intent.resolveActivity(packageManager) != null) {
            val chooser = Intent.createChooser(openIntent, file.name)
            val resInfoList = this.packageManager.queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY)

            for (resolveInfo in resInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(chooser)
        } else {
            // Handle the case where no app can open PDF files
            showToast("No app found to open PDF files")
            Log.w(Const.LOG_TAG, "No app found to open PDF files")
        }
    }
}
