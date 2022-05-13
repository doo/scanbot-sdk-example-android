package io.scanbot.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
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
                            val documentRecognizer = sdk.createGenericDocumentRecognizer()

                            val result = documentRecognizer.scanBitmap(bitmap, true, 0)

                            withContext(Dispatchers.Main) {
                                DocumentsResultsStorage.result = result
                                showResult()
                            }
                        }
                    }
                }
            }
        askPermission()
        findViewById<Button>(R.id.startScannerButton)?.run {
            setOnClickListener { startScannerActivity() }
            visibility = View.VISIBLE
        }
        findViewById<Button>(R.id.pick_image_btn)?.run {
            setOnClickListener {
                galleryImageLauncher.launch(Unit)
            }
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
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 999)
            return false
        }

        return true
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
