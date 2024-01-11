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

import io.scanbot.sdk.ui_v2.barcode.configuration.BarcodeScannerConfiguration
import io.scanbot.sdk.ui_v2.common.ScanbotColor

fun userGuidanceConfigSnippet() {
    // Create the default configuration object.
    BarcodeScannerConfiguration().apply {
        // Configure parameters (use explicit `this.` receiver for better code completion):

        // Hide/unhide the user guidance.
        this.userGuidance.visible = true

        // Configure the title.
        this.userGuidance.title.text = "Move the finder over a barcode"
        this.userGuidance.title.color = ScanbotColor("#FFFFFF")

        // Configure the background.
        this.userGuidance.background.fillColor = ScanbotColor("#7A000000")

        // Configure other parameters as needed.
    }
}
