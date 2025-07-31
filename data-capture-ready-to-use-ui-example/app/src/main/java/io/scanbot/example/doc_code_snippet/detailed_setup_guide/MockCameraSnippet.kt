package io.scanbot.example.doc_code_snippet.detailed_setup_guide

import io.scanbot.sdk.camera.CameraViewType
import io.scanbot.sdk.camera.MockCameraResourceProvider
import io.scanbot.sdk.camera.ScanbotCameraViewConfigurationProvider

class MockCameraSnippet {
    // @Tag("Mock Camera Initialization")
    /*
     * Initializes the Camera with a mock camera configuration.
     * This is useful for testing purposes without requiring a physical camera.
     *
     * The mock camera will display a static image as the camera feed.
     * Make sure to replace the image path with a valid one in your project.
     *
     * `MockCameraResourceProvider` - delegate that used for feeding camera with fake frames.
     * `getFrameImagePath()` - should return path to the absolute path to the `File` in format without `file://` schema and will be used as detection frame.
     * `getCapturedImagePath()` - should return path to the absolute path to the `File` in format without `file://` schema and will be used as captured image.
     * `showDebugImage` - if true, the mock camera will show `getFrameImagePath` file  as image preview.
     * `tryHideFinderView` - if true, the finder view will be hidden.
     * `tryHidePolygonView` - if true, the ar overlay polygon view will be hidden.
     * `delayBetweenFramesMs` - delay between frames in milliseconds, used to simulate a camera feed.
     * `forceImageUploadOnSamePath` - if true, the mock camera will reload the image from the file system even if the path has not changed. This is useful for testing scenarios where the image file might be updated.
     * `imageFileReadAttemptLimit` - the number of attempts to read the image file. Helps to handle cases where the file might not be immediately available.
     * `imageFileReadAttemptDelay` - delay between attempts to read the image file. Helps to avoid busy-waiting and allows the file system to stabilize if the file is being written to.
     */
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
                tryHidePolygonView = false,
                delayBetweenFramesMs = 100L
            )
    }
    // @EndTag("Mock Camera Initialization")
}