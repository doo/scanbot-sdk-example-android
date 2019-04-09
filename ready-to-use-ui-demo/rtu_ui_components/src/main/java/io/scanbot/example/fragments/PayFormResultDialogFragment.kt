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
import io.scanbot.payformscanner.model.PayFormRecognitionResult
import io.scanbot.sdk.ui.entity.workflow.*
import kotlinx.android.synthetic.main.fragment_workflow_result_dialog.view.*
import java.util.ArrayList


class PayFormResultDialogFragment : androidx.fragment.app.DialogFragment() {

    companion object {
        const val NAME = "PayFormResultDialogFragment"

        val WORKFLOW_EXTRA = "WORKFLOW_EXTRA"
        val WORKFLOW_RESULT_EXTRA = "WORKFLOW_RESULT_EXTRA"

        fun newInstance(workflow: Workflow, workflowStepResults: ArrayList<WorkflowStepResult>): PayFormResultDialogFragment {
            val f = PayFormResultDialogFragment()

            // Supply num input as an argument.
            val args = Bundle()
            args.putParcelable(WORKFLOW_EXTRA, workflow)
            args.putParcelableArrayList(WORKFLOW_RESULT_EXTRA, workflowStepResults)
            f.arguments = args

            return f
        }
    }

    private var workflow: Workflow? = null
    private var workflowStepResults: List<WorkflowStepResult>? = null

    private fun addContentView(inflater: LayoutInflater, container: ViewGroup?): View? {
        workflow = arguments?.getParcelable(WORKFLOW_EXTRA)
        workflowStepResults = arguments?.getParcelableArrayList(WORKFLOW_RESULT_EXTRA)

        val view = inflater.inflate(R.layout.fragment_workflow_result_dialog, container)

        view.title.text = "Detected SEPA Pay Form"

        val payFormScanStepResult = workflowStepResults?.get(0)
        if (payFormScanStepResult?.step is ScanPayFormWorkflowStep) {
            view.findViewById<TextView>(R.id.tv_data).text = payFormScanStepResult?.payformResult?.let { extractData(it) }
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
                val payFormScanStepResult = workflowStepResults?.get(0)
                if (payFormScanStepResult?.payformResult != null && payFormScanStepResult?.step is ScanPayFormWorkflowStep) {
                    val data = extractData(payFormScanStepResult.payformResult!!)

                    val clip = ClipData.newPlainText(data, data)

                    clipboard.primaryClip = clip
                }
                dismiss()
            }
        }
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(true)

        return dialog
    }

    private fun extractData(result: PayFormRecognitionResult): String? {
        return result.payformFields
                ?.map { recognizedField -> recognizedField.tokenType.name + ": " + recognizedField.value + "\n" }
                ?.reduce {str, itemStr -> str + itemStr}
    }
}