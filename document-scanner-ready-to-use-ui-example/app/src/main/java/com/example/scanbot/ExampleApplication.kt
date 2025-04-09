package com.example.scanbot

import android.app.Application
import android.widget.Toast
import io.scanbot.sap.IScanbotSDKLicenseErrorHandler
import io.scanbot.sap.Status
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.ScanbotSDKInitializer
import io.scanbot.sdk.document.DocumentScannerEngineMode
import io.scanbot.sdk.persistence.CameraImageFormat
import io.scanbot.sdk.persistence.page.PageStorageSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

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
            .licenceErrorHandler(IScanbotSDKLicenseErrorHandler { status, sdkFeature, errorMessage ->
                when (status) {
                    Status.StatusFailureNotSet,
                    Status.StatusFailureCorrupted,
                    Status.StatusFailureWrongOS,
                    Status.StatusFailureAppIDMismatch,
                    Status.StatusFailureExpired -> {
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
            ScanbotSDK(this@ExampleApplication).getSdkComponent()!!.provideDocumentStorage()
                .deleteAll()
        }
    }
}
