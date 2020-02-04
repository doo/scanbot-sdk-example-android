package io.scanbot.example.model

import io.scanbot.sdk.barcode.entity.BarcodeItem

object BarcodeResultRepository {
    var barcodeResultBundle: BarcodeResultBundle? = null

    var selectedBarcodeItem: BarcodeItem? = null
}