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
// #END_Launching The Scanner

// #Delegate `onResume` and `onPause` methods
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
// #END_Delegate `onResume` and `onPause` methods

fun setOpenCallbackSnippet(cameraView: ScanbotCameraXView) {
    // #Enable/disable shutter sound
    cameraView.setCameraOpenCallback(object : CameraOpenCallback {
        override fun onCameraOpened() {
            cameraView.postDelayed({
                cameraView.setShutterSound(false)
            }, 700)
        }
    })
    // #END_Enable/disable shutter sound
}

fun continuousFocusSnippet(cameraView: ScanbotCameraXView) {
    // #Enable Continuous Focus Mode
    cameraView.setCameraOpenCallback(object : CameraOpenCallback {
        override fun onCameraOpened() {
            cameraView.postDelayed({
                cameraView.continuousFocus()
            }, 700)
        }
    })
    // #END_Enable Continuous Focus Mode
}

fun onPictureTakenSnippet(cameraView: ScanbotCameraXView) {
    cameraView.addPictureCallback(object : PictureCallback() {
        // #`onPictureTaken()` example
        override fun onPictureTaken(image: ByteArray, captureInfo: CaptureInfo) {
            // image processing ...
            // ...

            cameraView.post {
                cameraView.continuousFocus()
                cameraView.startPreview()
            }
        }
        // #END_`onPictureTaken()` example
    })
}

fun setCameraModuleSnippet(cameraView: ScanbotCameraXView) {
    // #Set Camera Module
    cameraView.setCameraModule(CameraModule.FRONT)
    cameraView.restartPreview()
    // #END_Set Camera Module
}

fun setCustomPreviewPictureSizeSnippet(cameraView: ScanbotCameraXView) {
    // #Set Custom Picture and Preview Sizes
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
    // #END_Set Custom Picture and Preview Sizes
}

fun useFlashSnippet(cameraView: ScanbotCameraXView, enabled: Boolean) {
    // #Enable/disable Flashlight
    cameraView.useFlash(enabled)
    // #END_Enable/disable Flashlight
}

fun isFlashEnabled(cameraView: ScanbotCameraXView) {
    // #Get Flashlight state
    val state = cameraView.isFlashEnabled()
    // #END_Get Flashlight state
}

fun startDocumentScannerSnippet(cameraView: ScanbotCameraXView, context: Context) {
    // #Start Document Scanner (full example)
    val scanner: DocumentScanner = ScanbotSDK(context).createDocumentScanner()

    val frameHandler = DocumentScannerFrameHandler(context, scanner)
    cameraView.addFrameHandler(frameHandler)
    // #END_Start Document Scanner (full example)
}

fun startDocumentScannerShortSnippet(cameraView: ScanbotCameraXView, context: Context) {
    // #Start Document Scanner (short)
    val scanner: DocumentScanner = ScanbotSDK(context).createDocumentScanner()
    val frameHandler = DocumentScannerFrameHandler.attach(cameraView, scanner)
    // #END_Start Document Scanner (short)
}

fun handleResultSnippet(frameHandler: DocumentScannerFrameHandler) {
    // #Handle results
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
    // #END_Handle results
}

fun documentScanningParamsSnippet(cameraView: ScanbotCameraXView, context: Context) {
    // #Customize Document Scanner Parameters
    val scanner: DocumentScanner = ScanbotSDK(context).createDocumentScanner()
    val frameHandler = DocumentScannerFrameHandler.attach(cameraView, scanner)
    frameHandler.setAcceptedAngleScore(75.0)
    frameHandler.setAcceptedSizeScore(80.0)
    // #END_Customize Document Scanner Parameters
}

fun addPolygonViewSnippet(cameraView: ScanbotCameraXView, frameHandler: DocumentScannerFrameHandler) {
    // #Bind `PolygonView` to `DocumentScannerFrameHandler`
    val polygonView = cameraView.findViewById<PolygonView>(R.id.polygonView)
    frameHandler.addResultHandler(polygonView.documentScannerResultHandler)
    // #END_Bind `PolygonView` to `DocumentScannerFrameHandler`
}

fun autoSnappingSnippet(cameraView: ScanbotCameraXView, frameHandler: DocumentScannerFrameHandler, context: Context) {
    // #Attach `DocumentAutoSnappingController`
    val documentScanner = ScanbotSDK(context).createDocumentScanner()
    val documentScannerFrameHandler = DocumentScannerFrameHandler.attach(cameraView, documentScanner)
    val autoSnappingController = DocumentAutoSnappingController.attach(cameraView, documentScannerFrameHandler)
    // #END_Attach `DocumentAutoSnappingController`
}

fun autoSnappingSensitivitySnippet(autoSnappingController: DocumentAutoSnappingController) {
    // #Set Auto Snapping Sensitivity
    autoSnappingController.setSensitivity(1f)
    // #END_Set Auto Snapping Sensitivity
}

fun autoSnappingVisualisationSnippet(scanbotCameraView: ScanbotCameraXView, documentScanner: DocumentScanner, polygonView: PolygonView) {
    // #Auto Snapping Visualisation
    val autoSnappingController = DocumentAutoSnappingController.attach(scanbotCameraView, documentScanner)
    autoSnappingController.stateListener = polygonView
    // #END_Auto Snapping Visualisation
}

fun handlingDocumentScanningResultSnippet(documentScannerFrameHandler: DocumentScannerFrameHandler) {
    // #Handle Document Scanning Results
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
    // #END_Handle Document Scanning Results
}

fun handlingCameraPictureSnippet(cameraView: ScanbotCameraXView, context: Context) {
    // #Handle Camera Picture
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
    // #END_Handle Camera Picture
}

fun finderPictureCallbackSnippet(cameraView: ScanbotCameraXView, context: Context) {
    // #Creating `FinderPictureCallback`
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
    // #END_Creating `FinderPictureCallback`
}

fun editPolygonViewSetPointsSnippet(editPolygonView: EditPolygonImageView, context: Context, image: Bitmap) {
    // #Set Scanned Contour to `EditPolygonImageView`
    val scanner = ScanbotSDK(context).createDocumentScanner()
    val scanningResult = scanner.scanFromBitmap(image)
    editPolygonView.polygon = scanningResult?.pointsNormalized ?: emptyList()
    // #END_Set Scanned Contour to `EditPolygonImageView`
}

fun editPolygonViewSetLines(editPolygonView: EditPolygonImageView, scanningResult: DocumentDetectionResult) {
    // #Set Scanned Lines to `EditPolygonImageView`
    editPolygonView.setLines(scanningResult?.horizontalLinesNormalized ?: emptyList(), scanningResult?.verticalLinesNormalized ?: emptyList())
    // #END_Set Scanned Lines to `EditPolygonImageView`
}

fun setupMagnifierSnippet(magnifierView: MagnifierView, editPolygonView: EditPolygonImageView) {
    // #Setup `MagnifierView`
    magnifierView.setupMagnifier(editPolygonView)
    // #END_Setup `MagnifierView`
}

fun getCurrentPolygonSnippet(editPolygonView: EditPolygonImageView) {
    // #Get Selected Polygon from `EditPolygonImageView`
    val currentPolygon = editPolygonView.polygon
    // #END_Get Selected Polygon from `EditPolygonImageView`
}