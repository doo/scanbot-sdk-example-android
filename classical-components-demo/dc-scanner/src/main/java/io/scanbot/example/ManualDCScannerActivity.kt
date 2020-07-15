package io.scanbot.example

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import io.scanbot.example.DCResultActivity.Companion.newIntent
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.camera.CameraOpenCallback
import io.scanbot.sdk.camera.PictureCallback
import io.scanbot.sdk.camera.ScanbotCameraView
import io.scanbot.sdk.dcscanner.DCScanner
import io.scanbot.sdk.process.CropOperation
import io.scanbot.sdk.process.Operation
import java.util.*
import kotlin.math.roundToInt

class ManualDCScannerActivity : AppCompatActivity(), PictureCallback {
    private lateinit var cameraView: ScanbotCameraView
    private lateinit var resultImageView: ImageView

    private lateinit var dcScanner: DCScanner
    private lateinit var scanbotSDK: ScanbotSDK

    var flashEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_dc_scanner)
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
        cameraView.addPictureCallback(this)

        resultImageView = findViewById(R.id.resultImageView)

        scanbotSDK = ScanbotSDK(this)

        dcScanner = scanbotSDK.dcScanner()
        findViewById<View>(R.id.flash).setOnClickListener {
            flashEnabled = !flashEnabled
            cameraView.useFlash(flashEnabled)
        }

        findViewById<View>(R.id.take_picture_btn).setOnClickListener { v: View? -> cameraView.takePicture(false) }
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

    override fun onPictureTaken(image: ByteArray, imageOrientation: Int) {
        // Here we get the full image from the camera.
        // Implement a suitable async(!) detection and image handling here.

        // Decode Bitmap from bytes of original image:
        val options = BitmapFactory.Options()
        options.inSampleSize = 2 // use 1 for full, no downscaled image.
        var originalBitmap = BitmapFactory.decodeByteArray(image, 0, image.size, options)

        // rotate original image if required:
        if (imageOrientation > 0) {
            val matrix = Matrix()
            matrix.setRotate(imageOrientation.toFloat(), originalBitmap.width / 2f, originalBitmap.height / 2f)
            originalBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, false)
        }

        // Run document detection on original image:
        val detector = scanbotSDK.contourDetector()
        detector.detect(originalBitmap)
        val operations: MutableList<Operation> = ArrayList()
        operations.add(CropOperation(detector.polygonF!!))
        val documentImage = scanbotSDK.imageProcessor().process(originalBitmap, operations, false)

        documentImage?.let { docImage ->
            // Show the cropped image as thumbnail preview
            val thumbnailImage = resizeImage(docImage, 600f, 600f)
            runOnUiThread {
                resultImageView.setImageBitmap(thumbnailImage)
                // continue with camera preview
                cameraView.continuousFocus()
                cameraView.startPreview()
            }


            // And finally run DC recognition on prepared document image:
            val resultInfo = dcScanner.recognizeDCBitmap(docImage, 0)
            if (resultInfo != null && resultInfo.recognitionSuccessful) {
                startActivity(newIntent(this@ManualDCScannerActivity, resultInfo))
            } else {
                runOnUiThread {
                    val toast = Toast.makeText(this@ManualDCScannerActivity, "No DC content was recognized!", Toast.LENGTH_LONG)
                    toast.setGravity(Gravity.CENTER, 0, 0)
                    toast.show()
                }
            }

            // reset preview image
            resultImageView.postDelayed({ resultImageView.setImageBitmap(null) }, 1000)
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
        fun newIntent(context: Context?): Intent {
            return Intent(context, ManualDCScannerActivity::class.java)
        }
    }
}