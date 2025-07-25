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
    implementation(libs.room.common.jvm)

    implementation ("com.google.android.flexbox:flexbox:3.0.0")
    implementation ("com.google.android.material:material:1.11.0")
    implementation ("com.squareup.retrofit2:converter-scalars:2.9.0")
    implementation ("io.noties.markwon:core:4.6.2")
    implementation ("com.google.android.gms:play-services-auth:21.0.0")
    implementation(libs.lifecycle.viewmodel.android)
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")

    implementation(fileTree("D:/zalopay_sdk") {
        include("*.aar", "*.jar")
    })

    implementation(fileTree("D:/Apps") {
        include("*.aar", "*.jar")
    })
    implementation(fileTree(mapOf(
        "dir" to "D:\\UIT_Nam2_Ky2",
        "include" to listOf("*.aar", "*.jar"),
        "exclude" to listOf("")
    )))

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Retrofit for API calls
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.gson)

    implementation (libs.wheelpicker)
    implementation(libs.mpandroidchart)

    implementation ("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")

    implementation ("org.java-websocket:Java-WebSocket:1.5.2")
    implementation ("com.github.NaikSoftware:StompProtocolAndroid:1.6.6")
    implementation ("io.reactivex.rxjava2:rxjava:2.2.21")
    implementation ("io.reactivex.rxjava2:rxandroid:2.1.1")
    implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
}
