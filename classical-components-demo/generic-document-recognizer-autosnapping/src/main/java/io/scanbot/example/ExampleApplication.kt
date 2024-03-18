package io.scanbot.example

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import io.scanbot.sap.SdkFeature
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.ScanbotSDKInitializer
import io.scanbot.sdk.util.log.LoggerProvider

class ExampleApplication : Application() {
    /*
     * TODO 1/3: Add the Scanbot SDK license key here.
     * Please note: The Scanbot SDK will run without a license key for one minute per session!
     * After the trial period is over all Scanbot SDK functions as well as the UI components will stop working.
     * You can get an unrestricted "no-strings-attached" 30 day trial license key for free.
     * Please submit the trial license form (https://scanbot.io/en/sdk/demo/trial) on our website by using
     * the app identifier "io.scanbot.example.sdk.android" of this example app.
     */
    val licenseKey = ""

    override fun onCreate() {
        super.onCreate()

        val uiHandler = Handler(Looper.getMainLooper())
        ScanbotSDKInitializer()
            .withLogging(true)
            // TODO 2/3: Enable the Scanbot SDK license key
            //.license(this, licenseKey)
            .licenceErrorHandler { status, feature, statusMessage ->
                LoggerProvider.logger.d("ExampleApplication", "+++> License status: ${status.name}. Status message: $statusMessage")
                if (feature != SdkFeature.NoSdkFeature) {
                    LoggerProvider.logger.d("ExampleApplication", "+++> Feature not available: ${feature.name}")

                    // TODO: 3/3 Handle license error properly
                    uiHandler.post {
                        Toast.makeText(applicationContext, "Trial license expired", Toast.LENGTH_SHORT).show()
                        throw IllegalStateException("Trial license expired")
                    }
                }
            }
            //.sdkFilesDirectory(this, getExternalFilesDir(null)!!)
            .prepareOCRLanguagesBlobs(true)
            .initialize(this)

        LoggerProvider.logger.d("ExampleApplication", "Scanbot SDK was initialized")

        val licenseInfo = ScanbotSDK(this).licenseInfo
        LoggerProvider.logger.d("ExampleApplication", "License status: ${licenseInfo.status}")
        LoggerProvider.logger.d("ExampleApplication", "License isValid: ${licenseInfo.isValid}")
        LoggerProvider.logger.d("ExampleApplication", "License expirationDate: ${licenseInfo.expirationDate}")
    }
}
