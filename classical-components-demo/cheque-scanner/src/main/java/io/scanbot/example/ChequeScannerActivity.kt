package io.scanbot.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import io.scanbot.chequescanner.model.Result
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.camera.FrameHandlerResult
import io.scanbot.sdk.camera.ScanbotCameraView
import io.scanbot.sdk.chequescanner.ChequeScannerFrameHandler

class ChequeScannerActivity : AppCompatActivity() {
    private lateinit var cameraView: ScanbotCameraView
    private var flashEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cheque_scanner)
        supportActionBar!!.hide()
        cameraView = findViewById<View>(R.id.camera) as ScanbotCameraView
        cameraView.setCameraOpenCallback {
            cameraView.postDelayed({
                cameraView.useFlash(flashEnabled)
                cameraView.continuousFocus()
            }, 700)
        }
        val scanbotSDK = ScanbotSDK(this)
        val chequeScanner = scanbotSDK.createChequeScanner()
        val chequeScannerFrameHandler = ChequeScannerFrameHandler.attach(cameraView, chequeScanner)
        chequeScannerFrameHandler.addResultHandler { result ->
            if (result is FrameHandlerResult.Success) {
                val value = result.value
                if (value.accountNumber.value.isNotEmpty() || value.routingNumber.value.isNotEmpty()) {
                    runOnUiThread {
                        Toast.makeText(
                            this@ChequeScannerActivity,
                            extractData(value), Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            false
        }
        findViewById<View>(R.id.flash).setOnClickListener {
            flashEnabled = !flashEnabled
            cameraView.useFlash(flashEnabled)
        }
        Toast.makeText(
                this,
                if (scanbotSDK.isLicenseActive) "License is active" else "License is expired",
                Toast.LENGTH_LONG
        ).show()
    }

    private fun extractData(result: Result): String {
        return StringBuilder()
                .append("accountNumber: ").append(result.accountNumber.value).append("\n")
                .append("routingNumber: ").append(result.routingNumber.value).append("\n")
                .append("chequeNumber: ").append(result.chequeNumber.value).append("\n")
                .append("Polygon detection result: ").append(result.polygon.detectionResult.toString()).append("\n")
                .append("Polygon : ").append(result.polygon.points.toString()).append("\n")
                .toString()
    }

    override fun onResume() {
        super.onResume()
        cameraView.onResume()
    }

    override fun onPause() {
        super.onPause()
        cameraView.onPause()
    }

    companion object {
        @JvmStatic
        fun newIntent(context: Context?): Intent {
            return Intent(context, ChequeScannerActivity::class.java)
        }
    }
}