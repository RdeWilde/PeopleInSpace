pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        jcenter()
        maven(url = "https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

}

rootProject.name = "PeopleInSpace"

enableFeaturePreview("GRADLE_METADATA")

include(":app", ":common", ":compose-desktop")
include(":web")
include(":backend")