package io.scanbot.example.util

import android.content.Context
import java.io.File

@Deprecated("Leaving as is to clean end-users' storage for next several updates.")
class SharingCopier {

    companion object {

        fun clear(context: Context) {
            sharedDocsDirectory(context)?.deleteRecursively()
        }

        private fun sharedDocsDirectory(context: Context): File? {
            val dir = File(context.getExternalFilesDir(null), "my-shared-documents")
            return if (dir.exists()) dir else null
        }
    }
}
