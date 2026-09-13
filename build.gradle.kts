// Root build file. Individual module build files apply the plugins they need
// via `alias(libs.plugins.*) apply false` here, keeping plugin versions
// centralized in the version catalog (gradle/libs.versions.toml).
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint) apply false
}

subprojects {
    apply(plugin = rootProject.libs.plugins.ktlint.get().pluginId)

    plugins.withId(rootProject.libs.plugins.ktlint.get().pluginId) {
        extensions.configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
            version.set("1.3.1")
            android.set(true)
            ignoreFailures.set(false)
            filter {
                exclude { entry -> entry.file.path.contains("build/") }
            }
        }
    }
}

detekt {
    toolVersion = "1.23.7"
    config.setFrom(files("$rootDir/detekt.yml"))
    buildUponDefaultConfig = true
    allRules = false
    parallel = true
}

dependencies {
    detektPlugins(libs.detekt.formatting)
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
