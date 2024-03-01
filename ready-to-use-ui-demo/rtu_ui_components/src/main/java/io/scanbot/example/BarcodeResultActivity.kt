package io.scanbot.example

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.example.databinding.ActivityBarcodeResultBinding
import io.scanbot.example.databinding.BarcodeItemBinding
import io.scanbot.example.repository.BarcodeResultRepository
import io.scanbot.example.util.PicassoHelper
import io.scanbot.sdk.ui_v2.barcode.configuration.BarcodeScannerResult
import java.io.File

class BarcodeResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBarcodeResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarcodeResultBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(binding.toolbar)

        showSnapImageIfExists(
            BarcodeResultRepository.barcodeResultBundle?.previewPath
                ?: BarcodeResultRepository.barcodeResultBundle?.imagePath
        )

        showLatestBarcodeResult(BarcodeResultRepository.barcodeResultBundle?.barcodeScanningResult)
    }

    private fun showSnapImageIfExists(imagePath: String?) {
        imagePath?.let { imagePath ->
            binding.recognisedItems.addView(
                layoutInflater.inflate(
                    R.layout.snap_image_item,
                    binding.recognisedItems,
                    false
                )?.also {
                    PicassoHelper.with(this)
                        .load(File(imagePath)).into((it.findViewById(R.id.snapImage) as androidx.appcompat.widget.AppCompatImageView))
                })
        }
    }

    private fun showLatestBarcodeResult(detectedBarcodes: BarcodeScannerResult?) {

        detectedBarcodes?.let {
            detectedBarcodes.items.asSequence().map { item ->
                val itemViewBinding = BarcodeItemBinding.inflate(layoutInflater, binding.recognisedItems, false)
                itemViewBinding.barcodeFormat.text = item.type.name
                itemViewBinding.docFormat.text = item.formattedResult?.let { formattedResult ->
                    formattedResult::class.java.simpleName
                } ?: "Unknown document"
                itemViewBinding.docFormat.visibility = if (item.formattedResult != null) View.VISIBLE else View.GONE
                itemViewBinding.docText.text = item.textWithExtension
                itemViewBinding.count.text = "x${item.count}"
                itemViewBinding.root.setOnClickListener {
                    val intent = Intent(this, DetailedItemDataActivity::class.java)
                    BarcodeResultRepository.selectedBarcodeItem = item
                    startActivity(intent)
                }
                itemViewBinding.root
            }.forEach {
                binding.recognisedItems.addView(it)
            }
        }
    }
}
