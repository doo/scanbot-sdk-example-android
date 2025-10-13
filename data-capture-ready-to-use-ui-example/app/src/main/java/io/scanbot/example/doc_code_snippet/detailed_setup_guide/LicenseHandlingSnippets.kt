package io.scanbot.example.doc_code_snippet.detailed_setup_guide

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import io.scanbot.sdk.*
import io.scanbot.sdk.licensing.*
import io.scanbot.sdk.util.log.*

/*
    NOTE: this snippet of code is to be used only as a part of the website documentation.
    This code is not intended for any use outside of the support of documentation by Scanbot SDK GmbH employees.
*/

// NOTE for maintainers: whenever changing this code,
// ensure that links using it are still pointing to valid lines!
// Pay attention to imports adding/removal/sorting!
// Page URLs using this code:
// TODO: add URLs here

fun checkLicenseStatusSnippet(activity: AppCompatActivity) {
    // @Tag("Check License Status")
    // Check the license status:
    val licenseInfo = ScanbotSDK(activity).licenseInfo
    LoggerProvider.logger.d("ExampleApplication", "License status: ${licenseInfo.status}")
    LoggerProvider.logger.d("ExampleApplication", "License isValid: ${licenseInfo.isValid}")
    LoggerProvider.logger.d(
        "ExampleApplication",
        "License message: ${licenseInfo.licenseStatusMessage}"
    )

    if (licenseInfo.isValid) {
        // Making your call into ScanbotSDK API is now safe.
        // e.g. start document scanner
    }
    // @EndTag("Check License Status")
}

fun handleLicenseStatusSnippet(application: Application) {
    // @Tag("Handle License Status")
    val licenseInfo = ScanbotSDKInitializer()
        .license(application, "YOUR_SCANBOT_SDK_LICENSE_KEY")
        .licenseErrorHandler { status, feature, message ->
            LoggerProvider.logger.d(
                "ScanbotSDK",
                "license status:${status.name}, message: $message"
            )
            when (status) {
                LicenseStatus.OKAY,
                LicenseStatus.TRIAL -> {

                }

                LicenseStatus.OKAY_EXPIRING_SOON -> {

                }

                LicenseStatus.FAILURE_NOT_SET,
                LicenseStatus.FAILURE_CORRUPTED,
                LicenseStatus.FAILURE_WRONG_OS,
                LicenseStatus.FAILURE_APP_ID_MISMATCH,
                LicenseStatus.FAILURE_EXPIRED,
                LicenseStatus.FAILURE_SERVER,
                LicenseStatus.FAILURE_VERSION,
                LicenseStatus.FAILURE_INACTIVE -> {

                }
            }
        }
        .initialize(application)
    // @EndTag("Handle License Status")
}