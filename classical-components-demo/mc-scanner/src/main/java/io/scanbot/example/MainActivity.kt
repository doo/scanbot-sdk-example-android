package io.scanbot.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import io.scanbot.example.MedicalCertificateRecognizerActivity.Companion.newIntent
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.common.ImportImageContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        askPermission()
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
                            val medicalCertificateRecognizer =
                                sdk.createMedicalCertificateRecognizer()

                            val result =
                                medicalCertificateRecognizer.recognizeMcBitmap(
                                    bitmap,
                                    0,
                                    true,
                                    true,
                                    true
                                )

                            withContext(Dispatchers.Main) {
                                result?.let {
                                    startActivity(
                                        MedicalCertificateResultActivity.newIntent(
                                            activity,
                                            it
                                        )
                                    )
                                } ?: Toast.makeText(
                                    activity,
                                    "Nothing detected on image",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                        withContext(Dispatchers.Main) { progressBar.isVisible = false }
                    }
                }
            }
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
