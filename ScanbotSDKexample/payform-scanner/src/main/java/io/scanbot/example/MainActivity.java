package io.scanbot.example;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import net.doo.snap.ScanbotSDK;
import net.doo.snap.blob.BlobFactory;
import net.doo.snap.blob.BlobManager;
import net.doo.snap.entity.Blob;
import net.doo.snap.entity.Language;
import net.doo.snap.util.log.Logger;
import net.doo.snap.util.log.LoggerProvider;

import java.io.IOException;
import java.util.Collection;

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

        Button downloadBtn = (Button) findViewById(R.id.download_btn);
        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadOcrAndBanksData();
            }
        });
        Button scannerBtn = (Button) findViewById(R.id.scanner_btn);
        scannerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(PayformScannerActivity.newIntent(MainActivity.this));
            }
        });
    }

    private void initDependencies() {
        scanbotSDK = new ScanbotSDK(this);
        blobManager = scanbotSDK.blobManager();
        blobFactory = scanbotSDK.blobFactory();
    }

    private void downloadOcrAndBanksData() {
        Collection<Blob> blobs = null;
        try {
            blobs = blobFactory.ocrLanguageBlobs(Language.DEU);
            blobs.addAll(blobFactory.ocrLanguageBlobs(Language.ENG));
            blobs.addAll(blobFactory.languageDetectorBlobs());

            blobs.add(blobFactory.bankDataBlob());

            for (Blob blob : blobs) {
                if (!blobManager.isBlobAvailable(blob)) {
                    new DownloadOCRAndBanksDataTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                    return;
                }
            }
        } catch (IOException e) {
            logger.logException(e);
        }

        Toast.makeText(MainActivity.this, "OCR and banks data is downloaded! Try to scan some pay form...", Toast.LENGTH_LONG).show();
    }

    /*
    This AsyncTask is used here only for the sake of example. Please, try to avoid usage of
    AsyncTasks in your application
     */
    private class DownloadOCRAndBanksDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Collection<Blob> blobs = blobFactory.ocrLanguageBlobs(Language.DEU);
                blobs.addAll(blobFactory.ocrLanguageBlobs(Language.ENG));
                blobs.addAll(blobFactory.languageDetectorBlobs());

                blobs.add(blobFactory.bankDataBlob());
                for (Blob blob : blobs) {
                    blobManager.fetch(blob, false);
                }
            } catch (IOException e) {
                logger.logException(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(MainActivity.this, "OCR and banks data is downloading! Try to scan some pay form when data will be downloaded...", Toast.LENGTH_LONG).show();
        }
    }
}
