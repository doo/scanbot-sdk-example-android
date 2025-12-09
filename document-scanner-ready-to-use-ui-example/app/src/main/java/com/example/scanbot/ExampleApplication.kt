package com.example.scanbot

import android.app.Application
import android.widget.Toast
import io.scanbot.sap.IScanbotSDKLicenseErrorHandler
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.ScanbotSDKInitializer
import io.scanbot.sdk.persistence.CameraImageFormat
import io.scanbot.sdk.persistence.page.PageStorageSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import io.scanbot.sdk.documentscanner.DocumentScannerEngineMode
import io.scanbot.sdk.licensing.LicenseStatus

class ExampleApplication : Application(), CoroutineScope {

    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    companion object {
        /**
         * TODO Add the Scanbot Document Scanner SDK license key here.
         * Please note: Scanbot Document Scanner SDK will run without a license key for one minute per session!
         * After the trial period has expired all SDK features and UI components will stop working.
         * You can get a free "no-strings-attached" trial license key. Please submit the trial license
         * form (https://scanbot.io/trial) on our website by using the app identifier
         * "io.scanbot.example.document.usecases.android" of this example app.
         */
        private const val LICENSE_KEY = "" // "YOUR_SCANBOT_SDK_LICENSE_KEY"
        const val USE_ENCRYPTION = false
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize the Scanbot SDK:
        ScanbotSDKInitializer()
            .withLogging(true)
            .documentScannerEngineMode(DocumentScannerEngineMode.ML)
            .usePageStorageSettings(
                PageStorageSettings.Builder()
                    .imageFormat(CameraImageFormat.JPG)
                    .imageQuality(80)
                    .build()
            )
            .licenseErrorHandler (IScanbotSDKLicenseErrorHandler { status, sdkFeature, errorMessage ->
                when (status) {
                    LicenseStatus.FAILURE_NOT_SET,
                    LicenseStatus.FAILURE_CORRUPTED,
                    LicenseStatus.FAILURE_WRONG_OS,
                    LicenseStatus.FAILURE_APP_ID_MISMATCH,
                    LicenseStatus.FAILURE_EXPIRED -> {
                        Toast.makeText(this, "License error: $status ", Toast.LENGTH_LONG).show()
                    }

                    else -> { /* Can be empty for the purpose of this example. */
                    }
                }

            })
            // .useFileEncryption(USE_ENCRYPTION, AESEncryptedFileIOProcessor("YOUR_ENCRYPTION_KEY"))
            .license(this, LICENSE_KEY)
            .initialize(this)

        launch {
            // Delete all existing documents on app start
            ScanbotSDK(this@ExampleApplication).documentApi.deleteAllDocuments()
        }
    }
}
