package com.example.scanbot.doc_code_snippet


import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.ui_v2.common.ScanbotColor
import io.scanbot.sdk.ui_v2.document.CroppingActivity
import io.scanbot.sdk.ui_v2.document.configuration.CroppingConfiguration


private class StandaloneCropScreenSnippet : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //run this function on button click
        startScanning()
    }

    private val context = this
    private val documentScannerResult: ActivityResultLauncher<CroppingConfiguration> =
        registerForActivityResult(CroppingActivity.ResultContract()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.result?.let { result ->
                    // Retrieve the cropped document
                    val document =
                        ScanbotSDK(this@StandaloneCropScreenSnippet).documentApi.loadDocument(
                            documentId = result.documentUuid
                        ) ?: return@let
                    val page = document.pageWithId(result.pageUuid) ?: return@let

                }
            } else {
                // Indicates that the cancel button was tapped.
            }
        }


    fun startScanning() {
        // Retrieve the scanned document
        val document =
            ScanbotSDK(this@StandaloneCropScreenSnippet)
                .documentApi.loadDocument(documentId = "SOME_SAVED_UUID")
                ?: return

        // Retrieve the selected document page.
        val page = document.pages.getOrNull(0) ?: return
        // Create the default configuration object.
        val configuration =
            CroppingConfiguration(documentUuid = document.uuid, pageUuid = page.uuid).apply {
                // e.g disable the rotation feature.
                cropping.bottomBar.rotateButton.visible = false

                // e.g. configure various colors.
                appearance.topBarBackgroundColor =
                    ScanbotColor(color = Color.RED)
                cropping.topBarConfirmButton.foreground.color =
                    ScanbotColor(color = Color.WHITE)

                // e.g. customize a UI element's text
                localization.croppingCancelButtonTitle = "Cancel"

            }

        // Start the recognizer activity.
        documentScannerResult.launch(configuration)
    }
}

