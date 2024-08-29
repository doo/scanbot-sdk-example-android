package com.example.scanbot.usecases

import android.graphics.Bitmap
import com.example.scanbot.sharing.ISharingDocumentStorage
import com.example.scanbot.sharing.ensureFileExists
import io.scanbot.sdk.docprocessing.Document
import io.scanbot.sdk.persistence.page.PageFileStorage
import io.scanbot.sdk.persistence.page.PageFileType
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class GeneratePngForSharingUseCase @Inject constructor(
    sharingDocumentStorage: ISharingDocumentStorage,
    private val pageFileStorage: PageFileStorage,
) : GenerateFilesForSharingUseCase(sharingDocumentStorage) {

    override suspend fun generateFilesForDocument(documentSharingDir: File, document: Document): List<File> {

        return document.pageIds().mapIndexed { index, pageId ->
            val pageFileName = if (document.pageCount == 1) "${pageId}.png" else "$pageId (${index + 1}).png"
            val sharingPngFile = documentSharingDir.ensureFileExists().resolve(pageFileName)

            val documentImage =
                pageFileStorage.getImage(pageId, document.uuid, PageFileType.DOCUMENT)

            FileOutputStream(sharingPngFile).use {
                documentImage?.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            sharingPngFile
        }
    }
}
