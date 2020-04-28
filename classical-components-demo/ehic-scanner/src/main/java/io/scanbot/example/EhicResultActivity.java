package io.scanbot.example;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import io.scanbot.hicscanner.model.HealthInsuranceCardField;
import io.scanbot.hicscanner.model.HealthInsuranceCardRecognitionResult;

public class EhicResultActivity extends AppCompatActivity {

    private static final String EXTRA_EHIC_RESULT = "EHIC_RESULT";

    private TextView surname;
    private TextView givenName;
    private TextView personalNumber;
    private TextView dateOfBirth;
    private TextView institutionNumber;
    private TextView institutionName;
    private TextView cardNumber;
    private TextView cardDate;

    public static Intent newIntent(final Context context, final HealthInsuranceCardRecognitionResult result) {
        final Intent intent = new Intent(context, EhicResultActivity.class);
        intent.putExtra(EXTRA_EHIC_RESULT, result);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ehic_result);

        surname = findViewById(R.id.surname);
        givenName = findViewById(R.id.given_name);
        dateOfBirth = findViewById(R.id.date_of_birth);
        personalNumber = findViewById(R.id.personal_number);
        institutionNumber = findViewById(R.id.institution_number);
        institutionName = findViewById(R.id.institution_name);
        cardNumber = findViewById(R.id.card_number);
        cardDate = findViewById(R.id.card_date);

        final HealthInsuranceCardRecognitionResult result = getIntent().getParcelableExtra(EXTRA_EHIC_RESULT);

        for (HealthInsuranceCardField field : result.fields) {
            switch (field.type) {
                case SURNAME:
                    surname.setText(field.value);
                    break;
                case GIVEN_NAME:
                    givenName.setText(field.value);
                    break;
                case DATE_OF_BIRTH:
                    dateOfBirth.setText(field.value);
                    break;
                case PERSONAL_IDENTIFICATION_NUMBER:
                    personalNumber.setText(field.value);
                    break;
                case INSTITUTION_NUMBER:
                    institutionNumber.setText(field.value);
                    break;
                case INSTITUTION_NAME:
                    institutionName.setText(field.value);
                    break;
                case CARD_NUMBER:
                    cardNumber.setText(field.value);
                    break;
                case CARD_EXPIRATION_DATE:
                    cardDate.setText(field.value);
                    break;
            }
        }

        findViewById(R.id.retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
