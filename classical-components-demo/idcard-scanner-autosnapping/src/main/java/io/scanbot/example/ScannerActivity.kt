package io.scanbot.example

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.sap.SapManager
import io.scanbot.sap.Status
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.ScanbotSDKInitializer
import io.scanbot.sdk.SdkLicenseError
import io.scanbot.sdk.camera.CameraOpenCallback
import io.scanbot.sdk.camera.CameraPreviewMode
import io.scanbot.sdk.camera.FrameHandlerResult
import io.scanbot.sdk.camera.PictureCallback
import io.scanbot.sdk.idcardscanner.IdCardAutoSnappingController
import io.scanbot.sdk.idcardscanner.IdCardScannerFrameHandler
import io.scanbot.sdk.idcardscanner.IdScanResult
import io.scanbot.sdk.ui.camera.*

class ScannerActivity : AppCompatActivity() {

    private val scanbotSdk = ScanbotSDK(this)
    private val idCardScanner = scanbotSdk.idCardScanner()

    private lateinit var cameraView: IScanbotCameraView
    private lateinit var shutterButton: ShutterButton

    private lateinit var autoSnappingController: IdCardAutoSnappingController

    private var useFlash = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        cameraView = findViewById<ScanbotCameraXView>(R.id.cameraView)
        findViewById<FinderOverlayView>(R.id.finder_overlay).setRequiredAspectRatios(listOf(FinderAspectRatio(4.0, 3.0)))

        cameraView.setPreviewMode(CameraPreviewMode.FIT_IN)

        // TODO: adjust accepted sharpness score to control the blurriness of the result image
        idCardScanner.acceptedSharpnessScore = 80f
        autoSnappingController = IdCardAutoSnappingController.attach(cameraView, idCardScanner)

        cameraView.setCameraOpenCallback(object : CameraOpenCallback {
            override fun onCameraOpened() {
                cameraView.useFlash(useFlash)
                cameraView.continuousFocus()
            }
        })
        cameraView.addPictureCallback(object : PictureCallback() {
            override fun onPictureTaken(image: ByteArray, imageOrientation: Int) {
                processPictureTaken(image, imageOrientation)
            }
        })

        findViewById<Button>(R.id.flashButton).setOnClickListener { toggleFlash() }

        shutterButton = findViewById(R.id.shutterButton)
        shutterButton.setOnClickListener {
            cameraView.takePicture(false)
            shutterButton.isEnabled = false
        }
    }

    private fun processPictureTaken(image: ByteArray, imageOrientation: Int) {
        // pause autoSnappingController to stop detecting results on a preview during the recognition on the full-size picture
        autoSnappingController.isEnabled = false

        runOnUiThread {
            shutterButton.isEnabled = false
        }

        val bitmap = BitmapFactory.decodeByteArray(image, 0, image.size)
        val recognitionResult = idCardScanner.scanBitmap(bitmap, orientation = imageOrientation)

        val isSuccess = recognitionResult != null && recognitionResult.status == IdScanResult.RecognitionStatus.Success

        if (isSuccess) {
            proceedToResult(recognitionResult!!)
        } else {
            runOnUiThread {
                Toast.makeText(this@ScannerActivity, "Error scanning: ${recognitionResult?.status}", Toast.LENGTH_SHORT).show()
                shutterButton.isEnabled = true
            }
            autoSnappingController.isEnabled = true
        }
    }

    private fun proceedToResult(idScanResult: IdScanResult) {
        IdCardScannerResultsStorage.results = idScanResult
        startActivity(Intent(this, ResultActivity::class.java))
        finish()
    }

    private fun toggleFlash() {
        useFlash = !useFlash
        cameraView.useFlash(useFlash)
    }
}
