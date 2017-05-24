package io.scanbot.example;

import android.app.Application;


import net.doo.snap.ScanbotSDKInitializer;

import io.scanbot.example.inject.AndroidModule;
import io.scanbot.example.inject.ApplicationComponent;
import io.scanbot.example.inject.DaggerApplicationComponent;
import io.scanbot.example.inject.ExampleModule;

/**
 * {@link net.doo.snap.ScanbotSDKInitializer} should be called
 * in {@code Application.onCreate()} method for DI initialization
 */
public class ExampleApplication extends Application {

    private static ApplicationComponent applicationComponent;

    @Override
    public void onCreate() {
        if (applicationComponent == null) {
            applicationComponent = DaggerApplicationComponent
                    .builder()
                    .androidModule(new AndroidModule(this))
                    .exampleModule(new ExampleModule())
                    .build();
        }

        new ScanbotSDKInitializer().initialize(this);
        super.onCreate();
    }

    public static ApplicationComponent getApplicationComponent(){
        return applicationComponent;
    }
}
