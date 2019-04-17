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
        // limited trial license key!
        const val LICENSE =
                "lXV9acGzkTYK7RL78doCr5Y1Nh23Un" +
                "+Fpm4muCPEXItfLDHiO5YybtFLmHMM" +
                "QoP5kOz2bHH7IwHUOJoO5H2WQtBStJ" +
                "f27Q62rN3cJsRwmquDtZYoZp1nDP3f" +
                "g/gPgU1/yRC7kXIpfbQNL/1gDmAXb6" +
                "kQ6/6Q75vilgwINyiPqLbR7s475mRb" +
                "hIIHMy1o1shbqlJj0mr3bUFDj59VSp" +
                "OihIN1bIBwkBvzyRq3LFcDEqzYDSj9" +
                "vnIwWDR9DrOiM0VOhjnkqr9lyxkAMy" +
                "ZvSiqlj85F46gjPqbz2jegj25kS111" +
                "wzuWaaiDkDimr5EGcNkMoQ/gwlCghY" +
                "TitKLPEA9Nkg==\nU2NhbmJvdFNESw" +
                "ppby5zY2FuYm90LnNka2RlbW8KMTU1" +
                "ODEzNzU5OQoxMzEwNzEKMg==\n"
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
                .license(this, LICENSE) // TODO Add the Scanbot SDK license key here
                .initialize(this)

        launch {
            PageRepository.clearPages(this@Application)
            SharingCopier.clear(this@Application)
        }
    }

}
