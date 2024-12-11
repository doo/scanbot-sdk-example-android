package io.scanbot.example.doc_code_snippet.gdr

/*
    NOTE: this snippet of code is to be used only as a part of the website documentation.
    This code is not intended for any use outside of the support of documentation by Scanbot SDK GmbH employees.
*/

// NOTE for maintainers: whenever changing this code,
// ensure that links using it are still pointing to valid lines!
// Pay attention to imports adding/removal/sorting!

import android.content.Context
import android.widget.Toast
import io.scanbot.genericdocument.GenericDocumentRecognitionResult
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.SdkLicenseError
import io.scanbot.sdk.camera.FrameHandlerResult
import io.scanbot.sdk.genericdocument.GenericDocumentRecognizerFrameHandler

fun useGenericDocumentRecognizerFrameHandler(context: Context) {
    val genericDocumentRecognizer = ScanbotSDK(context).createGenericDocumentRecognizer()
    val frameHandler = GenericDocumentRecognizerFrameHandler(genericDocumentRecognizer)

    frameHandler.addResultHandler(object : GenericDocumentRecognizerFrameHandler.ResultHandler {

        override fun handle(result: FrameHandlerResult<GenericDocumentRecognitionResult, SdkLicenseError>): Boolean {
            val isSuccess = result is FrameHandlerResult.Success
            when {
                isSuccess -> {
                    // NOTE: 'handle' method runs in background thread
                    //   - don't forget to switch to main before touching any Views
                    Toast.makeText(
                        context,
                        "Document found!\nYou can now snap picture.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {} // Handle Failure case
            }
            return false
        }
    })
}
