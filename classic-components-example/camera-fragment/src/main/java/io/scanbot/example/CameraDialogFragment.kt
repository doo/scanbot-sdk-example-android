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
import io.scanbot.common.onSuccess


import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.camera.CaptureInfo
import io.scanbot.sdk.camera.PictureCallback
import io.scanbot.sdk.common.catchWithResult
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


    private var flashEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val scanbotSDK = ScanbotSDK(requireContext())
        val baseView =
            requireActivity().layoutInflater.inflate(R.layout.scanbot_camera_view, container, false)

        scanbotSDK.createDocumentScanner().onSuccess { scanner ->

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
                    processPictureTaken(image, scanner)
                }
            })

            baseView.findViewById<View>(R.id.snap)
                .setOnClickListener { cameraView.takePicture(false) }
            baseView.findViewById<View>(R.id.flash).setOnClickListener {
                flashEnabled = !flashEnabled
                cameraView.useFlash(flashEnabled)
            }
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

    private fun processPictureTaken(image: ImageRef, scanner: DocumentScanner) = catchWithResult {
        // Run document scanning on original image:
        val result = scanner.run(image).getOrReturn()
        val documentImage =
            result.pointsNormalized.takeIf { it.size == 4 }
                ?.let { ImageProcessor(image).crop(it).resize(200).processedBitmap() }
                ?.getOrReturn()
        resultView.post {
            resultView.setImageBitmap(documentImage)
            cameraView.continuousFocus()
            cameraView.startPreview()
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
