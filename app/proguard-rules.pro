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

# ============================================================
# 三家地图 SDK R8 混淆规则
# ============================================================

# --- 高德地图 ---
-keep class com.amap.api.** { *; }
-keep class com.autonavi.** { *; }
-keep class com.loc.** { *; }
-dontwarn com.amap.api.**
-dontwarn com.autonavi.**
-dontwarn com.loc.**

# --- 百度地图 ---
-keep class com.baidu.** { *; }
-keep class vi.com.gdi.** { *; }
-keep class mapsdkvi.com.gdi.** { *; }
-dontwarn com.baidu.**
-dontwarn vi.com.gdi.**
-dontwarn mapsdkvi.com.gdi.**

# --- 腾讯地图 ---
-keep class com.tencent.** { *; }
-dontwarn com.tencent.**

# --- 通用：保留所有地图 SDK 的 native 方法 ---
-keepclasseswithmembernames class * {
    native <methods>;
}
