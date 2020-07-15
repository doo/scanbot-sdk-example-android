package io.scanbot.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import io.scanbot.dcscanner.model.DisabilityCertificateRecognizerResultInfo
import io.scanbot.example.DCResultActivity.Companion.newIntent
import io.scanbot.example.DCScannerActivity
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.SdkLicenseError
import io.scanbot.sdk.camera.CameraOpenCallback
import io.scanbot.sdk.camera.FrameHandlerResult
import io.scanbot.sdk.camera.ScanbotCameraView
import io.scanbot.sdk.dcscanner.DCScannerFrameHandler
import io.scanbot.sdk.util.log.LoggerProvider

class DCScannerActivity : AppCompatActivity() {
    private lateinit var cameraView: ScanbotCameraView

    private val logger = LoggerProvider.logger
    private var flashEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dc_scanner)
        supportActionBar!!.hide()

        cameraView = findViewById<View>(R.id.camera) as ScanbotCameraView
        cameraView.setCameraOpenCallback(object : CameraOpenCallback {
            override fun onCameraOpened() {
                cameraView.postDelayed({
                    cameraView.useFlash(flashEnabled)
                    cameraView.continuousFocus()
                }, 700)
            }
        })

        val scanbotSDK = ScanbotSDK(this)
        val dcScanner = scanbotSDK.dcScanner()
        val dcScannerFrameHandler = DCScannerFrameHandler.attach(cameraView, dcScanner)

        dcScannerFrameHandler.addResultHandler(object : DCScannerFrameHandler.ResultHandler {
            override fun handle(result: FrameHandlerResult<DisabilityCertificateRecognizerResultInfo, SdkLicenseError>): Boolean {
                if (result is FrameHandlerResult.Success<*>) {
                    val resultInfo = (result as FrameHandlerResult.Success<DisabilityCertificateRecognizerResultInfo?>).value
                    if (resultInfo != null && resultInfo.recognitionSuccessful) {
                        val a = System.currentTimeMillis()
                        try {
                            startActivity(newIntent(this@DCScannerActivity, resultInfo))
                        } finally {
                            val b = System.currentTimeMillis()
                            logger.d("DCScanner", "Total scanning (sec): " + (b - a) / 1000f)
                        }
                    }
                }
                return false
            }
        })
        findViewById<View>(R.id.flash).setOnClickListener { v: View? ->
            flashEnabled = !flashEnabled
            cameraView.useFlash(flashEnabled)
        }
        Toast.makeText(
                this,
                if (scanbotSDK.isLicenseActive) "License is active" else "License is expired",
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