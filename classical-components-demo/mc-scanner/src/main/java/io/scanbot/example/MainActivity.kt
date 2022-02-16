package io.scanbot.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.scanbot.example.MedicalCertificateScannerActivity.Companion.newIntent

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        askPermission()

        val scannerBtn = findViewById<View>(R.id.scanner_btn) as Button
        scannerBtn.setOnClickListener { startActivity(newIntent(this@MainActivity)) }

        val manualScannerBtn = findViewById<View>(R.id.manual_scanner_btn) as Button
        manualScannerBtn.setOnClickListener { startActivity(ManualMedicalCertificateScannerActivity.newIntent(this@MainActivity)) }
    }

    private fun askPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 999)
        }
    }
}