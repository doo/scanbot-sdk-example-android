package io.scanbot.example

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.WindowCompat
import io.scanbot.example.fragments.MRZDialogFragment
import io.scanbot.mrzscanner.model.MRZGenericDocument
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.ui_v2.barcode.configuration.BarcodeScannerConfiguration
import io.scanbot.sdk.ui_v2.common.CameraConfiguration
import io.scanbot.sdk.ui_v2.common.StatusBarMode

@ExperimentalCamera2Interop
class MrzScannerComposeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artu_barcode_scanner)

        val cameraContainerView: ComposeView = findViewById(R.id.compose_container)
        cameraContainerView.apply {
            setContent {
                MrzScannerViewInternal(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = MrzViewModel(CameraConfiguration(), ScanbotSDK(this@MrzScannerComposeActivity)),
                    onMrz = { mrzRecognitionResult ->

                        showMrzDialog(mrzRecognitionResult)
                    },
                    onScannerClosed = {
                        val intent = Intent(context, BarcodeResultActivity::class.java)
                        startActivity(intent)
                        finish()
                    },
                )
            }
        }
    }

    private fun showMrzDialog(mrzRecognitionResult: MRZGenericDocument) {
        val dialogFragment = MRZDialogFragment.newInstance(mrzRecognitionResult)
        dialogFragment.show(supportFragmentManager, MRZDialogFragment.NAME)
    }

}




