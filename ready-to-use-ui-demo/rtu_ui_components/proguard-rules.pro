# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-dontshrink
-verbose
-ignorewarnings
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*,!code/allocation/variable
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature,InnerClasses

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

-keep class * implements android.os.Parcelable {*;}

-keepattributes Exceptions

-keep interface android.support.v7.** { *; }
-keep class !android.support.v7.internal.view.menu.**, android.support.** {*;}

-keep public class * extends android.support.v7.app.ActionBarActivity { *; }
-keep class android.support.v7.widget.** { *; }

# Scanbot SDK

-keep public class net.doo.snap.ScanbotSDKInitializer {
    public <methods>;
}
-keep public class net.doo.snap.ScanbotSDK {
    public <methods>;
}
-keep public class net.doo.snap.process.draft.** {
    public <methods>;
}
-keep public class net.doo.snap.Constants { *; }
-keep public class net.doo.snap.persistence.cleanup.Cleaner {
    public <methods>;
}
-keep public class net.doo.snap.process.DocumentProcessingResult {
    public <methods>;
}
-keep public class net.doo.snap.process.DocumentProcessor {
    public <methods>;
}
-keep public class net.doo.snap.util.thread.MimeUtils {
    public <methods>;
}
-keep public class net.doo.snap.ui.themes.ThemesProvider {
    public <methods>;
}
-keep public class net.doo.snap.intelligence.DocumentClassifier {
    public <methods>;
}
-keep public class net.doo.snap.intelligence.StubDocumentClassifier {
    public <methods>;
}
-keep public class net.doo.snap.process.compose.ComposerFactory {
    public <methods>;
}
-keep public class net.doo.snap.process.compose.BaseComposerFactory {
    public <methods>;
}
-keep public class net.doo.snap.smartname.SmartNameGenerator {
    public <methods>;
}
-keep public class net.doo.snap.smartname.StubSmartNameGenerator {
    public <methods>;
}
-keep public class net.doo.snap.persistence.cleanup.UnreferencedSourcesProvider {
    public <methods>;
}
-keep public class net.doo.snap.persistence.cleanup.BaseUnreferencedSourcesProvider {
    public <methods>;
}
-keep public class io.scanbot.sap.SapManager { *; }
-keep public class net.doo.snap.entity.Language { *; }

-keep public class net.doo.snap.util.FileUtils {
    public <methods>;
}
-keep public class net.doo.snap.util.ManifestConstantsProvider  { *; }
-keep public class net.doo.snap.PreferencesConstants  { *; }
-keep public class net.doo.snap.util.CursorUtil {
    public <methods>;
}
-keep public class net.doo.snap.PreferencesConstants  { *; }
-keep public class net.doo.snap.util.log.DebugLog {
    public <methods>;
}
-keep public class net.doo.snap.persistence.DocumentStoreStrategy {
    public <methods>;
}
-keep public class net.doo.snap.entity.DocumentType {
    public <methods>;
}
-keep public class net.doo.snap.entity.OcrStatus { *; }
-keep public class net.doo.snap.process.PDFProcessor {
    public <methods>;
}
-keep public class net.doo.snap.persistence.PageStoreStrategy {
    public <methods>;
}
-keep public class net.doo.snap.util.device.DeviceUtils {
    public <methods>;
}
-keep public class net.doo.snap.process.compose.Composer {
    public <methods>;
}
-keep public class net.doo.snap.process.compose.JpegComposer {
    public <methods>;
}
-keep public class net.doo.snap.process.compose.SimpleComposer {
    public <methods>;
}
-keep public class net.doo.snap.process.compose.DummyComposer {
    public <methods>;
}
-keep class net.doo.snap.process.compose.ComposerFactory { *; }
-keep class net.doo.snap.process.compose.ComposerFactory$* { *; }
-keep public class net.doo.snap.entity.Page { *; }
-keep enum net.doo.snap.entity.Page$* { *; }
-keep public class net.doo.snap.entity.RotationType { *; }
-keep public class net.doo.snap.util.** { *; }
-keep public class net.doo.snap.IntentExtras { *; }
-keep public class net.doo.snap.ui.edit.events.DocumentRenamedEvent {
    public <methods>;
}
-keep public class net.doo.snap.process.DocumentLockProvider {
    public <methods>;
}
-keep public class net.doo.snap.ui.widget.TermSpanDrawable {
    public <methods>;
}
-keep public class net.doo.snap.interactor.addon.CheckConnectionUseCase.ConnectionChecker {
    public <methods>;
}
-keep public class net.doo.snap.process.DocumentLockProvider {
    public <methods>;
}
-keep public class net.doo.snap.ui.ScanbotDialogFragment { *; }
-keep public class net.doo.snap.BuildConfig { *; }
-keep public class net.doo.snap.ui.themes.ThemesProvider { *; }
-keep public class net.doo.snap.entity.DocumentType { *; }
-keep public class net.doo.snap.ui.edit.RenameDocumentFragment { *; }
-keep public class net.doo.snap.ui.camera.CameraPreviewFragment { *; }
-keep public class net.doo.snap.ui.camera.ScanbotCameraFragment { *; }
-keep public class net.doo.snap.ui.camera.ScanbotCameraFragment* { *; }
-keep public class net.doo.snap.ui.BarcodeFragment { *; }
-keep public class net.doo.snap.persistence.PageFactory {
    public <methods>;
}
-keep public class net.doo.snap.blob.BlobManager { *; }
-keep public class net.doo.snap.blob.BlobFactory { *; }
-keep public class net.doo.snap.entity.Blob { *; }
-keep public class net.doo.snap.persistence.BlobStoreStrategy { *; }

-keep public class net.doo.snap.persistence.PageFactory$Result { *; }

-keep public class net.doo.snap.process.TextRecognition { *; }
-keep public class net.doo.snap.process.TextRecognizer {
    public <methods>;
}
-keep public class net.doo.snap.process.OcrPerformer { *; }
-keep public class net.doo.snap.process.OcrResult { *; }

-keep public class net.doo.snap.camera.** { *; }
-keep public class net.doo.snap.R { *; }

-keep public class net.doo.snap.ui.** { *; }

-keepclassmembers class * {
    void *(**Draft);
}

-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
    public void get*(...);
}

-keep public class * extends com.google.inject.AbstractModule {*;}

-keeppackagenames net.doo.snap.lib.detector.**
-keep public class net.doo.snap.lib.detector.**{ *; }

-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}

-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}

-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}

-keeppackagenames com.googlecode.tesseract.android.**
-keep public class com.googlecode.tesseract.android.**{ *; }

-keeppackagenames io.scanbot.payformscanner.**
-keep public class io.scanbot.payformscanner.**{ *; }