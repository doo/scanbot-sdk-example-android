package com.example.scanbot.usecases

import androidx.core.net.toFile
import com.example.scanbot.sharing.ISharingDocumentStorage
import com.example.scanbot.sharing.ensureFileExists
import io.scanbot.sdk.persistence.PageFileStorage
import java.io.File
import javax.inject.Inject

class GenerateJpgForSharingUseCase @Inject constructor(
    sharingDocumentStorage: ISharingDocumentStorage,
    private val pageFileStorage: PageFileStorage
) : GenerateFilesForSharingUseCase(sharingDocumentStorage) {
    override suspend fun generateFilesForDocument(documentSharingDir: File, pages: List<String>): List<File> {
        return pages.mapIndexed { index, page ->
            val pageFileName = if (pages.size == 1) "${page}.jpg" else "$page (${index + 1}).jpg"
            val sharingJpgFile = documentSharingDir.ensureFileExists().resolve(pageFileName)

            val documentImage = pageFileStorage.getImageURI(page, PageFileStorage.PageFileType.DOCUMENT)
            documentImage.toFile().copyTo(sharingJpgFile, overwrite = true)
           sharingJpgFile
        }
    }

}
