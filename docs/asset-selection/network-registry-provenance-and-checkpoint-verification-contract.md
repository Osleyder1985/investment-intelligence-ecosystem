# Network identity registry, provenance and checkpoint verification contract

**ID:** IIE-IDENTITY-NETWORK-EVIDENCE-001  
**Version:** 0.1.0-draft  
**Date:** 2026-10-31  
**Status:** PROPOSED / REVIEW_REQUIRED / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED  
**Issue:** #71  
**Scope:** Network identity registry, registry evidence, checkpoint evidence, finality assessment, deterministic offline verification and governance.

## 1. Purpose and normative boundary

This document proposes a production-oriented contract for establishing network identity and evaluating checkpoint evidence. It is a design artifact, not a production trust anchor.

The words **MUST**, **MUST NOT**, **SHOULD**, **SHOULD NOT** and **MAY** express proposed contract requirements. They do not imply institutional approval or implementation.

This contract does not:
- populate a registry with real networks or declare any network verified;
- connect to RPC providers, explorers, indexers or validators;
- prove consensus correctness merely from provider responses or signatures;
- establish asset identity, token legitimacy, backing, solvency, market eligibility or investment suitability;
- approve data-source licensing, retention or jurisdictional use;
- modify canonical IIM requirements, schemas, resolver code or live operational policy.

The current EVM/Solana resolver remains a synthetic offline slice. Its checkpoint-presence gate is not checkpoint authentication or finality verification.

## 2. Core separation of assertions

The system MUST represent these as independent assessments:

1. **NetworkIdentityAssessment (NIA):** whether the observed network identifiers resolve to one versioned registry subject.
2. **RegistryEvidenceAssessment (REA):** whether the claims supporting the registry entry are admissible, sufficiently corroborated, current and contract-valid for a stated purpose.
3. **CheckpointEvidenceAssessment (CEA):** whether a checkpoint observation is correctly bound to the network, height/slot, hash/state root, source and observation interval.
4. **FinalityAssessment (FA):** whether the checkpoint satisfies a family- and network-specific finality policy at a stated evaluation time.
5. **PurposeAdmissibilityAssessment (PAA):** whether the resulting information may be used for a declared purpose/scope under the applicable policy.

A successful NIA MUST NOT imply successful REA, CEA, FA or PAA. A checkpoint can be structurally valid but not final; finality can be unknown while the network identity is resolved. A provider signature authenticates a statement by that provider only; it does not by itself prove the provider's statement is true.

## 3. Typed subjects, identifiers and stable keys

### 3.1 Registry subject

A NetworkRegistryEntry MUST identify one network subject, not merely a display name, ticker, chain ID or provider endpoint. Proposed fields:

- `network_key`: immutable, globally unique, opaque identifier within the registry namespace;
- `network_namespace`: registry authority and namespace identifier;
- `network_family`: versioned protocol-family identifier (for example EVM or Solana);
- `family_profile_id` and `family_profile_version`;
- `identifiers[]`: typed, canonical, family-specific identifiers;
- `lifecycle_status`: PROPOSED, ACTIVE, SUSPENDED, SUPERSEDED, RETIRED, DISPUTED;
- `valid_from`, optional `valid_until`, `recorded_at`;
- `registry_version`, `entry_version`, `change_event_id`;
- `owner_role_id`, `reviewer_role_id`, `approver_role_id`;
- `evidence_claim_refs[]`, `policy_refs[]`, `audit_refs[]`.

The display name, symbol, explorer URL, RPC URL and provider-specific network label are descriptive metadata, never primary identity keys.

### 3.2 Family-specific identity profile

An identifier MUST be typed by namespace, algorithm/encoding and profile version. Canonicalization MUST be deterministic, injective over the accepted canonical domain, and fail closed on malformed or ambiguous input.

**EVM initial profile proposal**
- Chain ID is an unsigned integer represented canonically as a decimal string; leading plus signs, whitespace, exponent notation and non-canonical leading zeros are rejected.
- Genesis identity is a family-defined block-zero hash with an explicit hash algorithm and fixed byte length.
- The network identity tuple is at least (family profile, chain ID, genesis block hash). It is not sufficient to trust chain ID alone.
- Genesis hash comparison MUST use decoded bytes, not case-sensitive display text. Canonical serialization uses lowercase `0x` hexadecimal.
- Chain ID plus genesis hash is a candidate discriminator, not proof against deliberate cloning, copied genesis, or later consensus divergence. Additional lineage/fork evidence may be required by policy.

**Solana initial profile proposal**
- Genesis/cluster identity MUST be represented by a family-defined canonical genesis hash or equivalent independently evidenced identifier, not a human label such as mainnet-beta/devnet/testnet.
- Public keys and program IDs use canonical Base58 decoding to exactly 32 bytes where the relevant field requires a Pubkey.
- A cluster label or RPC endpoint is metadata only. It cannot override a conflicting genesis identity.
- A genesis identity alone may not distinguish all operationally relevant forks or cloned environments; lifecycle and checkpoint lineage evidence remain necessary.

**Other families**
Bitcoin-like UTXO, Cosmos/Tendermint, Cardano/eUTXO, rollups, appchains and other families MUST receive separate versioned profiles. EVM rules MUST NOT be generalized to them.

### 3.3 Uniqueness and collisions

Within one active registry version:
- `network_key` MUST be unique.
- A canonical identity tuple MUST resolve to at most one ACTIVE entry for the same namespace and effective interval.
- A collision MUST produce registry integrity failure or CONFLICTED; the resolver MUST NOT choose the first, newest or most popular entry.
- Reuse of an identifier after a fork, migration or network reset requires explicit lifecycle semantics and temporal validity.
- Registry uniqueness is scoped to a version and effective interval; historical versions remain replayable.

## 4. Network lifecycle, forks and migrations

A network entry is not silently rewritten when a fork, chain split, migration or reset occurs.

Proposed lifecycle events:
- NETWORK_PROPOSED
- NETWORK_ACTIVATED
- NETWORK_SUSPENDED
- FORK_DETECTED
- FORK_ADJUDICATED
- NETWORK_SPLIT
- NETWORK_MIGRATED
- NETWORK_SUPERSEDED
- NETWORK_RETIRED
- NETWORK_DISPUTED
- REGISTRY_ENTRY_CORRECTED

A fork creates distinct network lineage subjects when the identity policy determines they are materially distinct. Each child MUST reference its parent/ancestor and fork point through typed, directional, time-bounded relationships. A migration MUST preserve the old subject and establish an explicit successor relationship; it MUST NOT imply that all old identifiers or assets map one-to-one to the successor.

A registry correction creates a new entry version and event. Historical records and assessments remain immutable and replayable. A disputed entry can be suspended for specified purposes without erasing its historical identity.

## 5. Atomic registry claims and evidence model

### 5.1 Atomic claim

Every material identifier assertion MUST be expressed as an atomic claim with:
- `claim_id`, `claim_type`, `subject_ref`, typed predicate and `claim_schema_version`;
- canonical value and identifier profile;
- scope, purpose, activity and applicable network-family profile;
- `valid_time`, `observed_at`, `recorded_at`;
- source/evidence references and provenance;
- producer identity, policy version and assessment version;
- claim resolution status and independent freshness/lifecycle fields.

Claims MUST be narrow enough that one piece of evidence can support or refute the predicate without silently asserting unrelated facts. Example: “source S reported genesis hash H for endpoint E at time T” is not the same claim as “H is the canonical genesis hash of network N.”

### 5.2 Evidence record

An EvidenceRecord SHOULD include:
- immutable evidence ID and content digest;
- source organization, source class, endpoint/product and source identity;
- capture method, collector/software version and configuration digest;
- raw observation reference and normalized extraction reference;
- observed/received/recorded times and source sequence where available;
- cryptographic signature metadata, certificate/key identifier and verification result, if applicable;
- licensing basis, permitted purposes, jurisdiction, retention/deletion class and access classification;
- source reliability assessment and known conflicts;
- integrity status and chain-of-custody references.

The contract MUST distinguish evidence content from an evaluator's assessment of that evidence. A source signature proves origin/integrity under the relevant key assumptions, not factual correctness, independence or canonicality.

### 5.3 Source authority and diversity

A source policy MUST specify the source's authority for the exact claim type. A block explorer, RPC vendor, validator, protocol documentation page, client implementation, chain registry and governance record do not have interchangeable authority.

Corroboration MUST be assessed by administrative and technical independence, not by counting URLs. Multiple endpoints operated by one provider, mirrored datasets, resellers, shared upstream nodes or common software/configuration may be one failure domain.

No universal “two sources” or “three providers” rule is imposed here. Each claim policy MUST define:
- minimum evidence classes and independence dimensions;
- whether primary protocol artifacts or independent client implementations are required;
- how shared infrastructure and correlated failures are detected;
- treatment of provider disagreement;
- adjudication authority and abstention behavior.

User-supplied labels and a single uncorroborated provider response MUST NOT establish network identity.

## 6. Checkpoint evidence contract

### 6.1 Checkpoint envelope

A CheckpointObservation SHOULD contain:
- `checkpoint_id`, `network_key`, registry and network-entry versions;
- `family_profile_id` and version;
- typed coordinate: block height, slot, epoch/round or family-specific position;
- canonical coordinate value and explicit integer/range constraints;
- checkpoint kind (block header, block hash, state root, commitment, snapshot or other typed object);
- hash algorithm, byte length, canonical encoding and decoded digest bytes;
- parent/previous checkpoint reference where applicable;
- source identity, endpoint/collector, observation reference and capture method;
- `observed_at`, `recorded_at`, source timestamp/sequence if available;
- raw payload digest, normalized envelope digest and signature metadata;
- source's claimed status, stored as an untrusted claim rather than a trusted outcome.

A block hash, state root, slot and height are not interchangeable. A family profile MUST define which checkpoint kinds are valid and how each is encoded and bound.

### 6.2 Binding constraints

The verifier MUST check:
1. the referenced registry entry and profile exist and are version-compatible;
2. the network key and registry entry version are explicit;
3. the coordinate has the correct type, range and canonical representation (a boolean MUST NOT pass as an integer);
4. the hash decodes to the exact required byte length under the declared algorithm;
5. the checkpoint kind is permitted for that family/profile;
6. parent/lineage fields satisfy the profile's structural constraints;
7. source, observation and provenance references are present and contract-valid;
8. the observation's effective time is consistent with the evaluation cutoff and validity policy.

Passing these checks establishes structural binding only. It does not establish that the checkpoint exists on the canonical network or is final.

### 6.3 Independent checkpoint status dimensions

Do not collapse all outcomes into one status. Proposed dimensions:

**Evidence/verification status**
- NOT_ASSESSED
- STRUCTURALLY_VALID
- INVALID
- INSUFFICIENT_EVIDENCE
- CONFLICTED

**Observation availability**
- AVAILABLE
- UNAVAILABLE
- NOT_OBSERVED

**Canonicality/lineage status**
- CANONICALITY_CONFIRMED
- ORPHANED
- FORKED
- UNKNOWN
- NOT_ASSESSED

**Freshness**
- FRESH
- APPROACHING_EXPIRY
- STALE
- NOT_APPLICABLE

**Finality**
- FINALITY_SATISFIED
- CONFIRMATIONS_PENDING
- FINALITY_NOT_SATISFIED
- FINALITY_UNKNOWN
- FINALITY_NOT_ASSESSED

These are separate dimensions. INVALID is a contract/integrity failure, not a factual claim that a block never existed. UNAVAILABLE is not INVALID. STALE is not ORPHANED. UNKNOWN is not FALSE, EMPTY, NOT_APPLICABLE or NOT_ASSESSED.

## 7. Finality, reorgs and disagreement

Finality MUST be evaluated under a versioned family/network policy that defines:
- consensus/finality model and the exact meaning of finality;
- confirmation/finality threshold or cryptographic proof requirements;
- required chain ancestry and parent linkage;
- source and validator diversity assumptions;
- timeouts, observation windows and delayed-finality handling;
- reorg depth/rollback semantics where applicable;
- treatment of halted chains, liveness failures and conflicting finalized claims;
- policy owner, approver, effective interval and evidence basis.

A confirmation count is not universal finality. Probabilistic confirmation policies and deterministic/cryptographic finality proofs MUST be represented differently. Provider-reported “finalized” labels MUST remain claims until independently verified against the approved policy.

**Reorg handling**
- A checkpoint later removed from the canonical chain is marked ORPHANED for the relevant canonicality assessment, with the replacement lineage and event recorded.
- The prior observation is preserved; it is not deleted or rewritten.
- Dependent assessments are invalidated/reviewed only through explicit dependency edges and scope-aware propagation.
- A reorg MUST NOT automatically imply that the network identity itself changed.

**Fork handling**
- Competing branches at the same height/slot produce a conflict or unresolved canonicality assessment until the applicable policy adjudicates them.
- A network split may require separate network subjects and lineage links.
- No arbitrary “latest provider wins” or last-write-wins resolution is permitted.

**Provider disagreement**
- Preserve each provider observation and provenance independently.
- Compare only semantically compatible checkpoint claims.
- Apply source-diversity and adjudication policy; otherwise return CONFLICTED or UNKNOWN and abstain.
- Do not average hashes, majority-vote correlated sources, or treat response volume as proof.

## 8. Deterministic offline verification protocol

The verifier MUST be a pure, deterministic function over explicit, versioned inputs. It MUST NOT make network calls or consult mutable external state during replay.

Proposed input manifest:
- registry snapshot and digest;
- registry evidence envelopes and source-policy versions;
- checkpoint observations and evidence references;
- family profile and canonicalization versions;
- freshness, finality and purpose policies;
- evaluation cutoff and time-zone semantics;
- dependency/scope snapshot;
- verifier implementation and algorithm version.

Proposed processing stages:
1. Validate envelope/schema and reject malformed contracts.
2. Validate registry snapshot integrity, versions, uniqueness and effective intervals.
3. Validate atomic claims, evidence references, provenance and permission metadata.
4. Evaluate source authority, admissibility, sufficiency, freshness and corroboration independently.
5. Resolve network identity without trusting display labels or expected outputs.
6. Validate checkpoint structure and binding.
7. Evaluate lineage/canonicality and finality under explicit policies.
8. Apply scope/purpose admissibility and produce a typed outcome or abstention.
9. Emit deterministic diagnostics, proof manifest and canonical result digest.

The result MUST record all input digests, policy/profile versions, algorithm version, evaluation cutoff, evidence refs, dependency completeness, per-stage status, abstention reasons and replay manifest. The verifier MUST NOT emit a favorable status if a mandatory input is absent, stale, conflicted, unauthorized or unresolved.

A digest binds bytes; it does not prove the truth of the bytes. A replayable proof manifest demonstrates reproducibility of the verifier's computation, not consensus correctness unless the underlying proof system and trust assumptions are separately verified.

## 9. Scope, lifecycle and invalidation integration

All assessments MUST be scoped by the approved Scope Algebra dimensions. At minimum: network subject, representation where relevant, purpose, activity, route if applicable, jurisdiction, time interval, environment and source/data context.

The following integration rules are mandatory proposals:
- An unresolved scope or dependency MUST NOT be treated as universal scope, empty scope or NOT_AFFECTED.
- A fork/checkpoint conflict affects only the network/checkpoint scopes and downstream objects supported by explicit dependency edges.
- A checkpoint becoming stale for a real-time purpose does not make the network identity false or stale for every historical purpose.
- A registry entry superseded for current use remains available for historical replay.
- Evidence withdrawal, source compromise or registry correction creates new lifecycle events; it does not rewrite old evidence or decisions.
- Partial invalidation requires exact atomic partition coverage. Unknown partition membership requires review/abstention.
- Veto, risk and decision composition remain governed by their own domain contracts; this document creates no new veto authority.

Time fields MUST distinguish valid_time, observed_at, recorded_at, evaluated_at and effective_at. Freshness is purpose-, scope-, source- and cutoff-dependent; no universal TTL is set here.

## 10. Permissions, licensing and retention

Evidence collection and storage MUST be blocked unless the intended processing, storage, retention, redistribution and jurisdictional use are explicitly permitted.

Permission assessment MUST distinguish:
- access permission;
- collection/automated retrieval permission;
- storage and caching permission;
- retention duration and deletion obligation;
- transformation/derivative-data permission;
- redistribution/display permission;
- commercial-use permission;
- jurisdictional and contractual restrictions.

A source being publicly reachable does not automatically grant all of these permissions. Permission metadata is versioned evidence and must be reevaluated after terms, law or provider policy changes. If permission is unknown or insufficient, the evidence is not admissible for the prohibited use; it must not be silently retained or copied into a fixture.

## 11. Security and threat model

Threats include registry poisoning, malicious chain-ID reuse, copied genesis identities, endpoint spoofing, DNS/BGP compromise, provider collusion, stale caches, replayed observations, timestamp manipulation, compromised signing keys, hash-algorithm confusion, parser/canonicalization differentials, fork ambiguity, dependency omission, policy rollback, unauthorized registry edits and evidence exfiltration.

Minimum controls proposed:
- authenticated, least-privilege registry write paths and immutable audit events;
- independent review and approval for identity/lifecycle changes;
- signed release artifacts or equivalent tamper-evident registry publication;
- key rotation, revocation, compromise response and historical signature verification policy;
- strict parser limits, canonical encodings and duplicate-key rejection;
- explicit algorithm/profile allowlists and deprecation policy;
- replay protection and idempotency keys for events;
- deterministic serialization and digest binding;
- access controls and data minimization for licensed evidence;
- incident quarantine, rollback by new version, and post-incident review;
- adversarial tests for maliciously crafted registries, conflicting sources and partial evidence.

No single control or signature is treated as a substitute for independent evidence and governance.

## 12. Governance, RACI and segregation of duties

Proposed roles (role IDs to be bound to approved X-08 governance; no persons are assigned here):

| Activity | Producer | Independent reviewer | Approver/authority | Operator |
|---|---|---|---|---|
| Submit network claim/evidence | Registry contributor | Evidence reviewer | Registry authority | — |
| Change family profile | Profile maintainer | Protocol/security reviewer | Architecture authority | — |
| Activate/suspend registry entry | Registry steward | Independent identity reviewer | Designated registry authority | Registry operator |
| Approve source/permission policy | Data owner | Legal/licensing reviewer | Authorized policy owner | Data operator |
| Evaluate checkpoint/finality | Verification service | Independent verifier | Policy owner for exceptions | Monitoring operator |
| Resolve fork/provider conflict | Incident investigator | Independent technical reviewer | Designated adjudicator | Incident operator |
| Emergency suspension | Authorized incident role | Retrospective reviewer | Emergency authority under X-08 | On-call operator |

Segregation rules:
- A producer MUST NOT approve their own material registry change.
- A source operator MUST NOT unilaterally declare its own disputed response canonical.
- An emergency suspension MAY be immediate only under a pre-approved authority and scope; retrospective review and expiry/reassessment are mandatory.
- Lifting a suspension or restoring ACTIVE status requires new evidence and the designated independent authority.
- No role defined here overrides X-08; unresolved role ownership is a blocking governance gap.

## 13. Change control and operational response

Every registry/profile/policy change MUST have a change request, rationale, impact analysis, compatibility assessment, evidence references, reviewer and approver, effective time, version, digest and rollback/supersession plan.

Incidents include:
- suspected registry poisoning or unauthorized edit;
- compromised source/provider/signing key;
- conflicting genesis or checkpoint claims;
- material reorg/fork or finality failure;
- license/retention breach;
- verifier defect or nondeterministic replay.

Incident handling MUST preserve evidence, scope provisional restrictions, identify dependent assessments, notify designated owners, create versioned corrections and require independent closure. Rollback means publishing a new approved version; it does not erase the compromised version or its audit history.

## 14. Adversarial conformance scenarios

These scenarios are specifications, not executable tests. Expected outcomes are intentionally conservative.

| ID | Adversarial input or event | Required outcome |
|---|---|---|
| NCP-001 | Same EVM chain ID, different genesis hash | Distinct candidate identities or unresolved policy conflict; never merge by chain ID |
| NCP-002 | Same chain ID and genesis tuple copied by a malicious clone | Identity remains provisional until lineage/corroboration policy resolves clone ambiguity |
| NCP-003 | Same canonical tuple assigned to two active keys in one interval | Registry integrity failure / CONFLICTED |
| NCP-004 | Two registry entries reuse one key for different tuples | Reject registry snapshot |
| NCP-005 | Display label says “mainnet” but genesis evidence conflicts | Ignore label; unresolved/conflicted |
| NCP-006 | User supplies an uncorroborated network ID and endpoint | Insufficient evidence; no identity verification |
| NCP-007 | Provider signs a false genesis claim | Signature may authenticate provider statement; factual claim remains disputed/unverified |
| NCP-008 | Two URLs are mirrors of the same upstream provider | Count as one correlated failure domain |
| NCP-009 | Provider returns stale registry metadata | Freshness gate blocks purpose requiring current evidence |
| NCP-010 | Boolean supplied as block height/slot | Contract-invalid; do not coerce to integer |
| NCP-011 | Negative, fractional or non-canonical coordinate | Contract-invalid |
| NCP-012 | Hash has wrong algorithm, prefix or byte length | Contract-invalid |
| NCP-013 | Hash text has different casing but same decoded bytes where case-insensitive hex is defined | Canonicalize deterministically; same digest |
| NCP-014 | Hash algorithm identifier is omitted or ambiguous | Reject input; no guessed default |
| NCP-015 | Checkpoint exists structurally but no finality evidence exists | CEA may be structurally valid; FA remains UNKNOWN/NOT_ASSESSED |
| NCP-016 | Provider labels checkpoint “finalized” without verifiable policy evidence | Preserve provider claim; FA not satisfied |
| NCP-017 | Two independent sources report different hashes at same coordinate | CONFLICTED/UNKNOWN; preserve both observations |
| NCP-018 | Several endpoints share one provider backend | Do not treat as independent corroboration |
| NCP-019 | Reorg removes a previously observed checkpoint | Mark affected checkpoint ORPHANED; preserve history; propagate by dependency |
| NCP-020 | Competing fork branches share height/slot | Canonicality unresolved until policy adjudication |
| NCP-021 | Network fork creates two persistent lineages | Create explicit child subjects/lineage; no silent overwrite |
| NCP-022 | Migration changes endpoint and chain identifier | Require explicit successor/migration evidence; do not assume equivalence |
| NCP-023 | Solana cluster label conflicts with genesis identity | Genesis/profile evidence takes precedence over label; unresolved if conflict remains |
| NCP-024 | Registry key changes without a versioned change event | Reject registry update |
| NCP-025 | Registry entry expires while historical replay is requested | Permit historical replay under historical version; block current use if policy requires active entry |
| NCP-026 | Evidence is withdrawn by source | Create withdrawal event; reassess claims; do not infer claim false automatically |
| NCP-027 | Source signing key is later compromised | Reassess affected signatures/claims over the defined compromise interval |
| NCP-028 | Evidence is structurally valid but license forbids storage/use | Block prohibited processing; no evidence admissibility |
| NCP-029 | Source permission is unknown | Treat permission as unresolved; do not assume public access grants reuse |
| NCP-030 | Checkpoint observation is fresh but provider is compromised | Freshness cannot override source integrity/admissibility failure |
| NCP-031 | Checkpoint is stale for trading but useful for historical reconstruction | Purpose-specific freshness; block only the use requiring freshness |
| NCP-032 | Dependency graph cannot determine which assessments depend on a checkpoint | Mark dependency unresolved; do not claim NOT_AFFECTED |
| NCP-033 | Partial scope is affected by fork but partition coverage is incomplete | Review/abstain; do not invalidate or clear the entire parent scope by guess |
| NCP-034 | Registry producer attempts to approve their own change | Reject authorization under SoD |
| NCP-035 | Emergency suspension has no approved authority or scope | Reject transition; preserve attempted action in audit |
| NCP-036 | Two concurrent incompatible registry changes arrive | Conflict/adjudication; never last-write-wins |
| NCP-037 | Same event is replayed with identical idempotency key and payload | Idempotent no-op with traceable duplicate record |
| NCP-038 | Same event ID arrives with different payload digest | Integrity conflict; quarantine event |
| NCP-039 | Verifier is replayed with identical manifest and versioned inputs | Identical canonical output and digest |
| NCP-040 | Verifier output omits registry/policy version or evaluation cutoff | Incomplete proof manifest; no favorable result |
| NCP-041 | Hash digest is correct but source provenance is missing | Integrity alone insufficient; evidence remains inadmissible/insufficient |
| NCP-042 | New profile version changes canonicalization rules | Historical replay uses old profile; current assessment requires explicit migration/re-evaluation |
| NCP-043 | A source claims a checkpoint on a network not bound to its observation | Reject binding; no cross-network inference |
| NCP-044 | A finalized checkpoint conflicts with a verified ancestor/descendant chain | CONFLICTED; invoke incident/finality adjudication |
| NCP-045 | Registry snapshot contains duplicate JSON keys or ambiguous serialization | Reject before semantic evaluation |

## 15. Invariants

- NCP-INV-01: network identity, registry evidence, checkpoint evidence and finality are distinct assessments.
- NCP-INV-02: labels, symbols, endpoints and provider IDs are not canonical network identity.
- NCP-INV-03: one active canonical identity tuple cannot map to multiple active keys in the same namespace/effective interval.
- NCP-INV-04: registry collisions never resolve by ordering or popularity.
- NCP-INV-05: every identity claim is atomic, typed, scoped and time-bounded.
- NCP-INV-06: source signature does not imply factual truth or source independence.
- NCP-INV-07: multiple correlated endpoints do not constitute independent corroboration.
- NCP-INV-08: evidence permissions are separate from evidence integrity and factual reliability.
- NCP-INV-09: checkpoint existence does not imply canonicality or finality.
- NCP-INV-10: finality is family-, network-, policy- and time-specific.
- NCP-INV-11: UNKNOWN, UNAVAILABLE and NOT_ASSESSED never become PASS or FINALITY_SATISFIED by default.
- NCP-INV-12: STALE does not imply INVALID, ORPHANED or FALSE.
- NCP-INV-13: ORPHANED does not imply network identity changed.
- NCP-INV-14: provider disagreement is preserved and cannot be resolved by last-write-wins.
- NCP-INV-15: reorg/fork propagation follows explicit dependencies and exact scope.
- NCP-INV-16: historical registry/evidence versions remain immutable and replayable.
- NCP-INV-17: a digest proves byte binding, not semantic truth.
- NCP-INV-18: deterministic replay requires pinned inputs, policies, profiles, cutoff and algorithm version.
- NCP-INV-19: missing or unresolved mandatory evidence blocks favorable dependent outcomes.
- NCP-INV-20: no evidence is processed beyond its authorized purpose, retention or jurisdiction.
- NCP-INV-21: a producer cannot approve their own material registry change.
- NCP-INV-22: emergency authority is explicit, scoped, time-bounded and retrospectively reviewed.
- NCP-INV-23: identity resolution does not imply asset representation legitimacy, backing, solvency or investment eligibility.
- NCP-INV-24: no checkpoint/finality status creates a trading, custody or execution authorization.

## 16. Cross-audit against IIE architecture

### 16.1 Scope Algebra (X-09)

Alignment required:
- network, checkpoint, source, purpose, activity, time and environment are typed scope dimensions;
- unresolved membership remains unresolved, never empty or wildcard;
- fork/partial invalidation requires exact atomic partition and coverage;
- derived subset assessments carry their own scope identity and decision.

Open integration questions:
- define canonical scope dimension identifiers for network lineage, checkpoint coordinate and source failure domain;
- define a stable ScopeSignature binding for registry and checkpoint assessments;
- define proof-certificate binding to registry version, evidence set, policy and atomic scope;
- verify dependency closure before declaring a scope unaffected.

### 16.2 X-06 deterministic decision composition

This contract supplies inputs, not a new decision precedence. NIA/REA/CEA/FA/PAA outcomes must enter the canonical ValidationEnvelope and decision composition through approved producer/consumer mappings. Contract INVALID stops input processing; UNKNOWN/CONFLICTED/NOT_ASSESSED remain domain outcomes and cannot be collapsed into INELIGIBLE or favorable eligibility without explicit policy.

### 16.3 X-07 lifecycle and freshness

Use separate valid_time, observed_at, recorded_at, evaluated_at and effective_at. Freshness policy is purpose-specific. Corrections, source compromise, reorgs and profile changes create lifecycle events and scope-aware invalidation. Historical assessments remain immutable; no TTL or silence lifts a veto.

### 16.4 X-08 governance and SoD

Role names in this proposal are placeholders, not assigned authorities. Registry write, independent review, approval, emergency suspension and restoration must be mapped to approved X-08 roles, delegated authority and conflict-of-interest rules. No approval is inferred from repository authorship.

### 16.5 Identity R01 catalogs

The proposal preserves the separation:
- IdentityStatus: RESOLVED / UNRESOLVED / CONFLICTED;
- FreshnessStatus: FRESH / APPROACHING_EXPIRY / STALE / NOT_APPLICABLE;
- contract validity: VALID / INVALID as a separate envelope dimension;
- evidence admissibility/sufficiency, claim resolution and workflow/lifecycle remain separate;
- UNKNOWN is epistemic, NOT_ASSESSED is procedural, UNDETERMINED is applicability, INVALID is contractual.

No new status is declared canonical. Exact crosswalks and producer/consumer authority require review and approval.

## 17. Approval gates and implementation sequence

**Gate A — Semantics and profiles**
- Approve network subject, identifier tuples, family profiles and canonicalization.
- Approve lifecycle/fork/migration semantics and uniqueness scope.
- Approve distinct evidence, checkpoint, canonicality and finality vocabularies.

**Gate B — Evidence, sources and permissions**
- Approve atomic claim taxonomy and source-authority matrix.
- Approve independence/corroboration policy per claim class.
- Complete legal/licensing, retention, privacy and jurisdiction review.
- Define source compromise and withdrawal handling.

**Gate C — Governance**
- Map all roles to X-08 RACI/SoD and named accountable owners.
- Approve registry publication, emergency suspension, restoration and adjudication authority.
- Approve incident, change-control and audit procedures.

**Gate D — Executable contract**
- Design schemas/envelopes and compatibility/versioning rules in a separate authorized work item.
- Build deterministic offline validators and adversarial/property-based fixtures.
- Bind outputs to Scope Algebra, ValidationEnvelope, lifecycle and decision replay.
- Keep RPC adapters and real registry population out of scope until separately authorized.

**Gate E — Independent verification**
- Execute unit, contract, property-based, mutation and adversarial tests.
- Verify determinism, replay, parser robustness, collision handling and failure modes.
- Independently review security, licensing, source assumptions and family-specific finality models.
- Validate against real-world networks only under approved source, legal, security and operational controls.

## 18. Open decisions and limitations

1. Which authority owns the registry namespace and signs/releases registry versions?
2. Which family-specific primary sources and independent corroborators are approved?
3. What evidence proves genesis identity for each network family, and what clone/fork ambiguity remains?
4. Which finality models and thresholds are accepted for each family/network?
5. What source-independence taxonomy and correlated-provider detection are required?
6. What is the canonical schema for atomic claims, evidence envelopes and checkpoint proofs?
7. Which cryptographic signature formats, key lifecycle and revocation sources are approved?
8. How are network lineage and checkpoint dependencies represented in Scope Algebra?
9. Which exact X-08 roles may approve, suspend and restore registry entries?
10. What legal basis permits collection, storage, retention and commercial use of each source?

No numeric finality threshold, source quorum, TTL, approved provider list or real-network trust claim is set by this document.

## 19. Status declaration

**PROPOSED / REVIEW_REQUIRED / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED.**

This draft defines the contract boundary, proposed data model, deterministic verification stages, security/governance controls, 45 adversarial scenarios and 24 invariants. It is a static design specification. No executable tests, live RPC, real-network evidence, legal review, security assessment, independent approval or empirical validation were performed. No canonical IIM requirement, production schema, resolver implementation, fixture or test was modified.
