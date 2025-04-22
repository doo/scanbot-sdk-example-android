package io.scanbot.example.doc_code_snippet.ocr

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toFile
import io.scanbot.pdf.model.*
import io.scanbot.sdk.*
import io.scanbot.sdk.docprocessing.Document
import io.scanbot.sdk.entity.*
import io.scanbot.sdk.ocr.*
import io.scanbot.sdk.ocr.intelligence.*
import io.scanbot.sdk.ocr.model.*
import io.scanbot.sdk.ocr.process.*

/*
    NOTE: this snippet of code is to be used only as a part of the website documentation.
    This code is not intended for any use outside of the support of documentation by Scanbot SDK GmbH employees.
*/

// NOTE for maintainers: whenever changing this code,
// ensure that links using it are still pointing to valid lines!
// Pay attention to imports adding/removal/sorting!
// Page URLs using this code:
// TODO: add URLs here

fun initSdkSnippet(application: Application, licenseKey: String) {
    // @Tag("Initialize SDK")
    ScanbotSDKInitializer()
            .license(application, licenseKey)
            .prepareOCRLanguagesBlobs(true)
            //...
            .initialize(application)
    // @EndTag("Initialize SDK")
}

fun createOcrEngine(context: Context) {
    // @Tag("Create OCR Engine")
    val scanbotSDK = ScanbotSDK(context)
    val ocrRecognizer = scanbotSDK.createOcrEngine()
    // @EndTag("Create OCR Engine")
}

fun enableBinarizationInOcrSettingsSnippet(application: Application) {
    // @Tag("Enable Binarization in OCR Settings")
    ScanbotSDKInitializer()
            .useOcrSettings(OcrSettings.Builder().binarizeImage(true).build())
            //...
            .initialize(application)
    // @EndTag("Enable Binarization in OCR Settings")
}

fun engineModeTesseractSnippet(context: Context) {
    // @Tag("Engine Mode Tesseract")
    val ocrRecognizer = ScanbotSDK(context).createOcrEngine()

    val languages = mutableSetOf<Language>()
    languages.add(Language.ENG)

    ocrRecognizer.setOcrConfig(
            OcrEngine.OcrConfig(
                    engineMode = OcrEngine.EngineMode.TESSERACT,
                    languages = languages,
            )
    )
    // @EndTag("Engine Mode Tesseract")
}

fun runOcrOnUrisSnippet(ocrEngine: OcrEngine) {
    // @Tag("Run OCR from images")
    val imageFileUris: List<Uri> = listOf() // ["file:///some/path/file1.jpg", "file:///some/path/file2.jpg", ...]

    var result: OcrResult = ocrEngine.recognizeFromUris(imageFileUris, false)
    // @EndTag("Run OCR from images")
}

fun runOcrOnDocumentSnippet(ocrEngine: OcrEngine, yourDocument: Document) {
    // @Tag("Run OCR from Document")
    val document: Document = yourDocument

    var result: OcrResult = ocrEngine.recognizeFromDocument(document)
    // @EndTag("Run OCR from Document")
}

fun ocrResultHandlingSnippet(result: OcrResult) {
    // @Tag("OCR Result Handling")
    val text: String = result.recognizedText // recognized plain text

    // bounding boxes and text results of recognized paragraphs, lines and words (as example for the first page):
    val firstBlock: List<Block> = result.ocrPages[0].blocks
    val linesInFirstBlock: List<Line> = result.ocrPages[0].blocks[0].lines
    val wordsInFirstLine: List<Word> = result.ocrPages[0].blocks[0].lines[0].words
    // @EndTag("OCR Result Handling")
}

fun generatePdfWithOcrLayerSnippet(scanbotSDK: ScanbotSDK, document: Document) {
    // @Tag("Creating a PDF from a Document")
    // Create instance of PdfGenerator
    val pdfGenerator = scanbotSDK.createPdfGenerator()

    val pdfConfig = PdfConfiguration(
            attributes = PdfAttributes(
                    author = "",
                    title = "",
                    subject = "",
                    keywords = "",
                    creator = ""
            ),
            pageSize = PageSize.A4,
            pageDirection = PageDirection.AUTO,
            dpi = 200,
            jpegQuality = 100,
            pageFit = PageFit.NONE,
            resamplingMethod = ResamplingMethod.NONE,
    )
    val ocrConfig = OcrEngine.OcrConfig(
            engineMode = OcrEngine.EngineMode.SCANBOT_OCR
    )
    val pdfGenerated = pdfGenerator.generateWithOcrFromDocument(
            document = document,
            pdfConfig = pdfConfig,
            ocrConfig = ocrConfig
    )
    val pdfFile = document.pdfUri.toFile()
    if (pdfGenerated && pdfFile.exists()) {
        // Do something with the PDF file
    } else {
        Log.e("PdfWithOcrFromDocument", "Failed to create PDF")
    }
    // @EndTag("Creating a PDF from a Document")
}

fun generatePdfWithOcrLayerFormUrisSnippet(scanbotSDK: ScanbotSDK, imageFileUris: List<Uri>) {
    // @Tag("Creating a PDF from images with OCR")
    // Create instance of PdfGenerator
    val pdfGenerator = scanbotSDK.createPdfGenerator()

    val pdfConfig = PdfConfiguration(
            attributes = PdfAttributes(
                    author = "",
                    title = "",
                    subject = "",
                    keywords = "",
                    creator = ""
            ),
            pageSize = PageSize.A4,
            pageDirection = PageDirection.AUTO,
            dpi = 200,
            jpegQuality = 100,
            pageFit = PageFit.NONE,
            resamplingMethod = ResamplingMethod.NONE,
    )
    val ocrConfig = OcrEngine.OcrConfig(
            engineMode = OcrEngine.EngineMode.SCANBOT_OCR
    )
    val generatedPdfFile = pdfGenerator.generateWithOcrFromUris(
            imageFileUris = imageFileUris,
            pdfConfig = pdfConfig,
            ocrConfig = ocrConfig
    )
    if (generatedPdfFile != null) {
        // Do something with the PDF file
    } else {
        Log.e("PdfWithOcrFromImages", "Failed to create PDF")
    }
    // @EndTag("Creating a PDF from images with OCR")
}