package io.scanbot.example;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.inject.Inject;

import net.doo.snap.Constants;
import net.doo.snap.blob.BlobFactory;
import net.doo.snap.blob.BlobManager;
import net.doo.snap.entity.Blob;
import net.doo.snap.entity.Document;
import net.doo.snap.entity.Language;
import net.doo.snap.entity.OcrStatus;
import net.doo.snap.persistence.cleanup.Cleaner;
import net.doo.snap.process.DocumentProcessingResult;
import net.doo.snap.process.DocumentProcessor;
import net.doo.snap.process.util.DocumentDraft;
import net.doo.snap.ui.RoboActionBarActivity;
import net.doo.snap.ui.SnappingActivity;
import net.doo.snap.util.thread.MimeUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


public class MainActivity extends RoboActionBarActivity {

    private static final int REQUEST_CODE = 100;

    @Inject
    private DocumentProcessor documentProcessor;
    @Inject
    private Cleaner cleaner;
    @Inject
    private BlobManager blobManager;
    @Inject
    private BlobFactory blobFactory;

    private View progressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.scanButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collection<Blob> engOcrBlobs;
                try {
                    engOcrBlobs = blobFactory.ocrLanguageBlobs(Language.ENG);
                    for (Blob blob : engOcrBlobs) {
                        if (!blobManager.isBlobAvailable(blob)) {
                            Toast.makeText(MainActivity.this, "Download OCR data first!", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                startActivityForResult(
                        new Intent(v.getContext(), SnappingActivity.class),
                        REQUEST_CODE
                );
            }
        });
        findViewById(R.id.downloadOcrData).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collection<Blob> engOcrBlobs = null;
                try {
                    /*
                    In example OCR will be performed only for english language documents.
                    But you can use all supported languages in net.doo.snap.entity.Language enum.
                     */
                    engOcrBlobs = blobFactory.ocrLanguageBlobs(Language.ENG);
                    for (Blob blob : engOcrBlobs) {
                        if (!blobManager.isBlobAvailable(blob)) {
                            progressView.setVisibility(View.VISIBLE);
                            new DownloadOCRDataTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                            return;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Toast.makeText(MainActivity.this, "OCR data is downloaded! Try to scan some document...", Toast.LENGTH_LONG).show();
            }
        });
        progressView = findViewById(R.id.progressBar);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != REQUEST_CODE || resultCode != RESULT_OK) {
            return;
        }

        Parcelable[] parcelableArray = data.getParcelableArrayExtra(Constants.SNAPPING_RESULT);
        if (parcelableArray == null) {
            return;
        }

        DocumentDraft[] documentDrafts = Arrays.copyOf(parcelableArray, parcelableArray.length, DocumentDraft[].class);

        new ProcessDocumentTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, documentDrafts);
        progressView.setVisibility(View.VISIBLE);
    }

    private void openDocument(DocumentProcessingResult documentProcessingResult) {
        Document document = documentProcessingResult.getDocument();
        File documentFile = documentProcessingResult.getDocumentFile();

        Intent openIntent = new Intent();
        openIntent.setAction(Intent.ACTION_VIEW);
        openIntent.setDataAndType(
                Uri.fromFile(documentFile),
                MimeUtils.getMimeByName(document.getName())
        );

        if (openIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(openIntent);
        } else {
            Toast.makeText(MainActivity.this, getString(net.doo.snap.R.string.content_action_error), Toast.LENGTH_LONG).show();
        }
    }

    /*
    This AsyncTask is used here only for the sake of example. Please, try to avoid usage of
    AsyncTasks in your application
     */
    private class ProcessDocumentTask extends AsyncTask<DocumentDraft[], Void, DocumentProcessingResult> {

        @Override
        protected DocumentProcessingResult doInBackground(DocumentDraft[]... params) {
            DocumentProcessingResult result = null;

            for (DocumentDraft draft : params[0]) {
                try {

                    /*
                    Set OCR status for document processor.
                    OcrStatus.PENDING - OCR well be performed only if preference PreferencesConstants.PERFORM_OCR is true
                    and PreferencesConstants.OCR_ONLY_WHILE_CHARGING is false (or true and device is charging).

                    OcrStatus.PENDING_FORCED - OCR will be performed. Ignores all preferences flags.

                    OcrStatus.PENDING_ON_CHARGER - OCR will be performed only if device is charging. Ignores all preferences flags.
                    */
                    draft.getDocument().setOcrStatus(OcrStatus.PENDING);

                    result = documentProcessor.processDocument(draft);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            cleaner.cleanUp();

            return result;
        }

        @Override
        protected void onPostExecute(DocumentProcessingResult documentProcessingResult) {
            progressView.setVisibility(View.GONE);

            //open first document
            if (documentProcessingResult != null) {
                Log.i("Scanbot SDK OCR example", "First document content:\n" + documentProcessingResult.getDocument().getOcrText());
                openDocument(documentProcessingResult);
            }
        }

    }

    /*
    This AsyncTask is used here only for the sake of example. Please, try to avoid usage of
    AsyncTasks in your application
     */
    private class DownloadOCRDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Collection<Blob> engOcrBlobs = blobFactory.ocrLanguageBlobs(Language.ENG);
                for (Blob blob : engOcrBlobs) {
                    blobManager.fetch(blob, false);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressView.setVisibility(View.GONE);
            Toast.makeText(MainActivity.this, "English language OCR data is downloading! Try to scan some document when OCR data will be downloaded...", Toast.LENGTH_LONG).show();
        }
    }

}
