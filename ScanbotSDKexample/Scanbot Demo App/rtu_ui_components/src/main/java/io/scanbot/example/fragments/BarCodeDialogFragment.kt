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
import io.scanbot.sdk.barcode.entity.BarcodeScanningResult

class BarCodeDialogFragment : androidx.fragment.app.DialogFragment() {

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

    private var qrCodeData: BarcodeScanningResult? = null

    private fun addContentView(inflater: LayoutInflater, container: ViewGroup): View {
        qrCodeData = arguments!!.getParcelable(BarCodeDialogFragment.BARCODE_DATA)
        val view = inflater.inflate(R.layout.fragment_barcode_dialog, container)

        view.findViewById<TextView>(R.id.tv_data).text = qrCodeData?.text

        return view
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(this.activity!!)

        val inflater = LayoutInflater.from(activity)

        val contentContainer = inflater.inflate(R.layout.holo_dialog_frame, null, false) as ViewGroup
        addContentView(inflater, contentContainer)

        builder.setView(contentContainer)


        builder.setPositiveButton(
                R.string.cancel_dialog_button) { _, _ ->
            run {
                dismiss()
            }
        }

        builder.setNegativeButton(
                R.string.copy_dialog_button) { _, _ ->
            run {
                val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(qrCodeData?.text, qrCodeData?.text)
                clipboard.primaryClip = clip
                dismiss()
            }
        }
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(true)

        return dialog
    }
}