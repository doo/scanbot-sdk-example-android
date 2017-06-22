package io.scanbot.example;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import net.doo.snap.payformscanner.entity.RecognizedField;

import java.util.List;

public class PayformResultActivity extends AppCompatActivity {
    public static final String EXTRA_SENDER = "sender";
    public static final String EXTRA_IBAN = "iban";
    public static final String EXTRA_BIC = "bic";
    public static final String EXTRA_AMOUNT = "amount";
    public static final String EXTRA_REFERENCE_NUMBER = "reference_number";

    private TextView sender;
    private TextView iban;
    private TextView bic;
    private TextView amount;
    private TextView referenceNumber;

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
            switch (field.getTokenType()) {
                case RECEIVER:
                    intent.putExtra(EXTRA_SENDER, field.getValue());
                    break;
                case IBAN:
                    intent.putExtra(EXTRA_IBAN, field.getValue());
                    break;
                case AMOUNT:
                    intent.putExtra(EXTRA_AMOUNT, field.getValue());
                    break;
                case BIC:
                    intent.putExtra(EXTRA_BIC, field.getValue());
                    break;
                case REFERENCE_NUMBER:
                    intent.putExtra(EXTRA_REFERENCE_NUMBER, field.getValue());
                    break;
            }
        }
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payform_result);
        sender = (TextView) findViewById(R.id.sender);
        iban = (TextView) findViewById(R.id.iban);
        bic = (TextView) findViewById(R.id.bic);
        amount = (TextView) findViewById(R.id.amount);
        referenceNumber = (TextView) findViewById(R.id.reference_number);

        sender.setText(getIntent().getStringExtra(EXTRA_SENDER));
        iban.setText(getIntent().getStringExtra(EXTRA_IBAN));
        bic.setText(getIntent().getStringExtra(EXTRA_BIC));
        amount.setText(getIntent().getStringExtra(EXTRA_AMOUNT));
        referenceNumber.setText(getIntent().getStringExtra(EXTRA_REFERENCE_NUMBER));

        findViewById(R.id.retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.accept).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveTaskToBack(true);
            }
        });
    }
}
