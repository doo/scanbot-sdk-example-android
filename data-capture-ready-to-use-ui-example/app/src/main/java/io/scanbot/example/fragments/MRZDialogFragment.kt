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
import io.scanbot.mrzscanner.model.MRZGenericDocument


class MRZDialogFragment : androidx.fragment.app.DialogFragment() {

    companion object {
        const val MRZ_DATA = "MRZ_DATA"
        const val NAME = "MRZDialogFragment"

        @JvmStatic
        fun newInstance(data: MRZGenericDocument): MRZDialogFragment {
            val frag = MRZDialogFragment()
            val args = Bundle()
            args.putParcelable(MRZ_DATA, data)
            frag.arguments = args
            return frag
        }
    }

    private var mrzGenericDocument: MRZGenericDocument? = null

    private fun addContentView(inflater: LayoutInflater, container: ViewGroup?): View? {
        mrzGenericDocument = requireArguments().getParcelable(MRZ_DATA)

        val view = inflater.inflate(R.layout.fragment_mrz_dialog, container)
        view.findViewById<TextView>(R.id.tv_data).text = extractData(mrzGenericDocument!!)
        return view
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(this.requireActivity())
        val inflater = LayoutInflater.from(activity)
        val contentContainer = inflater.inflate(R.layout.holo_dialog_frame, null, false) as ViewGroup

        addContentView(inflater, contentContainer)

        builder.setView(contentContainer)

        builder.setPositiveButton(getString(R.string.cancel_dialog_button)) { _, _ ->
            dismiss()
        }

        builder.setNegativeButton(R.string.copy_dialog_button) { _, _ ->
            val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            if (mrzGenericDocument != null) {
                val data = extractData(mrzGenericDocument!!)
                val clip = ClipData.newPlainText(data, data)
                clipboard.setPrimaryClip(clip)
            }
            dismiss()
        }

        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(true)

        return dialog
    }

    private fun extractData(result: MRZGenericDocument): String {
        return result.document?.fields?.joinToString("\n") { "${it.type.name}: ${it.value?.text}" } ?: ""
    }
}