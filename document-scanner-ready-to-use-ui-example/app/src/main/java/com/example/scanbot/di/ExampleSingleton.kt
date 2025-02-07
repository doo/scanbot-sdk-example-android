package com.example.scanbot.di

import android.content.Context
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.docprocessing.PdfPagesExtractor
import io.scanbot.sdk.ocr.OcrEngine
import io.scanbot.sdk.persistence.fileio.FileIOProcessor
import io.scanbot.sdk.persistence.page.PageFileStorage
import io.scanbot.sdk.process.DocumentQualityAnalyzer
import io.scanbot.sdk.process.PdfGenerator
import io.scanbot.sdk.tiff.TiffGenerator

/** This singleton is used only for simplicity. Please, use Hilt or other DI framework in production code. */
interface ExampleSingleton {
    fun pagePdfExtractorInstance(): PdfPagesExtractor
    fun pageOcrEngine(): OcrEngine
    fun pagePDFRenderer(): PdfGenerator
    fun pageTIFFWriter(): TiffGenerator
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

    override fun pageOcrEngine(): OcrEngine {
        if (textRecognition == null) {
            textRecognition = scanbotSdk.createOcrEngine()
        }
        return textRecognition!!
    }

    override fun pagePDFRenderer(): PdfGenerator {
        if (pdfGenerator == null) {
            pdfGenerator = scanbotSdk.createPdfGenerator()
        }
        return pdfGenerator!!
    }

    override fun pageTIFFWriter(): TiffGenerator {
        if (tiffGenerator == null) {
            tiffGenerator = scanbotSdk.createTiffGenerator()
        }
        return tiffGenerator!!
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
        private var textRecognition: OcrEngine? = null
        private var pdfGenerator: PdfGenerator? = null
        private var tiffGenerator: TiffGenerator? = null
        private var documentQualityAnalyzer: DocumentQualityAnalyzer? = null
        private var fileIOProcessor: FileIOProcessor? = null
        private var pageFileStorage: PageFileStorage? = null
    }
}
