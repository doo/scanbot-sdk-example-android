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
import io.scanbot.example.R
import io.scanbot.example.model.BarcodeResultBundle
import io.scanbot.example.repository.BarcodeResultRepository
import io.scanbot.example.repository.BarcodeTypeRepository
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.SdkLicenseError
import io.scanbot.sdk.barcode.BarcodeAutoSnappingController
import io.scanbot.sdk.barcode.BarcodeDetectorFrameHandler
import io.scanbot.sdk.barcode.entity.BarcodeScanningResult
import io.scanbot.sdk.camera.CameraOpenCallback
import io.scanbot.sdk.camera.FrameHandlerResult
import io.scanbot.sdk.camera.PictureCallback
import io.scanbot.sdk.camera.ScanbotCameraView


class BarcodeScannerActivity : AppCompatActivity(), BarcodeDetectorFrameHandler.ResultHandler,
        PictureCallback {

    private var cameraView: ScanbotCameraView? = null
    private var resultView: ImageView? = null

    private var flashEnabled = false
    private var barcodeDetectorFrameHandler: BarcodeDetectorFrameHandler? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode_scanner)

        cameraView = findViewById(R.id.camera)
        resultView = findViewById(R.id.result)

        cameraView!!.setCameraOpenCallback(object : CameraOpenCallback {
            override fun onCameraOpened() {
                cameraView!!.postDelayed({
                    cameraView!!.useFlash(flashEnabled)
                    cameraView!!.continuousFocus()
                }, 300)
            }

        })

        barcodeDetectorFrameHandler = BarcodeDetectorFrameHandler.attach(
                cameraView!!,
                ScanbotSDK(this).barcodeDetector()
        )

        barcodeDetectorFrameHandler?.setDetectionInterval(1000)
        barcodeDetectorFrameHandler?.addResultHandler(this)
        barcodeDetectorFrameHandler?.saveCameraPreviewFrame(true)

        val barcodeAutoSnappingController =
                BarcodeAutoSnappingController.attach(cameraView!!, barcodeDetectorFrameHandler!!)
        barcodeAutoSnappingController.setSensitivity(1f)
        cameraView?.addPictureCallback(this)

        ScanbotSDK(this).barcodeDetector()
                .setBarcodeFormatsFilter(BarcodeTypeRepository.selectedTypes.toList())
    }

    override fun onResume() {
        super.onResume()
        cameraView?.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Use onActivityResult to handle permission rejection
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_PERMISSION_CODE)
        }
    }

    override fun onPause() {
        super.onPause()
        cameraView?.onPause()
    }

    private fun handleSuccess(result: FrameHandlerResult.Success<BarcodeScanningResult?>) {
        result.value?.let {
            BarcodeResultRepository.barcodeResultBundle = BarcodeResultBundle(it)
            val intent = Intent(this, BarcodeResultActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onPictureTaken(image: ByteArray, imageOrientation: Int) {
        val bitmap = BitmapFactory.decodeByteArray(image, 0, image.size)

        val matrix = Matrix()
        matrix.setRotate(imageOrientation.toFloat(), bitmap.width / 2f, bitmap.height / 2f)
        val resultBitmap =
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false)

        resultView?.post {
            resultView?.setImageBitmap(resultBitmap)
            cameraView?.continuousFocus()
            cameraView?.startPreview()
        }
    }

    override fun handle(result: FrameHandlerResult<BarcodeScanningResult?, SdkLicenseError>): Boolean {
        if (result is FrameHandlerResult.Success) {
            handleSuccess(result)
        } else {
            cameraView?.post {
                Toast.makeText(
                        this,
                        "License has expired!",
                        Toast.LENGTH_LONG
                ).show()
            }
        }
        return false
    }

    companion object {
        private const val REQUEST_PERMISSION_CODE = 200
    }
}
