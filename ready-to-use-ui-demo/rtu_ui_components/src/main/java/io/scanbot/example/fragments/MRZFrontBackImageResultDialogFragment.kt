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
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import io.scanbot.example.R
import io.scanbot.mrzscanner.model.MRZRecognitionResult
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.persistence.PageFileStorage
import io.scanbot.sdk.ui.entity.workflow.*
import kotlinx.android.synthetic.main.fragment_workflow_result_dialog.view.*
import java.io.File
import java.util.*


class MRZFrontBackImageResultDialogFragment : androidx.fragment.app.DialogFragment() {

    companion object {
        const val NAME = "MRZFrontBackImageResultDialogFragment"

        val WORKFLOW_EXTRA = "WORKFLOW_EXTRA"
        val WORKFLOW_RESULT_EXTRA = "WORKFLOW_RESULT_EXTRA"

        fun newInstance(workflow: Workflow, workflowStepResults: ArrayList<WorkflowStepResult>): MRZFrontBackImageResultDialogFragment {
            val f = MRZFrontBackImageResultDialogFragment()

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

        view.title.text = "Result"

        val frontScanStepResult = workflowStepResults?.get(0) as ContourDetectorWorkflowStepResult
        val backScanStepResult = workflowStepResults?.get(1) as MachineReadableZoneWorkflowStepResult
        if (frontScanStepResult.step is ScanDocumentPageWorkflowStep) {
            val pageFileStorage = ScanbotSDK(context!!.applicationContext).pageFileStorage()
            frontScanStepResult.capturedPage?.let {
                view.images_container.visibility = View.VISIBLE
                view.front_snap_result.visibility = View.VISIBLE
                val imagePath = pageFileStorage.getPreviewImageURI(it.pageId, PageFileStorage.PageFileType.DOCUMENT).path
                val originalImagePath = pageFileStorage.getPreviewImageURI(it.pageId, PageFileStorage.PageFileType.ORIGINAL).path
                val fileToShow = if (File(imagePath).exists()) File(imagePath) else File(originalImagePath)
                Picasso.with(context)
                        .load(fileToShow)
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .resizeDimen(R.dimen.move_preview_size, R.dimen.move_preview_size)
                        .centerInside()
                        .into(view.front_snap_result)
            }
        }
        if (backScanStepResult.step is ScanMachineReadableZoneWorkflowStep) {
            view.findViewById<TextView>(R.id.tv_data).text = backScanStepResult.mrzResult?.let { extractData(it) }

            val pageFileStorage = ScanbotSDK(context!!.applicationContext).pageFileStorage()
            backScanStepResult.capturedPage?.let {
                view.images_container.visibility = View.VISIBLE
                view.back_snap_result.visibility = View.VISIBLE
                val imagePath = pageFileStorage.getPreviewImageURI(it.pageId, PageFileStorage.PageFileType.DOCUMENT).path
                val originalImagePath = pageFileStorage.getPreviewImageURI(it.pageId, PageFileStorage.PageFileType.ORIGINAL).path
                val fileToShow = if (File(imagePath).exists()) File(imagePath) else File(originalImagePath)
                Picasso.with(context)
                        .load(fileToShow)
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .resizeDimen(R.dimen.move_preview_size, R.dimen.move_preview_size)
                        .centerInside()
                        .into(view.back_snap_result)
            }
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
                val mrzScanStepResult = workflowStepResults?.get(0) as MachineReadableZoneWorkflowStepResult
                if (mrzScanStepResult.mrzResult != null && mrzScanStepResult.step is ScanMachineReadableZoneWorkflowStep) {
                    val data = extractData(mrzScanStepResult.mrzResult!!)

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

    private fun extractData(result: MRZRecognitionResult): String {
        return StringBuilder()
                .append(getString(R.string.mrz_document_type)).append(" ").append(result.travelDocType?.name).append("\n")
                .append(getString(R.string.mrz_document_country)).append(" ").append(result.nationalityField()?.value).append("\n")
                .append(getString(R.string.mrz_last_name)).append(" ").append(result.lastNameField().value).append("\n")
                .append(getString(R.string.mrz_first_name)).append(" ").append(result.firstNameField().value).append("\n")
                .append(getString(R.string.mrz_document_code)).append(" ").append(result.documentCodeField().value).append("\n")
                .append(getString(R.string.mrz_dob)).append(" ").append(result.dateOfBirthField().value).append("\n")
                .append(getString(R.string.mrz_gender)).append(" ").append(result.genderField().value).append("\n")
                .append(getString(R.string.mrz_checksums)).append(" ").append(if (result.checkDigitsCount == result.validCheckDigitsCount) "Valid" else "Invalid").append("\n")
                .toString()
    }
}