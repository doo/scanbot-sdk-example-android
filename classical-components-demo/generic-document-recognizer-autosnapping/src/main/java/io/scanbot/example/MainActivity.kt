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

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (askPermission()) {
            startScannerActivity()
        } else {
            findViewById<Button>(R.id.startScannerButton).run {
                setOnClickListener { startScannerActivity() }
                visibility = View.VISIBLE

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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 999)
            return false
        }

        return true
    }
}
