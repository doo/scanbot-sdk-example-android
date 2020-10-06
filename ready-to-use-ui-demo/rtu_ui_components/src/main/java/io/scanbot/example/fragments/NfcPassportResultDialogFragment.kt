package io.scanbot.example.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import io.scanbot.example.R
import io.scanbot.sdk.ui.view.nfc.entity.NfcPassportScanningResult


class NfcPassportResultDialogFragment : androidx.fragment.app.DialogFragment() {

    companion object {
        const val NFC_DATA = "NFC_DATA"
        const val NAME = "NfcPassportResultDialogFragment"

        @JvmStatic
        fun newInstance(data: NfcPassportScanningResult): NfcPassportResultDialogFragment {
            val frag = NfcPassportResultDialogFragment()
            val args = Bundle()
            args.putParcelable(NFC_DATA, data)
            frag.arguments = args
            return frag
        }
    }

    private var nfcPassportScanningResult: NfcPassportScanningResult? = null

    private fun addContentView(inflater: LayoutInflater, container: ViewGroup?): View? {
        nfcPassportScanningResult = arguments!!.getParcelable(NFC_DATA)

        val view = inflater.inflate(R.layout.fragment_mrz_dialog, container)
        view.findViewById<TextView>(R.id.tv_data).text = extractData(nfcPassportScanningResult!!)
        return view
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(this.activity!!)
        val inflater = LayoutInflater.from(activity)
        val contentContainer = inflater.inflate(R.layout.holo_dialog_frame, null, false) as ViewGroup

        addContentView(inflater, contentContainer)

        builder.setView(contentContainer)
        builder.setPositiveButton(getString(R.string.cancel_dialog_button)) { _, _ ->
            dismiss()
        }

        builder.setNegativeButton(R.string.copy_dialog_button) { _, _ ->
            val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            if (nfcPassportScanningResult != null) {
                val data = extractData(nfcPassportScanningResult!!)
                val clip = ClipData.newPlainText(data, data)
                clipboard.setPrimaryClip(clip)
            }
            dismiss()
        }

        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(true)

        return dialog
    }

    private fun extractData(result: NfcPassportScanningResult): String {
        return StringBuilder()
                .append(getString(R.string.mrz_document_code)).append(result.dg1Group.documentNumber).append("\n")
                .append(getString(R.string.mrz_first_name)).append(result.dg1Group.nameOfDocumentHolder).append("\n")
                .append(getString(R.string.mrz_issuing_organization)).append(result.dg1Group.issuingStateOrOrganization).append("\n")
                .append(getString(R.string.mrz_nationality)).append(result.dg1Group.nationality).append("\n")
                .append(getString(R.string.mrz_dob)).append(result.dg1Group.dateOfBirth6Digit).append("\n")
                .append(getString(R.string.mrz_date_expiry)).append(result.dg1Group.dateOfExpiry).append("\n")
                .append(getString(R.string.mrz_gender)).append(result.dg2Group.gender).append("\n")
                // TODO: load and show any other data from the NfcPassportScanningResult object
                .toString()
    }
}