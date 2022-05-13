package io.scanbot.example

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.common.ImportImageContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val galleryImageLauncher =
            registerForActivityResult(ImportImageContract(this)) { resultEntity ->
                lifecycleScope.launch(Dispatchers.Default) {
                    val activity = this@MainActivity
                    val sdk = ScanbotSDK(activity)
                    if (!sdk.licenseInfo.isValid) {
                        Toast.makeText(
                            activity,
                            "License has expired!",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        resultEntity?.let { bitmap ->
                            val payformScanner = sdk.createPayFormScanner()
                            val stream = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                            val byteArray: ByteArray = stream.toByteArray()
                            bitmap.recycle()
                            val result =
                                payformScanner.recognizeFormJPEG(
                                    byteArray,
                                    bitmap.width,
                                    bitmap.height,
                                    0
                                )

                            withContext(Dispatchers.Main) {
                                result?.let {
                                    PayformResultActivity.newIntent(
                                        activity,
                                        it.payformFields
                                    )
                                }
                            }
                        }
                    }
                }
            }
        askPermission()

        val scannerBtn = findViewById<View>(R.id.scanner_btn) as Button
        scannerBtn.setOnClickListener { startActivity(PayformScannerActivity.newIntent(this@MainActivity)) }
        findViewById<Button>(R.id.pick_image_btn)?.run {
            setOnClickListener {
                galleryImageLauncher.launch(Unit)
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
}
