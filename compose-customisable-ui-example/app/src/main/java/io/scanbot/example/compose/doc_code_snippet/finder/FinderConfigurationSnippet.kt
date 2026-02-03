package io.scanbot.example.compose.doc_code_snippet.finder

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.scanbot.sdk.geometry.AspectRatio
import io.scanbot.sdk.ui_v2.common.components.FinderConfiguration


// @Tag("Detailed Finder Configuration")
@Composable
fun FinderConfigurationSnippet() {
    FinderConfiguration(
        // align the viewfinder to the top free space of the Camera Preview. Means that it will be close to the preview edge minus the previewInsets
        verticalAlignment = Alignment.Top,
        // align the viewfinder to the free horizontal space of the Camera Preview
        horizontalAlignment = Alignment.CenterHorizontally,
        // Insets from the edges of the camera preview to the viewfinder.
        previewInsets = PaddingValues(
            top = 32.dp,
            bottom = 32.dp,
            start = 16.dp,
            end = 16.dp
        ),
        // Aspect ratio of the viewfinder window:
        aspectRatio = AspectRatio(1.0, 1.0),
        // Change viewfinder overlay color here:
        overlayColor = Color.Black.copy(alpha = 0.3f),
        //  Viewfinder stroke color here, default is Transparent:
        strokeColor = Color.White,
        // Viewfinder stroke width here:
        strokeWidth = 2.dp,
        // radius for rounded corners of the viewfinder window:
        cornerRadius = 8.dp,
        // Limit the maximum width of the viewfinder window on the preview. This parameter work with aspect ratio to define the final size of the viewfinder.
        //preferredMaxWidth = 300.dp,
        // Limit the maximum height of the viewfinder window on the preview. This parameter work with aspect ratio to define the final size of the viewfinder.
        //preferredMaxHeight = 52.dp,
        // Composable area that is inserted inside the viewfinder window:
        finderContent = {
            // Box with border stroke color as an example of custom viewfinder content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
                    // Same but with rounded corners
                    .border(
                        4.dp,
                        Color.Cyan,
                        shape = RoundedCornerShape(
                            16.dp
                        )
                    )
            ) {

            }
        },
        // Composable area that are inserted between  the viewfinder window and the top edge of the camera view:
        topContent = {
            Text(
                "Custom Top Content",
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
            )
        },
        // Composable area that are inserted between  the viewfinder window and the bottom edge of the camera view:
        bottomContent = {
            // You may add custom components and other elements here:
            Text(
                "Custom Bottom Content",
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
            )
        },
        bottomLayer = {
            // Draw something between the viewfinder  and camera preview layers
        },
        topLayer = {
            // Draw something above the viewfinder layer if needed
        }
    )
}
// @EndTag("Detailed Finder Configuration")