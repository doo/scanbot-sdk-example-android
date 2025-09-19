package com.example.scanbot.usecases

import com.example.scanbot.sharing.ISharingDocumentStorage
import com.example.scanbot.sharing.ensureFileExists
import io.scanbot.sdk.docprocessing.Document
import java.io.File
import javax.inject.Inject
import io.scanbot.sdk.pdfgeneration.PageSize
import io.scanbot.sdk.pdfgeneration.PdfConfiguration
import io.scanbot.sdk.process.PdfGenerator

class GeneratePdfForSharingUseCase @Inject constructor(
    sharingDocumentStorage: ISharingDocumentStorage,
    private val pdfGenerator: PdfGenerator,
) : GenerateFilesForSharingUseCase(sharingDocumentStorage) {

    override suspend fun generateFilesForDocument(documentSharingDir: File, document: Document): List<File> {
        val sharingPdfFile =
            documentSharingDir.ensureFileExists().resolve("${documentSharingDir.name}.pdf")
        pdfGenerator.generateFromDocument(document, sharingPdfFile, PdfConfiguration.default().copy(pageSize = PageSize.A4))
        return listOf(sharingPdfFile)
    }
}
