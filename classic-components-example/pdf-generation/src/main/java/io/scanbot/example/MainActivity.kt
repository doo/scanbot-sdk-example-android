package io.scanbot.example

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.example.common.applyEdgeToEdge
import io.scanbot.example.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.hide()
        applyEdgeToEdge(binding.root)

        binding.pdfButton.setOnClickListener {
            val intent = Intent(this, PdfActivity::class.java)
            startActivity(intent)
        }

        binding.pdfWithOcrButton.setOnClickListener {
            val intent = Intent(this, PdfWithOcrActivity::class.java)
            startActivity(intent)
        }
    }
}
