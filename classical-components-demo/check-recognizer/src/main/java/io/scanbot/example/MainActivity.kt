package io.scanbot.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        askPermission()
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.check_recognizer).setOnClickListener {
            startActivity(CheckRecognizerActivity.newIntent(this))
        }
        findViewById<View>(R.id.check_recognizer_auto_snapping).setOnClickListener {
            startActivity(AutoSnappingCheckRecognizerActivity.newIntent(this))
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
