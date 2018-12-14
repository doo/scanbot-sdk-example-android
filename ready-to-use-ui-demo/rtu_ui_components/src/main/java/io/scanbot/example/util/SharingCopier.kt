package io.scanbot.example.util

import android.content.Context
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException

class SharingCopier {

    companion object {
        private const val SNAPPING_DOCUMENTS_DIR_NAME = "snapping_documents"

        @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        fun clear(context: Context){
            val destPath = File("${context.getExternalFilesDir(null).path}/$SNAPPING_DOCUMENTS_DIR_NAME")
            if (!destPath.exists()){
                destPath.mkdirs()
            }
            FileUtils.cleanDirectory(destPath)
        }

        /**
         * @return [java.io.File] directory where documents will be stored or `null` if not possible to create
         * @throws java.io.IOException if directory can't be opened
         */
        @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        @Throws(IOException::class)
        fun copyFile(context: Context, file: File): File {
            val destPath = File("${context.getExternalFilesDir(null).path}/$SNAPPING_DOCUMENTS_DIR_NAME")
            val destFile = File("${context.getExternalFilesDir(null).path}/$SNAPPING_DOCUMENTS_DIR_NAME/${file.name}")
            if (!destPath.exists()){
                destPath.mkdirs()
            }
            FileUtils.copyFile(file, destFile)
            return destFile
        }

    }

}