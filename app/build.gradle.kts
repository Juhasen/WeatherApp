import org.gradle.kotlin.dsl.implementation

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "pl.juhas.weatherapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "pl.juhas.weatherapp"
        minSdk = 33
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    val retroFitVersion = "2.11.0"
    implementation("com.squareup.retrofit2:retrofit:$retroFitVersion")
    implementation("com.squareup.retrofit2:converter-gson:$retroFitVersion")

    implementation("androidx.compose.runtime:runtime-livedata:1.8.1")
    implementation("androidx.compose.material:material:1.8.1")
    implementation ("io.coil-kt:coil-compose:2.2.2")

    implementation ("androidx.compose.foundation:foundation:1.5.1")
    implementation ("androidx.compose.ui:ui:1.5.1")

    implementation("androidx.navigation:navigation-compose:2.8.4")

    implementation("androidx.datastore:datastore-preferences:1.1.6")
}