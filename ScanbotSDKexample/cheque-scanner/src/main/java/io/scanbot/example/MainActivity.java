package io.scanbot.example;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import net.doo.snap.blob.BlobFactory;
import net.doo.snap.blob.BlobManager;
import net.doo.snap.entity.Blob;
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
                if (!checkChequeTraineddata()) {
                    return;
                }
                startActivity(ChequeScannerActivity.newIntent(MainActivity.this));
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
            final Blob blob1 = blobFactory.chequeTraineddataBlob();
            if (!blobManager.isBlobAvailable(blob1)) {
                new DownloadOCRDataTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
            }
            final Blob blob2 = blobFactory.accountNumberCascadeBlob();
            if (!blobManager.isBlobAvailable(blob2)) {
                new DownloadAccountCascadeDataTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
            }
            final Blob blob3 = blobFactory.routingNumberCascadeBlob();
            if (!blobManager.isBlobAvailable(blob3)) {
                new DownloadRoutingCascadeDataTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
            }
            if (!blobManager.isBlobAvailable(blob1)
                    && !blobManager.isBlobAvailable(blob2)
                    && !blobManager.isBlobAvailable(blob3)) {
                return;
            }
        } catch (IOException e) {
            logger.logException(e);
        }

        final Toast toast = Toast.makeText(this, "OCR data already downloaded. Try to scan a document with cheque scanner.", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private boolean checkChequeTraineddata() {
        try {
            final Blob blob1 = blobFactory.chequeTraineddataBlob();
            if (!blobManager.isBlobAvailable(blob1)) {
                new DownloadOCRDataTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
            }
            final Blob blob2 = blobFactory.accountNumberCascadeBlob();
            if (!blobManager.isBlobAvailable(blob2)) {
                new DownloadAccountCascadeDataTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
            }
            final Blob blob3 = blobFactory.routingNumberCascadeBlob();
            if (!blobManager.isBlobAvailable(blob3)) {
                new DownloadRoutingCascadeDataTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
            }
            if (blobManager.isBlobAvailable(blob1)
                    && blobManager.isBlobAvailable(blob2)
                    && blobManager.isBlobAvailable(blob3)) {
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

    /**
     * This AsyncTask is used here only for the sake of example. Please, try to avoid usage of
     * AsyncTasks in your application
     */
    private class DownloadOCRDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                blobManager.fetch(blobFactory.chequeTraineddataBlob(), false);
            } catch (IOException e) {
                logger.logException(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(MainActivity.this, "OCR data is downloading! Try to scan some Cheques when data will be downloaded...", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * This AsyncTask is used here only for the sake of example. Please, try to avoid usage of
     * AsyncTasks in your application
     */
    private class DownloadAccountCascadeDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                blobManager.fetch(blobFactory.accountNumberCascadeBlob(), false);
            } catch (IOException e) {
                logger.logException(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(MainActivity.this, "OCR data is downloading! Try to scan some Cheques when data will be downloaded...", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * This AsyncTask is used here only for the sake of example. Please, try to avoid usage of
     * AsyncTasks in your application
     */
    private class DownloadRoutingCascadeDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                blobManager.fetch(blobFactory.routingNumberCascadeBlob(), false);
            } catch (IOException e) {
                logger.logException(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(MainActivity.this, "OCR data is downloading! Try to scan some Cheques when data will be downloaded...", Toast.LENGTH_LONG).show();
        }
    }
}
