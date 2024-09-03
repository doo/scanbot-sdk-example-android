package io.scanbot.example

import android.Manifest
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import io.scanbot.example.common.getAppStorageDir
import io.scanbot.example.common.showToast
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.imagefilters.LegacyFilter
import io.scanbot.sdk.process.ImageFilterType
import io.scanbot.sdk.tiff.TIFFWriter
import io.scanbot.sdk.tiff.model.TIFFImageWriterCompressionOptions
import io.scanbot.sdk.tiff.model.TIFFImageWriterParameters
import io.scanbot.sdk.tiff.model.TIFFImageWriterUserDefinedField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : AppCompatActivity() {

    private val dpi = 200

    private lateinit var tiffWriter: TIFFWriter
    private lateinit var progressView: View
    private lateinit var resultTextView: TextView
    private lateinit var binarizationCheckBox: CheckBox
    private lateinit var customFieldsCheckBox: CheckBox

    private val scanbotSdk: ScanbotSDK by lazy { ScanbotSDK(this) }

    private val selectGalleryImageResultLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            if (!scanbotSdk.licenseInfo.isValid) {
                this@MainActivity.showToast("Scanbot SDK license (1-minute trial) has expired!")
                return@registerForActivityResult
            }
            progressView.visibility = View.VISIBLE

            intent?.let {
                processGalleryResult(it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        askPermission()

        tiffWriter = scanbotSdk.createTiffWriter()
        resultTextView = findViewById(R.id.resultTextView)
        binarizationCheckBox = findViewById(R.id.binarizationCheckBox)
        customFieldsCheckBox = findViewById(R.id.customFieldsCheckBox)

        findViewById<View>(R.id.selectImagesButton).setOnClickListener {
            resultTextView.text = ""
            selectGalleryImageResultLauncher.launch("image/*")
        }

        progressView = findViewById(R.id.progressBar)
    }

    private fun askPermission() {
        if (checkPermissionNotGranted(Manifest.permission.READ_EXTERNAL_STORAGE) ||
            checkPermissionNotGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), 999
            )
        }
    }

    private fun checkPermissionNotGranted(permission: String) =
        ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED

    private fun processGalleryResult(data: Intent) {
        val clipData = data.clipData
        val singleImageUri = data.data
        val imageUris: MutableList<Uri> = ArrayList()
        if (clipData != null && clipData.itemCount > 0) {
            // multiple images were selected
            imageUris.addAll(getImageUris(clipData))
        } else if (singleImageUri != null) {
            // a single image was selected
            imageUris.add(singleImageUri)
        }

        lifecycleScope.launch {
            writeTiffImages(imageUris, binarizationCheckBox.isChecked, customFieldsCheckBox.isChecked)
        }
    }

    private suspend fun writeTiffImages(imageUris: List<Uri>, binarize: Boolean, addCustomFields: Boolean) {
        var result: Boolean
        val resultFile = File(getAppStorageDir(), "tiff_result_${System.currentTimeMillis()}.tiff")

        withContext(Dispatchers.IO) {
            val images: MutableList<Bitmap> = ArrayList()
            for (uri in imageUris) {
                images.add(MediaStore.Images.Media.getBitmap(contentResolver, uri))
            }

            result = tiffWriter.writeTIFF(images.toTypedArray(), resultFile, constructParameters(binarize, addCustomFields))
        }

        withContext(Dispatchers.Main) {
            progressView.visibility = View.GONE
            if (result) {
                resultTextView.text = "TIFF file created: ${resultFile.path}"
            } else {
                this@MainActivity.showToast("ERROR: Could not create TIFF file.")
            }
        }
    }

    private fun constructParameters(binarize: Boolean, addCustomFields: Boolean): TIFFImageWriterParameters {
        // Please note that some compression types are only compatible for binarized images (1-bit encoded black & white images)!
        val compression =
            if (binarize) TIFFImageWriterCompressionOptions.COMPRESSION_CCITTFAX4 else TIFFImageWriterCompressionOptions.COMPRESSION_ADOBE_DEFLATE

        // Example for custom tags (fields) as userDefinedFields.
        // Please note the range for custom tag IDs and refer to TIFF specifications.
        val userDefinedFields = if (addCustomFields) {
            arrayListOf(
                TIFFImageWriterUserDefinedField.fieldWithStringValue("testStringValue", "custom_string_field_name", 65000),
                TIFFImageWriterUserDefinedField.fieldWithIntValue(100, "custom_number_field_name", 65001),
                TIFFImageWriterUserDefinedField.fieldWithDoubleValue(42.001, "custom_double_field_name", 65535)
            )
        } else {
            arrayListOf()
        }
        val binarizationFilter = if (binarize) LegacyFilter(ImageFilterType.PURE_BINARIZED.code) else null
        return TIFFImageWriterParameters(binarizationFilter, dpi, compression, userDefinedFields)
    }

    private fun getImageUris(clipData: ClipData): List<Uri> {
        val imageUris: MutableList<Uri> = ArrayList()
        for (i in 0 until clipData.itemCount) {
            val uri = clipData.getItemAt(i).uri
            if (uri != null) {
                imageUris.add(uri)
            }
        }
        return imageUris
    }
}
