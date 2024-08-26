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

import android.content.Context
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.barcode.entity.AustraliaPostCustomerFormat
import io.scanbot.sdk.barcode.entity.BarcodeScannerAdditionalConfig
import io.scanbot.sdk.barcode.entity.BarcodeScannerConfig
import io.scanbot.sdk.barcode.entity.Gs1Handling
import io.scanbot.sdk.barcode.entity.MSIPlesseyChecksumAlgorithm
import java.util.EnumSet

fun barcodeScannerWithAdditionalConfigSnippet(context: Context) {
    // Create the default configuration object.
    val barcodeDetector = ScanbotSDK(context).createBarcodeDetector()
    val additionalConfig = BarcodeScannerAdditionalConfig(
        minimumTextLength = 0,
        maximumTextLength = 0,
        minimum1DQuietZoneSize = 10,
        gs1Handling = Gs1Handling.PARSE,
        msiPlesseyChecksumAlgorithms = EnumSet.of(MSIPlesseyChecksumAlgorithm.Mod10),
        stripCheckDigits = false,
        lowPowerMode = false,
        useIata2Of5Checksum = true,
        useCode11Checksum = true,
        australiaPostCustomerFormat = AustraliaPostCustomerFormat.ALPHA_NUMERIC
    )
    val config = BarcodeScannerConfig(
        additionalConfig = additionalConfig,
        // modify other parameters
    )
    barcodeDetector.setConfig(config)
}

fun barcodeScannerWithModifyAdditionalConfigSnippet(context: Context) {
    // Create the default configuration object.
    val barcodeDetector = ScanbotSDK(context).createBarcodeDetector()
    barcodeDetector.modifyConfig {
        modifyAdditionalConfig {
            setMinimumTextLength(0)
            setMaximumTextLength(0)
            setMinimum1DQuietZoneSize(10)
            setGs1HandlingMode(Gs1Handling.PARSE)
            setMsiPlesseyChecksumAlgorithms(EnumSet.of(MSIPlesseyChecksumAlgorithm.Mod10))
            setStripCheckDigits(false)
            setLowPowerMode(false)
            setUseIata2Of5Checksum(true)
            setUseCode11Checksum(true)
            setAustraliaPostCustomerFormat(AustraliaPostCustomerFormat.ALPHA_NUMERIC)
        }
        // modify other parameters
    }
}
