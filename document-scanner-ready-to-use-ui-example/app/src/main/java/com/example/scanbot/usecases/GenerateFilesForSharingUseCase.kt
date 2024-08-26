package com.example.scanbot.usecases

import com.example.scanbot.sharing.ISharingDocumentStorage
import java.io.File
import java.io.IOException
import java.util.UUID

abstract class GenerateFilesForSharingUseCase constructor(
    private val sharingDocumentStorage: ISharingDocumentStorage,
) {
    @Throws(IOException::class)
    suspend fun generate(pages: List<String>): List<File> {
        val documentId = UUID.randomUUID().toString()
        val documentSharingDir = sharingDocumentStorage.getDocumentSharingDir(documentId)
        if (documentSharingDir.exists()) {
            documentSharingDir.deleteRecursively()
        }

        return generateFilesForDocument(documentSharingDir, pages)
    }

    protected abstract suspend fun generateFilesForDocument(
        documentSharingDir: File,
        pages: List<String>
    ): List<File>
}
