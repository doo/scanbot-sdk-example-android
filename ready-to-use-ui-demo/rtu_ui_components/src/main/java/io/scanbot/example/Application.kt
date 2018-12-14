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
        const val LICENSE = ""
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
//                .license(this, LICENSE)
                .initialize(this)

        launch {
            PageRepository.clearPages(this@Application)
            SharingCopier.clear(this@Application)
        }
    }

}
