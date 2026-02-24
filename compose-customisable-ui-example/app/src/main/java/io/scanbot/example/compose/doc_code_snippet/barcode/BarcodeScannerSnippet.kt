package io.scanbot.example.compose.doc_code_snippet.barcode

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.scanbot.common.*
import io.scanbot.example.compose.CustomBarcodesArView
import io.scanbot.sdk.barcode.textWithExtension
import io.scanbot.sdk.geometry.AspectRatio
import io.scanbot.sdk.ui_v2.barcode.BarcodeScannerCustomUI
import io.scanbot.sdk.ui_v2.common.CameraModule
import io.scanbot.sdk.ui_v2.common.CameraPermissionScreen
import io.scanbot.sdk.ui_v2.common.CameraPreviewMode
import io.scanbot.sdk.ui_v2.common.components.FinderConfiguration
import io.scanbot.sdk.ui_v2.common.components.ScanbotCameraPermissionView

// @Tag("Detailed Barcode Scanner Composable")
@Composable
fun BarcodeScannerSnippet() {
    // @Tag("Mutable states for camera control")
    // Use these states to control camera, torch and zoom
    val zoom = remember { mutableFloatStateOf(1.0f) }
    val torchEnabled = remember { mutableStateOf(false) }
    val cameraEnabled = remember { mutableStateOf(true) }
    // Unused in this example, but you may use it to
    // enable/disable barcode scanning dynamically
    val scanningEnabled = remember { mutableStateOf(true) }
    // @EndTag("Mutable states for camera control")
    BarcodeScannerCustomUI(
        // Modify Size here:
        modifier = Modifier
            .fillMaxSize(),
        // See more details about FinderConfiguration in the FinderConfigurationSnippet.kt
        finderConfiguration = FinderConfiguration(
            verticalAlignment = Alignment.Top,
            // Modify aspect ratio of the viewfinder here:
            aspectRatio = AspectRatio(1.0, 1.0),
        ),
        // Enable or disable camera view here:
        cameraEnabled = cameraEnabled.value,
        // Select front or back camera here:
        cameraModule = CameraModule.BACK,
        // Set camera preview mode here. Possible values: FIT_IN, FILL_IN
        cameraPreviewMode = CameraPreviewMode.FILL_IN,
        // Enable or disable Barcode scanning here:
        barcodeScanningEnabled = scanningEnabled.value,
        // Enable or disable torch here:
        torchEnabled = torchEnabled.value,
        // Set zoom level. Range from 1.0 to 5.0:
        zoomLevel = zoom.floatValue,
        // Permission view that will be shown if camera permission is not granted
        arPolygonView = { dataFlow->
            CustomBarcodesArView(dataFlow,{
                //handle click on barcode in AR
            })
        },
        permissionView = {
            // View that will be shown while camera permission is not granted
            // Use custom layout of camera permission handling view here:
            ScanbotCameraPermissionView(
                modifier = Modifier.fillMaxSize(),
                bottomContentPadding = 0.dp,
                permissionConfig = CameraPermissionScreen(),
                onClose = {
                    // Handle permission screen close if needed
                })
        },
        onBarcodeScanningResult = { result ->
            // See https://docs.scanbot.io/android/data-capture-modules/detailed-setup-guide/result-api/ for details of result handling
            result.onSuccess { data ->
                // Handle scanned barcodes here (for example, show a dialog or navigate to another screen)
                Log.d(
                    "BarcodeScannerScreen",
                    "Scanned Barcodes: ${data.barcodes.joinToString { it.textWithExtension }}"
                )
            }.onFailure { error ->
                when (error) {
                    is Result.InvalidLicenseError -> {
                        Log.e(
                            "BarcodeScannerScreen",
                            "Barcodes scanning license error: ${error.message}"
                        )
                    }

                    else -> {
                        // Handle error here
                        Log.e("BarcodeScannerScreen", "Barcodes scanning error: ${error.message}")
                    }
                }
            }
        }
    )
}
// @EndTag("Detailed Barcode Scanner Composable")
