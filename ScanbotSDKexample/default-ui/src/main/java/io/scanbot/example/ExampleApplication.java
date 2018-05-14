package io.scanbot.example;

import android.support.multidex.MultiDexApplication;

import io.scanbot.sdk.ScanbotSDKInitializer;
import io.scanbot.sdk.persistance.PageStorageSettings;


/**
 * {@link ScanbotSDKInitializer} should be called
 * in {@code Application.onCreate()} method for RoboGuice modules initialization
 */
public class ExampleApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        new ScanbotSDKInitializer()
                // TODO add your license
                // .license(this, "YOUR_SCANBOT_SDK_LICENSE_KEY")
                .usePageStorageSettings(
                        new PageStorageSettings.Builder()
                                .previewTargetMax(400)
                                .build())
                .initialize(this);
        super.onCreate();
    }
}
