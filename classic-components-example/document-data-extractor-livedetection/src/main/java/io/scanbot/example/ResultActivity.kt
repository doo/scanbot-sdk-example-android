package io.scanbot.example

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import io.scanbot.sdk.documentdata.DocumentDataExtractionResult
import io.scanbot.sdk.genericdocument.entity.Field


class ResultActivity : AppCompatActivity() {

    private lateinit var result: DocumentDataExtractionResult

    override fun onCreate(savedInstanceState: Bundle?) {
        if (this::result.isInitialized.not()) {
            result = DocumentsResultsStorage.result ?: kotlin.run {
                // results were not set - something went wrong (e.g. activity was restored)
                super.onCreate(savedInstanceState)
                finish()
                return
            }
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_idcard_scanner_result)

        val resultsRecyclerView = findViewById<RecyclerView>(R.id.resultsRecyclerView)
        val adapter = Adapter(result)

        val dividerDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        resultsRecyclerView.addItemDecoration(dividerDecoration)

        resultsRecyclerView.adapter = adapter
    }
}

/**
 * Static-backed temporary storage that will
 * safely null-out reference to results object once obtained by results activity.
 *
 * First-comes-to-mind solution - not for use production!
 */
object DocumentsResultsStorage {

    var result: DocumentDataExtractionResult? = null
        get() {
            val result = field
            field = null
            return result
        }
}

private class Adapter(
        private val result: DocumentDataExtractionResult
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class FieldViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val fieldType: TextView = itemView.findViewById(R.id.fieldType)
        private val fieldCroppedImage: ImageView = itemView.findViewById(R.id.fieldCroppedImage)
        private val fieldTextConfidence: TextView = itemView.findViewById(R.id.fieldTextConfidence)
        private val fieldRecognizedText: TextView = itemView.findViewById(R.id.fieldRecognizedText)

        fun bind(item: Field) {
            val confidence = String.format(FIELD_CONFIDENCE_FLOAT_FORMAT, (item.value?.confidence ?: 0.0) * 100)

            fieldType.text = String.format(FIELD_TYPE_FORMAT, item.type.name.capitalize())
            fieldCroppedImage.setImageBitmap(item.image?.toBitmap())
            fieldTextConfidence.text = String.format(FIELD_CONFIDENCE_FORMAT, confidence)
            fieldRecognizedText.text = item.value?.text
        }
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val croppedCard: ImageView = itemView.findViewById(R.id.croppedCard)
        private val cardType: TextView = itemView.findViewById(R.id.cardType)

        fun bind(item: DocumentDataExtractionResult) {
            croppedCard.setImageBitmap(item.croppedImage?.toBitmap())
            cardType.text = item.document?.type?.name
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) return ITEM_TYPE_HEADER else ITEM_TYPE_FIELD
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_TYPE_HEADER -> {
                val layout = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_idcard_results_header, parent, false)
                HeaderViewHolder(layout)
            }
            ITEM_TYPE_FIELD -> {
                val layout = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_idcard_scanner_result_field, parent, false)
                FieldViewHolder(layout)
            }
            else -> throw IllegalStateException("Unexpected item type: $viewType")
        }
    }

    override fun getItemCount(): Int = (fieldsList()?.size ?: 0) + 1

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            ITEM_TYPE_HEADER -> {
                (holder as HeaderViewHolder).bind(result)
            }
            ITEM_TYPE_FIELD -> {
                (holder as FieldViewHolder).bind(fieldsList()!![position - 1])
            }
        }
    }

    private fun fieldsList(): List<Field>? {
        val document = result.document
        return document?.fields?.plus(
                document.children.flatMap { depth1 ->
                    depth1.fields.plus(depth1.children.flatMap { depth2 -> depth2.fields })
                }
        )
    }

    private companion object {
        const val ITEM_TYPE_HEADER = 0
        const val ITEM_TYPE_FIELD = 1

        const val FIELD_TYPE_FORMAT = "Field type: %s"
        const val FIELD_CONFIDENCE_FORMAT = "Recognized text with confidence %1\$s%%"
        const val FIELD_CONFIDENCE_FLOAT_FORMAT = "%.2f"
    }
}
