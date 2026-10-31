# A01-RV-01..05 — Remediation decisions for network identity and lineage

**Document ID:** IIE-IDENTITY-NETWORK-LINEAGE-REMEDIATION-001  
**Version:** 0.1.0-draft  
**Date:** 2026-10-31  
**Parent proposal:** IIE-IDENTITY-NETWORK-LINEAGE-001 v0.1.0-draft  
**Review findings:** A01-RV-01 through A01-RV-05  
**Status:** PROPOSED / REVIEW_REQUIRED / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED  
**Branch:** `docs/network-identity-lineage-a01`

## 1. Purpose and decision summary

This document proposes explicit remediation decisions for the five HIGH findings in the A01 cross-audit. The goal is to remove implementation ambiguity without pretending that unresolved protocol, legal, security or institutional authority has already been supplied.

| Finding | Proposed resolution | Key safety property |
|---|---|---|
| A01-RV-01 | External trust bootstrap and versioned trust snapshots | A registry cannot self-authorize |
| A01-RV-02 | Explicit key-subject cardinality and alias/migration records | No implicit merge, reuse or reassignment |
| A01-RV-03 | Parent-preserving split transition; all children receive new IDs | No arbitrary child inherits parent identity |
| A01-RV-04 | Purpose/dimension-specific profile proof obligations | Fingerprint match cannot overstate identity |
| A01-RV-05 | State ownership and canonical crosswalk by dimension | No mixing factual, workflow, lifecycle or contract states |

These are candidate architectural decisions. They require review by architecture, protocol/security experts and the X-08 authority model. They do not amend canonical IIM requirements or authorize production implementation.

## 2. A01-RV-01 — Authority trust bootstrap

### 2.1 Separate identity, authentication and authorization

The registry MUST represent three different questions:

1. **Authority identity:** which stable authority principal is referenced?
2. **Authentication:** does the submitted record verify under the referenced key and cryptographic profile?
3. **Authorization:** was that authority, key and delegate permitted to issue this record for this namespace, record type, scope and effective interval?

A valid signature answers only the authentication question. It does not prove authority legitimacy or authorization.

### 2.2 External trust bootstrap

A consumer MUST obtain its initial trust anchors from a separately governed trust configuration, not solely from the registry being evaluated.

The bootstrap configuration is a versioned, immutable trust snapshot and MUST identify:
- trust configuration ID and version;
- authority principal IDs;
- root key IDs and cryptographic profile versions;
- authorized namespaces and record classes;
- validity intervals and activation/revocation events;
- approval evidence and approving authority references;
- distribution/authentication method for the trust snapshot;
- predecessor/successor snapshot references;
- emergency suspension and recovery policy.

The trust configuration itself requires an approved governance process and authenticated distribution channel. This proposal does not select the real-world root authorities, trust-anchor count, cryptographic algorithms or threshold/quorum values.

A registry MUST NOT add itself or a newly self-asserted authority to the consumer's trust anchors. Adding a trust anchor is a separate trust-administration decision with independent authorization and audit evidence.

### 2.3 Trust evaluation

For each record, evaluate independently:
- signature/record authenticity;
- key validity at signing/effective time;
- authority mandate validity;
- delegate authorization and scope;
- namespace/record-type permission;
- trust snapshot applicability;
- revocation/compromise status;
- record lifecycle and evidence admissibility.

The result is a structured trust assessment, not a single boolean. Missing trust snapshot, unresolved authority chain, invalid signature or out-of-scope delegation MUST prevent favorable registry trust.

### 2.4 Rotation, revocation and compromise

Key rotation creates a new key identity and an explicit transition signed/approved under the applicable trust policy. The prior key remains historically addressable.

Distinguish:
- planned key retirement;
- authority mandate expiry;
- delegate revocation;
- key compromise with known/unknown compromise interval;
- authority compromise;
- registry content error.

A revocation event MUST state its reason class, effective interval, knowledge/recorded time, affected key/authority/namespace/record set, evidence, approving authority and required reevaluation action.

If compromise time is unknown, consumers MUST NOT assume all prior signatures are valid or invalid. They must mark the affected trust assessment unresolved/review-required under policy and abstain from purposes requiring trusted authority until adjudication.

Historical replay pins the exact trust snapshot and revocation knowledge cutoff used at the time. A corrected replay may use later revocation knowledge but MUST NOT rewrite AS_KNOWN_THEN.

### 2.5 A01-RV-01 acceptance criteria

- [ ] Self-signed authority records cannot bootstrap their own trust.
- [ ] Identity, authentication and authorization are separate assessments.
- [ ] Trust snapshot is versioned, authenticated, approved and pinned in replay.
- [ ] Key rotation, mandate revocation and compromise are distinct event classes.
- [ ] Unknown compromise interval cannot silently preserve or erase trust.
- [ ] Real root authorities, cryptographic profile and approval process remain explicit open decisions.

## 3. A01-RV-02 — NetworkKey to NetworkSubject cardinality

### 3.1 Canonical binding rule

A qualified NetworkKey is an authority-issued registry handle. A NetworkSubject is the identity subject. Their relationship MUST be explicit and versioned.

Proposed constraints:

1. One active qualified NetworkKey maps to exactly one NetworkSubject at any effective instant.
2. One NetworkSubject MAY have multiple qualified NetworkKeys only through explicit, approved alias/federation records.
3. Within one authority/namespace, a NetworkSubject SHOULD have one designated canonical key; additional keys require a typed reason and non-overlapping authority/scope semantics.
4. A key MUST NOT be rebound from one NetworkSubject to another, including after retirement.
5. A NetworkSubject ID MUST NOT be reused, merged or reassigned.
6. Namespace migration creates a new qualified key and an explicit mapping; it does not mutate the old key.
7. Historical key resolution returns the subject binding valid at the requested valid-time and knowledge-time cutoffs.

### 3.2 Binding relation types

Use distinct relation types:
- CANONICAL_KEY_FOR: authority-designated canonical key for one subject within a namespace and interval.
- ALIAS_KEY_FOR: additional key for the same subject, explicitly authorized.
- SUCCESSOR_KEY_FOR: replacement key issued under a namespace/authority succession event; historical key remains resolvable.
- FEDERATED_REFERENCE_FOR: external authority's key mapped to a local subject under an explicit federation agreement.
- DUPLICATE_CANDIDATE_FOR: investigative relation only; never a resolution mapping.

These relations are not interchangeable and are not automatically transitive. The exact catalog still requires canonical relationship approval.

### 3.3 Merge prohibition and duplicate handling

Two NetworkSubjects MUST NOT be merged because their identifiers match or because an authority asserts they are duplicates. A merge proposal requires:
- explicit source and target subjects;
- identity dimensions and purpose/scope affected;
- proof that both records refer to the same subject under the applicable profile;
- review of dependent claims, assessments, branches and relationships;
- conflict and downstream impact analysis;
- independent approval and append-only merge/supersession event.

Until approval, both subjects remain distinct and the relationship is DUPLICATE_CANDIDATE_FOR or CONFLICTED. No downstream consumer may collapse them by heuristic.

A merge, if ever approved, is a registry-level resolution event; it does not delete either historical subject ID or rewrite historical assessments. This proposal does not authorize merges.

### 3.4 A01-RV-02 acceptance criteria

- [ ] Active qualified key resolves to exactly one subject for any requested effective instant.
- [ ] Key reuse/rebinding is prohibited.
- [ ] Multi-key subject binding requires explicit typed authorization.
- [ ] Namespace migration preserves historical keys and creates explicit mappings.
- [ ] Duplicate candidates never auto-merge.
- [ ] Historical resolution is valid-time and knowledge-time aware.

## 4. A01-RV-03 — Deterministic permanent split transition

### 4.1 Conservative parent-preserving policy

When a permanent split is approved, the pre-split NetworkSubject remains the immutable historical parent describing the shared pre-divergence lineage and historical scope. It is not silently assigned to either child.

Every post-split child receives a newly allocated, immutable NetworkSubject ID. No child inherits or reuses the parent's ID.

The split event MUST contain:
- split_event_id and idempotency key;
- parent NetworkSubject ID and exact parent registry version;
- divergence coordinate and parent checkpoint/branch reference;
- all child subject IDs and their family profiles;
- evidence and adjudication record references;
- valid-time effective interval and recorded/knowledge time;
- scope/purpose of the split conclusion;
- authority, reviewer, approver and SoD evidence;
- dependency impact and downstream reassessment requirements;
- deterministic allocation policy/version and event digest.

The parent retains historical identity. Its current applicability to post-divergence operations is not presumed. A consumer asking which current child is "the continuation" receives no implicit answer.

### 4.2 No privileged continuation child

The model MUST NOT select a continuation child based solely on:
- largest market capitalization or token price;
- provider count or endpoint popularity;
- chain name, ticker or social-media usage;
- chain ID or genesis hash alone;
- first-seen order or registry insertion order;
- arbitrary default or repository maintainer preference.

If a competent governance/protocol authority explicitly designates a continuity successor, record that as a separate, evidenced and scoped relationship (e.g. CONTINUITY_SUCCESSOR_OF). It does not transfer the parent ID and does not make other child IDs aliases.

If no authorized continuity determination exists, all children remain distinct and the continuity question remains unresolved.

### 4.3 Distinguish split from temporary branch

A permanent split cannot be declared merely because two endpoints temporarily disagree. Required evidence must include:
- a protocol-specific divergence and finality assessment;
- history ancestry and branch proof appropriate to the family;
- sustained independent histories under an approved policy;
- governance/operational evidence where required by the purpose;
- independent source and trust assessment;
- adjudication by an authorized role.

Until these obligations are met, record competing HistoryBranches under the existing subject and leave permanent split adjudication unresolved. The numerical duration, confirmation threshold and family-specific finality criteria remain open for A03 protocol profiles.

### 4.4 Partial and disputed splits

If only some scope/purpose is affected, the split assessment MUST be bound to an exact derived atomic scope. Parent-level records and unrelated scopes remain unchanged. A disputed split preserves all proposals and evidence; no last-write-wins.

### 4.5 A01-RV-03 acceptance criteria

- [ ] Permanent split preserves parent subject as historical lineage.
- [ ] Every child receives a new immutable subject ID.
- [ ] No child inherits parent ID by default.
- [ ] Continuity designation is a separate authorized relationship.
- [ ] Temporary branch/reorg cannot be promoted to permanent split without profile-specific proof and adjudication.
- [ ] Disputed split and partial scope impact are preserved and replayable.

## 5. A01-RV-04 — Minimum family-specific proof obligations

### 5.1 Profile completeness is purpose-specific

A protocol profile MUST NOT define a single universal "network verified" flag. It defines predicates and evidence obligations for separate identity dimensions:

| Identity dimension | Question |
|---|---|
| FAMILY_IDENTIFICATION | Is the protocol/network family correctly identified? |
| BOOTSTRAP_FINGERPRINT | Does the supplied genesis/bootstrap artifact match the declared profile? |
| NETWORK_INSTANCE | Does evidence distinguish this instance from clones, resets and lookalikes? |
| ENDPOINT_BINDING | Is this endpoint bound to the identified instance for the observed interval? |
| HISTORY_BRANCH | Which branch does the observation belong to? |
| CANONICAL_HISTORY | What evidence supports canonicality/finality for the requested family and time? |
| CONTINUITY_OR_LINEAGE | Is the claimed continuity, split or migration supported? |

A purpose declares which dimensions are mandatory. A successful narrow predicate MUST NOT be promoted to a broader dimension.

### 5.2 Required profile contract

Each approved family profile MUST define, for every predicate:
- predicate ID/version and exact proposition;
- subject and scope binding;
- mandatory input artifacts and canonical encodings;
- source and evidence admissibility requirements;
- verification procedure and verifier implementation/version;
- cryptographic/hash/protocol parameters where applicable;
- time/finality assumptions and valid-time semantics;
- corroboration and failure-domain requirements;
- contradiction, missing-data and stale-data behavior;
- known limitations and explicit unsupported cases;
- test vectors and independent review evidence.

A profile MUST specify which artifacts are independently recomputed, which are merely source assertions, and which require consensus/client verification. A provider-returned identifier is not independently verified merely because it is syntactically valid.

### 5.3 EVM profile obligations (candidate interface, not a completed protocol specification)

At minimum, separate:
- chain ID observation and canonical integer parsing;
- genesis header/artifact identity and hash recomputation under an explicitly referenced EVM header/hash profile;
- chain configuration and fork schedule claims;
- historical checkpoint ancestry and endpoint observations;
- endpoint-to-subject binding;
- clone/private/test environment classification;
- canonical-history/finality assessment for the requested height/time.

Chain ID plus genesis hash can support a bootstrap fingerprint match only. It cannot by itself establish current canonical history, governance continuity, endpoint binding or production environment.

This document does not specify exact header serialization, fork-dependent fields, consensus finality proof or client verification algorithm. Those belong to an independently reviewed EVM profile and A03.

### 5.4 Solana profile obligations (candidate interface, not a completed protocol specification)

At minimum, separate:
- cluster/genesis identity observation with exact bytes/encoding;
- independently authenticated bootstrap/genesis artifact where available;
- slot/block ancestry and branch observations;
- commitment/finality semantics under the approved Solana profile;
- endpoint-to-subject binding;
- validator-set, feature activation and software profile as time-bounded claims;
- canonical-history and environment assessment.

A cluster label or genesis fingerprint cannot by itself establish current branch, endpoint binding, validator-set continuity or production environment.

This document does not specify exact ledger artifact serialization, proof format, commitment threshold, validator-set verification or client verification algorithm. Those belong to an independently reviewed Solana profile and A03.

### 5.5 Profile compatibility and abstention

Profile versions MUST declare compatibility and supersession. An assessment is replayed with the exact profile version originally used. A newer profile creates a new assessment; it does not rewrite prior conclusions.

Mandatory abstention cases include missing mandatory artifact, unsupported family/version, unresolved endpoint binding, ambiguous fork lineage, invalid trust snapshot, unresolved source independence, incompatible profile versions or material contradictory evidence.

### 5.6 A01-RV-04 acceptance criteria

- [ ] Profiles define predicates by identity dimension and purpose.
- [ ] Raw artifact, encoding, verifier and version are pinned.
- [ ] Source assertion is distinguished from independent verification.
- [ ] EVM/Solana bootstrap match is not over-promoted.
- [ ] Profile compatibility and mandatory abstention are explicit.
- [ ] Protocol-specific cryptographic/finality details remain open until expert-reviewed.

## 6. A01-RV-05 — Canonical state ownership and crosswalk

### 6.1 Object-owned state dimensions

The A01 proposal's illustrative outcomes MUST be treated as decision result labels, not inserted into canonical domain status catalogs without approval.

| A01 concept/value | Owning object/dimension | Canonical integration rule |
|---|---|---|
| SAME_NETWORK_SUBJECT | Identity adjudication decision | Decision outcome; not IdentityStatus |
| DISTINCT_NETWORK_SUBJECTS | Split/lineage adjudication decision | Decision outcome; not ClaimResolutionStatus |
| TEMPORARY_BRANCH_ONLY | Branch/lineage assessment result | Branch result; not workflow state |
| IDENTITY_UNRESOLVED | Identity domain assessment | Map only through approved IdentityStatus/assessment contract |
| IDENTITY_CONFLICTED | Identity domain assessment | Map only through approved canonical conflict semantics |
| INVALID_REQUEST | Input/contract validation | INVALID belongs to contract validity, never factual identity |
| OBSERVED / COMPETING | HistoryBranch observation/assessment | Branch-specific states; not IdentityStatus |
| CANONICAL_CANDIDATE | Branch canonicality assessment | Requires profile-specific predicate; not proof of finality |
| ORPHANED | HistoryBranch disposition | Must preserve evidence and protocol context |
| FINALIZED_HISTORY | Historical branch/finality assessment | Must bind protocol-specific finality proof and time |
| UNRESOLVED (branch) | Branch assessment result | Must not be confused with workflow NOT_ASSESSED |

### 6.2 Orthogonal state axes

Every record MUST keep distinct:
- contract validity;
- identity domain status;
- claim resolution;
- evidence admissibility;
- evidence sufficiency;
- freshness;
- assessment workflow;
- record lifecycle;
- branch state;
- adjudication decision outcome;
- authority/trust state.

No axis may be used as a substitute for another. In particular:
- INVALID is not an identity fact;
- UNKNOWN is not NOT_ASSESSED;
- STALE is not UNRESOLVED;
- REVIEW_REQUIRED is not CONFLICTED;
- SUPERSEDED is not a negative identity finding;
- branch ORPHANED is not proof that the NetworkSubject ceased to exist.

### 6.3 Producer/consumer boundary

A01 identity resolver may emit only the outputs assigned to its approved contract. Split adjudication is a separate governance decision consumer. A branch verifier may emit branch evidence/status but cannot allocate subject IDs or approve a permanent split. A registry writer may persist only approved events and cannot manufacture evidence or approve its own exceptional action.

Every output must identify:
- producer and contract version;
- object type and state dimension;
- policy/profile/catalog versions;
- subject/scope/time;
- evidence/dependency references;
- workflow/lifecycle/freshness dimensions;
- decision authority and approval references, if any.

### 6.4 Canonical catalog gate

Before implementation, an architecture-owned crosswalk MUST map all A01 values to the approved R01 catalogs and X-07 lifecycle/workflow catalogs, including producers, consumers, transition preconditions, terminality, invalidation and legacy handling.

If canonical catalogs are not approved, A01 remains draft and implementations MUST NOT introduce competing "canonical" vocabularies.

### 6.5 A01-RV-05 acceptance criteria

- [ ] Every value is assigned to one object and one state dimension.
- [ ] INVALID_REQUEST is contract validation only.
- [ ] Identity, branch, workflow, lifecycle, freshness and trust are orthogonal.
- [ ] Producer/consumer responsibilities are explicit.
- [ ] Full transition/crosswalk review is required before implementation.

## 7. Cross-cutting deterministic replay requirements

Every registry/identity/split decision MUST pin:
- trust snapshot and revocation knowledge cutoff;
- registry authority, namespace and entry versions;
- NetworkSubject and key-binding versions;
- family profile and verifier versions;
- source/failure-domain graph and evidence records;
- scope signature and purpose/activity;
- valid-time and knowledge-time cutoffs;
- branch/checkpoint/finality assessment references;
- canonical status catalogs and transition policy;
- dependency graph snapshot and closure proof;
- decision algorithm version and deterministic serialization profile.

If any required dependency is missing or cannot be reproduced, the replay result is INCOMPLETE/UNRESOLVED under the appropriate owning dimension. It must not be converted to a favorable identity result.

## 8. Adversarial test vectors added by remediation

| ID | Case | Required result |
|---|---|---|
| RV-001 | Registry self-signs a new authority root | Not trusted absent external approved anchor |
| RV-002 | Valid signature from unauthorized delegate | Authenticated, not authorized; reject trust |
| RV-003 | Key compromise interval unknown | Trust unresolved/review; no silent PASS |
| RV-004 | Two keys bind same subject without alias authorization | Registry integrity conflict |
| RV-005 | Retired key rebound to another subject | Reject |
| RV-006 | Namespace migration rewrites old key | Reject; require explicit successor mapping |
| RV-007 | Duplicate subjects share genesis fingerprint | Keep distinct pending adjudication |
| RV-008 | Permanent split processed by two independent registries | Both allocate parent-preserving split with new child IDs under same policy |
| RV-009 | One child is popular/has higher market cap | No automatic continuity designation |
| RV-010 | Split evidence insufficient for finality | Retain branches; no permanent split |
| RV-011 | Parent subject queried for post-split operational route | No implicit child mapping; unresolved until scoped mapping |
| RV-012 | EVM chain ID/genesis match but no endpoint proof | Bootstrap match only |
| RV-013 | Solana cluster label/genesis match but slot ancestry conflict | No canonical-history PASS |
| RV-014 | Provider asserts canonicality without required artifact | Insufficient evidence |
| RV-015 | A01 result label INVALID_REQUEST stored as identity status | Reject catalog/contract conformance |
| RV-016 | Branch ORPHANED mapped to NetworkSubject UNRESOLVED | Reject state-dimension substitution |
| RV-017 | Freshness STALE but identity historically resolved | Preserve separate axes |
| RV-018 | New catalog version changes mapping | New assessment; preserve historical replay |

These are proposed acceptance vectors, not executable tests. They do not assert that protocol-level evidence is available for any real network.

## 9. Cross-document dependencies and approval gates

| Dependency | Required integration |
|---|---|
| X-09 Scope Algebra | Exact scope signature, atomic partition, membership uncertainty and proof binding |
| X-07 Lifecycle | valid-time/knowledge-time, immutable events, invalidation and replay |
| X-08 Governance | authorized trust bootstrap, split adjudicator, delegation, SoD and emergency suspension |
| R01 identity decisions | IdentityStatus, ClaimResolution, Applicability, evidence and assessment workflow ownership |
| NCP A02 | source independence, cryptographic trust, provenance, key lifecycle and revocation |
| NCP A03 | EVM/Solana checkpoint ancestry, finality, reorg and branch proof |
| NCP A04 | dependency closure and exact scope/proof binding |
| NCP A05 | real authority roles, delegation and emergency process |

Approval gates:
1. Architecture review of decisions in §§2–6.
2. Protocol/security review of family-specific proof obligations.
3. Governance approval of trust bootstrap and split adjudication authority.
4. Cross-audit against current approved canonical catalogs.
5. Independent adversarial review of original 35 and new 18 vectors.
6. Only then, separate authorization for schemas, code and executable tests.

## 10. Final status

**PROPOSED / REVIEW_REQUIRED / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED.**

This remediation resolves the five HIGH findings at the design-proposal level by supplying explicit candidate rules. It does not close them institutionally or technically until the dependencies and approval gates above are satisfied.

No canonical IIM requirement, production schema, resolver, fixture, test or live registry was changed. No real network identity, authority or split was verified. No tests were executed for this document.
