package io.scanbot.example.compose

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.camera.FrameHandlerResult
import io.scanbot.sdk.common.AspectRatio
import io.scanbot.sdk.mrz.MrzScannerFrameHandler
import io.scanbot.sdk.mrz.MrzScannerResult
import io.scanbot.sdk.ui_v2.common.ActionBarConfiguration
import io.scanbot.sdk.ui_v2.common.CameraConfiguration
import io.scanbot.sdk.ui_v2.common.CameraPermissionScreen
import io.scanbot.sdk.ui_v2.common.Constants
import io.scanbot.sdk.ui_v2.common.activity.CanceledByUser
import io.scanbot.sdk.ui_v2.common.activity.CloseReason
import io.scanbot.sdk.ui_v2.common.camera.FinderConfiguration
import io.scanbot.sdk.ui_v2.common.camera.ScanbotComposeCamera
import io.scanbot.sdk.ui_v2.common.camera.ScanbotComposeCameraViewModel
import io.scanbot.sdk.ui_v2.common.components.ScanbotCameraActionBar
import io.scanbot.sdk.ui_v2.common.components.ScanbotCameraPermissionView
import io.scanbot.sdk.ui_v2.common.components.ScanbotSystemBar
import io.scanbot.sdk.ui_v2.common.theme.LocalScanbotTheme
import io.scanbot.sdk.ui_v2.common.theme.Localization
import io.scanbot.sdk.ui_v2.common.theme.ScanbotTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow

@Composable
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterialApi::class)
@androidx.camera.camera2.interop.ExperimentalCamera2Interop
fun MrzScannerViewClassic(
    modifier: Modifier = Modifier.fillMaxSize(),
    enableBackNavigation: Boolean = true,
    onScannerClosed: (CloseReason) -> Unit = {},
    onMrz: (MrzScannerResult) -> Unit = {},
    viewModel: MrzViewModel,
) {

    LaunchedEffect(key1 = Unit) {
        viewModel.result.collect { mrzDocument ->
            mrzDocument?.let { onMrz(it) }
        }
    }
    CompositionLocalProvider(
        LocalScanbotTheme provides ScanbotTheme(
            localization = Localization(mutableMapOf())
        )
    ) {
        val context = LocalContext.current
        val previewMode = LocalInspectionMode.current

        BoxWithConstraints(modifier = modifier) {
            this.maxWidth
            val density = LocalDensity.current

            val scope = rememberCoroutineScope()
            BackHandler(enableBackNavigation, onBack = {
                onScannerClosed(CanceledByUser)
            })

            val backgroundColor = Color.Black

            val permissionGranted = if (!previewMode) {
                val cameraPermissionState =
                    rememberPermissionState(permission = Manifest.permission.CAMERA)

                // start scanning delay dialog counter if camera permissions are granted

                checkPermissionStatus(cameraPermissionState) {
                    viewModel.permissionEnabled.value = cameraPermissionState.status.isGranted
                }
                cameraPermissionState.status.isGranted
            } else {
                true
            }
            if (permissionGranted) {

                val scaffoldState = rememberScaffoldState()

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor),
                    topBar = {
                        TopAppBar(title = { Text(text = "Scan Mrz") })
                    },
                    backgroundColor = backgroundColor,
                    scaffoldState = scaffoldState,
                    content = { paddingValues ->
                        BoxWithConstraints(modifier = modifier) {
                            this.maxWidth
                            val cornerRadius = with(LocalDensity.current) {
                                MaterialTheme.shapes.large.topEnd.toPx(
                                    Size(
                                        maxWidth.toPx(), maxHeight.toPx()
                                    ), LocalDensity.current
                                ).toDp()
                            }
                            ScanbotComposeCamera(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(
                                        bottom = paddingValues.calculateBottomPadding(),
                                        end = paddingValues.calculateEndPadding(LocalLayoutDirection.current)
                                    ),
                                viewModel = viewModel,
                                cameraBackgroundColor = backgroundColor,
                                onViewCreated = { camera ->
                                    if (!previewMode) {
                                        camera.removeFrameHandler(viewModel.frameHandler)
                                        camera.addFrameHandler(viewModel.frameHandler)
                                    }
                                },
                                finderConfiguration = FinderConfiguration(
                                    aspectRatio = AspectRatio(
                                        5.0,
                                        2.0
                                    ),
                                    overlayColor = Color.Black.copy(alpha = 0.5f),
                                    strokeColor = Color.White,
                                ),
                            )

                        }
                    },
                    // action bar max scroll position from the bottom consists of sheet pick height, safe area(action bar with its padding itself) and the area from it to hint bottom
                    floatingActionButton = {
                        Column(
                            Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {

                            Box(Modifier.height(Constants.Ui.actionBarHeight)) {
                                ScanbotCameraActionBar(
                                    config = ActionBarConfiguration(),
                                    flashEnabled = viewModel.flashEnabled.collectAsState(),
                                    flashButtonEnabled = viewModel.flashButtonEnabled.collectAsState(),
                                    zoomState = viewModel.zoomFactorUi.collectAsState(),
                                    cameraModule = viewModel.cameraModule.collectAsState(),
                                    onAction = { viewModel.onAction(it) },
                                )
                            }
                        }
                    },
                )

            } else {
                ScanbotSystemBar(
                    systemBarColor = Color.Transparent,
                    statusBarDarkIcons = true
                ) {
                    ScanbotCameraPermissionView(
                        modifier = modifier,
                        bottomContentPadding = 0.dp,
                        permissionConfig = CameraPermissionScreen(),
                        onClose = { onScannerClosed(CanceledByUser) })
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalPermissionsApi::class)
inline fun checkPermissionStatus(
    cameraPermissionState: PermissionState,
    noinline permissionGrantedBlock: CoroutineScope.() -> Unit,
) {
    LaunchedEffect(key1 = cameraPermissionState.status.isGranted, block = permissionGrantedBlock)
    if (cameraPermissionState.status == PermissionStatus.Denied(false)) {
        if (!LocalInspectionMode.current) {
            SideEffect {
                cameraPermissionState.launchPermissionRequest()
            }
        }
    }
}

class MrzViewModel(
    val cameraConfiguration: CameraConfiguration,
    val sdk: ScanbotSDK,
    val flashAvailable: Boolean = true,
) : ScanbotComposeCameraViewModel(
    cameraConfiguration.cameraModule,
    cameraConfiguration.zoomSteps,
    cameraConfiguration.defaultZoomFactor,
    cameraConfiguration.flashEnabled,
    cameraConfiguration.minFocusDistanceLock,
    cameraConfiguration.touchToFocusEnabled,
    cameraConfiguration.pinchToZoomEnabled,
    false,
    cameraConfiguration.orientationLockMode,
    cameraConfiguration.cameraPreviewMode,
    flashAvailable
) {
    val frameHandler: MrzScannerFrameHandler = MrzScannerFrameHandler(sdk.createMrzScanner())
    val result: MutableSharedFlow<MrzScannerResult?> = MutableSharedFlow(1)
    val handler = MrzScannerFrameHandler.ResultHandler { result ->
        if (result is FrameHandlerResult.Success) {
            val mrzDocument = result.value as MrzScannerResult
            // handle mrz document
            if (mrzDocument.success) {
                this.result.tryEmit(mrzDocument)
            }
        }
        false
    }
    init {
        frameHandler.addResultHandler(handler)
    }
}