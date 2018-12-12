package io.scanbot.example;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import net.doo.snap.util.log.Logger;
import net.doo.snap.util.log.LoggerProvider;

import androidx.appcompat.app.AppCompatActivity;
import io.scanbot.sdk.ScanbotSDK;

public class MainActivity extends AppCompatActivity {

    private final Logger logger = LoggerProvider.getLogger();

    private ScanbotSDK scanbotSDK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initDependencies();

        Button scannerBtn = (Button) findViewById(R.id.scanner_btn);
        scannerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(PayformScannerActivity.newIntent(MainActivity.this));
            }
        });
    }

    private void initDependencies() {
        scanbotSDK = new ScanbotSDK(this);
    }
}
