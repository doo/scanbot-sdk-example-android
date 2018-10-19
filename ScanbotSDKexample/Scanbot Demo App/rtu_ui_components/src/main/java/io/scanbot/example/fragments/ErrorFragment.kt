package io.scanbot.example.fragments

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.scanbot.example.R


class ErrorFragment : DialogFragment() {

    companion object {
        const val NAME = "ErrorFragment"

        @JvmStatic
        fun newInstanse(): ErrorFragment {
            return ErrorFragment()
        }
    }

    private fun addContentView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_expired_license_dialog, container)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(this.activity!!)

        val inflater = LayoutInflater.from(activity)

        val contentContainer = inflater.inflate(R.layout.holo_dialog_frame, null, false) as ViewGroup
        addContentView(inflater, contentContainer, savedInstanceState)

        builder.setView(contentContainer)

        builder.setNegativeButton(
                "close") { _, _ ->
            run {
                dismiss()
            }
        }
        builder.setPositiveButton(
                "get license") { _, _ ->
            run {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://scanbot.io/en/sdk.html"))
                activity?.startActivity(Intent.createChooser(intent, "Choose Browser"))
                dismiss()
            }
        }

        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(true)

        return dialog
    }
}