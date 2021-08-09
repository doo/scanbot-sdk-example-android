package io.scanbot.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.mrzscanner.model.MRZRecognitionResult

class MRZResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mrz_result)

        val travelDocType = findViewById<TextView>(R.id.travelDocType)
        val documentCode = findViewById<TextView>(R.id.document_code)
        val firstName = findViewById<TextView>(R.id.first_name)
        val lastName = findViewById<TextView>(R.id.last_name)
        val issuingStateOrOrganization = findViewById<TextView>(R.id.issuingStateOrOrganization)
        val departmentOfIssuance = findViewById<TextView>(R.id.departmentOfIssuance)
        val nationality = findViewById<TextView>(R.id.nationality)
        val dateOfBirth = findViewById<TextView>(R.id.dateOfBirth)
        val gender = findViewById<TextView>(R.id.gender)
        val dateOfExpiry = findViewById<TextView>(R.id.dateOfExpiry)
        val personalNumber = findViewById<TextView>(R.id.personalNumber)
        val optional1 = findViewById<TextView>(R.id.optional1)
        val optional2 = findViewById<TextView>(R.id.optional2)
        val discreetIssuingStateOrOrganization = findViewById<TextView>(R.id.discreetIssuingStateOrOrganization)
        val validCheckDigitsCount = findViewById<TextView>(R.id.validCheckDigitsCount)
        val checkDigitsCount = findViewById<TextView>(R.id.checkDigitsCount)
        val checkDigits = findViewById<TextView>(R.id.checkDigits)
        val result: MRZRecognitionResult? = intent.getParcelableExtra(EXTRA_MRZ_RESULT)

        result?.let {
            travelDocType.text = result.travelDocType.name
            documentCode.text = result.documentCodeField().value
            firstName.text = result.firstNameField().value
            lastName.text = result.lastNameField().value
            issuingStateOrOrganization.text = result.issuingStateOrOrganizationField().value
            departmentOfIssuance.text = result.departmentOfIssuanceField().value
            nationality.text = result.nationalityField().value
            dateOfBirth.text = result.dateOfBirthField().value
            gender.text = result.genderField().value
            dateOfExpiry.text = result.dateOfExpiryField().value
            personalNumber.text = result.personalNumberField().value
            optional1.text = result.optional1Field().value
            optional2.text = result.optional2Field().value
            discreetIssuingStateOrOrganization.text = result.discreetIssuingStateOrOrganizationField().value
            validCheckDigitsCount.text = result.validCheckDigitsCount.toString()
            checkDigitsCount.text = result.checkDigitsCount.toString()

            var checkDigitsOutput = ""
            for ((checkDigitCharacter, successfullyValidated, type) in result.checkDigits) {
                checkDigitsOutput += type.name + ": " + checkDigitCharacter.toChar() + " (" + successfullyValidated + ")"
                checkDigitsOutput += "\n"
            }
            checkDigits.text = checkDigitsOutput
            findViewById<View>(R.id.retry).setOnClickListener { v: View? -> finish() }
        }
    }

    companion object {
        private const val EXTRA_MRZ_RESULT = "MRZ_RESULT"
        @JvmStatic
        fun newIntent(context: Context?, result: MRZRecognitionResult?): Intent {
            val intent = Intent(context, MRZResultActivity::class.java)
            intent.putExtra(EXTRA_MRZ_RESULT, result)
            return intent
        }
    }
}