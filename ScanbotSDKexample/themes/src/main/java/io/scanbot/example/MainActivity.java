package io.scanbot.example;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import net.doo.snap.ui.RoboActionBarActivity;
import net.doo.snap.ui.SnappingActivity;


public class MainActivity extends RoboActionBarActivity {

    private static final int REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.scanButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(
                        new Intent(v.getContext(), SnappingActivity.class),
                        REQUEST_CODE
                );
            }
        });
    }

}
