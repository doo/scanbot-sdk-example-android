package io.scanbot.example;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class BarcodeTypesActivity extends AppCompatActivity {

    public static final String SELECTED_BARCODE_TYPES = "SELECTED_BARCODE_TYPES";

    private ArrayList<Integer> selectedBarcodeFormatsIndexes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_types);

        if (getIntent().hasExtra(SELECTED_BARCODE_TYPES)) {
            selectedBarcodeFormatsIndexes = getIntent().getIntegerArrayListExtra(SELECTED_BARCODE_TYPES);
        }

        RecyclerView typesList = findViewById(R.id.barcode_types_list);
        typesList.setHasFixedSize(true);
        typesList.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));

        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        typesList.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        BarcodeTypesAdapter adapter = new BarcodeTypesAdapter(selectedBarcodeFormatsIndexes);
        typesList.setAdapter(adapter);

        findViewById(R.id.apply).setOnClickListener(v -> {
            Intent data = new Intent();
            ArrayList<Integer> arrayList = new ArrayList<>();
            for (int i = 0; i < adapter.selectedFormats.size(); i++)
                arrayList.add(adapter.selectedFormats.valueAt(i).ordinal());
            data.putIntegerArrayListExtra(SELECTED_BARCODE_TYPES, arrayList);
            setResult(RESULT_OK, data);
            finish();
        });
    }

}
