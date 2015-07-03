package io.scanbot.example;

import android.os.Bundle;
import android.widget.Toast;

import net.doo.snap.ui.RoboActionBarActivity;
import net.doo.snap.ui.camera.CameraPreviewExtendedFragment;
import net.doo.snap.util.ui.ViewUtils;


public class ExampleActivity extends RoboActionBarActivity {

    private CameraPreviewExtendedFragment cameraPreviewFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.example_activity);

        getSupportActionBar().hide();

        cameraPreviewFragment = (CameraPreviewExtendedFragment) getSupportFragmentManager().findFragmentById(net.doo.snap.R.id.camera);

        ViewUtils.postOnPreDraw(findViewById(net.doo.snap.R.id.camera), new Runnable() {
            @Override
            public void run() {
                cameraPreviewFragment.setPreviewMode(false);
            }
        });
        cameraPreviewFragment.setPictureTakenListener(new CameraPreviewExtendedFragment.PictureTakenListener() {
            @Override
            public void onPictureTaken(byte[] image, int imageOrientation) {
                Toast.makeText(ExampleActivity.this, "Picture delivered!", Toast.LENGTH_SHORT).show();
                //now you can save and start picture processing here (e.g. applying filters, cropping)
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraPreviewFragment.setPictureTakenListener(null);
    }

}
