package io.scanbot.example;

import android.content.res.Resources;

import io.scanbot.example.R;

import javax.inject.Inject;

/**
 * Android impl of {@link TextProvider}
 */
public class AndroidTextProvider implements TextProvider {

    private final Resources resources;

    @Inject
    public AndroidTextProvider(Resources resources) {
        this.resources = resources;
    }

    @Override
    public String get() {
        return resources.getString(R.string.app_name);
    }
}
