package io.scanbot.example

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.example.databinding.ActivityDetailedItemDataBinding
import io.scanbot.example.repository.BarcodeResultRepository
import io.scanbot.sdk.ui_v2.barcode.configuration.AAMVADocumentFormat
import io.scanbot.sdk.ui_v2.barcode.configuration.BoardingPassDocumentFormat
import io.scanbot.sdk.ui_v2.barcode.configuration.GS1DocumentFormat
import io.scanbot.sdk.ui_v2.barcode.configuration.IDCardPDF417DocumentFormat
import io.scanbot.sdk.ui_v2.barcode.configuration.MedicalCertificateDocumentFormat
import io.scanbot.sdk.ui_v2.barcode.configuration.MedicalPlanDocumentFormat
import io.scanbot.sdk.ui_v2.barcode.configuration.SEPADocumentFormat
import io.scanbot.sdk.ui_v2.barcode.configuration.SwissQRCodeDocumentFormat
import io.scanbot.sdk.ui_v2.barcode.configuration.VCardDocumentFormat

class DetailedItemDataActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityDetailedItemDataBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        BarcodeResultRepository.selectedBarcodeItem?.let { item ->
            binding.barcodeFormat.text = item.type.name
            binding.docFormat.text = item.formattedResult?.let { formattedResult ->
                formattedResult::class.java.simpleName
            } ?: "Unknown document"
            binding.description.text = printParsedFormat(item)
        }
    }

    private fun printParsedFormat(item: io.scanbot.sdk.ui_v2.barcode.configuration.BarcodeItem): String {
        val barcodeDocumentFormat = item.formattedResult
                ?: return "${item.textWithExtension}\n\nBinary data:\n${item.rawBytes.toByteArray().toHexString()}" // for not supported by current barcode detector implementation

        val barcodesResult = StringBuilder()
        when (barcodeDocumentFormat) {
            is AAMVADocumentFormat -> {
                barcodesResult.append("\n")
                        .append("AAMVA Document\n")
                        .append(barcodeDocumentFormat.aamvaVersionNumber).append("\n")
                        .append(barcodeDocumentFormat.issuerIdentificationNumber).append("\n")
                        .append(barcodeDocumentFormat.jurisdictionVersionNumber).append("\n")
                barcodeDocumentFormat.subfiles.forEach { subfile ->
                    for (field in subfile.fields) {
                        barcodesResult.append(field.type.name).append(": ").append(field.value)
                                .append("\n")
                    }
                }
            }
            is BoardingPassDocumentFormat -> {
                barcodesResult.append("\n")
                        .append("Boarding Pass Document\n")
                        .append("${barcodeDocumentFormat.name}\n")
                barcodeDocumentFormat.legs?.forEach { leg ->
                    leg.fields.forEach {
                        barcodesResult.append("${it.type.name}:${it.value}\n")
                    }
                }
            }
            is MedicalPlanDocumentFormat -> {
                barcodesResult.append("\nDE Medical Plan Document\n")

                barcodesResult.append("Doctor Fields:\n")
                barcodeDocumentFormat.doctor.fields.forEach {
                    barcodesResult.append("${it.type.name}: ${it.value}\n")
                }
                barcodesResult.append("\n")

                barcodesResult.append("Patient Fields:\n")
                barcodeDocumentFormat.patient.fields.forEach {
                    barcodesResult.append("${it.type.name}: ${it.value}\n")
                }
                barcodesResult.append("\n")

                barcodesResult.append("Medicine Fields:").append("\n")
                barcodeDocumentFormat.subheadings
                        .asSequence()
                        .flatMap {
                            barcodesResult.append("\n")
                            it.medicines.asSequence()
                        }
                        .flatMap { it.fields.asSequence() }
                        .forEach {
                            barcodesResult.append("${it.type.name}: ${it.value}\n")
                        }
            }
            is MedicalCertificateDocumentFormat -> {
                barcodesResult.append("\nMedical Certificate Document\n")

                barcodeDocumentFormat.fields.forEach {
                    barcodesResult.append("${it.type?.name}: ${it.value}\n")
                }
            }
            is IDCardPDF417DocumentFormat -> {
                barcodesResult.append("\nId Card PDF417\n")

                barcodeDocumentFormat.fields.forEach {
                    barcodesResult.append("${it.type?.name}: ${it.value}\n")
                }
            }
            is SEPADocumentFormat -> {
                barcodesResult.append("\nSEPA Document\n")

                barcodeDocumentFormat.fields.forEach {
                    barcodesResult.append("${it.type.name}: ${it.value}\n")
                }
            }
            is SwissQRCodeDocumentFormat -> {
                barcodesResult.append("\nSwiss QR Document\n")
                barcodesResult.append("Version: ${barcodeDocumentFormat.version.name}\n")

                barcodeDocumentFormat.fields.forEach {
                    barcodesResult.append("${it.type.name}: ${it.value}\n")
                }
            }
            is VCardDocumentFormat -> {
                barcodesResult.append("\nVcard Document\n")

                barcodeDocumentFormat.fields.forEach {
                    barcodesResult.append("${it.type.name}: ${it.rawText}\n")
                }
            }
            is GS1DocumentFormat -> {
                barcodesResult.append("\nGs1 Document\n")

                barcodeDocumentFormat.fields.forEach { field ->
                    barcodesResult.append("${field.dataTitle}: ${field.rawValue}\n")
                }
            }
        }
        return barcodesResult.toString()
    }

    private fun ByteArray.toHexString() = this.joinToString("") { String.format("%02X", it.toInt() and 0xFF) }

    companion object {
        const val BARCODE_ITEM = "BARCODE_ITEM"
    }
}
