# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve line number information for debugging stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ---- Retrofit / OkHttp / Gson ----
-keepattributes Signature
-keepattributes *Annotation*

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# Gson
-keep class com.google.gson.** { *; }
-keep class com.zakrni.app.clean.data.models.** { *; }
-keep class com.zakrni.app.clean.domain.models.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ---- Hilt / Dagger ----
-dontwarn dagger.**
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.** { *; }

# ---- AndroidX / Navigation ----
-keep class androidx.navigation.** { *; }

# ---- Glide ----
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { *; }
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}

# ---- ExoPlayer / Media3 ----
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# ---- App specific: keep data classes used by Gson ----
-keepclassmembers class com.zakrni.app.clean.data.models.** {
    <fields>;
    <init>(...);
}
-keepclassmembers class com.zakrni.app.clean.domain.models.** {
    <fields>;
    <init>(...);
}

# ---- ViewBinding ----
-keep class com.zakrni.app.databinding.** { *; }

# ---- Lottie ----
-dontwarn com.airbnb.lottie.**

# ---- Google Play Services ----
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# ---- Google AdMob ----
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

# ---- Google Play Billing ----
-keep class com.android.billingclient.** { *; }
-keep class com.android.vending.billing.** { *; }
-dontwarn com.android.billingclient.**

# ---- App Ads classes ----
-keep class com.zakrni.app.clean.ads.** { *; }

# ---- Strip debug/verbose logs from release builds ----
-assumenosideeffects class android.util.Log {
    public static int d(...);
    public static int v(...);
    public static int i(...);
    public static int w(...);
}