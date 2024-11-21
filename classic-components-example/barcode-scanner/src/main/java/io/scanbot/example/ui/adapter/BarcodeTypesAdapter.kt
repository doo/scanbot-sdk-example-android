package io.scanbot.example.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.scanbot.example.R
import io.scanbot.example.repository.BarcodeTypeRepository
import io.scanbot.example.ui.adapter.BarcodeTypesAdapter.BarcodeTypesViewHolder
import io.scanbot.sdk.barcode.BarcodeFormats

class BarcodeTypesAdapter : RecyclerView.Adapter<BarcodeTypesViewHolder>() {

    private val values = BarcodeFormats.all

    class BarcodeTypesViewHolder(barcodeTypeView: View) : RecyclerView.ViewHolder(barcodeTypeView) {
        var barcodeTypeName: TextView = barcodeTypeView.findViewById(R.id.barcode_type_name)
        var barcodeTypeChecker: CheckBox = barcodeTypeView.findViewById(R.id.barcode_type_checker)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarcodeTypesViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val barcodeTypeView = inflater.inflate(R.layout.barcode_type_view, parent, false)
        return BarcodeTypesViewHolder(barcodeTypeView)
    }

    override fun onBindViewHolder(holder: BarcodeTypesViewHolder, position: Int) {
        val barcodeFormat = values[position]
        holder.barcodeTypeName.text = barcodeFormat.name
        holder.barcodeTypeChecker.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                BarcodeTypeRepository.selectType(barcodeFormat)
            } else {
                BarcodeTypeRepository.deselectType(barcodeFormat)
            }
        }

        holder.barcodeTypeChecker.isChecked = BarcodeTypeRepository.selectedTypes.contains(barcodeFormat)
    }

    override fun getItemCount(): Int {
        return values.size
    }
}
