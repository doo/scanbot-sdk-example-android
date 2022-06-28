package io.scanbot.example

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.core.contourdetector.DetectionResult
import io.scanbot.sdk.docprocessing.PageProcessor
import io.scanbot.sdk.persistence.Page
import io.scanbot.sdk.persistence.PageFileStorage
import io.scanbot.sdk.persistence.PageFileStorage.PageFileType
import io.scanbot.sdk.process.ImageFilterType
import io.scanbot.sdk.util.thread.MimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var progressView: View
    private lateinit var pageFileStorage: PageFileStorage
    private lateinit var pageProcessor: PageProcessor
    private lateinit var image: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        askPermission()
        initializeDependencies()

        findViewById<View>(R.id.scanButton).setOnClickListener { v: View? -> openGallery() }
        progressView = findViewById(R.id.progressBar)
        image = findViewById(R.id.image)
    }

    private fun askPermission() {
        if (checkPermissionNotGranted(Manifest.permission.READ_EXTERNAL_STORAGE) || checkPermissionNotGranted(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), 999
            )
        }
    }

    private fun checkPermissionNotGranted(permission: String) =
        ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED

    private fun initializeDependencies() {
        val scanbotSDK = ScanbotSDK(this)
        pageFileStorage = scanbotSDK.createPageFileStorage()
        pageProcessor = scanbotSDK.createPageProcessor()
    }

    private fun openGallery() {
        val intent = Intent()
        intent.type = IMAGE_TYPE
        intent.action = Intent.ACTION_GET_CONTENT
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(
            Intent.createChooser(intent, "Select picture"),
            SELECT_PICTURE_REQUEST
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode != SELECT_PICTURE_REQUEST || resultCode != Activity.RESULT_OK) {
            return
        }
        val imageUris = ArrayList<Uri>()

        if (intent!!.clipData != null) {
            val data = intent.clipData!!
            for (i in 0 until data.itemCount) {
                val item = data.getItemAt(i)
                val uri = item.uri
                imageUris.add(uri)
            }
        } else if (intent.data != null) {
            imageUris.add(intent.data!!)
        }
        progressView.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.Default) {
            processImageTask(imageUris)
        }
    }

    private fun openDocument(processedDocument: File) {
        val openIntent = Intent()
        openIntent.action = Intent.ACTION_VIEW
        openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        openIntent.setDataAndType(
            FileProvider.getUriForFile(
                this,
                applicationContext.packageName + ".provider",
                processedDocument
            ),
            MimeUtils.getMimeByName(processedDocument.name)
        )
        if (openIntent.resolveActivity(packageManager) != null) {
            startActivity(openIntent)
        } else {
            Toast.makeText(this@MainActivity, "Error while opening the document", Toast.LENGTH_LONG)
                .show()
        }
    }

    /*
    This AsyncTask is used here only for the sake of example. Please, try to avoid usage of
    AsyncTasks in your application
     */
    private suspend fun processImageTask(imageUris: ArrayList<Uri>) {
        @Throws(IOException::class)
        fun loadImage(imageUri: Uri): Bitmap {
            return MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        }


        val bitmap = try {
            imageUris.firstOrNull()?.let { imageUri ->
                val bitmap = loadImage(imageUri)
                val newPageId = pageFileStorage.add(bitmap)
                val page =
                    Page(newPageId, emptyList(), DetectionResult.OK, ImageFilterType.GRAYSCALE)
                pageProcessor.detectDocument(page)
                MediaStore.Images.Media.getBitmap(
                    this@MainActivity.contentResolver,
                    pageFileStorage.getImageURI(page.pageId, PageFileType.DOCUMENT)
                )
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }


        withContext(Dispatchers.Main) {
            progressView.visibility = View.GONE

            //open first document
            if (bitmap != null) {
                image.setImageBitmap(bitmap)
            }
        }

    }

    companion object {
        private const val SELECT_PICTURE_REQUEST = 100
        private const val IMAGE_TYPE = "image/*"
    }
}
