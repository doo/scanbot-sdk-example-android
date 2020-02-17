package io.scanbot.example;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import net.doo.snap.lib.detector.DetectionResult;
import net.doo.snap.util.FileChooserUtils;
import net.doo.snap.util.bitmap.BitmapUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import io.scanbot.sdk.ScanbotSDK;
import io.scanbot.sdk.persistence.Page;
import io.scanbot.sdk.process.ImageFilterType;

public class SelectImageActivity extends AppCompatActivity {
    private static final int PHOTOLIB_REQUEST_CODE = 5711;

    private View progressView;

    private ScanbotSDK scanbotSDK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_image);
        progressView = findViewById(R.id.progressBar);

        scanbotSDK = new ScanbotSDK(this);

        askPermission();

        findViewById(R.id.import_from_lib_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });
    }

    private void askPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                    999);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PHOTOLIB_REQUEST_CODE && resultCode == RESULT_OK) {
            final Uri imageUri = data.getData();
            new ImportImageToPageTask(imageUri).execute();
            progressView.setVisibility(View.VISIBLE);
        }
    }

    private void openGallery() {
        final Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(Intent.createChooser(intent, "Select picture"), PHOTOLIB_REQUEST_CODE);
    }

    private Bitmap loadImage(final Uri imageUri) {
        final String filePath = FileChooserUtils.getPath(this, imageUri);
        return BitmapUtils.decodeQuietly(filePath, null);
    }

    private class ImportImageToPageTask extends AsyncTask {
        private final Uri imageUri;

        private ImportImageToPageTask(final Uri imageUri) {
            this.imageUri = imageUri;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            final String pageId = scanbotSDK.pageFileStorage().add(loadImage(imageUri));
            final List<PointF> emptyPolygon = Collections.emptyList();
            final Page newPage = new Page(pageId, emptyPolygon, DetectionResult.OK, ImageFilterType.NONE);

            try {
                return scanbotSDK.pageProcessor().detectDocument(newPage);
            } catch (final IOException ex) {
                Log.e("ImportImageToPageTask", "Error detecting document on page " + newPage.getPageId());
                return null;
            }
        }

        @Override
        protected void onPostExecute(final Object result) {
            super.onPostExecute(result);

            progressView.setVisibility(View.GONE);

            if (result != null) {
                final Intent intent = FilterTunesActivity.newIntent(SelectImageActivity.this, (Page) result);
                startActivity(intent);
            }
        }
    }
}
