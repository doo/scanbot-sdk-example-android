package io.scanbot.example

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.core.contourdetector.DetectionResult
import io.scanbot.sdk.docprocessing.PageProcessor
import io.scanbot.sdk.persistence.Page
import io.scanbot.sdk.persistence.PageFileStorage
import io.scanbot.sdk.process.ImageFilterType
import io.scanbot.sdk.process.PDFPageSize
import io.scanbot.sdk.process.PDFRenderer
import io.scanbot.sdk.util.thread.MimeUtils
import java.io.File
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var pdfRenderer: PDFRenderer
    private lateinit var progressView: View
    private lateinit var pageFileStorage: PageFileStorage
    private lateinit var pageProcessor: PageProcessor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        askPermission()
        initializeDependencies()

        findViewById<View>(R.id.scanButton).setOnClickListener { v: View? -> openGallery() }
        progressView = findViewById(R.id.progressBar)
    }

    private fun askPermission() {
        if (checkPermissionNotGranted(Manifest.permission.READ_EXTERNAL_STORAGE) ||
                checkPermissionNotGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE), 999)
        }
    }

    private fun checkPermissionNotGranted(permission: String) =
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED

    private fun initializeDependencies() {
        val scanbotSDK = ScanbotSDK(this)
        pdfRenderer = scanbotSDK.createPdfRenderer()
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
        intent?.let {
            intent.clipData?.let { clipData ->
                for (i in 0 until clipData.itemCount) {
                    val item = clipData.getItemAt(i)
                    val uri = item.uri
                    imageUris.add(uri)
                }
            } ?: intent.data?.let { data ->
                imageUris.add(data)
            }

            ProcessDocumentTask(imageUris).execute()
            progressView.visibility = View.VISIBLE
        }
    }

    private fun openDocument(pdfFile: File) {
        val uriForFile = androidx.core.content.FileProvider.getUriForFile(this,
            this.applicationContext.packageName + ".provider", pdfFile)
        val openIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uriForFile)
            type = MimeUtils.getMimeByName(pdfFile.name)
        }
        openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        if (openIntent.resolveActivity(packageManager) != null) {
            val chooser = Intent.createChooser(openIntent, pdfFile.name)
            val resInfoList = this.packageManager.queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY)

            for (resolveInfo in resInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                grantUriPermission(packageName, uriForFile, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(chooser)

        } else {
            Toast.makeText(this, "error opening document", Toast.LENGTH_LONG).show()
        }
    }

    /*
    This AsyncTask is used here only for the sake of example. Please, try to avoid usage of
    AsyncTasks in your application
     */

    private inner class ProcessDocumentTask(val imageUris: List<Uri>) : AsyncTask<Void, Void, File?>() {
        override fun doInBackground(vararg voids: Void): File? {
            try {
                val pages = arrayListOf<Page>()
                for (imageUri in imageUris) {

                    val bitmap = loadImage(imageUri)

                    val newPageId = pageFileStorage.add(bitmap)
                    val page = Page(newPageId, listOf(), DetectionResult.OK, ImageFilterType.GRAYSCALE)

                    val detectedPage = pageProcessor.detectDocument(page)
                    pages.add(detectedPage)
                }

                return pdfRenderer.renderDocumentFromPages(pages, PDFPageSize.FIXED_A4)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }

        fun loadImage(imageUri: Uri): Bitmap {
            return MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        }

        override fun onPostExecute(processedDocument: File?) {
            progressView.visibility = View.GONE

            //open first document
            processedDocument?.let { openDocument(it) }
        }
    }

    companion object {
        private const val SELECT_PICTURE_REQUEST = 100
        private const val IMAGE_TYPE = "image/*"
    }
}
