package io.scanbot.example;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import net.doo.snap.util.FileChooserUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import io.scanbot.sdk.ScanbotSDK;
import io.scanbot.tiffwriter.TIFFWriter;
import io.scanbot.tiffwriter.model.TIFFImageWriterCompressionOptions;
import io.scanbot.tiffwriter.model.TIFFImageWriterParameters;
import io.scanbot.tiffwriter.model.TIFFImageWriterUserDefinedField;

public class MainActivity extends AppCompatActivity {

    private static final int SELECT_PICTURE_REQUEST = 100;
    private static final String IMAGE_TYPE = "image/*";

    private TIFFWriter tiffWriter;

    private View progressView;
    private TextView resultTextView;
    private CheckBox binarizationCheckBox;
    private CheckBox customFieldsCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        askPermission();

        tiffWriter = new ScanbotSDK(this).tiffWriter();

        resultTextView = findViewById(R.id.resultTextView);
        binarizationCheckBox = findViewById(R.id.binarizationCheckBox);
        customFieldsCheckBox = findViewById(R.id.customFieldsCheckBox);

        findViewById(R.id.selectImagesButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resultTextView.setText("");
                openGallery();
            }
        });
        progressView = findViewById(R.id.progressBar);
    }

    private void askPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        999);
            }
        }
    }

    private void openGallery() {
        final Intent intent = new Intent();
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

       /* if (!new ScanbotSDK(this).isLicenseValid()) {
            Toast.makeText(this,
                    "Scanbot SDK license is not valid or the trial minute has expired.",
                    Toast.LENGTH_LONG).show();
            return;
        }*/

        progressView.setVisibility(View.VISIBLE);
        processGalleryResult(intent);
    }

    private void processGalleryResult(final Intent data) {
        final ClipData clipData = data.getClipData();
        final Uri singleImageUri = data.getData();

        final List<Uri> imageUris = new ArrayList<>();

        if (clipData != null && clipData.getItemCount() > 0) {
            // multiple images were selected
            imageUris.addAll(getImageUris(clipData));
        } else if (singleImageUri != null) {
            // a single image was selected
            imageUris.add(singleImageUri);
        }

        new WriteTIFFImageTask(imageUris, binarizationCheckBox.isChecked(), customFieldsCheckBox.isChecked()).execute();
    }

    private List<Uri> getImageUris(final ClipData clipData) {
        final List<Uri> imageUris = new ArrayList<>();
        for (int i = 0; i < clipData.getItemCount(); i++) {
            final Uri uri = clipData.getItemAt(i).getUri();
            if (uri != null) {
                imageUris.add(uri);
            }
        }
        return imageUris;
    }


    /*
    This AsyncTask is used here only for the sake of example. Please, try to avoid usage of
    AsyncTasks in your application
     */
    private class WriteTIFFImageTask extends AsyncTask<Void, Void, Boolean> {

        private final List<File> images = new ArrayList<>();
        private final File resultFile;
        private final TIFFImageWriterParameters parameters;
        private final int dpi = 200;

        private WriteTIFFImageTask(final List<Uri> imageUris, final boolean binarize, final boolean addCustomFields) {
            for (final Uri uri : imageUris) {
                final String imageFilePath = FileChooserUtils.getPath(MainActivity.this, uri);
                images.add(new File(imageFilePath));
            }

            resultFile = new File(getExternalFilesDir(null).getPath() + "/tiff_result_" + System.currentTimeMillis() + ".tiff");

            // Please note that some compression types are only compatible for binarized images (1-bit encoded black & white images)!
            final TIFFImageWriterCompressionOptions compression = (binarize ?
                    TIFFImageWriterCompressionOptions.COMPRESSION_CCITTFAX4 : TIFFImageWriterCompressionOptions.COMPRESSION_ADOBE_DEFLATE);

            // Optional custom fields as custom meta data (please refer to TIFF and EXIF specifications):
            final ArrayList<TIFFImageWriterUserDefinedField> customFields = new ArrayList<>();
            if (addCustomFields) {
                customFields.add(TIFFImageWriterUserDefinedField.fieldWithStringValue("testStringValue", "custom_string_field_name", 600));
                customFields.add(TIFFImageWriterUserDefinedField.fieldWithIntValue(100, "custom_number_field_name", 601));
                customFields.add(TIFFImageWriterUserDefinedField.fieldWithDoubleValue(42.001, "custom_double_field_name", 602));
            }

            parameters = new TIFFImageWriterParameters(binarize, dpi, compression, customFields);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return tiffWriter.writeTIFFFromFiles(images, resultFile, parameters);
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            progressView.setVisibility(View.GONE);

            if (result) {
                resultTextView.setText("TIFF file created:\n" + resultFile.getPath());
            } else {
                Toast.makeText(MainActivity.this,
                        "ERROR: Could not create TIFF file.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

}
