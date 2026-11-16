pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
    plugins {
        kotlin("jvm") version "2.4.21"
        kotlin("plugin.compose") version "2.4.21"
        id("org.jetbrains.compose") version "1.12.2"
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "kotlin-forgeember"
