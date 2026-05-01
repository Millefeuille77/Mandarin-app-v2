// Root project build file — Mandarin Learn
// Implements ARCHITECTURE.md §1 (build system: Gradle 8.4, Kotlin DSL only).
// Only plugin declarations live here; all dependency coordinates are in gradle/libs.versions.toml.

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
    // Phase 2: kotlinx-serialization plugin (applied in app/build.gradle.kts)
    alias(libs.plugins.kotlin.serialization) apply false
}
