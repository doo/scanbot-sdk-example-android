package io.scanbot.example.doc_code_snippet.data_extractor

/*
    NOTE: this snippet of code is to be used only as a part of the website documentation.
    This code is not intended for any use outside of the support of documentation by Scanbot SDK GmbH employees.
*/

// NOTE for maintainers: whenever changing this code,
// ensure that links using it are still pointing to valid lines!
// Pay attention to imports adding/removal/sorting!
// Page URLs using this code:
// TODO: add URLs here

import io.scanbot.sdk.documentdata.entity.*
import io.scanbot.sdk.genericdocument.entity.*

fun wrapGenericDocument(genericDocument: GenericDocument) {

    // @Tag("Manual data parsing snippet")
    when ( genericDocument.type.name) {
         DeIdCardFront.DOCUMENT_TYPE -> {
            val wrapper = DeIdCardFront(genericDocument)
            val id = wrapper.id
            val name = wrapper.givenNames
            val surname = wrapper.surname
            val cardAccessNumber = wrapper.cardAccessNumber
        }
        DeIdCardBack.DOCUMENT_TYPE  -> {
            // Handle ID card back
        }
        DePassport.DOCUMENT_TYPE  -> {
            // Handle passport
        }
        DeDriverLicenseFront.DOCUMENT_TYPE  -> {
            // Handle driver license front
        }
        DeDriverLicenseBack.DOCUMENT_TYPE  -> {
            // Handle driver license back
        }
        DeResidencePermitFront.DOCUMENT_TYPE  -> {
            // Handle residence permit front
        }
        DeResidencePermitBack.DOCUMENT_TYPE  -> {
            // Handle residence permit back
        }
        DeHealthInsuranceCardFront.DOCUMENT_TYPE  -> {
            // Handle health insurance card front
        }
        EuropeanHealthInsuranceCard.DOCUMENT_TYPE  -> {
            // Handle European health insurance card back
        }
        else -> {
            // Handle other document types
        }
    }
    // @EndTag("Manual data parsing snippet")
}


