package io.scanbot.example

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import io.scanbot.example.MedicalCertificateResultActivity.Companion.newIntent
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.camera.CameraPreviewMode
import io.scanbot.sdk.camera.CaptureInfo
import io.scanbot.sdk.camera.PictureCallback
import io.scanbot.sdk.camera.ScanbotCameraView
import io.scanbot.sdk.mcrecognizer.MedicalCertificateAutoSnappingController
import io.scanbot.sdk.mcrecognizer.MedicalCertificateFrameHandler
import io.scanbot.sdk.mcrecognizer.MedicalCertificateRecognizer
import kotlin.math.roundToInt

class MedicalCertificateRecognizerActivity : AppCompatActivity() {
    private lateinit var cameraView: ScanbotCameraView
    private lateinit var resultImageView: ImageView

    private var flashEnabled = false

    private lateinit var medicalCertificateRecognizer: MedicalCertificateRecognizer

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mc_recognizer)
        supportActionBar!!.hide()

        resultImageView = findViewById(R.id.resultImageView)

        cameraView = findViewById<View>(R.id.camera) as ScanbotCameraView
        cameraView.setCameraOpenCallback {
            cameraView.postDelayed({
                cameraView.useFlash(flashEnabled)
                cameraView.continuousFocus()
            }, 700)
        }
        cameraView.addPictureCallback(object : PictureCallback() {
            override fun onPictureTaken(image: ByteArray, captureInfo: CaptureInfo) {
                processPictureTaken(image, captureInfo.imageOrientation)
            }
        })
        cameraView.setPreviewMode(CameraPreviewMode.FIT_IN)

        val scanbotSDK = ScanbotSDK(this)
        medicalCertificateRecognizer = scanbotSDK.createMedicalCertificateRecognizer()

        // Attach `FrameHandler`, that will be detecting Medical Certificate document on the camera frames
        val frameHandler = MedicalCertificateFrameHandler.attach(cameraView, medicalCertificateRecognizer)
        // Attach `AutoSnappingController`, that will trigger the snap as soon as `FrameHandler` will detect Medical Certificate document on the frame successfully
        val autoSnappingController = MedicalCertificateAutoSnappingController.attach(cameraView, frameHandler)

        findViewById<View>(R.id.flash).setOnClickListener { v: View? ->
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
        cameraView.onResume()

        Toast.makeText(this, "Scanning Medical Certificate...", Toast.LENGTH_LONG)
    }

    override fun onPause() {
        super.onPause()
        cameraView.onPause()
    }

    private fun processPictureTaken(image: ByteArray, imageOrientation: Int) {
        // Here we get the full image from the camera.
        // Implement a suitable async(!) detection and image handling here.

        // Decode Bitmap from bytes of original image:
        val options = BitmapFactory.Options()
        options.inSampleSize = 2 // use 1 for full, no downscaled image.
        var originalBitmap = BitmapFactory.decodeByteArray(image, 0, image.size, options)

        // And finally run Medical Certificate recognition on prepared document image:
        val resultInfo = medicalCertificateRecognizer.recognizeMcBitmap(originalBitmap,
            0,
            shouldCropDocument = true,
            returnCroppedDocument = true,
            recognizePatientInfo = true,
            recognizeBarcode = true)

        if (resultInfo != null && resultInfo.recognitionSuccessful) {
            // Show the cropped image as thumbnail preview
            resultInfo.croppedImage?.let {
                val thumbnailImage = resizeImage(it, 600f, 600f)
                runOnUiThread {
                    resultImageView.setImageBitmap(thumbnailImage)
                }
            }

            resultImageView.postDelayed({ startActivity(newIntent(this, resultInfo))}, 2000)
        } else {
            runOnUiThread {
                val toast = Toast.makeText(this, "No Medical Certificate content was recognized!", Toast.LENGTH_LONG)
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
            return Intent(context, MedicalCertificateRecognizerActivity::class.java)
        }
    }
}