package io.scanbot.example

import android.widget.ImageView
import android.graphics.Bitmap
import android.graphics.BitmapFactory

/**
 * Wrap logic of loading images from disk into ImageViews.
 *
 * Solely for demo purposes:
 * in real code better use some image loader library, like Glide or Picasso
 *
 * Downscales image to be optimal for loading into area of 1x screen width and 1/4 screen's height
 */
class ImageLoader(private val imageView: ImageView) {

    fun loadFromFilePath(imageFilePath: String) {
        if (imageFilePath.isEmpty()) return

        imageView.setImageBitmap(decodeSampledBitmap(imageFilePath))
    }

    private fun calculateInSampleSize(
            options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight = height / 2
            val halfWidth = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize > reqHeight && halfWidth / inSampleSize > reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    private fun decodeSampledBitmap(pathName: String,
                                    reqWidth: Int, reqHeight: Int): Bitmap {

        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(pathName, options)

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(pathName, options)
    }

    //I added this to have a good approximation of the screen size:
    private fun decodeSampledBitmap(pathName: String): Bitmap {
        val display = imageView.resources.displayMetrics
        val width = display.widthPixels
        val height = display.heightPixels / 4
        return decodeSampledBitmap(pathName, width, height)
    }
}
