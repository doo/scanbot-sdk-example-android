package io.scanbot.example.inject;

import android.content.res.Resources;

import dagger.Module;
import dagger.Provides;
import io.scanbot.example.AndroidTextProvider;
import io.scanbot.example.TextProvider;

/**
 * DI example module
 */
@Module
public class ExampleModule {
    @Provides
    public TextProvider providesText(Resources resources){
        return new AndroidTextProvider(resources);
    }
}
