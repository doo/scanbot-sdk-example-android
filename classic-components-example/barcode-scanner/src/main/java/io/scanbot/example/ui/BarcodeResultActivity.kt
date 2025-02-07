package io.scanbot.example.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import io.scanbot.example.databinding.ActivityBarcodeResultBinding
import io.scanbot.example.databinding.BarcodeItemBinding
import io.scanbot.example.databinding.SnapImageItemBinding
import io.scanbot.example.repository.BarcodeResultRepository
import io.scanbot.sdk.barcode.BarcodeScannerResult
import io.scanbot.sdk.barcode.textWithExtension
import java.io.File

class BarcodeResultActivity : AppCompatActivity() {

    private val binding by lazy { ActivityBarcodeResultBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        showSnapImageIfExists(
            BarcodeResultRepository.barcodeResultBundle?.previewPath
                ?: BarcodeResultRepository.barcodeResultBundle?.imagePath
        )

        showLatestBarcodeResult(BarcodeResultRepository.barcodeResultBundle?.barcodeScanningResult)
    }

    private fun showSnapImageIfExists(imagePath: String?) {
        imagePath?.let { path ->
            binding.recognisedItems.addView(
                SnapImageItemBinding.inflate(
                    layoutInflater,
                    binding.recognisedItems,
                    false
                ).also {
                    Picasso.get().load(File(path)).into(it.snapImage)
                }.root
            )
        }
    }

    private fun showLatestBarcodeResult(detectedBarcodes: BarcodeScannerResult?) {
        detectedBarcodes?.let {
            detectedBarcodes.barcodes.asSequence().map { item ->
                BarcodeItemBinding.inflate(layoutInflater, binding.recognisedItems, false)
                    .also {
                        item.sourceImage?.let { image ->
                            it.image.setImageBitmap(image.toBitmap())
                        }
                        it.barcodeFormat.text = item.format.name
                        it.docFormat.text = item.extractedDocument?.let {
                            it::class.java.simpleName
                        } ?: "Unknown document"
                        it.docFormat.visibility =
                            if (item.extractedDocument != null) View.VISIBLE else View.GONE
                        it.docText.text = item.textWithExtension
                        it.root.setOnClickListener {
                            val intent = Intent(this, DetailedItemDataActivity::class.java)
                            BarcodeResultRepository.selectedBarcodeItem = item
                            startActivity(intent)
                        }
                    }
            }.forEach {
                binding.recognisedItems.addView(it.root)
            }
        }
    }
}
