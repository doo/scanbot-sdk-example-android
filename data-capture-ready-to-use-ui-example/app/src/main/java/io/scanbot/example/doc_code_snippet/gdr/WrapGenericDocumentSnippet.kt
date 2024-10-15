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

import io.scanbot.genericdocument.entity.DeDriverLicenseBack
import io.scanbot.genericdocument.entity.DeDriverLicenseFront
import io.scanbot.genericdocument.entity.DeHealthInsuranceCardFront
import io.scanbot.genericdocument.entity.DeIdCardBack
import io.scanbot.genericdocument.entity.DeIdCardFront
import io.scanbot.genericdocument.entity.DePassport
import io.scanbot.genericdocument.entity.DeResidencePermitBack
import io.scanbot.genericdocument.entity.DeResidencePermitFront
import io.scanbot.genericdocument.entity.EuropeanHealthInsuranceCard
import io.scanbot.genericdocument.entity.GenericDocument
import io.scanbot.genericdocument.entity.GenericDocumentLibrary.wrap

fun wrapGenericDocument(genericDocument: GenericDocument) {
    // Alternatively, use GenericDocumentLibrary.wrapperFromGenericDocument(genericDocument)
    when (val wrapper = genericDocument.wrap()) {
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


