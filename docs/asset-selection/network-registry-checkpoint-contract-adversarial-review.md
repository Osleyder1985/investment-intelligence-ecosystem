# Adversarial review — network registry and checkpoint verification contract

**Review ID:** IIE-IDENTITY-NCP-ADVERSARIAL-REVIEW-001  
**Version:** 0.1.0-draft  
**Date:** 2026-10-31  
**Target:** IIE-IDENTITY-NETWORK-EVIDENCE-001 v0.1.0-draft  
**Target PR:** #72  
**Review type:** Static adversarial design review (document inspection only)  
**Status:** REVIEW_REQUIRED / INTEGRATION_REVISE / NOT_APPROVED  
**Approval:** No independent institutional or external reviewer has approved this report.

## 1. Executive determination

The target draft establishes useful and necessary separations between network identity, registry evidence, checkpoint evidence, canonicality, finality and purpose admissibility. It also correctly rejects several dangerous shortcuts: chain ID alone, provider labels, signature-as-truth, URL-count corroboration, checkpoint-presence-as-finality, last-write-wins, universal TTL and implicit scope expansion.

However, the contract is **not yet sufficiently specified to serve as an implementation baseline**. It defines what categories must be evaluated but leaves several critical proof obligations, protocol-specific semantics, trust anchors, cross-document bindings and authority assignments unresolved.

**Determination: REVIEW_REQUIRED / INTEGRATION_REVISE / NOT_APPROVED.** The draft should not be promoted to canonical requirements, production schemas, live RPC integration or real-network trust assertions until the HIGH findings are closed and the cross-audit is accepted.

This is a static adversarial review. No executable code, schemas, live providers, consensus clients, legal sources, cryptographic proofs or real-network evidence were tested. Findings are design defects or unresolved obligations, not claims that a live network is compromised.

## 2. Review scope and evidence examined

Reviewed:
- `docs/asset-selection/network-registry-provenance-and-checkpoint-verification-contract.md` on PR #72;
- X-09 Scope Algebra, `docs/methodology/19-x09-scope-algebra-and-atomic-partition.md`;
- X-09 formal verification strategy, `docs/methodology/20-x09-scope-algebra-formal-verification-strategy.md`;
- X-07 lifecycle/freshness/invalidation, `docs/methodology/12-lifecycle-freshness-and-invalidation-framework.md`;
- X-08 RACI/SoD, `docs/methodology/13-governance-raci-and-segregation-of-duties.md`;
- identity architecture decisions D-01..D-08 and their stated status.

Review method:
1. Challenge each NCP-A01..A05 objective with adversarial counterexamples.
2. Compare proposed fields/statuses against existing canonical-candidate vocabularies and cross-document contracts.
3. Identify missing proof obligations, trust assumptions, failure semantics and authority boundaries.
4. Define closure evidence required before implementation.

No source was treated as approved merely because it exists in the repository.

## 3. Findings summary

| ID | Area | Severity | Finding |
|---|---|---|---|
| NCP-AR-01 | A01 | HIGH | Registry namespace and global key uniqueness are semantically ambiguous |
| NCP-AR-02 | A01 | HIGH | EVM/Solana identity tuples do not fully characterize fork/clone lineage |
| NCP-AR-03 | A01 | HIGH | Fork, split, reset and migration adjudication lack deterministic identity rules |
| NCP-AR-04 | A02 | HIGH | Source independence is required but not represented as an evaluable failure-domain model |
| NCP-AR-05 | A02 | HIGH | Evidence authenticity, signing-key trust and historical revocation are underspecified |
| NCP-AR-06 | A02 | HIGH | Claim-to-evidence sufficiency and conflict resolution have no explicit proof contract |
| NCP-AR-07 | A02 | MEDIUM | Permission assessment is described but lacks explicit decision states and evidence binding |
| NCP-AR-08 | A03 | HIGH | Checkpoint structural binding is not protocol-level checkpoint verification |
| NCP-AR-09 | A03 | HIGH | Finality policy lacks family-specific safety/liveness and failure semantics |
| NCP-AR-10 | A03 | HIGH | Checkpoint status product is incomplete and has no authoritative composition contract |
| NCP-AR-11 | A03 | HIGH | Rollups and externally anchored execution environments are not modeled |
| NCP-AR-12 | A03 | MEDIUM | Checkpoint coordinate and header semantics are too generic for heterogeneous protocols |
| NCP-AR-13 | A04 | HIGH | Scope binding is named but no stable scope signature/atomic scope contract is specified |
| NCP-AR-14 | A04 | HIGH | Dependency closure and proof completeness are not machine-verifiable in this contract |
| NCP-AR-15 | A04 | MEDIUM | Temporal envelope omits several X-07/D-07 knowledge-time distinctions |
| NCP-AR-16 | A04 | MEDIUM | Deterministic replay does not pin all external trust and verification dependencies |
| NCP-AR-17 | A05 | HIGH | Proposed authority roles are not mapped to X-08 approved role IDs and actual delegations |
| NCP-AR-18 | A05 | MEDIUM | Emergency suspension, adjudication, restoration and concurrent approvals lack complete workflow gates |
| NCP-AR-19 | Cross-cutting | MEDIUM | Canonical serialization and digest rules are not sufficiently specified |
| NCP-AR-20 | Cross-cutting | MEDIUM | The 45 scenarios are not yet mapped to proof obligations, executable test classes or coverage claims |

**Count:** 12 HIGH, 8 MEDIUM, 0 LOW.

## 4. Detailed findings

### NCP-AR-01 — Registry namespace and global key uniqueness are ambiguous

**Severity:** HIGH  
**Area:** A01 — network identity and registry

The field description calls `network_key` “globally unique” while also saying it is unique “within the registry namespace”. Those are different guarantees. If two authorities issue the same opaque key, consumers need a collision-free qualified identifier.

The contract also does not define whether namespace authority identity is immutable, transferable, delegated or revocable, nor how two registries merge or federate.

**Adversarial case:** Registry A and Registry B both issue `network_key=net-001`; a downstream claim references only `net-001`. The subject is ambiguous even though each local registry passes its own uniqueness check.

**Required remediation:**
- define a qualified key, e.g. (registry_authority_id, namespace_id, network_key), with immutable authority identity;
- define federation, delegation, namespace transfer and collision handling;
- specify uniqueness over registry version, effective interval and lifecycle state;
- define trust and revocation of registry signing authority.

**Closure evidence:** key grammar, uniqueness invariants, federation conflict scenarios and an approved registry authority model.

### NCP-AR-02 — EVM/Solana identity tuples do not fully characterize fork/clone lineage

**Severity:** HIGH  
**Area:** A01

The draft correctly warns that chain ID plus genesis hash and a Solana genesis identifier can be copied. It does not define what additional evidence is required before a cloned or divergent network can be resolved as the same network, a child network, a separate network or unresolved.

For EVM, identical genesis and chain ID can coexist with later divergence; a genesis hash is not a commitment to all future consensus rules, chain configuration, hard forks or canonical history. For Solana, a genesis/cluster hash does not by itself prove that two endpoints currently follow the same canonical ledger or share the same fork lineage.

**Adversarial case:** An operator clones a network from an existing genesis state, preserves chain ID/genesis identity and runs a divergent chain. The identity tuple matches, but the economic/operational network may not be the same subject for all purposes.

**Required remediation:**
- define identity confidence levels and purpose-specific resolution criteria;
- define when a fork is a new network subject versus a temporary branch of one network;
- bind relevant chain configuration, consensus/client rules, checkpoint ancestry and governance evidence as separate claims;
- state explicitly when identity remains unresolved despite matching genesis identifiers.

**Closure evidence:** family-specific decision tables and adversarial fixtures for copied genesis, divergence, replay, hard fork and temporary reorg.

### NCP-AR-03 — Fork, split, reset and migration adjudication lack deterministic identity rules

**Severity:** HIGH  
**Area:** A01

The lifecycle event names are useful, but the draft does not define transition preconditions, required evidence, authoritative adjudicator, effective-time semantics or the relationship between a temporary competing branch and a permanent network split.

“Creates distinct network lineage subjects when the identity policy determines they are materially distinct” defers the decisive rule to an undefined policy.

**Adversarial case:** Two independent maintainers disagree whether a long-lived chain split is a reorg or a permanent split. Both publish a registry update with different parent/child relationships.

**Required remediation:**
- specify a versioned fork/split adjudication protocol;
- distinguish transient fork, finalized divergence, permanent split, reset and migration;
- require evidence and authority for lineage transitions;
- define conflicting registry proposals, appeal/review and effective interval;
- prohibit automatic identity remapping of assets or representations.

**Closure evidence:** transition table, state-machine invariants, concurrent-change scenarios and RACI approval path.

### NCP-AR-04 — Source independence is not an evaluable failure-domain model

**Severity:** HIGH  
**Area:** A02 — provenance and source corroboration

The draft requires administrative and technical independence but supplies no taxonomy, identifiers or graph model by which a verifier can determine shared ownership, upstream infrastructure, software, data feeds, validators, cloud regions or control planes.

A policy saying “evaluate independence” is not enough for deterministic implementation if the inputs do not represent the relationships being evaluated.

**Adversarial case:** Three RPC brands return identical results but all proxy the same upstream node or share one cloud/operator. A URL count suggests quorum while the evidence has one failure domain.

**Required remediation:**
- define SourceIdentity and SourceFailureDomain entities and typed relationships;
- distinguish ownership, operation, hosting, upstream node, client implementation, validator set, data derivation and signing authority;
- define evidence quality and uncertainty for each relationship;
- specify conservative behavior when independence is unknown;
- define policy-controlled independence thresholds by claim/finality class, without hardcoding one universal quorum.

**Closure evidence:** source-dependency graph contract, failure-domain scenarios and deterministic independence evaluation rules.

### NCP-AR-05 — Evidence authenticity, signing-key trust and historical revocation are underspecified

**Severity:** HIGH  
**Area:** A02

The document lists signatures, certificate/key IDs, verification results, key rotation and compromise response, but does not define the trust-anchor snapshot, allowed signature algorithms, certificate/key validity semantics, revocation evidence, compromise effective interval or treatment of retrospective compromise.

A signature verifier that consults a mutable current trust store cannot produce deterministic historical replay unless the trust-store state and verification policy are pinned.

**Adversarial case:** A provider key is declared compromised today, but the provider signed an observation six months ago. The system cannot determine whether the signature predates compromise, was backdated or was produced by an attacker without a versioned compromise interval and trusted timestamp evidence.

**Required remediation:**
- define signature profile registry and algorithm allowlist;
- pin trust anchors, certificate chains, revocation lists/status responses and verification time;
- distinguish signature creation time, trusted timestamp, observed time and recorded time;
- define compromise interval uncertainty and retroactive reassessment;
- specify key rotation, revocation, expiry and algorithm deprecation.

**Closure evidence:** cryptographic verification profile, historical trust-state replay tests and compromise-window scenarios.

### NCP-AR-06 — Claim-to-evidence sufficiency and conflict resolution have no explicit proof contract

**Severity:** HIGH  
**Area:** A02

The atomic claim concept is sound, but the draft does not define the predicate contract, evidence-to-claim relation, admissibility/sufficiency dimensions, refutation semantics, closed-world boundaries or adjudication record. It also does not bind a registry entry's activation to a complete set of mandatory claims.

This risks treating a list of evidence references as if it were proof that each registry assertion follows from those references.

**Adversarial case:** A registry entry has three evidence references, but all support only “provider endpoint reports chain ID”, while the entry asserts genesis identity, canonicality and governance ownership.

**Required remediation:**
- define claim types and PredicateContract bindings;
- distinguish source observation, claim support, refutation, corroboration, admissibility, sufficiency, freshness and contract validity;
- define mandatory claim sets by family/profile and purpose;
- define conflict adjudication and dissent preservation;
- require proof that each activated registry field is supported by admissible evidence for that exact predicate and scope.

**Closure evidence:** claim/evidence mapping matrix, proof obligations and negative scenarios for unsupported assertions.

### NCP-AR-07 — Permission assessment lacks explicit decision states and evidence binding

**Severity:** MEDIUM  
**Area:** A02

The draft lists permission dimensions and says unknown permission blocks prohibited processing, but does not define a permission assessment object, statuses, policy authority, evidence source, effective interval or conflict precedence.

**Required remediation:** align with X-07 temporal lifecycle and a separately approved legal/licensing catalog; model PERMITTED, PERMITTED_WITH_LIMITATIONS, PROHIBITED, UNKNOWN, NOT_ASSESSED and INVALID only if approved, with explicit per-operation/per-purpose decisions.

### NCP-AR-08 — Checkpoint structural binding is not protocol-level checkpoint verification

**Severity:** HIGH  
**Area:** A03 — checkpoint and cryptographic verification

The verifier checks coordinate type, hash length, profile and references. Those checks can validate a well-formed envelope containing a fabricated hash. The draft acknowledges this in principle, but does not define the next proof step required to verify that the bytes are a valid protocol header/commitment and are connected to the claimed network state.

Missing family-specific obligations include header serialization, hash preimage, domain separation, parent linkage, state-root derivation, Merkle proof verification, commitment verification, signature/validator-set checks and protocol upgrade rules where applicable.

**Adversarial case:** A provider supplies a syntactically valid 32-byte hash at height N with a plausible parent reference, but no valid block header or authenticated ancestry. Structural validation passes despite no proof that the checkpoint belongs to the network.

**Required remediation:**
- split STRUCTURAL_VALIDATION from PROTOCOL_PROOF_VERIFICATION and CANONICALITY_ASSESSMENT;
- define family-profile verifier operations and required raw artifacts;
- define proof object types, trusted inputs and exact verification algorithms;
- return UNKNOWN/INSUFFICIENT_EVIDENCE when only provider assertions are available.

**Closure evidence:** family-specific proof contracts and adversarial forged-header/invalid-parent/proof-substitution cases.

### NCP-AR-09 — Finality policy lacks family-specific safety/liveness and failure semantics

**Severity:** HIGH  
**Area:** A03

The draft says finality policy must define model, threshold, ancestry and halted-chain behavior, but it does not distinguish safety from liveness or define what FINALITY_NOT_SATISFIED means. A chain that has not finalized because it is halted is different from a chain whose checkpoint is known to be non-final or has been reverted.

The same generic status is insufficient to represent probabilistic confirmation, deterministic BFT finality, optimistic rollup challenge windows, validity-proof rollups and UTXO confirmation depth.

**Adversarial case:** A chain halts after a checkpoint; the checkpoint has accumulated many confirmations but the finality gadget has not finalized. A policy based on a numeric threshold might incorrectly treat it as final.

**Required remediation:**
- define family-specific finality profile types and assumptions;
- separate safety violation, liveness delay, pending finality, finalized, reverted/orphaned and unknown;
- specify quorum/validator-set changes, slashing/weak-subjectivity or long-range assumptions where applicable;
- define proof of finality and trusted checkpoint bootstrap;
- define policy response to conflicting finality certificates.

**Closure evidence:** protocol-specific finality tables, safety/liveness invariants and halted/forked/compromised-validator scenarios.

### NCP-AR-10 — Checkpoint status product is incomplete and lacks composition semantics

**Severity:** HIGH  
**Area:** A03

The draft defines separate dimensions, which is preferable to one universal status, but it omits explicit states for a verified finality contradiction/safety violation and does not define legal combinations or precedence. For example, can `STRUCTURALLY_VALID + ORPHANED + FINALITY_SATISFIED` coexist? Does `INVALID + FINALITY_SATISFIED` ever make sense? What does `CANONICALITY_CONFIRMED + FINALITY_UNKNOWN` mean for each family?

**Required remediation:**
- define independent typed assessment objects and a compatibility matrix;
- reserve explicit contradiction/violation outcomes without conflating them with contract INVALID;
- define aggregate result composition and fail-closed behavior;
- specify whether finality can be satisfied absent canonicality proof (normally it should not);
- map all statuses to canonical assessment catalogs only after architecture approval.

**Closure evidence:** status product table, invalid combinations, aggregate algorithm and transition tests.

### NCP-AR-11 — Rollups and externally anchored execution environments are not modeled

**Severity:** HIGH  
**Area:** A03

The draft lists rollups among families needing separate profiles but does not model their multi-layer identity or finality. For rollups, L2 block production, L1 publication, data availability, state transition validity, sequencer assertions, challenge windows and L1 finality are distinct. A generic parent checkpoint reference does not capture these relations.

**Adversarial case:** L2 RPC reports a finalized block while its batch is not yet finalized on L1, data availability is unavailable, or the challenge window remains open.

**Required remediation:**
- explicitly exclude rollups from initial supported family scope or define a separate multi-layer profile;
- model execution layer, settlement layer, data-availability layer, proof/challenge mechanism and sequencer;
- define when L2 finality is inherited from L1 and under which assumptions;
- distinguish unsafe, safe, finalized and challenged states only as family-defined semantics.

**Closure evidence:** rollup threat model and L1/L2/DA finality matrix, or an explicit non-support gate.

### NCP-AR-12 — Checkpoint coordinate/header semantics are too generic for heterogeneous protocols

**Severity:** MEDIUM  
**Area:** A03

Height, slot, epoch, round and checkpoint kind are listed, but no typed coordinate schema establishes whether coordinates are monotonic, fork-local, composite or resettable. Parent linkage may be optional even when required by the family. Hashing a block header and hashing a state root require different domain and encoding rules.

**Required remediation:** make coordinate and checkpoint proof profiles fully typed and family-versioned; define required/forbidden fields and lineage constraints; reject generic coercion between coordinate types.

### NCP-AR-13 — Scope binding lacks a stable scope signature and atomic scope contract

**Severity:** HIGH  
**Area:** A04 — Scope Algebra integration

The document says assessments must use approved Scope Algebra dimensions but does not specify the actual scope_kind, subject selectors, purpose/activity, universe/version, normalization, scope signature, atomic partition ID or proof binding fields.

X-09 requires typed scopes, universe and normalization versions, resolved operation results, exact partitions and semantic digests. A textual list of scope dimensions is not enough to bind an NIA/CEA/FA result to the exact unit it assesses.

**Adversarial case:** A checkpoint proof is replayed against a different network lineage or a broader purpose/route because the proof digest binds evidence but not a canonical atomic scope and its universe version.

**Required remediation:**
- define the exact scope signature for each assessment type;
- bind scope_id/signature, scope_kind, subject, purpose, activity, universe, normalization, time interval and dependency snapshot into every assessment/proof manifest;
- require atomic scope coverage for partial fork/reorg impact;
- define crosswalk to X-09 scope operations and formal proof certificates.

**Closure evidence:** scope binding schema proposal, signature preimage definition and proof-replay substitution tests.

### NCP-AR-14 — Dependency closure and proof completeness are not machine-verifiable

**Severity:** HIGH  
**Area:** A04

The document requires dependency completeness in the output but does not define how completeness is proven, which graph snapshot is authoritative, how edge omission is detected or how incomplete traversal is distinguished from a complete empty dependency set.

X-07 and X-09 prohibit inferring NOT_AFFECTED from missing edges and require explicit scope-aware dependency propagation. A boolean `dependency_complete` without a proof object is an assertion, not evidence.

**Adversarial case:** A compromised source is linked to one checkpoint observation but an alternate path through a shared upstream provider is omitted. The verifier reports a complete dependency closure because it only traversed the supplied partial graph.

**Required remediation:**
- define authoritative dependency registry/snapshot and closure algorithm;
- define completeness certificate, root set, graph version, traversal bounds and unresolved edges;
- distinguish complete-empty from missing/unavailable/incomplete graph;
- fail closed on truncation, cycles, stale edges and unresolved scope transforms.

**Closure evidence:** graph closure proof model, bounded traversal semantics and omitted-edge adversarial tests.

### NCP-AR-15 — Temporal envelope omits X-07/D-07 knowledge-time distinctions

**Severity:** MEDIUM  
**Area:** A04

The evidence record includes observed/received/recorded time, and the replay manifest includes evaluation cutoff, but the core checkpoint envelope does not consistently require source_published_at, public_availability_at, authorized_availability_at, captured_at, ingested_at, trusted timestamp and knowledge profile when these matter.

X-07/D-07 explicitly distinguishes fact-time from knowledge-time and AS_KNOWN_THEN from corrected historical views. This matters for historical chain incidents, provider corrections and backtests.

**Required remediation:** define a temporal envelope that references the approved D-07 time types and knowledge profile; never infer availability from source timestamps.

### NCP-AR-16 — Deterministic replay does not pin all external trust dependencies

**Severity:** MEDIUM  
**Area:** A04

The manifest pins registry, evidence, policies, profiles and algorithm version but does not explicitly require trust-anchor/key state, revocation snapshot, signature verifier implementation, source-independence graph, license/permission policy, protocol client/specification version, validator set or finality proof verifier dependencies.

**Required remediation:** include every external trust input and dependency version in a typed manifest; define canonical serialization, digest preimage, software supply-chain identity and replay failure when any dependency is unavailable.

### NCP-AR-17 — Authority roles are not mapped to approved X-08 role IDs/delegations

**Severity:** HIGH  
**Area:** A05 — governance

The target document uses generic role labels (Registry Authority, Architecture Authority, designated adjudicator, emergency role). X-08 already has a proposed role catalog and RACI structure, but those roles are themselves unapproved and no actual authority assignment exists. The target does not define the mapping, compatibility or escalation path.

**Adversarial case:** A Registry Authority approves a family profile change, while X-08 assigns methodology changes to GOV-01/GOV-03 or another accountable authority. Both documents appear locally consistent but authorize different actors.

**Required remediation:**
- create a producer/consumer role crosswalk to X-08 role IDs;
- identify the authoritative approval activity and one accountable authority;
- define delegations, substitutions, conflicts of interest and quorum where applicable;
- keep the system blocked until actual owners and authority are approved;
- ensure a role name in the PR author field is never interpreted as approval.

**Closure evidence:** approved RACI crosswalk and authority assignment record.

### NCP-AR-18 — Emergency suspension, adjudication and restoration workflows are incomplete

**Severity:** MEDIUM  
**Area:** A05

The document mentions emergency suspension and retrospective review but does not define required trigger evidence, initial scope, maximum duration, notification SLA, dual control, expiry, extension authority, appeal or restoration workflow. Concurrent conflicting changes are also not tied to an explicit optimistic concurrency/version precondition.

**Required remediation:** specify state transitions and authorization envelope for suspend/quarantine/restore; include reason, evidence, scope, actor, authority, expiry, review deadline and event idempotency; require new independent evidence for restoration.

### NCP-AR-19 — Canonical serialization and digest rules are incomplete

**Severity:** MEDIUM  
**Area:** Cross-cutting

The document requires deterministic serialization, canonical JSON/digests and duplicate-key rejection but does not select or define a canonicalization profile, Unicode normalization, numeric representation, ordering, null/omitted semantics, timestamp precision, binary encoding or digest domain separation.

**Adversarial case:** Two semantically equivalent envelopes serialize differently, or two parsers interpret duplicate keys differently, producing inconsistent digests or signature verification.

**Required remediation:** specify a versioned canonical serialization and hash profile; domain-separate registry, claim, checkpoint, proof and event digests; define exact numeric/time/binary normalization; reject ambiguous encodings.

### NCP-AR-20 — Adversarial scenarios are not mapped to proof obligations or test classes

**Severity:** MEDIUM  
**Area:** Cross-cutting

The 45 scenarios provide breadth, but they remain prose acceptance examples. They are not mapped to the 20 findings, invariants, formal proof obligations, schema constraints, unit/property/mutation/integration tests or expected state transitions. Scenario count must not be represented as test coverage.

**Required remediation:** create a traceability matrix mapping each scenario to:
- attack/fault model and preconditions;
- inputs and pinned versions;
- expected per-dimension outcomes;
- invariants/proof obligations;
- test class and fixture requirements;
- evidence artifact and reviewer.

**Closure evidence:** complete scenario-to-invariant-to-test matrix, with no claim of tests executed until CI evidence exists.

## 5. NCP-A01..A05 closure plan

### A01 — Network identity and registry integrity
1. Resolve NCP-AR-01..03.
2. Define authority-qualified stable keys and federation.
3. Define family-specific identity/lineage decision tables.
4. Define fork/split/reset/migration state machine and adjudication authority.
5. Prove uniqueness and temporal replay invariants.

**Gate:** no real registry population before approval.

### A02 — Evidence and source provenance
1. Resolve NCP-AR-04..07.
2. Define source failure-domain graph and source-authority matrix.
3. Define trust-anchor, signature and compromise-time profiles.
4. Define atomic claim/evidence proof contract and mandatory claim sets.
5. Define licensing/permission assessment and retention controls.

**Gate:** no provider ingestion or evidence retention before legal/security and source-policy approval.

### A03 — Checkpoint and finality
1. Resolve NCP-AR-08..12.
2. Separate structural, protocol-proof, canonicality and finality assessments.
3. Define family-specific header/proof/finality profiles.
4. Define finality safety/liveness and contradiction states.
5. Explicitly exclude unsupported families, especially rollups, until profile coverage exists.

**Gate:** no status FINALITY_SATISFIED from provider labels, confirmation count alone or checkpoint presence.

### A04 — Scope, lifecycle and replay
1. Resolve NCP-AR-13..16.
2. Bind all assessments to exact atomic Scope Algebra signatures.
3. Define dependency closure certificates and partial-scope propagation.
4. Align temporal fields with X-07/D-07.
5. Pin all trust and software dependencies in deterministic replay.

**Gate:** no NOT_AFFECTED or favorable assessment without complete scope/dependency proof.

### A05 — Governance and authority
1. Resolve NCP-AR-17..18.
2. Map proposed roles to X-08 and obtain institutional approval.
3. Define authority, delegation, SoD, emergency suspension, restoration and adjudication.
4. Implement RBAC/ABAC only after organizational assignments and security review.

**Gate:** no activation, suspension lift, profile approval or exception without verified authority.

## 6. Required cross-document integration artifacts

Before a successor contract is approved, produce:
1. **Network identity profile registry proposal** — authority-qualified keys, family profiles, lifecycle and lineage.
2. **Source authority/failure-domain model** — source graph, claim-specific authority and independence.
3. **Evidence and cryptographic trust profile** — claims, signatures, trust anchors, revocation and replay.
4. **Checkpoint proof/finality profile catalog** — structural/protocol/canonicality/finality stages by supported family.
5. **Scope binding and dependency closure contract** — X-09 scope signature, atomic partition and completeness proof.
6. **Role/authority crosswalk** — target roles mapped to X-08, with real assignment explicitly pending.
7. **Scenario traceability matrix** — NCP-001..045 × findings × invariants × test cases.

These artifacts may be split into separate reviewable work items. They must not be silently embedded into production code before their semantics are approved.

## 7. What is acceptable to retain from the target draft

The following are sound design directions and should be preserved, subject to precise contracts:
- independent NIA, REA, CEA, FA and PAA assessments;
- labels/endpoints are metadata, not canonical identifiers;
- source signatures do not prove factual correctness;
- source diversity is failure-domain-based, not URL count;
- checkpoint structural validity is distinct from canonicality/finality;
- reorg/fork history is append-only and scope-aware;
- unknown, stale, unavailable and not-assessed are not favorable outcomes;
- no automatic network identity change from a reorg;
- no universal TTL, quorum or finality threshold;
- no inferred legal permission from public accessibility;
- no role assignment inferred from repository authorship;
- no claim that the synthetic resolver verifies real networks.

## 8. Final status

**REVIEW_REQUIRED / INTEGRATION_REVISE / NOT_APPROVED.**

The target PR #72 is a useful conceptual draft but does not yet define an implementation-ready, family-specific and governance-approved trust contract. The 12 HIGH findings are blocking. The 8 MEDIUM findings must be resolved before a complete conformance baseline is claimed.

This review is static and adversarial, not independent external assurance. No code, schema, canonical requirement, resolver, fixture or test was modified. No test suite was run specifically for this review. The existing CI result for PR #72 does not validate the semantic correctness of this document or this review.
