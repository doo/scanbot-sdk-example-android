package io.scanbot.example.doc_code_snippet.migration

/*
    NOTE: this snippet of code is to be used only as a part of the website documentation.
    This code is not intended for any use outside of the support of documentation by Scanbot SDK GmbH employees.
*/

// NOTE for maintainers: whenever changing this code,
// ensure that links using it are still pointing to valid lines!
// Pay attention to imports adding/removal/sorting!
// Page URLs using this code:
// TODO: add URLs here

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.example.R
import io.scanbot.sdk.*
import io.scanbot.sdk.common.*
import io.scanbot.sdk.docprocessing.*
import io.scanbot.sdk.geometry.AspectRatio
import io.scanbot.sdk.ui_v2.common.*
import io.scanbot.sdk.ui_v2.common.activity.*
import io.scanbot.sdk.ui_v2.document.*
import io.scanbot.sdk.ui_v2.document.configuration.*
import io.scanbot.sdk.util.*

// @Tag("Open Document Scanner RTU v2")
// Your activity class:
class MainActivityWithDocumentScannerRtuV2 : AppCompatActivity() {
    private lateinit var documentScannerResultLauncher: ActivityResultLauncher<DocumentScanningFlow>

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val previewImageView = findViewById<ImageView>(R.id.first_page_image_preview)

        documentScannerResultLauncher =
                registerForActivityResultOk(io.scanbot.sdk.ui_v2.document.DocumentScannerActivity.ResultContract()) {
                    resultEntity: io.scanbot.sdk.ui_v2.document.DocumentScannerActivity.Result ->
                    val result: Document? = resultEntity.result
                    val pages: List<Page>? = result?.pages
                    pages?.get(0)?.let {
                        // in v2 you can access the image bitmap directly from the Page:
                        val previewImage = it.documentPreviewImage
                        previewImageView.setImageBitmap(previewImage)
                    }
                }

        findViewById<Button>(R.id.open_document_scanner).setOnClickListener {
            // openDocumentScannerRtuV2()
        }
    }
    // ...
}
// @EndTag("Open Document Scanner RTU v2")


// @Tag("Configure Document Scanner RTU v2")
// ...
// in your Activity class:
private fun openDocumentScannerRtuV2() {
    // Customize text resources, behavior and UI:
    val configuration = DocumentScanningFlow().apply {
        screens.camera.apply {
            cameraConfiguration.apply {
                // Equivalent to setAutoSnappingSensitivity(0.75f)
                autoSnappingSensitivity = 0.75
            }

            // Equivalent to ignoreOrientationMismatch(true)
            screens.camera.scannerParameters.ignoreOrientationMismatch = true

            // Ready-to-Use UI v2 contains an acknowledgment screen to
            // verify the captured document with the built-in Document Quality Analyzer.
            // You can still disable this step:
            acknowledgement.acknowledgementMode = AcknowledgementMode.NONE

            // When you disable the acknowledgment screen, you can enable the capture feedback,
            // there are different options available, for example you can display a checkmark animation:
            captureFeedback = CaptureFeedback(
                    snapFeedbackMode = PageSnapFeedbackMode.pageSnapCheckMarkAnimation()
            )

            // You may hide the import button in the camera screen, if you don't need it:
            bottomBar.importButton.visible = false
        }

        // Equivalent to setBottomBarBackgroundColor(Color.RED), but not recommended:
        appearance.bottomBarBackgroundColor = ScanbotColor(Color.BLUE)

        // However, now all the colors can be conveniently set using the Palette object:
        palette.apply {
            sbColorPrimary = ScanbotColor(Color.BLUE)
            sbColorOnPrimary = ScanbotColor(Color.WHITE)
            sbColorOnSurfaceVariant = ScanbotColor(Color.DKGRAY)
            // ..
        }

        // Now all the text resources are in the localization object
        localization.apply {
            // Equivalent to setTextHintOK("Don't move.\nCapturing document...")
            cameraUserGuidanceReadyToCapture = "Don't move.\nCapturing document..."
        }

        // Ready-to-Use UI v2 contains a review screen, you can disable it:
        screens.review.enabled = false

        // Multi Page button is always hidden in RTU v2
        // Therefore setMultiPageButtonHidden(true) is not available

        // Equivalent to setMultiPageEnabled(false)
        outputSettings.pagesScanLimit = 1
    }
}
// @EndTag("Configure Document Scanner RTU v2")


// @Tag("Open Cropping UI RTU v2")
// Your activity class:
class MainActivityWithCroppingRtuV2 : AppCompatActivity() {
    private lateinit var croppingResultLauncher: ActivityResultLauncher<CroppingConfiguration>

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val previewImageView = findViewById<ImageView>(R.id.first_page_image_preview)

        croppingResultLauncher =
                registerForActivityResultOk(
                        io.scanbot.sdk.ui_v2.document.CroppingActivity.ResultContract()
                ) { resultEntity ->
                    resultEntity.result?.let {
                        val documentApi = ScanbotSDK(this).documentApi
                        // This way you can access the document from the Document API:
                        val document = documentApi.loadDocument(documentId = it.documentUuid)
                        val previewImage = document?.pageWithId(it.pageUuid)?.documentPreviewImage
                        previewImageView.setImageBitmap(previewImage)
                    }
                }

        findViewById<Button>(R.id.open_cropping_ui).setOnClickListener {
            // openCroppingRtuV2(documentUuid, pageUuid, croppingResultLauncher)
        }
    }
    // ...
}
// @EndTag("Open Cropping UI RTU v2")


// @Tag("Configure Cropping UI RTU v2")
// ...
// in your Activity class:
private fun openCroppingRtuV2(documentUuid: String, pageUuid: String, croppingResultLauncher: ActivityResultLauncher<CroppingConfiguration>) {
    // Customize text resources, behavior and UI:
    val configuration = CroppingConfiguration(
            // Now you need to pass the document UUID and the page UUID:
            documentUuid = documentUuid,
            pageUuid = pageUuid,
    )

    // Now you can apply your business colors using the Palette object:
    configuration.palette.apply {
        sbColorPrimary = ScanbotColor(Color.BLUE)
        // ..
    }

    croppingResultLauncher.launch(configuration)
}
// @EndTag("Configure Cropping UI RTU v2")


// @Tag("Open Finder Document Scanner RTU v2")
// Your activity class:
class MainActivityWithFinderRtuV2 : AppCompatActivity() {
    private lateinit var documentScannerResultLauncher: ActivityResultLauncher<DocumentScanningFlow>

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val previewImageView = findViewById<ImageView>(R.id.first_page_image_preview)

        documentScannerResultLauncher =
                registerForActivityResultOk(DocumentScannerActivity.ResultContract()) { resultEntity: DocumentScannerActivity.Result ->
                    resultEntity.result?.pages?.get(0)?.let {
                        // in v2 you can access the image bitmap directly from the result entity:
                        val previewImage = it.documentPreviewImage
                        previewImageView.setImageBitmap(previewImage)
                    }
                }

        findViewById<Button>(R.id.open_document_scanner).setOnClickListener {
            // openDocumentScannerRtuV2WithFinder(documentScannerResultLauncher)
        }
    }
    // ...
}
// @EndTag("Open Finder Document Scanner RTU v2")


// @Tag("Configure Finder Document Scanner RTU v2")
// ...
// in your Activity class:
private fun openDocumentScannerRtuV2withFinder(documentScannerResultLauncher: ActivityResultLauncher<DocumentScanningFlow>) {
    // Customize text resources, behavior and UI:
    val configuration = DocumentScanningFlow().apply {
        palette.apply {
            sbColorPrimary = ScanbotColor(Color.BLUE)
        }
        screens.camera.apply {
            viewFinder.apply {
                visible = true
                aspectRatio = AspectRatio(3.0, 4.0)
            }
            bottomBar.apply {
                previewButton = PreviewButton.noButtonMode()
                autoSnappingModeButton.visible = false
                importButton.visible = false
            }
            acknowledgement.acknowledgementMode = AcknowledgementMode.NONE
            captureFeedback.snapFeedbackMode = PageSnapFeedbackMode.pageSnapCheckMarkAnimation()
        }
        screens.review.enabled = false
        outputSettings.pagesScanLimit = 1
    }

    documentScannerResultLauncher.launch(configuration)
}
// @EndTag("Configure Finder Document Scanner RTU v2")


// @Tag("Storage Migration")
fun storageMigrationSnippet(context: Context) {
    // Take a list of legacy pages that represent one document and convert them to a new document.
    val legacyPages: List<io.scanbot.sdk.persistence.page.legacy.Page> = listOf(/* your legacy pages */)
    val document = legacyPages.toDocument(ScanbotSDK(context), documentImageSizeLimit = 2048)

    // Now you may delete the files corresponding to the `legacyPages` to free up storage.
}
// @EndTag("Storage Migration")