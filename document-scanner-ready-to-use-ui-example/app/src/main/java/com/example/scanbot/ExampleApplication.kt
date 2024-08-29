package com.example.scanbot

import android.app.Application
import android.widget.Toast
import io.scanbot.sap.IScanbotSDKLicenseErrorHandler
import io.scanbot.sap.Status
import io.scanbot.sdk.ScanbotSDKInitializer
import io.scanbot.sdk.core.contourdetector.ContourDetector
import io.scanbot.sdk.persistence.CameraImageFormat
import io.scanbot.sdk.persistence.page.PageStorageSettings

class ExampleApplication : Application() {

    companion object {
        /**
         * TODO Add the Scanbot Document Scanner SDK license key here.
         * Please note: Scanbot Document Scanner SDK will run without a license key for one minute per session!
         * After the trial period has expired all SDK features and UI components will stop working.
         * You can get a free "no-strings-attached" trial license key. Please submit the trial license
         * form (https://scanbot.io/trial) on our website by using the app identifier
         * "io.scanbot.example.document.usecases.android" of this example app.
         */
        private const val LICENSE_KEY =
            "NKSiws2QDx4SFHFxYdVObJELXs9OfW" +
                "glPlw2iOKnh1e/msha/6SoZsEFwm1j" +
                "yorWaVmWjZIcJy8dzojOZH1wwZE7EV" +
                "kTetS+KLW8eTWXftlHbLybYig5BK09" +
                "vtTVYuRmTqUy31I3UPNtMdRhQgMnqd" +
                "dCcB6ogoM0JvdLwvlBKWd/9zI8xHoz" +
                "gSYG0IkANbL6hhWXuioZzLRWD2uTpv" +
                "I04yTptMaEHFImrtPg0nCoviEASFzB" +
                "8/2QAjAFuoT3LCMriAD7cqvGP/XHSb" +
                "H19XI8WiUedmCVOWX3ixKjcexyBtii" +
                "I38CfcgRp3wH564K1/HEFJJoqcWpUd" +
                "eXBDo+2IDItQ==\nU2NhbmJvdFNESw" +
                "pkb28uc2NhbmJvdC5jYXBhY2l0b3Iu" +
                "ZXhhbXBsZXxpby5zY2FuYm90LmV4YW" +
                "1wbGUuZG9jdW1lbnQudXNlY2FzZXMu" +
                "YW5kcm9pZHxpby5zY2FuYm90LmV4YW" +
                "1wbGUuZmx1dHRlcnxpby5zY2FuYm90" +
                "LmV4YW1wbGUuc2RrLmFuZHJvaWR8aW" +
                "8uc2NhbmJvdC5leGFtcGxlLnNkay5i" +
                "YXJjb2RlLmFuZHJvaWR8aW8uc2Nhbm" +
                "JvdC5leGFtcGxlLnNkay5iYXJjb2Rl" +
                "LmNhcGFjaXRvcnxpby5zY2FuYm90Lm" +
                "V4YW1wbGUuc2RrLmJhcmNvZGUuZmx1" +
                "dHRlcnxpby5zY2FuYm90LmV4YW1wbG" +
                "Uuc2RrLmJhcmNvZGUuaW9uaWN8aW8u" +
                "c2NhbmJvdC5leGFtcGxlLnNkay5iYX" +
                "Jjb2RlLm1hdWl8aW8uc2NhbmJvdC5l" +
                "eGFtcGxlLnNkay5iYXJjb2RlLm5ldH" +
                "xpby5zY2FuYm90LmV4YW1wbGUuc2Rr" +
                "LmJhcmNvZGUucmVhY3RuYXRpdmV8aW" +
                "8uc2NhbmJvdC5leGFtcGxlLnNkay5i" +
                "YXJjb2RlLndpbmRvd3N8aW8uc2Nhbm" +
                "JvdC5leGFtcGxlLnNkay5iYXJjb2Rl" +
                "LnhhbWFyaW58aW8uc2NhbmJvdC5leG" +
                "FtcGxlLnNkay5iYXJjb2RlLnhhbWFy" +
                "aW4uZm9ybXN8aW8uc2NhbmJvdC5leG" +
                "FtcGxlLnNkay5jYXBhY2l0b3J8aW8u" +
                "c2NhbmJvdC5leGFtcGxlLnNkay5jYX" +
                "BhY2l0b3IuYW5ndWxhcnxpby5zY2Fu" +
                "Ym90LmV4YW1wbGUuc2RrLmNhcGFjaX" +
                "Rvci5pb25pY3xpby5zY2FuYm90LmV4" +
                "YW1wbGUuc2RrLmNhcGFjaXRvci5pb2" +
                "5pYy5yZWFjdHxpby5zY2FuYm90LmV4" +
                "YW1wbGUuc2RrLmNhcGFjaXRvci5pb2" +
                "5pYy52dWVqc3xpby5zY2FuYm90LmV4" +
                "YW1wbGUuc2RrLmNvcmRvdmEuaW9uaW" +
                "N8aW8uc2NhbmJvdC5leGFtcGxlLnNk" +
                "ay5mbHV0dGVyfGlvLnNjYW5ib3QuZX" +
                "hhbXBsZS5zZGsuaW9zLmJhcmNvZGV8" +
                "aW8uc2NhbmJvdC5leGFtcGxlLnNkay" +
                "5pb3MuY2xhc3NpY3xpby5zY2FuYm90" +
                "LmV4YW1wbGUuc2RrLmlvcy5ydHV1aX" +
                "xpby5zY2FuYm90LmV4YW1wbGUuc2Rr" +
                "Lm1hdWl8aW8uc2NhbmJvdC5leGFtcG" +
                "xlLnNkay5tYXVpLnJ0dXxpby5zY2Fu" +
                "Ym90LmV4YW1wbGUuc2RrLm5ldHxpby" +
                "5zY2FuYm90LmV4YW1wbGUuc2RrLnJl" +
                "YWN0bmF0aXZlfGlvLnNjYW5ib3QuZX" +
                "hhbXBsZS5zZGsucmVhY3QubmF0aXZl" +
                "fGlvLnNjYW5ib3QuZXhhbXBsZS5zZG" +
                "sucnR1LmFuZHJvaWR8aW8uc2NhbmJv" +
                "dC5leGFtcGxlLnNkay54YW1hcmlufG" +
                "lvLnNjYW5ib3QuZXhhbXBsZS5zZGsu" +
                "eGFtYXJpbi5mb3Jtc3xpby5zY2FuYm" +
                "90LmV4YW1wbGUuc2RrLnhhbWFyaW4u" +
                "cnR1fGlvLnNjYW5ib3QuZm9ybXMubm" +
                "F0aXZlcmVuZGVyZXJzLmV4YW1wbGV8" +
                "aW8uc2NhbmJvdC5uYXRpdmViYXJjb2" +
                "Rlc2RrcmVuZGVyZXJ8aW8uc2NhbmJv" +
                "dC5TY2FuYm90U0RLU3dpZnRVSURlbW" +
                "98aW8uc2NhbmJvdC5zZGtfd3JhcHBl" +
                "ci5kZW1vLmJhcmNvZGV8aW8uc2Nhbm" +
                "JvdC5zZGstd3JhcHBlci5kZW1vLmJh" +
                "cmNvZGV8aW8uc2NhbmJvdC5zZGsuaW" +
                "50ZXJuYWxkZW1vfGxvY2FsaG9zdHxP" +
                "cGVyYXRpbmdTeXN0ZW1TdGFuZGFsb2" +
                "5lfHNjYW5ib3RzZGstcWEtMS5zMy1l" +
                "dS13ZXN0LTEuYW1hem9uYXdzLmNvbX" +
                "xzY2FuYm90c2RrLXFhLTIuczMtZXUt" +
                "d2VzdC0xLmFtYXpvbmF3cy5jb218c2" +
                "NhbmJvdHNkay1xYS0zLnMzLWV1LXdl" +
                "c3QtMS5hbWF6b25hd3MuY29tfHNjYW" +
                "5ib3RzZGstcWEtNC5zMy1ldS13ZXN0" +
                "LTEuYW1hem9uYXdzLmNvbXxzY2FuYm" +
                "90c2RrLXFhLTUuczMtZXUtd2VzdC0x" +
                "LmFtYXpvbmF3cy5jb218c2NhbmJvdH" +
                "Nkay13YXNtLWRlYnVnaG9zdC5zMy1l" +
                "dS13ZXN0LTEuYW1hem9uYXdzLmNvbX" +
                "x3ZWJzZGstZGVtby1pbnRlcm5hbC5z" +
                "Y2FuYm90LmlvfCoucWEuc2NhbmJvdC" +
                "5pbwoxNzI3ODI3MTk5CjgzODg2MDcK" +
                "MzE=\n" // "YOUR_SCANBOT_SDK_LICENSE_KEY"
        const val USE_ENCRYPTION = false
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize the Scanbot SDK:
        ScanbotSDKInitializer()
            .withLogging(true)
            .contourDetectorType(ContourDetector.Type.ML_BASED)
            .usePageStorageSettings(
                PageStorageSettings.Builder()
                    .imageFormat(CameraImageFormat.JPG)
                    .imageQuality(80)
                    .build()
            )
            .licenceErrorHandler(IScanbotSDKLicenseErrorHandler { status, sdkFeature, errorMessage ->
                when (status) {
                    Status.StatusFailureNotSet,
                    Status.StatusFailureCorrupted,
                    Status.StatusFailureWrongOS,
                    Status.StatusFailureAppIDMismatch,
                    Status.StatusFailureExpired -> {
                        Toast.makeText(this, "License error: $status ", Toast.LENGTH_LONG).show()
                    }
                    else -> { /* Can be empty for the purpose of this example. */ }
                }

            })
            .useFileEncryption(USE_ENCRYPTION)
            .license(this, LICENSE_KEY)
            .initialize(this)
    }
}
