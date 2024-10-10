package com.example.scanbot.usecases

import android.graphics.Bitmap
import com.example.scanbot.sharing.ISharingDocumentStorage
import com.example.scanbot.sharing.ensureFileExists
import io.scanbot.sdk.docprocessing.Document
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class GeneratePngForSharingUseCase @Inject constructor(
    sharingDocumentStorage: ISharingDocumentStorage,
) : GenerateFilesForSharingUseCase(sharingDocumentStorage) {

    override suspend fun generateFilesForDocument(documentSharingDir: File, document: Document): List<File> {

        return document.pageIds().mapIndexed { index, pageId ->
            val pageFileName = if (document.pageCount == 1) "${pageId}.png" else "$pageId (${index + 1}).png"
            val sharingPngFile = documentSharingDir.ensureFileExists().resolve(pageFileName)

            val documentImage = document.pages[index].documentImage

            FileOutputStream(sharingPngFile).use {
                documentImage?.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            sharingPngFile
        }
    }
}
