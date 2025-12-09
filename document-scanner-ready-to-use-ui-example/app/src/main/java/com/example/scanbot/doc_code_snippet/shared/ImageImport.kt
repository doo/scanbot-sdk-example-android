package com.example.scanbot.doc_code_snippet.shared

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.image.ImageRef
import io.scanbot.sdk.usecases.documents.R
import io.scanbot.sdk.util.toImageRef

// @Tag("Import image Contract")
class ImportImageContract(private val context: Context) : ActivityResultContract<Unit, ImageRef?>() {
    override fun createIntent(context: Context, input: Unit): Intent {
        // An image is selected from the photo library and document detection is run on it:
        val imageIntent = Intent()
        imageIntent.type = "image/*"
        imageIntent.action = Intent.ACTION_GET_CONTENT
        imageIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, false)
        imageIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)

        return Intent.createChooser(imageIntent, "Select a picture")
    }

    override fun parseResult(resultCode: Int, intent: Intent?): ImageRef? {
        return if (resultCode == Activity.RESULT_OK && intent != null) {
            // use the extension function `Uri.toImageRef(contentResolver)` to create an instance of `ImageRef`
            return intent.data?.toImageRef(contentResolver = context.contentResolver)?.getOrNull()
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
            this.registerForActivityResult(ImportImageContract(this)) { resultImage ->
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
                        // Image processing is carried out
                        // processImage(resultImage)
                    }
                }
            }
        findViewById<View>(R.id.import_image).setOnClickListener {
            galleryImageLauncher.launch(Unit)
        }
        // @EndTag("Import image launcher")
    }
}
