package io.scanbot.example.fragments

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.scanbot.example.R
import io.scanbot.mrzscanner.model.MRZRecognitionResult


class MRZDialogFragment : DialogFragment() {

    companion object {
        const val MRZ_DATA = "BARCODE_DATA"
        const val NAME = "MRZDialogFragment"

        @JvmStatic
        fun newInstanse(data: MRZRecognitionResult): MRZDialogFragment {
            val frag = MRZDialogFragment()
            val args = Bundle()
            args.putParcelable(MRZ_DATA, data)
            frag.arguments = args
            return frag
        }
    }

    fun addContentView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val mrzRecognitionResult = arguments!!.getParcelable<MRZRecognitionResult>(MRZ_DATA)

        val view = inflater.inflate(R.layout.fragment_mrz_dialog, container)
        view.findViewById<TextView>(R.id.tv_data).text = extractData(mrzRecognitionResult);
        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(this.activity!!)

        val inflater = LayoutInflater.from(activity)

        val contentContainer = inflater.inflate(R.layout.holo_dialog_frame, null, false) as ViewGroup
        addContentView(inflater, contentContainer, savedInstanceState)

        builder.setView(contentContainer)


        builder.setNegativeButton(
                "Cancel") { _, _ ->
            run {
                dismiss()
            }
        }
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(true)

        return dialog
    }

    private fun extractData(result: MRZRecognitionResult): String {
        return StringBuilder()
                .append("documentCode: ").append(result.documentCodeField().value).append("\n")
                .append("First name: ").append(result.firstNameField().value).append("\n")
                .append("Last name: ").append(result.lastNameField().value).append("\n")
                .append("issuingStateOrOrganization: ").append(result.issuingStateOrOrganizationField().value).append("\n")
                .append("departmentOfIssuance: ").append(result.departmentOfIssuanceField().value).append("\n")
                .append("nationality: ").append(result.nationalityField().value).append("\n")
                .append("dateOfBirth: ").append(result.dateOfBirthField().value).append("\n")
                .append("gender: ").append(result.genderField().value).append("\n")
                .append("dateOfExpiry: ").append(result.dateOfExpiryField().value).append("\n")
                .append("personalNumber: ").append(result.personalNumberField().value).append("\n")
                .append("optional1: ").append(result.optional1Field().value).append("\n")
                .append("optional2: ").append(result.optional2Field().value).append("\n")
                .append("discreetIssuingStateOrOrganization: ").append(result.discreetIssuingStateOrOrganizationField().value).append("\n")
                .append("validCheckDigitsCount: ").append(result.validCheckDigitsCount).append("\n")
                .append("checkDigitsCount: ").append(result.checkDigitsCount).append("\n")
                .append("travelDocType: ").append(result.travelDocTypeField().value).append("\n")
                .toString()
    }
}