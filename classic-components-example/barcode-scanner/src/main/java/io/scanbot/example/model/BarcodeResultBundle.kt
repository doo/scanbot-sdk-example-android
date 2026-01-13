package io.scanbot.example.model

import io.scanbot.sdk.barcode.BarcodeScannerResult
import io.scanbot.sdk.image.ImageRef

data class BarcodeResultBundle(
    val barcodeScanningResult: BarcodeScannerResult,
    val imageRef: ImageRef? = null,
)