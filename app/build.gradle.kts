plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("com.google.dagger.hilt.android")
    id("kotlinx-serialization")
    kotlin("kapt")
}

android {
    namespace = "nstv.bluetoothmagic"
    compileSdk = 34

    defaultConfig {
        applicationId = "nstv.bluetoothmagic"
        minSdk = 31
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidxComposeCompiler.get().toString()
    }
    packaging {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.bundles.compose.jetpack)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose.design)
    implementation(libs.bundles.retrofit)
    implementation(libs.bundles.coil)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.accompanist.permissions)
    implementation(libs.androidx.bluetooth)
    implementation(libs.bundles.room)
    kapt(libs.room.compiler)

    testImplementation(libs.junit4)
    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test)
    androidTestImplementation(libs.androidx.bluetooth.testing)
    debugImplementation(libs.bundles.compose.debug)
}