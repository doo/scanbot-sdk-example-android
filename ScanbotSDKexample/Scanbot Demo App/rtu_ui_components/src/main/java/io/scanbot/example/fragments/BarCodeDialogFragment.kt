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
import io.scanbot.sdk.barcode.entity.BarcodeScanningResult

class BarCodeDialogFragment : DialogFragment() {

    companion object {
        const val BARCODE_DATA = "BarCodeDialogFragment"
        const val NAME = "BarCodeDialogFragment"

        @JvmStatic
        fun newInstanse(data: BarcodeScanningResult): BarCodeDialogFragment {
            val frag = BarCodeDialogFragment()
            val args = Bundle()
            args.putParcelable(BARCODE_DATA, data)
            frag.arguments = args
            return frag
        }
    }

    private fun addContentView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        val qrCodeData = arguments!!.getParcelable<BarcodeScanningResult>(BarCodeDialogFragment.BARCODE_DATA)
        val view = inflater.inflate(R.layout.fragment_barcode_dialog, container)

        view.findViewById<TextView>(R.id.tv_data).text = qrCodeData.text

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
}