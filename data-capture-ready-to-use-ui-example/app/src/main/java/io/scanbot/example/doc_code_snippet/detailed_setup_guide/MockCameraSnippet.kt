package io.scanbot.example.doc_code_snippet.detailed_setup_guide

import io.scanbot.sdk.camera.CameraViewType
import io.scanbot.sdk.camera.MockCameraResourceProvider
import io.scanbot.sdk.camera.ScanbotCameraViewConfigurationProvider

class MockCameraSnippet {
    // @Tag("Mock Camera Initialization")
    fun initMockCamera() {
        ScanbotCameraViewConfigurationProvider.cameraViewType =
            CameraViewType.Mock(
                resourceProvider = object : MockCameraResourceProvider {
                    override fun getFrameImagePath(): String {
                        return "/data/data/io.scanbot.example.sdk.rtu.android/files/test.png"
                    }

                    override fun getCapturedImagePath(): String? {
                        return "/data/data/io.scanbot.example.sdk.rtu.android/files/test.png"
                    }
                },
                showDebugImage = true,
                tryHideFinderView = false,
                tryHidePolygonView = false
            )
    }
    // @EndTag("Mock Camera Initialization")
}