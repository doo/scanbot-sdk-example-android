package com.example.scanbot.usecases

import com.example.scanbot.sharing.ISharingDocumentStorage
import com.example.scanbot.sharing.ensureFileExists
import io.scanbot.sdk.docprocessing.Document
import java.io.File
import javax.inject.Inject
import io.scanbot.sdk.tiff.TiffGenerator
import io.scanbot.sdk.tiff.model.TiffGeneratorParameters

class GenerateTiffForSharingUseCase @Inject constructor(
    sharingDocumentStorage: ISharingDocumentStorage,
    private val tiffGenerator: TiffGenerator,
) : GenerateFilesForSharingUseCase(sharingDocumentStorage) {

    override suspend fun generateFilesForDocument(documentSharingDir: File, document: Document): List<File> {
        val sharingTiffFile = documentSharingDir.ensureFileExists().resolve("${documentSharingDir.name}.tiff")

        tiffGenerator.generateFromDocument(
            document = document,
            targetFile = sharingTiffFile,
            parameters = TiffGeneratorParameters.default(),
        )

        return listOf(sharingTiffFile)
    }
}
