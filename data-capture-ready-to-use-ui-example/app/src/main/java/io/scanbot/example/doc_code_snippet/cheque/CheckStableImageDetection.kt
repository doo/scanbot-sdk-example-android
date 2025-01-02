package io.scanbot.example.doc_code_snippet.cheque

import android.graphics.Bitmap
import io.scanbot.sdk.check.*

private fun processImage(
    checkScanner: CheckScanner,
    bitmap: Bitmap
) {
    val result = checkScanner.scanFromBitmap(bitmap, 0)
    result?.check?.let { wrapCheck(it) }
    // Check recognition results are processed
}
