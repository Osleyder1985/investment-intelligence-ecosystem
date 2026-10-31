# A01 — Cross-document consistency and adversarial review

**Review ID:** IIE-IDENTITY-NETWORK-LINEAGE-REVIEW-001  
**Version:** 0.1.0-draft  
**Date:** 2026-10-31  
**Target:** `network-identity-registry-and-lineage-contract.md` v0.1.0-draft  
**Review type:** Static cross-document and adversarial review  
**Status:** REVIEW_REQUIRED / INTEGRATION_REVISE / NOT_APPROVED  
**Independence:** Internal design review; not external or institutional assurance.

## 1. Determination

The A01 proposal materially improves the design by authority-qualifying registry keys, separating NetworkSubject from HistoryBranch and EndpointObservation, treating EVM/Solana genesis identifiers as limited fingerprints, and distinguishing reorg, split, reset and migration.

It is **not yet ready for approval or implementation**. Several definitions remain incomplete or depend on unresolved A02/A03/A04/A05 contracts. In particular, authority-root trust, subject/key cardinality, permanent-split subject continuity, family-specific proof obligations, status crosswalk and exact scope binding require closure.

**Disposition: REVIEW_REQUIRED / INTEGRATION_REVISE / NOT_APPROVED.** The original NCP-AR-01..03 findings are addressed conceptually but are not closed as implementation-ready controls.

## 2. Review evidence and limits

Reviewed:
- A01 proposal on branch `docs/network-identity-lineage-a01`;
- NCP adversarial review of the parent network evidence contract;
- X-09 Scope Algebra document 19 and formal verification strategy 20;
- X-07 lifecycle/freshness document 12;
- X-08 RACI/SoD document 13;
- identity architecture D-09/D-10;
- parent network registry/checkpoint contract on PR #72.

No live registry, key infrastructure, protocol client, RPC endpoint, consensus proof, legal authority or production schema was tested. This review does not establish factual identity of any network.

## 3. Findings summary

| ID | Severity | Finding |
|---|---|---|
| A01-RV-01 | HIGH | Authority root identity and trust bootstrap are circular/incomplete |
| A01-RV-02 | HIGH | NetworkKey and network_subject_id create unresolved cardinality/alias semantics |
| A01-RV-03 | HIGH | Permanent split does not define parent-subject continuation and child allocation deterministically |
| A01-RV-04 | HIGH | Family identity profiles still lack minimum proof obligations and version compatibility rules |
| A01-RV-05 | HIGH | Identity outcomes and branch states are not reconciled with canonical R01/X-07 status catalogs |
| A01-RV-06 | MEDIUM | Network relationship vocabulary lacks cardinality, inverse, temporal and conflict constraints |
| A01-RV-07 | MEDIUM | Environment classification and private/local network uniqueness need stronger semantics |
| A01-RV-08 | MEDIUM | Scope binding and dependency closure are referenced but not concretely bound |
| A01-RV-09 | MEDIUM | Registry event concurrency, ordering and idempotency need explicit aggregate/version rules |
| A01-RV-10 | MEDIUM | Authority revocation and trust compromise effects on previously approved records are deferred |

**Count:** 5 HIGH, 5 MEDIUM, 0 LOW.

## 4. Detailed findings

### A01-RV-01 — Authority root identity and trust bootstrap are incomplete

**Severity:** HIGH

The proposal requires an immutable `registry_authority_id` and an immutable key/fingerprint reference, but does not define how the first authority identity is trusted, how its root key is anchored, how bootstrap is independently approved, or how authority identity is preserved through root-key rotation and compromise.

An opaque authority ID is not globally meaningful without a trust anchor. A key fingerprint identifies key material but does not prove the key is authorized to operate a registry.

**Adversarial case:** A malicious registry publishes a self-signed authority record with a new authority ID and valid signature. Every local signature check passes, but no consumer has a trust path to accept that authority.

**Required remediation:**
- define a trust bootstrap mechanism and root-of-authority policy;
- distinguish authority identity, key identity, authorization and authentication;
- define root-key rotation, compromise, revocation and recovery;
- pin the authority trust snapshot in every replay;
- specify how independent approval evidence is established.

**Closure:** approved authority trust bootstrap profile and adversarial trust-substitution scenarios. Depends on A02/A05.

### A01-RV-02 — NetworkKey and network_subject_id cardinality is unresolved

**Severity:** HIGH

The proposal defines a qualified NetworkKey and separately defines an opaque `network_subject_id`, but does not explicitly state whether the relationship is one-to-one, one-to-many across versions, or many-to-one through aliases. Section 4.4 prohibits overlapping active records for different subjects, but the exact alias/canonical key mapping and subject merge prohibition are not fully formalized.

**Adversarial case:** An authority issues two qualified keys for the same subject after a namespace migration; another registry imports both as separate subjects. A consumer cannot determine whether they are aliases, successors or duplicates.

**Required remediation:**
- define key-to-subject cardinality and immutable binding;
- distinguish alias, successor, replacement and duplicate-suspect records;
- prohibit subject merge/ID reuse without a separately approved merge protocol;
- define key deprecation and resolution of historical references;
- specify whether one subject may have multiple authority-qualified keys and under what explicit federation/alias agreement.

**Closure:** key-subject mapping table, constraints and migration/alias adversarial scenarios.

### A01-RV-03 — Permanent split lacks deterministic parent/child subject continuity

**Severity:** HIGH

The proposal says a permanent split may create two or more NetworkSubjects, but does not define whether the pre-split NetworkSubject remains a historical parent, is retired, continues as one child, or is superseded by all children. Different policies could produce different stable IDs for the same split event.

**Adversarial case:** Two registry operators process the same permanent split. One preserves the original subject for chain A and creates a child for chain B; the other retires the original and creates two new subjects. Both satisfy the prose, producing incompatible registry identity.

**Required remediation:**
- define deterministic split allocation policy, including treatment of the pre-split subject;
- require a split event with parent subject, divergence point, child subject IDs, effective times and evidence;
- define whether continuity can be assigned to one child, and the evidence/policy required;
- preserve all historical subject references and prohibit silent reassignment;
- define behavior where no child can be authoritatively designated as continuation.

**Closure:** split transition table and deterministic test vectors. Depends on A03/A05.

### A01-RV-04 — Family identity profiles lack minimum proof obligations

**Severity:** HIGH

The EVM and Solana sections list candidate evidence and high-level rules but do not specify minimum required claims, raw artifact formats, exact hash/serialization profile references, endpoint binding proof, profile compatibility windows or failure semantics for missing protocol artifacts.

The document correctly says the profiles are not implemented, but without minimum proof obligations it cannot determine when a profile is complete enough to support even a bounded identity resolution.

**Adversarial case:** An EVM profile accepts a genesis hash string from a single provider while raw header bytes and serialization profile are unavailable. Another implementation requires raw header recomputation. Both claim conformance.

**Required remediation:**
- define profile-specific mandatory claim sets by identity dimension and purpose;
- bind raw artifacts, encodings, hash algorithms and verifier versions;
- distinguish bootstrap fingerprint match from endpoint-to-network binding and history continuity;
- define profile compatibility, deprecation and migration;
- define mandatory abstention conditions.

**Closure:** EVM/Solana profile tables with predicate, artifact, verifier, evidence class, status and limitation columns. Depends on A02/A03.

### A01-RV-05 — Proposed outcomes/states are not aligned with canonical status catalogs

**Severity:** HIGH

The proposal introduces SAME_NETWORK_SUBJECT, DISTINCT_NETWORK_SUBJECTS, TEMPORARY_BRANCH_ONLY, IDENTITY_UNRESOLVED, IDENTITY_CONFLICTED and INVALID_REQUEST, plus branch states OBSERVED, COMPETING, CANONICAL_CANDIDATE, ORPHANED, FINALIZED_HISTORY and UNRESOLVED.

R01 and X-07 separate domain status, assessment workflow, freshness, evidence admissibility, record lifecycle and contract validity. The A01 proposal acknowledges the need for crosswalk but does not define which object owns each proposed outcome or whether these are decision outcomes, identity-domain statuses, branch statuses or workflow statuses.

**Required remediation:**
- classify every proposed value by object/state dimension;
- map to R01 IdentityStatus, ClaimResolution, Applicability, EvidenceStatus and X-07 lifecycle/workflow without direct semantic collapse;
- keep INVALID_REQUEST in contract validation, not factual identity status;
- define transition ownership and terminality;
- remove or rename any values that duplicate canonical catalogs.

**Closure:** producer/consumer and transition crosswalk approved by architecture.

### A01-RV-06 — Network relationship vocabulary lacks full constraints

**Severity:** MEDIUM

Relationship types are listed, but inverse semantics, allowed endpoint types, cardinality, cycle constraints, time overlap, conflict handling and whether relationships are asserted versus adjudicated are not specified. `SPLIT_FROM` and `FORKED_FROM` may overlap semantically; `SUCCESSOR_OF` and `MIGRATED_TO` may be redundant.

**Required remediation:** relationship catalog with domain/range, direction, inverse, cardinality, transitivity, temporal constraints, evidence predicates and conflict behavior.

### A01-RV-07 — Environment classification needs controlled semantics

**Severity:** MEDIUM

The example values production, test, development, private, local and unknown are not necessarily mutually exclusive or exhaustive. A private production network, public test network or local fork can occupy multiple dimensions. Environment is modeled as one classification even though visibility, purpose and deployment context may be orthogonal.

**Required remediation:** split environment into typed dimensions such as deployment context, access/visibility, intended use and operational status; define unknown and multi-valued semantics. Do not infer production from public accessibility.

### A01-RV-08 — Scope binding is not concrete enough

**Severity:** MEDIUM

A01 requires a scope signature, subject selector, purpose/activity, universe and normalization versions, but does not define the exact canonical signature fields or bind them into the identity assessment digest. X-09 also requires exact partition and dependency semantics.

**Required remediation:** defer the authoritative signature to an approved X-09 contract, but specify the exact required references and reject an identity result when any scope dimension is unresolved. Include the scope digest and dependency snapshot in replay proof binding.

### A01-RV-09 — Registry event ordering and concurrency are incomplete

**Severity:** MEDIUM

Event idempotency by event ID plus payload digest is necessary but does not define aggregate version preconditions, event sequence, concurrent updates, causal ordering, duplicate delivery, delayed events or conflict resolution.

**Required remediation:** define expected aggregate version, monotonic sequence per subject/namespace, causation/correlation IDs, idempotency scope, stale-write rejection and deterministic replay ordering.

### A01-RV-10 — Authority revocation effects are deferred

**Severity:** MEDIUM

The proposal says revocation blocks new trust according to policy but does not define how prior records are assessed when revocation is retroactive, time-bounded, uncertain or caused by key compromise. This intersects A02 trust anchors and A05 authority governance.

**Required remediation:** distinguish authority mandate revocation from key compromise and invalid registry content; define affected-record scope, effective time, historical replay and reapproval policy.

## 5. Closure sequence

1. Resolve A01-RV-01 and A01-RV-10 jointly with A02/A05 authority trust.
2. Resolve A01-RV-02 and A01-RV-03 with explicit key-subject mapping and split transition rules.
3. Resolve A01-RV-04 with protocol-specific EVM/Solana profile proof obligations.
4. Resolve A01-RV-05 with R01/X-07 state ownership and crosswalk.
5. Resolve A01-RV-06..09 with relationship, environment, scope and event contracts.
6. Run a second adversarial review and then propose executable contract/schema work as a separately authorized task.

## 6. What can be retained

Retain as sound design directions:
- authority-qualified keys rather than bare local keys;
- immutable subject IDs and append-only registry versions;
- explicit separation of NetworkSubject, HistoryBranch and EndpointObservation;
- chain/genesis identifiers as evidence-bearing fingerprints, not universal identity proofs;
- no automatic subject mutation from reorg, chain ID change or provider change;
- explicit lineage relationships and no inferred asset mapping across split/migration;
- deterministic abstention when identity evidence is insufficient;
- explicit governance and cross-document approval gates.

## 7. Final status

**REVIEW_REQUIRED / INTEGRATION_REVISE / NOT_APPROVED.**

The A01 proposal is a meaningful specification advance but is not implementation-ready. The five HIGH findings block approval. No canonical requirement, schema, code, fixture or test was changed. No tests were run specifically for this review. No external independent assurance or real-network identity validation is claimed.
