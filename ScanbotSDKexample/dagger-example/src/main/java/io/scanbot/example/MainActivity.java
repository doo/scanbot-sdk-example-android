package io.scanbot.example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import javax.inject.Inject;

import io.scanbot.example.R;

public class MainActivity extends AppCompatActivity {

    @Inject
    TextProvider textProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ExampleApplication.getApplicationComponent()
                .inject(this);

        setContentView(R.layout.activity_main);

        ((TextView) findViewById(R.id.text)).setText(textProvider.get());
    }
}
