package io.scanbot.example.compose.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import io.scanbot.sdk.barcode.BarcodeItem

@Composable
fun BarcodeItem(barcode: BarcodeItem) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Row() {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(64.dp)
            ) {
                val image = barcode.sourceImage?.toBitmap()?.getOrNull()?.asImageBitmap()
                image?.let {
                    Image(
                        modifier = Modifier.size(48.dp),
                        bitmap = it,
                        contentDescription = "Barcode Thumbnail",
                        contentScale = ContentScale.Inside
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        "Data: ${barcode.text}",
                        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface)
                    )
                    Text(
                        "Format: ${barcode.format}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }
    }
}