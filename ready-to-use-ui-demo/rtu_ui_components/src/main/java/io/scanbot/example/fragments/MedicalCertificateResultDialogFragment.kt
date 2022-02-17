package io.scanbot.example.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.squareup.picasso.MemoryPolicy
import io.scanbot.example.R
import io.scanbot.example.di.ExampleSingletonImpl
import io.scanbot.example.util.PicassoHelper
import io.scanbot.mcscanner.model.DateRecordType
import io.scanbot.mcscanner.model.McInfoBoxSubtype
import io.scanbot.sdk.mcrecognizer.entity.MedicalCertificateRecognizerResult
import io.scanbot.sdk.persistence.Page
import io.scanbot.sdk.persistence.PageFileStorage
import io.scanbot.sdk.ui.entity.workflow.DisabilityCertificateWorkflowStepResult
import io.scanbot.sdk.ui.entity.workflow.ScanDisabilityCertificateWorkflowStep
import io.scanbot.sdk.ui.entity.workflow.Workflow
import io.scanbot.sdk.ui.entity.workflow.WorkflowStepResult
import kotlinx.android.synthetic.main.fragment_workflow_result_dialog.view.*
import java.io.File

class MedicalCertificateResultDialogFragment : androidx.fragment.app.DialogFragment() {

    companion object {
        const val NAME = "MedicalCertificateResultDialogFragment"

        const val WORKFLOW_EXTRA = "WORKFLOW_EXTRA"
        const val WORKFLOW_RESULT_EXTRA = "WORKFLOW_RESULT_EXTRA"

        const val MEDICAL_CERTIFICATE_RESULT_EXTRA = "MEDICAL_CERTIFICATE_RESULT_EXTRA"

        fun newInstance(workflow: Workflow, workflowStepResults: List<WorkflowStepResult>): MedicalCertificateResultDialogFragment {
            val f = MedicalCertificateResultDialogFragment()

            // Supply num input as an argument.
            val args = Bundle()
            args.putParcelable(WORKFLOW_EXTRA, workflow)
            args.putParcelableArrayList(WORKFLOW_RESULT_EXTRA, ArrayList(workflowStepResults))
            f.arguments = args

            return f
        }

        fun newInstance(medicalCertificateScanResult: MedicalCertificateRecognizerResult): MedicalCertificateResultDialogFragment {
            val f = MedicalCertificateResultDialogFragment()

            // Supply num input as an argument.
            val args = Bundle()
            args.putParcelable(MEDICAL_CERTIFICATE_RESULT_EXTRA, medicalCertificateScanResult)
            f.arguments = args

            return f
        }
    }

    private var workflow: Workflow? = null
    private var workflowStepResults: List<WorkflowStepResult>? = null

    private var medicalCertificateResult: MedicalCertificateRecognizerResult? = null

    private fun addContentView(inflater: LayoutInflater, container: ViewGroup?): View? {
        workflow = arguments?.getParcelable(WORKFLOW_EXTRA)
        workflowStepResults = arguments?.getParcelableArrayList(WORKFLOW_RESULT_EXTRA)

        medicalCertificateResult = arguments?.getParcelable(MEDICAL_CERTIFICATE_RESULT_EXTRA)

        val view = inflater.inflate(R.layout.fragment_workflow_result_dialog, container)

        view.title.text = "Detected Medical Certificate Form"

        if (workflow != null) {
            val medicalCertificateScanResult =
                workflowStepResults?.get(0) as DisabilityCertificateWorkflowStepResult
            if (medicalCertificateScanResult.step is ScanDisabilityCertificateWorkflowStep) {
                view.findViewById<TextView>(R.id.tv_data).text =
                    medicalCertificateScanResult.medicalCertificateResult?.let { extractData(it) }

                medicalCertificateScanResult.capturedPage?.let {
                    view.images_container.visibility = View.VISIBLE
                    showPageImage(it, view.front_snap_result)
                }
            }
        } else {
            medicalCertificateResult?.let { result ->
                view.findViewById<TextView>(R.id.tv_data).text = extractData(result)
                view.images_container.visibility = View.VISIBLE
                result.croppedImage?.let { showBitmapImage(it, view.front_snap_result) }
            }
        }

        return view
    }

    private fun showPageImage(page: Page, imageView: ImageView) {
        val context = requireContext().applicationContext
        val pageFileStorage = ExampleSingletonImpl(requireContext()).pageFileStorageInstance()
        imageView.visibility = View.VISIBLE
        val docImageFile = File(pageFileStorage.getPreviewImageURI(page.pageId, PageFileStorage.PageFileType.DOCUMENT).path)
        val origImageFile = File(pageFileStorage.getPreviewImageURI(page.pageId, PageFileStorage.PageFileType.ORIGINAL).path)
        val fileToShow = if (docImageFile.exists()) docImageFile else origImageFile
        PicassoHelper.with(context)
                .load(fileToShow)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .resizeDimen(R.dimen.move_preview_size, R.dimen.move_preview_size)
                .centerInside()
                .into(imageView)
    }

    private fun showBitmapImage(mcImage: Bitmap, imageView: ImageView) {
        imageView.visibility = View.VISIBLE
        imageView.setImageBitmap(mcImage)
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
                val data = medicalCertificateResult?.let { extractData(it) } ?: workflowStepResults?.let {
                    val mcScanStepResult = it[0] as DisabilityCertificateWorkflowStepResult
                    if (mcScanStepResult.medicalCertificateResult != null && mcScanStepResult.step is ScanDisabilityCertificateWorkflowStep) {
                        extractData(mcScanStepResult.medicalCertificateResult!!)
                    } else {
                        medicalCertificateResult?.let { extractData(it) }
                    }
                }


                val clip = ClipData.newPlainText(data, data)

                clipboard.setPrimaryClip(clip)
                dismiss()
            }
        }
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(true)

        return dialog
    }

    private fun extractData(result: MedicalCertificateRecognizerResult): String {
        return StringBuilder()
                .append("Type: ").append(if (result.checkboxes
                                ?.find { medicalCertificateInfoBox -> medicalCertificateInfoBox.subType == McInfoBoxSubtype.McBoxInitialCertificate }
                                ?.hasContents == true)
                    "Initial"
                else if (result.checkboxes
                                ?.find { medicalCertificateInfoBox -> medicalCertificateInfoBox.subType == McInfoBoxSubtype.McBoxRenewedCertificate }
                                ?.hasContents == true)
                    "Renewed"
                else
                    "Unknown").append("\n")
                .append("Work Accident: ").append(if (result.checkboxes
                                ?.find { medicalCertificateInfoBox -> medicalCertificateInfoBox.subType == McInfoBoxSubtype.McBoxWorkAccident }
                                ?.hasContents == true)
                    "Yes"
                else "No").append("\n")
                .append("Accident Consultant: ").append(
                        if (result.checkboxes
                                        ?.find { medicalCertificateInfoBox -> medicalCertificateInfoBox.subType == McInfoBoxSubtype.McBoxAssignedToAccidentInsuranceDoctor }
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
                .append("Form type: ${result.mcFormType.name}")
                .append("\n")
                .append(result.patientInfoFields.joinToString(separator = "\n", prefix = "\n") { "${it.patientInfoFieldType.name}: ${it.value}" })
                .toString()
    }
}