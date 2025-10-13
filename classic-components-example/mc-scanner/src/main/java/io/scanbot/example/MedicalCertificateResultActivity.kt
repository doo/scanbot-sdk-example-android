package io.scanbot.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.example.common.applyEdgeToEdge
import io.scanbot.sdk.medicalcertificate.MedicalCertificateCheckBoxType
import io.scanbot.sdk.medicalcertificate.MedicalCertificateDateRecordType
import io.scanbot.sdk.medicalcertificate.MedicalCertificatePatientInfoField
import io.scanbot.sdk.medicalcertificate.MedicalCertificateScanningResult

class MedicalCertificateResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mc_result)
        applyEdgeToEdge(findViewById(R.id.root_view))

        val checkboxesLayout = findViewById<LinearLayout>(R.id.mc_result_checkboxes_layout)
        val datesLayout = findViewById<LinearLayout>(R.id.mc_result_dates_layout)
        val patientInfoLayout = findViewById<LinearLayout>(R.id.mc_result_patient_info_layout)
        val otherLayout = findViewById<LinearLayout>(R.id.mc_result_other_layout)


        addValueView(checkboxesLayout, "Work accident", intent.getBooleanExtra(EXTRA_workAccident, false))
        addConfidenceValueView(checkboxesLayout,  intent.getDoubleExtra(EXTRA_workAccidentConf, 0.0))
        addValueView(checkboxesLayout, "Assigned to ins. doctor", intent.getBooleanExtra(EXTRA_assignedInsDoctor, false))
        addConfidenceValueView(checkboxesLayout, intent.getDoubleExtra(EXTRA_assignedInsDoctorConf, 0.0))
        addValueView(checkboxesLayout, "Initial certificate", intent.getBooleanExtra(EXTRA_initialCertificate, false))
        addConfidenceValueView(checkboxesLayout, intent.getDoubleExtra(EXTRA_initialCertificateConf, 0.0))
        addValueView(checkboxesLayout, "Renewed certificate", intent.getBooleanExtra(EXTRA_renewedCertificate, false))
        addConfidenceValueView(checkboxesLayout, intent.getDoubleExtra(EXTRA_renewedCertificateConf, 0.0))
        addValueView(checkboxesLayout, "Insured pay case", intent.getBooleanExtra(EXTRA_insuredPayCase, false))
        addConfidenceValueView(checkboxesLayout, intent.getDoubleExtra(EXTRA_insuredPayCaseConf, 0.0))
        addValueView(checkboxesLayout, "Final certificate", intent.getBooleanExtra(EXTRA_finalCertificate, false))
        addConfidenceValueView(checkboxesLayout, intent.getDoubleExtra(EXTRA_finalCertificateConf, 0.0))
        addValueView(checkboxesLayout, "Requires care (yes)", intent.getBooleanExtra(EXTRA_requiresCareYes, false))
        addConfidenceValueView(checkboxesLayout, intent.getDoubleExtra(EXTRA_requiresCareYesConf, 0.0))
        addValueView(checkboxesLayout, "Requires care (no)", intent.getBooleanExtra(EXTRA_requiresCareNo, false))
        addConfidenceValueView(checkboxesLayout, intent.getDoubleExtra(EXTRA_requiresCareNoConf, 0.0))
        addValueView(checkboxesLayout, "Accident (yes)", intent.getBooleanExtra(EXTRA_accidentYes, false))
        addConfidenceValueView(checkboxesLayout, intent.getDoubleExtra(EXTRA_accidentYesConf, 0.0))
        addValueView(checkboxesLayout, "Accident (no)", intent.getBooleanExtra(EXTRA_accidentNo, false))
        addConfidenceValueView(checkboxesLayout, intent.getDoubleExtra(EXTRA_accidentNoConf, 0.0))
        addValueView(checkboxesLayout, "Other accident", intent.getBooleanExtra(EXTRA_otherAccident, false))
        addConfidenceValueView(checkboxesLayout, intent.getDoubleExtra(EXTRA_otherAccidentConf, 0.0))
        addValueView(checkboxesLayout, "Entitlement to continued payment (yes)", intent.getBooleanExtra(EXTRA_entitlementToContinuedPaymentYes, false))
        addConfidenceValueView(checkboxesLayout, intent.getDoubleExtra(EXTRA_entitlementToContinuedPaymentYesConf, 0.0))
        addValueView(checkboxesLayout, "Entitlement to continued payment (no)", intent.getBooleanExtra(EXTRA_entitlementToContinuedPaymentNo, false))
        addConfidenceValueView(checkboxesLayout, intent.getDoubleExtra(EXTRA_entitlementToContinuedPaymentNoConf, 0.0))
        addValueView(checkboxesLayout, "Sick pay was claimed (no)", intent.getBooleanExtra(EXTRA_sickPayWasClaimedNo, false))
        addConfidenceValueView(checkboxesLayout, intent.getDoubleExtra(EXTRA_sickPayWasClaimedNoConf, 0.0))
        addValueView(checkboxesLayout, "Sick pay was claimed (yes)", intent.getBooleanExtra(EXTRA_sickPayWasClaimedYes, false))
        addConfidenceValueView(checkboxesLayout, intent.getDoubleExtra(EXTRA_sickPayWasClaimedYesConf, 0.0))
        addValueView(checkboxesLayout, "Single parent (no)", intent.getBooleanExtra(EXTRA_singleParentNo, false))
        addConfidenceValueView(checkboxesLayout, intent.getDoubleExtra(EXTRA_singleParentNoConf, 0.0))
        addValueView(checkboxesLayout, "Single parent (yes)", intent.getBooleanExtra(EXTRA_singleParentYes, false))
        addConfidenceValueView(checkboxesLayout, intent.getDoubleExtra(EXTRA_singleParentYesConf, 0.0))

        intent.getStringExtra(EXTRA_incapableSince)?.let { addValueView(datesLayout, "Incapable since", it) }
        addConfidenceValueView(datesLayout,  intent.getDoubleExtra(EXTRA_incapableSinceRC, 0.0), ConfidenceType.RECOGNITION)

        intent.getStringExtra(EXTRA_incapableUntil)?.let { addValueView(datesLayout, "Incapable until", it) }
        addConfidenceValueView(datesLayout, intent.getDoubleExtra(EXTRA_incapableUntilRC, 0.0), ConfidenceType.RECOGNITION)

        intent.getStringExtra(EXTRA_diagnosedOn)?.let { addValueView(datesLayout, "Diagnosed on", it) }
        addConfidenceValueView(datesLayout, intent.getDoubleExtra(EXTRA_diagnosedOnRC, 0.0), ConfidenceType.RECOGNITION)

        intent.getStringExtra(EXTRA_documentDate)?.let { addValueView(datesLayout, "Document date", it) }
        addConfidenceValueView(datesLayout, intent.getDoubleExtra(EXTRA_documentDateRC, 0.0), ConfidenceType.RECOGNITION)

        intent.getStringExtra(EXTRA_birthDate)?.let { addValueView(datesLayout, "Birth date", it) }
        addConfidenceValueView(datesLayout, intent.getDoubleExtra(EXTRA_birthDateRC, 0.0), ConfidenceType.RECOGNITION)

        intent.getStringExtra(EXTRA_childNeedsCareFrom)?.let { addValueView(datesLayout, "Child needs care from", it) }
        addConfidenceValueView(datesLayout, intent.getDoubleExtra(EXTRA_childNeedsCareFromRC, 0.0), ConfidenceType.RECOGNITION)

        intent.getStringExtra(EXTRA_childNeedsCareUntil)?.let { addValueView(datesLayout, "Child needs care until", it) }
        addConfidenceValueView(datesLayout, intent.getDoubleExtra(EXTRA_childNeedsCareUntilRC, 0.0), ConfidenceType.RECOGNITION)

        intent.getStringExtra(EXTRA_formType)?.let { addValueView(otherLayout, "Form type", it) }
        val parcelableArrayExtra = intent.getParcelableArrayExtra(EXTRA_patientInfo) as Array<Parcelable>
        parcelableArrayExtra.forEach {
            if (it is MedicalCertificatePatientInfoField) {
                addValueView(patientInfoLayout, it.type.name, it.value)
                addConfidenceValueView(patientInfoLayout, it.recognitionConfidence)
            }
        }

        findViewById<View>(R.id.retry).setOnClickListener { finish() }
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
        const val EXTRA_insuredPayCase = "insuredPayCase"
        const val EXTRA_insuredPayCaseConf = "insuredPayCaseConf"
        const val EXTRA_finalCertificate = "finalCertificate"
        const val EXTRA_finalCertificateConf = "finalCertificateConf"
        const val EXTRA_requiresCareYes = "requiresCareYes"
        const val EXTRA_requiresCareYesConf = "requiresCareYesConf"
        const val EXTRA_requiresCareNo = "requiresCareNo"
        const val EXTRA_requiresCareNoConf = "requiresCareNoConf"
        const val EXTRA_accidentYes = "accidentYes"
        const val EXTRA_accidentYesConf = "accidentYesConf"
        const val EXTRA_accidentNo = "accidentNo"
        const val EXTRA_accidentNoConf = "accidentNoConf"
        const val EXTRA_otherAccident = "otherAccident"
        const val EXTRA_otherAccidentConf = "otherAccidentConf"
        const val EXTRA_entitlementToContinuedPaymentYes = "entitlementToContinuedPaymentYes"
        const val EXTRA_entitlementToContinuedPaymentYesConf = "entitlementToContinuedPaymentYesConf"
        const val EXTRA_entitlementToContinuedPaymentNo = "entitlementToContinuedPaymentNo"
        const val EXTRA_entitlementToContinuedPaymentNoConf = "entitlementToContinuedPaymentNoConf"
        const val EXTRA_sickPayWasClaimedNo = "sickPayWasClaimedNo"
        const val EXTRA_sickPayWasClaimedNoConf = "sickPayWasClaimedNoConf"
        const val EXTRA_sickPayWasClaimedYes = "sickPayWasClaimedYes"
        const val EXTRA_sickPayWasClaimedYesConf = "sickPayWasClaimedYesConf"
        const val EXTRA_singleParentNo = "singleParentNo"
        const val EXTRA_singleParentNoConf = "singleParentNoConf"
        const val EXTRA_singleParentYes = "singleParentYes"
        const val EXTRA_singleParentYesConf = "singleParentYesConf"
        const val EXTRA_incapableSince = "incapableSince"
        const val EXTRA_incapableSinceRC = "incapableSinceRC"
        const val EXTRA_incapableUntil = "incapableUntil"
        const val EXTRA_incapableUntilRC = "incapableUntilRC"
        const val EXTRA_diagnosedOn = "diagnosedOn"
        const val EXTRA_diagnosedOnRC = "diagnosedOnRC"
        const val EXTRA_documentDate = "documentDate"
        const val EXTRA_documentDateRC = "documentDateRC"
        const val EXTRA_birthDate = "birthDate"
        const val EXTRA_birthDateRC = "birthDateRC"
        const val EXTRA_childNeedsCareFrom = "childNeedsCareFrom"
        const val EXTRA_childNeedsCareFromRC = "childNeedsCareFromRC"
        const val EXTRA_childNeedsCareUntil = "childNeedsCareUntil"
        const val EXTRA_childNeedsCareUntilRC = "childNeedsCareUntilRC"
        const val EXTRA_patientInfo = "patientInfo"
        const val EXTRA_formType = "formType"

        @JvmStatic
        fun newIntent(context: Context?, result: MedicalCertificateScanningResult): Intent {
            val intent = Intent(context, MedicalCertificateResultActivity::class.java)
            for (checkbox in result.checkBoxes) {
                when (checkbox.type) {
                    MedicalCertificateCheckBoxType.UNKNOWN -> {
                    }
                    MedicalCertificateCheckBoxType.WORK_ACCIDENT -> {
                        intent.putExtra(EXTRA_workAccident, checkbox.checked)
                        intent.putExtra(EXTRA_workAccidentConf, checkbox.checkedConfidence)
                    }
                    MedicalCertificateCheckBoxType.ASSIGNED_TO_ACCIDENT_INSURANCE_DOCTOR -> {
                        intent.putExtra(EXTRA_assignedInsDoctor, checkbox.checked)
                        intent.putExtra(EXTRA_assignedInsDoctorConf, checkbox.checkedConfidence)
                    }
                    MedicalCertificateCheckBoxType.INITIAL_CERTIFICATE -> {
                        intent.putExtra(EXTRA_initialCertificate, checkbox.checked)
                        intent.putExtra(EXTRA_initialCertificateConf, checkbox.checkedConfidence)
                    }
                    MedicalCertificateCheckBoxType.RENEWED_CERTIFICATE -> {
                        intent.putExtra(EXTRA_renewedCertificate, checkbox.checked)
                        intent.putExtra(EXTRA_renewedCertificateConf, checkbox.checkedConfidence)
                    }
                    MedicalCertificateCheckBoxType.INSURED_PAY_CASE -> {
                        intent.putExtra(EXTRA_insuredPayCase, checkbox.checked)
                        intent.putExtra(EXTRA_insuredPayCaseConf, checkbox.checkedConfidence)
                    }
                    MedicalCertificateCheckBoxType.FINAL_CERTIFICATE -> {
                        intent.putExtra(EXTRA_finalCertificate, checkbox.checked)
                        intent.putExtra(EXTRA_finalCertificateConf, checkbox.checkedConfidence)
                    }
                    MedicalCertificateCheckBoxType.REQUIRES_CARE_YES -> {
                        intent.putExtra(EXTRA_requiresCareYes, checkbox.checked)
                        intent.putExtra(EXTRA_requiresCareYesConf, checkbox.checkedConfidence)
                    }
                    MedicalCertificateCheckBoxType.REQUIRES_CARE_NO -> {
                        intent.putExtra(EXTRA_requiresCareNo, checkbox.checked)
                        intent.putExtra(EXTRA_requiresCareNoConf, checkbox.checkedConfidence)
                    }
                    MedicalCertificateCheckBoxType.ACCIDENT_YES -> {
                        intent.putExtra(EXTRA_accidentYes, checkbox.checked)
                        intent.putExtra(EXTRA_accidentYesConf, checkbox.checkedConfidence)
                    }
                    MedicalCertificateCheckBoxType.ACCIDENT_NO -> {
                        intent.putExtra(EXTRA_accidentNo, checkbox.checked)
                        intent.putExtra(EXTRA_accidentNoConf, checkbox.checkedConfidence)
                    }
                    MedicalCertificateCheckBoxType.OTHER_ACCIDENT -> {
                        intent.putExtra(EXTRA_otherAccident, checkbox.checked)
                        intent.putExtra(EXTRA_otherAccidentConf, checkbox.checkedConfidence)
                    }
                    MedicalCertificateCheckBoxType.ENTITLEMENT_TO_CONTINUED_PAYMENT_YES -> {
                        intent.putExtra(EXTRA_entitlementToContinuedPaymentYes, checkbox.checked)
                        intent.putExtra(EXTRA_entitlementToContinuedPaymentYesConf, checkbox.checkedConfidence)
                    }
                    MedicalCertificateCheckBoxType.ENTITLEMENT_TO_CONTINUED_PAYMENT_NO -> {
                        intent.putExtra(EXTRA_entitlementToContinuedPaymentNo, checkbox.checked)
                        intent.putExtra(EXTRA_entitlementToContinuedPaymentNoConf, checkbox.checkedConfidence)
                    }
                    MedicalCertificateCheckBoxType.SICK_PAY_WAS_CLAIMED_YES -> {
                        intent.putExtra(EXTRA_sickPayWasClaimedNo, checkbox.checked)
                        intent.putExtra(EXTRA_sickPayWasClaimedNoConf, checkbox.checkedConfidence)
                    }
                    MedicalCertificateCheckBoxType.SICK_PAY_WAS_CLAIMED_NO -> {
                        intent.putExtra(EXTRA_sickPayWasClaimedYes, checkbox.checked)
                        intent.putExtra(EXTRA_sickPayWasClaimedYesConf, checkbox.checkedConfidence)
                    }
                    MedicalCertificateCheckBoxType.SINGLE_PARENT_NO -> {
                        intent.putExtra(EXTRA_singleParentNo, checkbox.checked)
                        intent.putExtra(EXTRA_singleParentNoConf, checkbox.checkedConfidence)
                    }
                    MedicalCertificateCheckBoxType.SINGLE_PARENT_YES -> {
                        intent.putExtra(EXTRA_singleParentYes, checkbox.checked)
                        intent.putExtra(EXTRA_singleParentYesConf, checkbox.checkedConfidence)
                    }
                }
            }
            for (date in result.dates) {
                when (date.type) {
                    MedicalCertificateDateRecordType.INCAPABLE_OF_WORK_SINCE -> {
                        intent.putExtra(EXTRA_incapableSince, date.value)
                        intent.putExtra(EXTRA_incapableSinceRC, date.recognitionConfidence)
                    }
                    MedicalCertificateDateRecordType.INCAPABLE_OF_WORK_UNTIL -> {
                        intent.putExtra(EXTRA_incapableUntil, date.value)
                        intent.putExtra(EXTRA_incapableUntilRC, date.recognitionConfidence)
                    }
                    MedicalCertificateDateRecordType.DIAGNOSED_ON -> {
                        intent.putExtra(EXTRA_diagnosedOn, date.value)
                        intent.putExtra(EXTRA_diagnosedOnRC, date.recognitionConfidence)
                    }
                    MedicalCertificateDateRecordType.UNDEFINED -> {
                    }

                    MedicalCertificateDateRecordType.DOCUMENT_DATE -> {
                        intent.putExtra(EXTRA_documentDate, date.value)
                        intent.putExtra(EXTRA_documentDateRC, date.recognitionConfidence)
                    }
                    MedicalCertificateDateRecordType.BIRTH_DATE -> {
                        intent.putExtra(EXTRA_birthDate, date.value)
                        intent.putExtra(EXTRA_birthDateRC, date.recognitionConfidence)
                    }
                    MedicalCertificateDateRecordType.CHILD_NEEDS_CARE_FROM -> {
                        intent.putExtra(EXTRA_childNeedsCareFrom, date.value)
                        intent.putExtra(EXTRA_childNeedsCareFromRC, date.recognitionConfidence)
                    }
                    MedicalCertificateDateRecordType.CHILD_NEEDS_CARE_UNTIL -> {
                        intent.putExtra(EXTRA_childNeedsCareUntil, date.value)
                        intent.putExtra(EXTRA_childNeedsCareUntilRC, date.recognitionConfidence)
                    }
                }
            }

            intent.putExtra(EXTRA_formType, result.formType.name)
            intent.putExtra(EXTRA_patientInfo, result.patientInfoBox.fields.toTypedArray())

            return intent
        }
    }
}

private enum class ConfidenceType {
    COMMON,
    VALIDATION,
    RECOGNITION
}
