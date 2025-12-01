package io.scanbot.example.doc_code_snippet.storage

/*
    NOTE: this snippet of code is to be used only as a part of the website documentation.
    This code is not intended for any use outside of the support of documentation by Scanbot SDK GmbH employees.
*/

// NOTE for maintainers: whenever changing this code,
// ensure that links using it are still pointing to valid lines!
// Pay attention to imports adding/removal/sorting!
// Page URLs using this code:
// TODO: add URLs here

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.example.*
import io.scanbot.sdk.*
import io.scanbot.sdk.persistence.fileio.*
import java.io.File

fun initializeCustomDir(application: Application) {
    // @Tag("Custom storage directory")
    // Example for using a sub-folder in the external(!) storage:
    val customStorageDir = File(application.getExternalFilesDir(null), "my-custom-storage-dir")
    customStorageDir.mkdirs()

    ScanbotSDKInitializer()
        .sdkFilesDirectory(application, customStorageDir)
    .initialize(application)
    // @EndTag("Custom storage directory")
}

fun defaultEncryption(application: Application) {
    // @Tag("Default encryption setup")
    ScanbotSDKInitializer()
        // ...
        .useFileEncryption(true)
        .initialize(application)
    // @EndTag("Default encryption setup")
}

fun customEncryption(application: Application) {
    // @Tag("Encryption setup")
    ScanbotSDKInitializer()
        // ...
        .useFileEncryption(
            true,
            AESEncryptedFileIOProcessor(
                "any_user_password",
                AESEncryptedFileIOProcessor.AESEncrypterMode.AES256
            )
        )
        .initialize(application)
    // @EndTag("Encryption setup")
}

fun publicEncryptionParams(context: Context) {
    // @Tag("Public encryption parameters")
    val scanbotSDK = ScanbotSDK(context)
    val aesEncryptedFileIOProcessor = scanbotSDK.fileIOProcessor() as AESEncryptedFileIOProcessor

    val generatedKey = aesEncryptedFileIOProcessor.key
    val initialSalt = aesEncryptedFileIOProcessor.salt
    val initialIterationCounts = aesEncryptedFileIOProcessor.iterationCount
    val initialIV = aesEncryptedFileIOProcessor.initializationVector
    // @EndTag("Public encryption parameters")
}

fun aesGcmEncryptedFileIoProcessor(application: Application) {
    ScanbotSDKInitializer()
        // ...
        // @Tag("AesGcmEncryptedFileIoProcessor")
        .useFileEncryption(enableFileEncryption = true, fileIOProcessor = AesGcmEncryptedFileIoProcessor(object :
            AesGcmKeyProvider {
            override fun getAesKeyForContext(
                file: String,
                keyMode: AesGcmEncryptedFileIoProcessor.AESGCMEncrypterMode,
            ): ByteArray {
                val key = "random_key_for_file_encryption".toByteArray()
                return key // return the key for the file
            }
        }))
        // @EndTag("AesGcmEncryptedFileIoProcessor")
        .initialize(application)
}

fun publicEncryptionParams(context: Context, source: File) {
    // @Tag("Image decryption")
    val scanbotSDK = ScanbotSDK(context)
    val fileIOProcessor = scanbotSDK.fileIOProcessor()
    val decryptedImageBitmap: Bitmap? = fileIOProcessor.readImage(source = source, options = null)
    // @EndTag("Image decryption")
}