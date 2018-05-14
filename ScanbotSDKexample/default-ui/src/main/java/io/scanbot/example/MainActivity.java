package io.scanbot.example;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import net.doo.snap.lib.detector.DetectionResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.scanbot.mrzscanner.model.MRZRecognitionResult;
import io.scanbot.sdk.ScanbotSDK;
import io.scanbot.sdk.barcode.entity.BarcodeScanningResult;
import io.scanbot.sdk.persistance.Page;
import io.scanbot.sdk.persistance.PageFileStorage;
import io.scanbot.sdk.persistance.PolygonHelper;
import io.scanbot.sdk.ui.view.barcode.BarcodeCameraActivity;
import io.scanbot.sdk.ui.view.barcode.configuration.BarcodeCameraConfiguration;
import io.scanbot.sdk.ui.view.base.configuration.CameraOrientationMode;
import io.scanbot.sdk.ui.view.edit.configuration.EditPolygonConfiguration;
import io.scanbot.sdk.ui.view.mrz.MRZCameraActivity;
import io.scanbot.sdk.ui.view.mrz.configuration.MRZCameraConfiguration;

public class MainActivity extends AppCompatActivity {

    private static final int MRZ_DEFAULT_UI_REQUEST_CODE = 909;
    private static final int BARCODE_DEFAULT_UI_REQUEST_CODE = 910;
    private static final int CROP_DEFAULT_UI_REQUEST_CODE = 9999;
    private static final int SELECT_PICTURE_REQUEST = 8888;

    private ScanbotSDK scanbotSDK;
    private BlobManager blobManager;
    private BlobFactory blobFactory;
    ProgressBar progressBar;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MRZ_DEFAULT_UI_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            MRZRecognitionResult mrzRecognitionResult = data.getParcelableExtra(MRZCameraActivity.EXTRACTED_FIELDS_EXTRA);
            Toast.makeText(MainActivity.this,
                    extractData(mrzRecognitionResult), Toast.LENGTH_LONG).show();
        } else if (requestCode == CROP_DEFAULT_UI_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Page page = data.getParcelableExtra
                    (io.scanbot.sdk.ui.view.edit.EditPolygonActivity.EDITED_PAGE_EXTRA);
        } else if (requestCode == BARCODE_DEFAULT_UI_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            BarcodeScanningResult barcodeData = data.getParcelableExtra
                    (BarcodeCameraActivity.SCANNED_BARCODE_EXTRA);
            Toast.makeText(MainActivity.this,
                    barcodeData.toString(), Toast.LENGTH_LONG).show();
        } else if (requestCode == SELECT_PICTURE_REQUEST) {
            if (resultCode == RESULT_OK) {
                new ProcessImage(data).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initDependencies();

        progressBar = findViewById(R.id.progressBar);
        Button crop_ui = findViewById(R.id.crop_default_ui);
        crop_ui.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE_REQUEST);
            }
        });


        Button camera_default_ui = findViewById(R.id.camera_default_ui);
        camera_default_ui.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PagePreviewActivity.class);
                startActivity(intent);
            }
        });

        Button mrz_camera_default_ui = findViewById(R.id.mrz_camera_default_ui);
        mrz_camera_default_ui.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MRZCameraConfiguration mrzCameraConfiguration = new MRZCameraConfiguration();
                mrzCameraConfiguration.setTopBarBackgroundColor(Color.parseColor("#00FFFF"));
                mrzCameraConfiguration.setTopBarButtonsColor(Color.parseColor("#FF0000"));

                mrzCameraConfiguration.setTextCancel("закончить");
                mrzCameraConfiguration.setTextUserGuidance("Помести код сюда");

                mrzCameraConfiguration.setCameraOverlayColor(Color.parseColor("#80F0F000"));
                mrzCameraConfiguration.setFinderLineColor(Color.parseColor("#00F0F0"));
                mrzCameraConfiguration.setFinderHeight(300);
                mrzCameraConfiguration.setFinderWidth(800);
                mrzCameraConfiguration.setFinderLineWidth(10);
                mrzCameraConfiguration.setTextPermissionDescription("Дай пермишн, ну пазязя!");
                mrzCameraConfiguration.setTextPermissionButton("На пермишн");

                mrzCameraConfiguration.setFlashEnabled(false);

                mrzCameraConfiguration.setOrientationMode(CameraOrientationMode.PORTRAIT);

                Intent intent = MRZCameraActivity.newIntent(MainActivity.this, mrzCameraConfiguration);
                startActivityForResult(intent, MRZ_DEFAULT_UI_REQUEST_CODE);
            }
        });

        Button barcode_camera_default_ui = findViewById(R.id.barcode_camera_default_ui);
        barcode_camera_default_ui.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BarcodeCameraConfiguration barcodeCameraConfiguration = new BarcodeCameraConfiguration();
                barcodeCameraConfiguration.setTopBarBackgroundColor(Color.parseColor("#00FFFF"));
                barcodeCameraConfiguration.setTopBarButtonsColor(Color.parseColor("#FF0000"));

                barcodeCameraConfiguration.setTextCancel("закончить");
                barcodeCameraConfiguration.setTextUserGuidance("Помести код сюда");

                barcodeCameraConfiguration.setCameraOverlayColor(Color.parseColor("#80F0F000"));
                barcodeCameraConfiguration.setFinderLineColor(Color.parseColor("#00F0F0"));
                barcodeCameraConfiguration.setFinderHeight(800);
                barcodeCameraConfiguration.setFinderWidth(800);
                barcodeCameraConfiguration.setFinderLineWidth(10);
                barcodeCameraConfiguration.setTextPermissionDescription("Дай пермишн, ну пазязя!");
                barcodeCameraConfiguration.setTextPermissionButton("На пермишн");

                barcodeCameraConfiguration.setFlashEnabled(false);

                barcodeCameraConfiguration.setOrientationMode(CameraOrientationMode.PORTRAIT);

                Intent intent = BarcodeCameraActivity.newIntent(MainActivity.this, barcodeCameraConfiguration);
                startActivityForResult(intent, BARCODE_DEFAULT_UI_REQUEST_CODE);
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
            return new Page(pageId, PolygonHelper.Companion.getFulPolygon(), DetectionResult.OK);
        }


        @Override
        protected void onPostExecute(Page page) {
            progressBar.setVisibility(View.GONE);
            EditPolygonConfiguration editPolygonConfiguration = new EditPolygonConfiguration();

            editPolygonConfiguration.setPage(
                    page
            );
            Intent intent = io.scanbot.sdk.ui.view.edit.EditPolygonActivity.newIntent(
                    getApplicationContext(),
                    editPolygonConfiguration
            );
            startActivityForResult(intent, CROP_DEFAULT_UI_REQUEST_CODE);
        }
    }
}
