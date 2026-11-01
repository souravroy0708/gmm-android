plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// Pilot builds on GitHub Actions are signed with YOUR key (from repository secrets), so every new
// APK installs as an update over the previous one. Without the secrets it falls back to the debug key.
val releaseKeystore: String? = System.getenv("GMM_KEYSTORE_PATH")
val buildNumber: Int = System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull() ?: 1

android {
    namespace = "com.gmmdistributors.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.gmmdistributors.app"
        minSdk = 26
        targetSdk = 36
        versionCode = buildNumber
        versionName = "1.0.$buildNumber"
        // Live Django server URL (used when Dummy API Mode is switched off in Profile -> Backend Settings)
        buildConfigField("String", "DEFAULT_SERVER", "\"https://gmmdistributors.xyz\"")
        buildConfigField("String", "OWNER_WHATSAPP", "\"919083732101\"")
        buildConfigField("String", "SUPPORT_PHONE", "\"9083732101\"")
        buildConfigField("String", "SUPPORT_EMAIL", "\"ray.chikur@gmail.com\"")
    }

    signingConfigs {
        create("release") {
            if (releaseKeystore != null) {
                storeFile = file(releaseKeystore)
                storePassword = System.getenv("GMM_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("GMM_KEY_ALIAS")
                keyPassword = System.getenv("GMM_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            manifestPlaceholders["cleartext"] = "true"   // allows http:// to local Django runserver on Wi-Fi
        }
        release {
            manifestPlaceholders["cleartext"] = "false"  // HTTPS only
            signingConfig = if (releaseKeystore != null) signingConfigs.getByName("release") else signingConfigs.getByName("debug")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    lint {
        htmlReport = true
        xmlReport = false
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.splashscreen)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.activity.compose)
    implementation(libs.fragment.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.coroutines.android)
}
