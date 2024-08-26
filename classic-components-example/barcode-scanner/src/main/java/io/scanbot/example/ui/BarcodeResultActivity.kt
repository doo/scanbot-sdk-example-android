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
import io.scanbot.sdk.barcode.entity.BarcodeScanningResult
import java.io.File

class BarcodeResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBarcodeResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarcodeResultBinding.inflate(layoutInflater)
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

    private fun showLatestBarcodeResult(detectedBarcodes: BarcodeScanningResult?) {
        detectedBarcodes?.let {
            detectedBarcodes.barcodeItems.asSequence().map { item ->
                BarcodeItemBinding.inflate(layoutInflater, binding.recognisedItems, false)
                    .also {
                        item.image?.let { bitmap ->
                            it.image.setImageBitmap(bitmap)
                        }
                        it.barcodeFormat.text = item.barcodeFormat.name
                        it.docFormat.text = item.formattedResult?.let {
                            it::class.java.simpleName
                        } ?: "Unknown document"
                        it.docFormat.visibility =
                            if (item.formattedResult != null) View.VISIBLE else View.GONE
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
