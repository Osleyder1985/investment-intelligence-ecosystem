# A02 — Source independence, evidence provenance and cryptographic trust contract

**Document ID:** IIE-EVIDENCE-TRUST-A02-001  
**Version:** 0.1.0-draft  
**Date:** 2026-10-31  
**Status:** PROPOSED / REVIEW_REQUIRED / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED  
**Related work:** Issue #71; PR #72; A01 network identity and lineage (PR #73); identity decisions D-05/D-06; X-07 lifecycle; X-08 governance; X-09 Scope Algebra.  
**Scope:** Source identity, source dependency/failure domains, evidence provenance and integrity, cryptographic trust, key lifecycle, evidence admissibility/sufficiency, contradiction handling, replay and permission boundaries.

## 1. Purpose and normative boundary

This proposal defines a disciplined model for deciding what a source is, how sources depend on one another, whether an evidence artifact is authentic and intact, whether that artifact is admissible and sufficient for a specific claim and purpose, and how trust changes affect historical and current assessments.

It is a design proposal, not a trust anchor, source registry, legal opinion, security certification or production implementation. Normative terms MUST, MUST NOT, SHOULD, SHOULD NOT and MAY express proposed requirements only.

This contract does not:
- declare any source, provider, authority, publisher, validator, auditor or key trusted;
- establish factual correctness merely from signatures, digests, HTTPS, reputation or source count;
- grant access, collection, storage, retention, transformation, redistribution or commercial-use rights;
- define universal source counts, quorum, confidence thresholds, TTLs or cryptographic algorithms;
- replace protocol-family verification, legal review, security review or X-08 authority approval;
- modify canonical IIM requirements, schemas, resolver code, fixtures or executable tests;
- authorize live RPC collection, production ingestion or investment decisions.

A cryptographic digest establishes byte-level integrity relative to a trusted digest and algorithm. A signature can authenticate a statement under a key and verification profile. Neither alone proves that the statement is true, the signer is competent, the source is independent, the evidence is complete, or the intended use is permitted.

## 2. Foundational separation of objects and assessments

The system MUST separate the following objects:

| Object | Question answered | It does not establish |
|---|---|---|
| SourcePrincipal | Who is the publisher/operator/producer? | Independence, competence, truth or permission |
| SourceEndpoint | Through which endpoint, product or channel was material obtained? | A distinct source principal |
| SourceLineageRecord | What ownership, syndication, upstream, infrastructure or methodological dependencies exist? | Independence unless assessed for a specific failure mode |
| EvidenceArtifact | What exact bytes, observation or record were preserved? | Authenticity or factual correctness |
| CaptureRecord | How, when and by which collector was the artifact acquired? | Truth of the source statement |
| IntegrityAssessment | Are bytes and transformations consistent with pinned digests and custody rules? | Source identity or claim truth |
| SignatureVerification | Does a signature verify under a pinned key/profile/time context? | Signer authorization or factual correctness |
| KeyTrustAssessment | Was the key valid and authorized for this statement, scope and time? | Truth or source independence |
| SourceAssessment | Is this source competent/authoritative for this claim type and use? | The claim itself |
| IndependenceAssessment | Are sources sufficiently independent for this failure model and purpose? | General independence for all claims |
| ClaimEvidenceAssessment | What does this artifact contribute to this exact claim? | Final claim resolution |
| EvidenceSufficiencyAssessment | Does the admissible evidence meet the claim-specific proof standard? | Universal confidence or truth |
| ClaimResolution | What conclusion follows under the PredicateContract? | Operational or investment authorization |
| PermissionAssessment | Is this processing/use legally and contractually permitted? | Probative value |

These are separately versioned assessments. No single global TRUSTED, VERIFIED, HIGH_CONFIDENCE or SOURCE_SCORE flag may replace them.

## 3. Source identity and source lineage

### 3.1 SourcePrincipal

A SourcePrincipal represents an accountable publisher, operator, institution, organization, protocol actor, data producer or identifiable individual where legally appropriate. Proposed attributes:
- immutable source_principal_id;
- principal type and legal/organizational identity references where verified;
- operating organization, parent/subsidiary and beneficial ownership claims where available;
- jurisdiction and authority/competence claims;
- source identity verification evidence;
- valid-time and knowledge-time intervals;
- lifecycle status and change events;
- declared conflicts, incentives and funding relationships;
- responsible role/contact references subject to privacy and access controls.

A display name, domain, TLS certificate, API key or endpoint URL MUST NOT be treated as a complete SourcePrincipal identity.

### 3.2 SourceEndpoint

SourceEndpoint identifies a concrete interface or distribution channel, such as:
- API endpoint, RPC endpoint, explorer, website, data product or downloadable dataset;
- signed release channel or repository;
- institutional publication channel;
- validator/node endpoint or protocol peer;
- archive, mirror, reseller or syndication feed.

An endpoint is associated with one or more principals over time, but endpoint ownership/operator changes MUST be represented as time-bounded events. Multiple endpoints under one principal are not independent principals.

### 3.3 Source lineage graph

The system MUST model source dependency as a typed, temporal graph. Candidate edge types include:
- OWNED_BY / CONTROLLED_BY;
- OPERATED_BY;
- PUBLISHED_BY;
- RESELLS / SYNDICATES;
- MIRRORS / REPLICATES;
- DERIVED_FROM;
- UPSTREAMS_FROM;
- HOSTED_ON;
- RUNS_CLIENT;
- USES_DATA_FEED;
- SHARES_VALIDATOR_SET;
- SHARES_SIGNING_AUTHORITY;
- SHARES_COLLECTION_AGENT;
- SHARES_METHODOLOGY;
- FUNDED_BY / SPONSORED_BY;
- CONFLICT_OF_INTEREST_WITH.

Each edge MUST identify direction, valid-time, recorded-time, evidence references, confidence/epistemic state, scope and provenance. Unknown edges are not evidence of independence.

Lineage is not a single transitive equivalence relation. For example, two sources may share cloud hosting but not data origin; they may share an upstream feed but use independent verification; or share ownership while having independent technical observations. Independence must therefore be assessed against explicit failure modes rather than inferred from one graph path.

### 3.4 Failure-domain model

An IndependenceAssessment MUST declare the failure model relevant to the claim. Candidate dimensions:
1. common ownership/control;
2. common operational administration;
3. common cloud/hosting/network infrastructure;
4. common upstream data provider or archive;
5. common protocol client/software implementation;
6. common validator set, sequencer or consensus trust domain;
7. common signing key, certificate authority or key-management system;
8. common collection agent, parser or normalization pipeline;
9. common methodology, model or analytical assumptions;
10. common funding, incentive or conflict-of-interest exposure;
11. common jurisdictional/legal dependency;
12. common time source or timestamp authority.

For each pair/group of sources, the assessment records:
- failure_domain_id and dimension;
- known shared dependencies and their evidence;
- independence status per dimension;
- coverage and unknowns;
- whether the shared dependency can cause correlated error for the exact claim;
- policy treatment and rationale;
- evaluator, version, scope and cutoff.

Do not reduce this to a scalar count. A claim-specific policy may require independent administrative control, independent data origin, independent protocol implementation, or a combination. It may also accept one authoritative primary source where the PredicateContract explicitly permits that evidence class.

### 3.5 Independence outcomes

Proposed per-dimension outcomes:
- INDEPENDENCE_SUPPORTED;
- DEPENDENCY_CONFIRMED;
- DEPENDENCY_SUSPECTED;
- INDEPENDENCE_UNKNOWN;
- NOT_ASSESSED;
- INVALID (contract error only).

An aggregate IndependenceAssessment MUST preserve the vector of dimensions. It may conclude SUFFICIENTLY_INDEPENDENT_FOR_PURPOSE only if the policy's required dimensions are supported and mandatory unknowns are absent. This is a purpose/scope-specific decision, not a universal source property.

## 4. Evidence artifact and provenance envelope

### 4.1 EvidenceArtifact

An EvidenceArtifact is immutable content or an immutable reference to content under controlled retention. Required conceptual fields:
- evidence_id and evidence_version;
- artifact_type and media/content format;
- exact byte length and cryptographic digest algorithm/value;
- raw content reference or governed storage locator;
- source endpoint and source principal references;
- source-declared publication/observation identifiers;
- capture record references;
- content encoding and canonicalization profile where applicable;
- access classification and permission constraints;
- valid-time (if the artifact asserts a fact about a period);
- published_at, observed_at, captured_at, received_at, ingested_at, recorded_at;
- provenance parent references for transformed/derived artifacts;
- integrity/lifecycle events and retention policy reference.

Unknown timestamps MUST remain unknown; they must not be synthesized from ingestion time. Precision and timezone semantics must be preserved.

### 4.2 Raw and derived evidence

Raw evidence MUST be preserved separately from normalized/derived evidence whenever retention and law permit. A derived artifact MUST identify:
- parent evidence IDs/versions/digests;
- transformation method, code and version;
- parameters and configuration digest;
- execution environment where material;
- output digest;
- known loss, filtering, redaction and limitations.

A derived artifact is not a new independent source. A parser output is not independent corroboration of the raw document it parsed.

If raw evidence cannot lawfully be retained, the system may preserve only approved metadata/digests/locators, but must mark replay limitations and must not claim byte-level re-verification where the bytes are unavailable.

### 4.3 Capture and chain of custody

CaptureRecord SHOULD include:
- collector principal and software identity/version;
- acquisition method and endpoint;
- request/query parameters, headers or protocol context, with secrets removed;
- capture start/end and local clock provenance;
- response status and transport metadata;
- raw payload digest and byte count;
- collector environment/configuration digest;
- operator or automated job identity;
- error, retry and truncation indicators;
- signed capture manifest where approved.

Chain-of-custody events are append-only. Any custody gap, mutation, truncation or unknown transformation must be disclosed and assessed. A collector signature authenticates the collector's capture statement, not the truth of source content.

## 5. Cryptographic integrity and authenticity

### 5.1 Digest contract

Every digest MUST identify:
- algorithm identifier and version/profile;
- exact input bytes and byte length;
- canonicalization profile, if any;
- encoding of the digest value;
- domain-separation context for structured objects;
- implementation/verifier version where material.

Do not hash an ambiguous textual representation where canonical bytes are required. Do not silently change algorithms or canonicalization. Algorithm deprecation creates a new assessment and migration event; it does not rewrite historical digests.

Digest equality does not establish authenticity, authority, freshness, completeness or truth.

### 5.2 Signature envelope

A SignatureEnvelope SHOULD include:
- signed object type and immutable object ID/version;
- exact payload digest and canonicalization profile;
- signature algorithm and parameters;
- key ID and key version;
- certificate/credential chain references where applicable;
- signing time claim and trusted timestamp evidence if available;
- signature bytes and encoding;
- verifier implementation/version and result;
- trust snapshot and revocation knowledge cutoff;
- signing purpose/context and authorized scope.

The signed payload MUST bind object type, object identity/version and purpose/context to prevent cross-protocol or cross-purpose substitution. Signatures over ambiguous or mutable references are insufficient.

### 5.3 Signature verification versus key trust

Evaluate separately:
1. cryptographic signature validity;
2. key identity and key-material continuity;
3. certificate/credential chain validity, if used;
4. key authorization for the signer/principal;
5. authorization scope (claim type, object class, namespace, purpose, jurisdiction);
6. key validity interval at signing/effective time;
7. revocation/compromise status under knowledge cutoff;
8. trust-anchor validity and provenance;
9. algorithm acceptability at the relevant time;
10. timestamp reliability and signing-time uncertainty.

A valid signature from an unauthorized key is AUTHENTICATED_BY_KEY but NOT_AUTHORIZED_FOR_PURPOSE. A trusted source principal does not make every statement from that principal authoritative.

### 5.4 Trust bootstrap and anti-rollback

Trust anchors MUST be supplied by an independently governed trust configuration, not self-asserted by the evidence or registry under evaluation. The trust configuration MUST be authenticated, versioned, sequenced, approved, effective-dated and replay-pinned.

Consumers MUST reject:
- unknown trust configuration versions where policy requires a known sequence;
- stale snapshots beyond the approved freshness policy;
- rollback to an older sequence absent explicit recovery authorization;
- self-signed additions without independent trust administration;
- trust snapshots with invalid signatures, missing approval evidence or unresolved provenance.

The initial out-of-band enrollment, root-of-trust governance, distribution channels, quorum/dual-control policy and recovery procedure remain open institutional decisions. No specific technology or threshold is mandated by this proposal.

### 5.5 Key lifecycle

Key lifecycle MUST be modeled as events, not one mutable status:
- KEY_REGISTERED;
- KEY_ACTIVATED;
- KEY_ROTATED;
- KEY_SUSPENDED;
- KEY_REVOKED;
- KEY_COMPROMISE_REPORTED;
- KEY_COMPROMISE_CONFIRMED;
- KEY_REINSTATED (only under explicit policy);
- KEY_RETIRED;
- TRUST_ANCHOR_CHANGED.

Each event records key identity, authority, scope, effective time, recorded/knowledge time, evidence, issuer, approver, predecessor/successor references and affected records.

Key compromise is distinct from routine rotation or planned expiry. A compromise event must state whether the compromise interval is known, bounded or unknown. If unknown, affected signatures cannot be automatically accepted or rejected; affected assessments enter review/unknown under purpose policy.

### 5.6 Revocation and historical signatures

Revocation evaluation is time- and knowledge-dependent:
- valid-time: when the key/mandate was actually invalid or compromised;
- knowledge-time: when the system learned or recorded the revocation;
- evaluation cutoff: which knowledge was available to the assessment.

Historical replay AS_KNOWN_THEN uses the trust/revocation snapshot available at the original cutoff. A corrected AS_KNOWN_NOW evaluation creates a new assessment and preserves the original.

Policies MUST distinguish:
- planned expiry/retirement (normally prospective);
- mandate/delegation revocation;
- key compromise with known start time;
- key compromise with uncertain start time;
- root authority compromise;
- erroneous revocation;
- algorithm/key-size deprecation;
- signer identity dispute.

No universal retroactive invalidation rule is imposed. The impact must be determined by an approved threat model, evidence and authority.

## 6. Source authority, competence and claim-specific admissibility

A SourceAssessment is contextual to:
`(source_principal, source_endpoint, claim_type, purpose, scope, valid_time, knowledge_cutoff, policy_version)`.

Evaluate distinct dimensions:
- identity verification;
- legal/organizational authority;
- technical competence;
- directness/proximity to the fact;
- methodological transparency and reproducibility;
- source lineage and independence;
- incentives/conflicts;
- integrity/security;
- coverage/completeness;
- freshness for the purpose;
- correction/retraction history;
- permission to process/use.

A source may be authoritative for one fact and not another. For example, a protocol's official release channel may be authoritative for a software release publication, but not independent proof that deployed nodes run that release.

Proposed authority classes are descriptive only and must be governed by claim-specific policies:
- PRIMARY_PROTOCOL_ARTIFACT;
- OFFICIAL_AUTHORITY_STATEMENT;
- INDEPENDENT_TECHNICAL_OBSERVATION;
- INDEPENDENT_AUDIT_OR_ATTESTATION;
- MARKET_DATA_PROVIDER;
- SECONDARY_ANALYSIS;
- USER_SUPPLIED;
- DERIVED_OR_MIRRORED;
- UNKNOWN_OR_UNVERIFIED.

No class is automatically sufficient. The PredicateContract determines acceptable evidence classes, scope and corroboration.

## 7. ClaimEvidenceAssessment and evidence sufficiency

### 7.1 Atomic claim alignment

Evidence is evaluated against an atomic claim, not a broad narrative. Each ClaimEvidenceAssessment binds:
- claim ID/version and exact predicate;
- evidence ID/version and digest;
- source and lineage assessment versions;
- purpose, scope and activity;
- valid-time and knowledge cutoff;
- relationship: SUPPORTS, REFUTES, NEUTRAL, INCONCLUSIVE;
- relevance, directness and coverage;
- admissibility and limitation reasons;
- contradictions and dependency references;
- evaluator/method/policy version and result digest.

A digest match or valid signature is not itself SUPPORTS or REFUTES. It only informs integrity/authenticity dimensions.

### 7.2 Admissibility, sufficiency and resolution are separate

Proposed independent axes:
- EvidenceAdmissibility: ADMISSIBLE, ADMISSIBLE_WITH_LIMITATIONS, QUARANTINED, REJECTED, NOT_ASSESSED;
- EvidenceSufficiency: SUFFICIENT, INSUFFICIENT, PARTIAL, CONFLICTED, NOT_ASSESSED;
- EvidenceFreshness: FRESH, APPROACHING_EXPIRY, STALE, NOT_APPLICABLE;
- ContractValidity: VALID, INVALID;
- ClaimResolution: CONFIRMED, REFUTED, UNKNOWN, CONFLICTED, NOT_ASSESSED, RETRACTED.

These labels are proposed and must be reconciled with approved canonical catalogs before implementation. INVALID is a contract error, not a factual or evidentiary status. STALE is freshness, not rejection. REJECTED evidence may still be retained in audit metadata subject to permission policy but contributes no admissible support/refutation.

### 7.3 Claim-specific sufficiency policy

A SufficiencyPolicy MUST identify:
- claim type and PredicateContract version;
- mandatory conditions and evidence classes;
- authority requirements;
- directness/coverage criteria;
- independence dimensions and failure model;
- treatment of primary sources and independent replication;
- contradictory evidence/adjudication requirements;
- temporal and freshness constraints;
- closed-world eligibility, if any;
- minimum proof artifacts and reviewer authority;
- abstention conditions and reason codes.

No universal source count or weighted score may override a mandatory condition. A single authoritative primary artifact may suffice only if the policy explicitly permits it and its authenticity, authority, scope and integrity are established. Several dependent sources do not become independent by count.

### 7.4 Contradictions

Preserve all material evidence and conflicting claims. Do not use majority vote, newest timestamp, highest reputation score or first-seen order unless an approved claim-specific policy explicitly justifies that rule and its assumptions.

Adjudication must identify:
- exact propositions in conflict;
- admissible evidence supporting each side;
- source lineage and failure domains;
- temporal/scope differences;
- authority and competence;
- adjudicator role and conflict-of-interest assessment;
- decision rationale, dissent and appeal/review path.

If conflict cannot be resolved, return CONFLICTED or UNKNOWN according to the canonical claim contract. Never force a favorable binary result.

## 8. Permission, licensing, confidentiality and retention

PermissionAssessment MUST be distinct from probative admissibility. Evaluate:
- access and automated retrieval;
- collection and processing;
- storage and caching;
- retention duration and deletion;
- transformation and derived-data rights;
- display and redistribution;
- commercial use;
- jurisdiction, data residency and cross-border transfer;
- confidentiality, trade secrets and personal data;
- contractual/API terms and rate limits.

Public reachability is not a blanket license. Unknown permission is not permission. If a use is prohibited or unresolved, block that use and preserve only legally permitted metadata, hashes or locators. Do not copy restricted evidence into fixtures or test data without an approved basis.

Permission changes create new assessments/events. They do not rewrite what was permitted or known at an earlier cutoff.

## 9. Time, lifecycle and invalidation

All objects and assessments MUST distinguish, where applicable:
- valid_time;
- published_at;
- observed_at;
- captured_at;
- received_at;
- recorded_at;
- evaluated_at;
- effective_at;
- knowledge_cutoff.

Late evidence, correction, withdrawal, source ownership change, key compromise, revocation, license change or lineage discovery creates a new event and dependent reassessment. Historical evidence and assessments remain immutable.

Invalidation MUST follow explicit dependency edges and exact scope. A source compromise does not automatically invalidate every claim ever touched by that source; determine affected key/time/scope/claim dependencies. Conversely, incomplete dependency closure MUST block a claim of complete impact analysis.

Freshness is purpose-, claim-, source-, scope- and cutoff-dependent. No global TTL is set.

## 10. Deterministic replay and proof manifest

A replay manifest MUST pin:
- source principal/endpoint/lineage graph versions;
- evidence IDs, versions, digests and raw/derived relationships;
- capture and chain-of-custody records;
- signature envelopes, key versions, trust snapshot and revocation cutoff;
- verifier, parser, canonicalization and digest algorithm versions;
- SourceAssessment and IndependenceAssessment versions;
- PredicateContract and SufficiencyPolicy versions;
- ClaimEvidenceAssessments and ClaimResolution versions;
- PermissionAssessment and retention policy versions;
- scope signature, purpose/activity and dependency graph snapshot;
- valid-time/knowledge-time cutoffs;
- decision/adjudication policy and engine version.

The verifier must be deterministic over these pinned inputs. If required content or a trust dependency is unavailable, replay is incomplete; the system must not fabricate a favorable result. A proof digest is not a substitute for the underlying evidence and verification procedure.

## 11. Security threat model and controls

Threats include:
- fake or impersonated source principals;
- domain, DNS, BGP, TLS or endpoint compromise;
- provider collusion and hidden common upstreams;
- copied/mirrored sources miscounted as independent;
- signing-key theft, substitution, downgrade or replay;
- trust-root poisoning and snapshot rollback;
- malicious or compromised collector/parser;
- content truncation, mutation, canonicalization differential and hash confusion;
- forged timestamps and delayed revocation;
- evidence substitution across claim, subject, scope or purpose;
- dependency graph omission;
- unauthorized access, retention or redistribution;
- source withdrawal or licensing changes;
- malicious registry/event ordering and concurrent updates.

Minimum proposed controls:
1. authenticated, versioned trust snapshots with anti-rollback;
2. immutable evidence identifiers and digest verification;
3. signed capture manifests where justified;
4. strict typed parsing and canonicalization;
5. key separation by purpose and least privilege;
6. key rotation/revocation/compromise event handling;
7. source lineage and failure-domain assessments;
8. independent review of high-impact trust changes;
9. append-only audit and replay;
10. access control, encryption and secrets minimization;
11. dependency closure verification and scope-aware invalidation;
12. incident response, quarantine and recovery workflows;
13. adversarial and differential test suites;
14. no production trust based on repository authorship or document status.

## 12. Governance and segregation of duties

The following functions MUST be separately assignable, with approved role IDs and delegation:
- register SourcePrincipal/SourceEndpoint;
- submit evidence and capture manifests;
- verify cryptographic integrity/signatures;
- assess source authority/competence;
- assess source lineage/independence;
- approve trust anchors and trust snapshot;
- issue/revoke keys and mandates;
- adjudicate evidence conflicts;
- approve claim-specific sufficiency policies;
- approve permission/licensing basis;
- authorize emergency quarantine/recovery;
- audit/review decisions and incidents.

No individual or service may unilaterally create a source, assert its independence, approve its trust anchor, adjudicate its own evidence and release it for high-impact use. Emergency break-glass authority must be bounded, time-limited, independently reviewed and fully audited.

X-08 remains draft/unapproved. This document assigns no real person, institution or role.

## 13. Adversarial scenarios

The following are proposed acceptance scenarios, not executable tests.

| ID | Adversarial case | Required outcome |
|---|---|---|
| A02-001 | Ten URLs mirror one primary feed | One source lineage; no ten-source corroboration |
| A02-002 | Two providers share a hidden upstream | Independence unknown/dependent until resolved |
| A02-003 | Two endpoints share owner but separate infrastructure | Ownership dependency retained; purpose-specific assessment |
| A02-004 | Two providers use same client/parser | Shared technical failure domain recorded |
| A02-005 | Source has valid TLS but false organizational identity | Endpoint transport does not prove principal identity |
| A02-006 | Valid signature from unauthorized key | Signature valid; authorization fails |
| A02-007 | Valid signature under revoked key, compromise time unknown | Affected trust unresolved/review; no silent pass |
| A02-008 | Registry self-adds its own root key | Reject trust bootstrap |
| A02-009 | Consumer receives older trust snapshot | Anti-rollback/staleness gate blocks acceptance |
| A02-010 | Digest matches but source authenticity unknown | Integrity only; no authenticity inference |
| A02-011 | Collector signs a truncated response | Capture integrity/coverage failure; quarantine |
| A02-012 | Parser output differs across versions | Preserve both derivations; no silent replacement |
| A02-013 | Source republishes another source's report | Dependency edge; not independent evidence |
| A02-014 | Official source is authoritative but stale | Authority retained; freshness evaluated separately |
| A02-015 | Independent sources contradict | CONFLICTED/UNKNOWN pending claim-specific adjudication |
| A02-016 | Three dependent sources agree against one independent artifact | No majority vote; apply failure model and authority policy |
| A02-017 | Evidence is probative but license prohibits storage | Block storage/use; do not copy into fixture |
| A02-018 | Public API terms prohibit commercial redistribution | Permission assessment blocks that use |
| A02-019 | Evidence valid for historical claim but stale for live monitoring | Purpose-specific freshness outcomes |
| A02-020 | Key rotated after signature | Verify under historical key/trust snapshot and applicable time policy |
| A02-021 | Root key compromise reported retroactively | Reassess affected trust interval; preserve AS_KNOWN_THEN |
| A02-022 | Source ownership changes | New lineage event and purpose-specific reassessment |
| A02-023 | Source lineage unknown | Do not treat as independent |
| A02-024 | Same evidence digest referenced under another claim/scope | Reject substitution unless exact binding is valid |
| A02-025 | Evidence reference missing during replay | Replay incomplete; no favorable claim |
| A02-026 | Two assessors use different trust snapshots | Results not treated as equivalent; record versions/cutoffs |
| A02-027 | Revocation event arrives late | New knowledge-time assessment; historical record immutable |
| A02-028 | Evidence source withdraws publication | Preserve permitted historical artifact and withdrawal event; reassess current use |
| A02-029 | One operator controls multiple nominal providers | Common administrative failure domain |
| A02-030 | One source is primary for software release but not deployed state | Claim-specific authority prevents overreach |
| A02-031 | Unknown license status | Use blocked pending permission determination |
| A02-032 | Source timestamp is forged or lacks trusted time | Preserve as source assertion; do not treat as trusted signing time |
| A02-033 | Hash algorithm is deprecated | New profile/assessment; preserve old digest and algorithm |
| A02-034 | Conflicting key identity claims | KEY_TRUST unresolved/conflicted; no favorable authorization |
| A02-035 | Dependency graph has unresolved edges | Independence/impact completeness not established |
| A02-036 | Evidence is a derived model output | Preserve parent data/model/version; not primary corroboration |
| A02-037 | Emergency quarantine is self-approved by evidence producer | Reject SoD; require independent authorized approval |
| A02-038 | A source is independent technically but shares financial incentives | Preserve dimensions; claim policy determines sufficiency |
| A02-039 | Same key signs different object types without domain separation | Reject signature context or mark invalid |
| A02-040 | Trust snapshot has valid signature but missing approval evidence | Authentication does not establish authorization |
| A02-041 | Evidence is admissible but neutral to claim | No support/refutation contribution |
| A02-042 | Evidence rejected but appears to refute claim | Rejected content contributes no logical refutation |
| A02-043 | Closed-world claim lacks complete universe manifest | UNKNOWN, not FALSE |
| A02-044 | Affected dependency closure cannot be proven after key compromise | Impact assessment incomplete; maintain quarantine/review |
| A02-045 | Two independent implementations replay same manifest | Identical deterministic output required for conformance |

## 14. Proposed invariants

- INV-A02-01: SourceEndpoint is never equated with SourcePrincipal.
- INV-A02-02: Source count is never used as a substitute for independence.
- INV-A02-03: Unknown dependency is never interpreted as independence.
- INV-A02-04: EvidenceArtifact is immutable; corrections create new versions/events.
- INV-A02-05: Derived evidence retains complete parent lineage.
- INV-A02-06: Digest validity does not imply authenticity or truth.
- INV-A02-07: Signature validity does not imply signer authorization.
- INV-A02-08: Signer authorization is scope-, purpose- and time-specific.
- INV-A02-09: Trust bootstrap cannot be self-authorized by the registry/source being assessed.
- INV-A02-10: Trust snapshots are versioned, authenticated and replay-pinned.
- INV-A02-11: Revocation and compromise preserve valid-time and knowledge-time.
- INV-A02-12: Unknown compromise interval cannot silently yield favorable trust.
- INV-A02-13: Evidence admissibility, sufficiency, freshness and claim resolution remain separate.
- INV-A02-14: Rejected evidence contributes no admissible support or refutation.
- INV-A02-15: Neutral evidence does not resolve a claim.
- INV-A02-16: Contradictory admissible evidence is not majority-voted absent explicit policy.
- INV-A02-17: Independence is assessed against an explicit failure model.
- INV-A02-18: Public accessibility does not imply permission for all processing/use.
- INV-A02-19: Permission failure blocks the prohibited use irrespective of probative value.
- INV-A02-20: Replay pins all trust, evidence, source, policy and verifier dependencies.
- INV-A02-21: Missing replay dependencies cannot produce a favorable conclusion.
- INV-A02-22: Historical assessments are not rewritten by later knowledge.
- INV-A02-23: Invalidation follows explicit dependency edges and exact scope.
- INV-A02-24: Unknown scope/dependency is not treated as NOT_AFFECTED.
- INV-A02-25: High-impact trust changes require independent approval and SoD.
- INV-A02-26: Emergency actions are bounded, auditable and independently reviewed.
- INV-A02-27: Source authority is claim-specific, not a universal reputation score.
- INV-A02-28: A single source can suffice only where the claim policy explicitly allows it.
- INV-A02-29: A source signature authenticates only the statement/context actually signed.
- INV-A02-30: Canonicalization and cryptographic profiles are explicit and versioned.
- INV-A02-31: A signature cannot be replayed across object type/purpose without valid domain binding.
- INV-A02-32: Trust/permission/lineage changes create new assessments and lifecycle events.

## 15. Cross-document integration

| Related contract | Required integration |
|---|---|
| A01 network identity/lineage | Authority trust, source independence, key lifecycle, registry claims and fork adjudication evidence |
| NCP parent contract | RegistryEvidenceAssessment, checkpoint source independence, cryptographic authenticity and provenance |
| X-07 lifecycle | valid-time/knowledge-time, append-only events, freshness and invalidation |
| X-08 governance | trust-anchor administration, source approval, key authority, conflict adjudication, emergency action and SoD |
| X-09 Scope Algebra | exact subject/purpose/activity/scope, atomic partition and proof/dependency binding |
| Identity D-05/D-06 | SourceRecord/EvidenceRecord/SourceAssessment/ClaimEvidenceAssessment and PredicateContract adapter |
| R01 catalogs | evidence admissibility, sufficiency, freshness, lifecycle, contract validity and claim resolution mapping |
| Security/compliance EPIC-09 | threat model, permission, retention, incident response and external review |

No new canonical state vocabulary is approved by this document. All proposed states require reconciliation with the approved catalog authority and R01 decisions before implementation.

## 16. Approval gates and open decisions

Before approval:
1. Architecture review of object boundaries and producer/consumer contracts.
2. Security review of trust bootstrap, signing profiles, key lifecycle and anti-rollback.
3. Legal/privacy/licensing review of source acquisition, storage, retention and use.
4. Governance approval of real authority roles, trust-root control, delegation and SoD.
5. Domain-specific claim policies and failure models for network identity, reserves/backing, governance, security, tokenomics and market data.
6. Mathematical review of evidence sufficiency and PredicateContract composition.
7. Cross-audit with A01, X-07, X-08, X-09 and R01 catalogs.
8. Adversarial scenario mapping to proof obligations and executable test classes.
9. Separate authorization for production schemas, implementation, data connectors and live collection.

Open decisions include actual trust-anchor governance, cryptographic profiles, acceptable source classes, failure-domain evidence acquisition, licensing policies, retention periods, key compromise adjudication, and claim-specific sufficiency standards.

## 17. Final status

**PROPOSED / REVIEW_REQUIRED / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED.**

This document defines a candidate A02 contract with 45 adversarial scenarios and 32 invariants. It does not establish that any source is independent, any key is trusted, any evidence is authentic or legally usable, or any claim is true. No production schema, code, fixture, test, source registry or live data connector was created or changed. No tests, external legal/security reviews or empirical validation were performed.
