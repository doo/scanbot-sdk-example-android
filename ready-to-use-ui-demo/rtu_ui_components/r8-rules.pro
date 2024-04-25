
# This is generated automatically by the Android Gradle plugin.
#-dontwarn org.bouncycastle.jsse.BCSSLParameters
#-dontwarn org.bouncycastle.jsse.BCSSLSocket
#-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
#-dontwarn org.conscrypt.Conscrypt$Version
#-dontwarn org.conscrypt.Conscrypt
#-dontwarn org.conscrypt.ConscryptHostnameVerifier
#-dontwarn org.openjsse.javax.net.ssl.SSLParameters
#-dontwarn org.openjsse.javax.net.ssl.SSLSocket
#-dontwarn org.openjsse.net.ssl.OpenJSSE
#
## Keep generic signatures; needed for correct type resolution
#-keepattributes Signature
#
## Keep Gson annotations
## Note: Cannot perform finer selection here to only cover Gson annotations, see also https://stackoverflow.com/q/47515093
#-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
#
#### The following rules are needed for R8 in "full mode" which only adheres to `-keepattribtues` if
#### the corresponding class or field is matches by a `-keep` rule as well, see
#### https://r8.googlesource.com/r8/+/refs/heads/main/compatibility-faq.md#r8-full-mode
#
## Keep class TypeToken (respectively its generic signature) if present
#-if class com.google.gson.reflect.TypeToken
#-keep,allowobfuscation class com.google.gson.reflect.TypeToken
#
## Keep any (anonymous) classes extending TypeToken
#-keep,allowobfuscation class * extends com.google.gson.reflect.TypeToken
#
## Keep classes with @JsonAdapter annotation
#-keep,allowobfuscation,allowoptimization @com.google.gson.annotations.JsonAdapter class *
#
## Keep fields with any other Gson annotation
## Also allow obfuscation, assuming that users will additionally use @SerializedName or
## other means to preserve the field names
#-keepclassmembers,allowobfuscation class * {
#  @com.google.gson.annotations.Expose <fields>;
#  @com.google.gson.annotations.JsonAdapter <fields>;
#  @com.google.gson.annotations.Since <fields>;
#  @com.google.gson.annotations.Until <fields>;
#}
#
## Keep no-args constructor of classes which can be used with @JsonAdapter
## By default their no-args constructor is invoked to create an adapter instance
#-keepclassmembers class * extends com.google.gson.TypeAdapter {
#  <init>();
#}
#-keepclassmembers class * implements com.google.gson.TypeAdapterFactory {
#  <init>();
#}
#-keepclassmembers class * implements com.google.gson.JsonSerializer {
#  <init>();
#}
#-keepclassmembers class * implements com.google.gson.JsonDeserializer {
#  <init>();
#}
#
## Keep fields annotated with @SerializedName for classes which are referenced.
## If classes with fields annotated with @SerializedName have a no-args
## constructor keep that as well. Based on
## https://issuetracker.google.com/issues/150189783#comment11.
## See also https://github.com/google/gson/pull/2420#discussion_r1241813541
## for a more detailed explanation.
#-if class *
#-keepclasseswithmembers,allowobfuscation class <1> {
#  @com.google.gson.annotations.SerializedName <fields>;
#}
#-if class * {
#  @com.google.gson.annotations.SerializedName <fields>;
#}
#-keepclassmembers,allowobfuscation,allowoptimization class <1> {
#  <init>();
#}
#
#-keep class com.google.gson.reflect.TypeToken
#-keep class * extends com.google.gson.reflect.TypeToken
#-keep public class * implements java.lang.reflect.Type
#
###############################################################################
#
#-whyareyoukeeping class io.scanbot.sdk.core.processor.ImageProcessor$ImageRotation
