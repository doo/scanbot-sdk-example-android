package io.scanbot.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.scanbot.example.EhicLiveDetectionActivity.Companion.newIntent

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        askPermission()

        val liveScannerBtn = findViewById<Button>(R.id.live_scanner_btn)
        liveScannerBtn.setOnClickListener { startActivity(newIntent(this@MainActivity)) }

        val stillImageScannerBtn = findViewById<Button>(R.id.still_image_detection_btn)
        stillImageScannerBtn.setOnClickListener {
            val intent = Intent(applicationContext, EhicStillImageDetectionActivity::class.java)
            startActivity(intent)
        }
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

    private fun checkPermissionNotGranted(permission: String) =
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
}