package com.example.scanbot.doc_code_snippet


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.core.ImageRotation
import io.scanbot.sdk.imagefilters.BrightnessFilter
import io.scanbot.sdk.imagefilters.OutputMode
import io.scanbot.sdk.imagefilters.ScanbotBinarizationFilter


class PageFilterSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startFiltering()
    }

    private val scanbotSDK = ScanbotSDK(this@PageFilterSnippet)

    fun startFiltering() {
        // @Tag("Processing Scanned Page")
        // Retrieve the scanned document
        val document = scanbotSDK.documentApi.loadDocument("SOME_SAVED_UUID")

        // Retrieve the selected document page.
        document?.pages?.forEach { page ->
            // Create the instances of the filters you want to apply.
            val filter1 = ScanbotBinarizationFilter(outputMode = OutputMode.ANTIALIASED)
            val filter2 = BrightnessFilter(brightness = 0.4)

            // Apply rotation on the page, and you can also pass the filters here if you want
            page.apply(
                newImageRotation = ImageRotation.CLOCKWISE_90,
                newFilters = listOf(filter1, filter2)
            )

            // Or you can also set the filters separately
            // Set the filters
            page.filters = listOf(filter1, filter2)
        }
        // @EndTag("Processing Scanned Page")
    }
}

