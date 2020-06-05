package io.scanbot.example;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import net.doo.snap.camera.CameraOpenCallback;
import net.doo.snap.camera.ScanbotCameraView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import org.jetbrains.annotations.NotNull;

import io.scanbot.chequescanner.model.Result;
import io.scanbot.sdk.ScanbotSDK;
import io.scanbot.sdk.SdkLicenseError;
import io.scanbot.sdk.camera.FrameHandlerResult;
import io.scanbot.sdk.chequescanner.ChequeScanner;
import io.scanbot.sdk.chequescanner.ChequeScannerFrameHandler;

public class ChequeScannerActivity extends AppCompatActivity {

    private ScanbotCameraView cameraView;

    boolean flashEnabled = false;

    public static Intent newIntent(Context context) {
        return new Intent(context, ChequeScannerActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cheque_scanner);

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
        final ChequeScanner chequeScanner = scanbotSDK.chequeScanner();
        ChequeScannerFrameHandler chequeScannerFrameHandler = ChequeScannerFrameHandler.attach(cameraView, chequeScanner);

        chequeScannerFrameHandler.addResultHandler(new ChequeScannerFrameHandler.ResultHandler() {
            @Override
            public boolean handle(@NotNull FrameHandlerResult<? extends Result, ? extends SdkLicenseError> frameHandlerResult) {
                if (frameHandlerResult instanceof FrameHandlerResult.Success) {
                    final Result result = ((FrameHandlerResult.Success<Result>) frameHandlerResult).getValue();
                    if (result != null
                            && ((result.accountNumber != null && !result.accountNumber.value.isEmpty())
                            || (result.routingNumber != null && !result.routingNumber.value.isEmpty()))) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ChequeScannerActivity.this,
                                        extractData(result), Toast.LENGTH_LONG).show();
                            }
                        });
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

    private String extractData(Result result) {
        return new StringBuilder()
                .append("accountNumber: ").append(result.accountNumber.value).append("\n")
                .append("routingNumber: ").append(result.routingNumber.value).append("\n")
                .append("chequeNumber: ").append(result.chequeNumber.value).append("\n")
                .append("Polygon detection result: ").append(result.polygon.detectionResult.toString()).append("\n")
                .append("Polygon : ").append(result.polygon.points.toString()).append("\n")
                .toString();
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
