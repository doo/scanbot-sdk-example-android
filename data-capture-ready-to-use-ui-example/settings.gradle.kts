
pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
        maven(url = "https://nexus.scanbot.io/nexus/content/repositories/releases/")
        maven(url = "https://nexus.scanbot.io/nexus/content/repositories/snapshots/")
    }
}

include(":app")
rootProject.name = "Scanbot Data Capture SDK RTU UI example"
