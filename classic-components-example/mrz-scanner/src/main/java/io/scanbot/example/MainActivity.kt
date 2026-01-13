package io.scanbot.example

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.example.common.applyEdgeToEdge
import io.scanbot.example.databinding.ActivityMainBinding

/**
Ths example uses new sdk APIs presented in Scanbot SDK v.8.x.x
Please, check the official documentation for more details:
Result API https://docs.scanbot.io/android/document-scanner-sdk/detailed-setup-guide/result-api/
ImageRef API https://docs.scanbot.io/android/document-scanner-sdk/detailed-setup-guide/image-ref-api/
 */

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportActionBar?.hide()
        applyEdgeToEdge(findViewById(R.id.root_view))

        binding.liveScannerBtn.setOnClickListener { startActivity(MRZLiveScanningActivity.newIntent(this)) }
        val stillImageScannerBtn = findViewById<Button>(R.id.still_image_detection_btn)

        stillImageScannerBtn.setOnClickListener {
            val intent = Intent(applicationContext, MrzStillImageScanningActivity::class.java)
            startActivity(intent)
        }
    }
}
