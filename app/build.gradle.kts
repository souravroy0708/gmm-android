plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// Pilot builds on GitHub Actions are signed with YOUR key (from repository secrets), so every new
// APK installs as an update over the previous one. Without the secrets it falls back to the debug key.
val releaseKeystore: String? = System.getenv("GMM_KEYSTORE_PATH")
val buildNumber: Int = System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull() ?: 1

// Live Django server. Override without editing code:  ./gradlew assembleRelease -PGMM_SERVER=https://your.domain
val liveServer: String = (project.findProperty("GMM_SERVER") as String?) ?: "https://shop.gmmdistributors.xyz"
// Debug builds default to the laptop running `python manage.py runserver 0.0.0.0:8000`
// (10.0.2.2 = the host machine from the Android emulator). Change in Profile -> Developer: server.
val debugServer: String = (project.findProperty("GMM_DEBUG_SERVER") as String?) ?: "http://10.0.2.2:8000"

android {
    namespace = "com.gmmdistributors.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.gmmdistributors.app"
        minSdk = 26
        targetSdk = 36
        versionCode = buildNumber
        versionName = "1.0.$buildNumber"
        // Ship only the languages the app speaks: drops ~80 unused translations from AndroidX libraries.
        androidResources { localeFilters += listOf("en", "bn") }
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
            buildConfigField("String", "DEFAULT_SERVER", "\"$debugServer\"")
        }
        release {
            manifestPlaceholders["cleartext"] = "false"  // HTTPS only
            buildConfigField("String", "DEFAULT_SERVER", "\"$liveServer\"")
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
    packaging {
        resources {
            excludes += listOf("/META-INF/{AL2.0,LGPL2.1}", "/META-INF/*.version", "DebugProbesKt.bin", "/kotlin/**", "/META-INF/*.kotlin_module")
        }
    }
    bundle {
        // Play delivers only the screen density and CPU each phone needs; keep all languages in the base
        // so switching বাংলা / English inside the app always works.
        language { enableSplit = false }
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
    debugImplementation(libs.compose.ui.tooling.preview)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.coroutines.android)
    implementation(libs.work.runtime.ktx)
    implementation(libs.play.app.update.ktx)  // Google Play in-app updates (mandatory update)
}
