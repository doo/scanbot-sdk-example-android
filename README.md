<p align="left">
  <img src=".images/ScanbotSDKLogo.png#gh-light-mode-only" width="15%" />
</p>
<p align="left">
  <img src=".images/ScanbotSDKLogo_darkmode.png#gh-dark-mode-only" width="15%" />
</p>

# Example app for the Scanbot Android Document Scanner SDK and Data Capture Modules

This example app shows how to integrate the [Scanbot Document Scanner SDK](https://scanbot.io/document-scanner-sdk/?utm_source=github.com&utm_medium=referral&utm_campaign=dev_sites) and [Scanbot Data Capture Modules](https://scanbot.io/data-capture-software/?utm_source=github.com&utm_medium=referral&utm_campaign=dev_sites) for Android. 

## What is the Scanbot SDK?

The Scanbot SDK is a set of high-level APIs that lets you integrate document scanning and data extraction functionalities into your mobile apps and websites. It runs on all common mobile devices and operates entirely offline. No data is transmitted to our or third-party servers.

With our Ready-To-Use UI (RTU UI) components, you can integrate the Scanbot SDK into your app in less than an hour.

💡 For more details about the Scanbot Document Scanner SDK and Data Capture Modules, please check out our [documentation](https://docs.scanbot.io/document-scanner-sdk/android/introduction/?utm_source=github.com&utm_medium=referral&utm_campaign=dev_sites).

## How to run this example app?

Make sure you have the latest stable version of [Android Studio](https://developer.android.com/studio) installed.

1. Clone this repository to a local folder.

2. There are 3 separate folders with standalone examples:  

	* **classic-components-example**: Example project for the Scanbot SDK Classic Components which allow for full customization. 
	* **document-scanner-ready-to-use-ui-example:** Example project for the Scanbot Document Scanner SDK using our Ready-To-Use UI components.
	* **data-capture-ready-to-use-ui-example**: Example project showcasing the Scanbot Data Capture Modules using our Ready-to-Use UI components.

3. Open one of the 3 separate projects in Android Studio, build, and run.

## Overview of the Scanbot SDK

### Document Scanner SDK

The Scanbot Android Document Scanner SDK offers the following features:

* **User guidance**: Ease of use is crucial for large user bases. Our on-screen user guidance helps even non-tech-savvy users create perfect scans.

* **Automatic capture**: The SDK automatically captures the document when the device is optimally positioned over the document. This reduces the risk of blurry or incomplete document scans compared to manually triggered capture.

* **Automatic cropping**: Our document scanning SDK automatically straightens and crops scanned documents, ensuring high-quality document scan results.

* **Custom filters:** Every document scanning use case has specific image requirements. With the SDK’s custom filters, you can tailor the document scanning image output to your backend system. They include grayscale options, multiple binarizations, and other settings to optimize your document scanning for various document types.

* **Document Quality Analyzer:** This feature automatically rates the quality of the scanned pages from “very poor” to “excellent.” If the quality is below a specified threshold, the SDK prompts the user to rescan. 

* **Export formats:** The Scanbot Document Scanner SDK supports several output formats for exporting digitized documents (JPG, PDF, TIFF, and PNG). This ensures your downstream solutions receive the best format to store, print, or share the digitized document – or to process it further. 

| ![User guidance](.images/android-user-guidance.png) | ![Automatic capture](.images/android-auto-capture.png) | ![Automatic cropping](.images/android-auto-crop.png) |
| :-- | :-- | :-- |

### Data Capture Modules

The Scanbot SDK Data Capture Modules allow you to extract data from a wide range of structured documents and to integrate OCR text recognition capabilities. They include:

#### [MRZ Scanner](https://scanbot.io/data-capture-software/mrz-scanner/?utm_source=github.com&utm_medium=referral&utm_campaign=dev_sites) 
This module allows quick and accurate data extraction from the machine-readable zones on identity documents. It captures all important MRZ data from IDs and passports and returns it in the form of simple key-value pairs. This is much simpler, faster, and less mistake-prone than manual data entry.

#### [Check Scanner (MICR)](https://scanbot.io/data-capture-software/check-scanner/?utm_source=github.com&utm_medium=referral&utm_campaign=dev_sites)
The MICR Scanner offers reliable data extraction from international paper checks, capturing check numbers, routing numbers, and account numbers from MICR codes. This simplifies workflows and reduces errors that frustrate customers and employees.

#### [Text Pattern Scanner](https://scanbot.io/data-capture-software/data-scanner/?utm_source=github.com&utm_medium=referral&utm_campaign=dev_sites)
Our Text Pattern Scanner allows quick and accurate extraction of single-line data. It captures information based on customizable patterns tailored to your specific use case. This replaces error-prone manual data entry with automatic capture.

#### [VIN Scanner](https://scanbot.io/data-capture-software/vin-scanner/?utm_source=github.com&utm_medium=referral&utm_campaign=dev_sites)
The VIN scanner enables instant capture of vehicle identification numbers (VINs) from trucks or car doors. It uses OCR to convert the image of the VIN code into structured data for backend processing. This module integrates into mobile or web-based fleet management applications, enabling you to replace error-prone manual entry with fast, reliable data extraction.

#### Document Data Extractor
Through this feature, our SDK offers document detection and data capture capabilities for a wider range of documents. It accurately identifies and crops various standardized document types including [German ID cards](https://scanbot.io/data-capture-software/id-scanner/?utm_source=github.com&utm_medium=referral&utm_campaign=dev_sites), passports, [driver's licenses](https://scanbot.io/data-capture-software/german-drivers-license-scanner/?utm_source=github.com&utm_medium=referral&utm_campaign=dev_sites), [residence permits](https://scanbot.io/data-capture-software/residence-permit-scanner/?utm_source=github.com&utm_medium=referral&utm_campaign=dev_sites), and the [EHIC](https://scanbot.io/data-capture-software/ehic-scanner/?utm_source=github.com&utm_medium=referral&utm_campaign=dev_sites). It uses the Scanbot OCR engine for accurate data field recognition, without requiring additional OCR language files.

| ![MRZ Scanner](.images/android-mrz-scanner.png) | ![VIN Scanner](.images/android-vin-scanner.png) | ![Check Scanner](.images/android-check-scanner.png) |
| :-- | :-- | :-- |

## Additional information

### Free integration support

Need help integrating or testing our Document Scanner SDK? We offer [free developer support](https://docs.scanbot.io/support/?utm_source=github.com&utm_medium=referral&utm_campaign=dev_sites) via Slack, MS Teams, or email.

As a customer, you also get access to a dedicated support Slack or Microsoft Teams channel to talk directly to your Customer Success Manager and our engineers.

### Trial licensing and pricing

The Scanbot SDK examples will run one minute per session without a license. After that, all functionalities and UI components will stop working. 

To try the Scanbot SDK without a one-minute limit, you can request a free, no-strings-attached [7-day trial license](https://scanbot.io/trial/?utm_source=github.com&utm_medium=referral&utm_campaign=dev_sites). 

Alternatively, check out our [demo apps](https://scanbot.io/demo-apps/?utm_source=github.com&utm_medium=referral&utm_campaign=dev_sites) to test the SDK.

Our pricing model is simple: Unlimited document scanning for a flat annual license fee, full support included. There are no tiers, usage charges, or extra fees. [Contact](https://scanbot.io/contact-sales/?utm_source=github.com&utm_medium=referral&utm_campaign=dev_sites) our team to receive your quote. 

### Other supported platforms

Besides Android, the Scanbot SDK is also available on iOS and most common cross-platform environments, such as React Native, Flutter, or .NET MAUI: 

* [iOS](https://github.com/doo/scanbot-sdk-example-ios) (native)
* [JavaScript](https://github.com/doo/scanbot-sdk-example-web)
* [Flutter](https://github.com/doo/scanbot-sdk-example-flutter)
* [Capacitor & Ionic (Angular)](https://github.com/doo/scanbot-sdk-example-capacitor-ionic)
* [Capacitor & Ionic (React)](https://github.com/doo/scanbot-sdk-example-ionic-react)
* [Capacitor & Ionic (Vue.js)](https://github.com/doo/scanbot-sdk-example-ionic-vuejs)
* [Cordova & Ionic](https://github.com/doo/scanbot-sdk-example-ionic) 
* [.NET MAUI](https://github.com/doo/scanbot-sdk-maui-example)
* [React Native](https://github.com/doo/scanbot-sdk-example-react-native)
* [Xamarin](https://github.com/doo/scanbot-sdk-example-xamarin) & [Xamarin.Forms](https://github.com/doo/scanbot-sdk-example-xamarin-forms)

Our Barcode Scanner SDK additionally also supports [Compose Multiplatform / KMP](https://github.com/doo/scanbot-barcode-scanner-sdk-example-kmp), [UWP](https://github.com/doo/scanbot-barcode-scanner-sdk-example-windows) (Windows), and [Linux](https://github.com/doo/scanbot-sdk-example-linux).
