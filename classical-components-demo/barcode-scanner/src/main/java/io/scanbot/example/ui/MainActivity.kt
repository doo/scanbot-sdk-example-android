package io.scanbot.example.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import io.scanbot.example.R
import io.scanbot.example.model.BarcodeResultBundle
import io.scanbot.example.repository.BarcodeResultRepository
import io.scanbot.example.repository.BarcodeTypeRepository
import io.scanbot.sap.Status
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.example.common.ImportImageContract
import io.scanbot.example.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val galleryImageLauncher =
            registerForActivityResult(ImportImageContract(this)) { resultEntity ->
                lifecycleScope.launch(Dispatchers.Default) {
                    val activity = this@MainActivity
                    val sdk = ScanbotSDK(activity)
                    if (!sdk.licenseInfo.isValid) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                activity,
                                "License has expired!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        val progressBar = findViewById<View>(R.id.progress_bar)
                        withContext(Dispatchers.Main) { progressBar.isVisible = true }
                        resultEntity?.let { bitmap ->
                            val barcodeDetector = sdk.createBarcodeDetector()
                            barcodeDetector.modifyConfig { setBarcodeFormats(BarcodeTypeRepository.selectedTypes.toList()) }
                            val result = barcodeDetector.detectFromBitmap(bitmap, 0)

                            BarcodeResultRepository.barcodeResultBundle =
                                result?.let { BarcodeResultBundle(it, null, null) }

                            withContext(Dispatchers.Main) {
                                startActivity(
                                    Intent(
                                        activity,
                                        BarcodeResultActivity::class.java
                                    )
                                )
                            }
                        }
                        withContext(Dispatchers.Main) { progressBar.isVisible = false }
                    }
                }
            }
        setContentView(binding.root)

        findViewById<View>(R.id.qr_demo).setOnClickListener {
            val intent = Intent(applicationContext, BarcodeScannerActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.barcode_scanner_view_demo).setOnClickListener {
            val intent = Intent(applicationContext, BarcodeScannerViewActivity::class.java)
            startActivity(intent)
        }
        findViewById<View>(R.id.barcode_counter_view_demo).setOnClickListener {
            val intent = Intent(applicationContext, BarcodeScanAndCountViewActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.settings).setOnClickListener {
            val intent = Intent(this@MainActivity, BarcodeTypesActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.import_image).setOnClickListener {
            // select an image from photo library and run document detection on it:
            galleryImageLauncher.launch(Unit)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.warningView.isVisible = ScanbotSDK(this).licenseInfo.status == Status.StatusTrial
    }
}
