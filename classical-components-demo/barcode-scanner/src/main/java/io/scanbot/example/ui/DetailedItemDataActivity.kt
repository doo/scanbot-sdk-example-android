package io.scanbot.example.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.barcodescanner.entity.AAMVA
import io.scanbot.barcodescanner.entity.BarcodeDocumentLibrary.wrap
import io.scanbot.barcodescanner.entity.BoardingPass
import io.scanbot.barcodescanner.entity.DEMedicalPlan
import io.scanbot.barcodescanner.entity.IDCardPDF417
import io.scanbot.barcodescanner.entity.MedicalCertificate
import io.scanbot.barcodescanner.entity.SEPA
import io.scanbot.barcodescanner.entity.SwissQR
import io.scanbot.barcodescanner.entity.VCard
import io.scanbot.example.databinding.ActivityDetailedItemDataBinding
import io.scanbot.example.repository.BarcodeResultRepository
import io.scanbot.sdk.barcode.entity.BarcodeItem

class DetailedItemDataActivity : AppCompatActivity() {

    private val binding: ActivityDetailedItemDataBinding by lazy {
        ActivityDetailedItemDataBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        BarcodeResultRepository.selectedBarcodeItem?.let { item ->
            binding.container?.also {
                binding.image.setImageBitmap(item.image)
                binding.barcodeFormat.text = item.barcodeFormat.name
                binding.docFormat.text = item.formattedResult?.let {
                    it::class.java.simpleName
                } ?: ""
                binding.description.text = printParsedFormat(item)
            }
        }
    }

    private fun printParsedFormat(item: BarcodeItem): String {
        val barcodeDocumentFormat = item.formattedResult?.wrap()
            ?: return "${item.textWithExtension}\n\nBinary data:\n${item.rawBytes.toHexString()}" // for not supported by current barcode detector implementation

        val barcodesResult = StringBuilder()
        when (barcodeDocumentFormat) {
            is AAMVA -> {
                barcodesResult.append("\n")
                        .append("AAMVA Document\n")
                        .append(barcodeDocumentFormat.version).append("\n")
                        .append(barcodeDocumentFormat.issuerIdentificationNumber).append("\n")
                        .append(barcodeDocumentFormat.jurisdictionVersionNumber).append("\n")

                barcodeDocumentFormat.document.children.forEach { subfile ->
                    for (field in subfile.fields) {
                        barcodesResult.append(field.type.name).append(": ").append(field.value)
                                .append("\n")
                    }
                }
            }
            is BoardingPass -> {
                barcodesResult.append("\n")
                    .append("Boarding Pass Document\n")
                    .append("${barcodeDocumentFormat.name}\n")
                barcodeDocumentFormat.legs?.forEach { leg ->
                    leg.document.fields.forEach {
                        barcodesResult.append("${it.type.name}:${it.value}\n")
                    }
                }
            }
            is DEMedicalPlan -> {
                barcodesResult.append("\nDE Medical Plan Document\n")

                barcodesResult.append("Doctor Fields:\n")
                barcodeDocumentFormat.doctor.document.fields.forEach {
                    barcodesResult.append("${it.type.name}: ${it.value}\n")
                }
                barcodesResult.append("\n")

                barcodesResult.append("Patient Fields:\n")
                barcodeDocumentFormat.patient.document.fields.forEach {
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
                    .flatMap { it.document.fields.asSequence() }
                    .forEach {
                        barcodesResult.append("${it.type.name}: ${it.value}\n")
                    }
            }
            is MedicalCertificate -> {
                barcodesResult.append("\nMedical Certificate Document\n")

                barcodeDocumentFormat.document.fields.forEach {
                    barcodesResult.append("${it.type?.name}: ${it.value}\n")
                }
            }
            is IDCardPDF417 -> {
                barcodesResult.append("\nId Card PDF417\n")

                barcodeDocumentFormat.document.fields.forEach {
                    barcodesResult.append("${it.type?.name}: ${it.value}\n")
                }
            }
            is SEPA -> {
                barcodesResult.append("\nSEPA Document\n")

                barcodeDocumentFormat.document.fields.forEach {
                    barcodesResult.append("${it.type.name}: ${it.value}\n")
                }
            }
            is SwissQR -> {
                barcodesResult.append("\nSwiss QR Document\n")
                barcodesResult.append("Version: ${barcodeDocumentFormat.version.value.text}\n")

                barcodeDocumentFormat.document.fields.forEach {
                    barcodesResult.append("${it.type.name}: ${it.value}\n")
                }
            }
            is VCard -> {
                barcodesResult.append("\nVcard Document\n")

                barcodeDocumentFormat.document.fields.forEach {
                    barcodesResult.append("${it.type.name}: ${it.value}\n")
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
