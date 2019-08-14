package io.scanbot.example;

import android.app.Application;

import io.scanbot.sdk.ScanbotSDKInitializer;

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
                // .license(this, LICENSE_KEY)
                .initialize(this);
        super.onCreate();
    }
}
