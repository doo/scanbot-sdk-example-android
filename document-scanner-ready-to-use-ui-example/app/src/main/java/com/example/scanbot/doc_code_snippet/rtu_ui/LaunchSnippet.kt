package com.example.scanbot.doc_code_snippet.rtu_ui


import android.app.Activity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.sdk.ui_v2.document.DocumentScannerActivity
import io.scanbot.sdk.ui_v2.document.configuration.DocumentScanningFlow


class LaunchSnippet : AppCompatActivity() {

    private val context = this
    private val documentScannerResult: ActivityResultLauncher<DocumentScanningFlow> by lazy {
        registerForActivityResult(DocumentScannerActivity.ResultContract(context)) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.result?.let { document ->
                    // Handle the document.
                }
            } else {
                // Indicates that the cancel button was tapped.
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startScanning()
    }

    fun startScanning() {
        // Create the default configuration object.
        val configuration = DocumentScanningFlow()

        // Start the recognizer activity.
        documentScannerResult.launch(configuration)
    }
}

