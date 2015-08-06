package io.scanbot.example;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.WindowCompat;
import android.view.View;
import android.widget.ImageView;

import com.google.inject.Key;

import net.doo.snap.camera.AutoSnappingController;
import net.doo.snap.camera.ContourDetectorFrameHandler;
import net.doo.snap.camera.PictureCallback;
import net.doo.snap.camera.ScanbotCameraView;
import net.doo.snap.ui.PolygonView;
import net.doo.snap.ui.RoboActionBarActivity;

import java.util.HashMap;
import java.util.Map;

import roboguice.RoboGuice;
import roboguice.util.RoboContext;


public class MainActivity extends RoboActionBarActivity implements RoboContext, PictureCallback {

    protected final HashMap<Key<?>, Object> scopedObjects = new HashMap<>();

    private ScanbotCameraView cameraView;
    private ImageView resultView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY);

        super.onCreate(savedInstanceState);
        RoboGuice.getInjector(this).injectMembers(this);

        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        cameraView = (ScanbotCameraView) findViewById(R.id.camera);
        resultView = (ImageView) findViewById(R.id.result);

        ContourDetectorFrameHandler contourDetectorFrameHandler = ContourDetectorFrameHandler.attach(cameraView);

        PolygonView polygonView = (PolygonView) findViewById(R.id.polygonView);
        contourDetectorFrameHandler.addResultHandler(polygonView);

        AutoSnappingController.attach(cameraView, contourDetectorFrameHandler);

        cameraView.addPictureCallback(this);

        findViewById(R.id.snap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.takePicture(false);
            }
        });

        findViewById(R.id.flash).setOnClickListener(new View.OnClickListener() {

            boolean flashEnabled = false;

            @Override
            public void onClick(View v) {
                cameraView.useFlash(!flashEnabled);
                flashEnabled = !flashEnabled;
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
    public Map<Key<?>, Object> getScopedObjectMap() {
        return scopedObjects;
    }

    @Override
    public void onPictureTaken(byte[] image, int imageOrientation) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;

        final Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length, options);

        resultView.post(new Runnable() {
            @Override
            public void run() {
                resultView.setImageBitmap(bitmap);
                cameraView.startPreview();
            }
        });
    }

}
