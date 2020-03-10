package io.scanbot.example;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

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
     * TODO : Add the Scanbot SDK license key here.
     * Please note: The Scanbot SDK will run without a license key for one minute per session!
     * After the trial period is over all Scanbot SDK functions as well as the UI components will stop working.
     * You can get an unrestricted "no-strings-attached" 30 day trial license key for free.
     * Please submit the trial license form (https://scanbot.io/sdk/trial.html) on our website by using
     * the app identifier "io.scanbot.example.sdk.android" of this example app.
     */
    private static final String LICENSE_KEY = "";

    @Override
    public void onCreate() {
        super.onCreate();

        final SdkLicenseInfo sdkLicenseInfo = new ScanbotSDKInitializer()
                .licenceErrorHandler(new IScanbotSDKLicenseErrorHandler() {
                    @Override
                    public void handleLicenceStatusError(final Status status, final SdkFeature feature) {
                        // Handle license issues here.
                        // A license issue can either be an invalid or expired license key
                        // or missing SDK feature (see SDK feature packages on https://scanbot.io).
                        final String errorMsg;
                        if (status != Status.StatusOkay && status != Status.StatusTrial) {
                            errorMsg = "License Error! License status: " + status.name();
                        }
                        else {
                            errorMsg = "License Error! Missing SDK feature in license: " + feature.name();
                        }
                        Log.d("ScanbotExample", errorMsg);
                        Toast.makeText(ExampleApplication.this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                })
                .license(this, LICENSE_KEY)
                .initialize(this);

        Log.d("ScanbotExample", "Is license valid: " + sdkLicenseInfo.isValid());
        Log.d("ScanbotExample", "License status: " + sdkLicenseInfo.getStatus().name());
    }
}
