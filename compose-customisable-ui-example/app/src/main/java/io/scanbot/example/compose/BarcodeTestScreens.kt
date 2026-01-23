package io.scanbot.example.compose

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import io.scanbot.common.onFailure
import io.scanbot.common.onSuccess
import io.scanbot.example.compose.components.CorneredFinder
import io.scanbot.sdk.barcode.BarcodeItem
import io.scanbot.sdk.barcode.BarcodeScannerResult
import io.scanbot.sdk.geometry.AspectRatio
import io.scanbot.sdk.ui_v2.barcode.BarcodeScannerCustomUI
import io.scanbot.sdk.ui_v2.common.CameraPermissionScreen
import io.scanbot.sdk.ui_v2.common.CameraPreviewMode
import io.scanbot.sdk.ui_v2.common.components.FinderConfiguration
import io.scanbot.sdk.ui_v2.common.components.ScanbotCameraPermissionView
import io.scanbot.sdk.util.snap.SoundControllerImpl
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalCamera2Interop::class)
@Composable
fun BarcodeScannerScreen1(navController: NavHostController) {

    Column(modifier = Modifier.systemBarsPadding()) {
        // Use these states to control camera, torch and zoom
        val zoom = remember { mutableFloatStateOf(1.0f) }
        val torchEnabled = remember { mutableStateOf(false) }
        val cameraEnabled = remember { mutableStateOf(true) }

        // Unused in this example, but you may use it to
        // enable/disable barcode scanning dynamically
        val barcodeScanningEnabled = remember { mutableStateOf(true) }

        BarcodeScannerCustomUI(
            // Modify Size here:
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f),
            finderConfiguration = FinderConfiguration(
                verticalAlignment = Alignment.Top,
                previewInsets = PaddingValues(
                    top = 32.dp,
                    bottom = 32.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                // Modify aspect ratio of the viewfinder here:
                aspectRatio = AspectRatio(1.0, 1.0),
                // Change viewfinder overlay color here:
                overlayColor = Color.Transparent,
                // Change viewfinder stroke color here:
                strokeColor = Color.Transparent,

                // Alternatively, it is possible to provide a completely custom viewfinder content:
                finderContent = {
                    // Custom cornered viewfinder. Can be replaced with any custom Composable
                    CorneredFinder()
                },
                topContent = {
                    androidx.compose.material.Text(
                        "Custom Top Content",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                },
                bottomContent = {
                    // You may add custom buttons and other elements here:
                    androidx.compose.material.Text(
                        "Custom Bottom Content",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            ),
            cameraEnabled = cameraEnabled.value,
            barcodeScanningEnabled = barcodeScanningEnabled.value,
            torchEnabled = torchEnabled.value,
            zoomLevel = zoom.floatValue,
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
            arPolygonView = { barcodesFlow ->
                // Configure AR overlay polygon appearance inside CustomBarcodesArView if needed
                CustomBarcodesArView(
                    barcodesFlow = barcodesFlow,
                    onBarcodeClick = {
                        // Handle barcode click on barcode from AR overlay if needed
                    }, density = LocalDensity.current
                )
            },
            onBarcodeScanningResult = { result ->
                result.onSuccess { data ->
                    // Apply feedback, sound, vibration here if needed
                    // ...

                    // Handle scanned barcodes here (for example, show a dialog)
                    Log.d(
                        "BarcodeComposeClassic", "Scanned barcodes: ${data.barcodes}"
                    )
                }

            },
        )
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

    }
}

@OptIn(ExperimentalCamera2Interop::class)
@Composable
fun BarcodeScannerScreen2(navController: NavHostController) {
    val zoom = remember { mutableFloatStateOf(1.0f) }
    val torchEnabled = remember { mutableStateOf(false) }
    val cameraEnabled = remember { mutableStateOf(true) }
    val barcodeScanningEnabled = remember { mutableStateOf(true) }
    val scannedBarcodes = remember { mutableStateListOf<BarcodeItem>() }
    val context = LocalContext.current
    val soundController = remember {
        SoundControllerImpl(context).apply {
            // Prepare Scanbot beep sound controller:
            setUp()
            setVibrationEnabled(true)
            setBleepEnabled(true)
        }
    }
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        BarcodeScannerCustomUI(
            modifier = Modifier.weight(1f),
            cameraEnabled = cameraEnabled.value,
            barcodeScanningEnabled = barcodeScanningEnabled.value,
            torchEnabled = torchEnabled.value,
            zoomLevel = zoom.floatValue,
            finderConfiguration = FinderConfiguration(
                aspectRatio = AspectRatio(2.0, 1.0),
                overlayColor = Color(0x5500FF00),
                strokeColor = Color.Transparent,
                finderContent = {
                    CorneredFinder()
                }
            ),
            permissionView = {},
            onBarcodeScanningResult = { result ->
                result.onSuccess { result ->
                    result.barcodes.forEach { data ->
                        if (scannedBarcodes.none { it.text == data.text && it.format == data.format }) {
                            scope.launch {
                                // Provide sound and vibration feedback on scan:
                                soundController.playBleepSound()
                            }
                            scannedBarcodes.add(data)
                        }
                    }
                }.onFailure { error ->
                    Log.e(
                        "BarcodeScannerScreen2",
                        "Barcode scanning error: ${error.message}",
                        error
                    )
                }
            }
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp),
        ) {
            items(scannedBarcodes.reversed()) { barcode ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text("Data: ${barcode.text}", style = MaterialTheme.typography.bodyLarge)
                    Text("Format: ${barcode.format}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@OptIn(ExperimentalCamera2Interop::class)
@Composable
fun BarcodeScannerScreen3(navController: NavHostController) {
    Column(modifier = Modifier.systemBarsPadding()) {
        val zoom = remember { mutableFloatStateOf(1.0f) }
        val torchEnabled = remember { mutableStateOf(false) }
        val cameraEnabled = remember { mutableStateOf(true) }
        val barcodeScanningEnabled = remember { mutableStateOf(true) }
        val touchToFocusEnabled = remember { mutableStateOf(true) }
        val previewMode = remember { mutableStateOf(CameraPreviewMode.FIT_IN) }
        BarcodeScannerCustomUI(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            cameraEnabled = cameraEnabled.value,
            cameraPreviewMode = previewMode.value,
            barcodeScanningEnabled = barcodeScanningEnabled.value,
            touchToFocusEnabled = touchToFocusEnabled.value,
            torchEnabled = torchEnabled.value,
            zoomLevel = zoom.floatValue,
            finderConfiguration = FinderConfiguration(
                aspectRatio = AspectRatio(2.0, 1.0),
                overlayColor = Color(0x5500FF00),
                strokeColor = Color.Blue
            ),
            permissionView = {},
            onBarcodeScanningResult = { result ->
                result.onSuccess { data ->
                    // Navigate to detail screen for the first barcode
                    val firstBarcode = data.barcodes.firstOrNull()
                    if (firstBarcode != null) {
                        navController.navigate(
                            Screen.BarcodeDetail.createRoute(
                                firstBarcode.text,
                                firstBarcode.format.name
                            ),
                            navOptions = NavOptions.Builder().setLaunchSingleTop(true).build()
                        )
                    }
                }.onFailure { error ->
                    Log.e(
                        "BarcodeScannerScreen3",
                        "Barcode scanning error: ${error.message}",
                        error
                    )
                }
            }

        )
        Column(modifier = Modifier.fillMaxWidth()) {
            Button(onClick = {
                when (previewMode.value) {
                    CameraPreviewMode.FIT_IN -> previewMode.value = CameraPreviewMode.FILL_IN
                    CameraPreviewMode.FILL_IN -> previewMode.value = CameraPreviewMode.FIT_IN
                }
            }) {
                Text("Preview: (${previewMode.value.name})")
            }
            Button(modifier = Modifier, onClick = {
                barcodeScanningEnabled.value = !barcodeScanningEnabled.value
            }) {
                Text("scanningEnabled: ${barcodeScanningEnabled.value}")
            }
            Button(modifier = Modifier, onClick = {
                touchToFocusEnabled.value = !touchToFocusEnabled.value
            }) {
                Text("touchToFocus: ${touchToFocusEnabled.value}")
            }
        }
    }
}
