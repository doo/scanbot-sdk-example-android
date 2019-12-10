package io.scanbot.example;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.multidex.MultiDex;

import io.scanbot.sap.IScanbotSDKLicenseErrorHandler;
import io.scanbot.sap.SdkFeature;
import io.scanbot.sap.SdkLicenseInfo;
import io.scanbot.sap.Status;
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
        SdkLicenseInfo sdkLicenseInfo = new ScanbotSDKInitializer()
                .licenceErrorHandler(new IScanbotSDKLicenseErrorHandler() {

                    @Override
                    public void handleLicenceStatusError(Status status, SdkFeature feature) {
                        //handle license problem
                        Log.d("ScanbotExample", "Status ${status.name} feature ${feature.name}")
                    }
                })
                // TODO 2/2: Enable the Scanbot SDK license key
                // .license(this, LICENSE_KEY)
                .initialize(this);
        Log.d("ScanbotExample", "Status " + sdkLicenseInfo.getStatus().name());

        super.onCreate();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
