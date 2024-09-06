package io.scanbot.example

import android.Manifest
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.scanbot.example.common.Const
import io.scanbot.example.common.showToast
import io.scanbot.example.databinding.ActivityMainBinding
import io.scanbot.sdk.ScanbotSDK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val scanbotSdk: ScanbotSDK by lazy { ScanbotSDK(this) }

    private val selectGalleryImageResultLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (!scanbotSdk.licenseInfo.isValid) {
            this@MainActivity.showToast("1-minute trial license has expired!")
            Log.e(Const.LOG_TAG, "1-minute trial license has expired!")
            return@registerForActivityResult
        }

        if (uri == null) {
            showToast("Error obtaining selected image!")
            Log.e(Const.LOG_TAG, "Error obtaining selected image!")
            return@registerForActivityResult
        }

        lifecycleScope.launch {
            val documentRecognizer = scanbotSdk.createGenericDocumentRecognizer()

            val result = withContext(Dispatchers.Default) {
                val inputStream = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                documentRecognizer.scanBitmap(bitmap, true, 0)
            }

            withContext(Dispatchers.Main) {
                DocumentsResultsStorage.result = result
                showResult()
            }
        }
    }

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

        binding.startScannerButton.run {
            setOnClickListener { requestCameraLauncher.launch(Manifest.permission.CAMERA) }
            visibility = View.VISIBLE
        }
        binding.pickImageBtn.run {
            setOnClickListener {
                selectGalleryImageResultLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        }
    }

    private fun startScannerActivity() {
        val intent = Intent(this, ScannerActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showResult() {
        val intent = Intent(this, ResultActivity::class.java)
        startActivity(intent)
        finish()
    }
}
