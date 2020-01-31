package io.scanbot.example;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.view.WindowCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Toast;

import net.doo.snap.camera.CameraOpenCallback;
import net.doo.snap.camera.ScanbotCameraView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import io.scanbot.barcodescanner.model.DEMedicalPlan.DEMedicalPlanDoctorField;
import io.scanbot.barcodescanner.model.DEMedicalPlan.DEMedicalPlanDocument;
import io.scanbot.barcodescanner.model.DEMedicalPlan.DEMedicalPlanMedicine;
import io.scanbot.barcodescanner.model.DEMedicalPlan.DEMedicalPlanMedicineField;
import io.scanbot.barcodescanner.model.DEMedicalPlan.DEMedicalPlanPatientField;
import io.scanbot.barcodescanner.model.DEMedicalPlan.DEMedicalPlanStandardSubheading;
import io.scanbot.barcodescanner.model.boardingPass.BoardingPassDocument;
import io.scanbot.barcodescanner.model.boardingPass.BoardingPassLeg;
import io.scanbot.barcodescanner.model.boardingPass.BoardingPassLegField;
import io.scanbot.sdk.ScanbotSDK;
import io.scanbot.sdk.SdkLicenseError;
import io.scanbot.sdk.barcode.BarcodeDetectorFrameHandler;
import io.scanbot.sdk.barcode.entity.BarcodeFormat;
import io.scanbot.sdk.barcode.entity.BarcodeItem;
import io.scanbot.sdk.barcode.entity.BarcodeScanningResult;
import io.scanbot.sdk.camera.FrameHandlerResult;

public class BarcodeScannerActivity extends AppCompatActivity implements BarcodeDetectorFrameHandler.ResultHandler {

    private static final int BARCODE_TYPES_REQUEST = 1001;

    private ScanbotCameraView cameraView;

    boolean flashEnabled = false;
    private Toast toast;
    private BarcodeDetectorFrameHandler barcodeDetectorFrameHandler;
    private ArrayList<Integer> selectedBarcodeFormatsIndexes = new ArrayList<>();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BARCODE_TYPES_REQUEST && resultCode == RESULT_OK) {
            selectedBarcodeFormatsIndexes = data.getIntegerArrayListExtra(BarcodeTypesActivity.SELECTED_BARCODE_TYPES);
            ArrayList<BarcodeFormat> selectedBarcodeFormats = new ArrayList<>();
            for (Integer typeIndex : selectedBarcodeFormatsIndexes) {
                selectedBarcodeFormats.add(BarcodeFormat.values()[typeIndex]);
            }

            barcodeDetectorFrameHandler.setBarcodeFormatsFilter(selectedBarcodeFormats);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scanner);

        getSupportActionBar().hide();

        cameraView = (ScanbotCameraView) findViewById(R.id.camera);

        cameraView.setCameraOpenCallback(new CameraOpenCallback() {
            @Override
            public void onCameraOpened() {
                cameraView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        cameraView.useFlash(flashEnabled);
                        cameraView.continuousFocus();
                    }
                }, 300);
            }
        });

        findViewById(R.id.barcode_types).setOnClickListener(v -> {
            Intent intent = new Intent(BarcodeScannerActivity.this, BarcodeTypesActivity.class);
            intent.putIntegerArrayListExtra(BarcodeTypesActivity.SELECTED_BARCODE_TYPES, selectedBarcodeFormatsIndexes);
            startActivityForResult(intent, BARCODE_TYPES_REQUEST);
        });

        barcodeDetectorFrameHandler = BarcodeDetectorFrameHandler.attach(cameraView, new ScanbotSDK(this).barcodeDetector());
        barcodeDetectorFrameHandler.setDetectionInterval(1000);
        barcodeDetectorFrameHandler.addResultHandler(this);

        findViewById(R.id.flash).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flashEnabled = !flashEnabled;
                cameraView.useFlash(flashEnabled);
            }
        });

        final ScanbotSDK scanbotSDK = new ScanbotSDK(this);
        Toast.makeText(
                this,
                scanbotSDK.isLicenseValid()
                        ? "License is valid"
                        : "License has expired!",
                Toast.LENGTH_LONG
        ).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.onPause();
    }

    @Override
    public boolean handle(@NotNull FrameHandlerResult<? extends BarcodeScanningResult, ? extends SdkLicenseError> result) {
        if (result instanceof FrameHandlerResult.Success) {
            BarcodeScanningResult recognitionResult = (BarcodeScanningResult) ((FrameHandlerResult.Success) result).getValue();
            if (recognitionResult != null && recognitionResult.getBarcodeItems().size() > 0) {
                showBarcodeResults(recognitionResult);
            }
        }
        return false;
    }

    private void showBarcodeResults(final BarcodeScanningResult result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (toast != null) {
                    toast.cancel();
                }
                final StringBuilder barcodesResult = new StringBuilder();
                for (final BarcodeItem item : result.getBarcodeItems()) {
                    barcodesResult.append(item.getBarcodeFormat() + "\n" + item.getText())
                            .append("\n")
                            .append(printParsedFormat(item))
                            .append("\n")
                            .append("-------------------")
                            .append("\n");
                }
                toast = Toast.makeText(
                        BarcodeScannerActivity.this,
                        barcodesResult.toString(),
                        Toast.LENGTH_LONG
                );
                toast.show();
            }
        });
    }

    private String printParsedFormat(final BarcodeItem item) {
        if (item.getBarcodeDocumentFormat() == null) {
            // not supported by current barcode detector implementation
            return "";
        }

        final StringBuilder barcodesResult = new StringBuilder();
        if (item.getBarcodeDocumentFormat() instanceof BoardingPassDocument) {
            final BoardingPassDocument barcodDocumentFormat = (BoardingPassDocument) item.getBarcodeDocumentFormat();
            barcodesResult.append("\n")
                    .append("Boarding Pass Document").append("\n")
                    .append(barcodDocumentFormat.name).append("\n");
            for (final BoardingPassLeg leg : barcodDocumentFormat.legs) {
                for (final BoardingPassLegField field : leg.fields) {
                    barcodesResult.append(field.type.name()).append(": ").append(field.value).append("\n");
                }
            }
        } else if (item.getBarcodeDocumentFormat() instanceof DEMedicalPlanDocument) {
            final DEMedicalPlanDocument medicalPlanDocFormat = (DEMedicalPlanDocument) item.getBarcodeDocumentFormat();
            barcodesResult.append("\n").append("DE Medical Plan Document").append("\n");

            barcodesResult.append("Doctor Fields:").append("\n");
            for (final DEMedicalPlanDoctorField field: medicalPlanDocFormat.doctor.fields) {
                barcodesResult.append(field.type.name()).append(": ").append(field.value).append("\n");
            }

            barcodesResult.append("Patient Fields:").append("\n");
            for (final DEMedicalPlanPatientField field: medicalPlanDocFormat.patient.fields) {
                barcodesResult.append(field.type.name()).append(": ").append(field.value).append("\n");
            }

            barcodesResult.append("Medicine Fields:").append("\n");
            for (final DEMedicalPlanStandardSubheading sh: medicalPlanDocFormat.subheadings) {
                for (final DEMedicalPlanMedicine m: sh.medicines) {
                    for (final DEMedicalPlanMedicineField mf: m.fields) {
                        barcodesResult.append(mf.type.name()).append(": ").append(mf.value).append("\n");
                    }
                }
            }

            // medicalPlanDocFormat...
        }

        return barcodesResult.toString();
    }

}
