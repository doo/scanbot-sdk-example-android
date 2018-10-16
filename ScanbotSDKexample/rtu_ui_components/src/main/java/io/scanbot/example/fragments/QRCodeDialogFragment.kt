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

class QRCodeDialogFragment : DialogFragment() {

    companion object {
        const val QR_DATA = "QR_DATA"
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Scanbot_Demo_Dialog)
    }


    private fun onCreateContentView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        val qrCodeData = arguments!!.getParcelable<BarcodeScanningResult>(QRCodeDialogFragment.QR_DATA)
        val view = inflater.inflate(R.layout.fragment_qr_code_dialog, container)

        view.findViewById<TextView>(R.id.tv_data).text = qrCodeData.text

        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(this.activity!!)

        val inflater = LayoutInflater.from(activity)

        val contentContainer = inflater.inflate(R.layout.holo_dialog_frame, null, false) as ViewGroup
        val contentView = onCreateContentView(inflater, contentContainer, savedInstanceState)

        builder.setView(contentContainer)


        builder.setNegativeButton(
                "Cancel",
                { _, _ ->
                    run {

                    }
                }
        )
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(true)

        return dialog
    }
}