package io.scanbot.example

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import io.scanbot.example.common.Const
import io.scanbot.example.common.applyEdgeToEdge
import io.scanbot.example.common.getAppStorageDir
import io.scanbot.example.common.showToast
import io.scanbot.example.databinding.ActivityMainBinding
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.imageprocessing.ParametricFilter
import io.scanbot.sdk.tiffgeneration.CompressionMode
import io.scanbot.sdk.tiffgeneration.TiffGeneratorParameters
import io.scanbot.sdk.tiffgeneration.UserField
import io.scanbot.sdk.tiffgeneration.UserFieldDoubleValue
import io.scanbot.sdk.tiffgeneration.UserFieldIntValue
import io.scanbot.sdk.tiffgeneration.UserFieldStringValue
import io.scanbot.sdk.util.FileChooserUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/**
Ths example uses new sdk APIs presented in Scanbot SDK v.8.x.x
Please, check the official documentation for more details:
Result API https://docs.scanbot.io/android/document-scanner-sdk/detailed-setup-guide/result-api/
ImageRef API https://docs.scanbot.io/android/document-scanner-sdk/detailed-setup-guide/image-ref-api/
 */

class MainActivity : AppCompatActivity() {

    private val scanbotSdk: ScanbotSDK by lazy { ScanbotSDK(this) }
    private val tiffGenerator by lazy { scanbotSdk.createTiffGeneratorManager() }

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

            binding.progressBar.visibility = View.VISIBLE

            lifecycleScope.launch {
                writeTiffImages(
                    uris,
                    binding.binarizationCheckBox.isChecked,
                    binding.customFieldsCheckBox.isChecked
                )
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportActionBar?.hide()
        applyEdgeToEdge(findViewById(R.id.root_view))

        binding.selectImagesButton.setOnClickListener {
            binding.resultTextView.text = ""
            selectGalleryImageResultLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private suspend fun writeTiffImages(
        imageUris: List<Uri>,
        binarize: Boolean,
        addCustomFields: Boolean
    ) {
        val appStorageDir = getAppStorageDir(this@MainActivity)
        appStorageDir.mkdirs()
        val resultFile =
            File(appStorageDir, "tiff_result_${System.currentTimeMillis()}.tiff")

        val result = withContext(Dispatchers.IO) {

            // Convert URIs to local files DON'T USE IN PRODUCTION
            val files = imageUris.toTypedArray().map { uri ->

                val file = appStorageDir.resolve(
                    UUID.randomUUID().toString() + ".jpg"
                )
                file.createNewFile()
                file.outputStream().use { output ->
                    val openInputStream = contentResolver.openInputStream(uri)
                    openInputStream.use { input ->
                        input?.copyTo(output)
                    }
                }
                file
            }

            tiffGenerator.generateFromFiles(
                files,
                false,
                resultFile,
                constructParameters(binarize, addCustomFields)
            ).getOrNull()
        }

        withContext(Dispatchers.Main)
        {
            binding.progressBar.visibility = View.GONE
            if (result != null) {
                binding.resultTextView.text = "TIFF file created: ${resultFile.path}"
            } else {
                this@MainActivity.showToast("ERROR: Could not create TIFF file.")
            }
        }
    }

    private fun constructParameters(
        binarize: Boolean,
        addCustomFields: Boolean
    ): TiffGeneratorParameters {
        // Please note that some compression types are only compatible for binarized images (1-bit encoded black & white images)!
        val compression =
            if (binarize) CompressionMode.CCITT_T4
            else CompressionMode.ADOBE_DEFLATE

        // Example for custom tags (fields) as userDefinedFields.
        // Please note the range for custom tag IDs and refer to TIFF specifications.
        val userDefinedFields = if (addCustomFields) {
            arrayListOf(
                UserField(
                    65001,
                    "custom_string_field_name",
                    UserFieldStringValue("testStringValue"),
                ),
                UserField(
                    65001,
                    "custom_number_field_name",
                    UserFieldIntValue(100)
                ),
                UserField(
                    65535,
                    "custom_number_field_name",
                    UserFieldDoubleValue(42.001)
                ),
            )
        } else {
            arrayListOf()
        }
        val binarizationFilter =
            if (binarize) ParametricFilter.scanbotBinarizationFilter() else null
        return TiffGeneratorParameters(
            binarizationFilter = binarizationFilter,
            dpi = DPI,
            compression = compression,
            userFields = userDefinedFields
        )
    }

    private companion object {
        private const val DPI = 200
    }
}
