package io.scanbot.example

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.WindowCompat
import io.scanbot.example.compose.MrzScannerViewClassic
import io.scanbot.example.compose.MrzViewModel
import io.scanbot.example.fragments.MRZDialogFragment
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.mrz.MrzScannerResult
import io.scanbot.sdk.ui_v2.common.CameraConfiguration


class MrzScannerComposeActivity : AppCompatActivity() {
    @ExperimentalCamera2Interop
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ComposeView(this).apply {
            setContent {
                MrzScannerViewClassic(
                    modifier = Modifier.fillMaxSize().statusBarsPadding(),
                    viewModel = MrzViewModel(CameraConfiguration(), ScanbotSDK(this@MrzScannerComposeActivity)),
                    onMrz = { mrzRecognitionResult ->
                        showMrzDialog(mrzRecognitionResult)
                    },
                    onScannerClosed = {
                        finish()
                    },
                )
            }
        })

    }

    private fun showMrzDialog(mrzRecognitionResult: MrzScannerResult) {
        val dialogFragment = MRZDialogFragment.newInstance(mrzRecognitionResult.document)
        dialogFragment.show(supportFragmentManager, MRZDialogFragment.NAME)
    }

}

