package io.scanbot.example.doc_code_snippet.mc

/*
    NOTE: this snippet of code is to be used only as a part of the website documentation.
    This code is not intended for any use outside of the support of documentation by Scanbot SDK GmbH employees.
*/

// NOTE for maintainers: whenever changing this code,
// ensure that links using it are still pointing to valid lines!
// Pay attention to imports adding/removal/sorting!
// Page URLs using this code:
// TODO: add URLs here

import android.graphics.Bitmap
import io.scanbot.sdk.mc.*


fun processImage(
    medicalCertificateScanner: MedicalCertificateScanner,
    bitmap: Bitmap
) {
    val result = medicalCertificateScanner.scanFromBitmap(
        bitmap,
        0,
        parameters = MedicalCertificateScanningParameters(
            shouldCropDocument = true,
            extractCroppedImage = true,
            recognizePatientInfoBox = true,
            recognizeBarcode = true
        )
    )
    if (result != null && result.scanningSuccessful) {
        // Document scanning results are processed
        // processResul
    }
}

