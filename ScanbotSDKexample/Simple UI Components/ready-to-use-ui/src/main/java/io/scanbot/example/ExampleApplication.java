package io.scanbot.example;


import androidx.multidex.MultiDexApplication;
import io.scanbot.sdk.ScanbotSDKInitializer;
import io.scanbot.sdk.persistence.PageStorageSettings;


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
                                .previewTargetMax(600)
                                .build())
                .prepareMRZBlobs(true)
                .initialize(this);
        super.onCreate();
    }
}
