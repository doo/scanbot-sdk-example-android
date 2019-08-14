package io.scanbot.example

import androidx.multidex.MultiDexApplication
import io.scanbot.example.repository.PageRepository
import io.scanbot.example.util.SharingCopier

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
        ScanbotSDKInitializer()
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
                // TODO 2/2: Enable the Scanbot SDK license key
                // .license(this, LICENSE_KEY)
                .initialize(this)

        launch {
            PageRepository.clearPages(this@Application)
            SharingCopier.clear(this@Application)
        }
    }

}
