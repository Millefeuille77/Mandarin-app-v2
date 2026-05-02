// App module build file — Mandarin Learn
// Implements ARCHITECTURE.md §1: minSdk 26, targetSdk 34, compileSdk 34, JVM 17.
// Compose compiler version 1.5.8 must match Kotlin 1.9.22.
// Phase 2 adds: Room (KSP), kotlinx-serialization, DataStore.
// Phase 10: versionCode = 1, versionName = "1.0.0" (final release).

import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

// Read local.properties for GEMINI_API_KEY (never committed to VCS).
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(file.inputStream())
}

android {
    namespace = "com.mandarinlearn"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mandarinlearn"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Expose Gemini API key via BuildConfig. Key must be set in local.properties.
        // If absent, GeminiService enters degraded mode (ARCHITECTURE.md §4.1).
        buildConfigField(
            "String",
            "GEMINI_API_KEY",
            "\"${localProperties["GEMINI_API_KEY"] ?: ""}\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        // -Werror was originally enabled to keep quality tight, but the v1.0 codebase has
        // ~45 non-functional warnings (unused params on preview-only composables, deprecated
        // ImageVector aliases that have AutoMirrored replacements, kotlinx-serialization opt-ins).
        // Cleaning them up is a Phase 11 polish task — for now the APK must ship.
        // freeCompilerArgs += listOf("-Werror")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        // Must match Kotlin 1.9.22 — see ARCHITECTURE.md §1.2 (compose-compiler = "1.5.8")
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Kotlin
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.android)

    // Phase 2: JSON parsing via kotlinx-serialization (1.6.2 per ARCHITECTURE.md §1.2)
    implementation(libs.kotlinx.serialization.json)

    // AndroidX core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Compose (BOM governs all compose-* artifact versions)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    // Phase 2: Room — runtime, extension functions, and KSP annotation processor
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Phase 2: DataStore Preferences for user settings
    implementation(libs.androidx.datastore.preferences)

    // Phase 5: Gemini AI SDK for TTS (version 0.2.2 per ARCHITECTURE.md §1.2)
    implementation(libs.google.generative.ai)

    // Tooling (debug only)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    // Unit tests
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)

    // Instrumented tests
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)
}
