# Collins Dictionary Android

An unofficial Android dictionary client for looking up English words through
Collins Online Dictionary pages. The app focuses on the COBUILD dictionary
section and presents definitions in a native Android Compose interface.

This project is not affiliated with, endorsed by, or licensed by Collins. The
Collins name and dictionary content belong to their respective owners.

## Features

- Search English words from an Android app.
- Display COBUILD definitions, word forms, word frequency, examples, synonyms,
  IPA pronunciation, and pronunciation audio when available.
- Show spelling suggestions when Collins returns similar alternatives.
- Cache lookup results locally and refresh cached entries when online.
- Support light and dark system themes.
- Build debug APKs automatically with GitHub Actions.

## Android Requirements

- Minimum SDK: Android 6.0, API 23
- Target SDK: Android 15, API 35
- Required permission: `android.permission.INTERNET`

The app only uses network access to request dictionary pages and pronunciation
audio. Cached search data is stored locally in the app's private storage.

## Download and Install

For testing builds, open the repository's **Actions** tab, run **Build Debug
APK**, then download the `collins-dictionary-debug-apk` artifact.

Debug APK output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Debug APKs are signed with the default Android debug key and are suitable for
personal testing only.

## Build from Source

Open this folder in Android Studio:

```text
D:\CollinsDictionary\CollinsDictionaryAndroid
```

Let Android Studio sync Gradle, then build or run the `app` configuration.

Command line build:

```bash
./gradlew :app:assembleDebug --no-daemon
```

## GitHub Actions

The project includes:

```text
.github/workflows/build-debug-apk.yml
```

The workflow installs JDK 17, prepares the Gradle wrapper, runs:

```bash
./gradlew :app:assembleDebug --no-daemon
```

and uploads the debug APK as an artifact.

## Release Builds

For public distribution, create a release signing key and configure Android
Gradle signing in `app/build.gradle.kts` or through Android Studio:

```text
Build > Generate Signed Bundle / APK
```

Recommended release artifact:

```bash
./gradlew :app:bundleRelease
```

Before publishing, review Collins' website terms and trademark requirements.
Because this app retrieves and parses Collins web pages, commercial or public
store distribution may require permission from the content owner.

## Project Structure

```text
app/src/main/java/me/konyaco/collinsdictionary/android/
audio/      Pronunciation playback
data/       Repository and lookup flow
domain/     Dictionary data models
network/    Collins HTTP client and HTML parser
storage/    Local cache
ui/         Jetpack Compose UI
```

## Known Limitations

- The parser depends on Collins page structure. If Collins changes its HTML,
  lookup or parsing may fail until `CollinsHtmlParser.kt` is updated.
- The app currently focuses on COBUILD entries.
- American Dictionary, English Dictionary, daily word, multi-tab browsing, and
  full release signing automation are not included.

## Development Notes

This Android-only project was derived from the original multiplatform codebase
and simplified for Android distribution. It removes desktop packaging and shared
multiplatform setup, uses Android-native Compose, improves cache safety, and
handles Collins search redirects more defensively.
