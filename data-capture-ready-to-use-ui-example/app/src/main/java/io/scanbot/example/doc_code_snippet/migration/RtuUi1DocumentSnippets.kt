package io.scanbot.example.doc_code_snippet.migration

/** Legacy SDK Snippets

package io.scanbot.example.doc_code_snippet.migration

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.example.R
import io.scanbot.sdk.*
import io.scanbot.sdk.common.*
import io.scanbot.sdk.geometry.AspectRatio
import io.scanbot.sdk.persistence.page.*
import io.scanbot.sdk.ui.*
import io.scanbot.sdk.ui.view.camera.*
import io.scanbot.sdk.ui.view.camera.configuration.*
import io.scanbot.sdk.ui.view.edit.*

// @Tag("Open Document Scanner RTU v1")
// Your activity class:
class MainActivityWithDocumentScannerRtuV1 : AppCompatActivity() {
    private lateinit var documentScannerResultLauncher: ActivityResultLauncher<DocumentScannerConfiguration>

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val previewImageView = findViewById<ImageView>(R.id.first_page_image_preview)

        documentScannerResultLauncher =
                registerForActivityResultOk(
                        DocumentScannerActivity.ResultContract()
                ) { resultEntity ->
                    resultEntity.result?.get(0)?.let {
                        // In v1 you had to use PageFileStorage to access the image bitmap:
                        val previewImage =
                                ScanbotSDK(context = this).createPageFileStorage().getPreviewImage(
                                        it.pageId, type = PageFileType.DOCUMENT
                                )
                        previewImageView.setImageBitmap(previewImage)
                    }
                }

        findViewById<Button>(R.id.open_document_scanner).setOnClickListener {
            // openDocumentScannerRtuV1(documentScannerResultLauncher)
        }
    }
    // ...
}
// @EndTag("Open Document Scanner RTU v1")


// @Tag("Configure Document Scanner RTU v1")
// ..
// in your Activity class:
fun openDocumentScannerRtuV1(documentScannerResultLauncher: ActivityResultLauncher<DocumentScannerConfiguration>) {
    // Customize text resources, behavior and UI:
    val configuration = DocumentScannerConfiguration()

    configuration.setIgnoreOrientationMismatch(true)
    configuration.setAutoSnappingSensitivity(0.75f)

    configuration.setTopBarBackgroundColor(Color.BLUE)
    configuration.setBottomBarBackgroundColor(Color.BLUE)

    configuration.setTextHintOK("Don't move.\nCapturing document...")

    configuration.setMultiPageButtonHidden(true)
    configuration.setMultiPageEnabled(false)

    documentScannerResultLauncher.launch(configuration)
}
// @EndTag("Configure Document Scanner RTU v1")


// @Tag("Open Cropping UI RTU v1")
// Your activity class:
class MainActivityWithCropping : AppCompatActivity() {
    private lateinit var croppingResultLauncher: ActivityResultLauncher<io.scanbot.sdk.ui.view.edit.configuration.CroppingConfiguration>

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val previewImageView = findViewById<ImageView>(R.id.first_page_image_preview)

        croppingResultLauncher =
                registerForActivityResultOk(
                        CroppingActivity.ResultContract()
                ) { resultEntity ->
                    resultEntity.result?.let {
                        // In v1 you had to use PageFileStorage to access the image bitmap:
                        val previewImage =
                                ScanbotSDK(context = this).createPageFileStorage().getPreviewImage(
                                        it.pageId, type = PageFileType.DOCUMENT
                                )
                        previewImageView.setImageBitmap(previewImage)
                    }
                }

        findViewById<Button>(R.id.open_cropping_ui).setOnClickListener {
            // openCroppingRtuV1(pageId, croppingResultLauncher)
        }
    }
    // ...
}
// @EndTag("Open Cropping UI RTU v1")


// @Tag("Configure Cropping UI RTU v1")
// ..
// in your Activity class:
private fun openCroppingRtuV1(pageId: String, croppingResultLauncher: ActivityResultLauncher<io.scanbot.sdk.ui.view.edit.configuration.CroppingConfiguration>) {
    // Customize text resources, behavior and UI:
    val configuration = io.scanbot.sdk.ui.view.edit.configuration.CroppingConfiguration(
            // In v1, you passed the Page object you needed to re-crop:
            io.scanbot.sdk.persistence.page.legacy.Page(pageId)
    )

    configuration.setDoneButtonTitle("Apply")

    configuration.setTopBarBackgroundColor(Color.BLUE)
    configuration.setBottomBarBackgroundColor(Color.BLUE)

    croppingResultLauncher.launch(configuration)
}
// @EndTag("Configure Cropping UI RTU v1")


// @Tag("Open Finder Document Scanner RTU v1")
// Your activity class:
class MainActivityWithFinderRtuV1 : AppCompatActivity() {
    private lateinit var finderScannerResultLauncher: ActivityResultLauncher<FinderDocumentScannerConfiguration>

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val previewImageView = findViewById<ImageView>(R.id.first_page_image_preview)

        finderScannerResultLauncher =
                registerForActivityResultOk(
                        FinderDocumentScannerActivity.ResultContract()
                ) { resultEntity ->
                    resultEntity.result?.let {
                        // In v1 you had to use PageFileStorage to access the image bitmap:
                        val previewImage =
                                ScanbotSDK(context = this).createPageFileStorage().getPreviewImage(
                                        it.pageId, type = PageFileType.DOCUMENT
                                )
                        previewImageView.setImageBitmap(previewImage)
                    }
                }

        findViewById<Button>(R.id.open_document_scanner).setOnClickListener {
            // openDocumentScannerRtuV1WithFinder(finderScannerResultLauncher)
        }
        // ...
    }
}
// @EndTag("Open Finder Document Scanner RTU v1")


// @Tag("Configure Finder Document Scanner RTU v1")
// ..
// in your Activity class:
private fun openDocumentScannerRtuV1WithFinder(finderScannerResultLauncher: ActivityResultLauncher<FinderDocumentScannerConfiguration>) {
    // Customize text resources, behavior and UI:
    val configuration = FinderDocumentScannerConfiguration()

    configuration.setTopBarBackgroundColor(Color.BLUE)
    configuration.setFinderAspectRatio(AspectRatio(3.0, 4.0))
    configuration.setShutterButtonHidden(false)

    finderScannerResultLauncher.launch(configuration)
}
// @EndTag("Configure Finder Document Scanner RTU v1")

 */