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
    compileSdk = 35

    defaultConfig {
        applicationId = "com.cleardu.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 8
        versionName = "1.4.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    // === Signing configurations ===
    // Debug and Release use separate, dedicated keystores so the two APK variants
    // never share a signature. Keystores are generated on demand by the
    // `ensureDebugKeystore` / `ensureReleaseKeystore` tasks below; a real
    // production release keystore can be injected via environment variables
    // (CLEARDU_RELEASE_KEYSTORE_*) without changing the build script.
    signingConfigs {
        create("release") {
            // Production path: inject a real release keystore via env vars.
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
                else -> File(rootDir, "build/tmp/release.keystore").absolutePath
            }
            storeFile = file(resolvedPath)
            // PKCS12 keystores (Java 17+ default) require store & key passwords to be
            // identical, so we use a single secret for both.
            storePassword = System.getenv("CLEARDU_RELEASE_STORE_PASSWORD") ?: "cleardu-release"
            keyAlias = System.getenv("CLEARDU_RELEASE_KEY_ALIAS") ?: "cleardu-release"
            keyPassword = System.getenv("CLEARDU_RELEASE_KEY_PASSWORD") ?: "cleardu-release"
        }
        getByName("debug") {
            // Force a project-local debug keystore so debug builds always use a
            // dedicated signature that differs from release. The keystore is
            // generated on demand by the `ensureDebugKeystore` task below.
            val debugKeystore = File(rootDir, "build/tmp/debug.keystore")
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

// === Auto-generate the dedicated debug keystore on first build ===
// Uses keytool (bundled with the JDK) so the build is self-contained: a fresh
// checkout in Android Studio will produce build/tmp/debug.keystore automatically
// before validateSigningDebug runs. No manual keytool invocation required.
val ensureDebugKeystore by tasks.registering {
    val debugKeystore = File(rootDir, "build/tmp/debug.keystore")
    outputs.file(debugKeystore)
    doLast {
        if (!debugKeystore.exists()) {
            debugKeystore.parentFile.mkdirs()
            generateKeystore(
                target = debugKeystore,
                alias = "cleardu-debug",
                storePass = "android",
                keyPass = "android",
                dname = "CN=ClearDu Debug, OU=Mobile, O=ClearDu, L=Beijing, ST=Beijing, C=CN"
            )
            logger.lifecycle("Generated dedicated debug keystore at ${debugKeystore.absolutePath}")
        }
    }
}

// === Auto-generate the dedicated release keystore on first build ===
// Only used when no real release keystore is injected via CLEARDU_RELEASE_* env
// vars. Produces a separate self-signed keystore so debug and release APKs
// always carry different signatures. Replace with a real code-signing cert for
// Play Store distribution.
val ensureReleaseKeystore by tasks.registering {
    val releaseKeystore = File(rootDir, "build/tmp/release.keystore")
    outputs.file(releaseKeystore)
    doLast {
        if (!releaseKeystore.exists() &&
            System.getenv("CLEARDU_RELEASE_KEYSTORE_PATH").isNullOrEmpty() &&
            System.getenv("CLEARDU_RELEASE_KEYSTORE_BASE64").isNullOrEmpty()
        ) {
            releaseKeystore.parentFile.mkdirs()
            generateKeystore(
                target = releaseKeystore,
                alias = "cleardu-release",
                storePass = "cleardu-release",
                keyPass = "cleardu-release",
                dname = "CN=ClearDu Release, OU=Mobile, O=ClearDu, L=Beijing, ST=Beijing, C=CN"
            )
            logger.lifecycle("Generated dedicated release keystore at ${releaseKeystore.absolutePath}")
        }
    }
}

fun generateKeystore(
    target: File,
    alias: String,
    storePass: String,
    keyPass: String,
    dname: String
) {
    val toolHome = System.getProperty("java.home")
    val keytool = File(toolHome, "bin/keytool").let { if (it.exists()) it else File(toolHome, "keytool") }
    val keytoolBin = if (keytool.exists()) keytool.absolutePath else "keytool"
    exec {
        commandLine(
            keytoolBin, "-genkeypair", "-v",
            "-keystore", target.absolutePath,
            "-alias", alias,
            "-storepass", storePass,
            "-keypass", keyPass,
            "-keyalg", "RSA", "-keysize", "2048", "-validity", "10000",
            "-dname", dname
        )
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