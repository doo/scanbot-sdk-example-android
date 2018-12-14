package io.scanbot.example;

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

        Button scannerBtn = (Button) findViewById(R.id.scanner_btn);
        scannerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(DCScannerActivity.newIntent(MainActivity.this));
            }
        });
        Button manualScannerBtn = (Button) findViewById(R.id.manual_scanner_btn);
        manualScannerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(ManualDCScannerActivity.newIntent(MainActivity.this));
            }
        });
    }
}
