package io.scanbot.example;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        // Create and show the dialog.
        DialogFragment newFragment = CameraDialogFragment.newInstance();
        newFragment.show(ft, "dialog");
    }
}
