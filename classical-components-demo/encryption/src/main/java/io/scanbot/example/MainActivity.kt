package io.scanbot.example

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.pdf.model.PageSize
import io.scanbot.pdf.model.PdfConfig
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.persistence.fileio.FileIOProcessor
import io.scanbot.sdk.process.PDFRenderer
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var fileIOProcessor: FileIOProcessor
    private lateinit var pdfRenderer: PDFRenderer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val scanbotSDK = ScanbotSDK(applicationContext)
        fileIOProcessor = scanbotSDK.fileIOProcessor()
        pdfRenderer = scanbotSDK.createPdfRenderer()

        findViewById<Button>(R.id.encrypt_image).setOnClickListener {
            writeEncryptedImage()
        }

        findViewById<Button>(R.id.write_pdf).setOnClickListener {
            writeEncryptedPDF()
        }
    }

    private fun writeEncryptedImage() {
        val originalBitmap = loadBitmapFromAssets("demo_image.jpg")
        val encryptedDestination = getExternalFile("encrypted.jpeg")
        fileIOProcessor.writeImage(originalBitmap, Bitmap.CompressFormat.JPEG, 100, encryptedDestination)
        showToast("The encrypted image was written to: $encryptedDestination")

        // TODO: to open it you should use
        //  to open it as a bitmap:
        //  fileIOProcessor.readImage(encryptedDestination)
        //  or
        //  more general to open it as inputStream:
        //  fileIOProcessor.openFileInputStream(encryptedDestination)
        //  or
        //  if it is needed to write an unencrypted file to the persistent storage:
        //  fileIOProcessor.copyRaw(encryptedDestination, getExternalFile("unencrypted.jpeg"))
    }

    private fun writeEncryptedPDF() {
        val originalBitmapFile = copyFromAssetsToInternal("demo_image.jpg")
        val imageFileUris = listOf(Uri.fromFile(originalBitmapFile))
        // PDF renderer uses FileIOProcessor under the hood, so all the created files on the persistent storage will be encrypted:
        // Here we use the file from assets as input, so [sourceFilesEncrypted] should be false.
        // If it is planned to use an encrypted file, created via our SDK, it should be true.
        val encryptedDestination = pdfRenderer.renderDocumentFromImages(
            imageFileUris,
            false,
            PdfConfig.defaultConfig().copy(pageSize = PageSize.A4)
        ) ?: return

        showToast("The encrypted pdf was written to: $encryptedDestination")

        // TODO: to open it you should use
        //  to open it as inputStream:
        //  fileIOProcessor.openFileInputStream(encryptedDestination)
        //  or
        //  if it is needed to write an unencrypted file to the persistent storage:
        //  fileIOProcessor.copyRaw(encryptedDestination, getExternalFile("unencryptedPdf.pdf"))
    }

    private fun copyFromAssetsToInternal(filename: String): File {
        val internalFile = filesDir.resolve(filename)
        internalFile.writeBytes(assets.open(filename).readBytes())
        return internalFile
    }

    private fun showToast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    private fun getExternalFile(filename: String) = getExternalFilesDir(null)?.resolve(filename)!!

    private fun loadBitmapFromAssets(filePath: String): Bitmap = BitmapFactory.decodeStream(assets.open(filePath))
}
