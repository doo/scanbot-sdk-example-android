package io.scanbot.example.util

import android.content.Context
import androidx.core.net.toFile
import com.squareup.picasso.Picasso
import com.squareup.picasso.Request
import com.squareup.picasso.RequestHandler
import io.scanbot.example.Application
import io.scanbot.sdk.ScanbotSDK
import okio.Okio
import java.io.IOException

object PicassoHelper {

    lateinit var encryptionRequestHandler: RequestHandler

    @Synchronized
    fun with(context: Context): Picasso {
        if (Application.USE_ENCRYPTION) {
            if (this::encryptionRequestHandler.isInitialized.not()) {
                val fileIOProcessor = ScanbotSDK(context).fileIOProcessor()
                encryptionRequestHandler = object : RequestHandler() {
                    override fun canHandleRequest(data: Request?): Boolean {
                        return data?.uri?.isAbsolute ?: false
                    }
                    override fun load(request: Request?, networkPolicy: Int): Result {
                        val uri = request?.uri
                        if (uri != null) {
                            // it is possible to read the file from encrypted storage this way
                            val openFileInputStream = fileIOProcessor.openFileInputStream(uri.toFile())
                            val imageSource = Okio.source(openFileInputStream)
                            return Result(imageSource, Picasso.LoadedFrom.DISK)
                        } else {
                            throw IOException("Image download failed")
                        }
                    }
                }
            }
            return Picasso.Builder(context).addRequestHandler(encryptionRequestHandler).build()
        }
        return Picasso.get()
    }
}
