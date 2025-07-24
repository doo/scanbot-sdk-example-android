package io.scanbot.example.doc_code_snippet.data_extractor

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.example.R
import io.scanbot.sdk.*
import io.scanbot.sdk.documentdata.*
import io.scanbot.sdk.documentdata.entity.*
import io.scanbot.sdk.process.*
import io.scanbot.sdk.ui.camera.*

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

fun getDocumentDataExtractorInstanceFromSdkSnippet(context: Context, cameraView: ScanbotCameraXView) {
    // @Tag("Get DocumentDataExtractor instance and attach it to ScanbotCameraXView")
    val scanbotSdk = ScanbotSDK(context)

    // Please note that each call to this method will create a new instance of DocumentDataExtractor
    // It should be used on a "single instance per screen" basis
    val documentDataExtractor = scanbotSdk.createDocumentDataExtractor()

    var acceptedDocumentTypes = RootDocumentType.ALL_TYPES

    // Uncomment to scan only ID cards and passports
    // acceptedDocumentTypes = listOf(
    //     RootDocumentType.DePassport,
    //     RootDocumentType.DeIdCardFront,
    //     RootDocumentType.DeIdCardBack
    // )

    // Uncomment to scan only Driver's licenses
    // acceptedDocumentTypes = listOf(
    //     RootDocumentType.DeDriverLicenseFront,
    //     RootDocumentType.DeDriverLicenseBack
    // )

    // Uncomment to scan only Residence permit cards
    // acceptedDocumentTypes = listOf(
    //     RootDocumentType.DeResidencePermitFront,
    //     RootDocumentType.DeResidencePermitBack
    // )

    // Uncomment to scan only back side of European health insurance cards
    // acceptedDocumentTypes = listOf(
    //     RootDocumentType.EuropeanHealthInsuranceCard
    // )

    // Uncomment to scan only front side of German health insurance cards
    // acceptedDocumentTypes = listOf(
    //     RootDocumentType.RootDocumentType.DeHealthInsuranceCardFront
    // )

    // To scan all the supported document types (default value)
    documentDataExtractor.setConfiguration(DocumentDataExtractorConfigurationBuilder()
        .setAcceptedDocumentTypes(acceptedDocumentTypes)
        .build())

    val frameHandler = DocumentDataExtractorFrameHandler.attach(cameraView, documentDataExtractor)
    // @EndTag("Get DocumentDataExtractor instance and attach it to ScanbotCameraXView")
}

fun getDocumentDataExtractorInstanceWithEHICFromSdkSnippet(
    context: Context,
    cameraView: ScanbotCameraXView
) {
    // @Tag("Get DocumentDataExtractor instance with EHIC doc type and attach it to ScanbotCameraXView")
    val scanbotSdk = ScanbotSDK(context)

    // Please note that each call to this method will create a new instance of DocumentDataExtractor
    // It should be used on a "single instance per screen" basis
    val documentDataExtractor = scanbotSdk.createDocumentDataExtractor()

    documentDataExtractor.setConfiguration(
        DocumentDataExtractorConfigurationBuilder()
            .setAcceptedDocumentTypes(
                listOf(
                    RootDocumentType.EuropeanHealthInsuranceCard,
                    RootDocumentType.DeHealthInsuranceCardFront
                )
            )
            .build()
    )

    val frameHandler = DocumentDataExtractorFrameHandler.attach(cameraView, documentDataExtractor)
    // @EndTag("Get DocumentDataExtractor instance with EHIC doc type and attach it to ScanbotCameraXView")
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

fun handleEHICResultSnippet(activity: AppCompatActivity, recognitionResult: DocumentDataExtractionResult) {
    // @Tag("Set the obtained extraction results to a TextView")
    val myTextView = activity.findViewById<TextView>(R.id.my_text_view)

    val ehicDocument = EuropeanHealthInsuranceCard(recognitionResult.document!!)
    
    val resultsMessage = "Recognition results:\n" +
            "Recognition status: ${recognitionResult.status}\n" +
            "Card type: ${recognitionResult.document?.type}\n" +
            "Number of fields scanned: ${recognitionResult.document?.fields?.size ?: 0}"

    myTextView.text = resultsMessage
    // @EndTag("Set the obtained extraction results to a TextView")
}