pluginManagement {
    // 1) Pre-declare the Google Services plugin with a version
    plugins {
        id("org.jetbrains.kotlin.android") version "1.9.10" apply false
        id("com.google.gms.google-services") version "4.3.15" apply false
    }

    // 2) Define where to find plugins
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "PoePart2"
include(":app")
