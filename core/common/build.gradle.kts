// :core:common holds cross-cutting, framework-light infrastructure shared by
// every feature module: the NetworkResult wrapper, a coroutine
// DispatcherProvider, and the base MVI contracts (UiState/UiIntent/UiEffect)
// plus the generic MviViewModel used by every feature ViewModel.
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.syednoufal.rideflow.core.common"
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
    // :core:common is allowed to depend on :core:domain (but never the
    // reverse): it hosts small, deterministic fixtures — like the shared
    // demo driver catalog — that operate purely on domain models so every
    // fake repository across feature modules can produce consistent-looking
    // sample data without duplicating it.
    api(project(":core:domain"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.bundles.lifecycle)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}
