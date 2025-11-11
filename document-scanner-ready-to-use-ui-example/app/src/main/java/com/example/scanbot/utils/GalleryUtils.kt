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


private fun exifToGrad(exifOrientation: Int): Int {
    return when (exifOrientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90
        ExifInterface.ORIENTATION_ROTATE_180 -> 180
        ExifInterface.ORIENTATION_ROTATE_270 -> 270
        else -> 0
    }
}
