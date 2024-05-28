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
import io.scanbot.ehicscanner.model.EhicRecognitionResult
import io.scanbot.example.R


class EHICResultDialogFragment : androidx.fragment.app.DialogFragment() {

    companion object {
        const val EHIC_DATA = "EHIC_DATA"
        const val NAME = "EHICResultDialogFragment"

        @JvmStatic
        fun newInstance(recognitionResult: EhicRecognitionResult): EHICResultDialogFragment {
            val frag = EHICResultDialogFragment()
            val args = Bundle()
            args.putParcelable(EHIC_DATA, recognitionResult)
            frag.arguments = args
            return frag
        }
    }

    private var ehicRecognitionResult: EhicRecognitionResult? = null

    private fun addContentView(inflater: LayoutInflater, container: ViewGroup?): View? {
        ehicRecognitionResult = requireArguments().getParcelable(EHIC_DATA)

        val view = inflater.inflate(R.layout.fragment_ehic_result_dialog, container)
        view.findViewById<TextView>(R.id.ehic_data).text = extractData(ehicRecognitionResult!!)
        return view
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())

        val inflater = LayoutInflater.from(activity)

        val contentContainer = inflater.inflate(R.layout.holo_dialog_frame, null, false) as ViewGroup
        addContentView(inflater, contentContainer)

        builder.setView(contentContainer)


        builder.setPositiveButton(
                getString(R.string.cancel_dialog_button)) { _, _ ->
            run {
                dismiss()
            }
        }

        builder.setNegativeButton(
                R.string.copy_dialog_button) { _, _ ->
            run {
                val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

                ehicRecognitionResult?.let {
                    val data = extractData(it)
                    val clip = ClipData.newPlainText(data, data)
                    clipboard.setPrimaryClip(clip)
                }

                dismiss()
            }
        }
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(true)

        return dialog
    }

    private fun extractData(result: EhicRecognitionResult): String {
        val builder = StringBuilder()
        for (field in result.fields) {
            builder.append(field.type.name).append(": ").append(field.value).append("\n\n")
        }
        return builder.toString()
    }
}