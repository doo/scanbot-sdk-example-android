package io.scanbot.example;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.WindowCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import net.doo.snap.ScanbotSDK;
import net.doo.snap.camera.CameraOpenCallback;
import net.doo.snap.camera.PictureCallback;
import net.doo.snap.camera.ScanbotCameraView;
import net.doo.snap.dcscanner.DCScanner;

import io.scanbot.dcscanner.model.DisabilityCertificateRecognizerResultInfo;

public class ManualDCScannerActivity extends AppCompatActivity implements PictureCallback {

    private ScanbotCameraView cameraView;

    boolean flashEnabled = false;
    private DCScanner dcScanner;

    public static Intent newIntent(Context context) {
        return new Intent(context, ManualDCScannerActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_dc_scanner);

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
        cameraView.addPictureCallback(this);

        ScanbotSDK scanbotSDK = new ScanbotSDK(this);
        dcScanner = scanbotSDK.dcScanner();

        findViewById(R.id.flash).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                flashEnabled = !flashEnabled;
                cameraView.useFlash(flashEnabled);
            }
        });

        findViewById(R.id.take_picture_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.takePicture(false);
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

    @Override
    public void onPictureTaken(byte[] image, int imageOrientation) {
        // Here we get the full image from the camera.
        // Implement a suitable async(!) detection and image handling here.

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeByteArray(image, 0, image.length, options);

        // Run DC content recognition:
        final DisabilityCertificateRecognizerResultInfo resultInfo = dcScanner.recognizeDCJPEG(image, options.outWidth, options.outHeight, imageOrientation);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (resultInfo != null && resultInfo.recognitionSuccessful) {
                    startActivity(DCResultActivity.newIntent(ManualDCScannerActivity.this, resultInfo));
                } else {
                    Toast.makeText(ManualDCScannerActivity.this, "No DC content detected!", Toast.LENGTH_SHORT).show();
                }

                cameraView.continuousFocus();
                cameraView.startPreview();
            }
        });
    }
}
