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
    // @Tag("Check Scanner Result Wrapper")
    when ( genericDocument.type.name) {
         USACheck.DOCUMENT_TYPE -> {
            val wrapper = USACheck(genericDocument)
            val number = wrapper.accountNumber
        }
        FRACheck.DOCUMENT_TYPE  -> {
            // Handle FRACheck
        }
        KWTCheck.DOCUMENT_TYPE  -> {
            // Handle KWTCheck
        }
        AUSCheck.DOCUMENT_TYPE  -> {
            // Handle AUSCheck
        }
        INDCheck.DOCUMENT_TYPE  -> {
            // Handle INDCheck
        }
        ISRCheck.DOCUMENT_TYPE  -> {
            // Handle ISRCheck
        }
        UAECheck.DOCUMENT_TYPE  -> {
            // Handle UAECheck
        }
        CANCheck.DOCUMENT_TYPE  -> {
            // Handle CANCheck
        }
        else -> {
            // Handle other document types
        }
    }
    // @EndTag("Check Scanner Result Wrapper")
}


