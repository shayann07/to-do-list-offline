// =============================================================
// Plugin Management
// =============================================================
pluginManagement {
    repositories {
        // Use Google's Maven repository for Android-related plugins.
        google {
            content {
                // Limit the scope to specific groups for faster resolution.
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        // Central repository for Maven artifacts.
        mavenCentral()
        // Gradle Plugin Portal for additional plugins.
        gradlePluginPortal()
        // Explicit Maven repository for Gradle plugins.
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }
}

// =============================================================
// Dependency Resolution Management
// =============================================================
dependencyResolutionManagement {
    // Fail if any project attempts to declare its own repositories.
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// =============================================================
// Root Project Configuration
// =============================================================
rootProject.name = "Reminders (IOS)"

// =============================================================
// Module Inclusions
// =============================================================
// Include the main app module.
include(":app")

// Include the library module, with a custom project directory.
include(":library")
project(":library").projectDir = file("app/library")