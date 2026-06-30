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

// @Tag("Simple MRZ Scanner Composable")
@Composable
fun MrzScannerSimpleSnippet() {
    MrzScannerCustomUI(
        // Modify Size here:
        modifier = Modifier
            .fillMaxSize(),
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
            }
        }
    )
}
// @EndTag("Simple MRZ Scanner Composable")
