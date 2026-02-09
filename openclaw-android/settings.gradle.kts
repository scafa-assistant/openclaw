pluginManagement {
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
        // Fallback repositories
        maven { url = uri("https://plugins.gradle.org/m2/") }
        maven { url = uri("https://repo1.maven.org/maven2") }
    }
}

rootProject.name = "OpenClaw"
include(":app")
