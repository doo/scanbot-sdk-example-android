package com.example.scanbot.doc_code_snippet.shared

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.usecases.documents.R

// @Tag("Import image Contract")
class ImportImageContract(private val context: Context) : ActivityResultContract<Unit, Bitmap?>() {
    override fun createIntent(context: Context, input: Unit): Intent {
        // An image is selected from the photo library and document detection is run on it:
        val imageIntent = Intent()
        imageIntent.type = "image/*"
        imageIntent.action = Intent.ACTION_GET_CONTENT
        imageIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, false)
        imageIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)

        return Intent.createChooser(imageIntent, "Select a picture")
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
// @EndTag("Import image Contract")


fun galleryImageLauncher(activity: AppCompatActivity) {
    activity.apply {
        // @Tag("Import image launcher")
        val galleryImageLauncher =
            this.registerForActivityResult(ImportImageContract(this)) { resultEntity ->
                lifecycleScope.launch(Dispatchers.Default) {
                    val activity = this
                    val sdk = ScanbotSDK(activity as Context)
                    if (!sdk.licenseInfo.isValid) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                activity,
                                "License has expired!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        resultEntity?.let { bitmap ->
                            // Image processing is carried out
                            // processImage()
                        }
                    }
                }
            }
        findViewById<View>(R.id.import_image).setOnClickListener {
            galleryImageLauncher.launch(Unit)
        }
        // @EndTag("Import image launcher")
    }
}
