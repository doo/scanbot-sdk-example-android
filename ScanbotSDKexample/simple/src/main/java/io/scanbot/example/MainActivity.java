package io.scanbot.example;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.Toast;

import com.google.inject.Inject;

import net.doo.snap.Constants;
import net.doo.snap.entity.Document;
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
import java.util.List;


public class MainActivity extends RoboActionBarActivity {

    private static final int REQUEST_CODE = 100;

    @Inject
    private DocumentProcessor documentProcessor;
    @Inject
    private Cleaner cleaner;

    private View progressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.scanButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(
                        new Intent(v.getContext(), SnappingActivity.class),
                        REQUEST_CODE
                );
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
    private class ProcessDocumentTask extends AsyncTask<DocumentDraft[], Void, List<DocumentProcessingResult>> {

        @Override
        protected List<DocumentProcessingResult> doInBackground(DocumentDraft[]... params) {
            List<DocumentProcessingResult> results = new ArrayList<>();

            for (DocumentDraft draft : params[0]) {
                try {
                    results.add(documentProcessor.processDocument(draft));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            cleaner.cleanUp();

            return results;
        }

        @Override
        protected void onPostExecute(List<DocumentProcessingResult> documentProcessingResults) {
            progressView.setVisibility(View.GONE);

            //open first document
            if (documentProcessingResults.size() > 0) {
                openDocument(documentProcessingResults.get(0));
            }
        }

    }

}
