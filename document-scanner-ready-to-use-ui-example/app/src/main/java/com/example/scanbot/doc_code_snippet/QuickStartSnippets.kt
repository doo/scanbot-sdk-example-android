package com.example.scanbot.doc_code_snippet

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.sdk.usecases.documents.R

// Add the following imports in your Activity (for example, MainActivity.kt):
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import io.scanbot.sdk.ScanbotSDKInitializer
import io.scanbot.sdk.docprocessing.Document
import io.scanbot.sdk.docprocessing.Page
import io.scanbot.sdk.ui_v2.common.activity.registerForActivityResultOk
import io.scanbot.sdk.ui_v2.document.DocumentScannerActivity
import io.scanbot.sdk.ui_v2.document.configuration.DocumentScanningFlow

class QuickStartSnippetsActivity : AppCompatActivity() {

    // Adapt the 'onCreate' method in your Activity (for example, MainActivity.kt):
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ...
        // Initialize the SDK here:
        ScanbotSDKInitializer()
            // optional: uncomment the next line if you have a license key
            // .license(this.application, LICENSE_KEY)
            .initialize(this.application)
    }
}

class QuickStartDocumentScannerSnippetsActivity : AppCompatActivity() {

    // Add the following variable in your Activity:
    private lateinit var documentScreenLauncher: ActivityResultLauncher<DocumentScanningFlow>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.quick_start_snippets_activity)

        // ...
        // After the super.onCreate(...) and ScanbotSDKInitializer call:

        // We will use this image view to display the first page preview image, it should exist in your layout xml:
        val previewImageView = findViewById<ImageView>(R.id.first_page_image_preview)

        documentScreenLauncher =
            registerForActivityResultOk(DocumentScannerActivity.ResultContract()) {
                    resultEntity: DocumentScannerActivity.Result ->
                val result: Document? = resultEntity.result
                val pages: List<Page>? = result?.pages

                // Display the first page preview image:
                pages?.get(0)?.let {
                    val previewImage = it.documentPreviewImage
                    previewImageView.setImageBitmap(previewImage)
                }
            }

        // Set up a button to open the document scanner, it should exist in your layout xml:
        findViewById<Button>(R.id.open_document_scanner).setOnClickListener {
            openDocumentScannerRtuV2()
        }
    }

    // Launch the Document Scanner in your Activity:
    private fun openDocumentScannerRtuV2() {
        val configuration = DocumentScanningFlow().apply {
            // Customize text resources, behavior and UI:
            // ...
        }

        documentScreenLauncher.launch(configuration)
    }
}
