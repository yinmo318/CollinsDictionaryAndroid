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

---

# Collins Dictionary Android 中文说明

这是一个非官方的 Android 英语词典客户端，通过 Collins Online Dictionary
网页查询英文单词，并解析其中的 COBUILD 词典内容。应用使用原生 Android
Jetpack Compose 界面展示释义。

本项目与 Collins 官方无关，未获得 Collins 授权、认可或许可。Collins 名称
和词典内容归其各自权利人所有。

## 功能

- 在 Android 应用中查询英文单词。
- 展示 COBUILD 释义、词形、词频、例句、同义词、IPA 音标，以及可用的发音音频。
- 当 Collins 返回相似词时，展示可点击的拼写建议。
- 本地缓存查询结果，并在联网时刷新缓存内容。
- 支持跟随系统的浅色/深色主题。
- 支持通过 GitHub Actions 自动构建 debug APK。

## Android 要求

- 最低系统版本：Android 6.0，API 23
- 目标系统版本：Android 15，API 35
- 所需权限：`android.permission.INTERNET`

应用只使用网络权限请求词典网页和发音音频。缓存数据保存在应用私有存储中。

## 下载和安装

测试版本可以在 GitHub 仓库的 **Actions** 页面运行 **Build Debug APK**，
然后下载 `collins-dictionary-debug-apk` 构建产物。

debug APK 输出路径：

```text
app/build/outputs/apk/debug/app-debug.apk
```

debug APK 使用 Android 默认 debug key 签名，只适合个人测试。

## 从源码构建

用 Android Studio 打开此目录：

```text
D:\CollinsDictionary\CollinsDictionaryAndroid
```

等待 Gradle 同步完成，然后构建或运行 `app` 配置。

命令行构建：

```bash
./gradlew :app:assembleDebug --no-daemon
```

## GitHub Actions

项目包含：

```text
.github/workflows/build-debug-apk.yml
```

该 workflow 会安装 JDK 17，准备 Gradle wrapper，执行：

```bash
./gradlew :app:assembleDebug --no-daemon
```

然后把 debug APK 上传为 GitHub Actions artifact。

## 正式发布构建

如果要公开发布，需要创建 release 签名密钥，并在 `app/build.gradle.kts`
中配置 Android Gradle 签名；也可以通过 Android Studio 操作：

```text
Build > Generate Signed Bundle / APK
```

推荐发布产物：

```bash
./gradlew :app:bundleRelease
```

公开发布前，请先审阅 Collins 网站条款和商标要求。本应用会请求并解析
Collins 网页，商业用途或公开上架可能需要内容权利人的许可。

## 项目结构

```text
app/src/main/java/me/konyaco/collinsdictionary/android/
audio/      发音播放
data/       仓储层和查询流程
domain/     词典数据模型
network/    Collins HTTP 客户端和 HTML 解析器
storage/    本地缓存
ui/         Jetpack Compose 界面
```

## 已知限制

- 解析器依赖 Collins 网页结构。如果 Collins 修改 HTML，查询或解析可能失败，
  需要更新 `CollinsHtmlParser.kt`。
- 当前主要支持 COBUILD 词典内容。
- 尚未包含 American Dictionary、English Dictionary、每日单词、多标签浏览、
  以及完整的 release 签名自动化。

## 开发说明

这个 Android-only 项目由原多平台代码整理而来，目标是更方便地作为 Android
应用维护和发布。新版本移除了桌面端打包和多平台共享配置，使用 Android 原生
Compose，实现了更安全的缓存处理，并更稳健地处理 Collins 搜索重定向。
