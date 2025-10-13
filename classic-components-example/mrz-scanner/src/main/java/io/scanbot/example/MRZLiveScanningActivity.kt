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
import io.scanbot.common.getOrThrow
import io.scanbot.example.common.applyEdgeToEdge
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.camera.FrameHandlerResult
import io.scanbot.sdk.documentdata.entity.MRZ
import io.scanbot.sdk.geometry.AspectRatio
import io.scanbot.sdk.mrz.MrzScannerFrameHandler
import io.scanbot.sdk.ui.camera.FinderOverlayView
import io.scanbot.sdk.ui.camera.ScanbotCameraXView
import io.scanbot.sdk.util.log.LoggerProvider

class MRZLiveScanningActivity : AppCompatActivity() {
    private val logger = LoggerProvider.logger
    // @Tag("Mrz Classic Camera")
    private lateinit var cameraView: ScanbotCameraXView
    private lateinit var finderOverlay: FinderOverlayView
    private lateinit var mrzScannerFrameHandler : MrzScannerFrameHandler
    private var flashEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mrz_live_scanner)
        askPermission()
        supportActionBar!!.hide()
        applyEdgeToEdge(findViewById(R.id.root_view))

        // Configure Initial camera state
        cameraView = findViewById(R.id.camera)
        cameraView.setCameraOpenCallback {
            cameraView.postDelayed({
                cameraView.useFlash(flashEnabled)
                cameraView.continuousFocus()
            }, 700)
        }

        // Configure finder overlay with required aspect ratios
        finderOverlay = findViewById(R.id.finder_overlay)
        finderOverlay.setRequiredAspectRatios(listOf(AspectRatio(5.0, 1.0)))
        // Get the scanbot sdk instance
        val scanbotSDK = ScanbotSDK(this)
        // Configure mrz scanner
        val mrzScanner = scanbotSDK.createMrzScanner().getOrThrow()
        // Attach mrz scanner to the camera
        mrzScannerFrameHandler = MrzScannerFrameHandler.attach(cameraView, mrzScanner)
        // Handle live mrz scanning results
        mrzScannerFrameHandler.addResultHandler { result ->
            if (result is FrameHandlerResult.Success) {
                val scannerResult = result.value

                // It is recommended to use a frame accumulation as well and expect at least 2 of 4 frames to be equal

                val mrzDocument = scannerResult.document?.let { MRZ(it) }
                if (scannerResult.success
                    && mrzDocument?.checkDigitGeneral?.isValid == true
                ) {
                    mrzScannerFrameHandler.isEnabled = false
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

    override fun onResume() {
        super.onResume()
        mrzScannerFrameHandler.isEnabled = true
    }

    // @EndTag("Mrz Classic Camera")
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
