# IIE Android MVP

Native Android application using Kotlin and Jetpack Compose.

## Current scope

The first screen is an **asset-universe explorer prototype** with:
- search by asset name or symbol;
- asset detail dialog;
- explicit demo-mode disclosure.

The current asset list is static demonstration data. It is not connected to a market-data provider, backend API, wallet, exchange, or trading system. No prices or investment recommendations are shown.

## Toolchain baseline

- Android Studio with Android SDK Platform 35
- JDK 17
- Android Gradle Plugin 8.7.3
- Kotlin 2.0.21
- Gradle 8.9 or compatible Gradle distribution

Open the repository root as a Gradle project in Android Studio, allow Gradle sync, then run the `:app` configuration on an emulator or Android device.

## Known limitations

- No Gradle Wrapper has been committed yet; local Gradle/Android Studio setup is required.
- No backend API or persistence is connected.
- Watchlist, provider integration, provenance timestamps, pagination and automated Android CI are pending.
- The application ID `com.iie.android` is provisional.
