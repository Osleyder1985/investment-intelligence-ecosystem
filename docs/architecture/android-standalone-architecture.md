# IIE Android Standalone Architecture

- **Document ID:** IIE-ARCH-ANDROID-STANDALONE-001
- **Version:** 1.0.0-draft
- **Date:** 2026-10-31
- **Status:** Proposed implementation baseline; pending repository review
- **Product scope:** First release for Android only

## 1. Decision

The first IIE release is a standalone Android application. Its user interface, application/domain logic, analytical capabilities, and primary database run on the Android device.

The first release does not require:
- a separately deployed backend;
- a remote database;
- Cloudflare Workers, D1, or R2;
- a continuously running PC or cloud server;
- user accounts or cross-device synchronization.

Internet access is optional for local application operation, but is required when retrieving external market, blockchain, project, or other provider data. External providers are data sources, not IIE backend infrastructure.

This decision establishes the product and deployment boundary for the first Android release. It does not claim that all listed capabilities are implemented.

## 2. Architectural principles

1. **Local-first:** the local database is the source of truth for data already acquired by the application.
2. **Offline-capable:** browsing, searching, reviewing stored evidence, and running supported analyses must work without network access.
3. **Evidence-aware:** externally sourced observations retain source identity, retrieval/observation timestamps, coverage, quality/status, and applicable licensing/attribution metadata.
4. **Explicit uncertainty:** missing, stale, conflicting, or unverified values remain distinguishable; they are never silently converted to zero or treated as current.
5. **Deterministic core where feasible:** analytical use cases should be testable with fixed inputs and reproducible configuration/version metadata.
6. **Resource-aware:** expensive operations are bounded, cancellable, resumable where appropriate, and must not block the UI thread.
7. **Modular portability:** domain rules must not depend on Android UI classes or a specific database implementation. This preserves a migration path if future workloads require external compute.
8. **No premature infrastructure:** no cloud backend, microservices, distributed queue, or remote database is introduced in the first release.

## 3. Technology baseline

| Concern | Initial technology | Notes |
|---|---|---|
| Platform | Android | First release is Android-only |
| Language | Kotlin | Main application and domain language |
| UI | Jetpack Compose | Presentation layer |
| App architecture | Modular, layered architecture; MVVM/UDF at presentation boundary | Avoid putting business rules in Composables |
| Local relational storage | Room over SQLite | Primary persistent store |
| Preferences | Jetpack DataStore | Small settings and user preferences |
| Background work | WorkManager | Deferrable, persistent work; not a promise of exact-time execution |
| Concurrency | Kotlin Coroutines and Flow | Cancellation and structured concurrency |
| Dependency injection | Constructor injection initially; evaluate Hilt when module count justifies it | Avoid unnecessary framework complexity |
| Tests | JUnit, coroutine tests, Room/database tests, Compose UI tests | Separate unit, integration, and UI coverage |
| Secrets | Android Keystore-backed mechanisms where secrets are genuinely required | Never embed provider secrets in the APK |
| Distribution | APK for controlled testing; Android App Bundle if later distributed through Play | Signing keys must remain protected |

Dependency versions must be selected as a compatible, maintained set during implementation and verified by Gradle build/CI. This document does not certify compatibility merely by naming the technologies.

## 4. Logical modules

The initial module boundaries are logical boundaries; they do not require a separate Gradle module for every item in the first commit.

### 4.1 Presentation
- Navigation and screens.
- ViewModels/state holders.
- User input and rendering.
- No direct SQL or provider calls from UI components.

### 4.2 Domain
- Core entities and value objects.
- Repository interfaces.
- Use cases and business rules.
- Explicit result, error, and uncertainty types.
- No dependency on Compose, Android UI, or Room APIs.

### 4.3 Data
- Room entities and DAOs.
- Repository implementations and mappings.
- Local transactions and migrations.
- Import/export boundaries.

### 4.4 Asset identity and universe
- Asset and network identifiers.
- Native asset versus token/representation distinctions.
- Identity resolution and duplicate/conflict handling.
- Search, filtering, pagination over local data.

### 4.5 Provenance and evidence
- Source/provider registry metadata.
- Retrieval and observation timestamps.
- Freshness and quality status.
- Evidence references, content hashes where appropriate, and audit history.
- Clear distinction between demo fixtures and externally sourced observations.

### 4.6 Analytics
- Fundamental and quantitative calculations as independently testable use cases.
- Versioned parameters and calculation metadata.
- Bounded workloads and cancellation.
- No investment ranking or recommendation until its criteria and validation are sufficiently specified.

### 4.7 Risk and decision
- Eligibility checks and non-compensable vetoes.
- Explicit abstention when evidence is insufficient.
- Explainable decision outputs and references to supporting evidence.
- No automatic execution of trades.

### 4.8 Synchronization and provider adapters
- Optional network clients for approved data providers.
- Rate-limit handling, retries with backoff, and cancellation.
- Incremental ingestion into the local database.
- Provider licensing, attribution, and permitted-use checks before integration.
- No secrets embedded in application source or distributable binaries.

## 5. Local data and integrity

Room/SQLite is the primary persistent store in the first release.

The schema must evolve through explicit, tested migrations. Destructive migration fallbacks must not be used for user data.

The data model must distinguish at least:
- canonical asset identity;
- network and contract/representation identity;
- provider/source identity;
- raw or normalized observation;
- observation time and retrieval time;
- freshness/status and quality;
- evidence and provenance references;
- analytical run, configuration/version, and result;
- user watchlist and preferences;
- audit events for material changes and decisions.

Large raw artifacts may require a file-based store with database metadata rather than storing every byte in SQLite. Retention and storage limits must be explicit.

Backups and exports must be user-accessible and tested for restore. Android backup behavior must be configured deliberately; it must not be assumed to provide a complete, reliable application backup.

## 6. Offline and online behavior

### Offline
- Read locally stored assets, observations, evidence, and analysis results.
- Search and filter the local universe.
- Run only analyses whose input coverage is sufficient.
- Display the age and status of stored information.
- Abstain from conclusions requiring unavailable or stale inputs.

### Online
- Retrieve data from approved providers.
- Validate and normalize responses.
- Preserve provenance and retrieval metadata.
- Commit updates transactionally.
- Report partial failures without presenting incomplete updates as complete.
- Refresh derived results only when their input dependencies and versions are known.

The UI must show the last successful update and the status of each relevant data category. Network availability alone does not prove data freshness.

## 7. Android resource constraints

The application must not assume server-class compute or unlimited storage.

Implementation requirements:
- Keep database and network work off the main thread.
- Use paging or bounded queries for large lists.
- Process large universes in chunks.
- Support cancellation and progress reporting for long analyses.
- Avoid retaining large historical datasets in memory.
- Make retention, cache eviction, and user-controlled export explicit.
- Test on representative low- and mid-range Android devices before setting performance claims.
- Do not promise continuous background execution; Android may defer or stop background work.

## 8. Security baseline

- Request only necessary Android permissions.
- Use HTTPS for external provider traffic.
- Do not ship private provider/API credentials in the APK.
- Store any necessary user secrets using Android platform security facilities.
- Minimize sensitive data collection; the first release does not need account credentials or exchange keys.
- Validate external input and treat provider payloads as untrusted.
- Protect exported backups and explain that exports may contain sensitive information.
- No wallet custody, private-key management, transaction signing, or trade execution in the first release.

## 9. Testing and release gates

Before calling the Android MVP buildable or tested, the repository must provide evidence of:
1. Reproducible Gradle Wrapper and documented JDK/Android SDK baseline.
2. Successful debug build in CI.
3. Unit tests for domain rules and mappings.
4. Room persistence, transaction, and migration tests.
5. Tests for missing, stale, conflicting, and malformed data.
6. Offline behavior tests.
7. Compose UI tests for the first vertical slice.
8. At least one emulator/device smoke test.
9. Backup/export and restore verification before relying on those features for user data.

A green Python workflow does not validate the Android application. Android CI status must be reported separately.

## 10. First vertical slice

The first end-to-end capability remains the Crypto Asset Universe Explorer, adapted to the standalone boundary:

1. Android app shell and navigation.
2. Local Room schema for asset identity and source/provenance metadata.
3. Demo fixture importer clearly separated from production/provider data.
4. Asset list, search, and detail screens backed by repository/use-case interfaces.
5. Local watchlist persistence.
6. Explicit data status, source, and timestamps.
7. Unit and database tests.
8. Reproducible Android build and CI.

No live provider is considered integrated until permitted use, attribution, rate limits, reliability, and licensing are reviewed.

## 11. Future evolution boundary

The domain and analytical modules must remain independent of UI and storage implementation details. If measured workloads exceed device constraints, selected computational use cases may later be delegated to an external service through explicit interfaces.

Such a future service is not part of the first release and must require a separate architecture decision. The Android application must remain useful for local data access and offline review.

## 12. Current status and non-claims

At the time of this draft:
- the Android development branch contains a Jetpack Compose explorer connected to a Room-backed repository and local watchlist;
- an initial Room schema, DAOs, database provider, asset repository, and instrumentation-test source have been added on a development branch;
- the Android CI run for commit `b201899f4c46c64d5e314aed26b0f9e05c0c8aae` successfully built the debug app, compiled Android test sources, and passed unit tests; the API 35 emulator booted, but instrumentation stopped at test-runner initialization (`Failed to instantiate test runner class androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner`), before the test methods ran;
- the generated Room schema version 1 was published as CI artifact `room-schema-android` and inspected: seven entities, primary keys, indexes, and foreign keys were present, including the composite observation-to-deployment reference `(deploymentId, assetId)`; the JSON is not yet committed;
- a transactional demo-fixture seeder and persistent watchlist interaction have been added as implementation work;
- the analytical engine and external provider integration are not yet implemented;
- a Gradle Wrapper is not yet present; the Android app build, Android-test compilation, and unit-test task passed in CI, but instrumented Room test methods have not yet executed successfully;
- no successful Room instrumentation run, performance benchmark, or security assessment is claimed by this document.

This is an architecture proposal and implementation baseline, not evidence of implementation, empirical validation, or ISO certification.

## 13. References for implementation review

- Android architecture recommendations: https://developer.android.com/topic/architecture/recommendations
- Android offline-first data layer guidance: https://developer.android.com/topic/architecture/data-layer/offline-first
- Room: https://developer.android.com/training/data-storage/room
- WorkManager: https://developer.android.com/topic/libraries/architecture/workmanager
- Android Keystore: https://developer.android.com/privacy-and-security/keystore
