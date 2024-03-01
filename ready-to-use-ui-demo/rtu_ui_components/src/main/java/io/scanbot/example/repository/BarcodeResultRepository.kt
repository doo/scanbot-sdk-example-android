package io.scanbot.example.repository

import io.scanbot.example.model.BarcodeResultBundle
import io.scanbot.sdk.ui_v2.barcode.configuration.BarcodeItem

object BarcodeResultRepository {
    var barcodeResultBundle: BarcodeResultBundle? = null

    var selectedBarcodeItem: BarcodeItem? = null
}