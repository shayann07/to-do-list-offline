plugins {
    // Use version catalogs for plugin management (see libs.versions.toml)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
    id("com.google.devtools.ksp")
}

android {
    // Define the app's package namespace and compile SDK version.
    namespace = "com.shayan.remindersios"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.shayan.remindersios"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // Specify the instrumentation test runner.
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // Disable code minification for now. Adjust for production with proper ProGuard/R8 rules.
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        // Set Java compatibility to version 11.
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        // Use JVM target 11 for Kotlin.
        jvmTarget = "11"
    }

    buildFeatures {
        // Enable ViewBinding for easier view references.
        viewBinding = true
    }
}

dependencies {

    // -------------------------------
    // Core AndroidX Libraries
    // -------------------------------
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // -------------------------------
    // Navigation Components
    // -------------------------------
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // -------------------------------
    // UI Components
    // -------------------------------
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.recyclerview)
    implementation(libs.androidx.fragment.ktx)

    // -------------------------------
    // Room & Database
    // -------------------------------
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // -------------------------------
    // Lifecycle & Coroutines
    // -------------------------------
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // -------------------------------
    // Third-Party Libraries
    // -------------------------------
    implementation(libs.ultra.ptr)

    // -------------------------------
    // Testing Libraries
    // -------------------------------
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // -------------------------------
    // Project Modules
    // -------------------------------
    implementation(project(":library"))
}