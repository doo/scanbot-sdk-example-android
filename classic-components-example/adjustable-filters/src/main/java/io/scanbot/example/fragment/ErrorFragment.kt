package io.scanbot.example.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import io.scanbot.example.R

class ErrorFragment : DialogFragment() {

    companion object {
        const val NAME = "ErrorFragment"

        @JvmStatic
        fun newInstance(): ErrorFragment {
            return ErrorFragment()
        }
    }

    private fun addContentView(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.fragment_expired_license_dialog, container)
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())

        val inflater = LayoutInflater.from(activity)

        val contentContainer = inflater.inflate(R.layout.holo_dialog_frame, null, false) as ViewGroup
        addContentView(inflater, contentContainer)

        builder.setView(contentContainer)

        builder.setNegativeButton(getString(R.string.cancel_dialog_button)) { _, _ ->
            dismiss()
        }
        builder.setPositiveButton(getString(R.string.get_license)) { _, _ ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://scanbot.io/en/sdk.html"))
            activity?.startActivity(Intent.createChooser(intent, "Choose Browser"))
            dismiss()
        }

        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(true)

        return dialog
    }
}
