package io.scanbot.example

import android.util.Log
import android.widget.Toast
import androidx.multidex.MultiDexApplication
import io.scanbot.example.repository.PageRepository
import io.scanbot.example.util.SharingCopier
import io.scanbot.sap.IScanbotSDKLicenseErrorHandler
import io.scanbot.sap.Status
import io.scanbot.sdk.ScanbotSDKInitializer
import io.scanbot.sdk.persistence.CameraImageFormat
import io.scanbot.sdk.persistence.PageStorageSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.CoroutineContext

class Application : MultiDexApplication(), CoroutineScope {

    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    companion object {
        /*
         * TODO Add the Scanbot SDK license key here.
         * Please note: The Scanbot SDK will run without a license key for one minute per session!
         * After the trial period is over all Scanbot SDK functions as well as the UI components will stop working.
         * You can get an unrestricted "no-strings-attached" 30 day trial license key for free.
         * Please submit the trial license form (https://scanbot.io/sdk/trial.html) on our website by using
         * the app identifier "io.scanbot.example.sdk.rtu.android" of this example app.
         */
        const val LICENSE_KEY = ""
    }

    override fun onCreate() {
        super.onCreate()
        val sdkLicenseInfo = ScanbotSDKInitializer()
                .withLogging(BuildConfig.DEBUG)
                // Optional, custom SDK files directory. Please see the comments below!
                .sdkFilesDirectory(this, customStorageDirectory())
                .usePageStorageSettings(
                        PageStorageSettings.Builder()
                                .imageFormat(CameraImageFormat.JPG)
                                .imageQuality(80)
                                .previewTargetMax(1500)
                                .build()
                )
                .prepareOCRLanguagesBlobs(true)
                .prepareMRZBlobs(true)
                .prepareDcBlobs(true)
                .preparePayFormBlobs(true)
                .prepareBarcodeScannerBlobs(true)
                .licenceErrorHandler(IScanbotSDKLicenseErrorHandler { status, feature ->
                    // Optional license failure handler implementation. Handle license issues here.
                    // A license issue can either be an invalid or expired license key
                    // or missing SDK feature (see SDK feature packages on https://scanbot.io).
                    val errorMsg = if (status != Status.StatusOkay && status != Status.StatusTrial) {
                        "License Error! License status: ${status.name}"
                    }
                    else {
                        "License Error! Missing SDK feature in license: ${feature.name}"
                    }
                    Log.d("ScanbotSDKExample", errorMsg)
                    Toast.makeText(this@Application, errorMsg, Toast.LENGTH_LONG).show()
                })
                .license(this, LICENSE_KEY)
                .initialize(this)

        // Check the Scanbot SDK license status:
        Log.d("ScanbotSDKExample", "Is license valid: " + sdkLicenseInfo.isValid)
        Log.d("ScanbotSDKExample", "License status " + sdkLicenseInfo.status.name)

        launch {
            PageRepository.clearPages(this@Application)
            SharingCopier.clear(this@Application)
        }
    }

    private fun customStorageDirectory(): File {
        // !! Please note !!
        // It is strongly recommended to use the default (secure) storage directory of the Scanbot SDK.
        // However, for demo purposes we use a custom external(!) storage directory here, which is a public(!) folder.
        // All image files and export files (PDF, TIFF, etc) created by the Scanbot SDK in this demo app will be stored
        // in this public storage directory and will be accessible for every(!) app having external storage permissions!
        // Again, this is only for demo purposes, which allows us to easily fetch and check the generated files
        // via Android "adb" CLI tools, Android File Transfer app, Android Studio, etc.
        //
        // For more details about the storage system of the Scanbot SDK please see our docs:
        // https://github.com/doo/scanbot-sdk-example-android/wiki/Storage
        //
        // For more details about the file system on Android we also highly recommend to check out:
        // - https://developer.android.com/guide/topics/data/data-storage
        // - https://developer.android.com/training/data-storage/files

        val customDir = File(this.getExternalFilesDir(null), "my-custom-storage-folder")
        customDir.mkdirs()
        return customDir
    }

}
