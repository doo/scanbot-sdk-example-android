package io.scanbot.example

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.example.common.ImportImageContract
import io.scanbot.sdk.core.contourdetector.DocumentDetectionStatus
import io.scanbot.sdk.docprocessing.legacy.PageProcessor
import io.scanbot.sdk.persistence.page.PageFileStorage
import io.scanbot.sdk.process.ImageFilterType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    val scanbotSDK = ScanbotSDK(this)
    val contourDetector = scanbotSDK.createContourDetector()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val galleryImageLauncher =
            registerForActivityResult(ImportImageContract(this)) { resultEntity ->
                lifecycleScope.launch(Dispatchers.Default) {
                    val activity = this@MainActivity
                    val sdk = ScanbotSDK(activity)
                    if (!sdk.licenseInfo.isValid) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                activity,
                                "License has expired!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        resultEntity?.let { bitmap ->
                            processImageForAutoDocumentDetection(
                                bitmap
                            )
                        }
                    }
                }
            }

        findViewById<View>(R.id.show_dialog_btn).setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(
                        this@MainActivity,
                        Manifest.permission.CAMERA
                    )
                ) {
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.CAMERA),
                        PERMISSIONS_REQUEST_CAMERA
                    )
                }
            } else {
                openCameraDialog()
            }
        }

        findViewById<View>(R.id.import_image).setOnClickListener {
            galleryImageLauncher.launch(Unit)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CAMERA) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCameraDialog()
            }
        }
    }

    private fun openCameraDialog() {
        val ft = supportFragmentManager.beginTransaction()
        val prev = supportFragmentManager.findFragmentByTag("dialog")
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(null)

        // Create and show the dialog.
        val newFragment: DialogFragment = CameraDialogFragment.newInstance()
        newFragment.show(ft, "dialog")
    }

    /** Imports a selected image as original image and performs auto document detection on it. */
    suspend fun processImageForAutoDocumentDetection(
        bitmap: Bitmap
    ) {
        val progressBar = findViewById<View>(R.id.progress_bar)
        val importResultImage = findViewById<ImageView>(R.id.import_result)
        withContext(Dispatchers.Main) {
            progressBar.visibility = View.VISIBLE
            Toast.makeText(
                this@MainActivity,
                "importing page", Toast.LENGTH_LONG
            ).show()
        }

        val page = withContext(Dispatchers.Default) {
            // create a new Document object with given image as original image:
            val newDocument = scanbotSDK.documentApi.createDocument()
            val page = newDocument.addPage(bitmap)
            // run auto document detection on it:
            val detectionResult = contourDetector.detect(bitmap)
            if (detectionResult != null && detectionResult.status == DocumentDetectionStatus.OK && detectionResult.polygonF.isNotEmpty()) {
                // apply the detected polygon to the new page
                page.apply(newPolygon = detectionResult.polygonF)
            }
            page
        }

        withContext(Dispatchers.Main) {
            progressBar.visibility = View.GONE
            val image = page.documentImage
            importResultImage.setImageBitmap(
                image
            )
            importResultImage.visibility = View.VISIBLE
            //show Page
        }
    }

    companion object {
        private const val PERMISSIONS_REQUEST_CAMERA = 314
    }
}
