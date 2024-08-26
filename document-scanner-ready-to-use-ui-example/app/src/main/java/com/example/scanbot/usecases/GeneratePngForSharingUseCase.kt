package com.example.scanbot.usecases

import android.graphics.Bitmap
import com.example.scanbot.sharing.ISharingDocumentStorage
import com.example.scanbot.sharing.ensureFileExists
import io.scanbot.sdk.persistence.PageFileStorage
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class GeneratePngForSharingUseCase @Inject constructor(
    sharingDocumentStorage: ISharingDocumentStorage,
    private val pageFileStorage: PageFileStorage
) : GenerateFilesForSharingUseCase(sharingDocumentStorage) {
    override suspend fun generateFilesForDocument(
        documentSharingDir: File,
        pages: List<String>
    ): List<File> {
        return pages.mapIndexed { index, page ->
            val pageFileName = if (pages.size == 1) "${page}.png" else "$page (${index + 1}).png"
            val sharingPngFile = documentSharingDir.ensureFileExists().resolve(pageFileName)

            val documentImage =
                pageFileStorage.getImage(page, PageFileStorage.PageFileType.DOCUMENT)

            FileOutputStream(sharingPngFile).use {
                documentImage?.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            sharingPngFile
        }
    }
}
