import java.util.Base64
import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.cleardu.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.cleardu.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 44
        versionName = "1.13.6"

        // === Native 库过滤：仅保留 armeabi-v7a 和 arm64-v8a，去除 x86/x86_64 减小包体积 ===
        // 同时解决三家地图 SDK 的 so 库冲突
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a")
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // === API keys from local.properties (gitignored) ===
        // Keys are never hardcoded in source or committed to Git.
        val localProps = Properties().apply {
            val f = File(rootDir, "local.properties")
            if (f.exists()) load(FileInputStream(f))
        }
        buildConfigField("String", "WEATHER_API_KEY", "\"${localProps.getProperty("WEATHER_API_KEY", "")}\"")
        buildConfigField("String", "PEXELS_API_KEY", "\"${localProps.getProperty("PEXELS_API_KEY", "")}\"")
        buildConfigField("String", "PEXELS_PROXY_KOYEB_URL", "\"${localProps.getProperty("PEXELS_PROXY_KOYEB_URL", "https://cleardu-pexels-proxy.cleardu.workers.dev")}\"")
        buildConfigField("String", "PEXELS_PROXY_RENDER_URL", "\"${localProps.getProperty("PEXELS_PROXY_RENDER_URL", "https://cleardu-pexels-proxy.cleardu.workers.dev")}\"")
        buildConfigField("String", "GITHUB_PROXY_URL", "\"${localProps.getProperty("GITHUB_PROXY_URL", "https://cleardu-pexels-proxy.cleardu.workers.dev")}\"")
    }

    // === Signing configurations ===
    // 签名永久固化：Debug 和 Release 分别使用 app/keystore/ 下的固定 keystore。
    // 自 v1.7.14 起，所有后续版本的签名指纹锁定不变，SHA1/SHA256 禁止更改。
    // 环境变量 CLEARDU_RELEASE_KEYSTORE_PATH / CLEARDU_RELEASE_KEYSTORE_BASE64
    // 仅在正式发布时注入真实生产签名，覆盖默认的固化 keystore。
    //
    // 固化签名指纹（自 v1.7.14 锁定）：
    //   Debug  SHA1: CE:55:41:52:05:48:5C:0A:58:DF:AC:1C:36:22:38:BB:15:2F:26:42
    //   Debug  SHA256: 9A:70:79:8D:E3:BF:71:EF:0E:E8:C7:18:C6:38:B1:ED:33:50:D2:A1:44:CC:0E:7D:F7:B4:5A:BD:C0:E1:04:3B
    //   Release SHA1: 0F:06:9A:DC:BC:09:66:E9:9A:DC:00:31:EB:15:84:CB:4F:81:D9:66
    //   Release SHA256: 5C:0D:51:26:7E:E9:A8:F2:EA:9F:F0:58:B4:F7:E6:68:65:1C:19:8F:56:EC:95:8C:38:AD:51:7B:6C:A4:05:B2
    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("CLEARDU_RELEASE_KEYSTORE_PATH")
            val keystoreBase64 = System.getenv("CLEARDU_RELEASE_KEYSTORE_BASE64")
            val resolvedPath = when {
                !keystorePath.isNullOrEmpty() -> keystorePath
                !keystoreBase64.isNullOrEmpty() -> {
                    val tmpFile = File(rootDir, "build/tmp/release.keystore")
                    tmpFile.parentFile.mkdirs()
                    tmpFile.writeBytes(Base64.getDecoder().decode(keystoreBase64))
                    tmpFile.absolutePath
                }
                else -> File(projectDir, "keystore/release.keystore").absolutePath
            }
            storeFile = file(resolvedPath)
            storePassword = System.getenv("CLEARDU_RELEASE_STORE_PASSWORD") ?: "cleardu-release"
            keyAlias = System.getenv("CLEARDU_RELEASE_KEY_ALIAS") ?: "cleardu-release"
            keyPassword = System.getenv("CLEARDU_RELEASE_KEY_PASSWORD") ?: "cleardu-release"
        }
        getByName("debug") {
            val debugKeystore = File(projectDir, "keystore/debug.keystore")
            storeFile = debugKeystore
            storePassword = "android"
            keyAlias = "cleardu-debug"
            keyPassword = "android"
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            isDebuggable = false
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = signingConfigs.getByName("release")
        }
    }

    // APK 输出文件名：cleardu-v{versionName}-{debug/release}.apk
    applicationVariants.all {
        val variant = this
        variant.outputs.all {
            val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            val type = variant.buildType.name
            val baseVersion = defaultConfig.versionName
            output.outputFileName = "cleardu-v$baseVersion-$type.apk"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }

    testOptions {
        unitTests {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
        }
    }
}

// === 签名固化检查：确保永久 keystore 存在，不存在则报错 ===
// 自 v1.7.14 起，签名永久锁定。不再自动生成 keystore。
// 如果 app/keystore/ 下的 keystore 丢失，请从备份恢复，或联系管理员。
val ensureDebugKeystore by tasks.registering {
    val debugKeystore = File(projectDir, "keystore/debug.keystore")
    doLast {
        if (!debugKeystore.exists()) {
            throw GradleException(
                "Debug keystore 缺失: ${debugKeystore.absolutePath}\n" +
                "签名自 v1.7.14 起已永久固化，禁止自动生成新 keystore。\n" +
                "请从备份恢复 app/keystore/debug.keystore 文件。"
            )
        }
    }
}

val ensureReleaseKeystore by tasks.registering {
    val releaseKeystore = File(projectDir, "keystore/release.keystore")
    doLast {
        if (!releaseKeystore.exists() &&
            System.getenv("CLEARDU_RELEASE_KEYSTORE_PATH").isNullOrEmpty() &&
            System.getenv("CLEARDU_RELEASE_KEYSTORE_BASE64").isNullOrEmpty()
        ) {
            throw GradleException(
                "Release keystore 缺失: ${releaseKeystore.absolutePath}\n" +
                "签名自 v1.7.14 起已永久固化，禁止自动生成新 keystore。\n" +
                "请从备份恢复 app/keystore/release.keystore 文件，\n" +
                "或设置 CLEARDU_RELEASE_KEYSTORE_PATH / CLEARDU_RELEASE_KEYSTORE_BASE64 环境变量。"
            )
        }
    }
}

afterEvaluate {
    // Make sure each keystore exists before AGP validates its signing config.
    tasks.matching { it.name == "validateSigningDebug" }.configureEach {
        dependsOn(ensureDebugKeystore)
    }
    tasks.matching { it.name == "validateSigningRelease" }.configureEach {
        dependsOn(ensureReleaseKeystore)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.datastore.preferences)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.androidx.navigation.compose)

    // ============================================================
    // 三家地图 SDK 依赖（对外分发版本需移除腾讯 SDK 规避授权限制）
    // ============================================================

    // --- 高德地图 3D 地图 + 定位 + 搜索 ---
    // 注意：3dmap 10.0.600 已包含完整定位 SDK，无需单独引用 location
    implementation("com.amap.api:3dmap:10.0.600")
    implementation("com.amap.api:search:9.7.0")

    // --- 百度地图 基础地图 + 定位 + 搜索 ---
    implementation("com.baidu.lbsyun:BaiduMapSDK_Map:8.2.0")
    implementation("com.baidu.lbsyun:BaiduMapSDK_Location:9.6.9")
    implementation("com.baidu.lbsyun:BaiduMapSDK_Search:8.2.0")

    // --- 腾讯地图 矢量地图 SDK ---
    // 对外分发版本需移除，规避腾讯地图授权限制
    // 腾讯地图 SDK 已发布至 Maven Central，可直接引用
    implementation("com.tencent.map:tencent-map-vector-sdk:5.4.1")
    // 腾讯地图组件库（POI 搜索、地理编码等）
    implementation("com.tencent.map:sdk-utilities:1.0.9")
    // 腾讯定位 SDK
    implementation("com.tencent.map.geolocation:TencentLocationSdk-openplatform:7.5.3.2")

    // OSMDroid — 保留作为无 SDK 环境的备用方案（仅瓦片显示，不参与三家降级链）
    implementation("org.osmdroid:osmdroid-android:6.1.18")

    // ============================================================
    // 天气背景功能依赖
    // ============================================================
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)
    implementation(libs.coil.compose)

    debugImplementation(libs.androidx.compose.ui.tooling)
}