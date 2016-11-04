##ScanboSDK Xamarin Binding

###Requiered tools to work with Xamaring wrapper:

1. [Xamarin Studio](https://www.xamarin.com/studio)

###Building ScanbotCameraViewDemo project:

1. Open ScanbotCameraViewDemo.sln solution
2. Add .Net Assembly references (Project -> Edit References -> .Net Assembly):
	* Camera-0.6.17.dll (Download link: http://sdkdownload.scanbot.io.s3-website.eu-central-1.amazonaws.com/xamarin-sdk/android/Camera-0.6.17.dll)
	* Detector-1.5.1.dll (Download link: http://sdkdownload.scanbot.io.s3-website.eu-central-1.amazonaws.com/xamarin-sdk/android/Detector-1.5.1.dll)
	* Licensing-1.4.0.dll (Download link: http://sdkdownload.scanbot.io.s3-website.eu-central-1.amazonaws.com/xamarin-sdk/android/Licensing-1.4.0.dll)
	* ScanbotSDK-1.20.0.dll (Download link: http://sdkdownload.scanbot.io.s3-website.eu-central-1.amazonaws.com/xamarin-sdk/android/ScanbotSDK-1.20.0.dll)
	* ScanbotSDK1-1.20.0.dll (Download link: http://sdkdownload.scanbot.io.s3-website.eu-central-1.amazonaws.com/xamarin-sdk/android/ScanbotSDK1-1.20.0.dll)
3. Open Android SDK Manager	and download 
	* Android SDK Tools (latest version)
	* Android SDK Platform-Tools (latest version)
	* Android SDK Build-tools (latest version)
	* Android 5.1.1 (API 22)
	* Android 4.0.3 (API 15)
4. Add NuGet package "Xamarin Android Support Library - v7 AppCompat" v. 22.1.1 (Project -> Add NuGet Packages...)
5. Compile `./Scanbot-SDK-Examples/Xamarin/ScanbotCameraViewDemo/ScanbotCameraViewDemo/ScanbotCameraViewDemo.csproj`.
6. You can now run demo project: `./Scanbot-SDK-Examples/Xamarin/ScanbotCameraViewDemo/ScanbotCameraViewDemo/ScanbotCameraViewDemo.csproj`.


