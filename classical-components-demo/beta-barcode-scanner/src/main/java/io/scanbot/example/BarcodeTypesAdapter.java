package io.scanbot.example;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import io.scanbot.sdk.barcode.entity.BarcodeFormat;

public class BarcodeTypesAdapter extends RecyclerView.Adapter<BarcodeTypesAdapter.BarcodeTypesViewHolder> {

    public final SparseArray<BarcodeFormat> selectedFormats = new SparseArray<>();

    private final ArrayList<Integer> preSelectedBarcodeFormatsIndexes;

    public BarcodeTypesAdapter(ArrayList<Integer> preSelectedBarcodeFormatsIndexes) {
        this.preSelectedBarcodeFormatsIndexes = preSelectedBarcodeFormatsIndexes;
    }

    public static class BarcodeTypesViewHolder extends RecyclerView.ViewHolder {
        public TextView barcodeTypeName;
        public CheckBox barcodeTypeChecker;

        public BarcodeTypesViewHolder(@NonNull View barcodeTypeView) {
            super(barcodeTypeView);
            this.barcodeTypeName = barcodeTypeView.findViewById(R.id.barcode_type_name);
            this.barcodeTypeChecker = barcodeTypeView.findViewById(R.id.barcode_type_checker);
        }
    }

    @Override
    public BarcodeTypesViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        View barcodeTypeView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.barcode_type_view, parent, false);

        BarcodeTypesViewHolder viewHolder = new BarcodeTypesViewHolder(barcodeTypeView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(BarcodeTypesViewHolder holder, int position) {
        BarcodeFormat barcodeFormat = BarcodeFormat.values()[position];
        holder.barcodeTypeName.setText(barcodeFormat.name());
        holder.barcodeTypeChecker.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedFormats.append(position, barcodeFormat);
            } else {
                selectedFormats.remove(position);
            }
        });

        if (preSelectedBarcodeFormatsIndexes.contains(barcodeFormat.ordinal())) {
            holder.barcodeTypeChecker.setChecked(true);
        }
    }

    @Override
    public int getItemCount() {
        return BarcodeFormat.values().length;
    }
}
