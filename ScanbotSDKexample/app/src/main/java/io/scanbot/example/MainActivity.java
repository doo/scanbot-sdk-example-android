package io.scanbot.example;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import net.doo.snap.Constants;
import net.doo.snap.entity.Document;
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
import java.util.concurrent.Executors;

import roboguice.RoboGuice;


public class MainActivity extends RoboActionBarActivity {

    private static final int REQUEST_CODE = 100;
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
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            DocumentDraft[] documentDrafts = null;
            Parcelable[] parcelableArray = data.getParcelableArrayExtra(Constants.SNAPPING_RESULT);
            if (parcelableArray != null) {
                documentDrafts = Arrays.copyOf(parcelableArray, parcelableArray.length, DocumentDraft[].class);
                new AsyncTask<DocumentDraft[], Void, List<DocumentProcessingResult>>() {

                    @Override
                    protected List<DocumentProcessingResult> doInBackground(DocumentDraft[]... params) {
                        List<DocumentProcessingResult> results = new ArrayList<>();

                        DocumentProcessor documentProcessor = RoboGuice.getInjector(MainActivity.this).getInstance(DocumentProcessor.class);
                        for (DocumentDraft draft : params[0]) {
                            try {
                                results.add(documentProcessor.processDocument(draft));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        return results;
                    }

                    @Override
                    protected void onPostExecute(List<DocumentProcessingResult> documentProcessingResults) {
                        progressView.setVisibility(View.GONE);

                        //open first document
                        if (documentProcessingResults.size() > 0) {
                            Intent openIntent = new Intent();
                            openIntent.setAction(Intent.ACTION_VIEW);
                            Document document = documentProcessingResults.get(0).getDocument();
                            File documentFile = documentProcessingResults.get(0).getDocumentFile();
                            openIntent.setDataAndType(Uri.fromFile(documentFile), MimeUtils.getMimeByName(document.getName()));
                            if (openIntent.resolveActivity(getPackageManager()) != null) {
                                startActivity(openIntent);
                            } else {
                                Toast.makeText(MainActivity.this, getString(net.doo.snap.R.string.content_action_error), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, documentDrafts);
                progressView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
