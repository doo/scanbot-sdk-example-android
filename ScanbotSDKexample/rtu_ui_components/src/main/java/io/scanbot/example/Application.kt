package io.scanbot.example

import android.support.multidex.MultiDexApplication

import io.scanbot.sdk.ScanbotSDKInitializer
import io.scanbot.sdk.barcode.ScanbotBarcodeDetector
import io.scanbot.sdk.persistence.CameraImageFormat
import io.scanbot.sdk.persistence.PageStorageSettings

class Application : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        ScanbotSDKInitializer()
                .withLogging(true)
                .ocrBlobsPath(this, "http://download.scanbot.io/di/tessdata")
                .languageClassifierBlobPath(this, "http://download.scanbot.io/di/android")
                .useBarcodeDetector(ScanbotBarcodeDetector.BarcodeDetectorType.ZXing)
                .usePageStorageSettings(
                        PageStorageSettings.Builder()
                                .imageFormat(CameraImageFormat.JPG)
                                .previewTargetMax(600)
                                .build()
                )
                .initialize(this)
    }
}
