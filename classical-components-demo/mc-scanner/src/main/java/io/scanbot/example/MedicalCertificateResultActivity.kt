package io.scanbot.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.mcscanner.model.DateRecordType
import io.scanbot.mcscanner.model.CheckBoxType
import io.scanbot.mcscanner.model.McPatientInfoField
import io.scanbot.sdk.mcrecognizer.entity.MedicalCertificateRecognizerResult

class MedicalCertificateResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mc_result)

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
            if (it is McPatientInfoField) {
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
        fun newIntent(context: Context?, result: MedicalCertificateRecognizerResult): Intent {
            val intent = Intent(context, MedicalCertificateResultActivity::class.java)
            for (checkbox in result.checkboxes) {
                when (checkbox.type) {
                    CheckBoxType.McBoxUnknown -> {
                    }
                    CheckBoxType.McBoxWorkAccident -> {
                        intent.putExtra(EXTRA_workAccident, checkbox.hasContents)
                        intent.putExtra(EXTRA_workAccidentConf, checkbox.contentsValidationConfidenceValue)
                    }
                    CheckBoxType.McBoxAssignedToAccidentInsuranceDoctor -> {
                        intent.putExtra(EXTRA_assignedInsDoctor, checkbox.hasContents)
                        intent.putExtra(EXTRA_assignedInsDoctorConf, checkbox.contentsValidationConfidenceValue)
                    }
                    CheckBoxType.McBoxInitialCertificate -> {
                        intent.putExtra(EXTRA_initialCertificate, checkbox.hasContents)
                        intent.putExtra(EXTRA_initialCertificateConf, checkbox.contentsValidationConfidenceValue)
                    }
                    CheckBoxType.McBoxRenewedCertificate -> {
                        intent.putExtra(EXTRA_renewedCertificate, checkbox.hasContents)
                        intent.putExtra(EXTRA_renewedCertificateConf, checkbox.contentsValidationConfidenceValue)
                    }
                    CheckBoxType.McBoxInsuredPayCase -> {
                        intent.putExtra(EXTRA_insuredPayCase, checkbox.hasContents)
                        intent.putExtra(EXTRA_insuredPayCaseConf, checkbox.contentsValidationConfidenceValue)
                    }
                    CheckBoxType.McBoxFinalCertificate -> {
                        intent.putExtra(EXTRA_finalCertificate, checkbox.hasContents)
                        intent.putExtra(EXTRA_finalCertificateConf, checkbox.contentsValidationConfidenceValue)
                    }
                    CheckBoxType.McBoxRequiresCareYes -> {
                        intent.putExtra(EXTRA_requiresCareYes, checkbox.hasContents)
                        intent.putExtra(EXTRA_requiresCareYesConf, checkbox.contentsValidationConfidenceValue)
                    }
                    CheckBoxType.McBoxRequiresCareNo -> {
                        intent.putExtra(EXTRA_requiresCareNo, checkbox.hasContents)
                        intent.putExtra(EXTRA_requiresCareNoConf, checkbox.contentsValidationConfidenceValue)
                    }
                    CheckBoxType.McBoxAccidentYes -> {
                        intent.putExtra(EXTRA_accidentYes, checkbox.hasContents)
                        intent.putExtra(EXTRA_accidentYesConf, checkbox.contentsValidationConfidenceValue)
                    }
                    CheckBoxType.McBoxAccidentNo -> {
                        intent.putExtra(EXTRA_accidentNo, checkbox.hasContents)
                        intent.putExtra(EXTRA_accidentNoConf, checkbox.contentsValidationConfidenceValue)
                    }
                    CheckBoxType.McBoxOtherAccident -> {
                        intent.putExtra(EXTRA_otherAccident, checkbox.hasContents)
                        intent.putExtra(EXTRA_otherAccidentConf, checkbox.contentsValidationConfidenceValue)
                    }
                    CheckBoxType.McBoxEntitlementToContinuedPaymentYes -> {
                        intent.putExtra(EXTRA_entitlementToContinuedPaymentYes, checkbox.hasContents)
                        intent.putExtra(EXTRA_entitlementToContinuedPaymentYesConf, checkbox.contentsValidationConfidenceValue)
                    }
                    CheckBoxType.McBoxEntitlementToContinuedPaymentNo -> {
                        intent.putExtra(EXTRA_entitlementToContinuedPaymentNo, checkbox.hasContents)
                        intent.putExtra(EXTRA_entitlementToContinuedPaymentNoConf, checkbox.contentsValidationConfidenceValue)
                    }
                    CheckBoxType.McBoxSickPayWasClaimedNo -> {
                        intent.putExtra(EXTRA_sickPayWasClaimedNo, checkbox.hasContents)
                        intent.putExtra(EXTRA_sickPayWasClaimedNoConf, checkbox.contentsValidationConfidenceValue)
                    }
                    CheckBoxType.McBoxSickPayWasClaimedYes -> {
                        intent.putExtra(EXTRA_sickPayWasClaimedYes, checkbox.hasContents)
                        intent.putExtra(EXTRA_sickPayWasClaimedYesConf, checkbox.contentsValidationConfidenceValue)
                    }
                    CheckBoxType.McBoxSingleParentNo -> {
                        intent.putExtra(EXTRA_singleParentNo, checkbox.hasContents)
                        intent.putExtra(EXTRA_singleParentNoConf, checkbox.contentsValidationConfidenceValue)
                    }
                    CheckBoxType.McBoxSingleParentYes -> {
                        intent.putExtra(EXTRA_singleParentYes, checkbox.hasContents)
                        intent.putExtra(EXTRA_singleParentYesConf, checkbox.contentsValidationConfidenceValue)
                    }
                }
            }
            for (date in result.dates) {
                when (date.type) {
                    DateRecordType.DateRecordIncapableOfWorkSince -> {
                        intent.putExtra(EXTRA_incapableSince, date.dateString)
                        intent.putExtra(EXTRA_incapableSinceRC, date.recognitionConfidenceValue)
                    }
                    DateRecordType.DateRecordIncapableOfWorkUntil -> {
                        intent.putExtra(EXTRA_incapableUntil, date.dateString)
                        intent.putExtra(EXTRA_incapableUntilRC, date.recognitionConfidenceValue)
                    }
                    DateRecordType.DateRecordDiagnosedOn -> {
                        intent.putExtra(EXTRA_diagnosedOn, date.dateString)
                        intent.putExtra(EXTRA_diagnosedOnRC, date.recognitionConfidenceValue)
                    }
                    DateRecordType.DateRecordUndefined -> {
                    }

                    DateRecordType.DateRecordDocumentDate -> {
                        intent.putExtra(EXTRA_documentDate, date.dateString)
                        intent.putExtra(EXTRA_documentDateRC, date.recognitionConfidenceValue)
                    }
                    DateRecordType.DateRecordBirthDate -> {
                        intent.putExtra(EXTRA_birthDate, date.dateString)
                        intent.putExtra(EXTRA_birthDateRC, date.recognitionConfidenceValue)
                    }
                    DateRecordType.DateRecordChildNeedsCareFrom -> {
                        intent.putExtra(EXTRA_childNeedsCareFrom, date.dateString)
                        intent.putExtra(EXTRA_childNeedsCareFromRC, date.recognitionConfidenceValue)
                    }
                    DateRecordType.DateRecordChildNeedsCareUntil -> {
                        intent.putExtra(EXTRA_childNeedsCareUntil, date.dateString)
                        intent.putExtra(EXTRA_childNeedsCareUntilRC, date.recognitionConfidenceValue)
                    }
                }
            }

            intent.putExtra(EXTRA_formType, result.mcFormType.name)
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