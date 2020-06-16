package io.scanbot.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.dcscanner.model.DCInfoBoxSubtype
import io.scanbot.dcscanner.model.DateRecordType
import io.scanbot.dcscanner.model.DisabilityCertificateRecognizerResultInfo

class DCResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dc_result)

        val workAccident = findViewById<View>(R.id.work_accident) as TextView
        val workAccidentConf = findViewById<View>(R.id.work_accident_confidence) as TextView
        val assignedInsDoctor = findViewById<View>(R.id.assigned_ins_doctor) as TextView
        val assignedInsDoctorConf = findViewById<View>(R.id.assigned_ins_doctor_confidence) as TextView
        val initialCertificate = findViewById<View>(R.id.initial_certificate) as TextView
        val initialCertificateConf = findViewById<View>(R.id.initial_certificate_confidence) as TextView
        val renewedCertificate = findViewById<View>(R.id.renewed_certificate) as TextView
        val renewedCertificateConf = findViewById<View>(R.id.renewed_certificate_confidence) as TextView
        val incapableSince = findViewById<View>(R.id.incapable_since) as TextView
        val incapableSinceRC = findViewById<View>(R.id.incapable_since_rc) as TextView
        val incapableSinceVC = findViewById<View>(R.id.incapable_since_vc) as TextView
        val incapableUntil = findViewById<View>(R.id.incapable_until) as TextView
        val incapableUntilRC = findViewById<View>(R.id.incapable_until_rc) as TextView
        val incapableUntilVC = findViewById<View>(R.id.incapable_until_vc) as TextView
        val diagnosedOn = findViewById<View>(R.id.diagnosed_on) as TextView
        val diagnosedOnRC = findViewById<View>(R.id.diagnosed_on_rc) as TextView
        val diagnosedOnVC = findViewById<View>(R.id.diagnosed_on_vc) as TextView


        workAccident.text = if (intent.getBooleanExtra(EXTRA_workAccident, false)) "Checked" else "Unchecked"
        workAccidentConf.text = intent.getDoubleExtra(EXTRA_workAccidentConf, 0.0).toString()
        assignedInsDoctor.text = if (intent.getBooleanExtra(EXTRA_assignedInsDoctor, false)) "Checked" else "Unchecked"
        assignedInsDoctorConf.text = intent.getDoubleExtra(EXTRA_assignedInsDoctorConf, 0.0).toString()
        initialCertificate.text = if (intent.getBooleanExtra(EXTRA_initialCertificate, false)) "Checked" else "Unchecked"
        initialCertificateConf.text = intent.getDoubleExtra(EXTRA_initialCertificateConf, 0.0).toString()
        renewedCertificate.text = if (intent.getBooleanExtra(EXTRA_renewedCertificate, false)) "Checked" else "Unchecked"
        renewedCertificateConf.text = intent.getDoubleExtra(EXTRA_renewedCertificateConf, 0.0).toString()
        incapableSince.text = intent.getStringExtra(EXTRA_incapableSince)
        incapableSinceRC.text = intent.getDoubleExtra(EXTRA_incapableSinceRC, 0.0).toString()
        incapableSinceVC.text = intent.getDoubleExtra(EXTRA_incapableSinceVC, 0.0).toString()
        incapableUntil.text = intent.getStringExtra(EXTRA_incapableUntil)
        incapableUntilRC.text = intent.getDoubleExtra(EXTRA_incapableUntilRC, 0.0).toString()
        incapableUntilVC.text = intent.getDoubleExtra(EXTRA_incapableUntilVC, 0.0).toString()
        diagnosedOn.text = intent.getStringExtra(EXTRA_diagnosedOn)
        diagnosedOnRC.text = intent.getDoubleExtra(EXTRA_diagnosedOnRC, 0.0).toString()
        diagnosedOnVC.text = intent.getDoubleExtra(EXTRA_diagnosedOnVC, 0.0).toString()
        findViewById<View>(R.id.retry).setOnClickListener { v: View? -> finish() }
    }

    companion object {
        const val EXTRA_workAccident = "workAccident"
        const val EXTRA_workAccidentConf = "workAccidentConf"
        const val EXTRA_assignedInsDoctor = "assignedInsDoctor"
        const val EXTRA_assignedInsDoctorConf = "assignedInsDoctorConf"
        const val EXTRA_initialCertificate = "initialCertificate"
        const val EXTRA_initialCertificateConf = "initialCertificateConf"
        const val EXTRA_renewedCertificate = "renewedCertificate"
        const val EXTRA_renewedCertificateConf = "renewedCertificateConf"
        const val EXTRA_incapableSince = "incapableSince"
        const val EXTRA_incapableSinceRC = "incapableSinceRC"
        const val EXTRA_incapableSinceVC = "incapableSinceVC"
        const val EXTRA_incapableUntil = "incapableUntil"
        const val EXTRA_incapableUntilRC = "incapableUntilRC"
        const val EXTRA_incapableUntilVC = "incapableUntilVC"
        const val EXTRA_diagnosedOn = "diagnosedOn"
        const val EXTRA_diagnosedOnRC = "diagnosedOnRC"
        const val EXTRA_diagnosedOnVC = "diagnosedOnVC"

        @JvmStatic
        fun newIntent(context: Context?, result: DisabilityCertificateRecognizerResultInfo): Intent {
            val intent = Intent(context, DCResultActivity::class.java)
            for (chackbox in result.checkboxes) {
                when (chackbox.subType) {
                    DCInfoBoxSubtype.DCBoxUnknown, DCInfoBoxSubtype.DCBoxPatientInfo -> {
                    }
                    DCInfoBoxSubtype.DCBoxWorkAccident -> {
                        intent.putExtra(EXTRA_workAccident, chackbox.hasContents)
                        intent.putExtra(EXTRA_workAccidentConf, chackbox.contentsValidationConfidenceValue)
                    }
                    DCInfoBoxSubtype.DCBoxAssignedToAccidentInsuranceDoctor -> {
                        intent.putExtra(EXTRA_assignedInsDoctor, chackbox.hasContents)
                        intent.putExtra(EXTRA_assignedInsDoctorConf, chackbox.contentsValidationConfidenceValue)
                    }
                    DCInfoBoxSubtype.DCBoxInitialCertificate -> {
                        intent.putExtra(EXTRA_initialCertificate, chackbox.hasContents)
                        intent.putExtra(EXTRA_initialCertificateConf, chackbox.contentsValidationConfidenceValue)
                    }
                    DCInfoBoxSubtype.DCBoxRenewedCertificate -> {
                        intent.putExtra(EXTRA_renewedCertificate, chackbox.hasContents)
                        intent.putExtra(EXTRA_renewedCertificateConf, chackbox.contentsValidationConfidenceValue)
                    }
                }
            }
            for (date in result.dates) {
                when (date.type) {
                    DateRecordType.DateRecordIncapableOfWorkSince -> {
                        intent.putExtra(EXTRA_incapableSince, date.dateString)
                        intent.putExtra(EXTRA_incapableSinceRC, date.recognitionConfidenceValue)
                        intent.putExtra(EXTRA_incapableSinceVC, date.validationConfidenceValue)
                    }
                    DateRecordType.DateRecordIncapableOfWorkUntil -> {
                        intent.putExtra(EXTRA_incapableUntil, date.dateString)
                        intent.putExtra(EXTRA_incapableUntilRC, date.recognitionConfidenceValue)
                        intent.putExtra(EXTRA_incapableUntilVC, date.validationConfidenceValue)
                    }
                    DateRecordType.DateRecordDiagnosedOn -> {
                        intent.putExtra(EXTRA_diagnosedOn, date.dateString)
                        intent.putExtra(EXTRA_diagnosedOnRC, date.recognitionConfidenceValue)
                        intent.putExtra(EXTRA_diagnosedOnVC, date.validationConfidenceValue)
                    }
                    DateRecordType.DateRecordUndefined -> {
                    }
                }
            }
            return intent
        }
    }
}