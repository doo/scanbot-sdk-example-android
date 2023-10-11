package io.scanbot.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.PointF
import android.graphics.RectF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import io.scanbot.example.R
import io.scanbot.example.repository.BarcodeTypeRepository
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.barcode.entity.BarcodeItem
import io.scanbot.sdk.barcode.ui.BarcodePolygonsStaticView
import io.scanbot.sdk.barcode.ui.BarcodeScanAndCountView
import io.scanbot.sdk.barcode.ui.IBarcodeScanCountViewCallback

class BarcodeScanAndCountViewActivity : AppCompatActivity() {
    private lateinit var scanCountView: BarcodeScanAndCountView
    private lateinit var scanButton: Button
    private lateinit var nextButton: Button
    private lateinit var snapResult: TextView

    private var flashEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode_count_view)

        scanCountView = findViewById(R.id.barcode_scanner_view)
        scanButton = findViewById(R.id.snapButton)
        nextButton = findViewById(R.id.nextButton)
        snapResult = findViewById(R.id.snapped_message)

        val barcodeDetector = ScanbotSDK(this).createBarcodeDetector()
        barcodeDetector.modifyConfig {
            setBarcodeFormats(BarcodeTypeRepository.selectedTypes.toList())
            setSaveCameraPreviewFrame(false)
        }
        scanButton.setOnClickListener {
            scanCountView.viewController.scanAndCount() // call this to run the scan and count
        }
        nextButton.setOnClickListener {
            scanCountView.viewController.continueScanning() // call this after the scan to reset the view state and continue scanning
            scanButton.isEnabled = true
            nextButton.isEnabled = false
        }

        scanButton.isEnabled = true
        nextButton.isEnabled = false

        scanCountView.apply {
            initCamera()
            initDetectionBehavior(
                barcodeDetector,
                callback = object : IBarcodeScanCountViewCallback {
                    override fun onCameraOpen() {
                        scanCountView.viewController.useFlash(flashEnabled)
                    }

                    override fun onLicenseError() {
                        scanCountView.post {
                            Toast.makeText(
                                this@BarcodeScanAndCountViewActivity,
                                "License has expired!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    override fun onScanAndCountStarted() {
                        scanButton.isEnabled = false
                    }

                    override fun onScanAndCountFinished(barcodes: List<BarcodeItem>) {
                        scanButton.isEnabled = false
                        nextButton.isEnabled = true
                        // barcodes is the result of the last scanning session, but to ge all counted barcodes, use the following code
                        handleSnap(scanCountView.countedBarcodes)
                    }
                }
            )
        }

        // Setting the Selection Overlay (AR)
        scanCountView.counterOverlayController.setBarcodeItemViewFactory(object :
            BarcodePolygonsStaticView.BarcodeItemViewFactory {
            override fun createView(): View {
                val inflater = LayoutInflater.from(this@BarcodeScanAndCountViewActivity)
                return inflater.inflate(R.layout.custom_view_for_counter, scanCountView, false)
            }
        })
        scanCountView.counterOverlayController.setBarcodeItemViewBinder(object :
            BarcodePolygonsStaticView.BarcodeItemViewBinder {
            override fun bindView(view: View, barcodeItem: BarcodeItem, shouldHighlight: Boolean) {
                val valueTextView = view.findViewById<TextView>(R.id.custom_ar_view_value)
                val imageView = view.findViewById<ImageView>(R.id.custom_ar_view)
                valueTextView.isVisible = false //uncomment to show barcode value
                valueTextView.text = barcodeItem.textWithExtension
                valueTextView.maxLines = 2
            }
        })

        scanCountView.counterOverlayController.setBarcodeItemViewPositionHandler(object :
            BarcodePolygonsStaticView.BarcodeItemViewPositionHandler {

            override fun adjustPosition(view: View, path: List<PointF>, bounds: RectF) {
                val valueTextView = view.findViewById<TextView>(R.id.custom_ar_view_value)
                valueTextView.translationY = bounds.height() / 2 + 20f
            }
        })

        // TODO to get the result of all scanned barcodes, use the following code
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Use onActivityResult to handle permission rejection
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_PERMISSION_CODE
            )
        }
    }

    private fun handleSnap(barcodes: Map<BarcodeItem, Int>) {
        barcodes.let {
            val sb = StringBuilder()
            for ((key, value) in it) {
                sb.append(key.textWithExtension).append(" - ").append(value).append("\n")
            }
            snapResult.text = sb.toString()
        }
    }

    companion object {
        private const val REQUEST_PERMISSION_CODE = 200
    }
}
