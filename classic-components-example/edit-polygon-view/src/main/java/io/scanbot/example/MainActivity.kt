package io.scanbot.example

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.PointF
import android.os.Bundle
import android.util.Pair
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.common.LineSegmentFloat
import io.scanbot.sdk.document.DocumentDetectionStatus
import io.scanbot.sdk.document.DocumentScanner
import io.scanbot.sdk.process.ImageProcessor
import io.scanbot.sdk.ui.EditPolygonImageView
import io.scanbot.sdk.ui.MagnifierView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var editPolygonView: EditPolygonImageView
    private lateinit var magnifierView: MagnifierView
    private lateinit var resultImageView: ImageView
    private lateinit var cropButton: Button
    private lateinit var rotateButton: Button
    private lateinit var backButton: Button

    private lateinit var originalBitmap: Bitmap
    private lateinit var previewBitmap: Bitmap

    private lateinit var scanner: DocumentScanner

    private var lastRotationEventTs = 0L
    private var rotationDegrees = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val scanbotSDK = ScanbotSDK(this)
        scanner = scanbotSDK.createDocumentScanner()

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

        lifecycleScope.launch {
            val initImageResult = withContext(Dispatchers.Default) {
                originalBitmap = loadBitmapFromAssets("demo_image.jpg")
                previewBitmap = resizeForPreview(originalBitmap)

                val result = scanner.scanFromBitmap(originalBitmap)
                return@withContext when (result?.status) {
                    DocumentDetectionStatus.OK,
                    DocumentDetectionStatus.OK_BUT_BAD_ANGLES,
                    DocumentDetectionStatus.OK_BUT_TOO_SMALL,
                    DocumentDetectionStatus.OK_BUT_BAD_ASPECT_RATIO -> {
                        val linesPair = Pair(
                            result?.horizontalLinesNormalized ?: emptyList(),
                            result?.verticalLinesNormalized ?: emptyList()
                        )
                        val polygon = result?.pointsNormalized ?: emptyList()

                        InitImageResult(linesPair, polygon)
                    }

                    else -> InitImageResult(Pair(listOf(), listOf()), listOf())
                }
            }

            withContext(Dispatchers.Main) {
                editPolygonView.setImageBitmap(previewBitmap)
                magnifierView.setupMagnifier(editPolygonView)

                // set detected polygon and lines into EditPolygonImageView
                editPolygonView.polygon = initImageResult.polygon
                editPolygonView.setLines(
                    initImageResult.linesPair.first,
                    initImageResult.linesPair.second
                )
            }
        }
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
        var documentImage =
            ImageProcessor(originalBitmap).crop(editPolygonView.polygon).processedBitmap()
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

    internal inner class InitImageResult(
        val linesPair: Pair<List<LineSegmentFloat>, List<LineSegmentFloat>>,
        val polygon: List<PointF>
    )
}
