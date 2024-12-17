package io.scanbot.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.example.databinding.ActivityScannerBinding
import io.scanbot.sdk.AspectRatio
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.camera.CameraPreviewMode
import io.scanbot.sdk.camera.FrameHandlerResult
import io.scanbot.sdk.creditcard.CreditCardScanner
import io.scanbot.sdk.creditcard.CreditCardScannerFrameHandler

class ScannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScannerBinding

    private lateinit var scanner: CreditCardScanner

    private var useFlash = false

    private lateinit var frameHandler: CreditCardScannerFrameHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        scanner = ScanbotSDK(this).createCreditCardScanner()

        binding.finderOverlay.setRequiredAspectRatios(listOf(AspectRatio(1.586, 1.0))) // standard credit card aspect ratio
//        binding.finderOverlay.setFixedFinderHeight( // TODO: needed?
//                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
//                        70f, resources.displayMetrics).toInt()
//        )

        binding.cameraView.setPreviewMode(CameraPreviewMode.FIT_IN)

        frameHandler = CreditCardScannerFrameHandler.attach(binding.cameraView, scanner)

        frameHandler.addResultHandler { result ->
            val resultText: String = when (result) {
                is FrameHandlerResult.Success -> {
                    if (result.value.validationSuccessful) {
                        result.value.rawString
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
}
