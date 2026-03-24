@file:kotlin.OptIn(ExperimentalMaterial3Api::class)

package io.scanbot.example.compose

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import io.scanbot.common.onSuccess
import io.scanbot.sdk.mrz.MrzScannerResult
import io.scanbot.sdk.ui_v2.common.CameraPermissionScreen
import io.scanbot.sdk.ui_v2.common.components.ScanbotCameraPermissionView
import io.scanbot.sdk.ui_v2.mrz.MrzScannerCustomUI
import kotlin.random.Random

@OptIn(ExperimentalCamera2Interop::class, ExperimentalMaterial3Api::class)
@Composable
fun MrzScannerScreen(navController: NavHostController) {

    Column(modifier = Modifier.systemBarsPadding()) {
        // Use these states to control camera, torch and zoom
        val zoom = remember { mutableFloatStateOf(1.0f) }
        val torchEnabled = remember { mutableStateOf(false) }
        val cameraEnabled = remember { mutableStateOf(true) }

        // Unused in this example, but you may use it to
        // enable/disable barcode scanning dynamically
        val scanningEnabled = remember { mutableStateOf(true) }
        val mrzResult = remember { mutableStateOf<MrzScannerResult?>(null) }
        MrzScannerCustomUI(
            // Modify Size here:
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f),
            /*          finderConfiguration = FinderConfiguration(
                          verticalAlignment = Alignment.Top,
                          // Modify aspect ratio of the viewfinder here:
                          aspectRatio = AspectRatio(adjustedMrzThreeLinedFinderAspectRatio, 1.0),
                          // Alternatively, it is possible to provide a completely custom viewfinder content:
                          finderContent = {
                              // Box with border stroke color as an example of custom viewfinder content
                              Box(
                                  modifier = Modifier
                                      .fillMaxSize()
                                      .background(Color.Transparent)
                                      // Same but with rounded corners
                                      .border(
                                          4.dp,
                                          Color.Cyan,
                                          shape = RoundedCornerShape(
                                              16.dp
                                          )
                                      )
                              ) {

                              }
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
                      ),*/
            cameraEnabled = cameraEnabled.value,
            mrzScanningEnabled = scanningEnabled.value,
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
            onMrzScanningResult = { result ->
                result.onSuccess { data ->
                    // Apply feedback, sound, vibration here if needed
                    // ...
                    if (scanningEnabled.value && data.success) {
                        scanningEnabled.value = false
                        mrzResult.value = data
                    }
                    // Handle scanned barcodes here (for example, show a dialog)
                    Log.d(
                        "MrzScannerScreen", "Scanned mrz: ${data.rawMRZ}"
                    )
                }

            },
        )
        val value = mrzResult.value
        if (value != null && value.success) {
            BasicAlertDialog(
                modifier = Modifier,
                onDismissRequest = {
                    mrzResult.value = null
                    scanningEnabled.value = true
                }, properties = DialogProperties()
            ) {
                Surface(modifier = Modifier, shape = MaterialTheme.shapes.medium) {
                    LazyColumn(Modifier.padding(16.dp)) {
                        item {
                            Text(value?.rawMRZ ?: "")
                        }
                   /*     items(value.document?.fields?.size ?: 0) {
                            val item = value.document?.fields[it]
                            Text(item?.toJson().toString())
                        }*/
                    }
                }
            }
        }
        Row {
            Button(modifier = Modifier.weight(1f), onClick = {
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
