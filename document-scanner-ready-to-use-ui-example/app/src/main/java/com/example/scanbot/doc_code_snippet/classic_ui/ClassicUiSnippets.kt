package com.example.scanbot.doc_code_snippet.classic_ui

import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import io.scanbot.common.getOrNull
import io.scanbot.common.getOrThrow
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
import io.scanbot.sdk.document.DocumentScannerFrameHandler
import io.scanbot.sdk.documentscanner.DocumentDetectionResult
import io.scanbot.sdk.documentscanner.DocumentScanner
import io.scanbot.sdk.image.ImageRef
import io.scanbot.sdk.process.ImageProcessor
import io.scanbot.sdk.ui.EditPolygonImageView
import io.scanbot.sdk.ui.MagnifierView
import io.scanbot.sdk.ui.PolygonView
import io.scanbot.sdk.ui.camera.ScanbotCameraXView
import io.scanbot.sdk.usecases.documents.R

// @Tag("Launching The Scanner")
class ExampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize the Scanbot Scanner SDK:
        ScanbotSDKInitializer().initialize(this)
    }
}
// @EndTag("Launching The Scanner")

// @Tag("Delegate onResume and onPause methods")
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
// @EndTag("Delegate onResume and onPause methods")

fun setOpenCallbackSnippet(cameraView: ScanbotCameraXView) {
    // @Tag("Enable/disable shutter sound")
    cameraView.setCameraOpenCallback(object : CameraOpenCallback {
        override fun onCameraOpened() {
            cameraView.postDelayed({
                cameraView.setShutterSound(false)
            }, 700)
        }
    })
    // @EndTag("Enable/disable shutter sound")
}

fun continuousFocusSnippet(cameraView: ScanbotCameraXView) {
    // @Tag("Enable Continuous Focus Mode")
    cameraView.setCameraOpenCallback(object : CameraOpenCallback {
        override fun onCameraOpened() {
            cameraView.postDelayed({
                cameraView.continuousFocus()
            }, 700)
        }
    })
    // @EndTag("Enable Continuous Focus Mode")
}

fun onPictureTakenSnippet(cameraView: ScanbotCameraXView) {
    cameraView.addPictureCallback(object : PictureCallback() {
        // @Tag("onPictureTaken() example")
        override fun onPictureTaken(image: ImageRef, captureInfo: CaptureInfo) {
            // image processing ...
            // ...

            cameraView.post {
                cameraView.continuousFocus()
                cameraView.startPreview()
            }
        }
        // @EndTag("onPictureTaken() example")
    })
}

fun setCameraModuleSnippet(cameraView: ScanbotCameraXView) {
    // @Tag("Set Camera Module")
    cameraView.setCameraModule(CameraModule.FRONT)
    cameraView.restartPreview()
    // @EndTag("Set Camera Module")
}

fun setCustomPreviewPictureSizeSnippet(cameraView: ScanbotCameraXView) {
    // @Tag("Set Custom Picture and Preview Sizes")
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
    // @EndTag("Set Custom Picture and Preview Sizes")
}

fun useFlashSnippet(cameraView: ScanbotCameraXView, enabled: Boolean) {
    // @Tag("Enable/disable Flashlight")
    cameraView.useFlash(enabled)
    // @EndTag("Enable/disable Flashlight")
}

fun isFlashEnabled(cameraView: ScanbotCameraXView) {
    // @Tag("Get Flashlight state")
    val state = cameraView.isFlashEnabled()
    // @EndTag("Get Flashlight state")
}

fun startDocumentScannerSnippet(cameraView: ScanbotCameraXView, context: Context) {
    // @Tag("Start Document Scanner (full example)")
    val scanner: DocumentScanner = ScanbotSDK(context).createDocumentScanner().getOrThrow()

    val frameHandler = DocumentScannerFrameHandler(context, scanner)
    cameraView.addFrameHandler(frameHandler)
    // @EndTag("Start Document Scanner (full example)")
}

fun startDocumentScannerShortSnippet(cameraView: ScanbotCameraXView, context: Context) {
    // @Tag("Start Document Scanner (short)")
    val scanner: DocumentScanner = ScanbotSDK(context).createDocumentScanner().getOrThrow()
    val frameHandler = DocumentScannerFrameHandler.attach(cameraView, scanner)
    // @EndTag("Start Document Scanner (short)")
}

fun handleResultSnippet(frameHandler: DocumentScannerFrameHandler) {
    // @Tag("Handle results")
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
    // @EndTag("Handle results")
}

fun documentScanningParamsSnippet(cameraView: ScanbotCameraXView, context: Context) {
    // @Tag("Customize Document Scanner Parameters")
    val scanner: DocumentScanner = ScanbotSDK(context).createDocumentScanner().getOrThrow()
    val frameHandler = DocumentScannerFrameHandler.attach(cameraView, scanner)
    scanner.setConfiguration(scanner.copyCurrentConfiguration().apply {
        this.parameters.acceptedSizeScore = 80
        this.parameters.acceptedAngleScore = 75
    })
    // @EndTag("Customize Document Scanner Parameters")
}

fun addPolygonViewSnippet(
    cameraView: ScanbotCameraXView,
    frameHandler: DocumentScannerFrameHandler
) {
    // @Tag("Bind PolygonView to DocumentScannerFrameHandler")
    val polygonView = cameraView.findViewById<PolygonView>(R.id.polygonView)
    frameHandler.addResultHandler(polygonView.documentScannerResultHandler)
    // @EndTag("Bind PolygonView to DocumentScannerFrameHandler")
}

fun autoSnappingSnippet(
    cameraView: ScanbotCameraXView,
    frameHandler: DocumentScannerFrameHandler,
    context: Context
) {
    // @Tag("Attach DocumentAutoSnappingController")
    val documentScanner = ScanbotSDK(context).createDocumentScanner().getOrThrow()
    val documentScannerFrameHandler =
        DocumentScannerFrameHandler.attach(cameraView, documentScanner)
    val autoSnappingController =
        DocumentAutoSnappingController.attach(cameraView, documentScannerFrameHandler)
    // @EndTag("Attach DocumentAutoSnappingController")
}

fun autoSnappingSensitivitySnippet(autoSnappingController: DocumentAutoSnappingController) {
    // @Tag("Set Auto Snapping Sensitivity")
    autoSnappingController.setSensitivity(1f)
    // @EndTag("Set Auto Snapping Sensitivity")
}

fun autoSnappingVisualisationSnippet(
    scanbotCameraView: ScanbotCameraXView,
    documentScanner: DocumentScanner,
    polygonView: PolygonView
) {
    // @Tag("Auto Snapping Visualisation")
    val autoSnappingController =
        DocumentAutoSnappingController.attach(scanbotCameraView, documentScanner)
    autoSnappingController.stateListener = polygonView
    // @EndTag("Auto Snapping Visualisation")
}

fun handlingDocumentScanningResultSnippet(documentScannerFrameHandler: DocumentScannerFrameHandler) {
    // @Tag("Handle Document Scanning Results")
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
    // @EndTag("Handle Document Scanning Results")
}

fun handlingCameraPictureSnippet(cameraView: ScanbotCameraXView, context: Context) {
    // @Tag("Handle Camera Picture")
    // Create one instance per screen
    val scanner: DocumentScanner = ScanbotSDK(context).createDocumentScanner().getOrThrow()

    //...

    cameraView.addPictureCallback(object : PictureCallback() {
        override fun onPictureTaken(imageRef: ImageRef, captureInfo: CaptureInfo) {
            fun restartCamera() {
                // Continue with the camera preview to scan the next image:
                cameraView.post {
                    cameraView.continuousFocus()
                    cameraView.startPreview()
                }
            }

            // Decode image byte array to Bitmap, and rotate according to orientation:
            val image =
                ImageProcessor(imageRef).rotate(captureInfo.imageOrientation).processedImageRef()

            if (image == null) {
                // license or feature is not available
                restartCamera()
                return
            }

            // Run document contour detection on original image:
            val scanningResult = scanner.run(image).getOrNull()
            val documentPolygon = scanningResult?.pointsNormalized
            if (documentPolygon != null) {
                // And crop using detected polygon to get the final document image:
                val documentImage = ImageProcessor(image).crop(documentPolygon).processedBitmap()

                // Work with the final document image (store it as a file, etc)
                // ...

                restartCamera()
            }
        }
    })
    // @EndTag("Handle Camera Picture")
}

fun finderPictureCallbackSnippet(cameraView: ScanbotCameraXView, context: Context) {
    // @Tag("Creating FinderPictureCallback")
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
    // @EndTag("Creating FinderPictureCallback")
}

fun editPolygonViewSetPointsSnippet(
    editPolygonView: EditPolygonImageView,
    context: Context,
    image: ImageRef
) {
    // @Tag("Set Scanned Contour to EditPolygonImageView")
    val scanner = ScanbotSDK(context).createDocumentScanner().getOrNull()
    val scanningResult = scanner?.run(image)?.getOrNull()
    editPolygonView.polygon = scanningResult?.pointsNormalized ?: emptyList()
    // @EndTag("Set Scanned Contour to EditPolygonImageView")
}

fun editPolygonViewSetLines(
    editPolygonView: EditPolygonImageView,
    scanningResult: DocumentDetectionResult
) {
    // @Tag("Set Scanned Lines to EditPolygonImageView")
    editPolygonView.setLines(
        scanningResult?.horizontalLinesNormalized ?: emptyList(),
        scanningResult?.verticalLinesNormalized ?: emptyList()
    )
    // @EndTag("Set Scanned Lines to EditPolygonImageView")
}

fun setupMagnifierSnippet(magnifierView: MagnifierView, editPolygonView: EditPolygonImageView) {
    // @Tag("Setup MagnifierView")
    magnifierView.setupMagnifier(editPolygonView)
    // @EndTag("Setup MagnifierView")
}

fun getCurrentPolygonSnippet(editPolygonView: EditPolygonImageView) {
    // @Tag("Get Selected Polygon from EditPolygonImageView")
    val currentPolygon = editPolygonView.polygon
    // @EndTag("Get Selected Polygon from EditPolygonImageView")
}