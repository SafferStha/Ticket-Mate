# ─────────────────────────────────────────────────────────────────────────────
# Preserve line numbers and source file names for Crashlytics stack traces.
# ─────────────────────────────────────────────────────────────────────────────
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ─────────────────────────────────────────────────────────────────────────────
# Firebase Realtime Database — data model classes are serialized/deserialized
# via reflection. All fields must keep their original names.
# ─────────────────────────────────────────────────────────────────────────────
-keepclassmembers class com.example.individual_project.data.model.** {
    public <init>();
    public *;
}
-keep class com.example.individual_project.data.model.** { *; }

# ─────────────────────────────────────────────────────────────────────────────
# Firebase SDKs (Auth, Database, Storage, Crashlytics, Analytics)
# ─────────────────────────────────────────────────────────────────────────────
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# ─────────────────────────────────────────────────────────────────────────────
# Crashlytics — preserve mapping metadata
# ─────────────────────────────────────────────────────────────────────────────
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**

# ─────────────────────────────────────────────────────────────────────────────
# Hilt / Dagger — generated component classes must not be stripped
# ─────────────────────────────────────────────────────────────────────────────
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-dontwarn dagger.hilt.**

# ─────────────────────────────────────────────────────────────────────────────
# Kotlin Coroutines
# ─────────────────────────────────────────────────────────────────────────────
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# ─────────────────────────────────────────────────────────────────────────────
# Coil — image loading reflection hooks
# ─────────────────────────────────────────────────────────────────────────────
-dontwarn coil.**

# ─────────────────────────────────────────────────────────────────────────────
# Jetpack Compose / AndroidX — handled by the library's own consumer rules,
# but suppress any residual warnings from the optimizer.
# ─────────────────────────────────────────────────────────────────────────────
-dontwarn androidx.**
