package io.scanbot.example

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.scanbot.sdk.ScanbotSDK

class MainActivity : AppCompatActivity() {
    companion object {
        private const val IMPORT_IMAGE_REQUEST_CODE = 911
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        askPermission()
        findViewById<Button>(R.id.startScannerButton)?.run {
            setOnClickListener { startScannerActivity() }
            visibility = View.VISIBLE
        }
        findViewById<Button>(R.id.pick_image_btn)?.run {
            val imageIntent = Intent()
            imageIntent.type = "image/*"
            imageIntent.action = Intent.ACTION_GET_CONTENT
            imageIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, false)
            imageIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
            startActivityForResult(
                Intent.createChooser(
                    imageIntent,
                    "import image for detect"
                ), IMPORT_IMAGE_REQUEST_CODE
            )
        }

    }

    private fun startScannerActivity() {
        if (askPermission().not()) {
            Toast.makeText(this, "Needs permission", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, ScannerActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun askPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 999)
            return false
        }

        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMPORT_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val sdk = ScanbotSDK(this)
            if (!sdk.licenseInfo.isValid) {
                Toast.makeText(
                    this,
                    "License has expired!",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                processGalleryResult(data!!)?.let { bitmap ->
                    val documentRecognizer = sdk.createGenericDocumentRecognizer()

                    val result = documentRecognizer.scanBitmap(bitmap, true,0)

                    runOnUiThread {
                        DocumentsResultsStorage.result = result
                        showResult()
                    }
                }
            }
        }
    }

    private fun processGalleryResult(data: Intent): Bitmap? {
        val imageUri = data.data
        return MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
    }

    private fun showResult() {
        if (askPermission().not()) {
            Toast.makeText(this, "Needs permission", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, ResultActivity::class.java)
        startActivity(intent)
        finish()
    }
}
