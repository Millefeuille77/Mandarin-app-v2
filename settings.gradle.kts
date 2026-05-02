// Settings file — Mandarin Learn
// Declares version catalog location and project modules.

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
    // Note: gradle/libs.versions.toml is auto-loaded as the "libs" catalog by Gradle.
    // Do NOT declare it explicitly — that triggers "you can only call the 'from' method
    // a single time" (Gradle docs: too_many_import_invocation).
}

rootProject.name = "MandarinLearn"
include(":app")
