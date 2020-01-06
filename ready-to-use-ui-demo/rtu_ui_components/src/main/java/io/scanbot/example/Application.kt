package io.scanbot.example

import android.util.Log
import androidx.multidex.MultiDexApplication
import io.scanbot.example.repository.PageRepository
import io.scanbot.example.util.SharingCopier
import io.scanbot.sap.IScanbotSDKLicenseErrorHandler
import io.scanbot.sap.Status
import io.scanbot.sap.Status.*

import io.scanbot.sdk.ScanbotSDKInitializer
import io.scanbot.sdk.barcode.ScanbotBarcodeDetector
import io.scanbot.sdk.persistence.CameraImageFormat
import io.scanbot.sdk.persistence.PageStorageSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class Application : MultiDexApplication(), CoroutineScope {

    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job


    companion object {
        /*
         * TODO 1/2: Add the Scanbot SDK license key here.
         * Please note: The Scanbot SDK will run without a license key for one minute per session!
         * After the trial period is over all Scanbot SDK functions as well as the UI components will stop working
         * or may be terminated. You can get an unrestricted "no-strings-attached" 30 day trial license key for free.
         * Please submit the trial license form (https://scanbot.io/sdk/trial.html) on our website by using
         * the app identifier "io.scanbot.example.sdk.rtu.android" of this example app.
         */
        const val LICENSE_KEY = ""
    }

    override fun onCreate() {
        super.onCreate()
        val sdkLicenseInfo = ScanbotSDKInitializer()
                .useBarcodeDetector(ScanbotBarcodeDetector.BarcodeDetectorType.ZXing)
                .withLogging(BuildConfig.DEBUG)
                .usePageStorageSettings(
                        PageStorageSettings.Builder()
                                .imageFormat(CameraImageFormat.JPG)
                                .previewTargetMax(900)
                                .build()
                )
                .prepareOCRLanguagesBlobs(true)
                .prepareMRZBlobs(true)
                .preparePayFormBlobs(true)
                .licenceErrorHandler(IScanbotSDKLicenseErrorHandler { status, feature ->
                    //handle license problem
                    Log.d("ScanbotExample", "Status ${status.name} feature ${feature.name}")
                })
                // TODO 2/2: Enable the Scanbot SDK license key
                // .license(this, LICENSE_KEY)
                .initialize(this)

        //you can check status here
        Log.d("ScanbotExample", "Status " + sdkLicenseInfo.status.name)

        launch {
            PageRepository.clearPages(this@Application)
            SharingCopier.clear(this@Application)
        }
    }

}
