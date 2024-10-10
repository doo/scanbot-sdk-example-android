package io.scanbot.example

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.example.EhicLiveDetectionActivity.Companion.newIntent
import io.scanbot.example.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.liveScannerBtn.setOnClickListener { startActivity(newIntent(this@MainActivity)) }

        binding.stillImageDetectionBtn.setOnClickListener {
            val intent = Intent(applicationContext, EhicStillImageDetectionActivity::class.java)
            startActivity(intent)
        }
    }
}
