package io.scanbot.example;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import net.doo.snap.camera.CameraOpenCallback;
import net.doo.snap.camera.ScanbotCameraView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import io.scanbot.hicscanner.model.HealthInsuranceCardDetectionStatus;
import io.scanbot.hicscanner.model.HealthInsuranceCardRecognitionResult;
import io.scanbot.sdk.ScanbotSDK;
import io.scanbot.sdk.SdkLicenseError;
import io.scanbot.sdk.camera.FrameHandlerResult;
import io.scanbot.sdk.hicscanner.HealthInsuranceCardScanner;
import io.scanbot.sdk.hicscanner.HealthInsuranceCardScannerFrameHandler;
import io.scanbot.sdk.util.log.Logger;
import io.scanbot.sdk.util.log.LoggerProvider;


public class EhicLiveDetectionActivity extends AppCompatActivity {

    private final Logger logger = LoggerProvider.getLogger();

    private ScanbotCameraView cameraView;
    private TextView resultView;

    boolean flashEnabled = false;

    public static Intent newIntent(Context context) {
        return new Intent(context, EhicLiveDetectionActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ehic_live_scanner);

        getSupportActionBar().hide();

        cameraView = findViewById(R.id.camera);

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

        resultView = findViewById(R.id.result);

        ScanbotSDK scanbotSDK = new ScanbotSDK(this);
        final HealthInsuranceCardScanner healthInsuranceCardScanner = scanbotSDK.healthInsuranceCardScanner();
        HealthInsuranceCardScannerFrameHandler healthInsuranceCardScannerFrameHandler = HealthInsuranceCardScannerFrameHandler.attach(cameraView, healthInsuranceCardScanner);

        healthInsuranceCardScannerFrameHandler.addResultHandler(new HealthInsuranceCardScannerFrameHandler.ResultHandler() {
            @Override
            public boolean handle(FrameHandlerResult<? extends HealthInsuranceCardRecognitionResult, ? extends SdkLicenseError> frameHandlerResult) {
                if (frameHandlerResult instanceof FrameHandlerResult.Success) {
                    HealthInsuranceCardRecognitionResult ehicRecognitionResult = (HealthInsuranceCardRecognitionResult) ((FrameHandlerResult.Success) frameHandlerResult).getValue();
                    if (ehicRecognitionResult != null && ehicRecognitionResult.status == HealthInsuranceCardDetectionStatus.SUCCESS) {
                        long a = System.currentTimeMillis();

                        try {
                            startActivity(EhicResultActivity.newIntent(EhicLiveDetectionActivity.this, ehicRecognitionResult));
                        } finally {
                            long b = System.currentTimeMillis();
                            logger.d("EHICScanner", "Total scanning (sec): " + (b - a) / 1000f);
                        }
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
