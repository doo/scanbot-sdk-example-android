package io.scanbot.example

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.WindowCompat
import io.scanbot.example.model.BarcodeResultBundle
import io.scanbot.example.repository.BarcodeResultRepository
import io.scanbot.sdk.ui_v2.barcode.BarcodeScannerView
import io.scanbot.sdk.ui_v2.barcode.common.mappers.getName
import io.scanbot.sdk.ui_v2.barcode.configuration.BarcodeItem
import io.scanbot.sdk.ui_v2.barcode.configuration.BarcodeItemInfoPosition
import io.scanbot.sdk.ui_v2.barcode.configuration.BarcodeItemMapper
import io.scanbot.sdk.ui_v2.barcode.configuration.BarcodeMappedData
import io.scanbot.sdk.ui_v2.barcode.configuration.BarcodeMappingResult
import io.scanbot.sdk.ui_v2.barcode.configuration.BarcodeScannerConfiguration
import io.scanbot.sdk.ui_v2.barcode.configuration.CollapsedVisibleHeight
import io.scanbot.sdk.ui_v2.barcode.configuration.MultipleBarcodesScanningMode
import io.scanbot.sdk.ui_v2.barcode.configuration.MultipleScanningMode
import io.scanbot.sdk.ui_v2.common.CameraPreviewMode
import io.scanbot.sdk.ui_v2.common.FinderCorneredStyle
import io.scanbot.sdk.ui_v2.common.ScanbotColor
import io.scanbot.sdk.ui_v2.common.StatusBarMode
import io.scanbot.sdk.ui_v2.common.TopBarMode
import io.scanbot.sdk.ui_v2.common.activity.AutoCancelTimeout
import io.scanbot.sdk.ui_v2.common.activity.CanceledByUser
import io.scanbot.sdk.ui_v2.common.activity.LicenseInvalid
import io.scanbot.sdk.ui_v2.common.theme.LocalScanbotTheme
import io.scanbot.sdk.ui_v2.common.theme.ScanbotTheme


class ARTUBarcodeScannerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artu_barcode_scanner)

        val cameraContainerView: ComposeView = findViewById(R.id.camera_container)
        cameraContainerView.apply {
            setContent {
                val configuration = remember {
                    BarcodeScannerConfiguration().apply {
                        // modify config here
                    }
                }

                val statusBarHidden = configuration?.topBar?.statusBarMode == StatusBarMode.HIDDEN
                LaunchedEffect(key1 = true, block = {
                    if (statusBarHidden) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                        }

                        WindowCompat.setDecorFitsSystemWindows(window, false)
                    }
                })

                CompositionLocalProvider(LocalScanbotTheme provides ScanbotTheme(configuration.palette)) {
                    Box {
                        BarcodeScannerView(
                            configuration = configuration,
                            onBarcodeScanned = {
                                BarcodeResultRepository.barcodeResultBundle = BarcodeResultBundle(it)
                                val intent = Intent(context, BarcodeResultActivity::class.java)
                                startActivity(intent)
                                finish()
                            },
                            onBarcodeScannerClosed = {
                                when (it) {
                                    LicenseInvalid -> Toast.makeText(
                                        context,
                                        "License has expired!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    AutoCancelTimeout -> null // just close screen
                                    CanceledByUser -> null // just close screen
                                }
                                finish()
                            })
                    }
                }
            }
        }
    }

}
