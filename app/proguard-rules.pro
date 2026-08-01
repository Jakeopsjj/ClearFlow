# Keep Compose runtime metadata
-dontobfuscate

# Kotlin Coroutines
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Keep Compose related classes
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep app package model classes (Parcelize / reflection safe)
-keep class com.cleardu.app.data.** { *; }
