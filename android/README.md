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

## Current prototype and persistence work

The current explorer UI is still a prototype backed by static demonstration data. It supports search by asset name/symbol and a detail dialog. It is not yet connected to Room.

A first local persistence layer has been added on a development branch:
- canonical assets and non-unique ticker symbols;
- provider/namespace identifiers mapped to canonical assets;
- networks and native/token deployments;
- data-source registry and time-stamped observations;
- local watchlist entries;
- Room DAOs, database declaration/provider, and a Room-backed asset repository;
- instrumentation-test source for core identity and persistence constraints.

The data model and test source are pending Android compilation, generated Room schema review, and emulator/device test execution. Their presence does not mean the tests have passed.

## Toolchain baseline

- Android Studio with Android SDK Platform 35
- JDK 17
- Android Gradle Plugin 8.7.3
- Kotlin 2.0.21
- KSP 2.0.21-1.0.28
- Room 2.6.1
- Gradle 8.9 or compatible Gradle distribution

Open the repository root as a Gradle project in Android Studio, allow Gradle sync, then run the `:app` configuration on an emulator or Android device.

Room schema export is configured to `app/schemas`. Generated schema JSON must be reviewed and committed after a successful KSP build.

## Planned local architecture

- Presentation: Jetpack Compose and ViewModels.
- Domain: entities, repository interfaces, and use cases.
- Data: Room/SQLite and repository implementations.
- Analytics: independently testable Kotlin calculation modules.
- Provenance: source, observation/retrieval timestamps, freshness, and evidence metadata.
- Background work: WorkManager for deferrable tasks; it does not guarantee exact-time execution.
- Preferences: DataStore where appropriate.

See:
- [Standalone Android Architecture](../docs/architecture/android-standalone-architecture.md)
- [Local Asset Data Model](../docs/architecture/local-asset-data-model.md)

## Known limitations

- No Gradle Wrapper has been committed yet; local Gradle/Android Studio setup is required.
- The explorer UI still uses static demo data and is not wired to the repository.
- Production canonicalization/identifier normalization rules are not finalized.
- No analytical engine or external provider is integrated.
- No generated Room schema JSON has been verified or committed.
- Android compilation, instrumentation tests, automated Android CI, and device/emulator verification are pending.
- The application ID `com.iie.android` is provisional.
