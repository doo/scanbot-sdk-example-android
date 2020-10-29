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

class QRCodeDialogFragment : androidx.fragment.app.DialogFragment() {

    companion object {
        const val QR_DATA = "BARCODE_DATA"
        const val NAME = "QRCodeDialogFragment"

        @JvmStatic
        fun newInstanse(data: BarcodeScanningResult): QRCodeDialogFragment {
            val frag = QRCodeDialogFragment()
            val args = Bundle()
            args.putParcelable(QR_DATA, data)
            frag.arguments = args
            return frag
        }
    }

    private var qrCodeData: BarcodeScanningResult? = null

    private fun addContentView(inflater: LayoutInflater, container: ViewGroup): View {
        qrCodeData = arguments!!.getParcelable(QRCodeDialogFragment.QR_DATA)
        val view = inflater.inflate(R.layout.fragment_qr_code_dialog, container)

        val barcodeItem = qrCodeData?.barcodeItems?.firstOrNull()
        barcodeItem?.let {
            view.findViewById<TextView>(R.id.tv_data).text = it.text
        }

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
                getString(R.string.cancel_dialog_button)) { _, _ ->
            run {
                dismiss()
            }
        }

        builder.setNegativeButton(
                R.string.copy_dialog_button) { _, _ ->
            run {
                val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

                val barcodeItem = qrCodeData?.barcodeItems?.firstOrNull()
                barcodeItem?.let {
                    val clip = ClipData.newPlainText(it.text, it.text)
                    clipboard.setPrimaryClip(clip)
                }

                dismiss()
            }
        }
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(true)

        return dialog
    }
}