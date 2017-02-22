package io.scanbot.example.inject;

import javax.inject.Singleton;

import dagger.Component;
import io.scanbot.example.MainActivity;

/**
 * Example component for DI
 */
@Singleton
@Component(modules = {
        AndroidModule.class,
        ExampleModule.class
})
public interface ApplicationComponent {
    void inject(MainActivity activity);
}
