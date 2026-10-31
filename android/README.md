# IIE Android MVP

Standalone Android application using Kotlin and Jetpack Compose.

## Product boundary

The first release is designed to run on the Android device:
- no separately deployed backend;
- no remote database;
- no Cloudflare or cloud runtime dependency;
- local application/domain logic and analytical capabilities;
- Room/SQLite as the primary local persistent store;
- internet access only when approved external data providers are needed.

The domain and analytics layers must remain independent of Android UI and persistence details so that future compute offloading can be evaluated without rewriting the methodology. External processing is not part of the first release.

## Current prototype

The first screen is an **asset-universe explorer prototype** with:
- search by asset name or symbol;
- asset detail dialog;
- explicit demo-mode disclosure.

The current asset list is static demonstration data. It is not connected to a market-data provider, persistent database, wallet, exchange, or trading system. No prices or investment recommendations are shown.

## Toolchain baseline

- Android Studio with Android SDK Platform 35
- JDK 17
- Android Gradle Plugin 8.7.3
- Kotlin 2.0.21
- Gradle 8.9 or compatible Gradle distribution

Open the repository root as a Gradle project in Android Studio, allow Gradle sync, then run the `:app` configuration on an emulator or Android device.

## Planned local architecture

- Presentation: Jetpack Compose and ViewModels.
- Domain: entities, repository interfaces, and use cases.
- Data: Room/SQLite and repository implementations.
- Analytics: independently testable Kotlin calculation modules.
- Provenance: source, observation/retrieval timestamps, freshness, and evidence metadata.
- Background work: WorkManager for deferrable tasks; it does not guarantee exact-time execution.
- Preferences: DataStore where appropriate.

See [Standalone Android Architecture](../docs/architecture/android-standalone-architecture.md) for the proposed architecture and implementation gates.

## Known limitations

- No Gradle Wrapper has been committed yet; local Gradle/Android Studio setup is required.
- No Room persistence or local watchlist is connected.
- No analytical engine or external provider is integrated.
- Provenance timestamps, pagination, automated Android CI, and device/emulator verification are pending.
- The application ID `com.iie.android` is provisional.
- This prototype has not been verified by an Android build or device test in the current delivery.
