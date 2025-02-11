package io.scanbot.example

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.camera.FrameHandlerResult
import io.scanbot.sdk.common.AspectRatio
import io.scanbot.sdk.documentdata.entity.MRZ
import io.scanbot.sdk.mrz.MrzScannerFrameHandler
import io.scanbot.sdk.ui.camera.FinderOverlayView
import io.scanbot.sdk.ui.camera.ScanbotCameraXView
import io.scanbot.sdk.util.log.LoggerProvider

class MRZLiveScanningActivity : AppCompatActivity() {
    private val logger = LoggerProvider.logger

    private lateinit var cameraView: ScanbotCameraXView
    private lateinit var finderOverlay: FinderOverlayView

    private var flashEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mrz_live_scanner)
        askPermission()
        supportActionBar!!.hide()
        cameraView = findViewById<View>(R.id.camera) as ScanbotCameraXView

        cameraView.setCameraOpenCallback {
            cameraView.postDelayed({
                cameraView.useFlash(flashEnabled)
                cameraView.continuousFocus()
            }, 700)
        }

        finderOverlay = findViewById(R.id.finder_overlay)
        finderOverlay.setRequiredAspectRatios(listOf(AspectRatio(5.0, 1.0)))

        val scanbotSDK = ScanbotSDK(this)

        val mrzScanner = scanbotSDK.createMrzScanner()
        val mrzScannerFrameHandler = MrzScannerFrameHandler.attach(cameraView, mrzScanner)

        mrzScannerFrameHandler.addResultHandler { result ->
            if (result is FrameHandlerResult.Success) {
                val scannerResult = result.value

                // It is recommended to use a frame accumulation as well and expect at least 2 of 4 frames to be equal

                val mrzDocument = scannerResult.document?.let { MRZ(it) }
                if (scannerResult.success
                    && mrzDocument?.checkDigitGeneral?.isValid == true
                ) {
                    startActivity(MRZResultActivity.newIntent(this, scannerResult))
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
            if (scanbotSDK.licenseInfo.isValid) "License is active" else "License is expired",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun askPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 999)
        }
    }

    companion object {
        fun newIntent(context: Context?): Intent {
            return Intent(context, MRZLiveScanningActivity::class.java)
        }
    }
}
