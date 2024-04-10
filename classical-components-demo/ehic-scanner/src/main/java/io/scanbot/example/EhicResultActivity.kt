package io.scanbot.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.ehicscanner.model.EhicFieldType
import io.scanbot.ehicscanner.model.EhicRecognitionResult

class EhicResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ehic_result)

        val surname = findViewById<TextView>(R.id.surname)
        val givenName = findViewById<TextView>(R.id.given_name)
        val dateOfBirth = findViewById<TextView>(R.id.date_of_birth)
        val personalNumber = findViewById<TextView>(R.id.personal_number)
        val institutionNumber = findViewById<TextView>(R.id.institution_number)
        val institutionName = findViewById<TextView>(R.id.institution_name)
        val cardNumber = findViewById<TextView>(R.id.card_number)
        val cardDate = findViewById<TextView>(R.id.card_date)
        val country = findViewById<TextView>(R.id.country)
        val result : EhicRecognitionResult = intent.getParcelableExtra(EXTRA_EHIC_RESULT)!!

        for ((type, value) in result.fields) {
            when (type) {
                EhicFieldType.SURNAME -> surname.text = value
                EhicFieldType.GIVEN_NAME -> givenName.text = value
                EhicFieldType.DATE_OF_BIRTH -> dateOfBirth.text = value
                EhicFieldType.PERSONAL_IDENTIFICATION_NUMBER -> personalNumber.text = value
                EhicFieldType.INSTITUTION_NUMBER -> institutionNumber.text = value
                EhicFieldType.INSTITUTION_NAME -> institutionName.text = value
                EhicFieldType.CARD_NUMBER -> cardNumber.text = value
                EhicFieldType.CARD_EXPIRATION_DATE -> cardDate.text = value
                EhicFieldType.COUNTRY -> country.text = value
            }
        }
        findViewById<View>(R.id.retry).setOnClickListener { finish() }
    }

    companion object {
        private const val EXTRA_EHIC_RESULT = "EHIC_RESULT"
        @JvmStatic
        fun newIntent(context: Context?, result: EhicRecognitionResult): Intent {
            val intent = Intent(context, EhicResultActivity::class.java)
            intent.putExtra(EXTRA_EHIC_RESULT, result)
            return intent
        }
    }
}