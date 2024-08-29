package com.example.scanbot.usecases

import androidx.core.net.toFile
import com.example.scanbot.sharing.ISharingDocumentStorage
import com.example.scanbot.sharing.ensureFileExists
import io.scanbot.sdk.docprocessing.Document
import io.scanbot.sdk.persistence.page.PageFileStorage
import io.scanbot.sdk.persistence.page.PageFileType
import java.io.File
import javax.inject.Inject

class GenerateJpgForSharingUseCase @Inject constructor(
    sharingDocumentStorage: ISharingDocumentStorage,
    private val pageFileStorage: PageFileStorage,
) : GenerateFilesForSharingUseCase(sharingDocumentStorage) {

    override suspend fun generateFilesForDocument(documentSharingDir: File, document: Document): List<File> {
        return document.pageIds().mapIndexed { index, pageId ->
            val pageFileName = if (document.pageCount == 1) "${pageId}.jpg" else "$pageId (${index + 1}).jpg"
            val sharingJpgFile = documentSharingDir.ensureFileExists().resolve(pageFileName)

            val documentImage = pageFileStorage.getImageURI(pageId, document.uuid, PageFileType.DOCUMENT)
            documentImage.toFile().copyTo(sharingJpgFile, overwrite = true)
           sharingJpgFile
        }
    }
}
