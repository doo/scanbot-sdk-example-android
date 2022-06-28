package io.scanbot.example

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
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
                        val progressBar = findViewById<View>(R.id.progress_bar)
                        withContext(Dispatchers.Main) { progressBar.isVisible = true }
                        resultEntity?.let { bitmap ->
                            val checkRecognizer = sdk.createCheckRecognizer()
                            val stream = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                            val result =
                                checkRecognizer.recognizeBitmap(
                                    bitmap,
                                    0
                                )

                            withContext(Dispatchers.Main) {
                                result?.let {
                                    CheckRecognizerResultActivity.newIntent(
                                        activity,
                                        it
                                    )
                                }
                            }
                        }
                        withContext(Dispatchers.Main) { progressBar.isVisible = false }
                    }
                }
            }
        askPermission()
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.check_recognizer).setOnClickListener {
            startActivity(CheckRecognizerActivity.newIntent(this))
        }
        findViewById<View>(R.id.check_recognizer_auto_snapping).setOnClickListener {
            startActivity(AutoSnappingCheckRecognizerActivity.newIntent(this))
        }

        findViewById<View>(R.id.check_recognizer_pick_image).setOnClickListener {
            galleryImageLauncher.launch(Unit)
        }
    }

    private fun askPermission() {
        if (checkPermissionNotGranted(Manifest.permission.READ_EXTERNAL_STORAGE) ||
            checkPermissionNotGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
            checkPermissionNotGranted(Manifest.permission.CAMERA)
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                ), 999
            )
        }
    }

    private fun checkPermissionNotGranted(permission: String) =
        ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
}
