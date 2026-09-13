// :core:designsystem is RideFlow's Material3-based UI toolkit: color
// schemes, typography, spacing tokens and reusable Composables. It has no
// dependency on any other RideFlow module so it can be previewed and evolved
// in isolation, the way a design-system module is treated in production.
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.syednoufal.rideflow.core.designsystem"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    buildFeatures {
        compose = true
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
    // Only for the DemoGeoProjection coordinate-transform helper used by the
    // Canvas map — designsystem otherwise has no dependency on any other
    // RideFlow module.
    implementation(project(":core:domain"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose.ui)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.ui.tooling.preview)
}
