## ScanbotSDK Xamarin Demo

### How it works?

Check out JavaDocs: http://doo.github.io/Scanbot-SDK-Documentation/Android/

Documentation is generated for the Java code, but C# namespaces and names of classes are the same as in Java packages. (net.doo.snap.camera.ScanbotCameraView in Java == Net.Doo.Snap.Camera.ScanbotCameraView in C#)
C# specific documentation will be added later.

### Required tools to work with the Xamarin Demo projects:

1. [Xamarin Studio](https://www.xamarin.com/studio)

### Building ScanbotCameraViewDemo project:

1. Open the ScanbotCameraViewDemo.sln solution
2. Add .Net Assembly references (Project -> Edit References -> .Net Assembly):
	* Camera.dll - Contains core camera functionality. (Download link: http://sdkdownload.scanbot.io.s3-website.eu-central-1.amazonaws.com/xamarin-sdk/android/camera/0.6.17/Camera.dll)
	* Detector.dll - Contains core page contour detection functionality. (Download link: http://sdkdownload.scanbot.io.s3-website.eu-central-1.amazonaws.com/xamarin-sdk/android/detector/1.5.1/Detector.dll)
	* Licensing.dll - Contains SDK licensing functionality. (Download link: http://sdkdownload.scanbot.io.s3-website.eu-central-1.amazonaws.com/xamarin-sdk/android/licensing/1.4.0/Licensing.dll)
	* ScanbotSDK.dll - Contains core Scanbot SDK functionality. (Download link: http://sdkdownload.scanbot.io.s3-website.eu-central-1.amazonaws.com/xamarin-sdk/android/scanbotsdk/1.20.0/ScanbotSDK.dll)
	* ScanbotSDK1.dll - Contains Scanbot SDK Package 1 features functionality. (Download link: http://sdkdownload.scanbot.io.s3-website.eu-central-1.amazonaws.com/xamarin-sdk/android/scanbotsdk1/1.20.0/ScanbotSDK1.dll)
3. Open the Android SDK Manager	and download (Tools -> SDK Manager)
	* Android SDK Tools (latest version)
	* Android SDK Platform-Tools (latest version)
	* Android SDK Build-tools (latest version)
	* Android 5.1.1 (API 22)
	* Android 4.0.3 (API 15)
4. Add the NuGet package "Xamarin Android Support Library - v7 AppCompat" v. 22.1.1 (Project -> Add NuGet Packages...)
5. Compile `./Scanbot-SDK-Examples/Xamarin/ScanbotCameraViewDemo/ScanbotCameraViewDemo/ScanbotCameraViewDemo.csproj`.
6. You can now run the demo project: `./Scanbot-SDK-Examples/Xamarin/ScanbotCameraViewDemo/ScanbotCameraViewDemo/ScanbotCameraViewDemo.csproj`.

### What is the latest version of the SDK?

Current version is 1.20.0

### Why example is not working / stopped working

Either your trial period is expired or you set the license incorrectly. Please, double check that.

### Note

Please kindly take note of the following points when downloading the SDK:

- This is a trial version and will only work for 1 minute (if you require a trial license which works for a longer period of time please contact us).
- We are constantly updating and evolving the SDK and it is no final product.
- The SDK with a trial license should only be tested in a experimental setting and it is not developed to be integrated into your live products.

