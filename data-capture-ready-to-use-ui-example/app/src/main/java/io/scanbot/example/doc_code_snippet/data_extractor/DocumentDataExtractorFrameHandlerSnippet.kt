package io.scanbot.example.doc_code_snippet.data_extractor

/*
    NOTE: this snippet of code is to be used only as a part of the website documentation.
    This code is not intended for any use outside of the support of documentation by Scanbot SDK GmbH employees.
*/

// NOTE for maintainers: whenever changing this code,
// ensure that links using it are still pointing to valid lines!
// Pay attention to imports adding/removal/sorting!

import android.content.Context
import android.widget.Toast
import io.scanbot.common.Result
import io.scanbot.common.onFailure
import io.scanbot.common.onSuccess
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.camera.FrameHandler
import io.scanbot.sdk.documentdata.*

fun useDocumentDataExtractorFrameHandler(context: Context) {
    // @Tag("Add a frame handler for DocumentDataExtractor")
    val dataExtractor = ScanbotSDK(context).createDocumentDataExtractor().getOrThrow()
    val frameHandler = DocumentDataExtractorFrameHandler(dataExtractor)

    frameHandler.addResultHandler(object : DocumentDataExtractorFrameHandler.ResultHandler {

        override fun handle(
            result: Result<DocumentDataExtractionResult>,
            frame: FrameHandler.Frame
        ): Boolean {
            result.onSuccess {
                // NOTE: 'handle' method runs in background thread
                //   - don't forget to switch to main before touching any Views
                Toast.makeText(
                    context,
                    "Document found!\nYou can now snap picture.",
                    Toast.LENGTH_SHORT
                ).show()
            }.onFailure {
                // Handle Failure case
            }
            return false
        }
    })
    // @EndTag("Add a frame handler for DocumentDataExtractor")
}
