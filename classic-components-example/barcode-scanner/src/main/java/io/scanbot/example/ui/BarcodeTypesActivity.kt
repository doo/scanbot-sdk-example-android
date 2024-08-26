package io.scanbot.example.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.scanbot.example.R
import io.scanbot.example.ui.adapter.BarcodeTypesAdapter

class BarcodeTypesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode_types)

        val typesList = findViewById<RecyclerView>(R.id.barcode_types_list)
        typesList.setHasFixedSize(true)
        typesList.addItemDecoration(
                DividerItemDecoration(
                        this,
                        DividerItemDecoration.VERTICAL
                )
        )

        // use a linear layout manager
        val layoutManager = LinearLayoutManager(this)
        typesList.layoutManager = layoutManager


        val adapter = BarcodeTypesAdapter()
        typesList.adapter = adapter

        findViewById<View>(R.id.apply).setOnClickListener { v ->
            finish()
        }
    }
}