package com.example.scanbot.usecases

import com.example.scanbot.sharing.ISharingDocumentStorage
import com.example.scanbot.sharing.ensureFileExists
import io.scanbot.sdk.docprocessing.Document
import io.scanbot.sdk.tiff.TIFFWriter
import io.scanbot.tiffwriter.model.TiffWriterParameters
import java.io.File
import javax.inject.Inject

class GenerateTiffForSharingUseCase @Inject constructor(
    sharingDocumentStorage: ISharingDocumentStorage,
    private val tiffWriter: TIFFWriter,
) : GenerateFilesForSharingUseCase(sharingDocumentStorage) {

    override suspend fun generateFilesForDocument(documentSharingDir: File, document: Document): List<File> {
        val sharingTiffFile = documentSharingDir.ensureFileExists().resolve("${documentSharingDir.name}.tiff")

        tiffWriter.writeTIFF(
            document = document,
            targetFile = sharingTiffFile,
            parameters = TiffWriterParameters.default(),
        )

        return listOf(sharingTiffFile)
    }
}
