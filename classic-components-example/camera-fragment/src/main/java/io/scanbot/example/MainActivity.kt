package io.scanbot.example

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope


import io.scanbot.example.common.Const
import io.scanbot.example.common.applyEdgeToEdge
import io.scanbot.example.common.showToast
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.common.catchWithResult
import io.scanbot.sdk.documentscanner.DocumentScanner
import io.scanbot.sdk.image.ImageRef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
Ths example uses new sdk APIs presented in Scanbot SDK v.8.x.x
Please, check the official documentation for more details:
Result API https://docs.scanbot.io/android/document-scanner-sdk/detailed-setup-guide/result-api/
ImageRef API https://docs.scanbot.io/android/document-scanner-sdk/detailed-setup-guide/image-ref-api/
 */

class MainActivity : AppCompatActivity() {

    private val scanbotSdk: ScanbotSDK by lazy { ScanbotSDK(this) }
    private val scanner: DocumentScanner by lazy { scanbotSdk.createDocumentScanner().getOrThrow() }

    private val requestCameraLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openCameraDialog()
            } else {
                this@MainActivity.showToast("Camera permission is required to run this example!")
            }
        }

    private val selectGalleryImageResultLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
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

            if (scanbotSdk.licenseInfo.isValid) {
                lifecycleScope.launch { processImageForAutoDocumentScanning(uri) }
            } else {
                this@MainActivity.showToast("1-minute trial license has expired!")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        applyEdgeToEdge(findViewById(R.id.root_view))

        findViewById<View>(R.id.show_dialog_btn).setOnClickListener {
            requestCameraLauncher.launch(Manifest.permission.CAMERA)
        }

        findViewById<View>(R.id.import_image).setOnClickListener {
            selectGalleryImageResultLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private fun openCameraDialog() {
        val ft = supportFragmentManager.beginTransaction()
        val prev = supportFragmentManager.findFragmentByTag(DIALOG_TAG)
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(null)

        // Create and show the dialog.
        val newFragment: DialogFragment = CameraDialogFragment.newInstance()
        newFragment.show(ft, DIALOG_TAG)
    }

    /** Imports a selected image as original image and performs auto document scanning on it. */
    private suspend fun processImageForAutoDocumentScanning(imageUri: Uri) {
        val progressBar = findViewById<View>(R.id.progress_bar)
        val importResultImage = findViewById<ImageView>(R.id.import_result)
        withContext(Dispatchers.Main) {
            progressBar.visibility = View.VISIBLE
            this@MainActivity.showToast("Importing image...")
        }

        val page = withContext(Dispatchers.Default) {
            catchWithResult {
                // load the selected image:
                val inputStream = contentResolver.openInputStream(imageUri)
                    ?: throw IllegalStateException("Cannot open input stream from URI: $imageUri")
                val image = ImageRef.fromInputStream(inputStream)

                // create a new Document object with given image as original image:
                val newDocument = scanbotSdk.documentApi.createDocument()
                    .getOrReturn() // can be handled with .getOrNull() if needed
                val page = newDocument.addPage(image)
                    .getOrReturn() // can be handled with .getOrNull() if needed

                // run auto document scanning on it:
                val result = scanner.run(image).getOrReturn()

                /** We allow all `OK_*` [statuses][DocumentDetectionStatus] just for purpose of this example.
                 * Otherwise it is a good practice to differentiate between statuses and handle them accordingly.
                 */
                val statusOk = (result.status?.name?.startsWith("OK_")) ?: false
                if (statusOk && result.pointsNormalized.isNotEmpty()) {
                    // apply the detected polygon to the new page:
                    page.apply(newPolygon = result.pointsNormalized)
                }
                page
            }.getOrNull()
        }

        withContext(Dispatchers.Main) {
            progressBar.visibility = View.GONE
            // show Page's document image:
            importResultImage.setImageBitmap(page?.documentImage)
            importResultImage.visibility = View.VISIBLE
        }
    }

    private companion object {
        const val DIALOG_TAG = "scanbot_dialog"
    }
}
