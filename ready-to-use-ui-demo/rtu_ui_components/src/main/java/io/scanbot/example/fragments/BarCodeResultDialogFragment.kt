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
import io.scanbot.example.R
import io.scanbot.example.di.ExampleSingletonImpl
import io.scanbot.example.util.PicassoHelper
import io.scanbot.sdk.barcode.entity.BarcodeItem
import io.scanbot.sdk.persistence.Page
import io.scanbot.sdk.persistence.PageFileStorage
import io.scanbot.sdk.ui.entity.workflow.*
import kotlinx.android.synthetic.main.fragment_workflow_result_dialog.view.*
import java.io.File


class BarCodeResultDialogFragment : androidx.fragment.app.DialogFragment() {
    companion object {
        const val NAME = "BarCodeResultDialogFragment"

        const val WORKFLOW_EXTRA = "WORKFLOW_EXTRA"
        const val WORKFLOW_RESULT_EXTRA = "WORKFLOW_RESULT_EXTRA"

        fun newInstance(workflow: Workflow, workflowStepResults: List<WorkflowStepResult>): BarCodeResultDialogFragment {
            val f = BarCodeResultDialogFragment()

            // Supply num input as an argument.
            val args = Bundle()
            args.putParcelable(WORKFLOW_EXTRA, workflow)
            args.putParcelableArrayList(WORKFLOW_RESULT_EXTRA, ArrayList(workflowStepResults))
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
            view.findViewById<TextView>(R.id.tv_data).text = barcodeScanStepResult.barcodeResults?.barcodeItems?.firstOrNull()?.let { extractData(it) }
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
        val context = requireContext().applicationContext
        val pageFileStorage = ExampleSingletonImpl(context).pageFileStorageInstance()
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
                val barcodeScanStepResult = workflowStepResults?.get(0) as BarCodeWorkflowStepResult
                val barcodeItem = barcodeScanStepResult.barcodeResults?.barcodeItems?.firstOrNull()
                if (barcodeItem != null && barcodeScanStepResult.step is ScanBarCodeWorkflowStep) {
                    val data = extractData(barcodeItem)
                    val clip = ClipData.newPlainText(data, data)
                    clipboard.setPrimaryClip(clip)
                }
                dismiss()
            }
        }
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(true)

        return dialog
    }

    private fun extractData(barcodeItem: BarcodeItem): String {
        return StringBuilder()
                .append("QR-/Barcode Result:").append("\n")
                .append("Format: ").append(barcodeItem.barcodeFormat.name).append("\n")
                .append("Value: ").append(barcodeItem.text).append("\n")
                .toString()
    }
}