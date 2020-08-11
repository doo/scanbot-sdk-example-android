package io.scanbot.example.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.barcodescanner.model.DEMedicalPlan.DEMedicalPlanDocument
import io.scanbot.barcodescanner.model.DisabilityCertificate.DisabilityCertificateDocument
import io.scanbot.barcodescanner.model.SEPA.SEPADocument
import io.scanbot.barcodescanner.model.VCard.VCardDocument
import io.scanbot.barcodescanner.model.boardingPass.BoardingPassDocument
import io.scanbot.example.R
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
                it.docFormat.text = item.formattedResult?.documentFormat
                it.description.text = printParsedFormat(item)
            }
        }
    }

    private fun printParsedFormat(item: BarcodeItem): String {
        val barcodeDocumentFormat = item.formattedResult
            ?: return "${item.text}\n\nBinary data:\n${item.rawBytes.toHexString()}" // for not supported by current barcode detector implementation

        val barcodesResult = StringBuilder()
        when (barcodeDocumentFormat) {
            is BoardingPassDocument -> {
                barcodesResult.append("\n")
                    .append("Boarding Pass Document").append("\n")
                    .append(barcodeDocumentFormat.name).append("\n")
                barcodeDocumentFormat.legs?.forEach { leg ->
                    for (field in leg.fields) {
                        barcodesResult.append(field.type?.name).append(": ").append(field.value)
                                .append("\n")
                    }
                }
            }
            is DEMedicalPlanDocument -> {
                barcodesResult.append("\n").append("DE Medical Plan Document").append("\n")

                barcodesResult.append("Doctor Fields:").append("\n")
                barcodeDocumentFormat.doctor?.fields?.forEach { field ->
                    barcodesResult.append(field.type?.name).append(": ").append(field.value)
                        .append("\n")
                }
                barcodesResult.append("\n")

                barcodesResult.append("Patient Fields:").append("\n")
                barcodeDocumentFormat.patient?.fields?.forEach { field ->
                    barcodesResult.append(field.type?.name).append(": ").append(field.value)
                        .append("\n")
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
                        barcodesResult.append(it.type?.name).append(": ").append(it.value)
                            .append("\n")
                    }
            }
            is DisabilityCertificateDocument -> {
                barcodesResult.append("\n").append("Disability Certificate Document").append("\n")

                barcodeDocumentFormat.fields.forEach {
                    barcodesResult.append(it.type?.name).append(": ").append(it.value).append("\n")
                }
            }
            is SEPADocument -> {
                barcodesResult.append("\n").append("Sepa Document").append("\n")

                barcodeDocumentFormat.fields.forEach {
                    barcodesResult.append(it.type.name).append(": ").append(it.value).append("\n")
                }
            }

            is VCardDocument -> {
                barcodesResult.append("\n").append("Vcard Document").append("\n")

                barcodeDocumentFormat.fields.forEach {
                    barcodesResult.append(it.type.name).append(": ").append(it.rawText).append("\n")
                }
            }
        }
        return barcodesResult.toString()
    }

    private fun ByteArray.toHexString() = this.joinToString("") { String.format("%02X", (it.toInt() and 0xFF)) }

    companion object {
        const val BARCODE_ITEM = "BARCODE_ITEM"
    }
}
