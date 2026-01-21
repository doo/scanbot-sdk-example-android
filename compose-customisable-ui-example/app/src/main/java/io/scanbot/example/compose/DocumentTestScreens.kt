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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import io.scanbot.sdk.documentscanner.DocumentScannerConfiguration
import io.scanbot.sdk.documentscanner.DocumentScannerParameters
import io.scanbot.sdk.geometry.AspectRatio
import io.scanbot.sdk.imagemanipulation.ScanbotSdkImageManipulator
import io.scanbot.sdk.ui_v2.common.CameraPermissionScreen
import io.scanbot.sdk.ui_v2.common.camera.TakePictureActionController
import io.scanbot.sdk.ui_v2.common.components.FinderConfiguration
import io.scanbot.sdk.ui_v2.common.components.ScanbotCameraPermissionView
import io.scanbot.sdk.ui_v2.common.components.ScanbotSnapButton
import io.scanbot.sdk.ui_v2.document.DocumentScannerCustomUI
import io.scanbot.sdk.ui_v2.document.components.camera.ScanbotDocumentArOverlay
import io.scanbot.sdk.ui_v2.document.screen.AutoSnappingConfiguration
import kotlin.random.Random

@Composable
@OptIn(ExperimentalCamera2Interop::class)
fun DocumentScannerScreen1(navController: NavHostController) {
    val density = LocalDensity.current
    Column(modifier = Modifier.systemBarsPadding()) {
        // Use these states to control camera, torch and zoom
        val zoom = remember { mutableFloatStateOf(1.0f) }
        val torchEnabled = remember { mutableStateOf(false) }
        val cameraEnabled = remember { mutableStateOf(true) }

        // Unused in this example, but you may use it to
        // enable/disable barcode scanning dynamically
        val scanningEnabled = remember { mutableStateOf(true) }
        val autosnappingEnabled = remember { mutableStateOf(false) }
        val cameraInProcessingState = remember { mutableStateOf(false) }
        val scannedImage = remember { mutableStateOf<Bitmap?>(null) }
        val takePictureActionController = remember { mutableStateOf<TakePictureActionController?>(null) }

        Box(
            modifier = Modifier.fillMaxWidth().weight(1.0f),
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
                        ignoreOrientationMismatch = cameraEnabled.value
                    )
                ),
                finderConfiguration = FinderConfiguration(
                    //strokeColor = Color.Cyan,
                    verticalAlignment = Alignment.Top,
                    aspectRatio = AspectRatio(21.0,29.0) // Use default aspect ratio matching document size
                ),
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
                    ScanbotDocumentArOverlay(dataFlow = dataFlow, getProgressPolygonStyle = { defaultStyle ->
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
                    Log.d("DocumentScannerScreen1", "Picture snapped: $imageRef, captureInfo: $captureInfo")
                    scannedImage.value =
                        ScanbotSdkImageManipulator.create().resize(imageRef, 300).getOrNull()?.toBitmap()?.getOrNull()
                    cameraInProcessingState.value = false // Picture is received, allow auto-snapping again or proceed further and allow image snap after some additional processing
                },
                onTakePictureControllerCreated = {
                    takePictureActionController.value = it
                },
                onAutoSnappingShouldTriggered = {
                    !cameraInProcessingState.value // Disable auto-snapping while awaiting picture result after snap is triggered
                },
                onDocumentOnFrameScanned = { result ->
                    // Apply feedback, sound, vibration here if needed
                    // ...

                    // Handle scanned barcodes here (for example, show a dialog)
                    Log.d(
                        "BarcodeComposeClassic",
                        "Scanned polygon: ${
                            result.pointsNormalized.map {
                                with(density) {
                                    "(${it.x.toDp().value.toInt()}, ${it.y.toDp().value.toInt()})"
                                }
                            }
                        }",
                    )
                },
            )
            ScanbotSnapButton(
                modifier = Modifier.height(100.dp).align(Alignment.BottomCenter),
                clickable = scanningEnabled.value,
                autoCapture = autosnappingEnabled.value
            ) {
                takePictureActionController.value?.invoke()
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
