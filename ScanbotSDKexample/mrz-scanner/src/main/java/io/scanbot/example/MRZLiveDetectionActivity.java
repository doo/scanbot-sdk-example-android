package io.scanbot.example;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.WindowCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import net.doo.snap.ScanbotSDK;
import net.doo.snap.camera.CameraOpenCallback;
import net.doo.snap.camera.ScanbotCameraView;
import net.doo.snap.mrzscanner.MRZScanner;
import net.doo.snap.mrzscanner.MRZScannerFrameHandler;
import net.doo.snap.util.log.Logger;
import net.doo.snap.util.log.LoggerProvider;

import io.scanbot.mrzscanner.model.MRZRecognitionResult;

public class MRZLiveDetectionActivity extends AppCompatActivity {

    private final Logger logger = LoggerProvider.getLogger();

    private ScanbotCameraView cameraView;
    private TextView resultView;

    boolean flashEnabled = false;

    public static Intent newIntent(Context context) {
        return new Intent(context, MRZLiveDetectionActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mrz_live_scanner);

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

        resultView = (TextView) findViewById(R.id.result);

        ScanbotSDK scanbotSDK = new ScanbotSDK(this);
        final MRZScanner mrzScanner = scanbotSDK.mrzScanner();
        MRZScannerFrameHandler mrzScannerFrameHandler = MRZScannerFrameHandler.attach(cameraView, mrzScanner);

        mrzScannerFrameHandler.addResultHandler(new MRZScannerFrameHandler.ResultHandler() {
            @Override
            public boolean handleResult(MRZRecognitionResult mrzRecognitionResult) {
                if (mrzRecognitionResult != null && mrzRecognitionResult.recognitionSuccessful) {
                    long a = System.currentTimeMillis();

                    try {
                        startActivity(MRZResultActivity.newIntent(MRZLiveDetectionActivity.this, mrzRecognitionResult));
                    } finally {
                        long b = System.currentTimeMillis();
                        logger.d("MRZScanner", "Total scanning (sec): " + (b - a) / 1000f);
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
