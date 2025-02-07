package io.scanbot.example.doc_code_snippet.mc

import android.app.Application
import android.content.Context
import io.scanbot.sdk.*

/*
    NOTE: this snippet of code is to be used only as a part of the website documentation.
    This code is not intended for any use outside of the support of documentation by Scanbot SDK GmbH employees.
*/

// NOTE for maintainers: whenever changing this code,
// ensure that links using it are still pointing to valid lines!
// Pay attention to imports adding/removal/sorting!
// Page URLs using this code:
// TODO: add URLs here

// @Tag("Initialize SDK")
class ExampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // The Scanbot SDK initialization:
        ScanbotSDKInitializer()
                .prepareOCRLanguagesBlobs(true)
                // ...
                .initialize(this)
    }
}
// @EndTag("Initialize SDK")

fun scannerCreationSnippet(context: Context) {
    // @Tag("Create Medical Certificate Scanner")
    val scanbotSdk = ScanbotSDK(context)
    val medicalCertificateScanner = scanbotSdk.createMedicalCertificateScanner()
    // @EndTag("Create Medical Certificate Scanner")
}