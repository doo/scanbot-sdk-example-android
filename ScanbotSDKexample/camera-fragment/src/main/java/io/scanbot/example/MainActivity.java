package io.scanbot.example;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import net.doo.snap.ui.RoboActionBarActivity;


public class MainActivity extends RoboActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.openButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openIntent = new Intent(MainActivity.this, ExampleActivity.class);
                startActivity(openIntent);
            }
        });
    }
}
