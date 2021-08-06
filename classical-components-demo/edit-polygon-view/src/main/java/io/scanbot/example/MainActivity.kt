package io.scanbot.example

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.PointF
import android.os.AsyncTask
import android.os.Bundle
import android.util.Pair
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.core.contourdetector.ContourDetector
import io.scanbot.sdk.core.contourdetector.DetectionResult
import io.scanbot.sdk.core.contourdetector.Line2D
import io.scanbot.sdk.process.CropOperation
import io.scanbot.sdk.process.ImageProcessor
import io.scanbot.sdk.ui.EditPolygonImageView
import io.scanbot.sdk.ui.MagnifierView
import java.util.concurrent.Executors
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    private lateinit var editPolygonView: EditPolygonImageView
    private lateinit var magnifierView: MagnifierView
    private lateinit var resultImageView: ImageView
    private lateinit var cropButton: Button
    private lateinit var rotateButton: Button
    private lateinit var backButton: Button

    private lateinit var originalBitmap: Bitmap

    private lateinit var imageProcessor: ImageProcessor
    private lateinit var contourDetector: ContourDetector

    private var lastRotationEventTs = 0L
    private var rotationDegrees = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val scanbotSDK = ScanbotSDK(this)
        contourDetector = scanbotSDK.createContourDetector()
        imageProcessor = scanbotSDK.imageProcessor()

        supportActionBar!!.hide()

        editPolygonView = findViewById(R.id.polygonView)
        magnifierView = findViewById(R.id.magnifier)
        resultImageView = findViewById(R.id.resultImageView)
        resultImageView.visibility = View.GONE
        cropButton = findViewById(R.id.cropButton)
        cropButton.setOnClickListener { crop() }
        rotateButton = findViewById(R.id.rotateButton)
        rotateButton.setOnClickListener { rotatePreview() }
        backButton = findViewById(R.id.backButton)

        backButton.setOnClickListener {
            backButton.visibility = View.GONE
            resultImageView.visibility = View.GONE
            editPolygonView.visibility = View.VISIBLE
            cropButton.visibility = View.VISIBLE
            rotateButton.visibility = View.VISIBLE
        }
        InitImageViewTask().executeOnExecutor(Executors.newSingleThreadExecutor())
    }

    private fun loadBitmapFromAssets(filePath: String): Bitmap {
            val inputStream = assets.open(filePath)
            return BitmapFactory.decodeStream(inputStream)
    }

    private fun resizeForPreview(bitmap: Bitmap): Bitmap {
        val maxW = 1000f
        val maxH = 1000f
        val oldWidth = bitmap.width.toFloat()
        val oldHeight = bitmap.height.toFloat()
        val scaleFactor = if (oldWidth > oldHeight) maxW / oldWidth else maxH / oldHeight
        val scaledWidth = (oldWidth * scaleFactor).roundToInt()
        val scaledHeight = (oldHeight * scaleFactor).roundToInt()
        return Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, false)
    }

    private fun rotatePreview() {
        if (System.currentTimeMillis() - lastRotationEventTs < 350) {
            return
        }
        rotationDegrees += 90
        editPolygonView.rotateClockwise() // rotates only the preview image
        lastRotationEventTs = System.currentTimeMillis()
    }

    private fun crop() {
        // crop & warp image by selected polygon (editPolygonView.getPolygon())
        val operations = listOf(CropOperation(editPolygonView.polygon))

        var documentImage = imageProcessor.processBitmap(originalBitmap, operations, false)
        documentImage?.let {
            if (rotationDegrees > 0) {
                // rotate the final cropped image result based on current rotation value:
                val matrix = Matrix()
                matrix.postRotate(rotationDegrees.toFloat())
                documentImage = Bitmap.createBitmap(it, 0, 0, it.width, it.height, matrix, true)
            }

            editPolygonView.visibility = View.GONE
            cropButton.visibility = View.GONE
            rotateButton.visibility = View.GONE
            resultImageView.setImageBitmap(resizeForPreview(documentImage!!))
            resultImageView.visibility = View.VISIBLE
            backButton.visibility = View.VISIBLE
        }
    }

    // We use AsyncTask only for simplicity here. Avoid using it in your production app due to memory leaks, etc!
    internal inner class InitImageViewTask : AsyncTask<Void?, Void?, InitImageResult>() {
        private var previewBitmap: Bitmap? = null

        override fun doInBackground(vararg params: Void?): InitImageResult {
            originalBitmap = loadBitmapFromAssets("demo_image.jpg")!!
            previewBitmap = resizeForPreview(originalBitmap)

            return when (contourDetector.detect(originalBitmap)) {
                DetectionResult.OK,
                DetectionResult.OK_BUT_BAD_ANGLES,
                DetectionResult.OK_BUT_TOO_SMALL,
                DetectionResult.OK_BUT_BAD_ASPECT_RATIO -> {
                    val linesPair = Pair(contourDetector.horizontalLines, contourDetector.verticalLines)
                    val polygon = contourDetector.polygonF!!

                    InitImageResult(linesPair, polygon)
                }
                else -> InitImageResult(Pair(listOf(), listOf()), listOf())
            }
        }

        override fun onPostExecute(initImageResult: InitImageResult) {
            editPolygonView.setImageBitmap(previewBitmap)
            magnifierView.setupMagnifier(editPolygonView)

            // set detected polygon and lines into EditPolygonImageView
            editPolygonView.polygon = initImageResult.polygon
            editPolygonView.setLines(initImageResult.linesPair.first, initImageResult.linesPair.second)
        }
    }

    internal inner class InitImageResult(val linesPair: Pair<List<Line2D>, List<Line2D>>, val polygon: List<PointF>)
}