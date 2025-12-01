package com.example.scanbot.doc_code_snippet.classic_ui

import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.document.DocumentScannerFrameHandler
import io.scanbot.sdk.geometry.AspectRatio
import io.scanbot.sdk.ui.camera.AdaptiveFinderOverlayView
import io.scanbot.sdk.ui.camera.FinderOverlayView
import io.scanbot.sdk.ui.camera.ScanbotCameraXView
import io.scanbot.sdk.usecases.documents.R

fun finderOverlayViewRequiredPageAspectRatios(activity: AppCompatActivity) {
    // @Tag("Setting Required Aspect Ratios")
    val requiredPageAspectRatios = listOf(AspectRatio(21.0, 29.7)) // ~ A4 page size

    //...

    val finderOverlayView = activity.findViewById<FinderOverlayView>(R.id.finder_overlay_view)
    finderOverlayView.setRequiredAspectRatios(requiredPageAspectRatios)
    // @EndTag("Setting Required Aspect Ratios")
}

fun finderOverlayViewInsets(activity: AppCompatActivity) {
    // @Tag("Setting Insets for FinderOverlayView")
    val finderOverlayView = activity.findViewById<FinderOverlayView>(R.id.finder_overlay_view)
    finderOverlayView.finderInsets = Insets.of(50, 200, 50, 0)
    // ...
    // @EndTag("Setting Insets for FinderOverlayView")
}

fun finderOverlayViewOneInset(finderOverlayView: FinderOverlayView) {
    // @Tag("Setting One Inset for FinderOverlayView")
    finderOverlayView.setFinderInset(right = 50)
    // @EndTag("Setting One Inset for FinderOverlayView")
}

fun finderOverlayViewSafeAreaInsets(activity: AppCompatActivity) {
    // @Tag("Setting Safe Area Insets for FinderOverlayView")
    val finderOverlayView = activity.findViewById<FinderOverlayView>(R.id.finder_overlay_view)
    finderOverlayView.safeAreaInsets = Insets.of(0, 200, 0, 0)
    // @EndTag("Setting Safe Area Insets for FinderOverlayView")
}

fun finderOverlayViewOneSafeAreaInset(finderOverlayView: FinderOverlayView) {
    // @Tag("Set one Safe Area Inset for FinderOverlayView")
    finderOverlayView.setSafeAreaInset(top = 200)
    // @EndTag("Set one Safe Area Inset for FinderOverlayView")
}

fun adaptiveFinderOverlayViewSetup(activity: AppCompatActivity, scanbotSDK: ScanbotSDK) {
    // @Tag("Setting up AdaptiveFinderOverlayView")
    val cameraView = activity.findViewById<ScanbotCameraXView>(R.id.camera_view)
    val finderOverlayView = activity.findViewById<AdaptiveFinderOverlayView>(R.id.finder_overlay_view)

    // we can use several aspect ratios:
    val pageAspectRatios = listOf( // this will be used for DocumentScannerFrameHandler
        AspectRatio(21.0, 29.7), // a4 sheet size
        AspectRatio(85.60, 53.98)) // credit card size
    finderOverlayView.setRequiredAspectRatios(pageAspectRatios)

    val documentScanner = scanbotSDK.createDocumentScanner().getOrThrow()
    documentScanner.setConfiguration(documentScanner.copyCurrentConfiguration().apply {
        this.parameters.aspectRatios = pageAspectRatios
    })

    val documentScannerFrameHandler = DocumentScannerFrameHandler.attach(cameraView, documentScanner)
    documentScannerFrameHandler.addResultHandler(finderOverlayView.documentScannerFrameHandler)
    // @EndTag("Setting up AdaptiveFinderOverlayView")
}

fun finderOverlayViewPlaceholder(finderOverlayView: FinderOverlayView, view: android.view.View) {
    // @Tag("Setting Placeholder for FinderOverlayView")
    finderOverlayView.setTopPlaceholder(view)
    finderOverlayView.setBottomPlaceholder(view)
    finderOverlayView.setFinderPlaceholder(view)
    // @EndTag("Setting Placeholder for FinderOverlayView")
}