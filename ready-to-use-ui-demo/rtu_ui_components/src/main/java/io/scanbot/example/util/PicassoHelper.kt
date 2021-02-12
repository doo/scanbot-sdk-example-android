package io.scanbot.example.util

import android.content.Context
import androidx.core.net.toFile
import com.squareup.picasso.Picasso
import com.squareup.picasso.Request
import com.squareup.picasso.RequestHandler
import io.scanbot.example.Application
import io.scanbot.sdk.ScanbotSDK
import java.io.IOException

object PicassoHelper {
    private var picassoInstance: Picasso? = null

    @Synchronized
    fun with(context: Context): Picasso {
        if (picassoInstance == null) {
            val fileIOProcessor = ScanbotSDK(context).fileIOProcessor()
            if (Application.USE_ENCRYPTION) {
                picassoInstance = Picasso.Builder(context).addRequestHandler(object : RequestHandler() {
                    override fun canHandleRequest(data: Request?): Boolean {
                        return data?.uri?.isAbsolute ?: false
                    }

                    override fun load(request: Request?, networkPolicy: Int): Result {
                        val uri = request?.uri
                        if (uri != null) {
                            // it is possible to read the file from encrypted storage this way
                            val openFileInputStream = fileIOProcessor.openFileInputStream(uri.toFile())
                            return Result(openFileInputStream, Picasso.LoadedFrom.DISK)
                        } else {
                            throw IOException("Image download failed")
                        }
                    }
                }).build()
            } else {
                picassoInstance = Picasso.with(context)
            }
        }
        return picassoInstance!!
    }
}