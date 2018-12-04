package io.scanbot.example;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import io.scanbot.sdk.ScanbotSDK;
import io.scanbot.sdk.barcode.BarcodeDetectorFrameHandler;
import io.scanbot.sdk.barcode.entity.BarcodeScanningResult;

import net.doo.snap.camera.CameraOpenCallback;
import net.doo.snap.camera.ScanbotCameraView;

public class BarcodeScannerActivity extends AppCompatActivity implements BarcodeDetectorFrameHandler.ResultHandler {

    private ScanbotCameraView cameraView;

    private boolean flashEnabled = false;


    public static Intent newIntent(final Context context) {
        return new Intent(context, BarcodeScannerActivity.class);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_barcode_scanner);

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
        barcodeDetectorFrameHandler.setDetectionInterval(2000);

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
    public boolean handleResult(final BarcodeScanningResult detectedBarcode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (detectedBarcode != null) {

                    cameraView.stopPreview();

                    final AlertDialog.Builder builder = new AlertDialog.Builder(BarcodeScannerActivity.this);

                    builder.setTitle("Result")
                            .setMessage(detectedBarcode.getBarcodeFormat().toString() + "\n\n" + detectedBarcode.getText());

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            cameraView.continuousFocus();
                            cameraView.startPreview();
                        }
                    });

                    final AlertDialog dialog = builder.create();
                    dialog.show();

                    /* You can implement a suitable result handler, like create a contact, open URL, etc.
                    final ParsedResult parsedResult = ResultParser.parseResult(rawResult);
                    switch (parsedResult.getType()) {
                        case ADDRESSBOOK:
                            // ...
                            break;
                        case URI:
                            // ...
                            break;
                        // ...
                    }
                    */
                }
            }
        });
        return false;
    }
}
