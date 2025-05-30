package io.scanbot.example.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import io.scanbot.example.R
import io.scanbot.example.databinding.FragmentMedicalCertificateResultDialogBinding
import io.scanbot.sdk.core.ImageRef
import io.scanbot.sdk.mc.*

class MedicalCertificateResultDialogFragment : androidx.fragment.app.DialogFragment() {

    companion object {

        const val NAME = "MedicalCertificateResultDialogFragment"

        const val MEDICAL_CERTIFICATE_RESULT_EXTRA = "MEDICAL_CERTIFICATE_RESULT_EXTRA"

        fun newInstance(medicalCertificateScanResult: MedicalCertificateScanningResult): MedicalCertificateResultDialogFragment {
            val f = MedicalCertificateResultDialogFragment()

            // Supply num input as an argument.
            val args = Bundle()
            args.putParcelable(MEDICAL_CERTIFICATE_RESULT_EXTRA, medicalCertificateScanResult)
            f.arguments = args

            return f
        }
    }

    private var _binding: FragmentMedicalCertificateResultDialogBinding? = null
    private val binding get() = _binding!!

    private var medicalCertificateResult: MedicalCertificateScanningResult? = null

    private fun addContentView(inflater: LayoutInflater, container: ViewGroup?): View {
        medicalCertificateResult = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(MEDICAL_CERTIFICATE_RESULT_EXTRA, MedicalCertificateScanningResult::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(MEDICAL_CERTIFICATE_RESULT_EXTRA)
        }

        _binding = FragmentMedicalCertificateResultDialogBinding.inflate(inflater, container, true)

        binding.title.text = getString(R.string.mrz_result_caption)

        medicalCertificateResult?.let { result ->
            binding.tvData.text = extractData(result)
            binding.imagesContainer.visibility = View.VISIBLE
            result.croppedImage?.let { showBitmapImage(it, binding.frontSnapResult) }
        }

        return binding.root
    }

    private fun showBitmapImage(mcImage: ImageRef, imageView: ImageView) {
        imageView.visibility = View.VISIBLE
        imageView.setImageBitmap(mcImage.toBitmap())
    }


    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())

        val inflater = LayoutInflater.from(activity)

        val contentContainer = inflater.inflate(R.layout.holo_dialog_frame, null, false) as ViewGroup
        addContentView(inflater, contentContainer)

        builder.setView(contentContainer)


        builder.setPositiveButton(getString(R.string.cancel_dialog_button)) { _, _ ->
            run {
                dismiss()
            }
        }

        builder.setNegativeButton(R.string.copy_dialog_button) { _, _ ->
            run {
                val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val data = medicalCertificateResult?.let { extractData(it) }


                val clip = ClipData.newPlainText(data, data)

                clipboard.setPrimaryClip(clip)
                dismiss()
            }
        }
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(true)

        return dialog
    }

    private fun extractData(result: MedicalCertificateScanningResult): String {
        return StringBuilder()
                .append("Type: ").append(if (result.checkBoxes
                    .find { medicalCertificateInfoBox -> medicalCertificateInfoBox.type == MedicalCertificateCheckBoxType.INITIAL_CERTIFICATE }
                    ?.checked == true)
                    "Initial"
                else if (result.checkBoxes
                    .find { medicalCertificateInfoBox -> medicalCertificateInfoBox.type == MedicalCertificateCheckBoxType.RENEWED_CERTIFICATE }
                                ?.checked == true)
                    "Renewed"
                else
                    "Unknown").append("\n")
                .append("Work Accident: ").append(if (result.checkBoxes
                    .find { medicalCertificateInfoBox -> medicalCertificateInfoBox.type == MedicalCertificateCheckBoxType.WORK_ACCIDENT }
                                ?.checked == true)
                    "Yes"
                else "No").append("\n")
                .append("Accident Consultant: ").append(
                        if (result.checkBoxes
                                .find { medicalCertificateInfoBox -> medicalCertificateInfoBox.type == MedicalCertificateCheckBoxType.ASSIGNED_TO_ACCIDENT_INSURANCE_DOCTOR }
                                        ?.checked == true)
                            "Yes"
                        else "No"
                ).append("\n")
                .append("Start Date: ").append(
                        result.dates.find { dateRecord -> dateRecord.type == MedicalCertificateDateRecordType.INCAPABLE_OF_WORK_SINCE }?.value
                ).append("\n")
                .append("End Date: ").append(
                        result.dates.find { dateRecord -> dateRecord.type == MedicalCertificateDateRecordType.INCAPABLE_OF_WORK_UNTIL }?.value
                ).append("\n")
                .append("Issue Date: ").append(
                        result.dates.find { dateRecord -> dateRecord.type == MedicalCertificateDateRecordType.DIAGNOSED_ON }?.value
                )
                .append("\n")
                .append("Form type: ${result.formType.name}")
                .append("\n")
                .append(result.patientInfoBox.fields.joinToString(separator = "\n", prefix = "\n") { "${it.type.name}: ${it.value}" })
                .toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
