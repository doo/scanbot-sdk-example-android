package io.scanbot.example

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.SdkLicenseError
import io.scanbot.sdk.camera.CameraOpenCallback
import io.scanbot.sdk.camera.CameraPreviewMode
import io.scanbot.sdk.camera.FrameHandlerResult
import io.scanbot.sdk.camera.PictureCallback
import io.scanbot.sdk.idcardscanner.IdCardScannerFrameHandler
import io.scanbot.sdk.idcardscanner.IdScanResult
import io.scanbot.sdk.process.RotateOperation
import io.scanbot.sdk.ui.camera.*

class ScannerActivity : AppCompatActivity() {

    private val scanbotSdk = ScanbotSDK(this)
    private val idCardScanner = scanbotSdk.idCardScanner()

    private lateinit var cameraView: IScanbotCameraView
    private lateinit var resultTextView: TextView

    private lateinit var idCardScannerFrameHandler: IdCardScannerFrameHandler

    private var useFlash = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        cameraView = findViewById<ScanbotCameraXView>(R.id.cameraView)
        resultTextView = findViewById(R.id.resultTextView)
        findViewById<FinderOverlayView>(R.id.finder_overlay).setRequiredAspectRatios(listOf(FinderAspectRatio(4.0, 3.0)))

        cameraView.setPreviewMode(CameraPreviewMode.FIT_IN)

        idCardScannerFrameHandler = IdCardScannerFrameHandler.attach(cameraView, idCardScanner)
        idCardScannerFrameHandler.addResultHandler(object : IdCardScannerFrameHandler.ResultHandler {
            override fun handle(result: FrameHandlerResult<IdScanResult, SdkLicenseError>): Boolean {
                val resultText: String = when (result) {
                    is FrameHandlerResult.Success -> {
                        if (result.value.status == IdScanResult.RecognitionStatus.Success) {
                            // TODO: your code here
                        }
                        result.value.status.toString()
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
        cameraView.addPictureCallback(object : PictureCallback {
            override fun onPictureTaken(image: ByteArray, imageOrientation: Int) {
                processPictureTaken(image, imageOrientation)
            }
        })

        findViewById<Button>(R.id.flashButton).setOnClickListener { toggleFlash() }
        findViewById<ShutterButton>(R.id.shutterButton).setOnClickListener { cameraView.takePicture(false) }
    }

    private fun processPictureTaken(image: ByteArray, imageOrientation: Int) {
        val bitmap = BitmapFactory.decodeByteArray(image, 0, image.size)
        val rotatedBitmap = scanbotSdk.imageProcessor().processBitmap(bitmap, RotateOperation(imageOrientation))!!
        val recognitionResult = idCardScanner.scanBitmap(rotatedBitmap)

        val isSuccess = recognitionResult != null &&
                (recognitionResult.status == IdScanResult.RecognitionStatus.Success)

        if (isSuccess) {
            proceedToResult(recognitionResult!!)
        } else {
            runOnUiThread {
                Toast.makeText(this@ScannerActivity,
                        "Error scanning: ${recognitionResult?.status}",
                        Toast.LENGTH_SHORT)
                        .show()
            }
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
