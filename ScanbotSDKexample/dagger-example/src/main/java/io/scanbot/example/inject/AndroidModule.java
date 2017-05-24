package io.scanbot.example.inject;

import android.app.Application;
import android.content.res.Resources;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Contains android related dependencies
 */
@Module
public class AndroidModule {

    private final Application application;

    public AndroidModule(Application application) {
        this.application = application;
    }

    @Provides
    @Singleton
    public Resources providesResources() {
        return application.getResources();
    }
}
