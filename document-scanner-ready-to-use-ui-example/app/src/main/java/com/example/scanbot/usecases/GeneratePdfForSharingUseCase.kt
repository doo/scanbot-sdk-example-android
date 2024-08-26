package com.example.scanbot.usecases

import com.example.scanbot.sharing.ISharingDocumentStorage
import com.example.scanbot.sharing.ensureFileExists
import io.scanbot.pdf.model.PageSize
import io.scanbot.pdf.model.PdfConfig
import io.scanbot.sdk.core.contourdetector.DetectionStatus
import io.scanbot.sdk.persistence.Page
import io.scanbot.sdk.process.PDFRenderer
import io.scanbot.sdk.util.PolygonHelper
import java.io.File
import javax.inject.Inject

class GeneratePdfForSharingUseCase @Inject constructor(
    sharingDocumentStorage: ISharingDocumentStorage,
    private val pdfRenderer: PDFRenderer
) : GenerateFilesForSharingUseCase(sharingDocumentStorage) {
    override suspend fun generateFilesForDocument(
        documentSharingDir: File,
        pages: List<String>
    ): List<File> {
        val renderedPdfDocumentFile = pdfRenderer.renderDocumentFromPages(pages.map { pageId ->
            Page(
                pageId,
                PolygonHelper.getFullPolygon(),
                DetectionStatus.OK
            )
        }, PdfConfig.defaultConfig().copy(pageSize = PageSize.A4))

        val sharingPdfFile =
            documentSharingDir.ensureFileExists().resolve("${documentSharingDir.name}.pdf")
        renderedPdfDocumentFile?.copyTo(sharingPdfFile, overwrite = true)
        renderedPdfDocumentFile?.delete()
        return listOf(sharingPdfFile)
    }
}
