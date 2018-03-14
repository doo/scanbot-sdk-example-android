package io.scanbot.example;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import net.doo.snap.ScanbotSDK;
import net.doo.snap.blob.BlobFactory;
import net.doo.snap.blob.BlobManager;
import net.doo.snap.entity.Blob;
import net.doo.snap.util.log.Logger;
import net.doo.snap.util.log.LoggerProvider;

import java.io.IOException;

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

        Button downloadBtn = findViewById(R.id.download_btn);
        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadMRZTraineddata();
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
                startActivity(MRZStillImageDetectionActivity.newIntent(MainActivity.this));
            }
        });
    }

    private void initDependencies() {
        scanbotSDK = new ScanbotSDK(this);
        blobManager = scanbotSDK.blobManager();
        blobFactory = scanbotSDK.blobFactory();
    }

    private void downloadMRZTraineddata() {
        try {
            final Blob mrzBlob = blobFactory.mrzTraineddataBlob();

            if (!blobManager.isBlobAvailable(mrzBlob)) {
                new DownloadOCRDataTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                return;
            }
        } catch (IOException e) {
            logger.logException(e);
        }

        final Toast toast = Toast.makeText(this, "OCR data already downloaded. Try to scan a document with MRZ.", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private boolean checkMRZTraineddata() {
        try {
            final Blob mrzBlob = blobFactory.mrzTraineddataBlob();
            if (blobManager.isBlobAvailable(mrzBlob)) {
                return true;
            }
        } catch (IOException e) {
            logger.logException(e);
        }

        final Toast toast = Toast.makeText(MainActivity.this, "Please download the OCR data first!", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
        return false;
    }

    /*
    This AsyncTask is used here only for the sake of example. Please, try to avoid usage of
    AsyncTasks in your application
     */
    private class DownloadOCRDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                blobManager.fetch(blobFactory.mrzTraineddataBlob(), false);
            } catch (IOException e) {
                logger.logException(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(MainActivity.this, "OCR data is downloading! Try to scan some MRZ when data will be downloaded...", Toast.LENGTH_LONG).show();
        }
    }
}
