package com.example.scanbot.sharing

import android.content.Context
import java.io.File
import java.io.IOException

class SharingDocumentStorage constructor(
    private val context: Context
) : ISharingDocumentStorage {
    override fun getSharingDir(): File {
        return context.externalCacheDir?.resolve("sharing")?.ensureFileExists()
            ?: throw IOException("Unable to create sharing dir")
    }

    override fun getDocumentSharingDir(documentId: String): File {
        return getSharingDir().resolve(documentId).ensureFileExists()
    }
}

fun File.ensureFileExists(): File {
    if (!exists() && !mkdirs()) {
        throw IOException("File doesn't exists or dir is not reachable")
    }
    return this
}
