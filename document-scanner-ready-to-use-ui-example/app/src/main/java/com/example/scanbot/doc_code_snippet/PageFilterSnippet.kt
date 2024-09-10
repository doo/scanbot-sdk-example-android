package com.example.scanbot.doc_code_snippet


import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.scanbot.utils.getUrisFromGalleryResult
import com.example.scanbot.utils.toBitmap
import io.scanbot.page.PageImageSource
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.core.processor.ImageProcessor
import io.scanbot.sdk.docprocessing.Document
import io.scanbot.sdk.imagefilters.BrightnessFilter
import io.scanbot.sdk.imagefilters.OutputMode
import io.scanbot.sdk.imagefilters.ScanbotBinarizationFilter
import io.scanbot.sdk.ui_v2.common.ScanbotColor
import io.scanbot.sdk.ui_v2.document.CroppingActivity
import io.scanbot.sdk.ui_v2.document.configuration.CroppingConfiguration
import io.scanbot.sdk.util.isDefault
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class PageFilterSnippet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In the real application, you should call this function on button click
        startFiltering()
    }

    private val scanbotSDK = ScanbotSDK(this@PageFilterSnippet)
    private val context = this


    fun startFiltering() {
        // Retrieve the scanned document
        val document = scanbotSDK.documentApi.loadDocument("SOME_SAVED_UUID")

        // Retrieve the selected document page.
        document?.pages?.forEach { page ->
            // Create the instances of the filters you want to apply.
            val filter1 = ScanbotBinarizationFilter(outputMode = OutputMode.ANTIALIASED)
            val filter2 = BrightnessFilter(brightness = 0.4)

            // Apply rotation on the page, and you can also pass the filters here if you want
            page.apply(newRotationTimes = 1, newFilters = listOf(filter1, filter2))

            // Or you can also set the filters separately
            // Set the filters
            page.filters = listOf(filter1, filter2)
        }
    }
}

