package io.scanbot.example;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import net.doo.snap.blob.BlobFactory;
import net.doo.snap.blob.BlobManager;
import net.doo.snap.util.log.Logger;
import net.doo.snap.util.log.LoggerProvider;

import java.io.IOException;

import io.scanbot.sdk.ScanbotSDK;

public class MainActivity extends AppCompatActivity {

    private final Logger logger = LoggerProvider.getLogger();

    private ScanbotSDK scanbotSDK;
    private BlobManager blobManager;
    private BlobFactory blobFactory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initDependencies();

        Button downloadBtn = findViewById(R.id.prepare_traineddata_btn);
        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareMRZTraineddata();
            }
        });

        Button liveScannerBtn = findViewById(R.id.live_scanner_btn);
        liveScannerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkMRZTraineddata()) { return; }
                startActivity(MRZLiveDetectionActivity.newIntent(MainActivity.this));
            }
        });

        Button stillImageScannerBtn = findViewById(R.id.still_image_detection_btn);
        stillImageScannerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkMRZTraineddata()) { return; }
                final Intent intent = new Intent(getApplicationContext(), MRZStillImageDetectionActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initDependencies() {
        scanbotSDK = new ScanbotSDK(this);
        blobManager = scanbotSDK.blobManager();
        blobFactory = scanbotSDK.blobFactory();
    }

    private void prepareMRZTraineddata() {
        try {
            if (!blobManager.isBlobAvailable(blobFactory.mrzTraineddataBlob()) ||
                    !blobManager.isBlobAvailable(blobFactory.mrzCascadeBlob())) {
                new PrepareTraineddataBlobsTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                return;
            }
        } catch (final IOException e) {
            logger.logException(e);
            return;
        }

        final Toast toast = Toast.makeText(this, "MRZ trained data prepared. Try to scan an ID card with MRZ now.", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private boolean checkMRZTraineddata() {
        try {
            if (blobManager.isBlobAvailable(blobFactory.mrzTraineddataBlob()) &&
                    blobManager.isBlobAvailable(blobFactory.mrzCascadeBlob())) {
                return true;
            }
        } catch (IOException e) {
            logger.logException(e);
        }

        final Toast toast = Toast.makeText(MainActivity.this, "Please fetch/prepare the MRZ trained data first!", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
        return false;
    }


    /*
    This AsyncTask is used here only for the sake of example. Please, try to avoid usage of
    AsyncTasks in your application
     */
    private class PrepareTraineddataBlobsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                blobManager.fetch(blobFactory.mrzTraineddataBlob(), false);
                blobManager.fetch(blobFactory.mrzCascadeBlob(), false);
            } catch (IOException e) {
                logger.logException(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(MainActivity.this, "Fetching/preparing MRZ trained data... \nTry to scan an ID card with MRZ when trained data is ready.", Toast.LENGTH_LONG).show();
        }
    }
}
