package io.scanbot.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.hicscanner.model.HealthInsuranceCardFieldType
import io.scanbot.hicscanner.model.HealthInsuranceCardRecognitionResult

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
        val result : HealthInsuranceCardRecognitionResult = intent.getParcelableExtra(EXTRA_EHIC_RESULT)!!

        for ((type, value) in result.fields) {
            when (type) {
                HealthInsuranceCardFieldType.SURNAME -> surname.text = value
                HealthInsuranceCardFieldType.GIVEN_NAME -> givenName.text = value
                HealthInsuranceCardFieldType.DATE_OF_BIRTH -> dateOfBirth.text = value
                HealthInsuranceCardFieldType.PERSONAL_IDENTIFICATION_NUMBER -> personalNumber.text = value
                HealthInsuranceCardFieldType.INSTITUTION_NUMBER -> institutionNumber.text = value
                HealthInsuranceCardFieldType.INSTITUTION_NAME -> institutionName.text = value
                HealthInsuranceCardFieldType.CARD_NUMBER -> cardNumber.text = value
                HealthInsuranceCardFieldType.CARD_EXPIRATION_DATE -> cardDate.text = value
                HealthInsuranceCardFieldType.COUNTRY -> country.text = value
            }
        }
        findViewById<View>(R.id.retry).setOnClickListener { finish() }
    }

    companion object {
        private const val EXTRA_EHIC_RESULT = "EHIC_RESULT"
        @JvmStatic
        fun newIntent(context: Context?, result: HealthInsuranceCardRecognitionResult): Intent {
            val intent = Intent(context, EhicResultActivity::class.java)
            intent.putExtra(EXTRA_EHIC_RESULT, result)
            return intent
        }
    }
}