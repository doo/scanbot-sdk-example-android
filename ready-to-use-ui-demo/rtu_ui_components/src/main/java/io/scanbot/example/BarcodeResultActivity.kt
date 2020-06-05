package io.scanbot.example

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import io.scanbot.example.repository.BarcodeResultRepository
import io.scanbot.sdk.barcode.entity.BarcodeScanningResult
import kotlinx.android.synthetic.main.activity_barcode_result.*
import kotlinx.android.synthetic.main.barcode_item.view.*
import kotlinx.android.synthetic.main.snap_image_item.view.*
import java.io.File

class BarcodeResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode_result)
        setSupportActionBar(toolbar)

        showSnapImageIfExists(
            BarcodeResultRepository.barcodeResultBundle?.previewPath
                ?: BarcodeResultRepository.barcodeResultBundle?.imagePath
        )

        showLatestBarcodeResult(BarcodeResultRepository.barcodeResultBundle?.barcodeScanningResult)
    }

    private fun showSnapImageIfExists(imagePath: String?) {
        imagePath?.let { imagePath ->
            recognisedItems.addView(
                layoutInflater.inflate(
                    R.layout.snap_image_item,
                    recognisedItems,
                    false
                )?.also {
                    Picasso.with(this).load(File(imagePath)).into(it.snapImage)
                })
        }
    }

    private fun showLatestBarcodeResult(detectedBarcodes: BarcodeScanningResult?) {

        detectedBarcodes?.let {
            detectedBarcodes.barcodeItems.asSequence().map { item ->
                layoutInflater.inflate(R.layout.barcode_item, recognisedItems, false)?.also {
                    item.image?.let { bitmap ->
                        it.image.setImageBitmap(bitmap)
                    }
                    it.barcodeFormat.text = item.barcodeFormat.name
                    it.docFormat.text = item.formattedResult?.documentFormat
                    it.docFormat.visibility = if (item.formattedResult != null) View.VISIBLE else View.GONE
                    it.docText.text = item.text
                    it.setOnClickListener {
                        val intent = Intent(this, DetailedItemDataActivity::class.java)
                        BarcodeResultRepository.selectedBarcodeItem = item
                        startActivity(intent)
                    }
                }
            }.forEach {
                recognisedItems.addView(it)
            }
        }
    }
}
