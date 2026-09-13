@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "RideFlow"

include(":app")

include(":core:designsystem")
include(":core:common")
include(":core:network")
include(":core:database")
include(":core:domain")

include(":feature:auth")
include(":feature:booking")
include(":feature:tracking")
include(":feature:payments")
