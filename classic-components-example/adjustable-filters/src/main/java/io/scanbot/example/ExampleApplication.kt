package io.scanbot.example

import android.app.Application
import io.scanbot.example.common.Const
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.ScanbotSDKInitializer
import io.scanbot.sdk.licensing.Feature
import io.scanbot.sdk.util.log.LoggerProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class ExampleApplication : Application(), CoroutineScope {

    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    /*
     * TODO 1/2: Add the Scanbot SDK license key here.
     * Please note: The Scanbot SDK will run without a license key for one minute per session!
     * After the trial period is over all Scanbot SDK functions as well as the UI components will stop working.
     * You can get an unrestricted "no-strings-attached" 30 day trial license key for free.
     * Please submit the trial license form (https://scanbot.io/trial/) on our website by using
     * the app identifier "io.scanbot.example.sdk.android" of this example app.
     */
    private val licenseKey = ""

    override fun onCreate() {
        super.onCreate()

        val logger = LoggerProvider.logger

        ScanbotSDKInitializer()
            .withLogging(true)
            // TODO 2/2: Enable the Scanbot SDK license key
            //.license(this, licenseKey)
            .licenseErrorHandler { status, feature, statusMessage ->
                logger.d(Const.LOG_TAG, "+++> License status: ${status.name}. Status message: $statusMessage")
                logger.d(Const.LOG_TAG, "+++> Feature not available: ${feature.name}")
            }
            //.sdkFilesDirectory(this, getExternalFilesDir(null)!!)
            .initialize(this)

        logger.d(Const.LOG_TAG, "Scanbot SDK was initialized")

        val licenseInfo = ScanbotSDK(this).licenseInfo
        logger.d(Const.LOG_TAG, "License status: ${licenseInfo.status}")
        logger.d(Const.LOG_TAG, "License isValid: ${licenseInfo.isValid}")
        logger.d(Const.LOG_TAG, "License expirationDate: ${licenseInfo.expirationDateString}")

        launch {
            // Clear all previously created documents in storage
            ScanbotSDK(this@ExampleApplication).getSdkComponent()!!.provideDocumentStorage().deleteAll()
        }
    }
}
