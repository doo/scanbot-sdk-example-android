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
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import io.scanbot.dcscanner.model.DCInfoBoxSubtype
import io.scanbot.dcscanner.model.DateRecordType
import io.scanbot.dcscanner.model.DisabilityCertificateRecognizerResultInfo
import io.scanbot.example.R
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.persistence.Page
import io.scanbot.sdk.persistence.PageFileStorage
import io.scanbot.sdk.ui.entity.workflow.DisabilityCertificateWorkflowStepResult
import io.scanbot.sdk.ui.entity.workflow.ScanDisabilityCertificateWorkflowStep
import io.scanbot.sdk.ui.entity.workflow.Workflow
import io.scanbot.sdk.ui.entity.workflow.WorkflowStepResult
import kotlinx.android.synthetic.main.fragment_workflow_result_dialog.view.*
import java.io.File
import java.util.*


class DCResultDialogFragment : androidx.fragment.app.DialogFragment() {

    companion object {
        const val NAME = "DCResultDialogFragment"

        val WORKFLOW_EXTRA = "WORKFLOW_EXTRA"
        val WORKFLOW_RESULT_EXTRA = "WORKFLOW_RESULT_EXTRA"

        fun newInstance(workflow: Workflow, workflowStepResults: ArrayList<WorkflowStepResult>): DCResultDialogFragment {
            val f = DCResultDialogFragment()

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

        view.title.text = "Detected DC Form"

        val dcScanStepResult = workflowStepResults?.get(0) as DisabilityCertificateWorkflowStepResult
        if (dcScanStepResult.step is ScanDisabilityCertificateWorkflowStep) {
            view.findViewById<TextView>(R.id.tv_data).text = dcScanStepResult.disabilityCertificateResult?.let { extractData(it) }

            dcScanStepResult.capturedPage?.let {
                view.images_container.visibility = View.VISIBLE
                showPageImage(it, view.front_snap_result)
            }
        }

        return view
    }

    private fun showPageImage(page: Page, imageView: ImageView) {
        val pageFileStorage = ScanbotSDK(context!!.applicationContext).pageFileStorage()
        imageView.visibility = View.VISIBLE
        val docImageFile = File(pageFileStorage.getPreviewImageURI(page.pageId, PageFileStorage.PageFileType.DOCUMENT).path)
        val origImageFile = File(pageFileStorage.getPreviewImageURI(page.pageId, PageFileStorage.PageFileType.ORIGINAL).path)
        val fileToShow = if (docImageFile.exists()) docImageFile else origImageFile
        Picasso.with(context)
                .load(fileToShow)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .resizeDimen(R.dimen.move_preview_size, R.dimen.move_preview_size)
                .centerInside()
                .into(imageView)
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
                val dcScanStepResult = workflowStepResults?.get(0) as DisabilityCertificateWorkflowStepResult
                if (dcScanStepResult.disabilityCertificateResult != null && dcScanStepResult.step is ScanDisabilityCertificateWorkflowStep) {
                    val data = extractData(dcScanStepResult.disabilityCertificateResult!!)

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

    private fun extractData(result: DisabilityCertificateRecognizerResultInfo): String {
        return StringBuilder()
                .append("Type: ").append(if (result.checkboxes
                                ?.find { disabilityCertificateInfoBox -> disabilityCertificateInfoBox.subType == DCInfoBoxSubtype.DCBoxInitialCertificate }
                                ?.hasContents == true)
                    "Initial"
                else if (result.checkboxes
                                ?.find { disabilityCertificateInfoBox -> disabilityCertificateInfoBox.subType == DCInfoBoxSubtype.DCBoxRenewedCertificate }
                                ?.hasContents == true)
                    "Renewed"
                else
                    "Unknown").append("\n")
                .append("Work Accident: ").append(if (result.checkboxes
                                ?.find { disabilityCertificateInfoBox -> disabilityCertificateInfoBox.subType == DCInfoBoxSubtype.DCBoxWorkAccident }
                                ?.hasContents == true)
                    "Yes"
                else "No").append("\n")
                .append("Accident Consultant: ").append(
                        if (result.checkboxes
                                        ?.find { disabilityCertificateInfoBox -> disabilityCertificateInfoBox.subType == DCInfoBoxSubtype.DCBoxAssignedToAccidentInsuranceDoctor }
                                        ?.hasContents == true)
                            "Yes"
                        else "No"
                ).append("\n")
                .append("Start Date: ").append(
                        result.dates?.find { dateRecord -> dateRecord.type == DateRecordType.DateRecordIncapableOfWorkSince }?.dateString
                ).append("\n")
                .append("End Date: ").append(
                        result.dates?.find { dateRecord -> dateRecord.type == DateRecordType.DateRecordIncapableOfWorkUntil }?.dateString
                ).append("\n")
                .append("Issue Date: ").append(
                        result.dates?.find { dateRecord -> dateRecord.type == DateRecordType.DateRecordDiagnosedOn }?.dateString
                )
                .append("\n")
                .append("Form type: ${result.dcFormType.name}")
                .append("\n")
                .append(result.patientInfoFields.joinToString(separator = "\n", prefix = "\n") { "${it.patientInfoFieldType.name}: ${it.value}" })
                .toString()
    }
}