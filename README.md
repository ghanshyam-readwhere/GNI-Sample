# GNI Sample

Reference host app for the **GNI News SDK** (`com.gni:news-sdk:1.1.9`) — a minimal, readable
integration that wires up every SDK feature in one place.

## What it demonstrates

| Feature | Where |
| --- | --- |
| Category feeds (2 sections) | `NewsSdk.fetchArticles` → `HomeScreen.kt` |
| Streak UI — inline badge + floating pill | `insightsSection()` / `insightsFloating()` |
| Article detail + read tracking | `NewsSdk.recordRead` → `ArticleDetailScreen.kt` |
| AI analysis (Gemini + Firestore cache) | `NewsSdk.aiContent` |
| Home-screen widget (4×4 + shortcut row) | `widgetApiUrl` / `widgetShortcuts` config |
| Scheduled briefings + streak reminders | `notificationsEnabled`, `streakReminderEnabled` |
| "For You" personalised feeds | `NewsSdk.personalisedSection` |
| Sign in with Google | `NewsSdk.signInWithGoogle` |
| In-app review / update | `requestInAppReview`, `checkForInAppUpdate` |
| SDK analytics passthrough | `NewsSdk.setAnalyticsListener` |

Toolchain: AGP 8.5.0, Kotlin 1.9.24, Gradle 8.7, JDK 17, minSdk 24, compileSdk 36, Jetpack Compose.

## Try it without building

`app_sample.apk` in the repo root is a prebuilt debug build with every feature enabled and working —
including AI analysis and Sign in with Google. Install it directly, no toolchain or keys needed:

```bash
adb install app_sample.apk
```

Requires a device/emulator with Google Play services, and a Google account added on the device for
sign-in. Two caveats inherent to a sideloaded debug build: in-app review and in-app update only
surface on Play-installed builds, so they no-op; and the streak/briefing notifications fire on their
schedule (08:00, 17:00, 19:00), not on demand.

Build from source instead if you want to point the app at your own feeds, Firebase project or keys —
see Setup below.

## Setup

**1. Cloudsmith access.** The SDK is served from a private registry. Put the entitlement token in
`~/.gradle/gradle.properties` (never in the repo):

```
sdkEntitlementToken=<token provided by GNI>
```

`settings.gradle.kts` interpolates it into the repository URL. CI can pass
`-PsdkEntitlementToken=...` or set `ORG_GRADLE_PROJECT_sdkEntitlementToken` instead.

**2. Firebase.** `app/google-services.json` is included and points at the `gni-sample` Firebase
project (package `com.gni.sample`). The sample builds and runs as-is.

To point at your own project instead: create a Firebase project, register an Android app with
package name **`com.gni.sample`** (it must match exactly), download its `google-services.json` and
replace the one in `app/`.

The SDK uses the host app's default `FirebaseApp` for the AI summary cache (Firestore
`gni_ai_cache`) and the `gni_remote_config/ai` kill switch. Create the Firestore database and allow
client reads/writes to those two collections; the SDK auto-creates `gni_remote_config/ai` as
`{enabled: true}` on first run. It fails open — AI analysis still works without Firestore, but
nothing caches, so every article re-calls Gemini.

**3. Gemini API key.** The only value you must supply yourself. Add it to `local.properties`, which
is gitignored and must never be committed:

```
gemini_api_key=<Google AI Studio key>
```

Without it the AI analysis card renders its disabled state; the build still succeeds and every
other feature works.

The key is compiled into `BuildConfig`, so it is present in — and extractable from — any APK built
with it. Use a key restricted to the Generative Language API, and a separate key per environment.

Sign in with Google needs no setup here: `google_web_client_id` is already committed in
`app/build.gradle.kts` (an OAuth *client ID* is a public identifier, not a secret). Override it via
`local.properties` only if you switch to your own Google project.

**4. Sign-in prerequisite.** The build's signing certificate must be registered on the Firebase
Android app (Project settings → Your apps → Add fingerprint), or the account picker fails with a
generic error. Get the debug SHA-1 with:

```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android
```

## Run

Opening the project in Android Studio handles the Android SDK path for you. For a command-line
build, point Gradle at your SDK first — `local.properties` is gitignored, so a fresh clone has no
`sdk.dir` and the build fails with *"SDK location not found"*:

```bash
export ANDROID_HOME=$HOME/Library/Android/sdk   # or add sdk.dir=... to local.properties
./gradlew :app:installDebug
```

Requires a device/emulator with Google Play services for sign-in and in-app review/update.

## What's where

| File | Contents |
| --- | --- |
| `GniSampleApp.kt` | `NewsSdk.initialize` with every `NewsSdkConfig` field listed explicitly (feature flags switched on, copy/templates filled in, everything else at the SDK default); analytics listener |
| `Feeds.kt` | The two category feed URLs, the `ArticleKeys` JSON mapping, and categoryId → URL resolution |
| `HomeScreen.kt` | Two category sections, streak badge at top, floating streak pill in the FAB slot, "For You" feeds, sign-in button |
| `ArticleDetailScreen.kt` | `recordRead` on open, article body, and the AI analysis card |
| `MainActivity.kt` | Navigation, `parseTap` widget/notification routing, in-app review + update hooks, Google sign-in |

## Notes

- **Feature flags** ship OFF in the SDK. All are switched ON in `GniSampleApp` so this sample
  exercises everything; every other config value is left at the SDK default.
- **Streak UI** appears after `coldStartThreshold` distinct app-open days —
  `NewsSdkConfig.coldStartThreshold`, SDK default **1**, set explicitly to `1` here, so it shows
  from the very first launch. Raise it (e.g. `5`) to make new users build up usage first. The same
  gate applies to the streak / streak-lost reminders.
- **"For You" sections** appear once a category reaches `minTopicReadCount` reads (default 20).
- **In-app review and update** only surface on Play-installed builds, so they no-op on a local
  debug install.
- The widget provider, notification receivers and all permissions come from the SDK's own manifest
  via manifest merging — nothing to declare here. `network_security_config.xml` exists only because
  the widget shortcut icons are served over plain HTTP.
- `android:allowBackup="false"` is deliberate: the SDK's read-event DB is SQLCipher-encrypted with
  an Android Keystore key, which is not backed up. Allowing backup lets the encrypted DB restore
  onto a device that cannot decrypt it.
