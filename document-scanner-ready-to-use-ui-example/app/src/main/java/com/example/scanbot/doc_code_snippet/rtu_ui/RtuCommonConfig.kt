package com.example.scanbot.doc_code_snippet.rtu_ui

import androidx.appcompat.app.AppCompatActivity
import io.scanbot.sdk.ScanbotSDKInitializer
import io.scanbot.sdk.ui_v2.document.DocumentScannerActivity
import io.scanbot.sdk.ui_v2.document.configuration.DocumentScanningFlow

fun rtuWithCodeResult(activity: AppCompatActivity) {
    // @Tag("Launching The Scanner with the request code")
    // Starting the activity
    val intent = DocumentScannerActivity.newIntent(
        context = activity,
        scannerConfiguration = DocumentScanningFlow.default()
    )
    val DOCUMENT_SCANNER_UI_REQUEST_CODE = 42
    activity.startActivityForResult(intent, DOCUMENT_SCANNER_UI_REQUEST_CODE)
    // @EndTag("Launching The Scanner with the request code")
    // ...
    // @Tag("Force-closing the Activity")
    // When it is needed to close the Activity from outside, call the following method:
    DocumentScannerActivity.forceClose(context = activity, keepResult = true)
    // @EndTag("Force-closing the Activity")
}

