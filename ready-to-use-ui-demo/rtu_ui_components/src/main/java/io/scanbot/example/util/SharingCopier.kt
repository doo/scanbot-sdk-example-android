package io.scanbot.example.util

import android.content.Context
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException

class SharingCopier {

    companion object {

        fun clear(context: Context) {
            val sharedDocsDir = sharedDocsDirectory(context)
            FileUtils.cleanDirectory(sharedDocsDir)
        }

        @Throws(IOException::class)
        fun moveFile(context: Context, sourceFile: File): File {
            val destFile = copyFile(context, sourceFile)
            sourceFile.delete()
            return destFile
        }

        @Throws(IOException::class)
        fun copyFile(context: Context, sourceFile: File): File {
            val sharedDocsDir = sharedDocsDirectory(context)
            val destFile = File(sharedDocsDir, sourceFile.name)
            FileUtils.copyFile(sourceFile, destFile)
            return destFile
        }

        @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        private fun sharedDocsDirectory(context: Context): File {
            val dir = File(context.getExternalFilesDir(null), "my-shared-documents")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            return dir
        }

    }

}