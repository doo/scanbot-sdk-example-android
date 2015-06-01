package io.scanbot.example;

import android.app.Application;

import net.doo.snap.ScanbotSDKInitializer;

import roboguice.RoboGuice;

/**
 * {@link ScanbotSDKInitializer} should be called
 * in {@code Application.onCreate()} method for RoboGuice modules initialization
 */
public class ExampleApplication extends Application {
    static {
        RoboGuice.setUseAnnotationDatabases(false);
    }

    @Override
    public void onCreate() {
        (new ScanbotSDKInitializer()).initialize(this);
        super.onCreate();
    }
}
