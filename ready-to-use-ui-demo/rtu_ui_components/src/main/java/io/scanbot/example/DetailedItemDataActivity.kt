package io.scanbot.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.barcodescanner.model.DEMedicalPlan.DEMedicalPlanDocument
import io.scanbot.barcodescanner.model.MedicalCertificate.MedicalCertificateDocument
import io.scanbot.barcodescanner.model.IDCardPDF417.IDCardPDF417Document
import io.scanbot.barcodescanner.model.SEPA.SEPADocument
import io.scanbot.barcodescanner.model.VCard.VCardDocument
import io.scanbot.barcodescanner.model.aamva.AAMVADocument
import io.scanbot.barcodescanner.model.boardingPass.BoardingPassDocument
import io.scanbot.barcodescanner.model.swissqr.SwissQRDocument
import io.scanbot.example.repository.BarcodeResultRepository
import io.scanbot.sdk.barcode.entity.BarcodeItem
import kotlinx.android.synthetic.main.activity_detailed_item_data.*
import kotlinx.android.synthetic.main.activity_detailed_item_data.view.*

class DetailedItemDataActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detailed_item_data)
        setSupportActionBar(toolbar)

        BarcodeResultRepository.selectedBarcodeItem?.let { item ->
            container?.also {
                it.image.setImageBitmap(item.image)
                it.barcodeFormat.text = item.barcodeFormat.name
                it.docFormat.text = item.formattedResult?.let { formattedResult ->
                    formattedResult::class.java.simpleName
                } ?: "Unknown document"
                it.description.text = printParsedFormat(item)
            }
        }
    }

    private fun printParsedFormat(item: BarcodeItem): String {
        val barcodeDocumentFormat = item.formattedResult
                ?: return "${item.textWithExtension}\n\nBinary data:\n${item.rawBytes.toHexString()}" // for not supported by current barcode detector implementation

        val barcodesResult = StringBuilder()
        when (barcodeDocumentFormat) {
            is AAMVADocument -> {
                barcodesResult.append("\n")
                        .append("AAMVA Document\n")
                        .append(barcodeDocumentFormat.AAMVAVersionNumber).append("\n")
                        .append(barcodeDocumentFormat.issuerIdentificationNumber).append("\n")
                        .append(barcodeDocumentFormat.jurisdictionVersionNumber).append("\n")
                barcodeDocumentFormat.subfiles.forEach { subfile ->
                    for (field in subfile.fields) {
                        barcodesResult.append(field.type.name).append(": ").append(field.value)
                                .append("\n")
                    }
                }
            }
            is BoardingPassDocument -> {
                barcodesResult.append("\n")
                        .append("Boarding Pass Document\n")
                        .append("${barcodeDocumentFormat.name}\n")
                barcodeDocumentFormat.legs?.forEach { leg ->
                    leg.fields.forEach {
                        barcodesResult.append("${it.type.name}:${it.value}\n")
                    }
                }
            }
            is DEMedicalPlanDocument -> {
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
            is MedicalCertificateDocument -> {
                barcodesResult.append("\nMedical Certificate Document\n")

                barcodeDocumentFormat.fields.forEach {
                    barcodesResult.append("${it.type?.name}: ${it.value}\n")
                }
            }
            is IDCardPDF417Document -> {
                barcodesResult.append("\nId Card PDF417\n")

                barcodeDocumentFormat.fields.forEach {
                    barcodesResult.append("${it.type?.name}: ${it.value}\n")
                }
            }
            is SEPADocument -> {
                barcodesResult.append("\nSEPA Document\n")

                barcodeDocumentFormat.fields.forEach {
                    barcodesResult.append("${it.type.name}: ${it.value}\n")
                }
            }
            is SwissQRDocument -> {
                barcodesResult.append("\nSwiss QR Document\n")
                barcodesResult.append("Version: ${barcodeDocumentFormat.version.name}\n")

                barcodeDocumentFormat.fields.forEach {
                    barcodesResult.append("${it.type.name}: ${it.value}\n")
                }
            }
            is VCardDocument -> {
                barcodesResult.append("\nVcard Document\n")

                barcodeDocumentFormat.fields.forEach {
                    barcodesResult.append("${it.type.name}: ${it.rawText}\n")
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
