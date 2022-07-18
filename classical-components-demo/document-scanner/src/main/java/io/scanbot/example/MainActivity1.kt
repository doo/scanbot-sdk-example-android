package io.scanbot.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.common.ImportImageContract
import io.scanbot.sdk.core.contourdetector.DetectionStatus
import io.scanbot.sdk.docprocessing.PageProcessor
import io.scanbot.sdk.persistence.Page
import io.scanbot.sdk.persistence.PageFileStorage
import io.scanbot.sdk.process.ImageFilterType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val scanbotSDK = ScanbotSDK(this)
        val pageFileStorage = scanbotSDK.createPageFileStorage()
        val pageProcessor = scanbotSDK.createPageProcessor()
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
                                pageFileStorage,
                                pageProcessor,
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
                openCamera()
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
                openCamera()
            }
        }
    }

    fun openCamera(){
        startActivity(Intent(this, DocumentCameraActivity::class.java))
    }
    /** Imports a selected image as original image and performs auto document detection on it. */
    suspend fun processImageForAutoDocumentDetection(
        pageFileStorage: PageFileStorage,
        pageProcessor: PageProcessor,
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
            // create a new Page object with given image as original image:
            val pageId = pageFileStorage.add(bitmap)
            var page = Page(pageId, emptyList(), DetectionStatus.OK, ImageFilterType.NONE)
            // run auto document detection on it:
            page = pageProcessor.detectDocument(page)
            page
        }

        withContext(Dispatchers.Main) {
            progressBar.visibility = View.GONE
            val image = pageFileStorage.getImage(
                page.pageId,
                PageFileStorage.PageFileType.DOCUMENT //cropped image
            )
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
