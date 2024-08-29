package com.example.scanbot.usecases

import androidx.core.net.toFile
import com.example.scanbot.sharing.ISharingDocumentStorage
import com.example.scanbot.sharing.ensureFileExists
import io.scanbot.sdk.docprocessing.Document
import io.scanbot.sdk.persistence.page.PageFileStorage
import io.scanbot.sdk.persistence.page.PageFileType
import io.scanbot.sdk.tiff.TIFFWriter
import io.scanbot.sdk.tiff.model.TIFFImageWriterParameters
import java.io.File
import javax.inject.Inject

class GenerateTiffForSharingUseCase @Inject constructor(
    sharingDocumentStorage: ISharingDocumentStorage,
    private val pageFileStorage: PageFileStorage,
    private val tiffWriter: TIFFWriter,
) : GenerateFilesForSharingUseCase(sharingDocumentStorage) {

    override suspend fun generateFilesForDocument(documentSharingDir: File, document: Document): List<File> {
        val sharingTiffFile = documentSharingDir.ensureFileExists().resolve("${documentSharingDir.name}.tiff")

        val originalPagesFiles = document.pageIds().map { pageId ->
            val originalPageUri = pageFileStorage.getImageURI(pageId, document.uuid, PageFileType.DOCUMENT)
            originalPageUri.toFile()
        }

        tiffWriter.writeTIFF(
            sourceImages = originalPagesFiles.toTypedArray(),
            targetFile = sharingTiffFile,
            sourceFilesEncrypted = false,
            parameters = TIFFImageWriterParameters.defaultParametersForBinaryImages()
        )

        return listOf(sharingTiffFile)
    }
}
