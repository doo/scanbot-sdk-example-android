package com.example.scanbot.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import com.example.scanbot.sharing.SharingDocumentStorage
import io.scanbot.sdk.persistence.fileio.FileIOProcessor
import io.scanbot.sdk.util.thread.MimeUtils
import java.io.File
import java.io.FileOutputStream

object ExampleUtils {
    fun openBrowser(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }

    fun openDocument(context: Context, pdfFile: File) {
        val uriForFile = androidx.core.content.FileProvider.getUriForFile(
            context,
            context.applicationContext.packageName + ".provider", pdfFile
        )
        val openIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uriForFile)
            type = MimeUtils.getMimeByName(pdfFile.name)
        }
        openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        if (openIntent.resolveActivity(context.packageManager) != null) {
            val chooser = Intent.createChooser(openIntent, pdfFile.name)
            val resInfoList = context.packageManager.queryIntentActivities(
                chooser,
                PackageManager.MATCH_DEFAULT_ONLY
            )

            for (resolveInfo in resInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                context.grantUriPermission(
                    packageName,
                    uriForFile,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            context.startActivity(chooser)
        } else {
            Toast.makeText(
                context,
                "Error Opening Document!",
                Toast.LENGTH_LONG
            )
                .show()
        }
    }

    fun showEncryptedDocumentToast(
        context: Context,
        pdfFile: File,
        fileIOProcessor: FileIOProcessor
    ) {
        val stream = fileIOProcessor.openFileInputStream(pdfFile)
        stream.use { inputStream ->
            val unEncryptedFile = SharingDocumentStorage(context).getDocumentSharingDir(pdfFile.name)
                .resolve("unencrypted.pdf")
            FileOutputStream(unEncryptedFile).use { outputStream ->
                inputStream?.copyTo(outputStream)
            }
            openDocument(context, unEncryptedFile)
        }
        Toast.makeText(
            context,
            "Encrypted document saved!",
            Toast.LENGTH_LONG
        ).show()
    }
}