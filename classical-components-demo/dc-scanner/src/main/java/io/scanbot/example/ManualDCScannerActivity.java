package io.scanbot.example;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import net.doo.snap.camera.CameraOpenCallback;
import net.doo.snap.camera.PictureCallback;
import net.doo.snap.camera.ScanbotCameraView;
import net.doo.snap.dcscanner.DCScanner;
import net.doo.snap.lib.detector.ContourDetector;

import java.util.ArrayList;
import java.util.List;

import io.scanbot.dcscanner.model.DisabilityCertificateRecognizerResultInfo;
import io.scanbot.sdk.ScanbotSDK;
import io.scanbot.sdk.process.CropOperation;
import io.scanbot.sdk.process.Operation;

public class ManualDCScannerActivity extends AppCompatActivity implements PictureCallback {

    private ScanbotCameraView cameraView;
    private ImageView resultImageView;

    boolean flashEnabled = false;
    private DCScanner dcScanner;
    private ScanbotSDK scanbotSDK;

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

        resultImageView = findViewById(R.id.resultImageView);

        scanbotSDK = new ScanbotSDK(this);
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

        // Decode Bitmap from bytes of original image:
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2; // use 1 for full, no downscaled image.
        Bitmap originalBitmap = BitmapFactory.decodeByteArray(image, 0, image.length, options);

        // rotate original image if required:
        if (imageOrientation > 0) {
            final Matrix matrix = new Matrix();
            matrix.setRotate(imageOrientation, originalBitmap.getWidth() / 2f, originalBitmap.getHeight() / 2f);
            originalBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, false);
        }

        // Run document detection on original image:
        final ContourDetector detector = new ScanbotSDK(this).contourDetector();
        detector.detect(originalBitmap);
        List<Operation> operations = new ArrayList<>();
        operations.add(new CropOperation(detector.getPolygonF()));
        final Bitmap documentImage = scanbotSDK.imageProcessor().process(originalBitmap, operations, false);

        // Show the cropped image as thumbnail preview
        final Bitmap thumbnailImage = resizeImage(documentImage, 600, 600);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resultImageView.setImageBitmap(thumbnailImage);
                // continue with camera preview
                cameraView.continuousFocus();
                cameraView.startPreview();
            }
        });

        // And finally run DC recognition on prepared document image:
        final DisabilityCertificateRecognizerResultInfo resultInfo = dcScanner.recognizeDCBitmap(documentImage, 0);

        if (resultInfo != null && resultInfo.recognitionSuccessful) {
            startActivity(DCResultActivity.newIntent(ManualDCScannerActivity.this, resultInfo));
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final Toast toast = Toast.makeText(ManualDCScannerActivity.this, "No DC content was recognized!", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            });
        }

        // reset preview image
        resultImageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                resultImageView.setImageBitmap(null);
            }
        }, 1000);
    }

    private Bitmap resizeImage(final Bitmap bitmap, final float width, final float height) {
        final float oldWidth = bitmap.getWidth();
        final float oldHeight = bitmap.getHeight();
        final float scaleFactor = (oldWidth > oldHeight ? (width / oldWidth) : (height / oldHeight));

        final int scaledWidth = Math.round(oldWidth * scaleFactor);
        final int scaledHeight = Math.round(oldHeight * scaleFactor);

        return Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, false);
    }

}
