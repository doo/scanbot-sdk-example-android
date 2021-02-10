package io.scanbot.example

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.process.ImageFilterType
import io.scanbot.sdk.tiff.TIFFWriter
import io.scanbot.sdk.tiff.model.TIFFImageWriterCompressionOptions
import io.scanbot.sdk.tiff.model.TIFFImageWriterParameters
import io.scanbot.sdk.tiff.model.TIFFImageWriterUserDefinedField
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var tiffWriter: TIFFWriter
    private lateinit var progressView: View
    private lateinit var resultTextView: TextView
    private lateinit var binarizationCheckBox: CheckBox
    private lateinit var customFieldsCheckBox: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        askPermission()

        tiffWriter = ScanbotSDK(this).tiffWriter()
        resultTextView = findViewById(R.id.resultTextView)
        binarizationCheckBox = findViewById(R.id.binarizationCheckBox)
        customFieldsCheckBox = findViewById(R.id.customFieldsCheckBox)

        findViewById<View>(R.id.selectImagesButton).setOnClickListener {
            resultTextView.text = ""
            openGallery()
        }

        progressView = findViewById(R.id.progressBar)
    }

    private fun askPermission() {
        if (checkPermissionNotGranted(Manifest.permission.READ_EXTERNAL_STORAGE) ||
                checkPermissionNotGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE), 999)
        }
    }

    private fun checkPermissionNotGranted(permission: String) =
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED


    private fun openGallery() {
        val intent = Intent().apply {
            type = IMAGE_TYPE
            action = Intent.ACTION_GET_CONTENT
            putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }

        startActivityForResult(Intent.createChooser(intent, "Select picture"), SELECT_PICTURE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode != SELECT_PICTURE_REQUEST || resultCode != Activity.RESULT_OK) {
            return
        }
        if (!ScanbotSDK(this).isLicenseValid) {
            Toast.makeText(this,
                    "Scanbot SDK license is not valid or the trial minute has expired.",
                    Toast.LENGTH_LONG).show()
            return
        }
        progressView.visibility = View.VISIBLE

        intent?.let {
            processGalleryResult(it)
        }
    }

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
        WriteTIFFImageTask(imageUris, binarizationCheckBox.isChecked, customFieldsCheckBox.isChecked).execute()
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

    /*
    This AsyncTask is used here only for the sake of example. Please, try to avoid usage of
    AsyncTasks in your application
     */
    private inner class WriteTIFFImageTask(imageUris: List<Uri>, binarize: Boolean, addCustomFields: Boolean) : AsyncTask<Void, Void, Boolean>() {
        private val dpi = 200

        private val images: MutableList<Bitmap> = ArrayList()
        private val resultFile: File
        private val parameters: TIFFImageWriterParameters

        init {
            for (uri in imageUris) {
                images.add(MediaStore.Images.Media.getBitmap(contentResolver, uri))
            }
            resultFile = File(getExternalFilesDir(null)!!.path + "/tiff_result_" + System.currentTimeMillis() + ".tiff")

            // Please note that some compression types are only compatible for binarized images (1-bit encoded black & white images)!
            val compression =
                    if (binarize) TIFFImageWriterCompressionOptions.COMPRESSION_CCITTFAX4 else TIFFImageWriterCompressionOptions.COMPRESSION_ADOBE_DEFLATE

            // Example for custom tags (fields) as userDefinedFields.
            // Please note the range for custom tag IDs and refer to TIFF specifications.
            val userDefinedFields = if (addCustomFields) {
                arrayListOf(TIFFImageWriterUserDefinedField.fieldWithStringValue("testStringValue", "custom_string_field_name", 65000),
                        TIFFImageWriterUserDefinedField.fieldWithIntValue(100, "custom_number_field_name", 65001),
                        TIFFImageWriterUserDefinedField.fieldWithDoubleValue(42.001, "custom_double_field_name", 65535))
            } else {
                arrayListOf()
            }
            val imageFilterType = if (binarize) ImageFilterType.PURE_BINARIZED else ImageFilterType.NONE
            parameters = TIFFImageWriterParameters(imageFilterType, dpi, compression, userDefinedFields)
        }

        override fun doInBackground(vararg voids: Void): Boolean {
            return tiffWriter.writeTIFFFromImages(images.toTypedArray(), resultFile, parameters)
        }

        override fun onPostExecute(result: Boolean) {
            progressView.visibility = View.GONE
            if (result) {
                resultTextView.text = """
                    TIFF file created:
                    ${resultFile.path}
                    """.trimIndent()
            } else {
                Toast.makeText(this@MainActivity,
                        "ERROR: Could not create TIFF file.",
                        Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        private const val SELECT_PICTURE_REQUEST = 100
        private const val IMAGE_TYPE = "image/*"
    }
}