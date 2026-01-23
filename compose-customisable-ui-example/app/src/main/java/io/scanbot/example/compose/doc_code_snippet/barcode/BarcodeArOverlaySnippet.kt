package io.scanbot.example.compose.doc_code_snippet.barcode

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import io.scanbot.sdk.barcode.*
import io.scanbot.sdk.camera.*
import io.scanbot.sdk.ui_v2.barcode.components.ar_tracking.*
import kotlinx.coroutines.flow.SharedFlow


// @Tag("Customisable barcode AR view")
@Composable
fun BarcodeArOverlaySnippet(
    barcodesFlow: SharedFlow<Pair<BarcodeScannerResult, FrameHandler.Frame>?>,
) {
    val density = LocalDensity.current

    ScanbotBarcodesArOverlay(
        barcodesFlow,
        getData = { barcodeItem -> barcodeItem.textWithExtension },
        shouldHighlight = { barcodeItem ->
            // Here you can implement any custom logic to decide whether to highlight a barcode with second style or not.
            false
        },
        getPolygonStyle = { defaultStyle, barcodeItem ->
            // Customize polygon style here.
            // You may use barcodeItem to apply different styles for different barcode types, etc.
            defaultStyle.copy(
                drawPolygon = true,
                // Control whether to fill the polygon with fill color
                useFill = true,
                // Control whether to fill the polygon with fill color when highlighted
                useFillHighlighted = true,
                // Radius of the polygon corners in px
                cornerRadius = density.run { 20.dp.toPx() },
                cornerHighlightedRadius = density.run { 20.dp.toPx() },
                // Width of the polygon stroke in px
                strokeWidth = density.run { 5.dp.toPx() },
                // Width of the polygon stroke when highlighted in px
                strokeHighlightedWidth = density.run { 5.dp.toPx() },
                // Color of the polygon stroke
                strokeColor = Color.Green,
                // Color of the polygon stroke when highlighted
                strokeHighlightedColor = Color.Red,
                // Fill color of the polygon
                fillColor = Color.Green.copy(alpha = 0.3f),
                // Fill color of the polygon when highlighted
                fillHighlightedColor = Color.Red.copy(alpha = 0.3f),
                shouldDrawShadows = false
            )
        },
        //  Customize AR view for barcode polygon here if needed
        // For example, show barcode data below the polygon or display an icon, image etc.
        view = { path, barcodeItem, data, shouldHighlight ->
            // Implement custom view for barcode polygon if needed
            Box(modifier = Modifier.layout { measurable, constraints ->
                val placeable = measurable.measure(constraints);

                var rectF: Rect
                path.getBounds().also { rectF = it }

                val width = placeable.width
                val height = placeable.height
                val x = rectF.center.x - width / 2
                val y = rectF.center.y + rectF.height / 2 + 10.dp.toPx() // place below the polygon
                layout(width, height) {
                    placeable.placeRelative(x.toInt(), y.toInt())
                }
            }) {
                Text(
                    text = data,
                    color = if (shouldHighlight) Color.Red else Color.Green,
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(4.dp)
                )
            }
        },
        onClick = {
            //handle click on polygon area representing a barcode
        },
    )
}
//@EndTag("Customisable barcode AR view")