## ScanbotSDK Xamarin Demo

### How it works?

Check out the docs:
  - Scanbot SDK for Xamarin: https://scanbotsdk.github.io/documentation/xamarin/
  - JavaDocs: http://doo.github.io/Scanbot-SDK-Documentation/Android/

    The JavaDocs are generated for the Java code, but C# namespaces and names of classes are the same as in Java packages. (net.doo.snap.camera.ScanbotCameraView in Java == Net.Doo.Snap.Camera.ScanbotCameraView in C#).
    C# specific documentation will be added later.

### Required tools to work with the Xamarin Demo projects:

1. [Xamarin Studio](https://www.xamarin.com/studio)

### Building ScanbotCameraViewDemo project:

1. Open the ScanbotCameraViewDemo.sln solution
2. Add the NuGet package "ScanbotSDK.Xamarin.Android" with the latest version from nuget.org (Project -> Add NuGet Packages...)
3. Make sure the following Android SDKs and Tools are installed in the Android SDK Manager (Tools -> SDK Manager)
   * Android SDK Tools (latest version)
   * Android SDK Platform-Tools (latest version)
   * Android SDK Build-tools (latest version)
   * Android 5.1.1 (API 22)
   * Android 4.0.3 (API 15)
4. You can now build and run the demo project.

### What is the latest version of the Scanbot SDK?

Current version is 1.22.3

### Why example is not working / stopped working

Either your trial period is expired or you set the license incorrectly. Please, double check that.

### Note

Please kindly take note of the following points when downloading the SDK:

- This is a trial version and will only work for 1 minute (if you require a trial license which works for a longer period of time please contact us).
- We are constantly updating and evolving the SDK and it is no final product.
- The SDK with a trial license should only be tested in a experimental setting and it is not developed to be integrated into your live products.

