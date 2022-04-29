package io.scanbot.example

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.checkscanner.model.Result
import io.scanbot.genericdocument.entity.GenericDocument

class CheckScannerResultActivity : AppCompatActivity() {
    private lateinit var checkResultImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_result)

        val fieldsLayout = findViewById<LinearLayout>(R.id.check_result_fields_layout)
        checkResultImageView = findViewById(R.id.check_result_image)

        intent.getParcelableExtra<GenericDocument>(EXTRA_CHECK_DOCUMENT)?.also { checkDocument ->
            addValueView(fieldsLayout, "Type", checkDocument.type.name)
            checkDocument.fields.forEach { field ->
                field.value?.let { ocrResult ->
                    addValueView(fieldsLayout, field.type.name, ocrResult.text)
                }
            }
        }

        tempDocumentImage?.let {
            checkResultImageView.visibility = View.VISIBLE
            checkResultImageView.setImageBitmap(it)
        }

        findViewById<View>(R.id.retry).setOnClickListener {
            tempDocumentImage = null
            finish()
        }
    }

    private fun addValueView(layout: LinearLayout, title: String, value: String) {
        val v = layoutInflater.inflate(R.layout.view_key_value, layout, false)
        v.findViewById<TextView>(R.id.view_text_key).text = title
        v.findViewById<TextView>(R.id.view_text_value).text = value
        layout.addView(v)
    }

    companion object {
        const val EXTRA_CHECK_DOCUMENT = "CheckDocument"

        // TODO: handle image more carefully in production code
        var tempDocumentImage: Bitmap? = null

        @JvmStatic
        fun newIntent(context: Context?, result: Result): Intent {
            val intent = Intent(context, CheckScannerResultActivity::class.java)
            intent.putExtra(EXTRA_CHECK_DOCUMENT, result.check)
            return intent
        }
    }
}