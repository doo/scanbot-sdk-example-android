package io.scanbot.example;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import io.scanbot.dcscanner.model.DateRecord;
import io.scanbot.dcscanner.model.DisabilityCertificateInfoBox;
import io.scanbot.dcscanner.model.DisabilityCertificateRecognizerResultInfo;

public class DCResultActivity extends AppCompatActivity {
    public static final String EXTRA_workAccident = "workAccident";
    public static final String EXTRA_workAccidentConf = "workAccidentConf";
    public static final String EXTRA_assignedInsDoctor = "assignedInsDoctor";
    public static final String EXTRA_assignedInsDoctorConf = "assignedInsDoctorConf";
    public static final String EXTRA_initialCertificate = "initialCertificate";
    public static final String EXTRA_initialCertificateConf = "initialCertificateConf";
    public static final String EXTRA_renewedCertificate = "renewedCertificate";
    public static final String EXTRA_renewedCertificateConf = "renewedCertificateConf";
    public static final String EXTRA_incapableSince = "incapableSince";
    public static final String EXTRA_incapableSinceRC = "incapableSinceRC";
    public static final String EXTRA_incapableSinceVC = "incapableSinceVC";
    public static final String EXTRA_incapableUntil = "incapableUntil";
    public static final String EXTRA_incapableUntilRC = "incapableUntilRC";
    public static final String EXTRA_incapableUntilVC = "incapableUntilVC";
    public static final String EXTRA_diagnosedOn = "diagnosedOn";
    public static final String EXTRA_diagnosedOnRC = "diagnosedOnRC";
    public static final String EXTRA_diagnosedOnVC = "diagnosedOnVC";

    private TextView workAccident;
    private TextView workAccidentConf;
    private TextView assignedInsDoctor;
    private TextView assignedInsDoctorConf;
    private TextView initialCertificate;
    private TextView initialCertificateConf;
    private TextView renewedCertificate;
    private TextView renewedCertificateConf;
    private TextView incapableSince;
    private TextView incapableSinceRC;
    private TextView incapableSinceVC;
    private TextView incapableUntil;
    private TextView incapableUntilRC;
    private TextView incapableUntilVC;
    private TextView diagnosedOn;
    private TextView diagnosedOnRC;
    private TextView diagnosedOnVC;

    public static Intent newIntent(Context context, DisabilityCertificateRecognizerResultInfo result) {
        Intent intent = new Intent(context, DCResultActivity.class);

        for (DisabilityCertificateInfoBox chackbox : result.checkboxes) {
            switch (chackbox.subType) {
                case DCBoxUnknown:
                case DCBoxPatientInfo:
                    break;
                case DCBoxWorkAccident:
                    intent.putExtra(EXTRA_workAccident, chackbox.hasContents);
                    intent.putExtra(EXTRA_workAccidentConf, chackbox.contentsValidationConfidenceValue);
                    break;
                case DCBoxAssignedToAccidentInsuranceDoctor:
                    intent.putExtra(EXTRA_assignedInsDoctor, chackbox.hasContents);
                    intent.putExtra(EXTRA_assignedInsDoctorConf, chackbox.contentsValidationConfidenceValue);
                    break;
                case DCBoxInitialCertificate:
                    intent.putExtra(EXTRA_initialCertificate, chackbox.hasContents);
                    intent.putExtra(EXTRA_initialCertificateConf, chackbox.contentsValidationConfidenceValue);
                    break;
                case DCBoxRenewedCertificate:
                    intent.putExtra(EXTRA_renewedCertificate, chackbox.hasContents);
                    intent.putExtra(EXTRA_renewedCertificateConf, chackbox.contentsValidationConfidenceValue);
                    break;
            }
        }

        for (DateRecord date : result.dates) {
            switch (date.type) {
                case DateRecordIncapableOfWorkSince:
                    intent.putExtra(EXTRA_incapableSince, date.dateString);
                    intent.putExtra(EXTRA_incapableSinceRC, date.recognitionConfidenceValue);
                    intent.putExtra(EXTRA_incapableSinceVC, date.validationConfidenceValue);
                    break;
                case DateRecordIncapableOfWorkUntil:
                    intent.putExtra(EXTRA_incapableUntil, date.dateString);
                    intent.putExtra(EXTRA_incapableUntilRC, date.recognitionConfidenceValue);
                    intent.putExtra(EXTRA_incapableUntilVC, date.validationConfidenceValue);
                    break;
                case DateRecordDiagnosedOn:
                    intent.putExtra(EXTRA_diagnosedOn, date.dateString);
                    intent.putExtra(EXTRA_diagnosedOnRC, date.recognitionConfidenceValue);
                    intent.putExtra(EXTRA_diagnosedOnVC, date.validationConfidenceValue);
                    break;
                case DateRecordUndefined:
                    break;
            }
        }

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dc_result);
        workAccident = (TextView) findViewById(R.id.work_accident);
        workAccidentConf = (TextView) findViewById(R.id.work_accident_confidence);
        assignedInsDoctor = (TextView) findViewById(R.id.assigned_ins_doctor);
        assignedInsDoctorConf = (TextView) findViewById(R.id.assigned_ins_doctor_confidence);
        initialCertificate = (TextView) findViewById(R.id.initial_certificate);
        initialCertificateConf = (TextView) findViewById(R.id.initial_certificate_confidence);
        renewedCertificate = (TextView) findViewById(R.id.renewed_certificate);
        renewedCertificateConf = (TextView) findViewById(R.id.renewed_certificate_confidence);
        incapableSince = (TextView) findViewById(R.id.incapable_since);
        incapableSinceRC = (TextView) findViewById(R.id.incapable_since_rc);
        incapableSinceVC = (TextView) findViewById(R.id.incapable_since_vc);
        incapableUntil = (TextView) findViewById(R.id.incapable_until);
        incapableUntilRC = (TextView) findViewById(R.id.incapable_until_rc);
        incapableUntilVC = (TextView) findViewById(R.id.incapable_until_vc);
        diagnosedOn = (TextView) findViewById(R.id.diagnosed_on);
        diagnosedOnRC = (TextView) findViewById(R.id.diagnosed_on_rc);
        diagnosedOnVC = (TextView) findViewById(R.id.diagnosed_on_vc);

        workAccident.setText(getIntent().getBooleanExtra(EXTRA_workAccident, false) ? "Checked" : "Unchecked");
        workAccidentConf.setText(Double.toString(getIntent().getDoubleExtra(EXTRA_workAccidentConf, 0)));
        assignedInsDoctor.setText(getIntent().getBooleanExtra(EXTRA_assignedInsDoctor, false) ? "Checked" : "Unchecked");
        assignedInsDoctorConf.setText(Double.toString(getIntent().getDoubleExtra(EXTRA_assignedInsDoctorConf, 0)));
        initialCertificate.setText(getIntent().getBooleanExtra(EXTRA_initialCertificate, false) ? "Checked" : "Unchecked");
        initialCertificateConf.setText(Double.toString(getIntent().getDoubleExtra(EXTRA_initialCertificateConf, 0)));
        renewedCertificate.setText(getIntent().getBooleanExtra(EXTRA_renewedCertificate, false) ? "Checked" : "Unchecked");
        renewedCertificateConf.setText(Double.toString(getIntent().getDoubleExtra(EXTRA_renewedCertificateConf, 0)));
        incapableSince.setText(getIntent().getStringExtra(EXTRA_incapableSince));
        incapableSinceRC.setText(Double.toString(getIntent().getDoubleExtra(EXTRA_incapableSinceRC, 0)));
        incapableSinceVC.setText(Double.toString(getIntent().getDoubleExtra(EXTRA_incapableSinceVC, 0)));
        incapableUntil.setText(getIntent().getStringExtra(EXTRA_incapableUntil));
        incapableUntilRC.setText(Double.toString(getIntent().getDoubleExtra(EXTRA_incapableUntilRC, 0)));
        incapableUntilVC.setText(Double.toString(getIntent().getDoubleExtra(EXTRA_incapableUntilVC, 0)));
        diagnosedOn.setText(getIntent().getStringExtra(EXTRA_diagnosedOn));
        diagnosedOnRC.setText(Double.toString(getIntent().getDoubleExtra(EXTRA_diagnosedOnRC, 0)));
        diagnosedOnVC.setText(Double.toString(getIntent().getDoubleExtra(EXTRA_diagnosedOnVC, 0)));

        findViewById(R.id.retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}
