package io.scanbot.example.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.scanbot.sdk.barcode.BarcodeItem
import io.scanbot.sdk.barcode.BarcodeScannerResult
import io.scanbot.sdk.barcode.textWithExtension
import io.scanbot.sdk.camera.FrameHandler
import io.scanbot.sdk.ui_v2.barcode.components.ar_tracking.ScanbotBarcodesArOverlay
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun CustomBarcodesArView(
    barcodesFlow: SharedFlow<Pair<BarcodeScannerResult,FrameHandler.Frame>?>,
    onBarcodeClick: (BarcodeItem) -> Unit = {},
    density: Density,
) {

    ScanbotBarcodesArOverlay(
        barcodesFlow,
        getData = { barcodeItem -> barcodeItem.textWithExtension },
        getPolygonStyle = { defaultStyle, barcodeItem ->
            // Customize polygon style here.
            // You may use barcodeItem to apply different styles for different barcode types, etc.
            defaultStyle.copy(
                drawPolygon = true,
                useFill = true,
                useFillHighlighted = true,
                cornerRadius = density.run { 20.dp.toPx() },
                cornerHighlightedRadius = density.run { 20.dp.toPx() },
                strokeWidth = density.run { 5.dp.toPx() },
                strokeHighlightedWidth = density.run { 5.dp.toPx() },
                strokeColor = Color.Green,
                strokeHighlightedColor = Color.Red,
                fillColor = Color.Green.copy(alpha = 0.3f),
                fillHighlightedColor = Color.Red.copy(alpha = 0.3f),
                shouldDrawShadows = false
            )
        },
        shouldHighlight = { barcodeItem ->
            // Here you can implement any custom logic.
            false
        },
        view = { path, barcodeItem, data, shouldHighlight ->
            // If only polygon is needed without any additional UI, leave this block empty
        },

// Uncomment and  Customize AR view for barcode polygon here if needed
//        view = { path, barcodeItem, data, shouldHighlight ->
//            // Implement custom view for barcode polygon if needed
//            Box(modifier = Modifier.layout { measurable, constraints ->
//                val placeable = measurable.measure(constraints);
//
//                var rectF: Rect
//                path.getBounds().also { rectF = it }
//
//                val width = placeable.width
//                val height = placeable.height
//                val x = rectF.center.x - width / 2
//                val y = rectF.center.y + rectF.height / 2 + 10.dp.toPx() // place below the polygon
//                layout(width, height) {
//                    placeable.placeRelative(x.toInt(), y.toInt())
//                }
//            }) {
//                Text(
//                    text = data,
//                    color = if (shouldHighlight) Color.Red else Color.Green,
//                    style = MaterialTheme.typography.body2,
//                    modifier = Modifier
//                        .background(Color.Black.copy(alpha = 0.5f))
//                        .padding(4.dp)
//                )
//            }
//        },
        onClick = onBarcodeClick,
    )
}
