package io.scanbot.example.doc_code_snippet.storage

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.example.*
import io.scanbot.sdk.*
import io.scanbot.sdk.camera.*
import io.scanbot.sdk.licenseplate.*
import io.scanbot.sdk.persistence.fileio.*
import io.scanbot.sdk.ui.camera.*
import io.scanbot.sdk.ui.view.licenseplate.*
import io.scanbot.sdk.ui.view.licenseplate.configuration.LicensePlateScannerConfiguration
import io.scanbot.sdk.ui.view.licenseplate.entity.LicensePlateScannerResult
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

fun publicEncryptionParams(context: Context, source: File) {
    // @Tag("Image decryption")
    val scanbotSDK = ScanbotSDK(context)
    val fileIOProcessor = scanbotSDK.fileIOProcessor()
    val decryptedImageBitmap: Bitmap? = fileIOProcessor.readImage(source = source, options = null)
    // @EndTag("Image decryption")
}