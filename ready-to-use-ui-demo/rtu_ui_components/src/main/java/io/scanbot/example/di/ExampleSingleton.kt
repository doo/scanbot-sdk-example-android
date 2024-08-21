package io.scanbot.example.di

import android.content.Context
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.docprocessing.PdfPagesExtractor
import io.scanbot.sdk.docprocessing.legacy.PageProcessor
import io.scanbot.sdk.pdf.PdfImagesExtractor
import io.scanbot.sdk.persistence.page.legacy.PageFileStorage

/**
 * This singleton is used only for simplicity. Please, use Dagger or other DI framework in production code
 */
interface ExampleSingleton {
    fun pageFileStorageInstance(): PageFileStorage
    fun pageProcessorInstance(): PageProcessor
    fun pagePdfExtractorInstance(): PdfPagesExtractor
}

/**
 * This singleton is used only for simplicity. Please, use Dagger or other DI framework in production code
 */
class ExampleSingletonImpl(val context: Context) : ExampleSingleton {
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

    companion object {
        private var pageProcessor: PageProcessor? = null
        private var pageFileStorage: PageFileStorage? = null
        private var pdfExtractor: PdfPagesExtractor? = null
    }
}