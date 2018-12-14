package io.scanbot.example;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import net.doo.snap.camera.CameraOpenCallback;
import net.doo.snap.camera.ScanbotCameraView;
import net.doo.snap.dcscanner.DCScanner;
import net.doo.snap.dcscanner.DCScannerFrameHandler;
import net.doo.snap.util.log.Logger;
import net.doo.snap.util.log.LoggerProvider;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import io.scanbot.dcscanner.model.DisabilityCertificateRecognizerResultInfo;
import io.scanbot.sdk.ScanbotSDK;

public class DCScannerActivity extends AppCompatActivity {

    private final Logger logger = LoggerProvider.getLogger();

    private ScanbotCameraView cameraView;

    boolean flashEnabled = false;

    public static Intent newIntent(Context context) {
        return new Intent(context, DCScannerActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dc_scanner);

        getSupportActionBar().hide();

        cameraView = (ScanbotCameraView) findViewById(R.id.camera);

        cameraView.setCameraOpenCallback(new CameraOpenCallback() {
            @Override
            public void onCameraOpened() {
                cameraView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        cameraView.useFlash(flashEnabled);
                        cameraView.continuousFocus();
                    }
                }, 700);
            }
        });

        ScanbotSDK scanbotSDK = new ScanbotSDK(this);
        final DCScanner dcScanner = scanbotSDK.dcScanner();
        DCScannerFrameHandler dcScannerFrameHandler = DCScannerFrameHandler.attach(cameraView, dcScanner);

        dcScannerFrameHandler.addResultHandler(new DCScannerFrameHandler.ResultHandler() {
            @Override
            public boolean handleResult(DisabilityCertificateRecognizerResultInfo resultInfo) {
                if (resultInfo != null && resultInfo.recognitionSuccessful) {
                    long a = System.currentTimeMillis();

                    try {
                        startActivity(DCResultActivity.newIntent(DCScannerActivity.this, resultInfo));
                    } finally {
                        long b = System.currentTimeMillis();
                        logger.d("DCScanner", "Total scanning (sec): " + (b - a) / 1000f);
                    }
                }
                return false;
            }
        });

        findViewById(R.id.flash).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                flashEnabled = !flashEnabled;
                cameraView.useFlash(flashEnabled);
            }
        });

        Toast.makeText(
                this,
                scanbotSDK.isLicenseActive()
                        ? "License is active"
                        : "License is expired",
                Toast.LENGTH_LONG
        ).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.onPause();
    }
}
