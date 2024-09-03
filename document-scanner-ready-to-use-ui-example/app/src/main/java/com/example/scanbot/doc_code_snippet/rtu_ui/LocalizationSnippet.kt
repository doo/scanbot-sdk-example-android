package com.example.scanbot.doc_code_snippet.rtu_ui


import android.app.Activity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.sdk.ui_v2.document.DocumentScannerActivity
import io.scanbot.sdk.ui_v2.document.configuration.DocumentScanningFlow


private class LocalizationSnippet : AppCompatActivity() {
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

            // Configure the strings.
            localization.cameraTopBarTitle = "document.camera.title"
            localization.reviewScreenSubmitButtonTitle = "review.submit.title"
            localization.cameraUserGuidanceNoDocumentFound = "camera.userGuidance.noDocumentFound"
            localization.cameraUserGuidanceTooDark = "camera.userGuidance.tooDark"

        }

        // Start the recognizer activity.
        documentScannerResult.launch(configuration)
    }
}

