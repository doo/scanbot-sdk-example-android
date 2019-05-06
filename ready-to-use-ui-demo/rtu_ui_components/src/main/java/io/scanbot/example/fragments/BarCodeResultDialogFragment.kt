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
import io.scanbot.example.R
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.barcode.entity.BarcodeScanningResult
import io.scanbot.sdk.persistence.Page
import io.scanbot.sdk.persistence.PageFileStorage
import io.scanbot.sdk.ui.entity.workflow.*
import kotlinx.android.synthetic.main.fragment_workflow_result_dialog.view.*
import java.io.File
import java.util.*


class BarCodeResultDialogFragment : androidx.fragment.app.DialogFragment() {

    companion object {
        const val NAME = "BarCodeResultDialogFragment"

        val WORKFLOW_EXTRA = "WORKFLOW_EXTRA"
        val WORKFLOW_RESULT_EXTRA = "WORKFLOW_RESULT_EXTRA"

        fun newInstance(workflow: Workflow, workflowStepResults: ArrayList<WorkflowStepResult>): BarCodeResultDialogFragment {
            val f = BarCodeResultDialogFragment()

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

        view.title.text = "QR-/Barcode + Document Image"

        val barcodeScanStepResult = workflowStepResults?.get(0) as BarCodeWorkflowStepResult
        if (barcodeScanStepResult.step is ScanBarCodeWorkflowStep) {
            view.findViewById<TextView>(R.id.tv_data).text = barcodeScanStepResult.barcodeResults.firstOrNull()?.let { extractData(it) }
        }

        val documentScanStepResult = workflowStepResults?.get(1)
        if (documentScanStepResult?.step is ScanDocumentPageWorkflowStep) {
            documentScanStepResult.capturedPage?.let {
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
                val barcodeScanStepResult = workflowStepResults?.get(0) as BarCodeWorkflowStepResult
                val barcodeScanningResult = barcodeScanStepResult.barcodeResults.firstOrNull()
                if (barcodeScanningResult != null && barcodeScanStepResult.step is ScanBarCodeWorkflowStep) {
                    val data = extractData(barcodeScanningResult)
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

    private fun extractData(result: BarcodeScanningResult): String {
        return StringBuilder()
                .append("QR-/Barcode Result:").append("\n")
                .append("Format: ").append(result.barcodeFormat.name).append("\n")
                .append("Value: ").append(result.text).append("\n")
                .toString()
    }
}