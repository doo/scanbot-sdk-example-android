package io.scanbot.example.doc_code_snippet.cheque

/*
    NOTE: this snippet of code is to be used only as a part of the website documentation.
    This code is not intended for any use outside of the support of documentation by Scanbot SDK GmbH employees.
*/

// NOTE for maintainers: whenever changing this code,
// ensure that links using it are still pointing to valid lines!
// Pay attention to imports adding/removal/sorting!
// Page URLs using this code:
// TODO: add URLs here

import io.scanbot.sdk.check.entity.*
import io.scanbot.sdk.genericdocument.entity.*

fun wrapCheck(genericDocument: GenericDocument) {

    when ( genericDocument.type.name) {
         USACheck.DOCUMENT_TYPE -> {
            val wrapper = FRACheck(genericDocument)
            val number = wrapper.chequeNumber
        }
        FRACheck.DOCUMENT_TYPE  -> {
            // Handle ID card back
        }
        KWTCheck.DOCUMENT_TYPE  -> {
            // Handle passport
        }
        AUSCheck.DOCUMENT_TYPE  -> {
            // Handle driver license front
        }
        INDCheck.DOCUMENT_TYPE  -> {
            // Handle driver license back
        }
        ISRCheck.DOCUMENT_TYPE  -> {
            // Handle residence permit front
        }
        UAECheck.DOCUMENT_TYPE  -> {
            // Handle residence permit back
        }
        CANCheck.DOCUMENT_TYPE  -> {
            // Handle health insurance card front
        }
        else -> {
            // Handle other document types
        }
    }
}


