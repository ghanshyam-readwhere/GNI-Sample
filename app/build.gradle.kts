import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) load(f.inputStream())
}

android {
    namespace = "com.gni.sample"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.gni.sample"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        // Gemini key for the AI analysis card. Never commit it — put it in local.properties:
        //   gemini_api_key=...
        buildConfigField(
            "String",
            "GEMINI_API_KEY",
            "\"${localProps.getProperty("gemini_api_key", "")}\""
        )
        // Public OAuth 2.0 "Web application" client id for Sign in with Google — NOT a secret; it
        // ships in the APK either way (see NewsSdkConfig.googleWebClientId). Committed so the
        // sample signs in as-is. Override in local.properties (google_web_client_id=...) only if
        // you point at a different Google project.
        buildConfigField(
            "String",
            "GOOGLE_WEB_CLIENT_ID",
            "\"${localProps.getProperty(
                "google_web_client_id",
                "666618533805-4faf8ses59fmo8llnilrd3sqoru8ovri.apps.googleusercontent.com"
            )}\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}

dependencies {
    implementation("com.gni:news-sdk:1.1.9")

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.2")
    implementation("androidx.activity:activity-compose:1.9.0")

    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("io.coil-kt:coil-compose:2.6.0")
}
