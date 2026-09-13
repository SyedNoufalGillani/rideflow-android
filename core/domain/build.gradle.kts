// :core:domain is a pure Kotlin/JVM module with ZERO Android dependencies.
// It holds domain models, repository contracts and use cases — the innermost
// layer of Clean Architecture that every other layer depends on, but which
// depends on nothing itself.
plugins {
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    // JSR-330 @Inject/@Qualifier annotations only — NOT the Hilt/Dagger runtime.
    // Lets use cases declare `@Inject constructor(...)` so Hilt (wired in
    // outer, Android-aware modules) can construct them, without pulling any
    // dependency-injection framework or Android dependency into this module.
    implementation("javax.inject:javax.inject:1")

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}
