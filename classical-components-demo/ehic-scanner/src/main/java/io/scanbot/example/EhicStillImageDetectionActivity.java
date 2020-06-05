package io.scanbot.example;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import net.doo.snap.camera.CameraPreviewMode;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import io.scanbot.hicscanner.model.HealthInsuranceCardDetectionStatus;
import io.scanbot.hicscanner.model.HealthInsuranceCardRecognitionResult;
import io.scanbot.sdk.ScanbotSDK;
import io.scanbot.sdk.core.contourdetector.DetectionResult;
import io.scanbot.sdk.hicscanner.HealthInsuranceCardScanner;
import io.scanbot.sdk.persistence.Page;
import io.scanbot.sdk.persistence.PageFileStorage;
import io.scanbot.sdk.process.ImageFilterType;
import io.scanbot.sdk.ui.view.base.configuration.CameraOrientationMode;
import io.scanbot.sdk.ui.view.camera.DocumentScannerActivity;
import io.scanbot.sdk.ui.view.camera.configuration.DocumentScannerConfiguration;
import io.scanbot.sdk.ui.view.edit.CroppingActivity;
import io.scanbot.sdk.ui.view.edit.configuration.CroppingConfiguration;
import io.scanbot.sdk.util.FileChooserUtils;
import io.scanbot.sdk.util.bitmap.BitmapUtils;

public class EhicStillImageDetectionActivity extends AppCompatActivity {

    private static final int DOCUMENT_SCANNER_REQUEST_CODE = 4711;
    private static final int PHOTOLIB_REQUEST_CODE = 5711;
    private static final int CROP_REQUEST_CODE = 6711;

    private ImageView resultImageView;
    private Button cropBtn;
    private Button runRecognitionBtn;
    private View progressView;

    private Page page;

    private ScanbotSDK scanbotSDK;
    private HealthInsuranceCardScanner healthInsuranceCardScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ehic_still_image_detection);

        resultImageView = findViewById(R.id.resultImageView);
        progressView = findViewById(R.id.progressBar);

        scanbotSDK = new ScanbotSDK(this);
        healthInsuranceCardScanner = scanbotSDK.healthInsuranceCardScanner();

        findViewById(R.id.start_scanner_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final DocumentScannerConfiguration configuration = new DocumentScannerConfiguration();
                configuration.setMultiPageEnabled(false);
                configuration.setMultiPageButtonHidden(true);
                configuration.setAutoSnappingEnabled(false);
                configuration.setCameraPreviewMode(CameraPreviewMode.FIT_IN);
                configuration.setOrientationLockMode(CameraOrientationMode.PORTRAIT);
                configuration.setIgnoreBadAspectRatio(true);

                final Intent intent = DocumentScannerActivity.newIntent(EhicStillImageDetectionActivity.this, configuration);
                startActivityForResult(intent, DOCUMENT_SCANNER_REQUEST_CODE);
            }
        });

        findViewById(R.id.import_from_lib_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        cropBtn = findViewById(R.id.crop_btn);
        cropBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final CroppingConfiguration configuration = new CroppingConfiguration();
                configuration.setPage(page);
                final Intent intent = CroppingActivity.newIntent(EhicStillImageDetectionActivity.this, configuration);
                startActivityForResult(intent, CROP_REQUEST_CODE);
            }
        });

        runRecognitionBtn = findViewById(R.id.run_recognition_btn);
        runRecognitionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runRecognition();
            }
        });
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == DOCUMENT_SCANNER_REQUEST_CODE && resultCode == RESULT_OK) {
            final Parcelable[] parcelablePages = data.getParcelableArrayExtra(DocumentScannerActivity.SNAPPED_PAGE_EXTRA);
            this.page = (Page) parcelablePages[0];
            displayPreviewImage();
            cropBtn.setVisibility(View.VISIBLE);
            runRecognitionBtn.setVisibility(View.VISIBLE);
            return;
        }

        if (requestCode == PHOTOLIB_REQUEST_CODE && resultCode == RESULT_OK) {
            final Uri imageUri = data.getData();
            new ImportImageToPageTask(imageUri).execute();
            progressView.setVisibility(View.VISIBLE);
            return;
        }

        if (requestCode == CROP_REQUEST_CODE && resultCode == RESULT_OK) {
            this.page = data.getParcelableExtra(CroppingActivity.EDITED_PAGE_EXTRA);
            displayPreviewImage();
            return;
        }
    }

    private void openGallery() {
        final Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(Intent.createChooser(intent, "Select picture"), PHOTOLIB_REQUEST_CODE);
    }

    private void displayPreviewImage() {
        final Uri imageUri = scanbotSDK.pageFileStorage().getPreviewImageURI(page.getPageId(), PageFileStorage.PageFileType.DOCUMENT);
        resultImageView.setImageBitmap(loadImage(imageUri));
    }

    private Bitmap loadImage(final Uri imageUri) {
        final String filePath = FileChooserUtils.getPath(this, imageUri);
        return BitmapUtils.decodeQuietly(filePath, null);
    }

    private void runRecognition() {
        new RecognizeEhicTask(page).execute();
        progressView.setVisibility(View.VISIBLE);
    }

    private class RecognizeEhicTask extends AsyncTask {
        private final Page page;

        private RecognizeEhicTask(final Page page) {
            this.page = page;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            final Uri imageUri = scanbotSDK.pageFileStorage().getImageURI(page.getPageId(), PageFileStorage.PageFileType.DOCUMENT);
            final Bitmap documentImage = loadImage(imageUri);
            return healthInsuranceCardScanner.detectAndRecognizeBitmap(documentImage, 0);
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            progressView.setVisibility(View.GONE);

            final HealthInsuranceCardRecognitionResult healthInsuranceCardRecognitionResult = (HealthInsuranceCardRecognitionResult) o;
            if (healthInsuranceCardRecognitionResult != null && healthInsuranceCardRecognitionResult.status == HealthInsuranceCardDetectionStatus.SUCCESS) {
                startActivity(EhicResultActivity.newIntent(EhicStillImageDetectionActivity.this, healthInsuranceCardRecognitionResult));
            } else {
                Toast.makeText(EhicStillImageDetectionActivity.this,
                        "No EHIC data recognized!", Toast.LENGTH_LONG).show();
            }
        }
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
                page = (Page) result;
                displayPreviewImage();
                cropBtn.setVisibility(View.VISIBLE);
                runRecognitionBtn.setVisibility(View.VISIBLE);
            }
        }
    }
}
