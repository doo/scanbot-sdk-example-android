package io.scanbot.example;

import android.os.Bundle;
import android.widget.Toast;

import net.doo.snap.ui.RoboActionBarActivity;
import net.doo.snap.ui.camera.ScanbotCameraFragment;


public class ExampleActivity extends RoboActionBarActivity implements ScanbotCameraFragment.PictureTakenListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.example_activity);

        getSupportActionBar().hide();
    }

    @Override
    public void onPictureTaken(byte[] image, int imageOrientation) {
        Toast.makeText(this, "Picture delivered!", Toast.LENGTH_SHORT).show();
        //now you can save and start picture processing here (e.g. applying filters, cropping)
    }

}
