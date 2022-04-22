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
import io.scanbot.checkscanner.model.CheckFieldType
import io.scanbot.checkscanner.model.Result

class CheckScannerResultActivity : AppCompatActivity() {
    private lateinit var checkResultImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_result)

        val fieldsLayout = findViewById<LinearLayout>(R.id.check_result_fields_layout)
        checkResultImageView = findViewById(R.id.check_result_image)

        val fields = intent.getSerializableExtra(EXTRA_FIELDS) as ArrayList<Pair<CheckFieldType, String>>

        tempDocumentImage?.let {
            checkResultImageView.visibility = View.VISIBLE
            checkResultImageView.setImageBitmap(it)
        }

        fields.forEach { field ->
            addValueView(fieldsLayout, field.first.name, field.second)
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
        const val EXTRA_FIELDS = "fields"

        // TODO: handle image more carefully in production code
        var tempDocumentImage: Bitmap? = null

        @JvmStatic
        fun newIntent(context: Context?, result: Result): Intent {
            val intent = Intent(context, CheckScannerResultActivity::class.java)

            val listOf = ArrayList<Pair<CheckFieldType, String>>()
            for (field in result.fields) {
                val pair = field.key to field.value.value
                listOf.add(pair)
            }

            intent.putExtra(EXTRA_FIELDS, listOf)
            return intent
        }
    }
}