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
import io.scanbot.mcscanner.model.CheckBoxType
import io.scanbot.mcscanner.model.DateRecordType
import io.scanbot.sdk.mcrecognizer.entity.MedicalCertificateRecognizerResult

class MedicalCertificateResultDialogFragment : androidx.fragment.app.DialogFragment() {

    companion object {

        const val NAME = "MedicalCertificateResultDialogFragment"

        const val MEDICAL_CERTIFICATE_RESULT_EXTRA = "MEDICAL_CERTIFICATE_RESULT_EXTRA"

        fun newInstance(medicalCertificateScanResult: MedicalCertificateRecognizerResult): MedicalCertificateResultDialogFragment {
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

    private var medicalCertificateResult: MedicalCertificateRecognizerResult? = null

    private fun addContentView(inflater: LayoutInflater, container: ViewGroup?): View {
        medicalCertificateResult = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(MEDICAL_CERTIFICATE_RESULT_EXTRA, MedicalCertificateRecognizerResult::class.java)
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

    private fun extractData(result: MedicalCertificateRecognizerResult): String {
        return StringBuilder()
                .append("Type: ").append(if (result.checkboxes
                    .find { medicalCertificateInfoBox -> medicalCertificateInfoBox.type == CheckBoxType.McBoxInitialCertificate }
                                ?.hasContents == true)
                    "Initial"
                else if (result.checkboxes
                    .find { medicalCertificateInfoBox -> medicalCertificateInfoBox.type == CheckBoxType.McBoxRenewedCertificate }
                                ?.hasContents == true)
                    "Renewed"
                else
                    "Unknown").append("\n")
                .append("Work Accident: ").append(if (result.checkboxes
                    .find { medicalCertificateInfoBox -> medicalCertificateInfoBox.type == CheckBoxType.McBoxWorkAccident }
                                ?.hasContents == true)
                    "Yes"
                else "No").append("\n")
                .append("Accident Consultant: ").append(
                        if (result.checkboxes
                                .find { medicalCertificateInfoBox -> medicalCertificateInfoBox.type == CheckBoxType.McBoxAssignedToAccidentInsuranceDoctor }
                                        ?.hasContents == true)
                            "Yes"
                        else "No"
                ).append("\n")
                .append("Start Date: ").append(
                        result.dates.find { dateRecord -> dateRecord.type == DateRecordType.DateRecordIncapableOfWorkSince }?.dateString
                ).append("\n")
                .append("End Date: ").append(
                        result.dates.find { dateRecord -> dateRecord.type == DateRecordType.DateRecordIncapableOfWorkUntil }?.dateString
                ).append("\n")
                .append("Issue Date: ").append(
                        result.dates.find { dateRecord -> dateRecord.type == DateRecordType.DateRecordDiagnosedOn }?.dateString
                )
                .append("\n")
                .append("Form type: ${result.mcFormType.name}")
                .append("\n")
                .append(result.patientInfoBox.fields.joinToString(separator = "\n", prefix = "\n") { "${it.patientInfoFieldType.name}: ${it.value}" })
                .toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
