package com.example.scanbot.doc_code_snippet;

import android.util.Log;

import java.util.ArrayList;

import io.scanbot.common.Result;
import io.scanbot.sdk.barcode.BarcodeItem;
import io.scanbot.sdk.barcode.BarcodeScannerResult;

public class ResultJ {

    // @Tag("Handle Result with ifs in java")
    public void result(Result<BarcodeScannerResult> result) {
        if (result instanceof Result.Success) {
            BarcodeScannerResult barcodeScannerResult = ((Result.Success<BarcodeScannerResult>) result).getData();
            // Handle success
        } else if (result instanceof Result.Failure) {
            String message = ((Result.Failure) result).getMessage();
            // Handle failure
            Log.e("TAG", "Barcode scanning failed: " + message);
        }
    }
    // @Tag("Handle Result with ifs in java")

    // @Tag("Handle Result with getter functions in java")
    public void resultWithGetters(Result<BarcodeScannerResult> result) {
        // get value or null if unsuccessful
        BarcodeScannerResult nullableResult = result.getOrNull();
        // get value or throw exception if unsuccessful then handle exception with try-catch
        BarcodeScannerResult nonNullableResult = result.getOrThrow();
        // get value or put default value if unsuccessful
        BarcodeScannerResult resultOrDefaultValue = result.getOrDefault(
                new BarcodeScannerResult(
                        new ArrayList<BarcodeItem>(),
                        false
                )
        );
    }
    // @Tag("Handle Result with getter functions in java")
}
