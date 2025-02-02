// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    // Define the Android Application plugin from the version catalog.
    // 'apply false' means this plugin is not automatically applied to this top-level project,
    // but is available for subprojects/modules to use.
    alias(libs.plugins.android.application) apply false

    // Define the Kotlin Android plugin from the version catalog.
    alias(libs.plugins.kotlin.android) apply false

    // Define the Kotlin Symbol Processing (KSP) plugin with an explicit version.
    id("com.google.devtools.ksp") version "2.0.21-1.0.27" apply false
}