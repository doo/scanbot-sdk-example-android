package io.scanbot.example;

import android.app.Application;

import net.doo.snap.ScanbotSDKInitializer;
import net.doo.snap.ui.themes.ThemesProvider;

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
        new ScanbotSDKInitializer()
                .themesProvider(new ThemesProvider() {
                    @Override
                    public int getThemeResId(String s) {
                        return R.style.CustomScanbotTheme;
                    }
                })
                .initialize(this);

        super.onCreate();
    }
}
