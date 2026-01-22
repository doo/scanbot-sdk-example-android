package io.scanbot.example.compose.doc_code_snippet.document

import android.graphics.Bitmap
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.scanbot.common.mapSuccess
import io.scanbot.common.onFailure
import io.scanbot.common.onSuccess
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.camera.FrameHandler
import io.scanbot.sdk.documentscanner.DocumentDetectionResult
import io.scanbot.sdk.documentscanner.DocumentDetectionStatus
import io.scanbot.sdk.documentscanner.DocumentScanner
import io.scanbot.sdk.documentscanner.DocumentScannerConfiguration
import io.scanbot.sdk.documentscanner.DocumentScannerParameters
import io.scanbot.sdk.image.ImageRef
import io.scanbot.sdk.imageprocessing.ScanbotSdkImageProcessor
import io.scanbot.sdk.ui_v2.common.CameraModule
import io.scanbot.sdk.ui_v2.common.CameraPermissionScreen
import io.scanbot.sdk.ui_v2.common.CameraPreviewMode
import io.scanbot.sdk.ui_v2.common.camera.TakePictureActionController
import io.scanbot.sdk.ui_v2.common.components.ScanbotCameraPermissionView
import io.scanbot.sdk.ui_v2.common.components.ScanbotSnapButton
import io.scanbot.sdk.ui_v2.document.DocumentScannerCustomUI
import io.scanbot.sdk.ui_v2.document.components.camera.ScanbotDocumentArOverlay
import io.scanbot.sdk.ui_v2.document.screen.AutoSnappingConfiguration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

// @Tag("Detailed Document Scanner Composable")
@Composable
@OptIn(ExperimentalCamera2Interop::class)
fun DocumentScannerSnippet() {
    val context = LocalContext.current
    val sdk = remember { ScanbotSDK(context) }
    val imageProcessor = remember { ScanbotSdkImageProcessor.create() }
    val documentScanner = remember { sdk.createDocumentScanner().getOrNull() }
    //@Tag("Mutable states for camera control")
    // Use these states to control camera, torch and zoom
    val zoom = remember { mutableFloatStateOf(1.0f) }
    val torchEnabled = remember { mutableStateOf(false) }
    val cameraEnabled = remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    // Unused in this example, but you may use it to
    // enable/disable barcode scanning dynamically
    val scanningEnabled = remember { mutableStateOf(true) }
    val autosnappingEnabled = remember { mutableStateOf(false) }
    val cameraInProcessingState = remember { mutableStateOf(false) }
    val scannedImage = remember { mutableStateOf<Bitmap?>(null) }
    val takePictureActionController =
        remember { mutableStateOf<TakePictureActionController?>(null) }
    //@EndTag("Mutable states for camera control")
    Box(
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        DocumentScannerCustomUI(
            // Modify Size here:
            modifier = Modifier.fillMaxSize(),
            // Enable or disable camera view.
            cameraEnabled = cameraEnabled.value,
            // Select front or back camera here:
            cameraModule = CameraModule.BACK,
            // Set camera preview mode here. Possible values: FIT_IN, FILL_IN
            cameraPreviewMode = CameraPreviewMode.FILL_IN,
            // Enable or disable document scanning process here:
            documentScanningEnabled = scanningEnabled.value,
            // Auto-snapping configuration
            autoSnappingConfiguration = AutoSnappingConfiguration(
                enabled = autosnappingEnabled.value,
                //Changes sensitivity of auto-snapping. That is: the more sensitive it is the faster it shoots.
                // Sensitivity must be within `[0..1]` range. A value of 1.0 triggers automatic capturing immediately,
                // a value of 0.0 delays the automatic by 3 seconds.
                sensitivity = 0.66f,
                // Enable or disable shake detection to stop auto-snapping when device is shaken
                shakeDetectionEnabled = true,
            ),
            // Enable or disable torch here:
            torchEnabled = torchEnabled.value,
            // Set zoom level. Range from 1.0 to 5.0:
            zoomLevel = zoom.floatValue,
            // Document scanner configuration to customize scanning parameters . Eg document apect ratios, size ratios, etc.
            documentScannerConfiguration = DocumentScannerConfiguration(
                parameters = DocumentScannerParameters(
                    ignoreOrientationMismatch = true
                )
            ),
            //uncomment to turn on the finder view with custom aspect ratio
            // See more details about FinderConfiguration in the FinderConfigurationSnippet.kt
            /*finderConfiguration = FinderConfiguration(
                verticalAlignment = Alignment.CenterVertically,
                // Modify aspect ratio of the viewfinder here:
                aspectRatio = AspectRatio(21.0, 29.0),
            ),*/
            // Permission view that will be shown if camera permission is not granted
            permissionView = {
                // View that will be shown while camera permission is not granted
                ScanbotCameraPermissionView(
                    modifier = Modifier.fillMaxSize(),
                    bottomContentPadding = 0.dp,
                    permissionConfig = CameraPermissionScreen(),
                    onClose = {
                        // Handle permission screen close if needed
                    })
            },
            // AR Polygon overlay configuration
            arPolygonView = { dataFlow ->
                // AR Overlay composable that will be drawn over the camera preview
                // remove this block to disable AR overlay
                ScanbotDocumentArOverlay(
                    dataFlow = dataFlow,
                    // Enable or disable all polygons drawing here
                    drawPolygon = true,
                    isPolygonOk = { status ->
                        // Define which detection statuses are considered for AR polygon as OK
                        status == DocumentDetectionStatus.OK
                    },
                    // lambda to customize polygon style when document is shown as OK
                    getOkPolygonStyle = { defaultStyle ->
                        // Customize polygon style if needed
                        defaultStyle.copy(
                            strokeWidth = 8f,
                            strokeColor = Color.Blue,
                            fillColor = Color.Blue.copy(alpha = 0.15f)
                        )
                    },
                    // lambda to customize polygon style when document is shown as Not OK
                    getNotOkPolygonStyle = { defaultStyle ->
                        // Customize polygon style if needed
                        defaultStyle.copy(
                            strokeWidth = 8f,
                            strokeColor = Color.Red,
                            fillColor = Color.Red.copy(alpha = 0.15f)
                        )
                    },
                    // lambda to customize polygon that shown on top of the OK polygon during auto-snapping countdown
                    getProgressPolygonStyle = { defaultStyle ->
                        // Customize polygon style if needed
                        defaultStyle.copy(
                            strokeWidth = 8f,
                            strokeColor = Color.Green,
                            fillColor = Color.Transparent
                        )
                    },
                )
            },
            // Triggered when take picture is called (auto or manual)
            onTakePictureCalled = {
                Log.d("DocumentScannerScreen1", "Take picture called")
                cameraInProcessingState.value = true
            },
            // Triggered when take picture is canceled (auto or manual)
            onTakePictureCanceled = {
                Log.d("DocumentScannerScreen1", "Take picture canceled")
                cameraInProcessingState.value = false
            },
            // Triggered when picture is taken successfully and provides image for further processing
            onPictureSnapped = { imageRef, captureInfo ->
                // WARNING: move all processing operation to view model with proper coroutine scope in real apps to avoid data loss during recompositions
                scope.launch(Dispatchers.Default) {
                    scannedImage.value = createPreview(documentScanner, imageRef, imageProcessor)
                    // Picture is received, allow auto-snapping again or proceed further and allow image snap after some additional processing
                    cameraInProcessingState.value = false
                }
            },
            // Provides TakePictureActionController to use for triggering picture taking manually
            onTakePictureControllerCreated = {
                takePictureActionController.value = it
            },
            // Callback that is called right before auto-snapping should be triggered.
            // Return false to allow auto-snapping, true to prevent it.
            onAutoSnapping = {
                // return true if auto-snapping should be consumed and not proceed to take picture
                cameraInProcessingState.value // Disable auto-snapping while awaiting picture result after snap is triggered
            },
            // Callback invoked after each frame with document scanning result.
            onDocumentScanningResult = { result ->
                result.onSuccess { data ->
                    // Handle scanned barcodes here (for example, show a dialog)
                    val points = data.pointsNormalized
                    val status = data.status
                }
            },
        )
        ScanbotSnapButton(
            modifier = Modifier
                .height(100.dp)
                .align(Alignment.BottomCenter),
            // Disable button when scanning or auto-snapping is disabled
            clickable = scanningEnabled.value && !cameraInProcessingState.value,
            // Show indicator  that camera in auto-snapping mode
            autoCapture = autosnappingEnabled.value,
            // Animate progress when camera is processing the last taken picture
            animateProgress = cameraInProcessingState.value,
        ) {
            takePictureActionController.value?.invoke()
        }

        // Simple view that shows scanned image preview
        if (scannedImage.value != null) {
            AsyncImage(
                model = scannedImage.value,
                contentDescription = "ScannedImage",
                modifier = Modifier
                    .height(100.dp)
                    .align(Alignment.BottomEnd)
            )
        }
    }
}

private fun createPreview(
    documentScanner: DocumentScanner?,
    imageRef: ImageRef,
    imageProcessor: ScanbotSdkImageProcessor,
): Bitmap? {
    // See https://docs.scanbot.io/android/data-capture-modules/detailed-setup-guide/result-api/ for details of result handling
    // run detection and cropping on the captured image
    return documentScanner?.run(imageRef)?.mapSuccess { documentData ->
        val croppedImage =
            imageProcessor.crop(imageRef, documentData.pointsNormalized)
                .getOrReturn() // get the result of cropping operation or leave onSuccess if cropping failed
        imageRef.close() // clear image ref resources

        val downscaledCrop = imageProcessor.resize(croppedImage, 300).getOrReturn().toBitmap()
            .getOrReturn() // get the result of cropping operation or leave onSuccess if cropping failed
        croppedImage.close() // clear image ref resources
        downscaledCrop
    }?.onFailure { error ->
        Log.e(
            "DocumentScannerScreen",
            "Document scanning error: ${error.message}"
        )
    }?.getOrNull()
}
// @EndTag("Detailed Document Scanner Composable")

// @Tag("Detailed Snap Button Composable")
@Composable
fun SnapButtonSnippet() {
    ScanbotSnapButton(
        modifier = Modifier
            .height(100.dp),
        // Disable button when scanning or auto-snapping is disabled
        clickable = true,
        // Show indicator that camera in auto-snapping mode
        autoCapture = false,
        // Animate progress when camera is processing the last taken picture
        // Need to be true to show progress animation. Use mutable state to control it.
        animateProgress = false,
        // Rotation speed of the outer big arc in auto-capture mode
        bigArcSpeed = 3000,
        // Speed of the progress arc during processing
        progressSpeed = 400,
        // Color of buttons inner component
        innerColor = Color.White,
        // Outer color of buttons outer component
        outerColor = Color.Red,
        // Color of the outer Arc
        lineWidth = 5.dp,
        // Size of the empty space between inner and outer components
        emptyLineWidth = 10.dp,
        // Sweep angle of the outer arc
        arcSweepAngle = 270f,
        // Initial angle of the 360 degrees rotating big arc
        bigArcInitialAngle = 200f,
        // Scale factor applied to the button when pressed
        snapButtonPressedSize = 1.2f
    ) {
        // Handle button click here
    }
}
// @EndTag("Detailed Snap Button Composable")

// @Tag(" Document AR overlay Snippet")
@Composable
fun DocumentArOverlaySnippet(dataFlow: SharedFlow<Pair<DocumentDetectionResult, FrameHandler.Frame>>) {
    // AR Overlay composable that will be drawn over the camera preview
    // remove this block to disable AR overlay
    ScanbotDocumentArOverlay(
        dataFlow = dataFlow,
        // Enable or disable all polygons drawing here
        drawPolygon = true,
        isPolygonOk = { status ->
            // Define which detection statuses are considered for AR polygon as OK
            status == DocumentDetectionStatus.OK
        },
        // lambda to customize polygon style when document is shown as OK
        getOkPolygonStyle = { defaultStyle ->
            // Customize polygon style if needed
            defaultStyle.copy(
                strokeWidth = 8f,
                strokeColor = Color.Blue,
                fillColor = Color.Blue.copy(alpha = 0.15f)
            )
        },
        // lambda to customize polygon style when document is shown as Not OK
        getNotOkPolygonStyle = { defaultStyle ->
            // Customize polygon style if needed
            defaultStyle.copy(
                strokeWidth = 8f,
                strokeColor = Color.Red,
                fillColor = Color.Red.copy(alpha = 0.15f)
            )
        },
        // lambda to customize polygon that shown on top of the OK polygon during auto-snapping countdown
        getProgressPolygonStyle = { defaultStyle ->
            // Customize polygon style if needed
            defaultStyle.copy(
                strokeWidth = 8f,
                strokeColor = Color.Green,
                fillColor = Color.Transparent
            )
        },
    )
}