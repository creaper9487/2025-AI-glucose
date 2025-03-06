plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "rl.collab.diabeat"
    compileSdk = 35

    defaultConfig {
        applicationId = "rl.collab.diabeat"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.swiperefreshlayout)

    // Bio Login & Master Key & Encrypted Shared Pref
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.security.crypto)

    // Credential Manager
    implementation(libs.googleid)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)

    // Chart & Rich Text
    implementation(libs.mpandroidchart)
    implementation (libs.core)

    // Http Request
    implementation(libs.okhttp)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
}