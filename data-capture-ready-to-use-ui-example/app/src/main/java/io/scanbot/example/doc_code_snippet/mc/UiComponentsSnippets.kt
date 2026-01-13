package io.scanbot.example.doc_code_snippet.mc

import android.app.Application
import android.content.Context
import android.view.Gravity
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.common.onFailure
import io.scanbot.common.onSuccess
import io.scanbot.sdk.*
import io.scanbot.sdk.camera.*
import io.scanbot.sdk.image.*
import io.scanbot.sdk.mc.*
import io.scanbot.sdk.medicalcertificate.*
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
        .prepareOCRLanguagesBlobs(true)
        //...
        .initialize(application)
    // @EndTag("Initialize SDK")
}

fun useMcScannerFrameHandlerSnippet(context: Context, cameraView: ScanbotCameraXView) {
    // @Tag("Attach Medical Certificate Scanner to ScanbotCameraXView")
    val scanbotSDK = ScanbotSDK(context)
    val medicalCertificateScanner: MedicalCertificateScanner = scanbotSDK.createMedicalCertificateScanner().getOrThrow()

    // Attach `FrameHandler`, that will detect a Medical Certificate document in the camera frames
    val frameHandler: MedicalCertificateFrameHandler = MedicalCertificateFrameHandler.attach(cameraView, medicalCertificateScanner)

    // Attach `AutoSnappingController`, that will trigger the snap as soon as `FrameHandler` detects a Medical Certificate document in the preview frame successfully
    val autoSnappingController = MedicalCertificateAutoSnappingController.attach(cameraView, frameHandler)
    // @EndTag("Attach Medical Certificate Scanner to ScanbotCameraXView")
}

fun addSnappingResultListenerSnippet(cameraView: ScanbotCameraXView) {
    // @Tag("Add snapping result listener")
    cameraView.addPictureCallback(object : PictureCallback() {
        override fun onPictureTaken(image: ImageRef, captureInfo: CaptureInfo) {
            // processPictureTaken(image, captureInfo.imageOrientation)
        }
    })
    // @EndTag("Add snapping result listener")
}

fun processTakenPictureSnippet(image: ImageRef, medicalCertificateScanner: MedicalCertificateScanner, activity: AppCompatActivity) {
    // @Tag("Process the taken picture")
    // Here we get the full image from the camera.
    // Implement a suitable async(!) detection and image handling here.

    // Run Medical Certificate recognition on the snapped image:
    val scanningResult = medicalCertificateScanner.run(image, MedicalCertificateScanningParameters())

    scanningResult.onSuccess { scanningResult ->
        if (scanningResult.scanningSuccessful) {
            // Here you can handle and present recognized Medical Certificate information (patient info, the cropped image of the Medical Certificate, all checkboxes and dates)
        } else {
            // If recognition was not successful - show a warning and try again
            activity.runOnUiThread {
                val toast = Toast.makeText(activity, "No Medical Certificate content was recognized!", Toast.LENGTH_LONG)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            }
        }
    }.onFailure { error ->
        // Here you can handle the error
    }
    // @EndTag("Process the taken picture")
}


