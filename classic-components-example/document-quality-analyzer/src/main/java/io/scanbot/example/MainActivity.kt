package io.scanbot.example

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.process.DocumentQualityAnalyzer
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope {

    private lateinit var stillImageImageView: ImageView
    private lateinit var stillImageQualityCaption: TextView

    private lateinit var documentQualityAnalyzer: DocumentQualityAnalyzer

    private val parentJob = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + parentJob

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY)
        super.onCreate(savedInstanceState)

        val scanbotSDK = ScanbotSDK(this)
        documentQualityAnalyzer = scanbotSDK.createDocumentQualityAnalyzer()

        supportActionBar!!.hide()
        setContentView(R.layout.activity_main)
        askPermission()


        stillImageImageView = findViewById(R.id.still_image_image_view)
        stillImageQualityCaption = findViewById(R.id.still_image_quality_caption)

        findViewById<Button>(R.id.gallery_button).setOnClickListener { openGallery() }
        findViewById<Button>(R.id.still_image_close).setOnClickListener { close() }

        Toast.makeText(
                this,
                if (scanbotSDK.licenseInfo.isValid) "License is active" else "License is expired",
                Toast.LENGTH_LONG
        ).show()
    }

    private fun estimateOnStillImage(imageUri: Uri) {
        calculateForImage(imageUri)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PHOTOLIB_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let {
                estimateOnStillImage(it)
            }
        }
    }

    private fun close() {
        finish()
    }

    private fun openGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        startActivityForResult(
                Intent.createChooser(intent, "Select picture"), PHOTOLIB_REQUEST_CODE
        )
    }

    private fun askPermission() {
        if (checkPermissionNotGranted(Manifest.permission.READ_EXTERNAL_STORAGE) ||
                checkPermissionNotGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                checkPermissionNotGranted(Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA), 999)
        }
    }

    private fun calculateForImage(imageUri: Uri) {
        launch {
            val bitmap: Bitmap =
                    MediaStore.Images.Media.getBitmap(contentResolver, imageUri)

            withContext(Dispatchers.Main) {
                stillImageImageView.setImageBitmap(bitmap)
            }
            val result = documentQualityAnalyzer.analyzeInBitmap(bitmap, 0)
            withContext(Dispatchers.Main) {
                stillImageQualityCaption.text =
                    "Image quality: ${result?.name ?: "UNKNOWN"}"
            }
        }
    }

    override fun onDestroy() {
        parentJob.cancel()
        super.onDestroy()
    }

    private fun checkPermissionNotGranted(permission: String) =
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED

    private companion object {
        private const val PHOTOLIB_REQUEST_CODE = 5712
    }
}
