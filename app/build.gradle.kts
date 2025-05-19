plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.focusflow_frontend"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.focusflow_frontend"
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("com.kizitonwose.calendar:view:2.3.0")
    implementation("com.google.firebase:firebase-messaging:23.0.0")
    implementation(libs.room.common.jvm)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Retrofit for API calls
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.gson)
    implementation (libs.wheelpicker)

    implementation(libs.mpandroidchart)
}