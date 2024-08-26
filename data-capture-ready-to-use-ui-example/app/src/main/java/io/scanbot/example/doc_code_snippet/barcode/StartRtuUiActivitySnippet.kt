package io.scanbot.example.doc_code_snippet.barcode

/*
    NOTE: this snippet of code is to be used only as a part of the website documentation.
    This code is not intended for any use outside of the support of documentation by Scanbot SDK GmbH employees.
*/

// NOTE for maintainers: whenever changing this code,
// ensure that links using it are still pointing to valid lines!
// Pay attention to imports adding/removal/sorting!
// Page URLs using this code:
// TODO: add URLs here

import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import io.scanbot.example.R
import io.scanbot.sdk.ui_v2.barcode.BarcodeScannerActivity
import io.scanbot.sdk.ui_v2.barcode.configuration.BarcodeScannerConfiguration
import io.scanbot.sdk.ui_v2.common.activity.registerForActivityResultOk

class StartRtuUiActivitySnippetActivity : AppCompatActivity() {

    private val barcodeResultLauncher: ActivityResultLauncher<BarcodeScannerConfiguration> =
        registerForActivityResultOk(BarcodeScannerActivity.ResultContract()) { resultEntity ->

            // TODO: present barcode result as needed
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.doc_snippet_activity_rtu_barcode_scanner_start)

        val config = BarcodeScannerConfiguration().apply {
            // TODO: configure as needed
        }

        findViewById<AppCompatButton>(R.id.start_barcode_rtu_button).setOnClickListener {
            barcodeResultLauncher.launch(config)
        }
    }
}
