package io.scanbot.example.compose

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import io.scanbot.common.*
import io.scanbot.demo.composeui.ui.theme.*
import io.scanbot.example.compose.components.BarcodeItem
import io.scanbot.sdk.barcode.*
import io.scanbot.sdk.ui_v2.barcode.*
import io.scanbot.sdk.util.snap.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCamera2Interop::class)
@Composable
fun BarcodeScannerMultiScan(navController: NavHostController) {
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
    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Multiple Barcodes Scan",
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
                // @Tag("Scanning multiple barcodes")
                BarcodeScannerCustomUI(
                    modifier = Modifier.weight(1f),
                    cameraEnabled = cameraEnabled.value,
                    barcodeScanningEnabled = barcodeScanningEnabled.value,
                    torchEnabled = torchEnabled.value,
                    zoomLevel = zoom.floatValue,
                    finderConfiguration = null,
                    arPolygonView = { dataFlow ->
                        CustomBarcodesArView(dataFlow, { barcode ->
                            if (scannedBarcodes.none { it.textWithExtension == barcode.textWithExtension }) {
                                scannedBarcodes.add(barcode)
                            } else {
                                scannedBarcodes.removeAll { it.textWithExtension == barcode.textWithExtension }
                            }
                        })
                    },
                    permissionView = {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Text(
                                "Camera permission is required to scan barcodes.",
                                modifier = Modifier.padding(16.dp),
                                color = Color.White
                            )
                        }
                    },
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
                // @EndTag("Scanning multiple barcodes")
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(8.dp),
                ) {
                    items(scannedBarcodes.reversed()) { barcode ->
                        BarcodeItem(barcode)
                    }
                }
            }
        })
}
