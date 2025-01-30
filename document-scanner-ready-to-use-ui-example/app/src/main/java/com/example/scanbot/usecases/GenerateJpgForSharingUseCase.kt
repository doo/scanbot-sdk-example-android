package com.example.scanbot.usecases

import android.graphics.Bitmap
import androidx.core.net.toFile
import com.example.scanbot.sharing.ISharingDocumentStorage
import com.example.scanbot.sharing.ensureFileExists
import io.scanbot.sdk.docprocessing.Document
import io.scanbot.sdk.persistence.fileio.FileIOProcessor
import java.io.File
import javax.inject.Inject

class GenerateJpgForSharingUseCase @Inject constructor(
    sharingDocumentStorage: ISharingDocumentStorage,
    val fileIOProcessor: FileIOProcessor
) : GenerateFilesForSharingUseCase(sharingDocumentStorage) {

    override suspend fun generateFilesForDocument(documentSharingDir: File, document: Document): List<File> {
        return document.pageIds().mapIndexed { index, pageId ->
            val pageFileName = if (document.pageCount == 1) "${pageId}.jpg" else "$pageId (${index + 1}).jpg"
            val sharingJpgFile = documentSharingDir.ensureFileExists().resolve(pageFileName)


            val documentImage = document.pages[index].documentImage
            fileIOProcessor.openFileOutputStream(sharingJpgFile)?.use { fileOutputStream ->
                documentImage?.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            }

            sharingJpgFile
        }
    }
}
