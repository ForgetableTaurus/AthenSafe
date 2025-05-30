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
        jcenter() // Προαιρετικό, αλλά μερικές παλιές libs ακόμα το χρησιμοποιούν
    }
}


rootProject.name = "My Application"
include(":app")
