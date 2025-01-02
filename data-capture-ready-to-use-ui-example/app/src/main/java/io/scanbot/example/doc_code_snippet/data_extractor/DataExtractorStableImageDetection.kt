package io.scanbot.example.doc_code_snippet.data_extractor

/*
    NOTE: this snippet of code is to be used only as a part of the website documentation.
    This code is not intended for any use outside of the support of documentation by Scanbot SDK GmbH employees.
*/

// NOTE for maintainers: whenever changing this code,
// ensure that links using it are still pointing to valid lines!
// Pay attention to imports adding/removal/sorting!
// Page URLs using this code:
// TODO: add URLs here

import android.graphics.Bitmap
import io.scanbot.sdk.documentdata.*
import io.scanbot.sdk.mc.*


fun processImage(
    dataExtractor: DocumentDataExtractor,
    bitmap: Bitmap
) {
    val result = dataExtractor.extractFromBitmap(
        bitmap,
        0,
    )
    if (result != null && result.status == DocumentDataExtractionStatus.SUCCESS) {
        // Document results are processed
        result.document?.let { wrapGenericDocument(it) }
    }
}

