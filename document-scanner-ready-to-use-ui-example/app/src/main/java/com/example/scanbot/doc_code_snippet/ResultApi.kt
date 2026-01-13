package com.example.scanbot.doc_code_snippet

import io.scanbot.common.Result
import io.scanbot.common.combineResults
import io.scanbot.common.getOrDefault
import io.scanbot.common.mapFailure
import io.scanbot.common.mapSuccess
import io.scanbot.common.onFailure
import io.scanbot.common.onSuccess
import io.scanbot.sdk.barcode.BarcodeFormat
import io.scanbot.sdk.barcode.BarcodeScannerResult
import io.scanbot.sdk.barcode.ScanbotSdkBarcodeScanner
import io.scanbot.sdk.documentscanner.ScanbotSdkDocumentScanner
import io.scanbot.sdk.image.ImageRef

class ResultApi {
    // @Tag("Handle Result with when/switch")
    fun processResultWithSwitch(result: Result<BarcodeScannerResult>) = when (result) {
        is Result.Success<*> -> {
            val barcodeResult = result.data
            // Handle successful result
        }

        is Result.ComponentUnavailableError,
        is Result.IllegalStateError,
        is Result.InvalidArgumentError,
        is Result.InvalidDataError,
        is Result.InvalidImageRefError,
        is Result.InvalidLicenseError,
        is Result.IoError,
        is Result.NullPointerError,
        is Result.OperationCanceledError,
        is Result.OutOfMemoryError,
        is Result.TimeoutError,
        is Result.UnknownError -> {
            // all Result.[ERROR_NAME] are also of type `Result.Unexpected` and can be handled together as throwable errors or logged etc.
            val exception = result.message
            // Handle error
            println("Error scanning barcodes: ${exception}")
        }
    }
    // @EndTag("Handle Result with when/switch")

    // @Tag("Handle Result with getter functions")
    fun proceedWithGetters(result: Result<BarcodeScannerResult>) {
        // get value or null if unsuccessful
        val nullableResult = result.getOrNull()
        // get value or throw exception if unsuccessful then handle exception with try-catch
        val nonNullableResult = result.getOrThrow()
        // get value or put default value if unsuccessful
        val resultOrDefaultValue = result.getOrDefault(
            BarcodeScannerResult(
                emptyList(),
                false
            )
        )
    }
    // @EndTag("Handle Result with getter functions")

    // @Tag("Handle Result with chain API")
    fun processResultWithChainApi(result: Result<BarcodeScannerResult>) {
        // handle success and failure in a chained way without nesting and result type checking
        result.onSuccess { barcodeResult ->
            // Handle successful barcode scanning result
            val barcodes = barcodeResult.barcodes
            // Process barcodes as needed
        }.onFailure { exception ->
            // Handle error during barcode scanning
            println("Error scanning barcodes: ${exception.message}")
        }

        // chained transformations
        val barcodeSize = result.mapSuccess { barcodeResult ->
            // Transform the successful result to another successful result
            barcodeResult.barcodes.size // or Return Result.Unexpected in case of error
            //Result.IllegalStateError("Transformation error") // example of returning an error during transformation
        }.mapFailure { exception ->
            // Transform the error to a default value
            0
        }.getOrNull()
    }
    // @EndTag("Handle Result with chain API")

    // @Tag("Chain Result with SDK calls")
    fun chainWithScannerCall(
        image: ImageRef,
        barcoderScanner: ScanbotSdkBarcodeScanner,
        documentScanner: ScanbotSdkDocumentScanner
    ) {
        val polygon = barcoderScanner.run(image)
            .mapSuccess { barcodeScanResult ->
                // Transform barcode scan result to document scan result if the document contains barcode with specified type
                if (barcodeScanResult.barcodes.none { item -> item.format == BarcodeFormat.QR_CODE }) {
                    // return from mapSuccess with Result.IllegalStateError
                    throw Result.IllegalStateError("Document should contain unique qr code to be accepted")
                }
                // if result of document scanning is failed due to issues it will trigger mapSuccess function to return Unexpected Result type
                val documentScanResult = documentScanner.run(image).getOrReturn()
                // return document polygon if scanning result if successful
                documentScanResult.pointsNormalized
            }.getOrDefault(emptyList())
    }
    // @EndTag("Chain Result with SDK calls")

    // @Tag("Combine Result of SDK calls")
    fun combineWithScannerCall(
        image: ImageRef,
        barcoderScanner: ScanbotSdkBarcodeScanner,
        documentScanner: ScanbotSdkDocumentScanner
    ) {

        val polygon = combineResults(
            barcoderScanner.run(image),
            documentScanner.run(image)
        ) { barcodeResult, documentResult ->
            // Transform barcode scan result to document scan result if the document contains barcode with specified type
            if (barcodeResult.barcodes.none { item -> item.format == BarcodeFormat.QR_CODE }) {
                // return from mapSuccess with Result.IllegalStateError
                return@combineResults Result.IllegalStateError("Document should contain unique qr code to be accepted")
            }
            // return document polygon if scanning result if successful
            Result.Success(documentResult.pointsNormalized)
        }.getOrDefault(emptyList())

    }
    // @EndTag("Combine Result of SDK calls")
}