
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
        ":pdf-creation",
        ":camera-view",
        ":edit-polygon-view",
        ":camera-fragment",
        ":document-scanner",
        ":barcode-scanner",
        ":mrz-scanner",
        ":mc-scanner",
        ":generic-document-recognizer-autosnapping",
        ":generic-document-recognizer-livedetection",
        ":generic-text-recognizer",
        ":vin-scanner",
        ":licenseplate-scanner",
        ":tiff-writer",
        ":check-recognizer",
        ":manual-processing",
        ":camera-view-aspect-ratio-finder",
        ":ehic-scanner",
        ":adjustable-filters",
        ":encryption",
        ":document-quality-analyzer",
)
rootProject.name = "Scanbot SDK Classic Components examples"
