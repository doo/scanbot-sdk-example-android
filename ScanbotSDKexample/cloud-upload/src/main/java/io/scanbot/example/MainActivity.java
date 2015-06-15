package io.scanbot.example;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.inject.Inject;

import net.doo.snap.IntentExtras;
import net.doo.snap.ui.RoboActionBarActivity;
import net.doo.snap.ui.ScanbotDialogFragment;
import net.doo.snap.ui.upload.SlackManualUploadFragment;
import net.doo.snap.ui.upload.UploadInfo;
import net.doo.snap.upload.CloudStorage;
import net.doo.snap.upload.DocumentUploader;
import net.doo.snap.upload.auth.ConnectionResult;
import net.doo.snap.upload.cloud.OnFileUploadListener;
import net.doo.snap.util.FileChooserUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends RoboActionBarActivity {
    private static final int FILE_SELECT_CODE = 0;
    public static final String TAG = "UPLOAD_EXAMPLE";
    public static final String UPLOAD_FRAGMENT_TAG = "UPLOAD_FRAGMENT_TAG";

    @Inject
    private DocumentUploader documentUploader;

    private EditText filePathView;
    private Spinner cloudStorageSpinner;
    private CloudStorage selectedStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        filePathView = (EditText) findViewById(R.id.editText);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(filePathView.getText().toString());
                if (!file.exists()) {
                    Toast.makeText(MainActivity.this, "Cannot find file! Choose another one.", Toast.LENGTH_LONG).show();
                    return;
                }

                selectedStorage = (CloudStorage) cloudStorageSpinner.getSelectedItem();

                //If selected cloud storage is connected start document uploading, otherwise - start authorization activity.
                if (CloudStorage.isConnected(selectedStorage, MainActivity.this)) {
                    startUpload(file, selectedStorage);
                } else {
                    startActivityForResult(new Intent(MainActivity.this, selectedStorage.getAuthActivityClass()), IntentExtras.AUTH_REQUEST_CODE);
                }
            }
        });
        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser();
            }
        });
        cloudStorageSpinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CloudStorage> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, CloudStorage.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cloudStorageSpinner.setAdapter(adapter);
        cloudStorageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedStorage = (CloudStorage) cloudStorageSpinner.getSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedStorage = CloudStorage.DROPBOX;
            }
        });
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    Log.d(TAG, "File Uri: " + uri.toString());
                    // Get the path
                    String path = FileChooserUtils.getPath(this, uri);
                    filePathView.setText(path);
                    Log.d(TAG, "File Path: " + path);
                    // Get the file instance
                    // File file = new File(path);
                    // Initiate the upload
                }
                break;
            case IntentExtras.AUTH_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    ConnectionResult event = data.getParcelableExtra(IntentExtras.CONNECTION_EXTRA);
                    if (event.isConnected()) {
                        File file = new File(filePathView.getText().toString());

                        //when cloud storage successfully connected - start file uploading
                        startUpload(file, event.getCloudStorage());
                    } else {
                        Toast.makeText(MainActivity.this, "Cannot connect to cloud storage!", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startUpload(final File file, CloudStorage target) {
        if (target == CloudStorage.SLACK) {
            // Starts channels, groups and direct messages selection for Slack.
            // For other cloud storages can be ignored.
            startSlackChooser(file, target);
        } else {
            new UploadFileTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, new UploadInfo(file));
        }
    }

    private void startSlackChooser(final File file, CloudStorage target) {
        ArrayList<UploadInfo> uploadInfos = new ArrayList<>();
        uploadInfos.add(new UploadInfo(file.getName(), null, null, null, null, null, file));
        ScanbotDialogFragment dialog = CloudStorage.getManualUploadFragment(target, uploadInfos);
        if (dialog != null) {
            ((SlackManualUploadFragment) dialog).setSlackUploadListener(new SlackManualUploadFragment.SlackUploadListener() {
                @Override
                public void startUpload(List<UploadInfo> list, CloudStorage cloudStorage, Bundle extras) {
                    new UploadFileTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, new UploadInfo(null, null, null, null, null, extras, file));
                }
            });
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(dialog, UPLOAD_FRAGMENT_TAG);
            transaction.commitAllowingStateLoss();
        }
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private class UploadFileTask extends AsyncTask<UploadInfo, Void, Void> {
        @Override
        protected Void doInBackground(UploadInfo... params) {
            if (selectedStorage == null) {
                return null;
            }

            try {
                documentUploader.uploadDocument(params[0], selectedStorage, new OnFileUploadListenerExample());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class OnFileUploadListenerExample implements OnFileUploadListener {

        @Override
        public void onUploadFinished(String id, CloudStorage target, String cloudFileId) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "File uploaded!", Toast.LENGTH_LONG).show();
                }
            });
        }

        @Override
        public void onUploadFailed(String id, CloudStorage target) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "Upload failed!", Toast.LENGTH_LONG).show();
                }
            });
        }

        @Override
        public void onUploadFailed(CloudStorage target, String label) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "Upload uploaded!", Toast.LENGTH_LONG).show();
                }
            });
        }

        @Override
        public void onUploadProgress(String id, int progress, CloudStorage target) {

        }

        @Override
        public void onUploadAuthorizationFailed(String id, CloudStorage storage) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "Cloud storage authorization error!", Toast.LENGTH_LONG).show();
                }
            });
            CloudStorage.disconnect(storage, MainActivity.this);
        }
    }
}
