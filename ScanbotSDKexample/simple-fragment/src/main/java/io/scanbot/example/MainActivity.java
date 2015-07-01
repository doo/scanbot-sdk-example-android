package io.scanbot.example;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.WindowCompat;
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
import net.doo.snap.util.thread.MimeUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends RoboActionBarActivity {

    @Inject
    private DocumentProcessor documentProcessor;
    @Inject
    private Cleaner cleaner;

    private View snappingFragment;
    private View progressView;

    private BroadcastReceiver snappingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onSnappingResult(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_main);

        progressView = findViewById(R.id.progressBar);
        snappingFragment = findViewById(R.id.snapping_fragment);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(snappingReceiver, new IntentFilter(Constants.SNAPPING_RESULT_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(snappingReceiver);
    }

    private void onSnappingResult(Intent data) {
        Parcelable[] parcelableArray = data.getParcelableArrayExtra(Constants.SNAPPING_RESULT);
        if (parcelableArray == null) {
            return;
        }

        DocumentDraft[] documentDrafts = Arrays.copyOf(parcelableArray, parcelableArray.length, DocumentDraft[].class);

        new ProcessDocumentTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, documentDrafts);
        progressView.setVisibility(View.VISIBLE);
        snappingFragment.setVisibility(View.GONE);
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
