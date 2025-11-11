package io.scanbot.example.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import io.scanbot.common.Result
import io.scanbot.common.onFailure
import io.scanbot.common.onSuccess

import io.scanbot.example.R
import io.scanbot.example.common.applyEdgeToEdge
import io.scanbot.example.model.BarcodeResultBundle
import io.scanbot.example.repository.BarcodeResultRepository
import io.scanbot.example.repository.BarcodeTypeRepository
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.barcode.BarcodeAutoSnappingController
import io.scanbot.sdk.barcode.BarcodeScannerFrameHandler
import io.scanbot.sdk.barcode.BarcodeScannerResult
import io.scanbot.sdk.barcode.setBarcodeFormats
import io.scanbot.sdk.camera.*
import io.scanbot.sdk.geometry.AspectRatio
import io.scanbot.sdk.image.ImageRef
import io.scanbot.sdk.ui.camera.FinderOverlayView
import io.scanbot.sdk.ui.camera.ScanbotCameraXView


class BarcodeScannerActivity : AppCompatActivity(), BarcodeScannerFrameHandler.ResultHandler {
    private lateinit var cameraView: ScanbotCameraXView
    private lateinit var resultView: ImageView
    private lateinit var finderOverlay: FinderOverlayView

    private var flashEnabled = false
    private var scannerFrameHandler: BarcodeScannerFrameHandler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode_scanner)
        applyEdgeToEdge(findViewById(R.id.root_view))

        cameraView = findViewById(R.id.camera)
        resultView = findViewById(R.id.result)
        finderOverlay = findViewById(R.id.finder_overlay)

        cameraView.setCameraOpenCallback {
            cameraView.postDelayed({
                cameraView.useFlash(flashEnabled)
                cameraView.continuousFocus()
            }, 300)
        }

        finderOverlay.setRequiredAspectRatios(listOf(AspectRatio(1.0, 1.0)))
        val scanner = ScanbotSDK(this).createBarcodeScanner().getOrThrow()
        scanner.setConfiguration(scanner.copyCurrentConfiguration().copy().apply {
            setBarcodeFormats(barcodeFormats = BarcodeTypeRepository.selectedTypes.toList())
        })
        scannerFrameHandler =
            BarcodeScannerFrameHandler.attach(cameraView, scanner)

        scannerFrameHandler?.let { frameHandler ->
            frameHandler.setScanningInterval(1000)
            frameHandler.addResultHandler(this)

            val barcodeAutoSnappingController =
                BarcodeAutoSnappingController.attach(cameraView, frameHandler)
            barcodeAutoSnappingController.setSensitivity(1f)

        }
        cameraView.addPictureCallback(object : PictureCallback() {

            override fun onPictureTaken(
                image: ImageRef,
                captureInfo: CaptureInfo
            ) {
                processPictureTaken(image, captureInfo.imageOrientation)
            }
        })
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

    private fun handleSuccess(result: BarcodeScannerResult) {
        BarcodeResultRepository.barcodeResultBundle = BarcodeResultBundle(result)
        val intent = Intent(this, BarcodeResultActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun processPictureTaken(image: ImageRef, imageOrientation: Int) {
        val bitmap = image.toBitmap().getOrThrow()

        val matrix = Matrix()
        matrix.setRotate(imageOrientation.toFloat(), bitmap.width / 2f, bitmap.height / 2f)
        val resultBitmap =
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false)

        resultView.post {
            resultView.setImageBitmap(resultBitmap)
            cameraView.continuousFocus()
            cameraView.startPreview()
        }
    }


    companion object {
        private const val REQUEST_PERMISSION_CODE = 200
    }

    override fun handle(result: Result<BarcodeScannerResult>, frame: FrameHandler.Frame): Boolean {
        result.onSuccess {
            handleSuccess(it)
        }.onFailure {
            cameraView.post {
                Toast.makeText(
                    this@BarcodeScannerActivity,
                    "1-minute trial license has expired!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        return false
    }
}
