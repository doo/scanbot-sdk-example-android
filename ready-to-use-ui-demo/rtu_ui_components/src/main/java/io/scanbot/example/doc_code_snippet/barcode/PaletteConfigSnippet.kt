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
import io.scanbot.sdk.ui_v2.common.ScanbotPalette

fun paletteConfigSnippet() {
    // Create the default configuration object.
    val config = BarcodeScannerConfiguration().apply {
        // Configure parameters (use explicit `this.` receiver for better code completion):

        // Set the entirely new palette set of colors.
        this.palette = ScanbotPalette().apply {
            this.sbColorPrimary = ScanbotColor("#C8193C")
            this.sbColorPrimaryDisabled = ScanbotColor("#F5F5F5")
            this.sbColorNegative = ScanbotColor("#FF3737")
            this.sbColorPositive = ScanbotColor("#4EFFB4")
            this.sbColorWarning = ScanbotColor("#FFCE5C")
            this.sbColorSecondary = ScanbotColor("#FFEDEE")
            this.sbColorSecondaryDisabled = ScanbotColor("#F5F5F5")
            this.sbColorOnPrimary = ScanbotColor("#FFFFFF")
            this.sbColorOnSecondary = ScanbotColor("#C8193C")
            this.sbColorSurface = ScanbotColor("#FFFFFF")
            this.sbColorOutline = ScanbotColor("#EFEFEF")
            this.sbColorOnSurfaceVariant = ScanbotColor("#707070")
            this.sbColorOnSurface = ScanbotColor("#000000")
            this.sbColorSurfaceLow = ScanbotColor("#26000000")
            this.sbColorSurfaceHigh = ScanbotColor("#7A000000")
            this.sbColorModalOverlay = ScanbotColor("#A3000000")
        }

        // ...or just alter one color and keep other default.
        this.palette = ScanbotPalette().apply {
            this.sbColorPrimary = ScanbotColor("#c86e19")
        }
    }
}
