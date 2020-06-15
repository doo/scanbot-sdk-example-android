package io.scanbot.example;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

import io.scanbot.sdk.core.payformscanner.model.RecognizedField;

public class PayformResultActivity extends AppCompatActivity {
    public static final String EXTRA_SENDER = "sender";
    public static final String EXTRA_IBAN = "iban";
    public static final String EXTRA_BIC = "bic";
    public static final String EXTRA_AMOUNT = "amount";
    public static final String EXTRA_REFERENCE_NUMBER = "reference_number";
    public static final String EXTRA_REFERENCE_NUMBER_2 = "reference_number_2";
    public static final String EXTRA_SENDER_IBAN = "sender_iban";

    public static Intent newIntent(Context context, String sender, String iban, String bic, String amount, String referenceNumber) {
        Intent intent = new Intent(context, PayformResultActivity.class);
        intent.putExtra(EXTRA_SENDER, sender);
        intent.putExtra(EXTRA_IBAN, iban);
        intent.putExtra(EXTRA_BIC, bic);
        intent.putExtra(EXTRA_AMOUNT, amount);
        intent.putExtra(EXTRA_REFERENCE_NUMBER, referenceNumber);
        return intent;
    }

    public static Intent newIntent(Context context, List<RecognizedField> fields) {
        Intent intent = new Intent(context, PayformResultActivity.class);
        for (RecognizedField field : fields) {
            switch (field.tokenType) {
                case RECEIVER:
                    intent.putExtra(EXTRA_SENDER, field.value);
                    break;
                case IBAN:
                    intent.putExtra(EXTRA_IBAN, field.value);
                    break;
                case AMOUNT:
                    intent.putExtra(EXTRA_AMOUNT, field.value);
                    break;
                case BIC:
                    intent.putExtra(EXTRA_BIC, field.value);
                    break;
                case REFERENCE_NUMBER:
                    intent.putExtra(EXTRA_REFERENCE_NUMBER, field.value);
                    break;
                case REFERENCE_NUMBER2:
                    intent.putExtra(EXTRA_REFERENCE_NUMBER_2, field.value);
                    break;
                case SENDER:
                    intent.putExtra(EXTRA_SENDER, field.value);
                    break;
                case SENDER_IBAN:
                    intent.putExtra(EXTRA_SENDER_IBAN, field.value);
                    break;
            }
        }
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payform_result);
        TextView sender = (TextView) findViewById(R.id.sender);
        TextView senderIban = (TextView) findViewById(R.id.sender_iban);
        TextView iban = (TextView) findViewById(R.id.iban);
        TextView bic = (TextView) findViewById(R.id.bic);
        TextView amount = (TextView) findViewById(R.id.amount);
        TextView referenceNumber = (TextView) findViewById(R.id.reference_number);
        TextView referenceNumber2 = (TextView) findViewById(R.id.reference_number_2);

        sender.setText(getIntent().getStringExtra(EXTRA_SENDER));
        senderIban.setText(getIntent().getStringExtra(EXTRA_SENDER_IBAN));
        iban.setText(getIntent().getStringExtra(EXTRA_IBAN));
        bic.setText(getIntent().getStringExtra(EXTRA_BIC));
        amount.setText(getIntent().getStringExtra(EXTRA_AMOUNT));
        referenceNumber.setText(getIntent().getStringExtra(EXTRA_REFERENCE_NUMBER));
        referenceNumber2.setText(getIntent().getStringExtra(EXTRA_REFERENCE_NUMBER_2));

        findViewById(R.id.retry).setOnClickListener(v -> finish());

        findViewById(R.id.accept).setOnClickListener(v -> moveTaskToBack(true));
    }
}
