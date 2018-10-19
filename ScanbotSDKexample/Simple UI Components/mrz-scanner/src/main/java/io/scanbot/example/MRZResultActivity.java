package io.scanbot.example;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import io.scanbot.mrzscanner.model.MRZRecognitionResult;

public class MRZResultActivity extends AppCompatActivity {
    public static final String EXTRA_documentCode = "documentCode";
    public static final String EXTRA_firstName = "firstName";
    public static final String EXTRA_lastName = "lastName";
    public static final String EXTRA_issuingStateOrOrganization = "issuingStateOrOrganization";
    public static final String EXTRA_departmentOfIssuance = "departmentOfIssuance";
    public static final String EXTRA_nationality = "nationality";
    public static final String EXTRA_dateOfBirth = "dateOfBirth";
    public static final String EXTRA_gender = "gender";
    public static final String EXTRA_dateOfExpiry = "dateOfExpiry";
    public static final String EXTRA_personalNumber = "personalNumber";
    public static final String EXTRA_optional1 = "optional1";
    public static final String EXTRA_optional2 = "optional2";
    public static final String EXTRA_discreetIssuingStateOrOrganization = "discreetIssuingStateOrOrganization";
    public static final String EXTRA_validCheckDigitsCount = "validCheckDigitsCount";
    public static final String EXTRA_checkDigitsCount = "checkDigitsCount";
    public static final String EXTRA_travelDocTyp = "travelDocType";

    private TextView documentCode;
    private TextView firstName;
    private TextView lastName;
    private TextView issuingStateOrOrganization;
    private TextView departmentOfIssuance;
    private TextView nationality;
    private TextView dateOfBirth;
    private TextView gender;
    private TextView dateOfExpiry;
    private TextView personalNumber;
    private TextView optional1;
    private TextView optional2;
    private TextView discreetIssuingStateOrOrganization;
    private TextView validCheckDigitsCount;
    private TextView checkDigitsCount;
    private TextView travelDocType;

    public static Intent newIntent(Context context, MRZRecognitionResult result) {
        Intent intent = new Intent(context, MRZResultActivity.class);
        intent.putExtra(EXTRA_documentCode, result.documentCode);
        intent.putExtra(EXTRA_firstName, result.firstName);
        intent.putExtra(EXTRA_lastName, result.lastName);
        intent.putExtra(EXTRA_issuingStateOrOrganization, result.issuingStateOrOrganization);
        intent.putExtra(EXTRA_departmentOfIssuance, result.departmentOfIssuance);
        intent.putExtra(EXTRA_nationality, result.nationality);
        intent.putExtra(EXTRA_dateOfBirth, result.dateOfBirth);
        intent.putExtra(EXTRA_gender, result.gender);
        intent.putExtra(EXTRA_dateOfExpiry, result.dateOfExpiry);
        intent.putExtra(EXTRA_personalNumber, result.personalNumber);
        intent.putExtra(EXTRA_optional1, result.optional1);
        intent.putExtra(EXTRA_optional2, result.optional2);
        intent.putExtra(EXTRA_discreetIssuingStateOrOrganization, result.discreetIssuingStateOrOrganization);
        intent.putExtra(EXTRA_validCheckDigitsCount, result.validCheckDigitsCount);
        intent.putExtra(EXTRA_checkDigitsCount, result.checkDigitsCount);
        intent.putExtra(EXTRA_travelDocTyp, result.travelDocType.name());

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mrz_result);
        documentCode = (TextView) findViewById(R.id.document_code);
        firstName = (TextView) findViewById(R.id.first_name);
        lastName = (TextView) findViewById(R.id.last_name);
        issuingStateOrOrganization = (TextView) findViewById(R.id.issuingStateOrOrganization);
        departmentOfIssuance = (TextView) findViewById(R.id.departmentOfIssuance);
        nationality = (TextView) findViewById(R.id.nationality);
        dateOfBirth = (TextView) findViewById(R.id.dateOfBirth);
        gender = (TextView) findViewById(R.id.gender);
        dateOfExpiry = (TextView) findViewById(R.id.dateOfExpiry);
        personalNumber = (TextView) findViewById(R.id.personalNumber);
        optional1 = (TextView) findViewById(R.id.optional1);
        optional2 = (TextView) findViewById(R.id.optional2);
        discreetIssuingStateOrOrganization = (TextView) findViewById(R.id.discreetIssuingStateOrOrganization);
        validCheckDigitsCount = (TextView) findViewById(R.id.validCheckDigitsCount);
        checkDigitsCount = (TextView) findViewById(R.id.checkDigitsCount);
        travelDocType = (TextView) findViewById(R.id.travelDocType);

        documentCode.setText(getIntent().getStringExtra(EXTRA_documentCode));
        firstName.setText(getIntent().getStringExtra(EXTRA_firstName));
        lastName.setText(getIntent().getStringExtra(EXTRA_lastName));
        issuingStateOrOrganization.setText(getIntent().getStringExtra(EXTRA_issuingStateOrOrganization));
        departmentOfIssuance.setText(getIntent().getStringExtra(EXTRA_departmentOfIssuance));
        nationality.setText(getIntent().getStringExtra(EXTRA_nationality));
        dateOfBirth.setText(getIntent().getStringExtra(EXTRA_dateOfBirth));
        gender.setText(getIntent().getStringExtra(EXTRA_gender));
        dateOfExpiry.setText(getIntent().getStringExtra(EXTRA_dateOfExpiry));
        personalNumber.setText(getIntent().getStringExtra(EXTRA_personalNumber));
        optional1.setText(getIntent().getStringExtra(EXTRA_optional1));
        optional2.setText(getIntent().getStringExtra(EXTRA_optional2));
        discreetIssuingStateOrOrganization.setText(getIntent().getStringExtra(EXTRA_discreetIssuingStateOrOrganization));
        validCheckDigitsCount.setText("Value: " + getIntent().getIntExtra(EXTRA_validCheckDigitsCount, -1));
        checkDigitsCount.setText("Value: " + getIntent().getIntExtra(EXTRA_checkDigitsCount, -1));
        travelDocType.setText(getIntent().getStringExtra(EXTRA_travelDocTyp));

        findViewById(R.id.retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}
