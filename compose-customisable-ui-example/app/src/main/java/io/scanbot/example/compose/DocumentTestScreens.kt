package io.scanbot.example.compose

import android.graphics.Bitmap
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import io.scanbot.common.onFailure
import io.scanbot.common.onSuccess
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.documentscanner.DocumentDetectionStatus
import io.scanbot.sdk.documentscanner.DocumentScannerConfiguration
import io.scanbot.sdk.documentscanner.DocumentScannerParameters
import io.scanbot.sdk.geometry.AspectRatio
import io.scanbot.sdk.imagemanipulation.ScanbotSdkImageManipulator
import io.scanbot.sdk.imageprocessing.ScanbotSdkImageProcessor
import io.scanbot.sdk.ui_v2.common.CameraPermissionScreen
import io.scanbot.sdk.ui_v2.common.camera.TakePictureActionController
import io.scanbot.sdk.ui_v2.common.components.FinderConfiguration
import io.scanbot.sdk.ui_v2.common.components.ScanbotCameraPermissionView
import io.scanbot.sdk.ui_v2.common.components.ScanbotSnapButton
import io.scanbot.sdk.ui_v2.document.DocumentScannerCustomUI
import io.scanbot.sdk.ui_v2.document.components.camera.ScanbotDocumentArOverlay
import io.scanbot.sdk.ui_v2.document.screen.AutoSnappingConfiguration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
@OptIn(ExperimentalCamera2Interop::class)
fun DocumentScannerScreen1(navController: NavHostController) {
    val density = LocalDensity.current
    Column(modifier = Modifier.systemBarsPadding()) {
        val context = LocalContext.current
        val sdk = remember { ScanbotSDK(context) }
        val imageProcessor = remember { ScanbotSdkImageProcessor.create() }
        val documentScanner = remember { sdk.createDocumentScanner().getOrNull() }
        // Use these states to control camera, torch and zoom
        val zoom = remember { mutableFloatStateOf(1.0f) }
        val torchEnabled = remember { mutableStateOf(false) }
        val cameraEnabled = remember { mutableStateOf(true) }
        val scope = rememberCoroutineScope()
        // Unused in this example, but you may use it to
        // enable/disable barcode scanning dynamically
        val scanningEnabled = remember { mutableStateOf(true) }
        val autosnappingEnabled = remember { mutableStateOf(true) }
        val cameraInProcessingState = remember { mutableStateOf(false) }
        val scannedImage = remember { mutableStateOf<Bitmap?>(null) }
        val takePictureActionController =
            remember { mutableStateOf<TakePictureActionController?>(null) }
        val documentScanningStatus =
            remember { mutableStateOf(DocumentDetectionStatus.ERROR_NOTHING_DETECTED) }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f),
        ) {
            DocumentScannerCustomUI(
                // Modify Size here:
                modifier = Modifier.fillMaxSize(),
                cameraEnabled = cameraEnabled.value,
                documentScanningEnabled = scanningEnabled.value,
                autoSnappingConfiguration = AutoSnappingConfiguration(
                    enabled = autosnappingEnabled.value
                ),
                torchEnabled = torchEnabled.value,
                zoomLevel = zoom.floatValue,
                documentScannerConfiguration = DocumentScannerConfiguration(
                    parameters = DocumentScannerParameters(
                        ignoreOrientationMismatch = true
                    )
                ),
                /*  finderConfiguration = FinderConfiguration(
                      //strokeColor = Color.Cyan,
                      verticalAlignment = Alignment.Top,
                      aspectRatio = AspectRatio(
                          21.0,
                          29.0
                      ) // Use default aspect ratio matching document size
                  ),*/
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
                arPolygonView = { dataFlow ->
                    ScanbotDocumentArOverlay(
                        dataFlow = dataFlow,
                        getProgressPolygonStyle = { defaultStyle ->
                            // Customize polygon style if needed
                            defaultStyle.copy(strokeWidth = 8f, strokeColor = Color.Green)
                        })
                },
                onTakePictureCalled = {
                    Log.d("DocumentScannerScreen1", "Take picture called")
                    cameraInProcessingState.value = true
                },
                onTakePictureCanceled = {
                    Log.d("DocumentScannerScreen1", "Take picture canceled")
                    cameraInProcessingState.value = false
                },
                onPictureSnapped = { imageRef, captureInfo ->
                    // WARNING: move all processing operation to view model with proper coroutine scope in real apps to avoid data loss during recompositions
                    scope.launch(Dispatchers.Default) {
                        // See https://docs.scanbot.io/android/data-capture-modules/detailed-setup-guide/result-api/ for details of result handling
                        // run detection and cropping on the captured image
                        documentScanner?.run(imageRef)?.onSuccess { documentData ->
                            val croppedImage =
                                imageProcessor.crop(imageRef, documentData.pointsNormalized)
                                    .getOrReturn() // get the result of cropping operation or leave onSuccess if cropping failed
                            imageRef.close() // clear image ref resources
                            scannedImage.value =
                                imageProcessor.resize(croppedImage, 300).getOrReturn().toBitmap()
                                    .getOrReturn() // get the result of cropping operation or leave onSuccess if cropping failed
                            croppedImage.close() // clear image ref resources
                        }?.onFailure { error ->
                            Log.e(
                                "DocumentScannerScreen",
                                "Document scanning error: ${error.message}"
                            )
                        }
                        delay(1000)
                        cameraInProcessingState.value =
                            false // Picture is received, allow auto-snapping again or proceed further and allow image snap after some additional processing
                    }
                },
                onTakePictureControllerCreated = {
                    takePictureActionController.value = it
                },
                onAutoSnapping = {
                    // return true if auto-snapping should be consumed and not proceed to take picture
                    cameraInProcessingState.value // Disable auto-snapping while awaiting picture result after snap is triggered
                },
                onDocumentScanningResult = { result ->
                    // Update document scanning status to show feedback in the UI if needed
                    documentScanningStatus.value =
                        result.getOrNull()?.status ?: DocumentDetectionStatus.ERROR_NOTHING_DETECTED
                    result.onSuccess { data ->
                        Log.d(
                            "BarcodeComposeClassic",
                            "Scanned polygon: ${
                                data.pointsNormalized.map {
                                    with(density) {
                                        "(${it.x.toDp().value.toInt()}, ${it.y.toDp().value.toInt()})"
                                    }
                                }
                            }",
                        )
                    }

                },
            )
            ScanbotSnapButton(
                modifier = Modifier
                    .height(100.dp)
                    .align(Alignment.BottomCenter),
                // Disable button when scanning or auto-snapping is disabled
                clickable = scanningEnabled.value && !cameraInProcessingState.value,
                // Show indicator when camera is processing the last taken picture
                autoCapture = autosnappingEnabled.value,
                // animate progress when camera is processing the last taken picture
                animateProgress = cameraInProcessingState.value,
                // rotation speed of the outer big arc in auto-capture mode
                bigArcSpeed = 3000,
                // speed of the progress arc during processing
                progressSpeed = 500,
                // color of buttons inner component
                innerColor = Color.Red,
                // outer color of buttons outer component
                outerColor = Color.White,
                // outer circle line width
                lineWidth = 1.dp,
                // size of the empty space between inner and outer components
                emptyLineWidth = 10.dp,
                // initial angle of the 360 degrees rotating big arc
                bigArcInitialAngle = 200f
            ) {
                takePictureActionController.value?.invoke()
            }

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
            ) {
                Surface(color = Color.Green.copy(alpha = 0.3f)) {
                    Text(
                        text = instructionString(documentScanningStatus.value),
                        color = Color.White
                    )
                }
            }

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
        Column() {
            Row {
                androidx.compose.material.Button(modifier = Modifier.weight(1f), onClick = {
                    zoom.floatValue = 1.0f + Random.nextFloat()
                }) {
                    Text("Zoom")
                }

                Button(modifier = Modifier.weight(1f), onClick = {
                    torchEnabled.value = !torchEnabled.value
                }) {
                    Text("Flash")
                }

                Button(modifier = Modifier.weight(1f), onClick = {
                    cameraEnabled.value = !cameraEnabled.value
                }) {
                    Text("Visibility")
                }
            }
            Row {
                androidx.compose.material.Button(modifier = Modifier.weight(1f), onClick = {
                    autosnappingEnabled.value = !autosnappingEnabled.value
                }) {
                    Text("Autosnapping: ${autosnappingEnabled.value}")
                }
            }
        }
    }
}

@Composable
private fun instructionString(status: DocumentDetectionStatus): String = when (status) {
    DocumentDetectionStatus.NOT_ACQUIRED -> "Point the camera at a document"
    DocumentDetectionStatus.OK -> "Hold still..."
    DocumentDetectionStatus.OK_BUT_TOO_SMALL -> "Please move closer"
    DocumentDetectionStatus.OK_BUT_BAD_ANGLES -> "Please align document with the preview edges"
    DocumentDetectionStatus.OK_BUT_BAD_ASPECT_RATIO -> "Document aspect ratio mismatch"
    DocumentDetectionStatus.OK_BUT_ORIENTATION_MISMATCH -> "Please rotate the device"
    DocumentDetectionStatus.OK_BUT_OFF_CENTER -> "Please center the document in the camera preview"
    DocumentDetectionStatus.OK_BUT_TOO_DARK -> " Please turn on more light"
    DocumentDetectionStatus.ERROR_NOTHING_DETECTED -> "Document not detected"
    DocumentDetectionStatus.ERROR_PARTIALLY_VISIBLE -> "Please fit the document fully in the preview"
    DocumentDetectionStatus.ERROR_PARTIALLY_VISIBLE_TOO_CLOSE -> "Please move the device away from the document"
    DocumentDetectionStatus.ERROR_TOO_DARK -> "Please turn on more light"
    DocumentDetectionStatus.ERROR_TOO_NOISY -> "Image is too noisy"
}
