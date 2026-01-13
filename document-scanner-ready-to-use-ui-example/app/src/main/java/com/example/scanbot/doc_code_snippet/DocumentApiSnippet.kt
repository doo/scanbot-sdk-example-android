package com.example.scanbot.doc_code_snippet


import android.graphics.Bitmap
import android.net.Uri
import androidx.core.net.toFile
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.docprocessing.Document

// @Tag("Storing and retrieving a Document")
fun createScannedDocument(sdk: ScanbotSDK, images: List<Bitmap>) {

    // Create a new document with the specified maximum image size.
    // Setting the limit to 0, effectively disables the size limit.
    val scannedDocument = sdk.documentApi.createDocument(documentImageSizeLimit = 2048).getOrNull()

    // add images to the document.
    images.forEach { scannedDocument?.addPage(it) }
}

fun createFromUri(sdk: ScanbotSDK, uri: Uri): Document? {
    // Create the scanned document using convenience initializer `init?(document:documentImageSizeLimit:)`
    // `Document` doesn't support `documentImageSizeLimit`, but you can add it to unify size of the documents.
    val scannedDocument = sdk.documentApi.createDocument(documentImageSizeLimit = 2048).getOrNull()
    scannedDocument?.addPage(uri)
    // Return newly created scanned document
    return scannedDocument
}

fun accessImageURLs(scannedDocument: Document) {
    // get an array of original image URLs from scanned document.
    val originalImageUris = scannedDocument.pages.map { page -> page.originalFileUri }

    // get an array of document image (processed, rotated, cropped and filtered) URLs from scanned document.
    val documentImageUris = scannedDocument.pages.map { page -> page.documentFileUri }

    // get an array of screen-sized preview image URLs from scanned document.
    val previewImageUris = scannedDocument.pages.map { page -> page.documentPreviewFileUri }
}

fun reorderPagesInScannedDocument(scannedDocument: Document) {
    // Move last and first images in the scanned document.
    // Create source index.
    val sourceIndex = scannedDocument.pageCount - 1

    // create destination index.
    val destinationIndex = 0

    // Reorder images in the scanned document.
    scannedDocument.movePage(sourceIndex = sourceIndex, destinationIndex = destinationIndex)
}

fun removeAllPagesFromScannedDocument(scannedDocument: Document) {
    // Call the `removeAllPages()` to remove all pages from the document, but keep the document itself.
    scannedDocument.removeAllPages()
}

fun removePDFFromScannedDocument(scannedDocument: Document) {
    // Take a file from document and delete it
    scannedDocument.pdfUri.toFile().delete()
}

fun removeTIFFFromScannedDocument(scannedDocument: Document) {
    // Take a file from document and delete it
    scannedDocument.tiffUri.toFile().delete()
}

fun deleteScannedDocument(scannedDocument: Document) {
    // just call delete and document would be deleted
    scannedDocument.delete()
}
// @EndTag("Storing and retrieving a Document")
