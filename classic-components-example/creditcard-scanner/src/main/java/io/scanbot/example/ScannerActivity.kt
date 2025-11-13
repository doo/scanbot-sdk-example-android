package io.scanbot.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.common.mapFailure
import io.scanbot.common.mapSuccess
import io.scanbot.common.onSuccess

import io.scanbot.example.common.applyEdgeToEdge
import io.scanbot.example.databinding.ActivityScannerBinding
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.camera.CameraPreviewMode
import io.scanbot.sdk.creditcard.CreditCardScanner
import io.scanbot.sdk.creditcard.CreditCardScannerFrameHandler
import io.scanbot.sdk.creditcard.CreditCardScanningStatus
import io.scanbot.sdk.creditcard.entity.CreditCard
import io.scanbot.sdk.geometry.AspectRatio

class ScannerActivity : AppCompatActivity() {
    // @Tag("Credit Card Classic Camera")
    private lateinit var binding: ActivityScannerBinding


    private var useFlash = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.hide()
        applyEdgeToEdge(this.findViewById(R.id.root_view))

        // init scanbot sdk and create credit card scanner
        ScanbotSDK(this).createCreditCardScanner().onSuccess { scanner ->
            // attach scanner to the camera view
            val frameHandler = CreditCardScannerFrameHandler.attach(binding.cameraView, scanner)
            // handle live credit card scanning results
            frameHandler.addResultHandler { result, frame ->
                val resultText: String = result.mapSuccess { value ->
                    if (value.scanningStatus == CreditCardScanningStatus.SUCCESS ||
                        value.scanningStatus == CreditCardScanningStatus.INCOMPLETE
                    ) {
                        CreditCard(value.creditCard!!).cardNumber.value.text
                    } else {
                        "Credit card not found"
                    }
                }.mapFailure {
                  it.message ?: "Unknown error"
                }.getOrNull() ?: ""

                // NOTE: 'handle' method runs in background thread - don't forget to switch to main before touching any Views
                runOnUiThread { binding.resultTextView.text = resultText }

                false
            }
        }
        // set aspect ration for finder overlay
        binding.finderOverlay.setRequiredAspectRatios(
            listOf(
                AspectRatio(
                    1.586,
                    1.0
                )
            )
        ) // standard credit card aspect ratio

        // set camera preview mode to fit in the camera container
        binding.cameraView.setPreviewMode(CameraPreviewMode.FIT_IN)


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
    // @EndTag("Credit Card Classic Camera")
}
