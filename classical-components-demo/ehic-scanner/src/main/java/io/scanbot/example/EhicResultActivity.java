package io.scanbot.example;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import io.scanbot.hicscanner.model.HealthInsuranceCardField;
import io.scanbot.hicscanner.model.HealthInsuranceCardRecognitionResult;

public class EhicResultActivity extends AppCompatActivity {

    private static final String EXTRA_EHIC_RESULT = "EHIC_RESULT";

    public static Intent newIntent(final Context context, final HealthInsuranceCardRecognitionResult result) {
        final Intent intent = new Intent(context, EhicResultActivity.class);
        intent.putExtra(EXTRA_EHIC_RESULT, result);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ehic_result);

        TextView surname = findViewById(R.id.surname);
        TextView givenName = findViewById(R.id.given_name);
        TextView dateOfBirth = findViewById(R.id.date_of_birth);
        TextView personalNumber = findViewById(R.id.personal_number);
        TextView institutionNumber = findViewById(R.id.institution_number);
        TextView institutionName = findViewById(R.id.institution_name);
        TextView cardNumber = findViewById(R.id.card_number);
        TextView cardDate = findViewById(R.id.card_date);

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

        findViewById(R.id.retry).setOnClickListener(v -> finish());
    }
}
