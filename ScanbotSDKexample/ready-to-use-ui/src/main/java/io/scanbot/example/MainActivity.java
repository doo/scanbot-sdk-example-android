package io.scanbot.example;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import net.doo.snap.blob.BlobFactory;
import net.doo.snap.blob.BlobManager;
import net.doo.snap.camera.CameraPreviewMode;
import net.doo.snap.entity.Blob;
import net.doo.snap.lib.detector.DetectionResult;
import net.doo.snap.util.log.Logger;
import net.doo.snap.util.log.LoggerProvider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.scanbot.mrzscanner.model.MRZRecognitionResult;
import io.scanbot.sdk.ScanbotSDK;
import io.scanbot.sdk.barcode.entity.BarcodeScanningResult;
import io.scanbot.sdk.persistence.Page;
import io.scanbot.sdk.persistence.PageFileStorage;
import io.scanbot.sdk.ui.view.barcode.BarcodeScannerActivity;
import io.scanbot.sdk.ui.view.barcode.configuration.BarcodeScannerConfiguration;
import io.scanbot.sdk.ui.view.camera.DocumentScannerActivity;
import io.scanbot.sdk.ui.view.camera.configuration.DocumentScannerConfiguration;
import io.scanbot.sdk.ui.view.edit.CroppingActivity;
import io.scanbot.sdk.ui.view.edit.configuration.CroppingConfiguration;
import io.scanbot.sdk.ui.view.mrz.MRZScannerActivity;
import io.scanbot.sdk.ui.view.mrz.configuration.MRZScannerConfiguration;

public class MainActivity extends AppCompatActivity {

    private final Logger logger = LoggerProvider.getLogger();

    private static final int MRZ_UI_REQUEST_CODE = 909;
    private static final int BARCODE_UI_REQUEST_CODE = 910;
    private static final int CROP_UI_REQUEST_CODE = 9999;
    private static final int SELECT_PICTURE_REQUEST = 8888;
    private static final int CAMERA_UI_REQUEST_CODE = 1111;

    private ScanbotSDK scanbotSDK;
    private BlobManager blobManager;
    private BlobFactory blobFactory;
    private List<Blob> requiredTraineddataBlobs;

    private ProgressBar progressBar;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MRZ_UI_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            MRZRecognitionResult mrzRecognitionResult = data.getParcelableExtra(MRZScannerActivity.EXTRACTED_FIELDS_EXTRA);
            Toast.makeText(MainActivity.this,
                    extractData(mrzRecognitionResult), Toast.LENGTH_LONG).show();
        } else if (requestCode == CROP_UI_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Page page = data.getParcelableExtra
                    (io.scanbot.sdk.ui.view.edit.CroppingActivity.EDITED_PAGE_EXTRA);
        } else if (requestCode == BARCODE_UI_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            BarcodeScanningResult barcodeData = data.getParcelableExtra
                    (BarcodeScannerActivity.SCANNED_BARCODE_EXTRA);
            Toast.makeText(MainActivity.this,
                    barcodeData.getBarcodeFormat() + "\n" + barcodeData.getText(), Toast.LENGTH_LONG).show();
        } else if (requestCode == SELECT_PICTURE_REQUEST) {
            if (resultCode == RESULT_OK) {
                new ProcessImage(data).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
            }
        } else if (requestCode == CAMERA_UI_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Intent intent = new Intent(MainActivity.this, PagePreviewActivity.class);
            startActivity(intent);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initDependencies();
        checkPrepareTrainedDataBlobs();

        progressBar = findViewById(R.id.progressBar);
        Button cropping_ui_btn = findViewById(R.id.cropping_ui_btn);
        cropping_ui_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE_REQUEST);
            }
        });


        Button document_scanner_btn = findViewById(R.id.document_scanner_btn);
        document_scanner_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DocumentScannerConfiguration configuration = new DocumentScannerConfiguration();
                // Customize colors, text resources, etc via configuration:
                configuration.setCameraPreviewMode(CameraPreviewMode.FIT_IN);
                //configuration.setMultiPageEnabled(true);
                //configuration.set...

                Intent intent = DocumentScannerActivity.newIntent(MainActivity.this, configuration);
                startActivityForResult(intent, CAMERA_UI_REQUEST_CODE);
            }
        });
        Button page_preview_activity = findViewById(R.id.page_preview_activity);
        page_preview_activity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PagePreviewActivity.class);
                startActivity(intent);
            }
        });

        Button mrz_scanner_btn = findViewById(R.id.mrz_scanner_btn);
        mrz_scanner_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MRZScannerConfiguration configuration = new MRZScannerConfiguration();
                // Customize colors, text resources, etc via configuration:
                //configuration.setFinderLineColor(Color.parseColor("#FF0000"));
                //configuration.set...

                Intent intent = MRZScannerActivity.newIntent(MainActivity.this, configuration);
                startActivityForResult(intent, MRZ_UI_REQUEST_CODE);
            }
        });

        Button barcode_scanner_btn = findViewById(R.id.barcode_scanner_btn);
        barcode_scanner_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BarcodeScannerConfiguration configuration = new BarcodeScannerConfiguration();
                // Customize colors, text resources, etc via configuration:
                //configuration.setFinderLineColor(Color.parseColor("#FF0000"));
                //configuration.set...

                Intent intent = BarcodeScannerActivity.newIntent(MainActivity.this, configuration);
                startActivityForResult(intent, BARCODE_UI_REQUEST_CODE);
            }
        });

    }

    private Bitmap processGalleryResult(Intent data) {
        ClipData clipData = data.getClipData();
        Uri imageUri = data.getData();
        Bitmap bitmap = null;
        if (imageUri != null) {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            } catch (IOException e) {
            }
        }
        return bitmap;
    }

    private List<String> getImageUris(ClipData clipData) {
        int itemsCount = clipData.getItemCount();
        List<String> imageUris = new ArrayList<>();

        for (int i = 0; i < itemsCount; i++) {
            ClipData.Item item = clipData.getItemAt(i);
            Uri uri = item.getUri();
            if (uri != null) {
                imageUris.add(uri.toString());
            }
        }

        return imageUris;
    }

    private void initDependencies() {
        scanbotSDK = new ScanbotSDK(this);
        blobManager = scanbotSDK.blobManager();
        blobFactory = scanbotSDK.blobFactory();
    }

    private void checkPrepareTrainedDataBlobs() {
        try {
            requiredTraineddataBlobs = new ArrayList<Blob>();
            requiredTraineddataBlobs.add(blobFactory.mrzTraineddataBlob());
            requiredTraineddataBlobs.add(blobFactory.mrzCascadeBlob());
            // add further blobs here (e.g. OCR blobs, etc.)

            new PrepareTraineddataBlobsTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        } catch (final IOException e) {
            logger.logException(e);
            return;
        }
    }

    private String extractData(MRZRecognitionResult result) {
        return new StringBuilder()
                .append("documentCode: ").append(result.documentCodeField().value).append("\n")
                .append("First name: ").append(result.firstNameField().value).append("\n")
                .append("Last name: ").append(result.lastNameField().value).append("\n")
                .append("issuingStateOrOrganization: ").append(result.issuingStateOrOrganizationField().value).append("\n")
                .append("departmentOfIssuance: ").append(result.departmentOfIssuanceField().value).append("\n")
                .append("nationality: ").append(result.nationalityField().value).append("\n")
                .append("dateOfBirth: ").append(result.dateOfBirthField().value).append("\n")
                .append("gender: ").append(result.genderField().value).append("\n")
                .append("dateOfExpiry: ").append(result.dateOfExpiryField().value).append("\n")
                .append("personalNumber: ").append(result.personalNumberField().value).append("\n")
                .append("optional1: ").append(result.optional1Field().value).append("\n")
                .append("optional2: ").append(result.optional2Field().value).append("\n")
                .append("discreetIssuingStateOrOrganization: ").append(result.discreetIssuingStateOrOrganizationField().value).append("\n")
                .append("validCheckDigitsCount: ").append(result.validCheckDigitsCount).append("\n")
                .append("checkDigitsCount: ").append(result.checkDigitsCount).append("\n")
                .append("travelDocType: ").append(result.travelDocTypeField().value).append("\n")
                .toString();
    }

    /**
     * Processes image and creates optimized preview
     */
    class ProcessImage extends AsyncTask<Void, Void, Page> {
        private Intent data;

        public ProcessImage(Intent data) {
            this.data = data;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Page doInBackground(Void... voids) {
            Bitmap picture = processGalleryResult(data);

            PageFileStorage pageFileStorage = new ScanbotSDK(MainActivity.this).getPageFileStorage();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            picture.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            picture.recycle();

            String pageId = pageFileStorage.add(byteArray);
            return new Page(pageId, new ArrayList<>(), DetectionResult.OK);
        }


        @Override
        protected void onPostExecute(Page page) {
            progressBar.setVisibility(View.GONE);
            CroppingConfiguration configuration = new CroppingConfiguration();
            // Customize colors, text resources, etc via configuration:
            //configuration.set...

            configuration.setPage(page);

            Intent intent = CroppingActivity.newIntent(getApplicationContext(), configuration);
            startActivityForResult(intent, CROP_UI_REQUEST_CODE);
        }
    }


    private class PrepareTraineddataBlobsTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                for (final Blob blob: requiredTraineddataBlobs) {
                    if (!blobManager.isBlobAvailable(blob)) {
                        blobManager.fetch(blob, false);
                    }
                }
            } catch (final IOException e) {
                logger.logException(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

}
