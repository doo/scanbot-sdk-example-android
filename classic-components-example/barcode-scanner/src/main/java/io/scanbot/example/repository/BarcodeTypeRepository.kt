package io.scanbot.example.repository

import io.scanbot.sdk.barcode.BarcodeFormat
import io.scanbot.sdk.barcode.BarcodeFormats


object BarcodeTypeRepository {
    val selectedTypes = mutableSetOf<BarcodeFormat>().also {
        it.addAll(BarcodeFormats.common)
    }

    fun selectType(type: BarcodeFormat) {
        selectedTypes.add(type)
    }

    fun deselectType(type: BarcodeFormat) {
        selectedTypes.remove(type)
    }

}