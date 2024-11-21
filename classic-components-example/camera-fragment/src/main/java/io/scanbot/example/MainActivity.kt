package io.scanbot.example

import android.Manifest
import android.graphics.BitmapFactory
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
import io.scanbot.example.common.showToast
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.core.documentdetector.DocumentDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private val scanbotSdk: ScanbotSDK by lazy { ScanbotSDK(this) }
    private val contourDetector: DocumentDetector by lazy { scanbotSdk.createDocumentDetector() }

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
                lifecycleScope.launch { processImageForAutoDocumentDetection(uri) }
            } else {
                this@MainActivity.showToast("1-minute trial license has expired!")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

    /** Imports a selected image as original image and performs auto document detection on it. */
    private suspend fun processImageForAutoDocumentDetection(imageUri: Uri) {
        val progressBar = findViewById<View>(R.id.progress_bar)
        val importResultImage = findViewById<ImageView>(R.id.import_result)
        withContext(Dispatchers.Main) {
            progressBar.visibility = View.VISIBLE
            this@MainActivity.showToast("Importing image...")
        }

        val page = withContext(Dispatchers.Default) {
            // load the selected image:
            val inputStream = contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            // create a new Document object with given image as original image:
            val newDocument = scanbotSdk.documentApi.createDocument()
            val page = newDocument.addPage(bitmap)

            // run auto document detection on it:
            val detectionResult = contourDetector.detect(bitmap)

            /** We allow all `OK_*` [statuses][DocumentDetectionStatus] just for purpose of this example.
             * Otherwise it is a good practice to differentiate between statuses and handle them accordingly.
             */
            val isDetectionStatusOk = (detectionResult?.status?.name?.startsWith("OK_")) ?: false
            if (detectionResult != null && isDetectionStatusOk && detectionResult.pointsNormalized.isNotEmpty()) {
                // apply the detected polygon to the new page:
                page.apply(newPolygon = detectionResult.pointsNormalized)
            }
            page
        }

        withContext(Dispatchers.Main) {
            progressBar.visibility = View.GONE
            // show Page's document image:
            importResultImage.setImageBitmap(page.documentImage)
            importResultImage.visibility = View.VISIBLE
        }
    }

    private companion object {
        const val DIALOG_TAG = "scanbot_dialog"
    }
}
