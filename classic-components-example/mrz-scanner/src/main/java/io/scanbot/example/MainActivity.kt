package io.scanbot.example

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.example.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.liveScannerBtn.setOnClickListener { startActivity(MRZLiveDetectionActivity.newIntent(this)) }
        val stillImageScannerBtn = findViewById<Button>(R.id.still_image_detection_btn)

        stillImageScannerBtn.setOnClickListener {
            val intent = Intent(applicationContext, MrzStillImageDetectionActivity::class.java)
            startActivity(intent)
        }
    }
}
