package io.scanbot.example.compose.doc_code_snippet.mrz

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
import io.scanbot.sdk.geometry.*
import io.scanbot.sdk.ui_v2.common.*
import io.scanbot.sdk.ui_v2.common.components.*
import io.scanbot.sdk.ui_v2.mrz.*

// @Tag("Detailed MRZ Scanner Composable")
@Composable
fun MrzScannerSnippet() {
    // Use these states to control camera, torch and zoom
    val zoom = remember { mutableFloatStateOf(1.0f) }
    val torchEnabled = remember { mutableStateOf(false) }
    val cameraEnabled = remember { mutableStateOf(true) }
    // Unused in this example, but you may use it to
    // enable/disable barcode scanning dynamically
    val scanningEnabled = remember { mutableStateOf(true) }
    MrzScannerCustomUI(
        // Modify Size here:
        modifier = Modifier
            .fillMaxSize(),
        // See more details about FinderConfiguration in the FinderConfigurationSnippet.kt
        finderConfiguration = FinderConfiguration(
            verticalAlignment = Alignment.CenterVertically,
            // Modify aspect ratio of the viewfinder here:
            aspectRatio = AspectRatio(adjustedMrzThreeLinedFinderAspectRatio, 1.0),
        ),
        cameraEnabled = cameraEnabled.value,
        // Select front or back camera here:
        cameraModule = CameraModule.BACK,
        // Set camera preview mode here. Possible values: FIT_IN, FILL_IN
        cameraPreviewMode = CameraPreviewMode.FILL_IN,
        // Enable or disable MRZ scanning here:
        mrzScanningEnabled = scanningEnabled.value,
        // Enable or disable torch here:
        torchEnabled = torchEnabled.value,
        // Set zoom level here:
        zoomLevel = zoom.floatValue,
        // Permission view that will be shown if camera permission is not granted
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
        onMrzScanningResult = { result ->
            // See https://docs.scanbot.io/android/data-capture-modules/detailed-setup-guide/result-api/ for details of result handling
            result.onSuccess { data ->
                // Handle scanned barcodes here (for example, show a dialog or navigate to another screen)
                Log.d(
                    "MrzScannerScreen", "Scanned mrz: ${data.rawMRZ}"
                )
            }.onFailure { error ->
                when (error) {
                    is Result.InvalidLicenseError -> {
                        Log.e("MrzScannerScreen", "MRZ scanning license error: ${error.message}")
                    }

                    else -> {
                        // Handle error here
                        Log.e("MrzScannerScreen", "MRZ scanning error: ${error.message}")
                    }
                }
            }
        }
    )
}
// @EndTag("Detailed MRZ Scanner Composable")
