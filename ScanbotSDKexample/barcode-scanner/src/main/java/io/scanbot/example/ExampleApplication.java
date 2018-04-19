package io.scanbot.example;

import android.app.Application;

import net.doo.snap.ScanbotSDKInitializer;
import net.doo.snap.camera.barcode.ScanbotBarcodeDetector;

/**
 * {@link ScanbotSDKInitializer} should be called
 * in {@code Application.onCreate()} method for RoboGuice modules initialization
 */
public class ExampleApplication extends Application {

    @Override
    public void onCreate() {
        new ScanbotSDKInitializer()
                // TODO add your license
                // .license(this, "YOUR_SCANBOT_SDK_LICENSE_KEY")
                .initialize(this);
        super.onCreate();
    }
}
