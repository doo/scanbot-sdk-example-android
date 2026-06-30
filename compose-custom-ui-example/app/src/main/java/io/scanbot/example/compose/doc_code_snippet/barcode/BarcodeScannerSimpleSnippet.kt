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

// @Tag("Simple Barcode Scanner Composable")
@Composable
fun BarcodeScannerSimpleSnippet() {
    BarcodeScannerCustomUI(
        // Modify Size here:
        modifier = Modifier.fillMaxSize(),
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
            }
        }
    )
}
// @EndTag("Simple Barcode Scanner Composable")
