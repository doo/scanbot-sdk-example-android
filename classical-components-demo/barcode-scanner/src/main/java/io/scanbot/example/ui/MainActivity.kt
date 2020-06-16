package io.scanbot.example.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import io.scanbot.example.R
import io.scanbot.example.model.BarcodeResultBundle
import io.scanbot.example.repository.BarcodeResultRepository
import io.scanbot.example.repository.BarcodeTypeRepository
import io.scanbot.sap.Status
import io.scanbot.sdk.ScanbotSDK
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException

class MainActivity : AppCompatActivity() {
    companion object {
        private const val IMPORT_IMAGE_REQUEST_CODE = 911
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.qr_demo).setOnClickListener {
            val intent = Intent(applicationContext, BarcodeScannerActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.settings).setOnClickListener {
            val intent = Intent(this@MainActivity, BarcodeTypesActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.import_image).setOnClickListener {
            // select an image from photo library and run document detection on it:
            val imageIntent = Intent()
            imageIntent.type = "image/*"
            imageIntent.action = Intent.ACTION_GET_CONTENT
            imageIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, false)
            imageIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
            startActivityForResult(
                    Intent.createChooser(
                            imageIntent,
                            getString(R.string.share_title)
                    ), IMPORT_IMAGE_REQUEST_CODE
            )
        }
    }

    override fun onResume() {
        super.onResume()
        warning_view.isVisible = ScanbotSDK(this).licenseInfo.status == Status.StatusTrial
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
                    val barcodeDetector = sdk.barcodeDetector()
                    barcodeDetector.setBarcodeFormatsFilter(BarcodeTypeRepository.selectedTypes.toList())
                    val result = barcodeDetector.detectFromBitmap(bitmap, 0)

                    BarcodeResultRepository.barcodeResultBundle =
                            result?.let { BarcodeResultBundle(it, null, null) }

                    startActivity(Intent(this, BarcodeResultActivity::class.java))
                }
            }
        }
    }

    private fun processGalleryResult(data: Intent): Bitmap? {
        val imageUri = data.data
        var bitmap: Bitmap? = null
        if (imageUri != null) {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            } catch (e: IOException) {
            }
        }
        return bitmap
    }
}
