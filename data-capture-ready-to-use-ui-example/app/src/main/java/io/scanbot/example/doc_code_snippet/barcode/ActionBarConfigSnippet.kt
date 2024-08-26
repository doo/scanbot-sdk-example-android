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

fun actionBarConfigSnippet() {
    // Create the default configuration object.
    BarcodeScannerConfiguration().apply {
        // Configure parameters (use explicit `this.` receiver for better code completion):

        // Configure the action bar.
        // Hide/unhide the flash button.
        this.actionBar.flashButton.visible = true

        // Configure the inactive state of the flash button.
        this.actionBar.flashButton.backgroundColor = ScanbotColor("#7A000000")
        this.actionBar.flashButton.foregroundColor = ScanbotColor("#FFFFFF")

        // Configure the active state of the flash button.
        this.actionBar.flashButton.activeBackgroundColor = ScanbotColor("#FFCE5C")
        this.actionBar.flashButton.activeForegroundColor = ScanbotColor("#000000")

        // Hide/unhide the zoom button.
        this.actionBar.zoomButton.visible = true

        // Configure the inactive state of the zoom button.
        this.actionBar.zoomButton.backgroundColor = ScanbotColor("#7A000000")
        this.actionBar.zoomButton.foregroundColor = ScanbotColor("#FFFFFF")
        // Zoom button has no active state - it only switches between zoom levels (for configuring those please refer to camera configuring).

        // Hide/unhide the flip camera button.
        this.actionBar.flipCameraButton.visible = true

        // Configure the inactive state of the flip camera button.
        this.actionBar.flipCameraButton.backgroundColor = ScanbotColor("#7A000000")
        this.actionBar.flipCameraButton.foregroundColor = ScanbotColor("#FFFFFF")
        // Flip camera button has no active state - it only switches between front and back camera.

        // Configure other parameters as needed.
    }
}
