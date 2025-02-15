plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
}

dependencies {
    // Core libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose BOM (Bill of Materials)
    implementation(platform(libs.androidx.compose.bom))

    // Compose libraries
    implementation("androidx.compose.material:material:1.5.1") // Material 2 Components
    implementation("androidx.compose.material:material-icons-extended:1.7.6") // Material Icons
    implementation("androidx.compose.ui:ui:1.5.1") // Core Compose UI
    implementation("androidx.compose.foundation:foundation:1.5.1") // Compose Foundation
    implementation("androidx.compose.runtime:runtime:1.5.1") // Runtime
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.1")
    implementation(libs.androidx.appcompat)
    implementation(libs.firebase.firestore.ktx) // Preview support
    debugImplementation("androidx.compose.ui:ui-tooling:1.5.1") // Tooling for debug
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.5.1") // Test manifest

    // Material 3
    implementation("androidx.compose.material3:material3:1.1.1")


    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.5") // Navigation Compose

    // CameraX
    implementation("androidx.camera:camera-core:1.4.1")
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.8.0")) // Firebase BOM
    implementation(libs.google.firebase.auth) // Firebase Authentication
    implementation("com.google.firebase:firebase-database") // Firebase Realtime Database
    implementation("com.google.firebase:firebase-analytics") // Firebase Analytics
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.android.gms:play-services-auth:20.5.0") // Google Sign-In


    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.1")

    // YkCharts
    implementation("co.yml:ycharts:2.1.0")

    // For Animation
    implementation("com.airbnb.android:lottie-compose:6.0.0")

    // Retrofit dependencies for making network requests
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // OkHttp for network logging
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.1")
    implementation("com.android.volley:volley:1.2.1")

    // Cropping Image
    implementation("com.vanniktech:android-image-cropper:4.5.0")
    implementation("androidx.appcompat:appcompat:1.6.1")

    // Preferences
    implementation("androidx.datastore:datastore-preferences:1.0.0")
}
