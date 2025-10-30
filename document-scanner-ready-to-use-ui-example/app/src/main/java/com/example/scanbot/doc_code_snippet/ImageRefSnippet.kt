package com.example.scanbot.doc_code_snippet

import android.graphics.Bitmap
import android.graphics.Rect
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import io.scanbot.common.getOrThrow
import io.scanbot.sdk.barcode.BarcodeScanner
import io.scanbot.sdk.image.BasicImageLoadOptions
import io.scanbot.sdk.image.BufferImageLoadOptions
import io.scanbot.sdk.image.BufferLoadMode
import io.scanbot.sdk.image.ColorConversion
import io.scanbot.sdk.image.EncryptionMode
import io.scanbot.sdk.image.ImageRef
import io.scanbot.sdk.image.PathImageLoadOptions
import io.scanbot.sdk.image.PathLoadMode
import io.scanbot.sdk.image.SaveImageOptions
import io.scanbot.sdk.persistence.fileio.CoreStreamProvider

// @Tag("ImageRef from bitmap")
fun createImageRefFrom(bitmap: Bitmap) {
    // Create ImageRef from the full bitmap
    val original = ImageRef.fromBitmap(bitmap)
    // Create ImageRef from a cropped area of the bitmap
    val preCropped = ImageRef.fromBitmap(
        bitmap,
        BasicImageLoadOptions(
            cropRect = Rect(
                0,
                0,
                200,
                200
            ),
        )
    )
}
// @EndTag("ImageRef from bitmap")

// @Tag("ImageRef from stream")
fun createImageRefFrom(stream: InputStream) {
    // Create ImageRef from InputStream
    val imageRef = ImageRef.fromInputStream(stream)

    val imageRefWithOptions = ImageRef.fromInputStream(
        stream,
        BufferImageLoadOptions(
            // Define crop rectangle
            cropRect = Rect(
                0,
                0,
                200,
                200
            ),
            // Convert image to grayscale
            colorConversion = ColorConversion.GRAY,
            // Use lazy loading mode, image would be loaded into memory only when first used
            loadMode = BufferLoadMode.LAZY
        )
    )
}
// @EndTag("ImageRef from stream")

// @Tag("ImageRef from byte array")
fun createImageRefFrom(byteArray: ByteArray) {
    // Create ImageRef from byte array
    val imageRef = ImageRef.fromEncodedBuffer(byteArray)
    val imageRefWithOptions = ImageRef.fromEncodedBuffer(
        byteArray,
        BufferImageLoadOptions(
            // Define crop rectangle
            cropRect = Rect(
                0,
                0,
                200,
                200
            ),
            // Convert image to grayscale
            colorConversion = ColorConversion.GRAY,
            // Use lazy loading mode, image would be loaded into memory only when first used
            loadMode = BufferLoadMode.LAZY
        )
    )
}
// @EndTag("ImageRef from byte array")

// @Tag("ImageRef from file")
fun createImageRefFrom(filePath: String) {
    // Create ImageRef from file path
    val imageRef = ImageRef.fromPath(filePath)
    // Create ImageRef from file path with options
    val imageRefWithOptions = ImageRef.fromPath(
        filePath,
        PathImageLoadOptions(
            // Define crop rectangle
            cropRect = Rect(
                0,
                0,
                200,
                200
            ),
            // Convert image to grayscale
            colorConversion = ColorConversion.GRAY,
            // Use lazy loading mode, image would be loaded into memory only when first used
            loadMode = PathLoadMode.LAZY_WITH_COPY,
            // handle encryption automatically based on global ImageRef/ScanbotSdk encryption settings
            encryptionMode = EncryptionMode.AUTO,
            // to disable decryption while reading for this specific file (in case its not encrypted with sdk encryption ON), use
            //  encryptionMode = EncryptionMode.DISABLED,
            // if you need to handle encryption/decryption manually for this specific file, provide your implementation of CoreStreamProvider
            /* encryptionMode = EncryptionMode.REQUIRED,
                decrypter = object : CoreStreamProvider {
                  override fun openFileOutputStream(destinationFile: File): OutputStream? {
                     // implement encryption logic here
                  }

                  override fun openFileInputStream(sourceFile: File): InputStream? {
                     // implement decryption logic here
                  }
              }*/
        )
    )
}
// @EndTag("ImageRef from file")

// @Tag("Using ImageRef)
fun useImageRef(imageRef: ImageRef, barcodeScanner: BarcodeScanner) {
    // use image ref to detect barcodes on it
    val barcodes = barcodeScanner.run(imageRef)
    // clear image resources after usage to free memory
    imageRef.close()
}
// @EndTag("Using ImageRef)

// @Tag("ImageRef metadata")
fun getImageRefMetadata(imageRef: ImageRef) {
    val meta = imageRef.info().getOrThrow()
    val width = meta.width
    val height = meta.height
    // size on disk or in memory depending on load mode
    val sizeInBytes = meta.maxByteSize
}
// @EndTag("ImageRef metadata")

// @Tag("Save image from ImageRef to file")
fun saveImageRefToFile(imageRef: ImageRef, destinationFile: File) {
    imageRef.saveImage(
        destinationFile.absolutePath,
        SaveImageOptions(
            quality = 100,
            encryptionMode = EncryptionMode.AUTO
            // to disable decryption while reading for this specific file (in case its not encrypted with sdk encryption ON), use
            //  encryptionMode = EncryptionMode.DISABLED,
            // if you need to handle encryption/decryption manually for this specific file, provide your implementation of CoreStreamProvider
            /* encryptionMode = EncryptionMode.REQUIRED,
                decrypter = object : CoreStreamProvider {
                  override fun openFileOutputStream(destinationFile: File): OutputStream? {
                     // implement encryption logic here
                  }

                  override fun openFileInputStream(sourceFile: File): InputStream? {
                     // implement decryption logic here
                  }
              }*/
        )
    ).getOrThrow()
}
// @EndTag("Save image from ImageRef to file")