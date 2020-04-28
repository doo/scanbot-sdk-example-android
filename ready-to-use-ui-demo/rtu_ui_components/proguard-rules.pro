
# Core lib

-ignorewarnings

-keep public class net.doo.snap.ui.** { *; }
-keep public class io.scanbot.sdk.ui.** { *; }

-keep public class io.scanbot.sap.SapManager { *; }

-keeppackagenames net.doo.snap.lib.detector.**
-keep public class net.doo.snap.lib.detector.**{ *; }

-keeppackagenames com.googlecode.tesseract.android.**
-keep public class com.googlecode.tesseract.android.**{ *; }

-keeppackagenames io.scanbot.payformscanner.**
-keep public class io.scanbot.payformscanner.**{ *; }

-keeppackagenames io.scanbot.mrzscanner.**
-keep public class io.scanbot.mrzscanner.**{ *; }

-keeppackagenames io.scanbot.dcscanner.**
-keep public class io.scanbot.dcscanner.**{ *; }

-keeppackagenames io.scanbot.tiffwriter.**
-keep public class io.scanbot.tiffwriter.**{ *; }

-keeppackagenames io.scanbot.chequescanner.**
-keep public class io.scanbot.chequescanner.**{ *; }

-keeppackagenames io.scanbot.textorientation.**
-keep public class io.scanbot.textorientation.**{ *; }

-keeppackagenames io.scanbot.barcodescanner.**
-keep public class io.scanbot.barcodescanner.**{ *; }

-keeppackagenames io.scanbot.hicscanner.**
-keep public class io.scanbot.hicscanner.**{ *; }

-keeppackagenames io.scanbot.multipleobjectsscanner.**
-keep public class io.scanbot.multipleobjectsscanner.**{ *; }

-keeppackagenames io.scanbot.sdk.process.**
-keep public class io.scanbot.sdk.process.**{ *; }

# Gson
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**

-keep class com.google.gson.stream.** { *; }

# Prevent proguard from stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

##---------------End: proguard configuration for Gson  ----------