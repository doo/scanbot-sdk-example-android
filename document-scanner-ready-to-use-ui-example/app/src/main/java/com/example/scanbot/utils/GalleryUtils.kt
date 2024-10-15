package com.example.scanbot.utils

import android.content.ClipData
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import androidx.exifinterface.media.ExifInterface
import java.io.IOException

fun getUrisFromGalleryResult(data: Intent): List<Uri> {
    return if (data.clipData != null) {
        val mClipData: ClipData? = data.clipData
        val count: Int = mClipData?.itemCount ?: 0
        val result = mutableListOf<Uri>()

        for (i in 0 until count) {
            mClipData?.let {
                val imageUri: Uri? = it.getItemAt(i)?.uri
                if (imageUri != null) {
                    result.add(imageUri)
                }
            }
        }
        result
    } else {
        val imageUri = data.data
        listOfNotNull(imageUri)
    }
}

fun Uri.toBitmap(
    contentResolver: ContentResolver,
    onException: (Exception) -> Unit = {}
): Bitmap? {
    val maxImageSideSize = 4090
    try {
        var bitmap: Bitmap? = null

        val input = contentResolver.openInputStream(this)
        var orientation = 0

        input.use {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val exif = ExifInterface(input!!)
                orientation = exifToGrad(exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0))
            }
        }
        val inputNew = contentResolver.openInputStream(this)

        bitmap = inputNew.use { BitmapFactory.decodeStream(inputNew) }

        val width = bitmap.width
        val height = bitmap.height
        if (bitmap != null && (orientation != 0 || width > maxImageSideSize || height > maxImageSideSize)) {
            val matrix = Matrix()
            if (orientation != 0) {
                matrix.setRotate(orientation.toFloat(), width / 2f, height / 2f)
            }
            if (width > maxImageSideSize || height > maxImageSideSize) {
                val scale = if (width > height) {
                    maxImageSideSize / width.toFloat()
                } else {
                    maxImageSideSize / height.toFloat()
                }
                matrix.postScale(scale, scale)
            }
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false)
        }
        return bitmap
    } catch (e: IOException) {
        onException(e)
    }

    return null
}

private fun exifToGrad(exifOrientation: Int): Int {
    return when (exifOrientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90
        ExifInterface.ORIENTATION_ROTATE_180 -> 180
        ExifInterface.ORIENTATION_ROTATE_270 -> 270
        else -> 0
    }
}
