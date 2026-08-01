import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.cleardu.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.cleardu.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    // === Signing configurations ===
    // Debug and Release use separate, dedicated keystores so the two APK variants
    // never share a signature. Keystore files and passwords are NOT committed;
    // they are injected via environment variables / GitHub Secrets at build time.
    signingConfigs {
        create("release") {
            // Allow local builds without a keystore file present (CI provides it).
            val keystorePath = System.getenv("CLEARDU_RELEASE_KEYSTORE_PATH")
            val keystoreBase64 = System.getenv("CLEARDU_RELEASE_KEYSTORE_BASE64")
            val resolvedPath = when {
                !keystorePath.isNullOrEmpty() -> keystorePath
                !keystoreBase64.isNullOrEmpty() -> {
                    val tmpFile = File(rootDir, "build/tmp/release.keystore")
                    tmpFile.parentFile.mkdirs()
                    tmpFile.writeBytes(java.util.Base64.getDecoder().decode(keystoreBase64))
                    tmpFile.absolutePath
                }
                else -> ""
            }
            if (resolvedPath.isNotEmpty() && File(resolvedPath).exists()) {
                storeFile = file(resolvedPath)
                storePassword = System.getenv("CLEARDU_RELEASE_STORE_PASSWORD") ?: ""
                keyAlias = System.getenv("CLEARDU_RELEASE_KEY_ALIAS") ?: ""
                keyPassword = System.getenv("CLEARDU_RELEASE_KEY_PASSWORD") ?: ""
            }
        }
        getByName("debug") {
            // Force a project-local debug keystore so debug builds always use a
            // dedicated signature that differs from release.
            val debugKeystore = File(rootDir, "build/tmp/debug.keystore")
            if (!debugKeystore.exists()) {
                debugKeystore.parentFile.mkdirs()
            }
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
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
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

    testOptions {
        unitTests {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.accompanist.systemuicontroller)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
