@file:kotlin.OptIn(ExperimentalPermissionsApi::class)

package io.scanbot.example.compose

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import io.scanbot.sdk.image.ImageRef
import io.scanbot.sdk.imageprocessing.DocumentCleanupConfiguration
import io.scanbot.sdk.ui_v2.document.DocumentCleanupCustomUI
import io.scanbot.sdk.ui_v2.document.screen.documentcleanup.DocumentCleanupActionController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Interactive demo screen for [DocumentCleanupCustomUI]. The user picks an image from the
 * gallery, paints a mask on top of it and the masked regions are cleaned up by the SDK.
 *
 * For a code-only snippet (no image picker, no navigation), see
 * [io.scanbot.example.compose.doc_code_snippet.document.DocumentCleanupCustomUISnippet].
 */
@Composable
fun DocumentCleanupScreen(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var inputImage by remember { mutableStateOf<ImageRef?>(null) }
    val controller = remember { mutableStateOf<DocumentCleanupActionController?>(null) }
    val brushSize = remember { mutableFloatStateOf(40f) }

    val activeController = controller.value
    val canUndo = activeController?.canUndo?.collectAsState()?.value ?: false
    val canRedo = activeController?.canRedo?.collectAsState()?.value ?: false
    val progress = activeController?.progress?.collectAsState()?.value ?: false

    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val newImage = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                }.getOrNull()?.let { ImageRef.fromBitmap(it) }
            }
            if (newImage != null) {
                inputImage?.close()
                inputImage = newImage
            } else {
                Log.e("DocumentCleanupScreen", "Failed to load image from $uri")
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            val image = inputImage
            if (image != null) {
                DocumentCleanupCustomUI(
                    image = image,
                    modifier = Modifier.fillMaxSize(),
                    documentCleanupConfiguration = DocumentCleanupConfiguration(
                        keepText = true,
                        maxUndoRedoStackSize = 10,
                    ),
                    brushSize = brushSize.floatValue.dp,
                    brushColor = Color.Red.copy(alpha = 0.5f),
                    onActionControllerCreated = { controller.value = it },
                    onResultImageChanged = {
                        Log.d("DocumentCleanupScreen", "New result image received")
                    },
                    onError = { error ->
                        Log.e("DocumentCleanupScreen", "Cleanup error", error)
                    },
                )
                if (progress) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter),
                    )
                }
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "Pick an image to start cleanup",
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }

        Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        pickImageLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                ) { Text("Pick image") }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    modifier = Modifier.weight(1f),
                    enabled = canUndo && !progress,
                    onClick = { activeController?.undo() },
                ) { Text(text = "Undo", color = Color.White) }
                Button(
                    modifier = Modifier.weight(1f),
                    enabled = canRedo && !progress,
                    onClick = { activeController?.redo() },
                ) { Text(text = "Redo", color = Color.White) }
                Button(
                    modifier = Modifier.weight(1f),
                    enabled = canUndo && !progress,
                    onClick = { activeController?.reset() },
                ) { Text(text = "Reset", color = Color.White) }
            }

            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = "Brush size: ${brushSize.floatValue.toInt()} dp",
            )
            Slider(
                value = brushSize.floatValue,
                onValueChange = { brushSize.floatValue = it },
                valueRange = 8f..120f,
            )
        }
    }
}
