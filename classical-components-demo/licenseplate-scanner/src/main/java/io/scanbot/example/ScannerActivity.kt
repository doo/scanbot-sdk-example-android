package io.scanbot.example

import android.os.Bundle
import android.util.TypedValue
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.SdkLicenseError
import io.scanbot.sdk.camera.CameraOpenCallback
import io.scanbot.sdk.camera.CameraPreviewMode
import io.scanbot.sdk.camera.FrameHandlerResult
import io.scanbot.sdk.licenseplate.LicensePlateScanResult
import io.scanbot.sdk.licenseplate.LicensePlateScanStrategy
import io.scanbot.sdk.licenseplate.LicensePlateScannerFrameHandler
import io.scanbot.sdk.ui.camera.*

class ScannerActivity : AppCompatActivity() {
    private val scanbotSdk = ScanbotSDK(this)
    private val licensePlateScanner = scanbotSdk.licensePlateScanner()

    private lateinit var cameraView: IScanbotCameraView
    private lateinit var resultTextView: TextView

    private var useFlash = false

    private lateinit var licensePlateScannerFrameHandler: LicensePlateScannerFrameHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)
        licensePlateScanner.scanStrategy = LicensePlateScanStrategy.LicensePlateML

        cameraView = findViewById<ScanbotCameraXView>(R.id.cameraView)
        resultTextView = findViewById(R.id.resultTextView)

        val zoomFinderOverlay = findViewById<ZoomFinderOverlayView>(R.id.finder_overlay)
        // The smaller finder view brings better performance and allows user to detect text more precise
        zoomFinderOverlay.setRequiredAspectRatios(listOf(FinderAspectRatio(4.0, 1.0)))
        zoomFinderOverlay.zoomLevel = 1.4f
        zoomFinderOverlay.setFixedFinderHeight(
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        70f, resources.displayMetrics).toInt()
        )

        cameraView.setPreviewMode(CameraPreviewMode.FIT_IN)

        licensePlateScannerFrameHandler = LicensePlateScannerFrameHandler.attach(cameraView, licensePlateScanner)
        licensePlateScannerFrameHandler.addResultHandler(object : LicensePlateScannerFrameHandler.ResultHandler {
            override fun handle(result: FrameHandlerResult<LicensePlateScanResult, SdkLicenseError>): Boolean {
                val resultText: String = when (result) {
                    is FrameHandlerResult.Success -> {
                        when {
                            result.value.validationSuccessful -> {
                                result.value.rawString
                                // TODO: you can open the screen with a result as soon as
                            }
                            else -> ""
                        }
                    }
                    is FrameHandlerResult.Failure -> "Check your setup or license"
                }

                runOnUiThread { resultTextView.text = resultText }

                return false
            }
        })

        cameraView.setCameraOpenCallback(object : CameraOpenCallback {
            override fun onCameraOpened() {
                cameraView.useFlash(useFlash)
                cameraView.continuousFocus()
            }
        })
        findViewById<Button>(R.id.flashButton).setOnClickListener { toggleFlash() }
    }

    private fun toggleFlash() {
        useFlash = !useFlash
        cameraView.useFlash(useFlash)
    }
}
