# A01 — Network identity registry, fork lineage and lifecycle contract

**Document ID:** IIE-IDENTITY-NETWORK-LINEAGE-001  
**Version:** 0.1.0-draft  
**Date:** 2026-10-31  
**Status:** DRAFT / REVIEW_REQUIRED / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED  
**Workstream:** NCP-A01  
**Findings addressed:** NCP-AR-01, NCP-AR-02, NCP-AR-03  
**Parent:** Issue #71  
**Related review:** `network-registry-checkpoint-contract-adversarial-review.md`  
**Normative intent:** Proposed design contract only. No canonical IIM requirement or production authority is created.

## 1. Executive decision

This proposal defines a typed identity and lineage model for network registry subjects. It resolves three design ambiguities:

1. A network key is not globally unique merely because it is unique inside one registry. Its full identity is authority-qualified.
2. A genesis identifier or chain identifier is an identity observation/fingerprint component, not proof by itself that two endpoints follow the same continuing network history or have the same operational/economic identity.
3. A temporary competing branch, finalized divergence, permanent split, reset and migration are distinct events. They must not be collapsed into one generic "fork" label.

The design separates:
- **Registry namespace identity** — who issued a key and within which namespace.
- **Network subject identity** — the persistent registry subject representing a network instance/lineage.
- **Protocol identity profile** — family-specific rules for interpreting identifiers and history.
- **History branch** — a branch of observed ledger history, which may be transient or permanent.
- **Endpoint observation** — what a particular source reported at a time.
- **Network relationship** — explicit, evidenced lineage between distinct subjects.
- **Identity assessment** — a scoped and temporal conclusion about the subject.

A stable registry subject ID MUST NOT be silently rewritten when evidence changes. New evidence, corrections, disputes and lineage events create new immutable records/versions and assessments.

**Disposition:** suitable for continued design review only; not a production registry specification until cross-audit and governance approval.

## 2. Scope and non-goals

### 2.1 In scope
- Authority-qualified registry keys and namespace governance.
- Canonical network subject references and versioning.
- Family-specific identity profile interface, initially EVM and Solana as design targets.
- Separation of network identity from history branch and endpoint observation.
- Fork, split, reset, migration, retirement and correction semantics.
- Evidence obligations and deterministic adjudication workflow.
- Temporal identity and replay constraints.
- Adversarial conformance scenarios and invariants.

### 2.2 Out of scope
- Populating the registry with real networks or declaring any real network verified.
- RPC clients, consensus clients, cryptographic proof implementations or live provider ingestion.
- Selecting numeric source quorums, confirmation thresholds, finality thresholds or TTL values.
- Deciding economic-asset identity, token backing, solvency, redemption rights or asset equivalence.
- Production JSON Schemas, database migrations, resolver code, fixtures or CI tests.
- Assigning real people/teams or approving X-08 governance roles.
- Treating chain-list websites, provider metadata or user labels as authoritative by default.

## 3. Terminology and subject separation

| Concept | Meaning | Must not be confused with |
|---|---|---|
| RegistryAuthority | Governance principal authorized to issue registry records | RPC provider or repository contributor |
| RegistryNamespace | Key namespace controlled under an authority | Network family or chain ID |
| NetworkSubject | Persistent registry subject representing a network instance/lineage | Endpoint, chain name or ticker |
| NetworkKey | Authority-qualified stable key for a NetworkSubject | Bare local key |
| IdentityProfile | Versioned family-specific identifier and evidence rules | A claim that a network is verified |
| HistoryBranch | A particular ledger history branch from an explicit divergence point | A permanent network subject by default |
| EndpointObservation | Source-specific report about an endpoint at a time | Canonical network identity |
| NetworkRelationship | Typed, directional, temporal relationship between NetworkSubjects | Implicit alias or equivalence |
| IdentityAssessment | Purpose/scope/time-specific evaluation of identity claims | Mutable field on NetworkSubject |
| RegistryEntryVersion | Immutable content version of a subject's registry record | Subject identity itself |

A NetworkSubject may have multiple endpoints, providers, observations and history branches. None of those objects individually defines the NetworkSubject.

## 4. Authority-qualified registry identity

### 4.1 Qualified key

A registry key MUST be represented as a structured tuple:

`NetworkKey = (registry_authority_id, namespace_id, local_network_key)`

All three components are mandatory, typed, canonical and immutable identifiers.

- `registry_authority_id`: stable identifier of the authority namespace root; it MUST NOT be a display name, GitHub username, organization label or mutable URL.
- `namespace_id`: stable namespace controlled by that authority, with explicit creation and retirement records.
- `local_network_key`: opaque, non-reused key allocated within that namespace.

A serialized qualified key MUST include a versioned namespace/identifier profile. A consumer MUST reject a bare `local_network_key` where a qualified key is required.

### 4.2 Authority identity and delegation

RegistryAuthority identity is distinct from the human or service account currently exercising a role. Its record SHOULD include:
- authority ID and immutable key/fingerprint reference;
- authority class and jurisdictional/legal identity where relevant;
- authorization basis and scope;
- validity interval and lifecycle state;
- delegation records with explicit permissions and expiry;
- signing/trust profile references;
- predecessor/successor and revocation events;
- independent approval and audit references.

Authority transfer MUST NOT mutate the historical issuer of prior records. Transfer creates an explicit succession event with effective time, scope, approvals and continuity evidence. A successor authority cannot retroactively rewrite the predecessor's issued claims.

### 4.3 Namespace and federation rules

- Namespace IDs MUST be unique within their authority and MUST NOT be recycled.
- Federation MUST preserve each authority-qualified key; federation does not merge namespaces.
- A federated registry MUST record the federation agreement/version, participating authority IDs, trust scope, effective interval and conflict policy.
- A federation consumer MUST NOT infer that two authorities are equivalent or mutually trusted merely because both participate.
- Key collision across authorities is not a collision if full qualified keys differ; collision or aliasing inside one authority/namespace is a registry integrity incident.
- Namespace migration MUST create a signed/approved mapping record; old keys remain resolvable as historical references and are never silently rewritten.
- Revocation of an authority blocks new trust in its records according to a versioned policy; it does not erase historical issuance facts.

### 4.4 Uniqueness invariants

Uniqueness MUST be checked across the complete authority-qualified tuple, namespace lifecycle, registry version and effective interval. The registry MUST prevent:
- duplicate qualified keys;
- reuse of retired local keys;
- overlapping active records that claim the same qualified key for different subjects;
- unapproved alias substitution;
- ambiguous resolution when authority or namespace is unavailable.

A uniqueness check does not prove factual network identity; it proves only registry key integrity.

## 5. NetworkSubject identity model

### 5.1 Persistent subject identifier

Each NetworkSubject receives an immutable opaque `network_subject_id` allocated under an approved ID profile. The ID is not derived from chain ID, genesis hash, endpoint URL, chain name or provider ID.

The subject record includes:
- network_subject_id and schema/profile version;
- qualified NetworkKey;
- family and protocol profile reference/version;
- environment classification (e.g. production, test, development, private, local, unknown) under a controlled vocabulary;
- asserted identifiers with original raw values and normalized values;
- identity claims and evidence references;
- known history/lineage relationships;
- valid-time interval and record lifecycle;
- issuer, reviewer, approval and change-event references;
- limitations, conflicts and unresolved dimensions.

The same network_subject_id persists through ordinary endpoint changes, provider outages, software upgrades and temporary reorganizations. It MUST NOT be reused for a distinct network subject.

### 5.2 Identity is multidimensional and purpose-scoped

Identity conclusions MUST be evaluated for an explicit scope and purpose. At minimum, distinguish:
- protocol/network-family identification;
- network instance identification;
- endpoint-to-network binding;
- history-branch membership;
- canonical-history assessment;
- operational route suitability;
- economic-asset or representation linkage (out of scope of this A01 contract).

A result such as "same genesis fingerprint" may support protocol/bootstrap similarity but MUST NOT automatically imply:
- same current canonical history;
- same operator/governance community;
- same security assumptions;
- same asset namespace for every purpose;
- same operational route or venue eligibility;
- same economic asset or representation.

If the required identity dimension cannot be established, the assessment remains UNRESOLVED or CONFLICTED. It MUST NOT be promoted based on a name, ticker, chain ID alone or majority vote without a claim-specific policy.

### 5.3 Identifier observations versus identity claims

Every identifier is recorded as an observation or atomic claim, not an unqualified fact.

Example:
- Observation: endpoint E reported EVM chain ID 1 at observed time T.
- Claim: endpoint E was bound to NetworkSubject N under identity profile P at time T.
- Stronger claim: the observed history branch was canonical for NetworkSubject N at time T.

These claims have different predicates and evidence requirements. Evidence supporting one MUST NOT be reused as proof of another without an explicit inference rule.

## 6. Family-specific identity profile interface

A family profile MUST define required fields, canonicalization, evidence, conflict behavior, temporal rules and known limitations. A profile is versioned, reviewed and approved separately from any specific NetworkSubject.

### 6.1 EVM profile (design target; not implemented here)

Candidate identity evidence may include:
- EVM chain ID as an unsigned integer with strict canonical parsing;
- genesis block header and independently recomputed genesis block hash;
- chain configuration and fork activation schedule where available;
- selected historical checkpoint/header ancestry;
- client/protocol compatibility profile;
- environment and operator/governance assertions as separate claims.

Rules:
1. Chain ID is a routing/protocol field and is not globally unique.
2. A claimed genesis hash MUST be bound to raw genesis/header data and a declared hash/serialization profile; provider-returned text alone is not independent verification.
3. Matching chain ID and genesis hash is a bootstrap fingerprint match, not proof of shared continuing history or identical governance.
4. Divergence after a shared ancestor is represented as history branches first. Whether it creates a new NetworkSubject requires the split adjudication protocol in §8.
5. Chain configuration, fork schedule, client implementation and governance are separate claims; none is silently inferred from chain ID/genesis.
6. Private, local, cloned and test environments MUST remain distinguishable when evidence supports the distinction; unknown environment remains UNKNOWN.

### 6.2 Solana profile (design target; not implemented here)

Candidate identity evidence may include:
- genesis/cluster identity under a precisely defined encoding;
- raw genesis artifacts or authenticated checkpoint/bootstrap evidence;
- slot/block history and ancestry observations;
- feature activation and software/protocol profile;
- validator-set and cluster configuration observations as separate claims.

Rules:
1. A cluster label, endpoint URL or provider's cluster name is not a canonical key.
2. A reported genesis hash MUST be bound to the profile's exact bytes/encoding and source evidence.
3. Matching genesis identity does not prove endpoints currently follow the same fork or canonical ledger.
4. Slot observations are fork-sensitive and MUST be associated with ancestry/commitment evidence where required by the purpose.
5. Validator-set, feature activation and software identity are separate claims and time-bounded.
6. The profile MUST distinguish unavailable, stale, conflicting and not-assessed evidence; absence of an observation is not proof of a distinct network.

### 6.3 Other families

UTXO, Cosmos/IBC, Cardano/eUTXO, rollups and other families require separately versioned profiles. No profile may be inferred by adapting EVM/Solana fields without a documented equivalence argument and approval.

## 7. History branches and network identity

### 7.1 Two-level model

The model distinguishes:
- NetworkSubject: persistent identity record for a network instance/lineage.
- HistoryBranch: branch-specific history segment identified by a branch record, parent branch, divergence point and observations.

A temporary competing branch is not automatically a new NetworkSubject. A permanent split may result in two or more NetworkSubjects connected by explicit lineage relationships, but only after the split adjudication protocol.

### 7.2 Branch record

A HistoryBranch record SHOULD contain:
- branch_id, network_subject_ref and profile version;
- parent_branch_ref (if any);
- divergence coordinate and parent checkpoint reference;
- observed start/end and knowledge-time interval;
- source/evidence set and canonicality assessment references;
- branch state (OBSERVED, COMPETING, CANONICAL_CANDIDATE, ORPHANED, FINALIZED_HISTORY, UNRESOLVED);
- assessment policy/version and reasons.

Branch states describe evidence and assessment, not universal consensus truth. Their exact canonical vocabulary requires integration approval.

### 7.3 No automatic identity mutation

- A reorg changes branch/canonicality assessments and may invalidate dependent assessments; it does not automatically change NetworkSubject ID.
- A protocol upgrade does not automatically create a new NetworkSubject.
- A chain ID change does not automatically prove migration or distinct identity.
- A matching genesis hash does not automatically merge two NetworkSubjects.
- A permanent split does not automatically remap assets, contracts, representations, accounts or instruments between children.
- Any mapping across a split requires separate subject-specific claims and evidence.

## 8. Fork, split, reset and migration adjudication

### 8.1 Event taxonomy

| Event | Meaning | Default identity effect |
|---|---|---|
| Temporary competing branch / reorg | Competing history that may later be abandoned or reconciled under protocol rules | Same NetworkSubject; new HistoryBranch |
| Finalized divergence | Conflicting history claims that cannot be reconciled under the applicable finality assumptions | Identity/canonicality CONFLICTED; suspend affected purpose pending adjudication |
| Permanent network split | Durable divergence with distinct ongoing histories and governance/operation | Candidate distinct NetworkSubjects with explicit lineage |
| Reset/relaunch | History or state restarted under a new bootstrap/genesis or explicit reset procedure | New NetworkSubject candidate; never silently overwrite |
| Migration | Coordinated transition to a successor network/ledger | Preserve old subject; create successor subject and typed migration relation |
| Protocol upgrade | Change to protocol/client/rules within continuity assumptions | Usually same NetworkSubject; profile/configuration claim version changes |
| Endpoint/provider change | Infrastructure/source changes | No identity effect by itself |
| Registry correction | Erroneous record or evidence corrected | New registry entry version and correction event; no historical erasure |

"Default" is a conservative processing rule, not a substitute for evidence or approved adjudication.

### 8.2 Required split decision inputs

A split adjudication MUST evaluate, as separate claims:
- shared ancestor and divergence coordinate;
- protocol-specific finality and whether conflicting finalized histories exist;
- chain configuration and consensus-rule changes;
- continuity of validator/operator set and governance, where relevant;
- public declarations, social/governance process and operational continuity, where relevant;
- asset/contract continuity claims (never inferred from network split alone);
- source independence, evidence admissibility and temporal validity;
- exact purpose and scope of the requested identity decision.

No single factor (duration, chain ID, name, token price, social media declaration or provider count) is sufficient by itself.

### 8.3 Deterministic adjudication outcomes

Proposed decision outcomes:
- SAME_NETWORK_SUBJECT: evidence supports continuity for the requested purpose/scope.
- DISTINCT_NETWORK_SUBJECTS: evidence supports distinct subjects with explicit lineage.
- TEMPORARY_BRANCH_ONLY: distinct observed histories, no permanent subject split established.
- IDENTITY_UNRESOLVED: insufficient admissible evidence or unresolved criteria.
- IDENTITY_CONFLICTED: admissible evidence supports incompatible identity/lineage claims.
- INVALID_REQUEST: malformed scope/profile/contract; not a factual network conclusion.

Outcomes are proposed and must be mapped to canonical identity/assessment catalogs before implementation.

### 8.4 Concurrent or disputed adjudications

Conflicting split proposals MUST NOT be resolved by last-write-wins. The registry must:
1. retain both proposals and their evidence;
2. mark affected identity/lineage assessment CONFLICTED or REVIEW_REQUIRED;
3. preserve current subject records while restricting only explicitly affected purposes/scopes;
4. require an independent adjudication case and authority;
5. publish a new version only after the required approval;
6. preserve dissent, rationale, effective time and replay inputs.

## 9. Network relationship contract

Relationships between NetworkSubjects MUST be typed, directional, time-bounded and evidenced. Initial proposed relationship vocabulary:
- FORKED_FROM
- SPLIT_FROM
- MIGRATED_TO
- RESET_FROM
- SUCCESSOR_OF
- SAME_PROTOCOL_FAMILY_AS
- SHARES_GENESIS_WITH
- SHARES_ANCESTOR_WITH
- DISPUTED_LINEAGE_WITH

The vocabulary is not an assertion that every relationship is transitive. In particular:
- SHARES_GENESIS_WITH does not imply SAME_NETWORK_SUBJECT.
- SHARES_ANCESTOR_WITH does not imply current canonical-history equivalence.
- FORKED_FROM does not imply economic-asset equivalence.
- MIGRATED_TO does not imply one-to-one identifier continuity.
- SAME_PROTOCOL_FAMILY_AS does not imply shared security or governance.

Each relationship record includes subject/object refs, relation type/version, scope, valid time, recorded time, claims/evidence, lifecycle, producer, reviewer/approver and dependency references.

## 10. Temporal, lifecycle and correction semantics

### 10.1 Time model

Every material assertion distinguishes:
- valid_time: when the asserted fact applies in the modeled world;
- observed_at: when a source/collector observed it;
- recorded_at: when the system durably recorded it;
- evaluated_at: when the assessment ran;
- effective_at: when an approved registry event takes effect;
- knowledge_cutoff/profile: which information was admissible to a historical evaluation.

A correction recorded today MUST NOT rewrite what was knowable at an earlier cutoff. AS_KNOWN_THEN and corrected-history reconstruction remain distinct views under X-07/D-07.

### 10.2 Separate state dimensions

Do not merge:
- identity resolution status;
- claim resolution;
- evidence admissibility/sufficiency;
- freshness;
- assessment workflow;
- registry record lifecycle;
- branch/canonicality status;
- authority/trust status.

The exact status catalogs and transitions must be crosswalked to approved R01 and X-07 catalogs. This proposal intentionally does not create a replacement canonical status vocabulary.

### 10.3 Registry event lifecycle

Proposed event classes:
- SUBJECT_REGISTERED
- ENTRY_VERSION_PUBLISHED
- EVIDENCE_ADDED
- IDENTITY_DISPUTE_OPENED
- SUBJECT_QUARANTINED_FOR_SCOPE
- SPLIT_PROPOSED
- SPLIT_ADJUDICATED
- MIGRATION_DECLARED
- AUTHORITY_DELEGATED
- AUTHORITY_REVOKED
- ENTRY_CORRECTED
- SUBJECT_RETIRED

Events are append-only and idempotent by event ID plus payload digest. Corrections supersede prior records; they do not delete them. Quarantine must specify exact purpose/scope and must not silently affect unrelated scopes.

## 11. Deterministic registry resolution procedure

Given a requested qualified NetworkKey, scope, purpose, time cutoff and profile version:

1. Validate the qualified key syntax and canonicalization profile.
2. Resolve authority and namespace records at the requested knowledge cutoff.
3. Validate authority status, delegation chain and applicable trust snapshot.
4. Resolve exactly one immutable NetworkSubject and applicable entry version.
5. Validate profile compatibility and effective interval.
6. Retrieve required atomic identity claims and their evidence assessments.
7. Validate claim predicates, subject binding, scope, temporal applicability and provenance.
8. Evaluate family-specific identifier matching; preserve raw values and normalized values.
9. Evaluate known clone/fork/split/reset/migration relationships and conflicts.
10. Produce per-dimension assessment results, limitations and unresolved causes.
11. Bind the result to exact scope signature, policy/profile versions, evidence snapshot and dependency snapshot.
12. Emit deterministic diagnostics and replay manifest.

If any required authority, namespace, subject, profile, evidence, scope or lineage dependency is ambiguous or unavailable, resolution MUST abstain. A deterministic abstention is a valid result; it is not a successful identity proof.

## 12. Adversarial conformance scenarios

These are design scenarios, not executable tests. All expected outcomes are conditional on an approved policy/profile.

| ID | Adversarial input or event | Required behavior |
|---|---|---|
| A01-001 | Two authorities issue identical local key | Qualified keys remain distinct |
| A01-002 | Same authority/namespace reuses retired key | Reject registry integrity violation |
| A01-003 | Consumer supplies bare local key | Reject or return unresolved; never guess namespace |
| A01-004 | Authority display name changes | Stable authority ID remains unchanged |
| A01-005 | Authority key revoked | Apply versioned trust/revocation policy; preserve historical issuance |
| A01-006 | Namespace transferred without approval | Reject transfer; retain old authority ownership |
| A01-007 | Federation merges two namespaces | Preserve original qualified keys and federation provenance |
| A01-008 | Same qualified key points to two active subjects | CONFLICTED/INVALID registry integrity; no arbitrary selection |
| A01-009 | Same EVM chain ID, different genesis | Do not merge subjects |
| A01-010 | Same EVM chain ID and genesis, divergent later history | Bootstrap match only; assess branch/lineage separately |
| A01-011 | Different EVM chain IDs, same genesis bytes | Do not infer same network; require profile-specific adjudication |
| A01-012 | EVM endpoint returns chain ID but no genesis evidence | Insufficient identity evidence |
| A01-013 | Provider reports genesis hash but raw header unavailable | Do not claim independently recomputed genesis identity |
| A01-014 | Two endpoints share genesis but disagree on ancestry | CONFLICTED/UNRESOLVED canonical-history assessment |
| A01-015 | Solana endpoints share cluster label but differ in genesis identity | Do not merge |
| A01-016 | Solana endpoints share genesis but report incompatible slot ancestry | Separate endpoint/branch assessments; no automatic identity merge |
| A01-017 | Solana provider reports slot without commitment/ancestry context | Insufficient for purpose requiring canonical history |
| A01-018 | Endpoint URL changes, identifiers and evidence remain consistent | No subject identity change solely due to URL |
| A01-019 | Client software upgrade with continuity evidence | Same subject unless approved profile says otherwise |
| A01-020 | Temporary competing branch later orphaned | Preserve branch history; same subject by default |
| A01-021 | Conflicting finalized histories reported | Quarantine affected purpose/scope; open adjudication |
| A01-022 | Long-lived divergence but no governance/lineage evidence | Do not auto-create permanent split; remain unresolved |
| A01-023 | Two split proposals conflict | Preserve both; no last-write-wins |
| A01-024 | Permanent split approved | Create distinct subjects and explicit lineage; no asset remapping |
| A01-025 | Chain reset reuses chain ID | New subject candidate; never overwrite old subject |
| A01-026 | Migration declares successor but no independent evidence | Migration relationship remains unverified/unresolved |
| A01-027 | Migration maps one old identifier to multiple new identifiers | Preserve cardinality; no assumed one-to-one mapping |
| A01-028 | Registry correction changes genesis claim | New entry version; historical assessments replay unchanged |
| A01-029 | Evidence discovered after historical cutoff | Exclude from AS_KNOWN_THEN result; include only corrected view |
| A01-030 | Same evidence supports genesis fingerprint but not canonicality | Permit only the narrower claim; do not elevate canonicality |
| A01-031 | All evidence sources share one upstream | No false independent corroboration |
| A01-032 | Profile version changes normalization rules | Old assessments retain old profile; re-evaluate under new version |
| A01-033 | Environment is unknown | Do not default to production/mainnet |
| A01-034 | Namespace authority unavailable at replay | Replay is incomplete/abstain, not trusted by cached display label |
| A01-035 | Relationship edge omitted from supplied graph | Do not infer no relationship or no dependency |

## 13. Invariants

- INV-A01-01: Every NetworkKey is authority-qualified.
- INV-A01-02: A local key is unique and never reused within its authority/namespace.
- INV-A01-03: Registry authority identity is immutable; delegation is explicit and time-bounded.
- INV-A01-04: A NetworkSubject ID is opaque, immutable and never derived solely from mutable identifiers.
- INV-A01-05: Endpoint observations do not define NetworkSubject identity.
- INV-A01-06: Chain ID, cluster label or genesis fingerprint alone cannot establish all identity dimensions.
- INV-A01-07: Matching genesis does not imply matching current canonical history.
- INV-A01-08: A temporary reorg does not automatically create a new NetworkSubject.
- INV-A01-09: A permanent split cannot be represented by silent mutation of one subject.
- INV-A01-10: A migration preserves predecessor and successor subjects and records a typed relationship.
- INV-A01-11: Asset/representation identity is not inherited across a network split or migration.
- INV-A01-12: Conflicting lineage claims cannot be resolved by last-write-wins.
- INV-A01-13: Every relationship is typed, directional, scoped, temporal and evidenced.
- INV-A01-14: Historical registry entries and assessments are immutable and replayable.
- INV-A01-15: Unknown namespace, authority, profile or lineage cannot become a favorable identity result.
- INV-A01-16: No status or relationship is inferred from missing graph edges.
- INV-A01-17: Evidence is evaluated against the exact predicate it is claimed to support.
- INV-A01-18: All identity assessments bind exact purpose, scope, policy/profile versions and knowledge cutoff.
- INV-A01-19: A deterministic abstention is distinct from a positive identity resolution.
- INV-A01-20: Profile coverage claims are limited to families and versions explicitly reviewed and approved.

## 14. Cross-document integration requirements

### 14.1 D-09/D-10 identity architecture
- NetworkSubject is the NETWORK identity dimension in D-09.
- EndpointObservation and HistoryBranch are not substitutes for IdentitySubject.
- PurposeAssessment must evaluate the exact purpose and scope; no identity status implies purpose admissibility.
- No automatic inference from network lineage to representation, economic asset, backing or redemption.

### 14.2 Scope Algebra (X-09)
Every identity/lineage assessment MUST bind:
- scope_id/signature and scope_kind;
- subject selector and subject type;
- purpose/activity;
- universe and normalization versions;
- valid-time interval and environment;
- explicit resolution of each relevant dimension;
- dependency graph snapshot and exact evidence set.

No implicit wildcard, scope expansion or empty-set inference is permitted. Partial fork impact requires an explicit derived atomic scope and independent assessment.

### 14.3 X-07 lifecycle and D-07 temporal model
- Preserve valid-time and knowledge-time distinctions.
- Late evidence does not rewrite AS_KNOWN_THEN.
- Invalidation is scope/dependency-aware and append-only.
- Freshness is not identity resolution or record lifecycle.

### 14.4 X-08 governance
The proposed RegistryAuthority, reviewer, adjudicator and emergency roles require a formal role crosswalk to X-08. Since X-08 itself is not institutionally approved and real role owners are not assigned, no authority is granted by this document. No registry entry, split, migration or restoration is approved by repository authorship or PR merge alone.

### 14.5 NCP A02–A05
- A02 supplies evidence authority, source independence and cryptographic trust.
- A03 supplies checkpoint, branch ancestry and finality proof.
- A04 supplies exact scope signatures, dependency closure and replay.
- A05 supplies approved actors, delegations and decision authority.

A01 depends on these contracts; it cannot independently claim production-grade identity without them.

## 15. Open decisions and approval gates

| Decision | Required owner domain | Current state |
|---|---|---|
| Authority root identifier and trust anchor model | Governance/Security | OPEN |
| Registry federation and authority succession | Architecture/Governance | OPEN |
| EVM genesis artifact and header verification profile | Protocol/Security | OPEN |
| Solana genesis/cluster identity proof profile | Protocol/Security | OPEN |
| Fork versus permanent split criteria by family | Protocol/Governance | OPEN |
| Environment taxonomy and controlled vocabulary | Architecture | OPEN |
| Identity scope signature and canonical digest binding | Architecture/X-09 | OPEN |
| Status and event crosswalk to R01/X-07 | Architecture | OPEN |
| Real registry owners and approvers | Governance/X-08 | OPEN |
| Legal, licensing and retention authority | Legal/Compliance | OPEN |

Approval gates:
1. Technical review of qualified key and authority model.
2. Family-specific review by competent protocol experts.
3. Cross-audit with X-09, X-07, X-08 and identity R01 catalogs.
4. Independent adversarial review of the 35 scenarios and invariants.
5. Formal authority and source-policy approval before production population.

## 16. Acceptance criteria for this proposal

- [ ] All registry references use authority-qualified keys.
- [ ] Authority identity, namespace federation, succession and revocation are specified.
- [ ] NetworkSubject, HistoryBranch and EndpointObservation are separate objects.
- [ ] EVM/Solana identifiers are explicitly insufficient on their own for canonical-history identity.
- [ ] Temporary reorg, permanent split, reset, migration and protocol upgrade have distinct semantics.
- [ ] Conflicting lineage adjudications are preserved and cannot use last-write-wins.
- [ ] 35 adversarial scenarios and 20 invariants are documented.
- [ ] Scope Algebra, X-07, X-08 and D-09/D-10 integration requirements are explicit.
- [ ] Open decisions and authority gates are visible.
- [ ] No production implementation or real-network verification is claimed.

## 17. Final status

**DRAFT / REVIEW_REQUIRED / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED.**

This document addresses the design gaps represented by NCP-AR-01..03 but does not by itself close them. Closure requires cross-document consistency review, family-specific expert review, governance authority approval and subsequent executable conformance tests.

No canonical IIM requirement, schema, resolver code, fixture, test or live network registry was changed or created. No real network identity has been verified by this proposal.
