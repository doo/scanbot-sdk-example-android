package io.scanbot.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.dcscanner.model.DCInfoBoxSubtype
import io.scanbot.dcscanner.model.DCPatientInfoField
import io.scanbot.dcscanner.model.DateRecordType
import io.scanbot.dcscanner.model.DisabilityCertificateRecognizerResultInfo

class DCResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dc_result)

        val checkboxesLayout = findViewById<LinearLayout>(R.id.dc_result_checkboxes_layout)
        val datesLayout = findViewById<LinearLayout>(R.id.dc_result_dates_layout)
        val patientInfoLayout = findViewById<LinearLayout>(R.id.dc_result_patient_info_layout)
        val otherLayout = findViewById<LinearLayout>(R.id.dc_result_other_layout)


        addValueView(checkboxesLayout, "Work accident", intent.getBooleanExtra(EXTRA_workAccident, false))
        addConfidenceValueView(checkboxesLayout,  intent.getDoubleExtra(EXTRA_workAccidentConf, 0.0))
        addValueView(checkboxesLayout, "Assigned to ins. doctor", intent.getBooleanExtra(EXTRA_assignedInsDoctor, false))
        addConfidenceValueView(checkboxesLayout, intent.getDoubleExtra(EXTRA_assignedInsDoctorConf, 0.0))
        addValueView(checkboxesLayout, "Initial certificate", intent.getBooleanExtra(EXTRA_initialCertificate, false))
        addConfidenceValueView(checkboxesLayout, intent.getDoubleExtra(EXTRA_initialCertificateConf, 0.0))
        addValueView(checkboxesLayout, "Renewed certificate", intent.getBooleanExtra(EXTRA_renewedCertificate, false))
        addConfidenceValueView(checkboxesLayout, intent.getDoubleExtra(EXTRA_renewedCertificateConf, 0.0))

        addValueView(datesLayout, "Incapable since", intent.getStringExtra(EXTRA_incapableSince))
        addConfidenceValueView(datesLayout,  intent.getDoubleExtra(EXTRA_incapableSinceRC, 0.0), ConfidenceType.RECOGNITION)
        addConfidenceValueView(datesLayout, intent.getDoubleExtra(EXTRA_incapableSinceVC, 0.0), ConfidenceType.VALIDATION)

        addValueView(datesLayout, "Incapable until", intent.getStringExtra(EXTRA_incapableUntil))
        addConfidenceValueView(datesLayout, intent.getDoubleExtra(EXTRA_incapableUntilRC, 0.0), ConfidenceType.RECOGNITION)
        addConfidenceValueView(datesLayout, intent.getDoubleExtra(EXTRA_incapableUntilVC, 0.0), ConfidenceType.VALIDATION)

        addValueView(datesLayout, "Diagnosed on", intent.getStringExtra(EXTRA_diagnosedOn))
        addConfidenceValueView(datesLayout, intent.getDoubleExtra(EXTRA_diagnosedOnRC, 0.0), ConfidenceType.RECOGNITION)
        addConfidenceValueView(datesLayout, intent.getDoubleExtra(EXTRA_diagnosedOnVC, 0.0), ConfidenceType.VALIDATION)

        addValueView(otherLayout, "Form type", intent.getStringExtra(EXTRA_formType))
        val parcelableArrayExtra = intent.getParcelableArrayExtra(EXTRA_patientInfo) as Array<Parcelable>
        parcelableArrayExtra.forEach {
            if (it is DCPatientInfoField) {
                addValueView(patientInfoLayout, it.patientInfoFieldType.name, it.value)
                addConfidenceValueView(patientInfoLayout, it.confidenceValue)
            }
        }

        findViewById<View>(R.id.retry).setOnClickListener { v: View? -> finish() }
    }

    private fun addValueView(layout: LinearLayout, title: String, value: String) {
        val v = layoutInflater.inflate(R.layout.view_key_value, layout, false)
        v.findViewById<TextView>(R.id.view_text_key).text = title
        v.findViewById<TextView>(R.id.view_text_value).text = value
        layout.addView(v)
    }
    private fun addValueView(layout: LinearLayout, title: String, value: Boolean) {
        val v = layoutInflater.inflate(R.layout.view_key_value, layout, false)
        v.findViewById<TextView>(R.id.view_text_key).text = title
        v.findViewById<TextView>(R.id.view_text_value).text = if (value) "Checked" else "Unchecked"
        layout.addView(v)
    }

    private fun addConfidenceValueView(layout: LinearLayout, value: Double, type: ConfidenceType = ConfidenceType.COMMON) {
        val v = layoutInflater.inflate(R.layout.view_confidence, layout, false)
        v.findViewById<TextView>(R.id.view_text_key).text = when (type) {
            ConfidenceType.COMMON -> "Confidence"
            ConfidenceType.VALIDATION -> "Validation confidence"
            ConfidenceType.RECOGNITION -> "Recognition confidence"
        }
        v.findViewById<TextView>(R.id.view_text_value).text =  "%.4f".format(value)
        layout.addView(v)
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
        const val EXTRA_patientInfo = "patientInfo"
        const val EXTRA_formType = "formType"

        @JvmStatic
        fun newIntent(context: Context?, result: DisabilityCertificateRecognizerResultInfo): Intent {
            val intent = Intent(context, DCResultActivity::class.java)
            for (checkbox in result.checkboxes) {
                when (checkbox.subType) {
                    DCInfoBoxSubtype.DCBoxUnknown, DCInfoBoxSubtype.DCBoxPatientInfo -> {
                    }
                    DCInfoBoxSubtype.DCBoxWorkAccident -> {
                        intent.putExtra(EXTRA_workAccident, checkbox.hasContents)
                        intent.putExtra(EXTRA_workAccidentConf, checkbox.contentsValidationConfidenceValue)
                    }
                    DCInfoBoxSubtype.DCBoxAssignedToAccidentInsuranceDoctor -> {
                        intent.putExtra(EXTRA_assignedInsDoctor, checkbox.hasContents)
                        intent.putExtra(EXTRA_assignedInsDoctorConf, checkbox.contentsValidationConfidenceValue)
                    }
                    DCInfoBoxSubtype.DCBoxInitialCertificate -> {
                        intent.putExtra(EXTRA_initialCertificate, checkbox.hasContents)
                        intent.putExtra(EXTRA_initialCertificateConf, checkbox.contentsValidationConfidenceValue)
                    }
                    DCInfoBoxSubtype.DCBoxRenewedCertificate -> {
                        intent.putExtra(EXTRA_renewedCertificate, checkbox.hasContents)
                        intent.putExtra(EXTRA_renewedCertificateConf, checkbox.contentsValidationConfidenceValue)
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

            intent.putExtra(EXTRA_formType, result.dcFormType.name)
            intent.putExtra(EXTRA_patientInfo, result.patientInfoFields.toTypedArray())

            return intent
        }
    }
}

private enum class ConfidenceType {
    COMMON,
    VALIDATION,
    RECOGNITION
}