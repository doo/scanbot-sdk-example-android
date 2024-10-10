package com.example.scanbot.di

import android.content.Context
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.docprocessing.PdfPagesExtractor
import io.scanbot.sdk.ocr.OpticalCharacterRecognizer
import io.scanbot.sdk.persistence.fileio.FileIOProcessor
import io.scanbot.sdk.persistence.page.PageFileStorage
import io.scanbot.sdk.process.DocumentQualityAnalyzer
import io.scanbot.sdk.process.PDFRenderer
import io.scanbot.sdk.tiff.TIFFWriter

/** This singleton is used only for simplicity. Please, use Hilt or other DI framework in production code. */
interface ExampleSingleton {
    fun pagePdfExtractorInstance(): PdfPagesExtractor
    fun pageOpticalCharacterRecognizer(): OpticalCharacterRecognizer
    fun pagePDFRenderer(): PDFRenderer
    fun pageTIFFWriter(): TIFFWriter
    fun pageDocQualityAnalyzer(): DocumentQualityAnalyzer
    fun fileIOProcessor(): FileIOProcessor
    fun pageFileStorage(): PageFileStorage
}

/** This singleton is used only for simplicity. Please, use Hilt or other DI framework in production code. */
class ExampleSingletonImpl(private val context: Context) : ExampleSingleton {

    private val scanbotSdk by lazy { ScanbotSDK(context.applicationContext) }

    override fun pagePdfExtractorInstance(): PdfPagesExtractor {
        if (pdfExtractor == null) {
            pdfExtractor = scanbotSdk.createPdfPagesExtractor()
        }
        return pdfExtractor!!
    }

    override fun pageOpticalCharacterRecognizer(): OpticalCharacterRecognizer {
        if (textRecognition == null) {
            textRecognition = scanbotSdk.createOcrRecognizer()
        }
        return textRecognition!!
    }

    override fun pagePDFRenderer(): PDFRenderer {
        if (pdfRenderer == null) {
            pdfRenderer = scanbotSdk.createPdfRenderer()
        }
        return pdfRenderer!!
    }

    override fun pageTIFFWriter(): TIFFWriter {
        if (tiffWriter == null) {
            tiffWriter = scanbotSdk.createTiffWriter()
        }
        return tiffWriter!!
    }

    override fun pageDocQualityAnalyzer(): DocumentQualityAnalyzer {
        if (documentQualityAnalyzer == null) {
            documentQualityAnalyzer = scanbotSdk.createDocumentQualityAnalyzer()
        }
        return documentQualityAnalyzer!!
    }

    override fun fileIOProcessor(): FileIOProcessor {
        if (fileIOProcessor == null) {
            fileIOProcessor = scanbotSdk.fileIOProcessor()
        }
        return fileIOProcessor!!
    }

    override fun pageFileStorage(): PageFileStorage {
        if (pageFileStorage == null) {
            pageFileStorage = scanbotSdk.getSdkComponent()!!.providePageFileStorage()
        }
        return pageFileStorage!!
    }

    companion object {
        private var pdfExtractor: PdfPagesExtractor? = null
        private var textRecognition: OpticalCharacterRecognizer? = null
        private var pdfRenderer: PDFRenderer? = null
        private var tiffWriter: TIFFWriter? = null
        private var documentQualityAnalyzer: DocumentQualityAnalyzer? = null
        private var fileIOProcessor: FileIOProcessor? = null
        private var pageFileStorage: PageFileStorage? = null
    }
}
