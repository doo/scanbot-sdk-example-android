package io.scanbot.example

import android.content.Context
import android.graphics.RectF
import android.os.Parcel
import android.os.Parcelable
import io.scanbot.mrzscanner.model.MRZRecognitionResult
import io.scanbot.sdk.barcode.entity.BarcodeFormat
import io.scanbot.sdk.ui.entity.workflow.*
import net.doo.snap.camera.PreviewBuffer
import net.doo.snap.lib.detector.PageAspectRatio

/**
 * Provides predefined Workflow configurations
 */
class WorkflowFactory {

    companion object {
        fun scanMRZAndSnap(): Workflow {
            val ratios = listOf(
                    PageAspectRatio(85.0, 54.0), // ID card
                    PageAspectRatio(125.0, 88.0) // Passport
            )
            val steps = listOf(
                    ScanMachineReadableZoneWorkflowStep(
                            title = "Scan ID card or passport",
                            message = "Please align your ID card or passport in the frame.",
                            requiredAspectRatios = ratios,
                            wantsCapturedPage = true,
                            workflowStepValidation = object : WorkflowStep.WorkflowStepValidationHandler {
                                override fun invoke(stepResult: WorkflowStepResult): WorkflowStepError? {
                                    return if (stepResult.mrzResult == null
                                            || !stepResult.mrzResult!!.recognitionSuccessful
                                            || stepResult.mrzResult!!.errorCode != MRZRecognitionResult.NO_ERROR) {
                                        WorkflowStepError(
                                                1,
                                                "This does not seem to be the correct page.",
                                                WorkflowStepError.ShowMode.TOAST)
                                    } else {
                                        null
                                    }
                                }
                            }
                    )
            )
            return Workflow(steps, "Scanning MRZ Code & Front Side")
        }

        fun scanMRZAndFrontBackSnap(): Workflow {
            val ratios = listOf(PageAspectRatio(85.0, 54.0)) // ID card
            val steps = listOf(
                    ScanDocumentPageWorkflowStep(
                            "Scan 1/2",
                            "Please scan the front of your ID card.",
                            ratios
                    ),
                    ScanMachineReadableZoneWorkflowStep(
                            title = "Scan 2/2",
                            message = "Please scan the back of your ID card.",
                            requiredAspectRatios = ratios,
                            wantsCapturedPage = true,
                            workflowStepValidation = object : WorkflowStep.WorkflowStepValidationHandler {
                                override fun invoke(stepResult: WorkflowStepResult): WorkflowStepError? {
                                    return if (stepResult.mrzResult == null
                                            || !stepResult.mrzResult!!.recognitionSuccessful
                                            || stepResult.mrzResult!!.errorCode != MRZRecognitionResult.NO_ERROR) {
                                        WorkflowStepError(
                                                1,
                                                "This does not seem to be the correct side. Please scan the back with MRZ.",
                                                WorkflowStepError.ShowMode.DIALOG)
                                    } else {
                                        null
                                    }
                                }
                            }
                    )
            )
            return Workflow(steps, "Scanning MRZ Code & 2 Sides")
        }

        fun disabilityCertificate(): Workflow {
            val ratios = listOf(
                    PageAspectRatio(148.0, 210.0), // DC form A5 portrait (e.g. white sheet, AUB Muster 1b/E (1/2018))
                    PageAspectRatio(148.0, 105.0)  // DC form A6 landscape (e.g. yellow sheet, AUB Muster 1b (1.2018))
            )
            val steps = listOf(
                    ScanDisabilityCertificateWorkflowStep(
                            message = "Please align the DC form in the frame.",
                            requiredAspectRatios = ratios,
                            wantsCapturedPage = true,
                            workflowStepValidation = object : WorkflowStep.WorkflowStepValidationHandler {
                                override fun invoke(stepResult: WorkflowStepResult): WorkflowStepError? {
                                    return if (stepResult.disabilityCertificateResult == null
                                            || !stepResult.disabilityCertificateResult!!.recognitionSuccessful) {
                                        WorkflowStepError(
                                                1,
                                                "This does not seem to be the correct page.",
                                                WorkflowStepError.ShowMode.TOAST)
                                    } else {
                                        null
                                    }
                                }
                            }
                    )
            )
            return Workflow(steps, "Disability Certificate")
        }

        fun barcodeAndDocumentImage(): Workflow {
            val steps = listOf(
                    ScanBarCodeWorkflowStep(
                            title = "Step 1/2 - QR-/Barcode",
                            message = "Please scan a barcode or a QR code.",
                            acceptedCodeTypes = listOf(BarcodeFormat.ALL_FORMATS),
                            finderViewSize = FinderViewSize(1.0, 0.6),
                            workflowStepValidation = object : WorkflowStep.WorkflowStepValidationHandler {
                                override fun invoke(stepResult: WorkflowStepResult): WorkflowStepError? {
                                    return if (stepResult.barcodeResults == null
                                            || stepResult.barcodeResults!!.isEmpty()
                                            || stepResult.barcodeResults!!.none { barcodeScanningResult -> barcodeScanningResult.errorCode == MRZRecognitionResult.NO_ERROR }) {
                                        WorkflowStepError(
                                                1,
                                                "No barcode detected.",
                                                WorkflowStepError.ShowMode.TOAST)
                                    } else if (stepResult.barcodeResults!!.none { barcodeScanningResult ->
                                                barcodeScanningResult!!.barcodeFormat != BarcodeFormat.QR_CODE
                                                        && barcodeScanningResult!!.barcodeFormat != BarcodeFormat.DATA_MATRIX
                                                        && barcodeScanningResult!!.barcodeFormat != BarcodeFormat.AZTEC
                                                        && barcodeScanningResult!!.barcodeFormat != BarcodeFormat.UNKNOWN
                                            }) {
                                        WorkflowStepError(
                                                2,
                                                "No valid barcode detected.",
                                                WorkflowStepError.ShowMode.TOAST)
                                    } else {
                                        null
                                    }
                                }
                            }
                    ),
                    ScanDocumentPageWorkflowStep(
                            title = "Step 2/2 - A4 Document",
                            message = "Please align an A4 document in the frame.",
                            requiredAspectRatios = listOf(PageAspectRatio(210.0, 297.0))
                    )
            )
            return Workflow(steps, "Barcode + Document Image")
        }

        fun payFormWithClassicalDocPolygonDetection(): Workflow {
            val steps = listOf(
                    ScanPayFormWorkflowStep(
                            message = "Please scan a SEPA PayForm",
                            wantsCapturedPage = true,
                            workflowStepValidation = object : WorkflowStep.WorkflowStepValidationHandler {
                                override fun invoke(stepResult: WorkflowStepResult): WorkflowStepError? {
                                    return if (stepResult.payformResult == null
                                            || stepResult.payformResult?.payformFields.isNullOrEmpty()) {
                                        WorkflowStepError(
                                                1,
                                                "No PayForm data detected. Please try again.",
                                                WorkflowStepError.ShowMode.TOAST)
                                    } else {
                                        null
                                    }
                                }
                            }
                    )
            )
            return Workflow(steps, "PayForm - Polygon Doc")
        }
    }

    /**
     * Your own custom [Workflow] with custom [TestStep]s and [TestScanner]
     */
    fun customWorkflow(): Workflow {
        val steps = listOf(
                TestStep(
                        message = "Custom Workflow"
                )
        )
        return Workflow(steps, "Custom Workflow")
    }

    class TestScanner(context: Context, step: WorkflowStep) : WorkflowScanner(context, step) {
        override fun scanOnCameraFrame(previewFrame: PreviewBuffer.FrameHandler.Frame): WorkflowStepResult {
            // implement scanning logic here
            return WorkflowStepResult(step)
        }

        override fun scanOnCapturedImage(image: ByteArray, imageOrientation: Int, finderRectF: RectF?): WorkflowStepResult {
            // implement scanning logic here
            return WorkflowStepResult(step)
        }
    }

    class TestStep(override val title: String = "",
                   override val message: String = "",
                   override val requiredAspectRatios: List<PageAspectRatio> = emptyList(),
                   override val wantsCapturedPage: Boolean = false,
                   override val wantsVideoFramePage: Boolean = false,
                   override val workflowStepValidation: WorkflowStep.WorkflowStepValidationHandler = object : WorkflowStep.WorkflowStepValidationHandler {
                       override fun invoke(result: WorkflowStepResult): WorkflowStepError? {
                           return null
                       }
                   }
    ) : WorkflowStep(title, message, requiredAspectRatios, wantsCapturedPage, wantsVideoFramePage, workflowStepValidation) {
        constructor(parcel: Parcel) : this(
                "",
                "",
                emptyList(),
                false,
                false,
                object : WorkflowStep.WorkflowStepValidationHandler {
                    override fun invoke(result: WorkflowStepResult): WorkflowStepError? {
                        return null
                    }
                }
        )

        override fun writeToParcel(dest: Parcel?, flags: Int) { }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<TestStep> {
            override fun createFromParcel(parcel: Parcel): TestStep {
                return TestStep(parcel)
            }

            override fun newArray(size: Int): Array<TestStep?> {
                return arrayOfNulls(size)
            }
        }

    }

}

