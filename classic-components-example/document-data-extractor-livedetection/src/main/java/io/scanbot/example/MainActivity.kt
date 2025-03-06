package io.scanbot.example

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.example.common.applyEdgeToEdge
import io.scanbot.example.common.showToast
import io.scanbot.example.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val requestCameraLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            startScannerActivity()
        } else {
            this@MainActivity.showToast("Camera permission is required to run this example!")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportActionBar?.hide()
        applyEdgeToEdge(this.findViewById(R.id.root_view))

        requestCameraLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun startScannerActivity() {
        val intent = Intent(this, ScannerActivity::class.java)
        startActivity(intent)
        finish()
    }
}
