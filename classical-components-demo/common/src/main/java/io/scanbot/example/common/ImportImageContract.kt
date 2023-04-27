package io.scanbot.example.common

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract


class ImportImageContract(private val context: Context) : ActivityResultContract<Unit, Bitmap?>() {
    override fun createIntent(context: Context, input: Unit): Intent {
        // select an image from photo library and run document detection on it:
        val imageIntent = Intent()
        imageIntent.type = "image/*"
        imageIntent.action = Intent.ACTION_GET_CONTENT
        imageIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, false)
        imageIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)

       return Intent.createChooser(imageIntent, "Select picture")
    }

    private fun processGalleryResult(data: Intent): Bitmap? {
        val imageUri = data.data
        return MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Bitmap? {
       return if (resultCode == Activity.RESULT_OK && intent != null) {
           return processGalleryResult(intent)
        } else {
            null
        }
    }
}

