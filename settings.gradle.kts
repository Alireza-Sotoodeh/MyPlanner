pluginManagement {
  repositories {
    maven { url = uri("https://dl.google.com/dl/android/maven2/") }
    mavenCentral()
    gradlePluginPortal()
    mavenLocal()
  }
  resolutionStrategy {
    eachPlugin {
      if (requested.id.id == "com.android.application" || requested.id.id == "com.android.library") {
        useModule("com.android.tools.build:gradle:${requested.version}")
      }
    }
  }
}


dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
  }
}

rootProject.name = "My Application"

include(":app")
