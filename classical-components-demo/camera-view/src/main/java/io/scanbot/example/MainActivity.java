package io.scanbot.example;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import net.doo.snap.camera.AutoSnappingController;
import net.doo.snap.camera.CameraOpenCallback;
import net.doo.snap.camera.ContourDetectorFrameHandler;
import net.doo.snap.camera.PictureCallback;
import net.doo.snap.camera.ScanbotCameraView;
import net.doo.snap.lib.detector.ContourDetector;
import net.doo.snap.lib.detector.DetectionResult;
import net.doo.snap.ui.PolygonView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;

import org.jetbrains.annotations.NotNull;

import io.scanbot.sdk.SdkLicenseError;
import io.scanbot.sdk.camera.FrameHandlerResult;
import io.scanbot.sdk.ui.camera.ShutterButton;


public class MainActivity extends AppCompatActivity implements PictureCallback,
        ContourDetectorFrameHandler.ResultHandler {

    private ScanbotCameraView cameraView;
    private PolygonView polygonView;
    private ImageView resultView;
    private ContourDetectorFrameHandler contourDetectorFrameHandler;
    private AutoSnappingController autoSnappingController;
    private TextView userGuidanceHint;
    private long lastUserGuidanceHintTs = 0L;
    private Button autoSnappingToggleButton;
    private ShutterButton shutterButton;

    private boolean flashEnabled = false;
    private boolean autoSnappingEnabled = true;
    private final boolean ignoreBadAspectRatio = true;

    private static final int POLYGON_FILL_COLOR = Color.parseColor("#55ff0000");
    private static final int POLYGON_FILL_COLOR_OK = Color.parseColor("#4400ff00");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);

        askPermission();

        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        cameraView = (ScanbotCameraView) findViewById(R.id.camera);

        // In this example we demonstrate how to lock the orientation of the UI (Activity)
        // as well as the orientation of the taken picture to portrait.
        cameraView.lockToPortrait(true);

        // See https://github.com/doo/scanbot-sdk-example-android/wiki/Using-ScanbotCameraView#preview-mode
        //cameraView.setPreviewMode(net.doo.snap.camera.CameraPreviewMode.FIT_IN);

        cameraView.setCameraOpenCallback(new CameraOpenCallback() {
            @Override
            public void onCameraOpened() {
                cameraView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        cameraView.setAutoFocusSound(false);
                        // Shutter sound is ON by default. You can disable it:
                        /*
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            cameraView.setShutterSound(false);
                        }
                        */
                        cameraView.continuousFocus();
                        cameraView.useFlash(flashEnabled);
                    }
                }, 700);
            }
        });

        resultView = (ImageView) findViewById(R.id.result);

        polygonView = (PolygonView) findViewById(R.id.polygonView);
        polygonView.setFillColor(POLYGON_FILL_COLOR);
        polygonView.setFillColorOK(POLYGON_FILL_COLOR_OK);

        contourDetectorFrameHandler = ContourDetectorFrameHandler.attach(cameraView);
        // Please note: https://github.com/doo/Scanbot-SDK-Examples/wiki/Detecting-and-drawing-contours#contour-detection-parameters
        contourDetectorFrameHandler.setAcceptedAngleScore(60);
        contourDetectorFrameHandler.setAcceptedSizeScore(75);
        contourDetectorFrameHandler.addResultHandler(polygonView.contourDetectorResultHandler);
        contourDetectorFrameHandler.addResultHandler(this);

        autoSnappingController = AutoSnappingController.attach(cameraView, contourDetectorFrameHandler);
        autoSnappingController.setIgnoreBadAspectRatio(ignoreBadAspectRatio);

        // Please note: https://github.com/doo/Scanbot-SDK-Examples/wiki/Autosnapping#sensitivity
        autoSnappingController.setSensitivity(0.85f);

        cameraView.addPictureCallback(this);

        userGuidanceHint = findViewById(R.id.userGuidanceHint);

        shutterButton = findViewById(R.id.shutterButton);
        shutterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.takePicture(false);
            }
        });
        shutterButton.setVisibility(View.VISIBLE);

        findViewById(R.id.flashToggle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flashEnabled = !flashEnabled;
                cameraView.useFlash(flashEnabled);
            }
        });

        autoSnappingToggleButton = findViewById(R.id.autoSnappingToggle);
        autoSnappingToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoSnappingEnabled = !autoSnappingEnabled;
                setAutoSnapEnabled(autoSnappingEnabled);
            }
        });

        autoSnappingToggleButton.post(new Runnable() {
            @Override
            public void run() {
                setAutoSnapEnabled(autoSnappingEnabled);
            }
        });

    }

    private void askPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        999);
            }
        }
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
    public boolean handle(@NotNull FrameHandlerResult<? extends ContourDetectorFrameHandler.DetectedFrame, ? extends SdkLicenseError> frameHandlerResult) {
        if (frameHandlerResult instanceof FrameHandlerResult.Success) {
            final ContourDetectorFrameHandler.DetectedFrame detectedFrame = (ContourDetectorFrameHandler.DetectedFrame) ((FrameHandlerResult.Success) frameHandlerResult).getValue();
            // Here you are continuously notified about contour detection results.
            // For example, you can show a user guidance text depending on the current detection status.
            userGuidanceHint.post(new Runnable() {
                @Override
                public void run() {
                    showUserGuidance(detectedFrame.detectionResult);
                }
            });

        }
        return false;
    }

    private void showUserGuidance(final DetectionResult result) {
        if (!autoSnappingEnabled) {
            return;
        }

        if (System.currentTimeMillis() - lastUserGuidanceHintTs < 400) {
            return;
        }

        // Make sure to reset the default polygon fill color (see the ignoreBadAspectRatio case).
        polygonView.setFillColor(POLYGON_FILL_COLOR);

        switch (result) {
            case OK:
                userGuidanceHint.setText("Don't move");
                userGuidanceHint.setVisibility(View.VISIBLE);
                break;
            case OK_BUT_TOO_SMALL:
                userGuidanceHint.setText("Move closer");
                userGuidanceHint.setVisibility(View.VISIBLE);
                break;
            case OK_BUT_BAD_ANGLES:
                userGuidanceHint.setText("Perspective");
                userGuidanceHint.setVisibility(View.VISIBLE);
                break;
            case ERROR_NOTHING_DETECTED:
                userGuidanceHint.setText("No Document");
                userGuidanceHint.setVisibility(View.VISIBLE);
                break;
            case ERROR_TOO_NOISY:
                userGuidanceHint.setText("Background too noisy");
                userGuidanceHint.setVisibility(View.VISIBLE);
                break;
            case OK_BUT_BAD_ASPECT_RATIO:
                if (ignoreBadAspectRatio) {
                    userGuidanceHint.setText("Don't move");
                    // change polygon color to "OK"
                    polygonView.setFillColor(POLYGON_FILL_COLOR_OK);
                } else {
                    userGuidanceHint.setText("Wrong aspect ratio.\n Rotate your device.");
                }
                userGuidanceHint.setVisibility(View.VISIBLE);
                break;
            case ERROR_TOO_DARK:
                userGuidanceHint.setText("Poor light");
                userGuidanceHint.setVisibility(View.VISIBLE);
                break;
            default:
                userGuidanceHint.setVisibility(View.GONE);
                break;
        }

        lastUserGuidanceHintTs = System.currentTimeMillis();
    }

    @Override
    public void onPictureTaken(byte[] image, int imageOrientation) {
        // Here we get the full image from the camera.
        // Please see https://github.com/doo/Scanbot-SDK-Examples/wiki/Handling-camera-picture
        // This is just a demo showing the detected document image as a downscaled(!) preview image.

        // Decode Bitmap from bytes of original image:
        final BitmapFactory.Options options = new BitmapFactory.Options();
        // Please note: In this simple demo we downscale the original image to 1/8 for the preview!
        options.inSampleSize = 8;
        // Typically you will need the full resolution of the original image! So please change the "inSampleSize" value to 1!
        //options.inSampleSize = 1;
        Bitmap originalBitmap = BitmapFactory.decodeByteArray(image, 0, image.length, options);

        // Rotate the original image based on the imageOrientation value.
        // Required for some Android devices like Samsung!
        if (imageOrientation > 0) {
            final Matrix matrix = new Matrix();
            matrix.setRotate(imageOrientation, originalBitmap.getWidth() / 2f, originalBitmap.getHeight() / 2f);
            originalBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, false);
        }

        // Run document detection on original image:
        final ContourDetector detector = new ContourDetector();
        detector.detect(originalBitmap);
        final Bitmap documentImage = detector.processImageAndRelease(originalBitmap, detector.getPolygonF(), ContourDetector.IMAGE_FILTER_NONE);

        resultView.post(new Runnable() {
            @Override
            public void run() {
                resultView.setImageBitmap(documentImage);
            }
        });

        // continue scanning
        cameraView.postDelayed(new Runnable() {
            @Override
            public void run() {
                cameraView.continuousFocus();
                cameraView.startPreview();
            }
        }, 1000);
    }

    private void setAutoSnapEnabled(boolean enabled) {
        autoSnappingController.setEnabled(enabled);
        contourDetectorFrameHandler.setEnabled(enabled);
        polygonView.setVisibility(enabled ? View.VISIBLE : View.GONE);
        autoSnappingToggleButton.setText("Automatic " + (enabled ? "ON" : "OFF"));
        if (enabled) {
            shutterButton.showAutoButton();
        } else {
            shutterButton.showManualButton();
            userGuidanceHint.setVisibility(View.GONE);
        }
    }
}
