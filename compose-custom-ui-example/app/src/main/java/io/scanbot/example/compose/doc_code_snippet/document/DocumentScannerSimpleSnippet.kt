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

// @Tag("Simple Document Scanner Composable")
@Composable
@OptIn(ExperimentalCamera2Interop::class)
fun DocumentScannerSimpleSnippet() {
    val context = LocalContext.current
    val sdk = remember { ScanbotSDK(context) }
    val imageProcessor = remember { ScanbotSdkImageProcessor.create() }
    val documentScanner = remember { sdk.createDocumentScanner().getOrNull() }
    val scope = rememberCoroutineScope()
    val scannedImage = remember { mutableStateOf<Bitmap?>(null) }
    val takePictureActionController =
        remember { mutableStateOf<TakePictureActionController?>(null) }
    val cameraInProcessingState = remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        DocumentScannerCustomUI(
            // Modify Size here:
            modifier = Modifier.fillMaxSize(),
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
            clickable =  !cameraInProcessingState.value,
            // Show indicator  that camera in auto-snapping mode
            autoCapture = true,
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
// @EndTag("Simple Document Scanner Composable")
