package com.example.scanbot

import android.app.Application
import android.widget.Toast
import io.scanbot.sap.IScanbotSDKLicenseErrorHandler
import io.scanbot.sap.Status
import io.scanbot.sdk.ScanbotSDKInitializer
import io.scanbot.sdk.core.contourdetector.ContourDetector
import io.scanbot.sdk.persistence.CameraImageFormat
import io.scanbot.sdk.persistence.PageStorageSettings

class ExampleApplication : Application() {

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
            .contourDetectorType(ContourDetector.Type.ML_BASED)
            .usePageStorageSettings(
                PageStorageSettings.Builder()
                    .imageFormat(CameraImageFormat.JPG)
                    .imageQuality(80)
                    .build()
            )
            .licenceErrorHandler(IScanbotSDKLicenseErrorHandler { status, sdkFeature, s ->
                when (status) {
                    Status.StatusFailureNotSet,
                    Status.StatusFailureCorrupted,
                    Status.StatusFailureWrongOS,
                    Status.StatusFailureAppIDMismatch,
                    Status.StatusFailureExpired -> {
                        Toast.makeText(
                            this,
                            "License error: $status ",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    else -> {

                    }
                }

            })
            .useFileEncryption(USE_ENCRYPTION)
            .license(this, LICENSE_KEY)
            .initialize(this)
    }
}
