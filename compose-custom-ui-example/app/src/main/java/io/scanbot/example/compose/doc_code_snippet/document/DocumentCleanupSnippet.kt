package io.scanbot.example.compose.doc_code_snippet.document

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.scanbot.sdk.image.ImageRef
import io.scanbot.sdk.imageprocessing.DocumentCleanupConfiguration
import io.scanbot.sdk.ui_v2.document.DocumentCleanupCustomUI
import io.scanbot.sdk.ui_v2.document.screen.documentcleanup.DocumentCleanupActionController

// @Tag("Document Cleanup Custom UI Composable")
/**
 * Renders the `DocumentCleanupCustomUI` composable on top of the supplied [image].
 *
 * The composable wraps the `DocumentCleanup` classic API and gives the host application
 * full control over the surrounding chrome (top bar, bottom bar, undo / redo / reset
 * controls, brush-size slider, etc.).
 *
 * Use the [DocumentCleanupActionController] exposed via `onActionControllerCreated` to
 * trigger undo / redo / reset programmatically and to observe the current operation
 * progress and undo / redo availability.
 */
@Composable
fun DocumentCleanupCustomUISnippet(image: ImageRef) {
    val controller = remember { mutableStateOf<DocumentCleanupActionController?>(null) }
    val brushSize = remember { mutableFloatStateOf(40f) }
    val activeController = controller.value
    val canUndo = activeController?.canUndo?.collectAsState()?.value ?: false
    val canRedo = activeController?.canRedo?.collectAsState()?.value ?: false
    val progress = activeController?.progress?.collectAsState()?.value ?: false

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            DocumentCleanupCustomUI(
                image = image,
                modifier = Modifier.fillMaxSize(),
                // SDK-level cleanup configuration: keepText, undo/redo stack size, etc.
                documentCleanupConfiguration = DocumentCleanupConfiguration(
                    keepText = true,
                    maxUndoRedoStackSize = 10,
                ),
                // Background color behind the image canvas
                backgroundColor = Color.Black,
                // Diameter of the drawing brush
                brushSize = brushSize.floatValue.dp,
                // Brush stroke preview color
                brushColor = Color.Red.copy(alpha = 0.5f),
                // Maximum zoom scale supported by pinch-to-zoom
                maxZoomScale = 10f,
                // Capture the action controller to trigger undo / redo / reset and observe state.
                onActionControllerCreated = { controller.value = it },
                // Triggered after every successful cleanup, undo, redo or reset.
                // The provided ImageRef is owned by the composable - DO NOT close it.
                onResultImageChanged = { newImage ->
                    Log.d("DocumentCleanupCustomUI", "New result image: $newImage")
                },
                // Invoked on SDK setup or cleanup failures.
                onError = { error ->
                    Log.e("DocumentCleanupCustomUI", "Cleanup error: ${error.message}")
                },
            )

            if (progress) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                )
            }
        }

        Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    enabled = canUndo && !progress,
                    onClick = { activeController?.undo() },
                ) { Text("Undo") }
                Button(
                    modifier = Modifier.weight(1f),
                    enabled = canRedo && !progress,
                    onClick = { activeController?.redo() },
                ) { Text("Redo") }
                Button(
                    modifier = Modifier.weight(1f),
                    enabled = canUndo && !progress,
                    onClick = { activeController?.reset() },
                ) { Text("Reset") }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Brush size: ${brushSize.floatValue.toInt()} dp")
            Slider(
                value = brushSize.floatValue,
                onValueChange = { brushSize.floatValue = it },
                valueRange = 8f..120f,
            )
        }
    }
}
// @EndTag("Document Cleanup Custom UI Composable")
