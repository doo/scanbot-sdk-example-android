package com.example.scanbot.usecases

import androidx.core.net.toFile
import com.example.scanbot.sharing.ISharingDocumentStorage
import com.example.scanbot.sharing.ensureFileExists
import io.scanbot.sdk.persistence.PageFileStorage
import io.scanbot.sdk.tiff.TIFFWriter
import io.scanbot.sdk.tiff.model.TIFFImageWriterParameters
import java.io.File
import javax.inject.Inject

class GenerateTiffForSharingUseCase @Inject constructor(
    sharingDocumentStorage: ISharingDocumentStorage,
    private val pageFileStorage: PageFileStorage,
    private val tiffWriter: TIFFWriter
) : GenerateFilesForSharingUseCase(sharingDocumentStorage) {
    override suspend fun generateFilesForDocument(
        documentSharingDir: File,
        pages: List<String>
    ): List<File> {
        val sharingTiffFile =
            documentSharingDir.ensureFileExists().resolve("${documentSharingDir.name}.tiff")

        val originalPagesFiles = pages.map { pageId ->
            val originalPageUri =
                pageFileStorage.getImageURI(pageId, PageFileStorage.PageFileType.DOCUMENT)
            originalPageUri.toFile()
        }

        tiffWriter.writeTIFFFromFiles(
            sourceFiles = originalPagesFiles,
            targetFile = sharingTiffFile,
            sourceFilesEncrypted = false,
            parameters = TIFFImageWriterParameters.defaultParametersForBinaryImages()
        )

        return listOf(sharingTiffFile)
    }
}
