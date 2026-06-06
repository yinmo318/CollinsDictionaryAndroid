# Collins Dictionary Android

Android-only Collins Dictionary client based on the original multiplatform project.

The app still reads definitions from Collins Online Dictionary pages and parses the
COBUILD section. It is for learning purposes only and is not affiliated with or
licensed by Collins.

## Open in Android Studio

1. Open this folder in Android Studio:
   `D:\CollinsDictionary\CollinsDictionaryAndroid`
2. Let Android Studio sync Gradle.
3. Build or run the `app` configuration.

The project is a single Android application module. The original desktop and
multiplatform modules are not included in this new project.

## Build on GitHub Actions

This project includes `.github/workflows/build-debug-apk.yml`.

Push this folder as a GitHub repository, then open the repository's Actions tab
and run **Build Debug APK**. The generated debug APK is uploaded as an artifact
named `collins-dictionary-debug-apk`.

The workflow runs:

```bash
./gradlew :app:assembleDebug --no-daemon
```

The APK is produced at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## What Changed

- Removed Kotlin Multiplatform and desktop packaging.
- Removed Koin; the Android app owns its repository and cache setup directly.
- Replaced raw URL string concatenation with OkHttp `HttpUrl` builders.
- Added safer local cache file names using SHA-256 keys instead of raw query text.
- Added atomic cache writes and corrupt-cache deletion.
- The UI consumes cached results first and then continues to refresh from the
  network, instead of stopping after the first cached emission.
- Search-not-found alternatives are displayed as clickable chips.
- Parser failures are less likely to crash the app because missing DOM nodes are
  treated as absent fields where possible.
- Android pronunciation playback now uses `MediaPlayer.prepareAsync()` and
  releases resources consistently.

## Notes

Collins can change its page structure at any time. If definitions stop loading,
update `CollinsHtmlParser.kt` first and add fixture tests for the new HTML shape.
