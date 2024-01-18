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
import io.scanbot.sdk.ui_v2.barcode.configuration.CollapsedVisibleHeight
import io.scanbot.sdk.ui_v2.barcode.configuration.MultipleBarcodesScanningMode
import io.scanbot.sdk.ui_v2.barcode.configuration.MultipleScanningMode
import io.scanbot.sdk.ui_v2.barcode.configuration.SheetMode
import io.scanbot.sdk.ui_v2.common.ScanbotColor

fun multipleScanningUseCaseSnippet() {
    // Create the default configuration object.
    val config = BarcodeScannerConfiguration().apply {
        // Configure parameters (use explicit `this.` receiver for better code completion):

        // Initialize the use case for multiple scanning.
        this.useCase = MultipleScanningMode().apply {
            // Set the counting mode.
            this.mode = MultipleBarcodesScanningMode.COUNTING

            // Set the sheet mode for the barcodes preview.
            this.sheet.mode = SheetMode.COLLAPSED_SHEET

            // Set the height for the collapsed sheet.
            this.sheet.collapsedVisibleHeight = CollapsedVisibleHeight.LARGE

            // Enable manual count change.
            this.manualCountChangeEnabled = true

            // Set the delay before same barcode counting repeat.
            this.countingRepeatDelay = 1000

            // Configure the submit button.
            this.sheetContent.submitButton.text = "Submit"
            this.sheetContent.submitButton.foreground.color = ScanbotColor("#000000")

            // Configure other parameters, pertaining to multiple-scanning mode as needed.
        }

        // Set an array of accepted barcode types.
        this.recognizerConfiguration.barcodeFormats = BarcodeFormat.COMMON_CODES

        // Configure other parameters as needed.
    }
}
