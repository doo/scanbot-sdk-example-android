package io.scanbot.example.doc_code_snippet.data_extractor

import android.app.Application
import android.content.Context
import io.scanbot.sdk.*

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