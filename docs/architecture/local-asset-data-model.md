# IIE Local Asset Data Model

- **Document ID:** IIE-DATA-ANDROID-ASSET-001
- **Version:** 0.1.0-draft
- **Status:** Proposed implementation model; pending review and runtime verification
- **Parent architecture:** IIE-ARCH-ANDROID-STANDALONE-001

## 1. Purpose and boundary

This model supports the first Android vertical slice: a local Crypto Asset Universe Explorer with identity and provenance foundations.

Room/SQLite is the local persistence mechanism. This model is not a complete investment-analysis schema and does not define valuation, portfolio accounting, trade execution, or a complete evidence-management system.

## 2. Identity rules

### 2.1 Canonical asset

An `Asset` represents the canonical economic/network asset concept managed by IIE. It has an IIE-owned `assetId` and a unique `canonicalKey`.

- `assetId` is the stable internal primary key.
- `canonicalKey` is a controlled IIE identity key; its derivation and normalization policy must be specified by the identity methodology before importing production data.
- Name and symbol are searchable attributes, not identity keys.
- Symbols are intentionally non-unique.
- A provider's listing ID is not the canonical IIE identity.

The current prototype uses illustrative strings only. They must not be mistaken for a production canonical-key standard.

### 2.2 External identifiers

`AssetIdentifier` maps an identifier issued by a source/provider or namespace to an IIE canonical asset.

Uniqueness is enforced by a generated `identifierKey`, whose construction must include the source, namespace, optional network scope, and normalized identifier value. This avoids treating a provider ID as globally unique.

Identifier conflicts must be retained as unresolved evidence or rejected for manual review; they must not silently reassign an existing canonical asset.

### 2.3 Networks and deployments

`Network` represents a network namespace/chain. `AssetDeployment` represents an asset as deployed or native on a network.

- Native assets and token representations are different deployment kinds.
- Token references are scoped to their network and namespace.
- The deterministic unique `deploymentKey` is the primary duplicate-prevention boundary.
- A nullable contract/reference field is expected for native assets; a token deployment must have a valid namespace-specific reference.
- Decimals are metadata, not an identity key.
- Wrapped, bridged, and issuer-specific representations must not be collapsed into the underlying asset without an explicit identity decision.

The exact representation taxonomy and normalization rules remain subject to the identity methodology.

## 3. Provenance and observations

### 3.1 Data source

`DataSource` records provider identity and metadata relevant to permitted use, attribution, and terms review.

A registered source is not automatically approved. The application must not ingest production data until the source's permitted-use status is positively reviewed.

### 3.2 Observation

`AssetObservation` stores one normalized, typed observation and its provenance linkage.

Required semantics:
- `sourceId`: source that supplied the observation;
- `observationType`: typed observation category;
- `observedAtEpochMs`: provider's observation time, nullable when unavailable;
- `retrievedAtEpochMs`: local retrieval time;
- `status` and `quality`: explicit interpretation state;
- `valueSchemaVersion`: version of the normalized JSON value structure;
- `valueJson`: normalized value, not an arbitrary executable payload;
- `payloadSha256`: optional integrity fingerprint of the exact retained source payload, not proof that the source itself is authentic;
- `licenseReference`: applicable terms/permission reference.

Freshness is derived from timestamps, observation type, source policy, and configured thresholds. A single generic freshness duration must not be applied to every data category.

Raw provider payloads are not stored by default. Retention requires a separate policy that accounts for provider terms, storage, privacy, and reproducibility needs.

## 4. Local user state

`WatchlistEntry` is user-owned local state, keyed by canonical `assetId`. Repeated add requests are idempotent and must not silently replace the original creation timestamp or note.

## 5. Current Room schema

The initial Room database includes:

| Table | Purpose | Main integrity boundary |
|---|---|---|
| `assets` | Canonical assets | Unique canonical key; non-unique symbol/name |
| `asset_identifiers` | Provider/namespace identifiers | Unique generated identifier key; foreign keys |
| `networks` | Network registry | Unique network key |
| `asset_deployments` | Native/token representations | Unique deployment key; asset/network foreign keys |
| `data_sources` | Provider/source registry | Unique source key |
| `asset_observations` | Time-stamped sourced observations | Asset/source/deployment foreign keys |
| `watchlist_entries` | Local user watchlist | Asset primary key and foreign key |

All times are stored as Unix epoch milliseconds in UTC. IDs and enum-like values are stored as strings in this first implementation. Their allowed vocabularies and validation rules must be centralized before production ingestion.

## 6. Transaction and migration policy

- Related asset, identifier, deployment, source, and observation inserts must use database transactions when a single import unit must be atomic.
- The current database is schema version 1 and is not yet released to users.
- Every subsequent schema change must increment the version and provide an explicit migration.
- Destructive migration fallback is prohibited for user data.
- Exported Room schemas must be committed and reviewed with schema changes.
- Foreign-key restrictions protect referenced identity/provenance records from accidental deletion.
- User watchlist entries may cascade only when the parent asset is deliberately deleted; referenced evidence may still prevent that deletion.

## 7. Implementation status and limits

Implemented in the current development branch:
- Kotlin domain models for assets, deployments, and observations;
- Room entities for the tables listed above;
- DAOs for core local queries and inserts;
- Room database declaration and app-private database provider;
- a repository interface and Room-backed implementation for asset reads/watchlist operations;
- instrumentation-test source for symbol non-uniqueness, canonical-key uniqueness, deployment references, and idempotent watchlist insertion;
- Room/KSP dependency configuration and schema export destination.

Not yet established:
- successful Gradle compilation or KSP schema generation;
- execution of instrumentation tests on an emulator/device;
- a committed generated Room schema JSON;
- production identifier canonicalization rules;
- production provider registry or licensed source integration;
- atomic import orchestration and conflict-resolution workflow;
- backup/restore verification;
- full UI integration.

The existence of test source is not evidence that tests have passed. No empirical validation or ISO compliance is claimed.
