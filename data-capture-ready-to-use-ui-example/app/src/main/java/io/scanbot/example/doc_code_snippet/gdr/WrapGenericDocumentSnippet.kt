package io.scanbot.example.doc_code_snippet.gdr

/*
    NOTE: this snippet of code is to be used only as a part of the website documentation.
    This code is not intended for any use outside of the support of documentation by Scanbot SDK GmbH employees.
*/

// NOTE for maintainers: whenever changing this code,
// ensure that links using it are still pointing to valid lines!
// Pay attention to imports adding/removal/sorting!
// Page URLs using this code:
// TODO: add URLs here

import io.scanbot.genericdocument.entity.GenericDocumentWrapper
import io.scanbot.sdk.documentdata.entity.*
import io.scanbot.sdk.genericdocument.entity.*

fun wrapGenericDocument(genericDocument: GenericDocument) {
    // Alternatively, use GenericDocumentLibrary.wrapperFromGenericDocument(genericDocument)
    when (val wrapper = GenericDocumentWrapper(genericDocument)) {
        is DeIdCardFront -> {
            val id = wrapper.id
            val name = wrapper.givenNames
            val surname = wrapper.surname
            val cardAccessNumber = wrapper.cardAccessNumber
        }
        is DeIdCardBack -> {
            // Handle ID card back
        }
        is DePassport -> {
            // Handle passport
        }
        is DeDriverLicenseFront -> {
            // Handle driver license front
        }
        is DeDriverLicenseBack -> {
            // Handle driver license back
        }
        is DeResidencePermitFront -> {
            // Handle residence permit front
        }
        is DeResidencePermitBack -> {
            // Handle residence permit back
        }
        is DeHealthInsuranceCardFront -> {
            // Handle health insurance card front
        }
        is EuropeanHealthInsuranceCard -> {
            // Handle European health insurance card back
        }
        else -> {
            // Handle other document types
        }
    }
}


