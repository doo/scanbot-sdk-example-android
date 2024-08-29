package com.example.scanbot.usecases

import com.example.scanbot.sharing.ISharingDocumentStorage
import io.scanbot.sdk.docprocessing.Document
import java.io.File
import java.io.IOException
import java.util.UUID

abstract class GenerateFilesForSharingUseCase(private val sharingDocumentStorage: ISharingDocumentStorage) {

    @Throws(IOException::class)
    suspend fun generate(document: Document): List<File> {
        val documentId = UUID.randomUUID().toString()
        val documentSharingDir = sharingDocumentStorage.getDocumentSharingDir(documentId)
        if (documentSharingDir.exists()) {
            documentSharingDir.deleteRecursively()
        }

        return generateFilesForDocument(documentSharingDir, document)
    }

    protected abstract suspend fun generateFilesForDocument(documentSharingDir: File, document: Document): List<File>
}
