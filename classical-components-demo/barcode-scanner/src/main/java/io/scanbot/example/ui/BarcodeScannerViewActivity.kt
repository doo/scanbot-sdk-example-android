package io.scanbot.example.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import io.scanbot.example.R
import io.scanbot.example.model.BarcodeResultBundle
import io.scanbot.example.repository.BarcodeResultRepository
import io.scanbot.example.repository.BarcodeTypeRepository
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.barcode.entity.BarcodeScanningResult
import io.scanbot.sdk.barcode.ui.BarcodeScannerView
import io.scanbot.sdk.barcode.ui.IBarcodeScannerViewCallback
import io.scanbot.sdk.camera.CaptureInfo
import io.scanbot.sdk.camera.FrameHandlerResult
import io.scanbot.sdk.ui.camera.CameraUiSettings

class BarcodeScannerViewActivity : AppCompatActivity() {
    private lateinit var barcodeScannerView: BarcodeScannerView
    private lateinit var resultView: ImageView

    private var flashEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode_scanner_view)

        barcodeScannerView = findViewById(R.id.barcode_scanner_view)
        resultView = findViewById(R.id.result)

        val barcodeDetector = ScanbotSDK(this).createBarcodeDetector()
        barcodeDetector.modifyConfig {
            setBarcodeFormats(BarcodeTypeRepository.selectedTypes.toList())
            setSaveCameraPreviewFrame(false)
        }

        barcodeScannerView.initCamera(CameraUiSettings(false))
        barcodeScannerView.initDetectionBehavior(barcodeDetector,
            { result ->
                if (result is FrameHandlerResult.Success) {
                    handleSuccess(result)
                } else {
                    barcodeScannerView.post {
                        Toast.makeText(
                            this@BarcodeScannerViewActivity,
                            "License has expired!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                false
            },
            object : IBarcodeScannerViewCallback {
                override fun onCameraOpen() {
                    barcodeScannerView.viewController.useFlash(flashEnabled)
                }

                override fun onPictureTaken(image: ByteArray, captureInfo: CaptureInfo) {
                    // we don't need full size pictures in this example
                }
            }
        )

        barcodeScannerView.viewController.barcodeDetectionInterval = 1000
        barcodeScannerView.viewController.autoSnappingEnabled = false
    }

    override fun onResume() {
        super.onResume()
        barcodeScannerView.viewController.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Use onActivityResult to handle permission rejection
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_PERMISSION_CODE)
        }
    }

    override fun onPause() {
        super.onPause()
        barcodeScannerView.viewController.onPause()
    }

    private fun handleSuccess(result: FrameHandlerResult.Success<BarcodeScanningResult?>) {
        result.value?.let {
            BarcodeResultRepository.barcodeResultBundle = BarcodeResultBundle(it)
            val intent = Intent(this, BarcodeResultActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    companion object {
        private const val REQUEST_PERMISSION_CODE = 200
    }
}
