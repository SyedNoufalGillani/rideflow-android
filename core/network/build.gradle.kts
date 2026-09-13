// :core:network hosts the Retrofit/OkHttp setup, DTOs and auth interceptor
// that a real backend integration would use. No screen in this project talks
// to it by default — see BuildConfig.USE_FAKE_DATA_SOURCE in :app — but it is
// fully wired so swapping a fake repository for a real, Retrofit-backed one
// is a drop-in change, not a rewrite.
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.syednoufal.rideflow.core.network"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":core:common"))

    implementation(libs.bundles.network)
    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
