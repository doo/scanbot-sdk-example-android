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

        val stagingNexusUrl = providers.gradleProperty("STAGING_NEXUS_URL").orNull
        val stagingNexusLogin = providers.gradleProperty("STAGING_NEXUS_LOGIN").orNull
        val stagingNexusPwd = providers.gradleProperty("STAGING_NEXUS_PASSWORD").orNull
        if (stagingNexusUrl != null && stagingNexusLogin != null && stagingNexusPwd != null) {
            maven(url = stagingNexusUrl) {
                credentials {
                    username = stagingNexusLogin
                    password = stagingNexusPwd
                }
            }
        } else {
            logger.info(
                """STAGING_NEXUS_URL, STAGING_NEXUS_LOGIN, STAGING_NEXUS_PASSWORD (or some of them) 
                    are not set. Staging repository will not be used."""
            )
        }
    }
}

include(
    ":common",
    ":ocr",
    ":edit-polygon-view",
    ":pdf-generation",
    ":camera-view",
    ":edit-polygon-view",
    ":camera-fragment",
    ":document-scanner",
    ":barcode-scanner",
    ":mrz-scanner",
    ":mc-scanner",
    ":document-data-extractor-autosnapping",
    ":document-data-extractor-livedetection",
    ":text-pattern-scanner",
    ":vin-scanner",
    ":licenseplate-scanner",
    ":tiff-generation",
    ":check-recognizer",
    ":creditcard-scanner",
    ":manual-processing",
    ":camera-view-aspect-ratio-finder",
    ":ehic-scanner",
    ":adjustable-filters",
    ":encryption",
    ":document-quality-analyzer",
)
rootProject.name = "Scanbot SDK Classic Components examples"
