package io.scanbot.example;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import net.doo.snap.lib.detector.DetectionResult;
import net.doo.snap.util.FileChooserUtils;
import net.doo.snap.util.bitmap.BitmapUtils;
import net.doo.snap.util.thread.MimeUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import io.scanbot.sdk.ScanbotSDK;
import io.scanbot.sdk.persistence.Page;
import io.scanbot.sdk.persistence.PageFileStorage;
import io.scanbot.sdk.process.ImageFilterType;
import io.scanbot.sdk.process.PDFPageSize;
import io.scanbot.sdk.process.PDFRenderer;
import io.scanbot.sdk.process.PageProcessor;


public class MainActivity extends AppCompatActivity {

    private static final int SELECT_PICTURE_REQUEST = 100;
    private static final String IMAGE_TYPE = "image/*";

    private PDFRenderer pdfRenderer;

    private View progressView;
    private PageFileStorage pageFileStorage;
    private PageProcessor pageProcessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        askPermission();

        initializeDependencies();

        findViewById(R.id.scanButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

    private void initializeDependencies() {
        ScanbotSDK scanbotSDK = new ScanbotSDK(this);
        pdfRenderer = scanbotSDK.pdfRenderer();
        pageFileStorage = scanbotSDK.pageFileStorage();
        pageProcessor = scanbotSDK.pageProcessor();
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType(IMAGE_TYPE);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        startActivityForResult(
                Intent.createChooser(intent, "Select picture"),
                SELECT_PICTURE_REQUEST
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode != SELECT_PICTURE_REQUEST || resultCode != RESULT_OK) {
            return;
        }

        ArrayList<Uri> imageUris = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (intent.getClipData() != null) {
                ClipData mClipData = intent.getClipData();
                for (int i = 0; i < mClipData.getItemCount(); i++) {

                    ClipData.Item item = mClipData.getItemAt(i);
                    Uri uri = item.getUri();
                    imageUris.add(uri);
                }
            } else if (intent.getData() != null) {
                imageUris.add(intent.getData());
            }
        }

        new ProcessDocumentTask(imageUris).execute();
        progressView.setVisibility(View.VISIBLE);
    }

    private void openDocument(File processedDocument) {

        Intent openIntent = new Intent();
        openIntent.setAction(Intent.ACTION_VIEW);
        openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        openIntent.setDataAndType(
                FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", processedDocument),
                MimeUtils.getMimeByName(processedDocument.getName())
        );

        if (openIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(openIntent);
        } else {
            Toast.makeText(MainActivity.this, "Error while opening the document", Toast.LENGTH_LONG).show();
        }
    }

    /*
    This AsyncTask is used here only for the sake of example. Please, try to avoid usage of
    AsyncTasks in your application
     */
    private class ProcessDocumentTask extends AsyncTask<Void, Void, File> {

        private final ArrayList<Uri> imageUris;

        private ProcessDocumentTask(ArrayList<Uri> imageUris) {
            this.imageUris = imageUris;
        }

        @Override
        protected File doInBackground(Void... voids) {
            try {
                List<Page> pages = new ArrayList<>();
                for (Uri imageUri : imageUris) {
                    Bitmap bitmap = loadImage(imageUri);

                    String newPageId = pageFileStorage.add(bitmap);
                    Page page = new Page(newPageId, Collections.<PointF>emptyList(), DetectionResult.OK, ImageFilterType.GRAYSCALE);
                    pageProcessor.detectDocument(page);
                    pages.add(page);
                }

                return pdfRenderer.renderDocumentFromPages(pages, PDFPageSize.FIXED_A4);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        private Bitmap loadImage(Uri imageUri) throws IOException {
            String imagePath = FileChooserUtils.getPath(MainActivity.this, imageUri);
            Bitmap bitmap = BitmapUtils.decodeQuietly(imagePath, null);

            if (bitmap == null) {
                throw new IOException("Bitmap is null");
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(File processedDocument) {
            progressView.setVisibility(View.GONE);

            //open first document
            if (processedDocument != null) {
                openDocument(processedDocument);
            }
        }

    }

}
