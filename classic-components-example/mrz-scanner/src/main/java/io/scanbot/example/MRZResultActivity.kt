package io.scanbot.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.genericdocument.entity.MRZ
import io.scanbot.sdk.mrzscanner.MrzScannerResult

class MRZResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mrz_result)

        val travelDocType = findViewById<TextView>(R.id.travelDocType)
        val documentNumber = findViewById<TextView>(R.id.document_number)
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

        val checkDigitBirthDate = findViewById<TextView>(R.id.checkDigitBirthDate)
        val checkDigitDocumentNumber = findViewById<TextView>(R.id.checkDigitDocumentNumber)
        val checkDigitExpiryDate = findViewById<TextView>(R.id.checkDigitExpiryDate)
        val checkDigitGeneral = findViewById<TextView>(R.id.checkDigitGeneral)
        val checkDigitPersonalNumber = findViewById<TextView>(R.id.checkDigitPersonalNumber)

        val dateOfIssuance = findViewById<TextView>(R.id.dateOfIssuance)
        val documentTypeCode = findViewById<TextView>(R.id.documentTypeCode)
        val languageCode = findViewById<TextView>(R.id.languageCode)
        val pinCode = findViewById<TextView>(R.id.pinCode)
        val travelDocTypeVariant = findViewById<TextView>(R.id.travelDocTypeVariant)
        val versionNumber = findViewById<TextView>(R.id.versionNumber)

        val result: MrzScannerResult = intent.getParcelableExtra(EXTRA_MRZ_RESULT)!!

        val mrzResult = MRZ(result.document!!)

        travelDocType.text = mrzResult.travelDocType?.value?.text
        documentNumber.text = mrzResult.documentNumber?.value?.text
        firstName.text = mrzResult.givenNames?.value?.text
        lastName.text = mrzResult.surname?.value?.text
        issuingStateOrOrganization.text = mrzResult.issuingAuthority?.value?.text
        departmentOfIssuance.text = mrzResult.officeOfIssuance?.value?.text
        nationality.text = mrzResult.nationality?.value?.text
        dateOfBirth.text = mrzResult.birthDate?.value?.text
        gender.text = mrzResult.gender?.value?.text
        dateOfExpiry.text = mrzResult.expiryDate?.value?.text
        personalNumber.text = mrzResult.personalNumber?.value?.text
        optional1.text = mrzResult.optional1?.value?.text
        optional2.text = mrzResult.optional2?.value?.text

        checkDigitBirthDate.text = if (mrzResult.checkDigitBirthDate?.isValid == true) "Valid" else "NOT Valid"
        checkDigitDocumentNumber.text = if (mrzResult.checkDigitDocumentNumber?.isValid == true) "Valid" else "NOT Valid"
        checkDigitExpiryDate.text = if (mrzResult.checkDigitExpiryDate?.isValid == true) "Valid" else "NOT Valid"
        checkDigitGeneral.text = if (mrzResult.checkDigitGeneral?.isValid == true) "Valid" else "NOT Valid"
        checkDigitPersonalNumber.text = if (mrzResult.checkDigitPersonalNumber?.isValid == true) "Valid" else "NOT Valid"

        dateOfIssuance.text = mrzResult.dateOfIssuance?.value?.text
        documentTypeCode.text = mrzResult.documentTypeCode?.value?.text
        languageCode.text = mrzResult.languageCode?.value?.text
        pinCode.text = mrzResult.pinCode?.value?.text
        travelDocTypeVariant.text = mrzResult.travelDocTypeVariant?.value?.text
        versionNumber.text = mrzResult.versionNumber?.value?.text

        findViewById<View>(R.id.retry).setOnClickListener { finish() }
    }

    companion object {
        private const val EXTRA_MRZ_RESULT = "MRZ_RESULT"
        @JvmStatic
        fun newIntent(context: Context?, result: MrzScannerResult?): Intent {
            val intent = Intent(context, MRZResultActivity::class.java)
            intent.putExtra(EXTRA_MRZ_RESULT, result)
            return intent
        }
    }
}
