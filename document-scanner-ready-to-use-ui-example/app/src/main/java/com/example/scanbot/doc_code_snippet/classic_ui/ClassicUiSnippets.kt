package com.example.scanbot.doc_code_snippet.classic_ui

import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.ScanbotSDKInitializer
import io.scanbot.sdk.camera.CameraModule
import io.scanbot.sdk.camera.CameraOpenCallback
import io.scanbot.sdk.camera.CaptureInfo
import io.scanbot.sdk.camera.FinderPictureCallback
import io.scanbot.sdk.camera.FrameHandlerResult
import io.scanbot.sdk.camera.PictureCallback
import io.scanbot.sdk.camera.ScanbotCameraView
import io.scanbot.sdk.document.DocumentAutoSnappingController
import io.scanbot.sdk.document.DocumentDetectionResult
import io.scanbot.sdk.document.DocumentScanner
import io.scanbot.sdk.document.DocumentScannerFrameHandler
import io.scanbot.sdk.process.ImageProcessor
import io.scanbot.sdk.ui.EditPolygonImageView
import io.scanbot.sdk.ui.MagnifierView
import io.scanbot.sdk.ui.PolygonView
import io.scanbot.sdk.ui.camera.ScanbotCameraXView
import io.scanbot.sdk.usecases.documents.R

// #Launching The Scanner
class ExampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize the Scanbot Scanner SDK:
        ScanbotSDKInitializer().initialize(this)
    }
}
// #Launching The Scanner

class MyActivity : Activity() {

    private lateinit var scanbotCameraView: ScanbotCameraView

    //...

    override fun onResume() {
        super.onResume()
        scanbotCameraView.onResume()
    }

    override fun onPause() {
        scanbotCameraView.onPause()
        super.onPause()
    }
}

fun setOpenCallbackSnippet(cameraView: ScanbotCameraXView) {
    cameraView.setCameraOpenCallback(object : CameraOpenCallback {
        override fun onCameraOpened() {
            cameraView.postDelayed({
                cameraView.setShutterSound(false)
            }, 700)
        }
    })
}

fun continuousFocusSnippet(cameraView: ScanbotCameraXView) {
    cameraView.setCameraOpenCallback(object : CameraOpenCallback {
        override fun onCameraOpened() {
            cameraView.postDelayed({
                cameraView.continuousFocus()
            }, 700)
        }
    })
}

fun onPictureTakenSnippet(cameraView: ScanbotCameraXView) {
    cameraView.addPictureCallback(object : PictureCallback() {
        override fun onPictureTaken(image: ByteArray, captureInfo: CaptureInfo) {
            // image processing ...
            // ...

            cameraView.post {
                cameraView.continuousFocus()
                cameraView.startPreview()
            }
        }
    })
}

fun setCameraModuleSnippet(cameraView: ScanbotCameraXView) {
    cameraView.setCameraModule(CameraModule.FRONT);
    cameraView.restartPreview();
}

fun setCustomPreviewPictureSizeSnippet(cameraView: ScanbotCameraXView) {
    cameraView.setCameraOpenCallback {
        cameraView.stopPreview()

        val supportedPictureSizes = cameraView.supportedPictureSizes
        // For demo purposes we just take the first picture size from the supported list!
        cameraView.setPictureSize(supportedPictureSizes[0])

        val supportedPreviewSizes = cameraView.supportedPreviewSizes
        // For demo purposes we just take the first preview size from the supported list!
        cameraView.setPreviewFrameSize(supportedPreviewSizes[0])

        cameraView.startPreview()
    }
}

fun useFlashSnippet(cameraView: ScanbotCameraXView, enabled: Boolean) {
    cameraView.useFlash(enabled)
}

fun isFlashEnabled(cameraView: ScanbotCameraXView) {
    val state = cameraView.isFlashEnabled()
}

fun startDocumentScannerSnippet(cameraView: ScanbotCameraXView, context: Context) {
    val scanner: DocumentScanner = ScanbotSDK(context).createDocumentScanner()

    val frameHandler = DocumentScannerFrameHandler(context, scanner)
    cameraView.addFrameHandler(frameHandler)
}

fun startDocumentScannerShortSnippet(cameraView: ScanbotCameraXView, context: Context) {
    val scanner: DocumentScanner = ScanbotSDK(context).createDocumentScanner()
    val frameHandler = DocumentScannerFrameHandler.attach(cameraView, scanner)
}

fun handleResultSnippet(frameHandler: DocumentScannerFrameHandler) {
    frameHandler.addResultHandler(DocumentScannerFrameHandler.ResultHandler { result ->
        when (result) {
            is FrameHandlerResult.Success -> {
                result.value
                // handle result here result.value.detectionResult
            }
            is FrameHandlerResult.Failure -> {
                // there is a license problem that needs to be handled
            }
        }
        false
    })
}

fun documentScanningParamsSnippet(cameraView: ScanbotCameraXView, context: Context) {
    val scanner: DocumentScanner = ScanbotSDK(context).createDocumentScanner()
    val frameHandler = DocumentScannerFrameHandler.attach(cameraView, scanner)
    frameHandler.setAcceptedAngleScore(75.0)
    frameHandler.setAcceptedSizeScore(80.0)
}

fun addPolygonViewSnippet(cameraView: ScanbotCameraXView, frameHandler: DocumentScannerFrameHandler) {
    val polygonView = cameraView.findViewById<PolygonView>(R.id.polygonView)
    frameHandler.addResultHandler(polygonView.documentScannerResultHandler)
}

fun autoSnappingSnippet(cameraView: ScanbotCameraXView, frameHandler: DocumentScannerFrameHandler, context: Context) {
    val documentScanner = ScanbotSDK(context).createDocumentScanner()
    val documentScannerFrameHandler = DocumentScannerFrameHandler.attach(cameraView, documentScanner)
    val autoSnappingController = DocumentAutoSnappingController.attach(cameraView, documentScannerFrameHandler)
}

fun autoSnappingSensitivitySnippet(autoSnappingController: DocumentAutoSnappingController) {
    autoSnappingController.setSensitivity(1f)
}

fun autoSnappingVisualisationSnippet(scanbotCameraView: ScanbotCameraXView, documentScanner: DocumentScanner, polygonView: PolygonView) {
    val autoSnappingController = DocumentAutoSnappingController.attach(scanbotCameraView, documentScanner)
    autoSnappingController.stateListener = polygonView
}

fun handlingDocumentScanningResultSnippet(documentScannerFrameHandler: DocumentScannerFrameHandler) {
    documentScannerFrameHandler.addResultHandler(DocumentScannerFrameHandler.ResultHandler { result ->
        when (result) {
            is FrameHandlerResult.Success -> {
                // handle result here result.value.detectionResult
            }
            is FrameHandlerResult.Failure -> {
                // there is a license problem that needs to be handled
            }
        }
        false
    })
}

fun handlingCameraPictureSnippet(cameraView: ScanbotCameraXView, context: Context) {
    // Create one instance per screen
    val scanner: DocumentScanner = ScanbotSDK(context).createDocumentScanner()

    //...

    cameraView.addPictureCallback(object : PictureCallback() {
        override fun onPictureTaken(image: ByteArray, captureInfo: CaptureInfo) {
            fun restartCamera() {
                // Continue with the camera preview to scan the next image:
                cameraView.post {
                    cameraView.continuousFocus()
                    cameraView.startPreview()
                }
            }

            // Decode image byte array to Bitmap, and rotate according to orientation:
            val bitmap = ImageProcessor(image).rotate(captureInfo.imageOrientation).processedBitmap()

            if (bitmap == null) {
                // license or feature is not available
                restartCamera()
                return
            }

            // Run document contour detection on original image:
            val scanningResult = scanner.scanFromBitmap(bitmap)
            val documentPolygon = scanningResult?.pointsNormalized
            if (documentPolygon != null) {
                // And crop using detected polygon to get the final document image:
                val documentImage = ImageProcessor(bitmap).crop(documentPolygon).processedBitmap()

                // Work with the final document image (store it as a file, etc)
                // ...

                restartCamera()
            }
        }
    })
}

fun finderPictureCallbackSnippet(cameraView: ScanbotCameraXView, context: Context) {
    val scanbotSDK = ScanbotSDK(context)
    cameraView.addPictureCallback(object : FinderPictureCallback() {
        override fun onPictureTaken(image: Bitmap?, captureInfo: CaptureInfo) {
            // Work with the final image (store it as file, etc)
            // ...

            // Continue with the camera preview to scan the next image:
            cameraView.post {
                cameraView.continuousFocus()
                cameraView.startPreview()
            }
        }
    })
}

fun editPolygonViewSetPointsSnippet(editPolygonView: EditPolygonImageView, context: Context, image: Bitmap) {
    val scanner = ScanbotSDK(context).createDocumentScanner()
    val scanningResult = scanner.scanFromBitmap(image)
    editPolygonView.polygon = scanningResult?.pointsNormalized ?: emptyList()
}

fun editPolygonViewSetLines(editPolygonView: EditPolygonImageView, scanningResult: DocumentDetectionResult) {
    editPolygonView.setLines(scanningResult?.horizontalLinesNormalized ?: emptyList(), scanningResult?.verticalLinesNormalized ?: emptyList())
}

fun setupMagnifierSnippet(magnifierView: MagnifierView, editPolygonView: EditPolygonImageView) {
    magnifierView.setupMagnifier(editPolygonView)
}

fun getCurrentPolygonSnippet(editPolygonView: EditPolygonImageView) {
    val currentPolygon = editPolygonView.polygon
}