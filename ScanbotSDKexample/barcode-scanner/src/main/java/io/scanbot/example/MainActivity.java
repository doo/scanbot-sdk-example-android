package io.scanbot.example;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        findViewById(R.id.start_barcode_scanner_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(BarcodeScannerActivity.newIntent(MainActivity.this));
            }
        });

        findViewById(R.id.start_barcode_scanner_with_finder_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(BarcodeScannerWithFinderActivity.newIntent(MainActivity.this));
            }
        });
    }
}
