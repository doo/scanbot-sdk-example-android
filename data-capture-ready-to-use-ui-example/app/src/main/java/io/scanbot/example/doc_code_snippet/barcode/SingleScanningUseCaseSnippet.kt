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

import io.scanbot.sdk.ui_v2.barcode.common.mappers.COMMON_CODES
import io.scanbot.sdk.ui_v2.barcode.configuration.BarcodeFormat
import io.scanbot.sdk.ui_v2.barcode.configuration.BarcodeScannerConfiguration
import io.scanbot.sdk.ui_v2.barcode.configuration.SingleScanningMode
import io.scanbot.sdk.ui_v2.common.ScanbotColor

fun singleScanningUseCaseSnippet() {
    // Create the default configuration object.
    val config = BarcodeScannerConfiguration().apply {
        // Configure parameters (use explicit `this.` receiver for better code completion):

        // Initialize the use case for single scanning.
        this.useCase = SingleScanningMode().apply {
            // Enable and configure the confirmation sheet.
            this.confirmationSheetEnabled = true
            this.sheetColor = ScanbotColor("#FFFFFF")

            // Hide/unhide the barcode image.
            this.barcodeImageVisible = true

            // Configure the barcode title of the confirmation sheet.
            this.barcodeTitle.visible = true
            this.barcodeTitle.color = ScanbotColor("#000000")

            // Configure the barcode subtitle of the confirmation sheet.
            this.barcodeSubtitle.visible = true
            this.barcodeSubtitle.color = ScanbotColor("#000000")

            // Configure the cancel button of the confirmation sheet.
            this.cancelButton.text = "Close"
            this.cancelButton.foreground.color = ScanbotColor("#C8193C")
            this.cancelButton.background.fillColor = ScanbotColor("#00000000")

            // Configure the submit button of the confirmation sheet.
            this.submitButton.text = "Submit"
            this.submitButton.foreground.color = ScanbotColor("#FFFFFF")
            this.submitButton.background.fillColor = ScanbotColor("#C8193C")

            // Configure other parameters, pertaining to single-scanning mode as needed.
        }

        // Set an array of accepted barcode types.
        this.recognizerConfiguration.barcodeFormats = BarcodeFormat.COMMON_CODES

        // Configure other parameters as needed.
    }
}
