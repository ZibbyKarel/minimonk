pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

rootProject.name = "minimonk"

include(
    "libs:common-events",
    "libs:common-observability",
    "libs:common-security",
    "services:api-gateway",
    "services:user-service",
    "services:warehouse-service",
    "services:order-service",
    "services:payment-service",
)
