package io.scanbot.example

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import io.scanbot.example.MedicalCertificateResultActivity.Companion.newIntent
import io.scanbot.example.common.applyEdgeToEdge
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.camera.CameraPreviewMode
import io.scanbot.sdk.camera.CaptureInfo
import io.scanbot.sdk.camera.PictureCallback
import io.scanbot.sdk.mc.MedicalCertificateAutoSnappingController
import io.scanbot.sdk.mc.MedicalCertificateFrameHandler
import io.scanbot.sdk.mc.MedicalCertificateScanner
import io.scanbot.sdk.mc.MedicalCertificateScanningParameters
import io.scanbot.sdk.ui.camera.ScanbotCameraXView
import kotlin.math.roundToInt

class MedicalCertificateScannerActivity : AppCompatActivity() {

    private lateinit var cameraView: ScanbotCameraXView
    private lateinit var resultImageView: ImageView

    private var flashEnabled = false

    private lateinit var scanner: MedicalCertificateScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mc_scanner)
        supportActionBar!!.hide()
        applyEdgeToEdge(findViewById(R.id.root_view))
        askPermission()
        resultImageView = findViewById(R.id.resultImageView)

        cameraView = findViewById<View>(R.id.camera) as ScanbotCameraXView
        cameraView.setCameraOpenCallback {
            cameraView.postDelayed({
                cameraView.useFlash(flashEnabled)
                cameraView.continuousFocus()
            }, 700)
        }
        cameraView.addPictureCallback(object : PictureCallback() {
            override fun onPictureTaken(image: ByteArray, captureInfo: CaptureInfo) {
                processPictureTaken(image)
            }
        })
        cameraView.setPreviewMode(CameraPreviewMode.FIT_IN)

        val scanbotSDK = ScanbotSDK(this)
        scanner = scanbotSDK.createMedicalCertificateScanner()

        // Attach `FrameHandler`, that will be scanning for Medical Certificate document on the camera frames
        val frameHandler =
            MedicalCertificateFrameHandler.attach(cameraView, scanner)
        // Attach `AutoSnappingController`, that will trigger the snap as soon as `FrameHandler` will scanning Medical Certificate document on the frame successfully
        MedicalCertificateAutoSnappingController.attach(cameraView, frameHandler).apply {
            // possibly adjust auto-snapping parameters here
//            setSensitivity(0.5f)
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

        Toast.makeText(this, "Scanning Medical Certificate...", Toast.LENGTH_LONG)
    }

    private fun processPictureTaken(image: ByteArray) {
        // Here we get the full image from the camera.
        // Implement a suitable async(!) scanning and image handling here.

        // Decode Bitmap from bytes of original image:
        val options = BitmapFactory.Options()
        options.inSampleSize = 2 // use 1 for full, no downscaled image.
        var originalBitmap = BitmapFactory.decodeByteArray(image, 0, image.size, options)

        // And finally run Medical Certificate scanning on prepared document image:
        val resultInfo = scanner.scanFromBitmap(
            originalBitmap,
            0,
            MedicalCertificateScanningParameters(
                shouldCropDocument = true,
                extractCroppedImage = true,
                recognizePatientInfoBox = true,
                recognizeBarcode = true
            )
        )

        if (resultInfo != null && resultInfo.scanningSuccessful) {
            // Show the cropped image as thumbnail preview
            resultInfo.croppedImage?.toBitmap()?.let { image ->
                val thumbnailImage = resizeImage(image, 600f, 600f)
                runOnUiThread {
                    resultImageView.setImageBitmap(thumbnailImage)
                }
            }

            resultImageView.postDelayed({ startActivity(newIntent(this, resultInfo)) }, 2000)
        } else {
            runOnUiThread {
                val toast = Toast.makeText(
                    this,
                    "No Medical Certificate content was found!",
                    Toast.LENGTH_LONG
                )
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            }
        }

        // reset preview image
        resultImageView.postDelayed({ resultImageView.setImageBitmap(null) }, 2000)
        // and continue with camera preview
        runOnUiThread {
            cameraView.continuousFocus()
            cameraView.startPreview()
        }
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

    private fun resizeImage(bitmap: Bitmap, width: Float, height: Float): Bitmap {
        val oldWidth = bitmap.width.toFloat()
        val oldHeight = bitmap.height.toFloat()
        val scaleFactor = if (oldWidth > oldHeight) width / oldWidth else height / oldHeight
        val scaledWidth = (oldWidth * scaleFactor).roundToInt()
        val scaledHeight = (oldHeight * scaleFactor).roundToInt()
        return Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, false)
    }

    companion object {
        @JvmStatic
        fun newIntent(context: Context?): Intent {
            return Intent(context, MedicalCertificateScannerActivity::class.java)
        }
    }
}
