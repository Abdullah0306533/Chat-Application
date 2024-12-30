plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    id ("com.google.dagger.hilt.android")

    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.chattingapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.chattingapp"
        minSdk = 24
        targetSdk = 34
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
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    //Hilt dependencies
    implementation (libs.hilt.android)
    ksp (libs.hilt.compiler)

    // For instrumentation tests
    androidTestImplementation  (libs.hilt.android.testing)
    kspAndroidTest (libs.dagger.hilt.compiler)

    // For local unit tests
    testImplementation ("com.google.dagger:hilt-android-testing:2.53.1")
    kspTest (libs.dagger.hilt.compiler)

    //for ViewModel
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    //Navigation
    val nav_version = "2.8.5"
    implementation(libs.androidx.navigation.compose)

    //animations
    implementation (libs.lottie.compose)

    //coil to upload image
    implementation("io.coil-kt.coil3:coil-compose:3.0.4")
    // Coil's OkHttp integration (optional, for network operations)
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.4")

}