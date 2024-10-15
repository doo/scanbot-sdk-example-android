package com.example.scanbot.usecases

import androidx.core.net.toFile
import com.example.scanbot.sharing.ISharingDocumentStorage
import com.example.scanbot.sharing.ensureFileExists
import io.scanbot.sdk.docprocessing.Document
import java.io.File
import javax.inject.Inject

class GenerateJpgForSharingUseCase @Inject constructor(
    sharingDocumentStorage: ISharingDocumentStorage,
) : GenerateFilesForSharingUseCase(sharingDocumentStorage) {

    override suspend fun generateFilesForDocument(documentSharingDir: File, document: Document): List<File> {
        return document.pageIds().mapIndexed { index, pageId ->
            val pageFileName = if (document.pageCount == 1) "${pageId}.jpg" else "$pageId (${index + 1}).jpg"
            val sharingJpgFile = documentSharingDir.ensureFileExists().resolve(pageFileName)

            document.pages[index].documentFileUri.toFile().copyTo(sharingJpgFile, overwrite = true)
            sharingJpgFile
        }
    }
}
