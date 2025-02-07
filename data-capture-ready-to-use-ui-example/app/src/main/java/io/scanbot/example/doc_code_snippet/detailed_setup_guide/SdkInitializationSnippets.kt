package io.scanbot.example.doc_code_snippet.detailed_setup_guide

/*
    NOTE: this snippet of code is to be used only as a part of the website documentation.
    This code is not intended for any use outside of the support of documentation by Scanbot SDK GmbH employees.
*/

// NOTE for maintainers: whenever changing this code,
// ensure that links using it are still pointing to valid lines!
// Pay attention to imports adding/removal/sorting!
// Page URLs using this code:
// TODO: add URLs here

import io.scanbot.example.*
import io.scanbot.sdk.*

fun xnnpackAccelerationSnippet(application: Application) {
    // @Tag("XNNPACK Acceleration")
    ScanbotSDKInitializer()
            .allowXnnpackAcceleration(false)
            // ...
            .initialize(application)
    // @EndTag("XNNPACK Acceleration")
}

fun gpuAccelerationSnippet(application: Application) {
    // @Tag("GPU Acceleration")
    ScanbotSDKInitializer()
            .allowGpuAcceleration(false)
            // ...
            .initialize(application)
    // @EndTag("GPU Acceleration")
}

fun precompileGpuMlModelsSnippet(application: Application) {
    // @Tag("Precompile GPU ML Models")
    ScanbotSDKInitializer()
            .precompileGpuMlModels(
                    precompilingCallback = {},
            )
            // ...
            .initialize(application)
    // @EndTag("Precompile GPU ML Models")
}