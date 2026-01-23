package io.scanbot.example.compose.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.withSaveLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.min

@Composable
fun CorneredFinder(
    cornerRadius: Dp = 16.dp,
    strokeWidth: Dp = 4.dp,
    strokeColor: Color = Color.White,
) {
    Canvas(modifier = Modifier.fillMaxSize(), onDraw = {

        val maxWidth = this.size.width
        val maxHeight = this.size.height
        val cornerRadiusPx = cornerRadius.toPx()
        val strokeWidthPx = strokeWidth.toPx()
        val cornerSize = min(
            cornerRadiusPx + cornerRadiusPx / 2 + strokeWidthPx / 2,
            min(maxWidth, maxHeight) / 2f
        )


        val clearPath = Path().apply {
            moveTo(cornerSize, 0f)

            lineTo(maxWidth - cornerSize, 0f)
            moveTo(maxWidth, 0f)
            moveTo(maxWidth, cornerSize)

            lineTo(maxWidth, maxHeight - cornerSize)
            moveTo(maxWidth, maxHeight)
            moveTo(maxWidth - cornerSize, maxHeight)

            lineTo(cornerSize, maxHeight)
            moveTo(0f, maxHeight)
            moveTo(0f, maxHeight - cornerSize)
            lineTo(0f, cornerSize)
        }

        this.drawContext.canvas.withSaveLayer(
            bounds = Rect(
                0f,
                0f,
                maxWidth,
                maxHeight
            ),
            paint = Paint(),
        ) {
            drawRoundRect(
                color = strokeColor,
                topLeft = Offset(strokeWidthPx / 2, strokeWidthPx / 2),
                size = Size(maxWidth - strokeWidthPx, maxHeight - strokeWidthPx),
                style = Stroke(
                    width = strokeWidthPx
                ),
                cornerRadius = CornerRadius(cornerRadiusPx),
            )

            drawPath(
                path = clearPath, color = Color.Black, style = Stroke(
                    width = strokeWidthPx * 4,
                    cap = StrokeCap.Butt,
                ),
                blendMode = BlendMode.Clear
            )
        }

    })
}
