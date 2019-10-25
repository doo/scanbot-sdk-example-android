package io.scanbot.example;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import io.scanbot.mrzscanner.model.MRZCheckDigit;
import io.scanbot.mrzscanner.model.MRZRecognitionResult;

public class MRZResultActivity extends AppCompatActivity {

    private static final String EXTRA_MRZ_RESULT = "MRZ_RESULT";

    private TextView travelDocType;
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
    private TextView checkDigits;

    public static Intent newIntent(final Context context, final MRZRecognitionResult result) {
        final Intent intent = new Intent(context, MRZResultActivity.class);
        intent.putExtra(EXTRA_MRZ_RESULT, result);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mrz_result);

        travelDocType = findViewById(R.id.travelDocType);
        documentCode = findViewById(R.id.document_code);
        firstName = findViewById(R.id.first_name);
        lastName = findViewById(R.id.last_name);
        issuingStateOrOrganization = findViewById(R.id.issuingStateOrOrganization);
        departmentOfIssuance = findViewById(R.id.departmentOfIssuance);
        nationality = findViewById(R.id.nationality);
        dateOfBirth = findViewById(R.id.dateOfBirth);
        gender = findViewById(R.id.gender);
        dateOfExpiry = findViewById(R.id.dateOfExpiry);
        personalNumber = findViewById(R.id.personalNumber);
        optional1 = findViewById(R.id.optional1);
        optional2 = findViewById(R.id.optional2);
        discreetIssuingStateOrOrganization = findViewById(R.id.discreetIssuingStateOrOrganization);
        validCheckDigitsCount = findViewById(R.id.validCheckDigitsCount);
        checkDigitsCount = findViewById(R.id.checkDigitsCount);
        checkDigits = findViewById(R.id.checkDigits);

        final MRZRecognitionResult result = getIntent().getParcelableExtra(EXTRA_MRZ_RESULT);

        travelDocType.setText(result.travelDocType.name());
        documentCode.setText(result.documentCodeField().value);
        firstName.setText(result.firstNameField().value);
        lastName.setText(result.lastNameField().value);
        issuingStateOrOrganization.setText(result.issuingStateOrOrganizationField().value);
        departmentOfIssuance.setText(result.departmentOfIssuanceField().value);
        nationality.setText(result.nationalityField().value);
        dateOfBirth.setText(result.dateOfBirthField().value);
        gender.setText(result.genderField().value);
        dateOfExpiry.setText(result.dateOfExpiryField().value);
        personalNumber.setText(result.personalNumberField().value);
        optional1.setText(result.optional1Field().value);
        optional2.setText(result.optional2Field().value);
        discreetIssuingStateOrOrganization.setText(result.discreetIssuingStateOrOrganizationField().value);
        validCheckDigitsCount.setText("" + result.validCheckDigitsCount);
        checkDigitsCount.setText("" + result.checkDigitsCount);

        String checkDigitsOutput = "";
        for (final MRZCheckDigit cd: result.checkDigits) {
            checkDigitsOutput += cd.type.name() + ": " + (char)cd.checkDigitCharacter + " (" + cd.successfullyValidated + ")";
            checkDigitsOutput += "\n";
        }
        checkDigits.setText(checkDigitsOutput);

        findViewById(R.id.retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
