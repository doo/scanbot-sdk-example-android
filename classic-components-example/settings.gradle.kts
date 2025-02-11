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
    ":check-scanner",
    ":creditcard-scanner",
    ":manual-processing",
    ":camera-view-aspect-ratio-finder",
    ":adjustable-filters",
    ":encryption",
    ":document-quality-analyzer",
)
rootProject.name = "Scanbot SDK Classic Components examples"
