package io.scanbot.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.sdk.core.payformscanner.model.RecognizedField
import io.scanbot.sdk.core.payformscanner.model.TokenType

class PayformResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payform_result)

        val sender = findViewById<View>(R.id.sender) as TextView
        val senderIban = findViewById<View>(R.id.sender_iban) as TextView
        val iban = findViewById<View>(R.id.iban) as TextView
        val bic = findViewById<View>(R.id.bic) as TextView
        val amount = findViewById<View>(R.id.amount) as TextView
        val referenceNumber = findViewById<View>(R.id.reference_number) as TextView
        val referenceNumber2 = findViewById<View>(R.id.reference_number_2) as TextView

        sender.text = intent.getStringExtra(EXTRA_SENDER)
        senderIban.text = intent.getStringExtra(EXTRA_SENDER_IBAN)
        iban.text = intent.getStringExtra(EXTRA_IBAN)
        bic.text = intent.getStringExtra(EXTRA_BIC)
        amount.text = intent.getStringExtra(EXTRA_AMOUNT)
        referenceNumber.text = intent.getStringExtra(EXTRA_REFERENCE_NUMBER)
        referenceNumber2.text = intent.getStringExtra(EXTRA_REFERENCE_NUMBER_2)
        findViewById<View>(R.id.retry).setOnClickListener { v: View? -> finish() }
        findViewById<View>(R.id.accept).setOnClickListener { v: View? -> moveTaskToBack(true) }
    }

    companion object {
        const val EXTRA_RECEIVER = "receiver"
        const val EXTRA_SENDER = "sender"
        const val EXTRA_IBAN = "iban"
        const val EXTRA_BIC = "bic"
        const val EXTRA_AMOUNT = "amount"
        const val EXTRA_REFERENCE_NUMBER = "reference_number"
        const val EXTRA_REFERENCE_NUMBER_2 = "reference_number_2"
        const val EXTRA_SENDER_IBAN = "sender_iban"

        @JvmStatic
        fun newIntent(context: Context, recevier: String?, sender: String?, iban: String?, bic: String?, amount: String?, referenceNumber: String?): Intent {
            val intent = Intent(context, PayformResultActivity::class.java)
            intent.putExtra(EXTRA_RECEIVER, recevier)
            intent.putExtra(EXTRA_SENDER, sender)
            intent.putExtra(EXTRA_IBAN, iban)
            intent.putExtra(EXTRA_BIC, bic)
            intent.putExtra(EXTRA_AMOUNT, amount)
            intent.putExtra(EXTRA_REFERENCE_NUMBER, referenceNumber)
            return intent
        }

        @JvmStatic
        fun newIntent(context: Context, fields: List<RecognizedField>): Intent {
            val intent = Intent(context, PayformResultActivity::class.java)
            for ((tokenType, value) in fields) {
                when (tokenType) {
                    TokenType.RECEIVER -> intent.putExtra(EXTRA_RECEIVER, value)
                    TokenType.IBAN -> intent.putExtra(EXTRA_IBAN, value)
                    TokenType.AMOUNT -> intent.putExtra(EXTRA_AMOUNT, value)
                    TokenType.BIC -> intent.putExtra(EXTRA_BIC, value)
                    TokenType.REFERENCE_NUMBER -> intent.putExtra(EXTRA_REFERENCE_NUMBER, value)
                    TokenType.REFERENCE_NUMBER2 -> intent.putExtra(EXTRA_REFERENCE_NUMBER_2, value)
                    TokenType.SENDER -> intent.putExtra(EXTRA_SENDER, value)
                    TokenType.SENDER_IBAN -> intent.putExtra(EXTRA_SENDER_IBAN, value)
                }
            }
            return intent
        }
    }
}