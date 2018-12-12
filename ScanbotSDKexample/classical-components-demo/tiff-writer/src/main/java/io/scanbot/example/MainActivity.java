package io.scanbot.example;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import io.scanbot.sdk.ScanbotSDK;
import net.doo.snap.util.FileChooserUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.scanbot.tiffwriter.TIFFWriter;

public class MainActivity extends AppCompatActivity {

    private static final int SELECT_PICTURE_REQUEST = 100;
    private static final String IMAGE_TYPE = "image/*";

    private TIFFWriter tiffWriter;

    private View progressView;
    private TextView resultTextView;
    private CheckBox binarizationCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initDependencies();

        resultTextView = findViewById(R.id.result);
        binarizationCheckBox = findViewById(R.id.binarizationCheckBox);
        findViewById(R.id.scanButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
        progressView = findViewById(R.id.progressBar);
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType(IMAGE_TYPE);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }

        startActivityForResult(
                Intent.createChooser(intent, "Select picture"),
                SELECT_PICTURE_REQUEST
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode != SELECT_PICTURE_REQUEST || resultCode != RESULT_OK) {
            return;
        }

        processGalleryResult(intent);

        progressView.setVisibility(View.VISIBLE);
    }

    private void processGalleryResult(Intent data) {
        ClipData clipData = data.getClipData();
        Uri imageUri = data.getData();
        if (clipData != null && clipData.getItemCount() > 0) {
            List<Uri> imageUris = getImageUris(clipData);
            new WriteMultiPageTIFFImageTask(imageUris).execute();
        } else if (imageUri != null) {
            new WriteTIFFImageTask(imageUri).execute();
        }
    }

    private List<Uri> getImageUris(ClipData clipData) {
        int itemsCount = clipData.getItemCount();
        List<Uri> imageUris = new ArrayList<>();
        for (int i = 0; i < itemsCount; i++) {
            ClipData.Item item = clipData.getItemAt(i);
            Uri uri = item.getUri();
            if (uri != null) {
                imageUris.add(uri);
            }
        }

        return imageUris;
    }

    private void initDependencies() {
        ScanbotSDK scanbotSDK = new ScanbotSDK(this);
        tiffWriter = scanbotSDK.tiffWriter();
    }

    /*
    This AsyncTask is used here only for the sake of example. Please, try to avoid usage of
    AsyncTasks in your application
     */
    private class WriteTIFFImageTask extends AsyncTask<Void, Void, Boolean> {

        private final File imageFile;
        private final File resultFile;

        private WriteTIFFImageTask(Uri imageUri) {
            String imagePath = FileChooserUtils.getPath(MainActivity.this, imageUri);
            this.imageFile = new File(imagePath);
            resultFile = new File(getExternalFilesDir(null).getPath() + "/tiff_result_" + System.currentTimeMillis() + ".tiff");
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (binarizationCheckBox.isChecked()) {
                return tiffWriter.writeBinarizedSinglePageTIFFFromFile(imageFile, resultFile);
            } else {
                return tiffWriter.writeSinglePageTIFFFromFile(imageFile, resultFile);
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            progressView.setVisibility(View.GONE);

            if (result) {
                Log.i("TIFF example", "Generated TIFF image path:\n" + resultFile.getPath());
                resultTextView.setText("Result TIFF path:\n" + resultFile.getPath());
            }
        }

    }

    /*
    This AsyncTask is used here only for the sake of example. Please, try to avoid usage of
    AsyncTasks in your application
     */
    private class WriteMultiPageTIFFImageTask extends AsyncTask<Void, Void, Boolean> {

        private final List<File> images = new ArrayList<>();
        private final File resultFile;

        private WriteMultiPageTIFFImageTask(List<Uri> imageUris) {
            for (Uri uri : imageUris) {
                String imagePath = FileChooserUtils.getPath(MainActivity.this, uri);
                images.add(new File(imagePath));
            }

            String resultFilePath = getExternalFilesDir(null).getPath() + "/multi_page_tiff_result_" + System.currentTimeMillis() + ".tiff";
            resultFile = new File(resultFilePath);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (binarizationCheckBox.isChecked()) {
                return tiffWriter.writeBinarizedMultiPageTIFFFromFileList(images, resultFile);
            } else {
                return tiffWriter.writeMultiPageTIFFFromFileList(images, resultFile);
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            progressView.setVisibility(View.GONE);

            if (result) {
                Log.i("TIFF example", "Generated multi page TIFF path:\n" + resultFile.getPath());
                resultTextView.setText("Result TIFF path:\n" + resultFile.getPath());
            }
        }

    }
}
