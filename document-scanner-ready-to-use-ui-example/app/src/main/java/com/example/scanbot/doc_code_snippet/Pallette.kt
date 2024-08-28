package com.example.scanbot.doc_code_snippet


import android.app.Activity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.sdk.ui.registerForActivityResultOk
import io.scanbot.sdk.ui_v2.common.ScanbotColor
import io.scanbot.sdk.ui_v2.document.DocumentScannerActivity
import io.scanbot.sdk.ui_v2.document.configuration.DocumentScanningFlow


private class PalletteActivity : AppCompatActivity() {
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

            // Configure the colors.
            // The palette already has the default colors set, so you don't have to always set all the colors.
            palette.sbColorPrimary = ScanbotColor(value = "#C8193C")
            palette.sbColorPrimaryDisabled = ScanbotColor(value = "#F5F5F5")
            palette.sbColorNegative = ScanbotColor(value = "#FF3737")
            palette.sbColorPositive = ScanbotColor(value = "#4EFFB4")
            palette.sbColorWarning = ScanbotColor(value = "#FFCE5C")
            palette.sbColorSecondary = ScanbotColor(value = "#FFEDEE")
            palette.sbColorSecondaryDisabled = ScanbotColor(value = "#F5F5F5")
            palette.sbColorOnPrimary = ScanbotColor(value = "#FFFFFF")
            palette.sbColorOnSecondary = ScanbotColor(value = "#C8193C")
            palette.sbColorSurface = ScanbotColor(value = "#FFFFFF")
            palette.sbColorOutline = ScanbotColor(value = "#EFEFEF")
            palette.sbColorOnSurfaceVariant = ScanbotColor(value = "#707070")
            palette.sbColorOnSurface = ScanbotColor(value = "#000000")
            palette.sbColorSurfaceLow = ScanbotColor(value = "#26000000")
            palette.sbColorSurfaceHigh = ScanbotColor(value = "#7A000000")
            palette.sbColorModalOverlay = ScanbotColor(value = "#A3000000")
        }

        // Start the recognizer activity.
        documentScannerResult.launch(configuration)
    }
}

