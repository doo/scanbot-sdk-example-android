package io.scanbot.example;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import net.doo.snap.util.log.Logger;
import net.doo.snap.util.log.LoggerProvider;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private final Logger logger = LoggerProvider.getLogger();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button liveScannerBtn = findViewById(R.id.live_scanner_btn);
        liveScannerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(MRZLiveDetectionActivity.newIntent(MainActivity.this));
            }
        });

        Button stillImageScannerBtn = findViewById(R.id.still_image_detection_btn);
        stillImageScannerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(getApplicationContext(), MRZStillImageDetectionActivity.class);
                startActivity(intent);
            }
        });
    }
}
