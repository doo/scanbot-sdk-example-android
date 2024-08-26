package com.example.scanbot.sharing

import java.io.File
import java.io.IOException

interface ISharingDocumentStorage {
    @Throws(IOException::class)
    fun getSharingDir(): File

    @Throws(IOException::class)
    fun getDocumentSharingDir(documentId: String): File
}
