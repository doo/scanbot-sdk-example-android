package io.scanbot.example.model

import io.scanbot.sdk.barcode.entity.BarcodeScanningResult
import io.scanbot.sdk.barcodescanner.BarcodeScannerResult

data class BarcodeResultBundle(
    val barcodeScanningResult: BarcodeScannerResult,
    val imagePath: String? = null,
    val previewPath: String? = null
)