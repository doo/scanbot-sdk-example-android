package io.scanbot.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import io.scanbot.example.DCResultActivity.Companion.newIntent
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.camera.FrameHandlerResult
import io.scanbot.sdk.camera.ScanbotCameraView
import io.scanbot.sdk.dcscanner.DCScannerFrameHandler

class DCScannerActivity : AppCompatActivity() {
    private lateinit var cameraView: ScanbotCameraView

    private var flashEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dc_scanner)
        supportActionBar!!.hide()

        cameraView = findViewById<View>(R.id.camera) as ScanbotCameraView
        cameraView.setCameraOpenCallback {
            cameraView.postDelayed({
                cameraView.useFlash(flashEnabled)
                cameraView.continuousFocus()
            }, 700)
        }

        val scanbotSDK = ScanbotSDK(this)
        val dcScanner = scanbotSDK.createDcScanner()
        val dcScannerFrameHandler = DCScannerFrameHandler.attach(cameraView, dcScanner)

        dcScannerFrameHandler.addResultHandler { result ->
            if (result is FrameHandlerResult.Success) {
                val resultInfo = result.value
                if (resultInfo.recognitionSuccessful) {
                    startActivity(newIntent(this, resultInfo))
                }
            }
            false
        }
        findViewById<View>(R.id.flash).setOnClickListener { v: View? ->
            flashEnabled = !flashEnabled
            cameraView.useFlash(flashEnabled)
        }
        Toast.makeText(
                this,
                if (scanbotSDK.licenseInfo.isValid) "License is active" else "License is expired",
                Toast.LENGTH_LONG
        ).show()
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
            return Intent(context, DCScannerActivity::class.java)
        }
    }
}