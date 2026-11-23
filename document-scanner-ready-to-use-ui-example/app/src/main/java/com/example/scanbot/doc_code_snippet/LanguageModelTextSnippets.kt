package com.example.scanbot.doc_code_snippet

import android.graphics.PointF
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.scanbot.common.Result
import io.scanbot.common.onFailure
import io.scanbot.common.onSuccess
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.docprocessing.Document
import io.scanbot.sdk.documentqualityanalyzer.DocumentQualityAnalyzerConfiguration
import io.scanbot.sdk.image.ImageRef
import io.scanbot.sdk.image.ImageRotation
import io.scanbot.sdk.imageprocessing.ScanbotSdkImageProcessor
import io.scanbot.sdk.ocr.OcrEngineManager
import io.scanbot.sdk.ocr.OcrResult
import io.scanbot.sdk.ui_v2.document.DocumentScannerActivity
import io.scanbot.sdk.ui_v2.document.configuration.DocumentScanningFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.abs
import kotlin.math.roundToInt

/*
    NOTE: this snippet of code is to be used only as a part of the website documentation.
    This code is not intended for any use outside of the support of documentation by Scanbot SDK GmbH employees.
*/

// Page URLs using this code:
// https://docs.scanbot.io/android/data-capture-modules/ocr/scanning-from-an-image/
// https://docs.scanbot.io/android/data-capture-modules/ocr/preparing-text-for-language-models/

// @Tag("Converting the OCR result to JSON")
fun convertToJson(result: OcrResult): String {

    // Each page returns its blocks, lines, words, and glyphs as JSON with `toJson()`.
    // `io.scanbot.sdk.ocr.OcrResult` doesn't provide `toJson()`, so wrap the pages in one JSON object.
    val pages = JSONArray(result.ocrPages.map { it.toJson() })
    return JSONObject().put("pages", pages).toString()
}
// @EndTag("Converting the OCR result to JSON")

// @Tag("Creating a compact JSON from the OCR result")
fun convertToCompactJson(result: OcrResult): String {

    // Words with a low confidence are often icons, logos, or QR codes read as characters.
    val minimumWordConfidence = 0.5

    val pages = result.ocrPages.mapIndexed { index, page ->
        val lines = page.blocks.flatMap { it.lines }.mapNotNull { line ->
            val words = line.words.filter { it.confidence >= minimumWordConfidence }.map { it.text }
            if (words.isEmpty()) null else words.joinToString(" ")
        }
        JSONObject().put("page", index + 1).put("lines", JSONArray(lines))
    }
    return JSONArray(pages).toString()
}
// @EndTag("Creating a compact JSON from the OCR result")

class LanguageModelTextSnippetActivity : AppCompatActivity() {

    // @Tag("Scanning the pages")
    // Add the following to your Activity:
    private val scanbotSdk by lazy { ScanbotSDK(application) }

    private val documentScannerLauncher =
        registerForActivityResult(DocumentScannerActivity.ResultContract()) { activityResult ->
            activityResult.onSuccess { document ->
                lifecycleScope.launch {
                    try {
                        // Turn the scanned pages into a compact JSON. Run it off the main thread.
                        val json = withContext(Dispatchers.Default) { makeJson(scanbotSdk, document) }

                        // Pass the JSON to your language model.
                        Log.i("LanguageModelText", json)
                    } catch (error: Result.Failure) {
                        // An SDK call failed, for example because the license isn't valid.
                        Log.e("LanguageModelText", "Creating the JSON failed", error)
                    }
                }
            }.onFailure { error ->
                // Scanning failed. Canceling the scanner doesn't call `onFailure`.
                Log.e("LanguageModelText", "Scanning failed", error)
            }
        }

    fun startScanning() {
        // Scan as many pages as the user wants.
        val configuration = DocumentScanningFlow().apply {
            outputSettings.pagesScanLimit = 0
        }
        documentScannerLauncher.launch(configuration)
    }
    // @EndTag("Scanning the pages")
}

// @Tag("Preparing a page for OCR")
// The upright image in full size for the Barcode Scanner, and a smaller copy for OCR.
class PreparedImage(val upright: ImageRef, val forOcr: ImageRef)

class OcrInputPreparation(scanbotSdk: ScanbotSDK) {

    // Detect the text orientation. The input scales let the analyzer find large text too.
    // Input scales other than [1.0] require `maxImageSize` 0.
    private val qualityAnalyzer = scanbotSdk.createDocumentQualityAnalyzer(
        DocumentQualityAnalyzerConfiguration().apply {
            inputScales = listOf(1.0, 0.5, 0.25, 0.125)
            maxImageSize = 0
            // Keep the default `minRequiredOrientationConfidence` of 0.9.
            // A lower threshold lets the analyzer turn pages it's unsure about, for example photo posters with little text.
        }
    ).getOrThrow()

    private val imageProcessor = ScanbotSdkImageProcessor.create()

    fun prepare(image: ImageRef): PreparedImage {
        val orientation = qualityAnalyzer.run(image).getOrThrow().use { it.orientation }

        // The orientation is in degrees counterclockwise. Rotating the image clockwise by the same angle makes it upright.
        val rotation = when ((orientation / 90).roundToInt()) {
            1 -> ImageRotation.CLOCKWISE_90
            -1 -> ImageRotation.COUNTERCLOCKWISE_90
            2, -2 -> ImageRotation.CLOCKWISE_180
            else -> ImageRotation.NONE
        }
        val uprightImage = if (rotation == ImageRotation.NONE) {
            image
        } else {
            imageProcessor.rotate(image, rotation).getOrThrow()
        }

        // Limit the longest edge for OCR. At full camera resolution, the engine misses large text.
        // `resize` sets the longest edge to the given size, so resize only larger images.
        val info = uprightImage.info().getOrThrow()
        val ocrImage = if (maxOf(info.width, info.height) > 1400) {
            imageProcessor.resize(uprightImage, 1400).getOrThrow()
        } else {
            uprightImage
        }
        return PreparedImage(upright = uprightImage, forOcr = ocrImage)
    }
}
// @EndTag("Preparing a page for OCR")

// @Tag("Putting the lines in reading order")
// Text to place among the lines, for example the content of a QR code, with its position in the OCR image.
class PlacedText(val text: String, val roi: List<PointF>)

fun readingOrder(page: io.scanbot.sdk.ocr.Page, placedTexts: List<PlacedText>): List<String> {

    class PlacedLine(val text: String, val top: Float, val bottom: Float, val left: Float)

    fun placedLine(text: String, roi: List<PointF>) = PlacedLine(
        text = text,
        top = roi.minOfOrNull { it.y } ?: 0f,
        bottom = roi.maxOfOrNull { it.y } ?: 0f,
        left = roi.minOfOrNull { it.x } ?: 0f,
    )

    // Leave out words with a low confidence, and lines without a letter or number.
    // They're often icons, logos, or QR codes read as characters.
    val lines = page.blocks.flatMap { it.lines }.mapNotNull { line ->
        val text = line.words.filter { it.confidence >= 0.5 }.joinToString(" ") { it.text }
        // Letters (L), combining marks (M), and numbers (N) in any script.
        if (text.any { it.category.code[0] in "LMN" }) placedLine(text, line.roi) else null
    }.toMutableList()

    // Sort the placed texts in like lines, so each one ends up next to its label on the page.
    lines += placedTexts.map { placedLine(it.text, it.roi) }

    // Sort the lines from top to bottom.
    // Lines whose vertical centers are close share a row, from left to right.
    val rows = mutableListOf<MutableList<PlacedLine>>()
    for (line in lines.sortedBy { it.top }) {
        val isInLastRow = rows.lastOrNull()?.any { other ->
            val centerDistance = abs((other.top + other.bottom) / 2 - (line.top + line.bottom) / 2)
            centerDistance < 0.5f * minOf(line.bottom - line.top, other.bottom - other.top)
        } ?: false
        if (isInLastRow) {
            rows.last().add(line)
        } else {
            rows.add(mutableListOf(line))
        }
    }
    return rows.map { row -> row.sortedBy { it.left }.joinToString(" | ") { it.text } }
}
// @EndTag("Putting the lines in reading order")

// @Tag("Creating the JSON")
fun makeJson(scanbotSdk: ScanbotSDK, document: Document): String {
    val ocrEngineManager = scanbotSdk.createOcrEngineManager()
    ocrEngineManager.setOcrConfig(OcrEngineManager.OcrConfig(engineMode = OcrEngineManager.EngineMode.SCANBOT_OCR))
    val inputPreparation = OcrInputPreparation(scanbotSdk)
    val barcodeScanner = scanbotSdk.createBarcodeScanner().getOrThrow()
    val pages = JSONArray()

    for ((index, page) in document.pages.withIndex()) {
        val image = page.documentImageRef ?: continue
        val prepared = inputPreparation.prepare(image)
        val ocrImageInfo = prepared.forOcr.info().getOrThrow()
        val barcodes = JSONArray()
        val placedTexts = mutableListOf<PlacedText>()

        // OCR doesn't read the content of QR codes. The Barcode Scanner does, for example a link.
        barcodeScanner.run(prepared.upright).getOrThrow().use { result ->
            for (item in result.barcodes) {
                barcodes.put(JSONObject().put("format", item.format.name).put("text", item.text))

                // Place the content of each barcode where the barcode is on the page.
                // The normalized quad is relative to the upright image, so scale it to the OCR image.
                placedTexts += PlacedText(
                    text = "[${item.format.name}: ${item.text}]",
                    roi = item.quadNormalized.map { PointF(it.x * ocrImageInfo.width, it.y * ocrImageInfo.height) },
                )
            }
        }

        // Run OCR on the prepared image, then put the lines and the barcodes in reading order.
        val result = ocrEngineManager.recognizeFromImageRefs(listOf(prepared.forOcr)).getOrThrow()
        val lines = result.ocrPages.flatMap { readingOrder(it, placedTexts) }
        pages.put(JSONObject().put("page", index + 1).put("lines", JSONArray(lines)).put("barcodes", barcodes))

        // Release the page image and the images made for OCR. On an upright, small page, they're the same image.
        listOf(image, prepared.upright, prepared.forOcr).distinct().forEach { it.close() }
    }
    return JSONObject().put("pages", pages).toString()
}
// @EndTag("Creating the JSON")
