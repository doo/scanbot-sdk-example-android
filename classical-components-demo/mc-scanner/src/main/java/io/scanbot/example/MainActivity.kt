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
import androidx.lifecycle.lifecycleScope
import io.scanbot.example.MedicalCertificateRecognizerActivity.Companion.newIntent
import io.scanbot.sdk.ScanbotSDK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    companion object {
        private const val IMPORT_IMAGE_REQUEST_CODE = 911
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        askPermission()

        val scannerBtn = findViewById<View>(R.id.scanner_btn) as Button
        scannerBtn.setOnClickListener { startActivity(newIntent(this@MainActivity)) }

        val manualScannerBtn = findViewById<View>(R.id.manual_scanner_btn) as Button
        manualScannerBtn.setOnClickListener {
            startActivity(
                ManualMedicalCertificateScannerActivity.newIntent(
                    this@MainActivity
                )
            )
        }

        findViewById<Button>(R.id.pick_image_btn)?.run {
            setOnClickListener {
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
    }

    private fun askPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 999)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        lifecycleScope.launch(Dispatchers.Default) {
            if (requestCode == IMPORT_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
                val activity = this@MainActivity
                val sdk = ScanbotSDK(activity)
                if (!sdk.licenseInfo.isValid) {
                    Toast.makeText(
                        activity,
                        "License has expired!",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    processGalleryResult(data!!)?.let { bitmap ->
                        val medicalCertificateRecognizer = sdk.createMedicalCertificateRecognizer()

                        val result =
                            medicalCertificateRecognizer.recognizeMcBitmap(
                                bitmap,
                                0,
                                true,
                                true,
                                true
                            )

                        withContext(Dispatchers.Main) {
                            result?.let { MedicalCertificateResultActivity.newIntent(activity, it) }
                        }
                    }
                }
            }
        }
    }

    private fun processGalleryResult(data: Intent): Bitmap? {
        val imageUri = data.data
        return MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
    }
}
