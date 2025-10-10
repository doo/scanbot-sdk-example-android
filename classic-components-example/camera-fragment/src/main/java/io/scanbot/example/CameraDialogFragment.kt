package io.scanbot.example

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import io.scanbot.common.getOrNull
import io.scanbot.common.getOrThrow
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.camera.CaptureInfo
import io.scanbot.sdk.camera.PictureCallback
import io.scanbot.sdk.document.DocumentAutoSnappingController
import io.scanbot.sdk.document.DocumentScannerFrameHandler
import io.scanbot.sdk.documentscanner.DocumentScanner
import io.scanbot.sdk.image.ImageRef
import io.scanbot.sdk.process.ImageProcessor
import io.scanbot.sdk.ui.PolygonView
import io.scanbot.sdk.ui.camera.ScanbotCameraXView

/** [ScanbotCameraXView] integrated in [DialogFragment] example. */
class CameraDialogFragment : DialogFragment() {
    private lateinit var cameraView: ScanbotCameraXView
    private lateinit var resultView: ImageView

    private lateinit var scanner: DocumentScanner

    private var flashEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scanbotSDK = ScanbotSDK(requireContext())
        scanner = scanbotSDK.createDocumentScanner().getOrThrow()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val baseView = requireActivity().layoutInflater.inflate(R.layout.scanbot_camera_view, container, false)
        cameraView = baseView.findViewById<View>(R.id.camera) as ScanbotCameraXView
        cameraView.setCameraOpenCallback {
            cameraView.postDelayed({
                cameraView.continuousFocus()
                cameraView.useFlash(flashEnabled)
            }, 700)
        }
        resultView = baseView.findViewById<View>(R.id.result) as ImageView
        val frameHandler = DocumentScannerFrameHandler.attach(cameraView, scanner)
        val polygonView: PolygonView = baseView.findViewById(R.id.polygonView)
        frameHandler.addResultHandler(polygonView.documentScannerResultHandler)
        DocumentAutoSnappingController.attach(cameraView, frameHandler)

        cameraView.addPictureCallback(object : PictureCallback() {
            override fun onPictureTaken(image: ImageRef, captureInfo: CaptureInfo) {
                processPictureTaken(image, captureInfo.imageOrientation)
            }
        })

        baseView.findViewById<View>(R.id.snap).setOnClickListener { cameraView.takePicture(false) }
        baseView.findViewById<View>(R.id.flash).setOnClickListener {
            flashEnabled = !flashEnabled
            cameraView.useFlash(flashEnabled)
        }
        return baseView
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            it.setLayout(width, height)
        }
    }

    private fun processPictureTaken(image: ImageRef, imageOrientation: Int) {

        // Run document scanning on original image:
        val result = scanner.run(image).getOrNull()
        result?.pointsNormalized?.let { polygonF ->
            val documentImage = ImageProcessor(image).crop(polygonF).resize(200).processedBitmap().getOrNull()
            resultView.post {
                resultView.setImageBitmap(documentImage)
                cameraView.continuousFocus()
                cameraView.startPreview()
            }
        }
    }

    companion object {
        /**
         * Create a new instance of CameraDialogFragment
         */
        fun newInstance(): CameraDialogFragment {
            return CameraDialogFragment()
        }
    }
}
