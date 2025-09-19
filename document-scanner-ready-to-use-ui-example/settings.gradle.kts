
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

        // Scanbot SDK maven repos:
        maven(url = "https://nexus.scanbot.io/nexus/content/repositories/releases/")
        maven(url = "https://nexus.scanbot.io/nexus/content/repositories/snapshots/")
        val stagingNexusLogin = System.getenv()["STAGING_NEXUS_LOGIN"] ?: providers.gradleProperty("STAGING_NEXUS_LOGIN").orNull
        val stagingNexusPassword = System.getenv()["STAGING_NEXUS_PASSWORD"] ?: providers.gradleProperty("STAGING_NEXUS_PASSWORD").orNull
        maven(url = "https://nexus2-staging.scanbot.io/nexus/content/repositories/snapshots/") {
            credentials {
                username = stagingNexusLogin
                password = stagingNexusPassword
            }
        }
    }
}

include(":app")
rootProject.name = "Scanbot Document Scanner SDK RTU UI example"
