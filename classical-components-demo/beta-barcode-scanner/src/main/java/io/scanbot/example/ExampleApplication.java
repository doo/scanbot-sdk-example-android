package io.scanbot.example;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;
import io.scanbot.sdk.ScanbotSDKInitializer;
import io.scanbot.sdk.barcode.ScanbotBarcodeDetector;

/**
 * {@link ScanbotSDKInitializer} should be called
 * in {@code Application.onCreate()} method for modules initialization
 */
public class ExampleApplication extends Application {

    /*
     * TODO 1/2: Add the Scanbot SDK license key here.
     * Please note: The Scanbot SDK will run without a license key for one minute per session!
     * After the trial period is over all Scanbot SDK functions as well as the UI components will stop working
     * or may be terminated. You can get an unrestricted "no-strings-attached" 30 day trial license key for free.
     * Please submit the trial license form (https://scanbot.io/sdk/trial.html) on our website by using
     * the app identifier "io.scanbot.example.sdk.android" of this example app.
     */
    private static final String LICENSE_KEY = "";

    @Override
    public void onCreate() {
        new ScanbotSDKInitializer()
                // TODO 2/2: Enable the Scanbot SDK license key
                //.license(this, LICENSE_KEY)
                /*
                 * Please note: BarcodeDetectorType.Scanbot enables the new BETA Barcode Detector of Scanbot SDK.
                 *
                 * - It supports multiple barcode detection.
                 * - Provides better detection and extraction of 1D and 2D barcodes, especially Data Matrix and PDF 417 codes.
                 * - Provides out-of-the-box parsers, like German Medical Plans (Medikationsplan) based on Data Matrix,
                 *   ID Cards or US Driver Licenses, both based on PDF 417, etc.
                 *
                 * As this Barcode Detector is a BETA feature, it is still under active development and improvement.
                 * We will try to keep the API as stable as possible. However, please note that we can't guarantee that.
                 */
                .useBarcodeDetector(ScanbotBarcodeDetector.BarcodeDetectorType.Scanbot)
                .initialize(this);
        super.onCreate();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
