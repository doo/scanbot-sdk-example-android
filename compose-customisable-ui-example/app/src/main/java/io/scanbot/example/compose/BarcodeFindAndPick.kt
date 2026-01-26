package io.scanbot.example.compose

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.Icon
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import io.scanbot.common.*
import io.scanbot.demo.composeui.ui.theme.sbBrandColor
import io.scanbot.example.compose.components.*
import io.scanbot.sdk.barcode.textWithExtension
import io.scanbot.sdk.geometry.*
import io.scanbot.sdk.ui_v2.barcode.*
import io.scanbot.sdk.ui_v2.barcode.components.ar_tracking.ScanbotBarcodesArOverlay
import io.scanbot.sdk.ui_v2.common.*
import io.scanbot.sdk.ui_v2.common.components.*
import kotlin.random.*

@OptIn(ExperimentalCamera2Interop::class)
@Composable
fun BarcodeFindAndPick(navController: NavHostController) {
    val density = LocalDensity.current
    // Use these states to control camera, torch and zoom
    val zoom = remember { mutableFloatStateOf(1.0f) }
    val torchEnabled = remember { mutableStateOf(false) }
    val cameraEnabled = remember { mutableStateOf(true) }

    // Unused in this example, but you may use it to
    // enable/disable barcode scanning dynamically
    val barcodeScanningEnabled = remember { mutableStateOf(true) }
    val expectedBarcodeValue = "Scanbot" // Expected barcode value to find
    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Barcode Single Scan",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                },
                backgroundColor = sbBrandColor,
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            tint = Color.White,
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = ""
                        )
                    }
                })
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // @Tag("Find And Pick Single Barcode")
                BarcodeScannerCustomUI(
                    // Modify Size here:
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.0f),
                    cameraEnabled = cameraEnabled.value,
                    barcodeScanningEnabled = barcodeScanningEnabled.value,
                    torchEnabled = torchEnabled.value,
                    zoomLevel = zoom.floatValue,
                    permissionView = {
                        // View that will be shown while camera permission is not granted
                        Box(modifier = Modifier.fillMaxSize()) {
                            Text(
                                "Camera permission is required to scan barcodes.",
                                modifier = Modifier.padding(16.dp),
                                color = Color.White
                            )
                        }
                    },
                    arPolygonView = { barcodesFlow ->
                        // Configure AR overlay polygon appearance inside CustomBarcodesArView if needed
                        ScanbotBarcodesArOverlay(
                            barcodesFlow,
                            getData = { barcodeItem -> barcodeItem.textWithExtension },
                            getPolygonStyle = { defaultStyle, barcodeItem ->
                                // Customize polygon style here.
                                // You may use barcodeItem to apply different styles for different barcode types, etc.
                                defaultStyle.copy(
                                    drawPolygon = true,
                                    useFill = true,
                                    useFillHighlighted = true,
                                    cornerRadius = density.run { 20.dp.toPx() },
                                    cornerHighlightedRadius = density.run { 20.dp.toPx() },
                                    strokeWidth = density.run { 5.dp.toPx() },
                                    strokeHighlightedWidth = density.run { 5.dp.toPx() },
                                    strokeColor = Color.Red,
                                    strokeHighlightedColor = Color.Green,
                                    fillColor = Color.Red.copy(alpha = 0.3f),
                                    fillHighlightedColor = Color.Green.copy(alpha = 0.3f),
                                    shouldDrawShadows = false
                                )
                            },
                            shouldHighlight = { barcodeItem ->
                                // Here you can implement any custom logic.
                                barcodeItem.text == expectedBarcodeValue
                            },
                            // Customize AR view  for barcode data here if needed
                            view = { path, barcodeItem, data, shouldHighlight ->
                                // Implement custom view for barcode polygon if needed
                                // See CustomBarcodesArView.kt for details
                            },
                            onClick = {
                                // Handle barcode click on barcode from AR overlay if needed
                            },
                        )
                    },
                    onBarcodeScanningResult = { result ->
                        result.onSuccess { data ->
                            // Navigate to detail screen for the first barcode
                            val firstBarcode = data.barcodes.firstOrNull()
                           if(firstBarcode?.text == expectedBarcodeValue){
                               // handle the found barcode if needed
                           }
                        }.onFailure { error ->
                            Log.e(
                                "BarcodeScannerScreen3",
                                "Barcode scanning error: ${error.message}",
                                error
                            )
                        }
                    },
                )
                // @EndTag("Find And Pick Single Barcode")
            }
        }
    )
}