package io.scanbot.example.doc_code_snippet.data_extractor

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.example.R
import io.scanbot.genericdocument.entity.*
import io.scanbot.sdk.*
import io.scanbot.sdk.documentdata.*
import io.scanbot.sdk.documentdata.entity.*
import io.scanbot.sdk.genericdocument.entity.*
import io.scanbot.sdk.process.*
import io.scanbot.sdk.ui.*
import io.scanbot.sdk.ui.camera.*
import io.scanbot.sdk.ui.view.documentdata.*
import io.scanbot.sdk.ui.view.documentdata.configuration.DocumentDataExtractorConfiguration

/*
    NOTE: this snippet of code is to be used only as a part of the website documentation.
    This code is not intended for any use outside of the support of documentation by Scanbot SDK GmbH employees.
*/

// NOTE for maintainers: whenever changing this code,
// ensure that links using it are still pointing to valid lines!
// Pay attention to imports adding/removal/sorting!
// Page URLs using this code:
// TODO: add URLs here

fun initSdkSnippet(application: Application) {
    // @Tag("Initialize SDK")
    ScanbotSDKInitializer()
            //...
            .initialize(application)
    // @EndTag("Initialize SDK")
}

fun startDataExtractorRTUAndHandleResultSnippet(activity: AppCompatActivity, scanbotSDK: ScanbotSDK, myButton: Button) {
    // @Tag("Start RTU Data Extractor and Process Result")
    val genericDocumentResult: ActivityResultLauncher<DocumentDataExtractorConfiguration> = activity.registerForActivityResultOk(DocumentDataExtractorActivity.ResultContract()) { result ->
        val resultWrappers = result.result!!
        val firstWrapper = resultWrappers.first()
        val document = firstWrapper.document

        Toast.makeText(
                activity,
                document?.fields?.map { "${it.type.name} = ${it.value?.text}" }.toString(),
                Toast.LENGTH_LONG
        ).show()
    }

    //...

    myButton.setOnClickListener {
        val dataExtractorConfiguration = DocumentDataExtractorConfiguration()
        genericDocumentResult.launch(dataExtractorConfiguration)
    }
    // @EndTag("Start RTU Data Extractor and Process Result")
}

class StartDataExtractorRTUDeprecatedSnippetActivity : AppCompatActivity() {
    private val GENERIC_DOCUMENT_RECOGNIZER_DEFAULT_UI: Int = 1

    private lateinit var myButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // @Tag("(DEPRECATED) Start RTU Data Extractor")
        myButton.setOnClickListener {
            val dataExtractorConfiguration = DocumentDataExtractorConfiguration()
            val intent = DocumentDataExtractorActivity.newIntent(this, dataExtractorConfiguration)
            startActivityForResult(intent, GENERIC_DOCUMENT_RECOGNIZER_DEFAULT_UI)
        }
        // @EndTag("(DEPRECATED) Start RTU Data Extractor")
    }

    // @Tag("(DEPRECATED) Process Data Extractor Result")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GENERIC_DOCUMENT_RECOGNIZER_DEFAULT_UI) {
            val result: DocumentDataExtractorActivity.Result = DocumentDataExtractorActivity.extractResult(resultCode, data)
            if (!result.resultOk) {
                return
            }

            // Get the list of DocumentDataExtractorResult objects from the intent
            val documentDataExtractorResults = result.result

            val document = documentDataExtractorResults?.first()?.document

            Toast.makeText(
                    this,
                    document?.fields?.map { "${it.type.name} = ${it.value?.text}" }.toString(),
                    Toast.LENGTH_LONG
            ).show()
        }
    }
    // @EndTag("(DEPRECATED) Process Data Extractor Result")
}

fun documentDataExtractorConfigurationSnippet(context: Context) {
    // @Tag("Configure Document Data Extractor RTU UI activity")
    val dataExtractorConfiguration = DocumentDataExtractorConfiguration()

    // Apply the color configuration
    dataExtractorConfiguration.setTopBarButtonsInactiveColor(context.getColor(android.R.color.white))
    dataExtractorConfiguration.setTopBarBackgroundColor(context.getColor(android.R.color.system_primary_dark))
    //...

    // Apply the text configuration
    dataExtractorConfiguration.setClearButtonTitle(context.getString(R.string.clear_button))
    dataExtractorConfiguration.setSubmitButtonTitle(context.getString(R.string.submit_button))
    //...

    // Apply the parameters for fields
    dataExtractorConfiguration.setFieldsDisplayConfiguration(
            hashMapOf(
                    // Use constants from NormalizedFieldNames objects from the corresponding document type
                    DePassport.NormalizedFieldNames.PHOTO to FieldProperties(
                            "My passport photo",
                            FieldProperties.DisplayState.AlwaysVisible
                    ),
                    MRZ.NormalizedFieldNames.CHECK_DIGIT_GENERAL to FieldProperties(
                            "Check digit",
                            FieldProperties.DisplayState.AlwaysVisible
                    )
                    //...
            ))
    // @EndTag("Configure Document Data Extractor RTU UI activity")
}

fun excludeFieldsFromExtractingInConfigSnippet(dataExtractorConfiguration: DocumentDataExtractorConfiguration) {
    // @Tag("Exclude fields from being recognized in the configuration")
    // Exclude some document fields from being recognized
    dataExtractorConfiguration.setExcludedFieldTypes(hashSetOf(
            DeIdCardFront.NormalizedFieldNames.PHOTO,
            DeIdCardFront.NormalizedFieldNames.CARD_ACCESS_NUMBER,
            DePassport.NormalizedFieldNames.PHOTO,
            DePassport.NormalizedFieldNames.SIGNATURE,
            DeIdCardBack.NormalizedFieldNames.EYE_COLOR
    ))
    // @EndTag("Exclude fields from being recognized in the configuration")
}

fun firstGenericDocumentSnippet(documentDataExtractorResults: List<DocumentDataExtractionResult>) {
    // @Tag("Get the first document from the result list")
    val document = documentDataExtractorResults?.first()?.document
    // @EndTag("Get the first document from the result list")
}

fun printDocumentInToastSnippet(context: Context, document: GenericDocument) {
    // @Tag("Show the detected document fields in a Toast notification")
    Toast.makeText(
            context,
            document?.fields?.joinToString("\n") { "${it.type.name} = ${it.value?.text}" } ?: "",
            Toast.LENGTH_LONG
    ).show()
    // @EndTag("Show the detected document fields in a Toast notification")
}

fun getDocumentDataExtractorInstanceFromSdkSnippet(context: Context, cameraView: ScanbotCameraXView) {
    // @Tag("Get DocumentDataExtractor instance and attach it to ScanbotCameraXView")
    val scanbotSdk = ScanbotSDK(context)

    // Please note that each call to this method will create a new instance of DocumentDataExtractor
    // It should be used on a "single instance per screen" basis
    val documentDataExtractor = scanbotSdk.createDocumentDataExtractor()

    // Uncomment to scan only ID cards and passports
    // documentDataExtractor.acceptedDocumentTypes = listOf(
    //     RootDocumentType.DePassport,
    //     RootDocumentType.DeIdCardFront,
    //     RootDocumentType.DeIdCardBack
    // )

    // Uncomment to scan only Driver's licenses
    // documentDataExtractor.acceptedDocumentTypes = listOf(
    //     RootDocumentType.DeDriverLicenseFront,
    //     RootDocumentType.DeDriverLicenseBack
    // )

    // Uncomment to scan only Residence permit cards
    // documentDataExtractor.acceptedDocumentTypes = listOf(
    //     RootDocumentType.DeResidencePermitFront,
    //     RootDocumentType.DeResidencePermitBack
    // )

    // Uncomment to scan only back side of European health insurance cards
    // documentDataExtractor.acceptedDocumentTypes = listOf(
    //     RootDocumentType.EuropeanHealthInsuranceCard
    // )

    // Uncomment to scan only front side of German health insurance cards
    // documentDataExtractor.acceptedDocumentTypes = listOf(
    //     RootDocumentType.RootDocumentType.DeHealthInsuranceCardFront
    // )

    // To scan all the supported document types (default value)
    documentDataExtractor.setConfiguration(DocumentDataExtractorConfigurationBuilder()
        .setAcceptedDocumentTypes(RootDocumentType.ALL_TYPES)
        .build())

    val frameHandler = DocumentDataExtractorFrameHandler.attach(cameraView, documentDataExtractor)
    // @EndTag("Get DocumentDataExtractor instance and attach it to ScanbotCameraXView")
}

fun excludeFieldsFromExtractingSnippet() {
    // @Tag("Exclude fields from being recognized")
    // Exclude some document fields from being recognized
    DocumentDataExtractorConfigurationBuilder()
            //...
            .addExcludedField(DeIdCardFront.NormalizedFieldNames.PHOTO)
            .addExcludedField(DeIdCardFront.NormalizedFieldNames.CARD_ACCESS_NUMBER)
            .addExcludedField(DePassport.NormalizedFieldNames.PHOTO)
            .addExcludedField(DePassport.NormalizedFieldNames.SIGNATURE)
            .addExcludedField(DeIdCardBack.NormalizedFieldNames.EYE_COLOR)
            .build()
    // @EndTag("Exclude fields from being recognized")
}

fun rotateImageSnippet(image: ByteArray, imageOrientation: Int) {
    // @Tag("Rotate image")
    val resultBitmap = ImageProcessor(image).rotate(imageOrientation).processedBitmap()
    // @EndTag("Rotate image")
}

fun extractFromBitmapSnippet(documentDataExtractor: DocumentDataExtractor, resultBitmap: Bitmap) {
    // @Tag("Extract data from the image")
    val recognitionResult = documentDataExtractor.extractFromBitmap(resultBitmap)
    // @EndTag("Extract data from the image")
}

fun handleResultSnippet(activity: AppCompatActivity, recognitionResult: DocumentDataExtractionResult) {
    // @Tag("Set the obtained extraction results to a TextView")
    val myTextView = activity.findViewById<TextView>(R.id.my_text_view)

    val resultsMessage = "Recognition results:\n" +
            "Recognition status: ${recognitionResult.status}\n" +
            "Card type: ${recognitionResult.document?.type}\n" +
            "Number of fields scanned: ${recognitionResult.document?.fields?.size ?: 0}"

    myTextView.text = resultsMessage
    // @EndTag("Set the obtained extraction results to a TextView")
}