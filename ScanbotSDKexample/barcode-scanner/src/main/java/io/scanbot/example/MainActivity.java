package io.scanbot.example;

import android.os.Bundle;
import android.support.v4.view.WindowCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.google.zxing.Result;

import net.doo.snap.ScanbotSDK;
import net.doo.snap.camera.BarcodeDetectorFrameHandler;
import net.doo.snap.camera.CameraOpenCallback;
import net.doo.snap.camera.ScanbotCameraView;

public class MainActivity extends AppCompatActivity implements BarcodeDetectorFrameHandler.ResultHandler {

    private ScanbotCameraView cameraView;

    private boolean flashEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        cameraView = (ScanbotCameraView) findViewById(R.id.camera);
        cameraView.setCameraOpenCallback(new CameraOpenCallback() {
            @Override
            public void onCameraOpened() {
                cameraView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        cameraView.continuousFocus();
                        cameraView.useFlash(flashEnabled);
                    }
                }, 700);
            }
        });

        BarcodeDetectorFrameHandler barcodeDetectorFrameHandler = BarcodeDetectorFrameHandler.attach(cameraView, new ScanbotSDK(this));

        // Default detection interval is 10000 ms
        barcodeDetectorFrameHandler.setDetectionInterval(5000);

        barcodeDetectorFrameHandler.addResultHandler(this);

        findViewById(R.id.flash).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flashEnabled = !flashEnabled;
                cameraView.useFlash(flashEnabled);
            }
        });
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

    @Override
    public boolean handleResult(final Result detectedBarcode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (detectedBarcode != null) {
                    Toast.makeText(
                            MainActivity.this,
                            detectedBarcode.getText(),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }
        });
        return false;
    }
}
