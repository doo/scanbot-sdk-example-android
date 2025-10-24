package com.example.scanbot.doc_code_snippet;

import android.util.Log;

import io.scanbot.common.Result;
import io.scanbot.sdk.barcode.BarcodeScannerResult;

public class ResultJ {

    // @Tag("Handle Result with ifs")
    public void result(Result<BarcodeScannerResult> result) {
        if (result instanceof Result.Success) {
            BarcodeScannerResult barcodeScannerResult = ((Result.Success<BarcodeScannerResult>) result).getData();
            // Handle success
        } else if (result instanceof Result.Unexpected) {
            String message = ((Result.Unexpected) result).getMessage();
            // Handle failure
            Log.e("TAG", "Barcode scanning failed: " + message);
        }
    }
    // @Tag("Handle Result with ifs")
}
