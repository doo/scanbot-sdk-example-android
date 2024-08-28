package com.example.scanbot.doc_code_snippet


import android.app.Activity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.sdk.imagefilters.ColorDocumentFilter
import io.scanbot.sdk.imagefilters.ParametricFilter
import io.scanbot.sdk.imagefilters.WhiteBlackPointFilter
import io.scanbot.sdk.ui_v2.document.DocumentScannerActivity
import io.scanbot.sdk.ui_v2.document.configuration.DocumentScanningFlow


class AutomaticFilteringActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //run this function on button click
        startScanning()
    }

    private val context = this
    private val documentScannerResult: ActivityResultLauncher<DocumentScanningFlow> =
        registerForActivityResult(DocumentScannerActivity.ResultContract(context)) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.result?.let { document ->
                    // Handle the document.
                }
            } else {
                // Indicates that the cancel button was tapped.
            }
        }


    fun startScanning() {
        // Create the default configuration object.
        val configuration = DocumentScanningFlow().apply {
            // Set default filter with default values for the document scanner.
            outputSettings.defaultFilter = ParametricFilter.scanbotBinarizationFilter()
            //or you can set custom filter with custom values
            outputSettings.defaultFilter = WhiteBlackPointFilter(blackPoint = 0.1, whitePoint = 0.9)

        }

        // Start the recognizer activity.
        documentScannerResult.launch(configuration)
    }
}

