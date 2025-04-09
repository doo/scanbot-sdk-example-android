package io.scanbot.example.model

import io.scanbot.sdk.barcode.BarcodeScannerResult

data class BarcodeResultBundle(
    val barcodeScanningResult: BarcodeScannerResult,
    val imagePath: String? = null,
    val previewPath: String? = null
)