package io.scanbot.example.doc_code_snippet.mc

import android.app.Application
import android.content.Context
import android.content.Intent
import android.view.Gravity
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.sdk.*
import io.scanbot.sdk.camera.*
import io.scanbot.sdk.mc.*
import io.scanbot.sdk.ui.camera.*
import io.scanbot.sdk.ui.view.mc.*
import io.scanbot.sdk.ui.view.mc.configuration.*

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

fun startMcScannerRTUAndHandleResultSnippet(activity: AppCompatActivity, myButton: Button) {
    // @Tag("Start RTU Medical Certificate Scanner and handle the result")
    val resultLauncher: ActivityResultLauncher<MedicalCertificateScannerConfiguration>

    // ...

    resultLauncher = activity.registerForActivityResult(MedicalCertificateScannerActivity.ResultContract()) { resultEntity: MedicalCertificateScannerActivity.Result ->
        if (resultEntity.resultOk) {
            // Here you can handle and present recognized Medical Certificate information (patient info, the cropped image of the Medical Certificate, all checkboxes and dates)
        }
    }

    // ...

    myButton.setOnClickListener {
        val configuration = MedicalCertificateScannerConfiguration()
        resultLauncher.launch(configuration)
    }
    // @EndTag("Start RTU Medical Certificate Scanner and handle the result")
}

val MEDICAL_CERTIFICATE_REQUEST_CODE_CONSTANT = 1000

fun startMcScannerRTUDeprecatedSnippet(activity: AppCompatActivity, myButton: Button) {
    // @Tag("(DEPRECATED) Start RTU Medical Certificate Scanner")
    myButton.setOnClickListener {
        val configuration = MedicalCertificateScannerConfiguration()
        val intent = MedicalCertificateScannerActivity.newIntent(activity, configuration)
        activity.startActivityForResult(intent, MEDICAL_CERTIFICATE_REQUEST_CODE_CONSTANT)
    }
    // @EndTag("(DEPRECATED) Start RTU Medical Certificate Scanner")
}

fun handleResultDeprecatedSnippet(requestCode: Int, resultCode: Int, data: Intent?) {
    // @Tag("(DEPRECATED) Handle RTU Medical Certificate Scanner result")
    if (requestCode == MEDICAL_CERTIFICATE_REQUEST_CODE_CONSTANT) {
        val result: MedicalCertificateScannerActivity.Result = MedicalCertificateScannerActivity.extractResult(resultCode, data)
        if (result.resultOk) {
            // Here you can handle and present recognized Medical Certificate information (patient info, the cropped image of the Medical Certificate, all checkboxes and dates)
        }
    }
    // @EndTag("(DEPRECATED) Handle RTU Medical Certificate Scanner result")
}

fun setMcScannerRTUConfigurationSnippet() {
    // @Tag("Set RTU Medical Certificate Scanner configuration")
    val configuration = MedicalCertificateScannerConfiguration()
    configuration.setScanPatientInfo(true)
    configuration.setReturnCroppedDocumentImage(true)

    configuration.setFlashEnabled(false)
    configuration.setCancelButtonTitle("Stop")
    // @EndTag("Set RTU Medical Certificate Scanner configuration")
}

fun handleMcScannerRTUResultSnippet(context: Context) {
    // @Tag("Handle RTU Medical Certificate Scanner result")
    lateinit var result: MedicalCertificateScanningResult

    val medicalCertificateContent: String = StringBuilder()
            .append("Type: ").append(if (result.checkBoxes
                            ?.find { checkBox -> checkBox.type == MedicalCertificateCheckBoxType.INITIAL_CERTIFICATE }
                            ?.checked == true)
                "Initial"
            else if (result.checkBoxes
                            ?.find { checkBox -> checkBox.type == MedicalCertificateCheckBoxType.RENEWED_CERTIFICATE }
                            ?.checked == true)
                "Renewed"
            else
                "Unknown").append("\n")
            .append("Work Accident: ").append(if (result.checkBoxes
                            ?.find { checkBox -> checkBox.type == MedicalCertificateCheckBoxType.WORK_ACCIDENT }
                            ?.checked == true)
                "Yes"
            else "No").append("\n")
            .append("Accident Consultant: ").append(
                    if (result.checkBoxes
                                    ?.find { checkBox -> checkBox.type == MedicalCertificateCheckBoxType.ASSIGNED_TO_ACCIDENT_INSURANCE_DOCTOR }
                                    ?.checked == true)
                        "Yes"
                    else "No"
            ).append("\n")
            .append("Start Date: ").append(
                    result.dates?.find { dateRecord -> dateRecord.type == MedicalCertificateDateRecordType.INCAPABLE_OF_WORK_SINCE }?.value
            ).append("\n")
            .append("End Date: ").append(
                    result.dates?.find { dateRecord -> dateRecord.type == MedicalCertificateDateRecordType.INCAPABLE_OF_WORK_UNTIL }?.value
            ).append("\n")
            .append("Issue Date: ").append(
                    result.dates?.find { dateRecord -> dateRecord.type == MedicalCertificateDateRecordType.DIAGNOSED_ON }?.value
            )
            .append("\n")
            .append("Form type: ${result.formType.name}")
            .append("\n")
            .append(result.patientInfoBox.fields.joinToString(separator = "\n", prefix = "\n") { "${it.type.name}: ${it.value}" })
            .toString()

    Toast.makeText(context, medicalCertificateContent, Toast.LENGTH_LONG).show()
    // @EndTag("Handle RTU Medical Certificate Scanner result")
}

fun useMcScannerFrameHandlerSnippet(context: Context, cameraView: ScanbotCameraXView) {
    // @Tag("Attach Medical Certificate Scanner to ScanbotCameraXView")
    val scanbotSDK = ScanbotSDK(context)
    val medicalCertificateScanner: MedicalCertificateScanner = scanbotSDK.createMedicalCertificateScanner()

    // Attach `FrameHandler`, that will detect a Medical Certificate document in the camera frames
    val frameHandler: MedicalCertificateFrameHandler = MedicalCertificateFrameHandler.attach(cameraView, medicalCertificateScanner)

    // Attach `AutoSnappingController`, that will trigger the snap as soon as `FrameHandler` detects a Medical Certificate document in the preview frame successfully
    val autoSnappingController = MedicalCertificateAutoSnappingController.attach(cameraView, frameHandler)
    // @EndTag("Attach Medical Certificate Scanner to ScanbotCameraXView")
}

fun addSnappingResultListenerSnippet(cameraView: ScanbotCameraXView) {
    // @Tag("Add snapping result listener")
    cameraView.addPictureCallback(object : PictureCallback() {
        override fun onPictureTaken(image: ByteArray, captureInfo: CaptureInfo) {
            // processPictureTaken(image, captureInfo.imageOrientation)
        }
    })
    // @EndTag("Add snapping result listener")
}

fun processTakenPictureSnippet(image: ByteArray, imageOrientation: Int, medicalCertificateScanner: MedicalCertificateScanner, activity: AppCompatActivity) {
    // @Tag("Process the taken picture")
    // Here we get the full image from the camera.
    // Implement a suitable async(!) detection and image handling here.

    // Run Medical Certificate recognition on the snapped image:
    val resultInfo = medicalCertificateScanner.scanFromJpeg(image,
            0)

    if (resultInfo != null && resultInfo.scanningSuccessful) {
        // Here you can handle and present recognized Medical Certificate information (patient info, the cropped image of the Medical Certificate, all checkboxes and dates)
    } else {
        // If recognition was not successful - show a warning and try again
        activity.runOnUiThread {
            val toast = Toast.makeText(activity, "No Medical Certificate content was recognized!", Toast.LENGTH_LONG)
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()
        }
    }
    // @EndTag("Process the taken picture")
}

