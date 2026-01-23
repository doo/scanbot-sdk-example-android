package io.scanbot.example.compose

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import io.scanbot.common.onFailure
import io.scanbot.common.onSuccess
import io.scanbot.example.compose.components.CorneredFinder
import io.scanbot.sdk.barcode.BarcodeItem
import io.scanbot.sdk.geometry.AspectRatio
import io.scanbot.sdk.ui_v2.barcode.BarcodeScannerCustomUI
import io.scanbot.sdk.ui_v2.common.CameraPreviewMode
import io.scanbot.sdk.ui_v2.common.components.FinderConfiguration
import io.scanbot.sdk.util.snap.SoundControllerImpl
import kotlinx.coroutines.launch


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
