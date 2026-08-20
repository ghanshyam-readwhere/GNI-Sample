// Cloudsmith entitlement token. Set it in ~/.gradle/gradle.properties:
//   sdkEntitlementToken=<token provided by GNI>
// or pass -PsdkEntitlementToken=... / ORG_GRADLE_PROJECT_sdkEntitlementToken for CI.
val sdkEntitlementToken = providers.gradleProperty("sdkEntitlementToken").orNull ?: ""

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
        maven {
            url = uri("https://dl.cloudsmith.io/$sdkEntitlementToken/gni_arena/android-sdks-gni/maven/")
        }
    }
}

rootProject.name = "GniSample"
include(":app")
