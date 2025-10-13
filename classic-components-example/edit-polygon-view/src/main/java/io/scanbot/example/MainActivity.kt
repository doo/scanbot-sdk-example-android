package io.scanbot.example

import android.graphics.Bitmap
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
import io.scanbot.common.getOrNull
import io.scanbot.common.getOrThrow
import io.scanbot.example.common.applyEdgeToEdge
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.documentscanner.DocumentDetectionStatus
import io.scanbot.sdk.documentscanner.DocumentScanner
import io.scanbot.sdk.geometry.LineSegmentFloat
import io.scanbot.sdk.image.ImageRef
import io.scanbot.sdk.image.ImageRotation
import io.scanbot.sdk.ui.EditPolygonImageView
import io.scanbot.sdk.ui.MagnifierView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.scanbot.sdk.imageprocessing.ImageProcessor
import io.scanbot.sdk.imageprocessing.ScanbotSdkImageProcessor

class MainActivity : AppCompatActivity() {

    private lateinit var editPolygonView: EditPolygonImageView
    private lateinit var magnifierView: MagnifierView
    private lateinit var resultImageView: ImageView
    private lateinit var cropButton: Button
    private lateinit var rotateButton: Button
    private lateinit var backButton: Button

    private lateinit var originalImage: ImageRef
    private lateinit var previewImage: ImageRef

    private lateinit var scanner: DocumentScanner

    private var lastRotationEventTs = 0L
    private var rotationDegrees = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        applyEdgeToEdge(findViewById(R.id.root_view))

        val scanbotSDK = ScanbotSDK(this)

        scanner = scanbotSDK.createDocumentScanner().getOrThrow()

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
            withContext(Dispatchers.Default) {
                originalImage = loadImageFromAssets("demo_image.jpg")
                previewImage = resizeForPreview(originalImage)
            }
            val result = withContext(Dispatchers.Default) {
                scanner.run(originalImage).getOrThrow()
            }
            val editViewMetadata = withContext(Dispatchers.Default) {
                return@withContext when (result?.status) {
                    DocumentDetectionStatus.OK, DocumentDetectionStatus.OK_BUT_BAD_ANGLES, DocumentDetectionStatus.OK_BUT_TOO_SMALL, DocumentDetectionStatus.OK_BUT_BAD_ASPECT_RATIO -> {
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
                editPolygonView.setImageBitmap(previewImage?.toBitmap()?.getOrNull())
                magnifierView.setupMagnifier(editPolygonView)

                // set detected polygon and lines into EditPolygonImageView
                editPolygonView.polygon = editViewMetadata.polygon
                editPolygonView.setLines(
                    editViewMetadata.linesPair.first, editViewMetadata.linesPair.second
                )
            }
        }
    }

    private fun loadImageFromAssets(filePath: String): ImageRef {
        val inputStream = assets.open(filePath)
        return ImageRef.fromInputStream(inputStream)
    }

    private fun resizeForPreview(image: ImageRef): ImageRef {
        return ScanbotSdkImageProcessor.create().resize(image, 1000).getOrNull() ?: image
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

        val polygon = editPolygonView.polygon
        val imageProcessor = ScanbotSdkImageProcessor.create()
        val croppedImage = imageProcessor.crop(originalImage, polygon).getOrThrow()
        var documentImage =
            if (rotationDegrees > 0) imageProcessor.rotate(croppedImage, rotationDegrees.toImageRotation() ).getOrThrow()
            else croppedImage

        editPolygonView.visibility = View.GONE
        cropButton.visibility = View.GONE
        rotateButton.visibility = View.GONE
        resultImageView.setImageBitmap(resizeForPreview(documentImage!!).toBitmap().getOrNull())
        resultImageView.visibility = View.VISIBLE
        backButton.visibility = View.VISIBLE
    }

    internal inner class InitImageResult(
        val linesPair: Pair<List<LineSegmentFloat>, List<LineSegmentFloat>>,
        val polygon: List<PointF>
    )


    fun Int.toImageRotation() = when (this) {
        90 -> ImageRotation.CLOCKWISE_90
        180 -> ImageRotation.CLOCKWISE_180
        270 -> ImageRotation.COUNTERCLOCKWISE_90
        else -> ImageRotation.NONE
    }

}
