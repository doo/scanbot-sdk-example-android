package io.scanbot.example.doc_code_snippet.data_extractor

import android.app.Application
import android.content.Context
import io.scanbot.sdk.*
import io.scanbot.sdk.documentdata.*
import io.scanbot.sdk.documentdata.entity.*

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
                // ...
                .initialize(this)
    }
}
// @EndTag("Initialize SDK")

fun extractorCreationSnippet(context: Context) {
    // @Tag("Create Document Data Extractor")
    val scanbotSdk = ScanbotSDK(context)
    val dataExtractor = scanbotSdk.createDocumentDataExtractor()
    // @EndTag("Create Document Data Extractor")
}

fun ehicExtractorCreationSnippet(context: Context) {
    // @Tag("Create Document Data Extractor for EHIC")
    val scanbotSdk = ScanbotSDK(context)
    val dataExtractor = scanbotSdk.createDocumentDataExtractor().getOrNull()
    dataExtractor?.setConfiguration(
        DocumentDataExtractorConfigurationBuilder()
            .setAcceptedDocumentTypes(listOf(RootDocumentType.EuropeanHealthInsuranceCard, RootDocumentType.DeHealthInsuranceCardFront))
            .build())
    // @EndTag("Create Document Data Extractor for EHIC")
}