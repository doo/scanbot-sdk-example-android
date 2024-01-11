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
import io.scanbot.sdk.ui_v2.common.StatusBarMode
import io.scanbot.sdk.ui_v2.common.TopBarMode

fun topBarConfigSnippet() {
    // Create the default configuration object.
    BarcodeScannerConfiguration().apply {
        // Configure parameters (use explicit `this.` receiver for better code completion):

        // Configure the top bar.

        // Set the top bar mode.
        this.topBar.mode = TopBarMode.GRADIENT

        // Set the background color which will be used as a gradient.
        this.topBar.backgroundColor = ScanbotColor("#C8193C")

        // Configure the status bar look. If visible - select DARK or LIGHT according to your app's theme color.
        this.topBar.statusBarMode = StatusBarMode.HIDDEN

        // Configure the Cancel button.
        this.topBar.cancelButton.text = "Cancel"
        this.topBar.cancelButton.foreground.color = ScanbotColor("#FFFFFF")

        // Configure other parameters as needed.
    }
}
