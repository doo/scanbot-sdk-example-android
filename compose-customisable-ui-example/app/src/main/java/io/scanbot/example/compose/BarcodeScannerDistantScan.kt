package io.scanbot.example.compose

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import io.scanbot.common.*
import io.scanbot.demo.composeui.ui.theme.sbBrandColor
import io.scanbot.example.compose.components.*
import io.scanbot.sdk.geometry.*
import io.scanbot.sdk.ui_v2.barcode.*
import io.scanbot.sdk.ui_v2.common.*
import io.scanbot.sdk.ui_v2.common.components.*
import kotlin.random.*

@OptIn(ExperimentalCamera2Interop::class)
@Composable
fun BarcodeScannerDistantScan(navController: NavHostController) {
    // Use these states to control camera, torch and zoom

    // THIS IS IMPORTANT FOR DISTANT SCAN USECASE
    val zoom = remember { mutableFloatStateOf(20.0f) }
    val torchEnabled = remember { mutableStateOf(false) }
    val cameraEnabled = remember { mutableStateOf(true) }

    // Unused in this example, but you may use it to
    // enable/disable barcode scanning dynamically
    val barcodeScanningEnabled = remember { mutableStateOf(true) }

    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Distant Barcode Scan",
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
                //@Tag("Scanning distant barcodes")
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
                        CustomBarcodesArView(
                            barcodesFlow = barcodesFlow,
                            onBarcodeClick = {
                                // Handle barcode click on barcode from AR overlay if needed
                            }
                        )
                    },
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
                                    navOptions = NavOptions.Builder().setLaunchSingleTop(true)
                                        .build()
                                )
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
                // @EndTag("Scanning distant barcodes")
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
    )
}