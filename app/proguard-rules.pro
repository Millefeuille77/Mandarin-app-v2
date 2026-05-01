# ProGuard rules — Mandarin Learn
# Add project-specific ProGuard rules here.
# By default, the flags in the Shrinker are set to be off in debug builds.

# Kotlin Serialization (needed when kotlinx-serialization is added in Phase 2)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Keep data classes used in Room entities (added in Phase 2)
-keep class com.mandarinlearn.data.local.entity.** { *; }

# Keep BuildConfig
-keep class com.mandarinlearn.BuildConfig { *; }
