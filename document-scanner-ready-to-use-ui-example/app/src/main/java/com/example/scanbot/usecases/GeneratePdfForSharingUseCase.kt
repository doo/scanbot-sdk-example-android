package com.example.scanbot.usecases

import com.example.scanbot.sharing.ISharingDocumentStorage
import com.example.scanbot.sharing.ensureFileExists
import io.scanbot.pdf.model.PageSize
import io.scanbot.pdf.model.PdfConfig
import io.scanbot.sdk.docprocessing.Document
import io.scanbot.sdk.process.PDFRenderer
import java.io.File
import javax.inject.Inject

class GeneratePdfForSharingUseCase @Inject constructor(
    sharingDocumentStorage: ISharingDocumentStorage,
    private val pdfRenderer: PDFRenderer,
) : GenerateFilesForSharingUseCase(sharingDocumentStorage) {

    override suspend fun generateFilesForDocument(documentSharingDir: File, document: Document): List<File> {
        val sharingPdfFile =
            documentSharingDir.ensureFileExists().resolve("${documentSharingDir.name}.pdf")
        pdfRenderer.render(document, sharingPdfFile, PdfConfig.defaultConfig().copy(pageSize = PageSize.A4))
        return listOf(sharingPdfFile)
    }
}
