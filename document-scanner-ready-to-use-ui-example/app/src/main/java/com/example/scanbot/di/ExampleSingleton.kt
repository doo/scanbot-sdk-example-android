package com.example.scanbot.di

import android.content.Context
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.docprocessing.PageProcessor
import io.scanbot.sdk.docprocessing.PdfPagesExtractor
import io.scanbot.sdk.ocr.OpticalCharacterRecognizer
import io.scanbot.sdk.persistence.PageFileStorage
import io.scanbot.sdk.persistence.fileio.FileIOProcessor
import io.scanbot.sdk.process.DocumentQualityAnalyzer
import io.scanbot.sdk.process.PDFRenderer
import io.scanbot.sdk.tiff.TIFFWriter

/**
 * This singleton is used only for simplicity. Please, use Dagger or other DI framework in production code
 */
interface ExampleSingleton {
    fun pageFileStorageInstance(): PageFileStorage
    fun pageProcessorInstance(): PageProcessor
    fun pagePdfExtractorInstance(): PdfPagesExtractor
    fun pageOpticalCharacterRecognizer(): OpticalCharacterRecognizer
    fun pagePDFRenderer(): PDFRenderer
    fun pageTIFFWriter(): TIFFWriter
    fun pageDocQualityAnalyzer(): DocumentQualityAnalyzer
    fun fileIOProcessor(): FileIOProcessor

}

/**
 * This singleton is used only for simplicity. Please, use Dagger or other DI framework in production code
 */
class ExampleSingletonImpl(private val context: Context) : ExampleSingleton {
    override fun pageFileStorageInstance(): PageFileStorage {
        if (pageFileStorage == null) {
            pageFileStorage = ScanbotSDK(context.applicationContext).createPageFileStorage()
        }
        return pageFileStorage!!
    }

    override fun pageProcessorInstance(): PageProcessor {
        if (pageProcessor == null) {
            pageProcessor = ScanbotSDK(context.applicationContext).createPageProcessor()
        }
        return pageProcessor!!
    }

    override fun pagePdfExtractorInstance(): PdfPagesExtractor {
        if (pdfExtractor == null) {
            pdfExtractor = ScanbotSDK(context.applicationContext).createPdfPagesExtractor()
        }
        return pdfExtractor!!
    }

    override fun pageOpticalCharacterRecognizer(): OpticalCharacterRecognizer {
        if (textRecognition == null) {
            textRecognition = ScanbotSDK(context.applicationContext).createOcrRecognizer()
        }
        return textRecognition!!
    }

    override fun pagePDFRenderer(): PDFRenderer {
        if (pdfRenderer == null) {
            pdfRenderer = ScanbotSDK(context.applicationContext).createPdfRenderer()
        }
        return pdfRenderer!!
    }

    override fun pageTIFFWriter(): TIFFWriter {
        if (tiffWriter == null) {
            tiffWriter = ScanbotSDK(context.applicationContext).createTiffWriter()
        }
        return tiffWriter!!
    }

    override fun pageDocQualityAnalyzer(): DocumentQualityAnalyzer {
        if (documentQualityAnalyzer == null) {
            documentQualityAnalyzer = ScanbotSDK(context.applicationContext).createDocumentQualityAnalyzer()
        }
        return documentQualityAnalyzer!!
    }

    override fun fileIOProcessor(): FileIOProcessor {
        if (fileIOProcessor == null) {
            fileIOProcessor = ScanbotSDK(context.applicationContext).fileIOProcessor()
        }
        return fileIOProcessor!!
    }

    companion object {
        private var pageProcessor: PageProcessor? = null
        private var pageFileStorage: PageFileStorage? = null
        private var pdfExtractor: PdfPagesExtractor? = null
        private var textRecognition: OpticalCharacterRecognizer? = null
        private var pdfRenderer: PDFRenderer? = null
        private var tiffWriter: TIFFWriter? = null
        private var documentQualityAnalyzer: DocumentQualityAnalyzer? = null
        private var fileIOProcessor: FileIOProcessor? = null
    }
}