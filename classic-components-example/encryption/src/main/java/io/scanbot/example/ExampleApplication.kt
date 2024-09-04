package io.scanbot.example

import android.app.Application
import io.scanbot.sap.SdkFeature
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.ScanbotSDKInitializer
import io.scanbot.sdk.persistence.fileio.AESEncryptedFileIOProcessor
import io.scanbot.sdk.util.log.LoggerProvider

class ExampleApplication : Application() {
    /*
     * TODO: Add the Scanbot SDK license key here.
     * Please note: The Scanbot SDK will run without a license key for one minute per session!
     * After the trial period is over all Scanbot SDK functions as well as the UI components will stop working.
     * You can get an unrestricted "no-strings-attached" 30 day trial license key for free.
     * Please submit the trial license form (https://scanbot.io/trial/) on our website by using
     * the app identifier "io.scanbot.example.sdk.android" of this example app.
     */
    val licenseKey = ""

    companion object {
        // TODO: you can enable encryption of all the image files and generated PDFs by changing this property
        const val USE_ENCRYPTION = true

        // TODO: you should store a password in a secure place or let the user enter it manually
        private const val ENCRYPTION_PASSWORD = "password"

        // TODO: you can select an encryption method
        private val ENCRYPTION_METHOD = AESEncryptedFileIOProcessor.AESEncrypterMode.AES256
    }

    override fun onCreate() {
        super.onCreate()

        ScanbotSDKInitializer()
            .withLogging(true)
            // TODO 2/2: Enable the Scanbot SDK license key
            //.license(this, licenseKey)
            .licenceErrorHandler { status, feature, statusMessage ->
                LoggerProvider.logger.d("ExampleApplication", "+++> License status: ${status.name}. Status message: $statusMessage")
                if (feature != SdkFeature.NoSdkFeature) {
                    LoggerProvider.logger.d("ExampleApplication", "+++> Feature not available: ${feature.name}")
                }
            }
            .useFileEncryption(USE_ENCRYPTION, AESEncryptedFileIOProcessor(ENCRYPTION_PASSWORD, ENCRYPTION_METHOD))
            //.sdkFilesDirectory(this, getExternalFilesDir(null)!!)
            .initialize(this)

        LoggerProvider.logger.d("ExampleApplication", "Scanbot SDK was initialized")

        val licenseInfo = ScanbotSDK(this).licenseInfo
        LoggerProvider.logger.d("ExampleApplication", "License status: ${licenseInfo.status}")
        LoggerProvider.logger.d("ExampleApplication", "License isValid: ${licenseInfo.isValid}")
        LoggerProvider.logger.d("ExampleApplication", "License expirationDate: ${licenseInfo.expirationDate}")
    }
}
