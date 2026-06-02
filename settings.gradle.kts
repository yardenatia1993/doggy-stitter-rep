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

if (!JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17)) {
    throw GradleException(
        "DoggySitter requires JDK 17 or newer to run Gradle. " +
                "Set JAVA_HOME to a JDK 17+ installation or configure Android Studio's Gradle JDK."
    )
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Doggy sitter"
include(":app")
