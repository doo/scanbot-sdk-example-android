package io.scanbot.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.example.databinding.ActivityScannerBinding
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.camera.CameraPreviewMode
import io.scanbot.sdk.camera.FrameHandlerResult
import io.scanbot.sdk.common.AspectRatio
import io.scanbot.sdk.creditcard.CreditCardScanner
import io.scanbot.sdk.creditcard.CreditCardScannerFrameHandler
import io.scanbot.sdk.creditcard.CreditCardScanningStatus
import io.scanbot.sdk.creditcard.entity.CreditCard

class ScannerActivity : AppCompatActivity() {
    // @Tag("Credit Card Classic Camera")
    private lateinit var binding: ActivityScannerBinding

    private lateinit var scanner: CreditCardScanner

    private var useFlash = false

    private lateinit var frameHandler: CreditCardScannerFrameHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // init scanbot sdk and create credit card scanner
        scanner = ScanbotSDK(this).createCreditCardScanner()
        // set aspect ration for finder overlay
        binding.finderOverlay.setRequiredAspectRatios(listOf(AspectRatio(1.586, 1.0))) // standard credit card aspect ratio

        // set camera preview mode to fit in the camera container
        binding.cameraView.setPreviewMode(CameraPreviewMode.FIT_IN)

        // attach scanner to the camera view
        frameHandler = CreditCardScannerFrameHandler.attach(binding.cameraView, scanner)
        // handle live credit card scanning results
        frameHandler.addResultHandler { result ->
            val resultText: String = when (result) {
                is FrameHandlerResult.Success -> {
                    if (result.value.scanningStatus == CreditCardScanningStatus.SUCCESS ||
                        result.value.scanningStatus == CreditCardScanningStatus.INCOMPLETE
                    ) {
                        CreditCard(result.value.creditCard!!).cardNumber.value.text
                    } else {
                        "Credit card not found"
                    }
                }

                is FrameHandlerResult.Failure -> "Check your setup or license"
            }

            // NOTE: 'handle' method runs in background thread - don't forget to switch to main before touching any Views
            runOnUiThread { binding.resultTextView.text = resultText }

            false
        }

        // configure camera view
        binding.cameraView.setCameraOpenCallback {
            binding.cameraView.useFlash(useFlash)
            binding.cameraView.continuousFocus()
        }
        binding.flashButton.setOnClickListener { toggleFlash() }
    }

    private fun toggleFlash() {
        useFlash = !useFlash
        binding.cameraView.useFlash(useFlash)
    }
    // @Tag("Credit Card Classic Camera")
}
