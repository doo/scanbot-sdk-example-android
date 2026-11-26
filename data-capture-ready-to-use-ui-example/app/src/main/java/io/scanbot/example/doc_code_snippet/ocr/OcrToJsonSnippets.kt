package io.scanbot.example.doc_code_snippet.ocr

import android.graphics.PointF
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.scanbot.common.Result
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.docprocessing.Document
import io.scanbot.sdk.ocr.OcrResult
import io.scanbot.sdk.ui_v2.common.activity.registerForActivityResultOk
import io.scanbot.sdk.ui_v2.document.DocumentScannerActivity
import io.scanbot.sdk.ui_v2.document.configuration.DocumentScanningFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt

/*
    NOTE: this snippet of code is to be used only as a part of the website documentation.
    This code is not intended for any use outside of the support of documentation by Scanbot SDK GmbH employees.
*/

// NOTE for maintainers: whenever changing this code,
// ensure that links using it are still pointing to valid lines!
// Pay attention to imports adding/removal/sorting!
// Page URLs using this code:
// https://docs.scanbot.io/android/data-capture-modules/ocr/scanning-from-an-image/
// https://docs.scanbot.io/android/data-capture-modules/ocr/ocr-to-json-for-llms/

// @Tag("Converting the OCR result to JSON")
fun convertToJson(result: OcrResult): String {

    // Each page returns its blocks, lines, words, and glyphs as JSON with `toJson()`.
    // `io.scanbot.sdk.ocr.OcrResult` doesn't provide `toJson()`, so wrap the pages in one JSON object.
    val pages = JSONArray()
    for (page in result.ocrPages) {
        pages.put(page.toJson())
    }
    return JSONObject().put("pages", pages).toString()
}
// @EndTag("Converting the OCR result to JSON")

class OcrToJsonSnippetActivity : AppCompatActivity() {

    // @Tag("Scanning a page")
    // Add the following to your Activity:
    private val documentScannerLauncher =
        registerForActivityResultOk(DocumentScannerActivity.ResultContract()) { document ->
            lifecycleScope.launch {
                try {
                    // OCR takes time, so run steps 2 and 3 off the main thread.
                    val json = withContext(Dispatchers.Default) {
                        // Step 2: run OCR on the page.
                        val result = runOcr(document)

                        // Step 3: create the JSON.
                        createJson(result)
                    }

                    // Pass the JSON to your LLM.
                    Log.i("OcrToJson", json)
                } catch (error: Result.Failure) {
                    // OCR failed, for example because the license isn't valid.
                    Log.e("OcrToJson", "OCR failed", error)
                }
            }
        }

    private fun scanPage() {
        // Step 1: scan one page.
        val configuration = DocumentScanningFlow().apply {
            outputSettings.pagesScanLimit = 1
        }
        documentScannerLauncher.launch(configuration)
    }
    // @EndTag("Scanning a page")

    // @Tag("Running OCR")
    private fun runOcr(document: Document): OcrResult {

        // Run OCR with the default settings.
        val ocrEngineManager = ScanbotSDK(this).createOcrEngineManager()
        return ocrEngineManager.recognizeFromDocument(document).getOrThrow()
    }
    // @EndTag("Running OCR")

    // @Tag("Creating the JSON")
    private fun createJson(result: OcrResult): String {

        // Collect the text and the position of every line.
        val pages = JSONArray()
        for (page in result.ocrPages) {
            val lines = JSONArray()
            for (block in page.blocks) {
                for (line in block.lines) {
                    lines.put(JSONObject().put("text", line.text).put("box", boxAround(line.roi)))
                }
            }
            pages.put(JSONObject().put("lines", lines))
        }

        // Create the JSON.
        return JSONObject().put("pages", pages).toString()
    }

    // The box around the polygon of a line: x, y, width, and height in pixels of the page image.
    private fun boxAround(roi: List<PointF>): JSONArray {
        val left = roi.minOf { it.x }
        val top = roi.minOf { it.y }
        val width = roi.maxOf { it.x } - left
        val height = roi.maxOf { it.y } - top
        return JSONArray(listOf(left, top, width, height).map { it.roundToInt() })
    }
    // @EndTag("Creating the JSON")
}
