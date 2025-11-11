package io.scanbot.example

import android.content.Intent
import android.content.pm.PackageManager
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
import io.scanbot.common.mapSuccess
import io.scanbot.common.onSuccess
import io.scanbot.example.common.Const
import io.scanbot.example.common.applyEdgeToEdge
import io.scanbot.example.common.showToast
import io.scanbot.example.databinding.ActivityMainBinding
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.image.ImageRef
import io.scanbot.sdk.imageprocessing.ParametricFilter
import io.scanbot.sdk.ocr.OcrEngineManager
import io.scanbot.sdk.pdfgeneration.PageSize
import io.scanbot.sdk.pdfgeneration.PdfConfiguration
import io.scanbot.sdk.pdfgeneration.PdfGenerator
import io.scanbot.sdk.util.PolygonHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File

/**
Ths example uses new sdk APIs presented in Scanbot SDK v.8.x.x
Please, check the official documentation for more details:
Result API https://docs.scanbot.io/android/document-scanner-sdk/detailed-setup-guide/result-api/
ImageRef API https://docs.scanbot.io/android/document-scanner-sdk/detailed-setup-guide/image-ref-api/
 */

class MainActivity : AppCompatActivity() {
    private val runOcr = false
    private val scanbotSdk by lazy { ScanbotSDK(this) }
    private val pdfGenerator by lazy { scanbotSdk.createPdfGenerator(if (runOcr) OcrEngineManager.OcrConfig() else null) }

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
        supportActionBar!!.hide()
        applyEdgeToEdge(findViewById(R.id.root_view))

        binding.scanButton.setOnClickListener {
            selectGalleryImageResultLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private suspend fun processDocument(uris: List<Uri>, applyGrayscale: Boolean) {
        val filters =
            if (applyGrayscale) listOf(ParametricFilter.grayscaleFilter()) else emptyList()
        withContext(Dispatchers.Main) { binding.progressBar.visibility = View.VISIBLE }

        withContext(Dispatchers.Default) {
            scanbotSdk.createDocumentScanner().mapSuccess { documentScanner ->
                //can be handled with .getOrNull() if needed
                val document = scanbotSdk.documentApi.createDocument()
                    .getOrReturn() //can be handled with .getOrNull() if needed
                uris.asSequence().forEach { uri ->
                    val imageRef = contentResolver.openInputStream(uri)?.use { inputStream ->
                        ImageRef.fromInputStream(inputStream)
                    }
                    if (imageRef == null) {
                        Log.w(Const.LOG_TAG, "Cannot open input stream from URI: $uri")
                        return@forEach
                    }
                    val newPolygon = documentScanner.run(imageRef).getOrNull()?.pointsNormalized
                        ?: PolygonHelper.getFullPolygon()
                    val page = document.addPage(imageRef)
                        .getOrReturn() //can be handled with .getOrNull() if needed
                    page.apply(newPolygon = newPolygon, newFilters = filters)
                }
                pdfGenerator.generate(
                    document,
                    PdfConfiguration.default().copy(pageSize = PageSize.A4)
                )
                document.pdfUri.toFile()
            }.onSuccess { renderedPdfFile ->
                runBlocking(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    openPdfDocument(renderedPdfFile)
                }
            }
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
            val resInfoList = this.packageManager.queryIntentActivities(
                chooser,
                PackageManager.MATCH_DEFAULT_ONLY
            )

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
