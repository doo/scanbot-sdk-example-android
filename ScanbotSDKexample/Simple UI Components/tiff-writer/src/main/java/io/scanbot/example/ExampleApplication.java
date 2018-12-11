package io.scanbot.example;

import android.app.Application;

import io.scanbot.sdk.ScanbotSDKInitializer;

/**
 * {@link ScanbotSDKInitializer} should be called
 * in {@code Application.onCreate()} method for Dagger modules initialization
 */
public class ExampleApplication extends Application {

    @Override
    public void onCreate() {
        new ScanbotSDKInitializer()
                // TODO add your license
                // .license(this, "YOUR_SCANBOT_SDK_LICENSE_KEY")
                .sdkFilesDirectory(this, getExternalFilesDir(null))
                .initialize(this);
        super.onCreate();
    }
}
