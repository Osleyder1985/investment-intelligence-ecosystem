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

The explorer now observes the Room-backed asset repository through a ViewModel. Search is performed against local SQLite data, and the user can add/remove assets from a persistent local watchlist. The first-run catalogue is an explicitly labelled local demo fixture.

A first local persistence layer has been added on a development branch:
- canonical assets and non-unique ticker symbols;
- provider/namespace identifiers mapped to canonical assets;
- networks and native/token deployments;
- data-source registry and time-stamped observations;
- local watchlist entries;
- Room DAOs, database declaration/provider, and a Room-backed asset repository;
- instrumentation-test source for core identity and persistence constraints.

The Room data model, fixture seeder, and explorer integration are on development branches/stacked pull requests. The Android CI run for commit `b201899f4c46c64d5e314aed26b0f9e05c0c8aae` successfully built the debug app, compiled Android test sources, and passed unit tests. The API 35 emulator booted and started instrumentation, but the test runner failed to initialize (`Failed to instantiate test runner class androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner`). The Room instrumentation tests therefore remain unvalidated. Test source and successful compilation are not evidence that those tests passed.

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
- The initial catalogue contains only four demo assets; it is not a production-wide asset universe.
- Production canonicalization/identifier normalization rules are not finalized.
- No analytical engine or external provider is integrated.
- The demo fixture does not establish production asset identity; USDC is intentionally left without a network deployment because its representation is multi-chain.
- The Room schema export was generated and published as CI artifact `room-schema-android` (run #27, artifact 13054179613). The exported version-1 JSON was inspected: seven entities, primary keys, indexes, and foreign keys are present, including the composite observation-to-deployment reference `(deploymentId, assetId)`. The schema JSON is not yet committed.
- Android CI run #27 booted the API 35 emulator and started instrumentation, but failed before executing the test methods because the AndroidJUnit4 test runner could not be instantiated. This is a test infrastructure/runner initialization failure; it is not evidence of a Room assertion failure. The unit-test task and Android compilation passed.
- The application ID `com.iie.android` is provisional.
