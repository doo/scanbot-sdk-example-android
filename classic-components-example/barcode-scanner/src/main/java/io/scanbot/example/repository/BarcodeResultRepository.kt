package io.scanbot.example.repository

import io.scanbot.example.model.BarcodeResultBundle
import io.scanbot.sdk.barcodescanner.BarcodeItem

object BarcodeResultRepository {
    var barcodeResultBundle: BarcodeResultBundle? = null

    var selectedBarcodeItem: BarcodeItem? = null
}