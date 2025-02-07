package io.scanbot.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.SdkLicenseError
import io.scanbot.sdk.camera.CameraPreviewMode
import io.scanbot.sdk.camera.FrameHandlerResult
import io.scanbot.sdk.check.CheckScannerFrameHandler
import io.scanbot.sdk.check.CheckScannerFrameHandler.Companion.attach
import io.scanbot.sdk.check.CheckScanningResult
import io.scanbot.sdk.check.CheckRecognitionStatus
import io.scanbot.sdk.ui.camera.ScanbotCameraXView

class CheckRecognizerActivity : AppCompatActivity() {
    private lateinit var cameraView: ScanbotCameraXView
    private lateinit var resultView: TextView
    private lateinit var frameHandler: CheckScannerFrameHandler
    var flashEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_recognizer)
        cameraView = findViewById<ScanbotCameraXView>(R.id.camera).also { cameraView ->
            cameraView.setPreviewMode(CameraPreviewMode.FIT_IN)
            cameraView.setCameraOpenCallback {
                cameraView.postDelayed({
                    cameraView.useFlash(flashEnabled)
                    cameraView.continuousFocus()
                }, 700)
            }
        }

        resultView = findViewById<View>(R.id.result) as TextView
        val scanbotSDK = ScanbotSDK(this)

        val checkScanner = scanbotSDK.createCheckScanner()
        frameHandler = attach(cameraView, checkScanner)
        frameHandler.addResultHandler { result: FrameHandlerResult<CheckScanningResult?, SdkLicenseError?>? ->
            if (result is FrameHandlerResult.Success<*>) {
                val recognitionResult = (result as FrameHandlerResult.Success<*>).value as CheckScanningResult?
                if (recognitionResult?.status == CheckRecognitionStatus.SUCCESS) {
                    frameHandler.isEnabled = false
                    startActivity(CheckScannerResultActivity.newIntent(this, recognitionResult))
                }
            } else if (!scanbotSDK.licenseInfo.isValid) {
                frameHandler.isEnabled = false
                runOnUiThread {
                    Toast.makeText(
                        this,
                        "License is expired",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
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
        frameHandler.isEnabled = true
    }

    companion object {
        @JvmStatic
        fun newIntent(context: Context?): Intent {
            return Intent(context, CheckRecognizerActivity::class.java)
        }
    }

}
