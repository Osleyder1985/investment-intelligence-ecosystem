# A01 — Second adversarial review of HIGH-findings remediation

**Review ID:** IIE-IDENTITY-NETWORK-LINEAGE-REVIEW-002  
**Version:** 0.1.0-draft  
**Date:** 2026-10-31  
**Target:** `network-identity-lineage-a01-high-findings-remediation.md` v0.1.0-draft  
**Related documents:** A01 identity/lineage proposal and first cross-audit  
**Review type:** Static adversarial and cross-document review  
**Status:** REVIEW_REQUIRED / INTEGRATION_REVISE / NOT_APPROVED  
**Independence:** Internal review; not external assurance.

## 1. Determination

The remediation document makes substantial progress. It supplies explicit candidate rules for trust bootstrap, key-to-subject cardinality, permanent split identity allocation, purpose-specific proof obligations and state ownership.

The five original HIGH findings are **not all closed**. The design has improved, but the trust-root bootstrap, authority of split adjudication, key-subject temporal cardinality, and executable protocol proof obligations still depend on governance and A02/A03. In addition, several edge cases need to be made deterministic before schemas or code are authorized.

**Disposition: REVIEW_REQUIRED / INTEGRATION_REVISE / NOT_APPROVED.**

This is a design-level review only. No protocol clients, cryptographic proofs, real authorities, registries or production implementations were tested.

## 2. Reassessment of original HIGH findings

| Original finding | Reassessment | Reason |
|---|---|---|
| A01-RV-01 Authority trust bootstrap | PARTIALLY_ADDRESSED | External trust snapshot prevents registry self-authorization conceptually, but bootstrap authority, authenticated distribution, trust-anchor change authority and recovery remain unresolved |
| A01-RV-02 Key-subject cardinality | PARTIALLY_ADDRESSED | Active-key cardinality and no-reuse are clearer; alias/federation/authority succession interactions and temporal overlap rules need formal constraints |
| A01-RV-03 Permanent split transition | DESIGN_RULE_DEFINED / APPROVAL_PENDING | Parent-preserving/new-child IDs are deterministic; actual split criteria and adjudicator authority depend on A03/X-08 |
| A01-RV-04 Family proof obligations | PARTIALLY_ADDRESSED | Predicate framework is sound, but EVM/Solana profiles still lack concrete, independently reviewed artifacts/verifiers/test vectors |
| A01-RV-05 State ownership/crosswalk | PARTIALLY_ADDRESSED | Axes are separated, but exact producer/consumer transitions and mappings to approved canonical catalogs are not yet formally complete |

No original HIGH finding is declared fully closed for production.

## 3. New residual findings

| ID | Severity | Finding |
|---|---|---|
| A01-RV2-01 | HIGH | Trust bootstrap remains recursively dependent on an unspecified governance root and trust-snapshot distribution |
| A01-RV2-02 | HIGH | Split allocation is deterministic after adjudication, but split authority and family-specific decision predicates remain unbound |
| A01-RV2-03 | HIGH | EVM/Solana proof obligations are dimensioned but not operationally decidable without concrete profile artifacts and verifier contracts |
| A01-RV2-04 | MEDIUM | Alias/federation key cardinality lacks formal temporal uniqueness and conflict resolution across authority succession |
| A01-RV2-05 | MEDIUM | Split children and parent lack a complete relationship/cardinality and query semantics contract |
| A01-RV2-06 | MEDIUM | State crosswalk remains descriptive; transition ownership, terminality and legacy migration vectors are incomplete |

**Count:** 3 HIGH, 3 MEDIUM, 0 LOW.

## 4. Detailed findings

### A01-RV2-01 — Trust bootstrap root and trust-snapshot distribution remain unspecified

**Severity:** HIGH

The remediation correctly prevents a registry from self-adding trust anchors. However, it delegates bootstrap to a "separately governed trust configuration" without specifying the first trust authority, how a consumer authenticates the first snapshot, how trust configuration updates are distributed, or how a consumer detects rollback/substitution.

This is a deliberate governance dependency, but it remains a blocking design gap for production trust.

**Adversarial case:** A consumer receives an authentic but obsolete trust snapshot from a compromised distribution channel. Signatures verify under a previously trusted root, but the snapshot omits a key revocation. Without monotonic version/rollback protection and authenticated freshness, the consumer accepts a revoked registry authority.

**Required remediation:**
- specify bootstrap trust source and out-of-band enrollment procedure;
- bind snapshot digest/version/sequence and validity interval;
- define anti-rollback and stale-snapshot behavior;
- define dual-control or independent approval requirements under approved X-08 authority;
- define recovery when trust root is compromised or unavailable;
- pin snapshot provenance and distribution evidence in replay.

The review does not prescribe a threshold or specific trust technology.

### A01-RV2-02 — Split adjudication authority and criteria remain unbound

**Severity:** HIGH

The parent-preserving rule removes ambiguity about identity allocation once a split is approved. It does not decide who may approve a split, nor define a complete decision predicate. The remediation explicitly defers finality and duration thresholds to A03 and authority to X-08, both still draft/unapproved.

**Adversarial case:** Two parties claim authority to designate different continuity successors after the same fork. Both present plausible governance evidence. The registry can preserve the conflict, but there is no approved authority hierarchy or adjudication procedure to determine whether either claim can be accepted.

**Required remediation:**
- define an adjudication case contract and authorized role/delegation references;
- require evidence of jurisdiction/scope of authority over the specific network subject;
- define conflict-of-authority, appeal and dissent handling;
- bind split decision to A03 family profile and X-08 role IDs;
- until authority is resolved, preserve competing proposals and abstain from permanent split approval.

### A01-RV2-03 — Family proof obligations remain non-executable

**Severity:** HIGH

The remediation decomposes identity into useful dimensions and requires raw artifacts, verifier versions, predicates and test vectors. Yet it does not identify the actual EVM/Solana artifacts, canonical encoding, hash/preimage rules, verifier obligations, trust assumptions, branch ancestry evidence or failure semantics. It explicitly defers them to future profiles/A03.

This is acceptable as an interface-level proposal, not sufficient for implementation or a claim that NCP-AR-02 is closed.

**Adversarial case:** Two conforming implementations use different genesis artifact canonicalization or endpoint binding procedures but both satisfy the current high-level profile language.

**Required remediation:**
- produce separately reviewed EVM and Solana profile specifications;
- specify raw input artifacts, canonicalization and verification steps;
- include positive, negative, malformed, conflicting and replay test vectors;
- define unsupported versions and fail-closed behavior;
- obtain protocol-expert review and independent implementation comparison.

### A01-RV2-04 — Alias/federation temporal uniqueness remains underspecified

**Severity:** MEDIUM

The rule "one active key maps to exactly one subject at any effective instant" is useful. However, it needs formal handling of overlapping valid-time intervals, late-recorded corrections, cross-authority alias collisions, namespace transfer and concurrent issuance. "SHOULD have one canonical key" is weaker than the uniqueness constraints and could permit two canonical keys in one namespace.

**Required remediation:** define uniqueness constraints for each relation type, authority/namespace boundary, effective interval and knowledge-time; specify whether canonical key is exactly one per subject/authority/namespace/time or optional; define deterministic conflict handling.

### A01-RV2-05 — Parent/child lineage relation cardinality and query semantics remain incomplete

**Severity:** MEDIUM

The remediation says every child gets a new subject ID and parent is preserved. It does not fully define relation cardinality, whether a split event can have one or more children, whether a child can have multiple parents after complex merges, how successive splits compose, or what queries return for historical versus current scope.

**Required remediation:** define a SplitEvent aggregate with one parent and a non-empty set of distinct children (or explicitly permit complex multi-parent lineage as a different event type); define lineage graph cycle constraints; define query semantics by time and purpose; never infer current successor from graph reachability alone.

### A01-RV2-06 — State crosswalk remains descriptive rather than executable

**Severity:** MEDIUM

The table classifies outcomes and dimensions but does not define complete transition preconditions, event owners, terminality, invalidation and legacy mapping for all branch and adjudication states. X-07 requires separate state machines and immutable events; R01 has proposed but unapproved catalogs.

**Required remediation:** create a producer/consumer matrix and transition table tied to exact catalog versions; specify legacy status mapping as explicit migration decisions, never automatic inference.

## 5. Cross-document consistency observations

### 5.1 Scope Algebra

The remediation correctly requires exact scope signature, purpose/activity and dependency snapshots. It must not invent a competing scope digest. The authoritative canonical serialization, signature fields, universe and normalization version must be supplied by an approved X-09 contract. Until then, A01 assessments must remain non-consumable for production purposes if exact scope binding cannot be proven.

### 5.2 X-07 lifecycle

The trust and split events need to preserve valid-time, observed/recorded/knowledge-time distinctions. Corrections and late revocations must create new events and reassessments, not rewrite prior registry state. Idempotency requires aggregate version and ordering semantics in addition to event ID/payload digest.

### 5.3 X-08 governance

No role in this proposal is a real authority assignment. Split adjudication, trust-anchor administration, emergency suspension and recovery require approved role IDs, delegation and segregation of duties. Repository authorship or PR merge is not authority.

### 5.4 D-09/D-10

NetworkSubject remains the NETWORK identity dimension, while representation/instrument/listing identity stays separate. A split does not transfer asset identity or operational eligibility. Any child mapping for downstream representation requires separate claims and purpose-scoped assessments.

## 6. Recommended closure sequence

1. Close A01-RV2-01 with A02/A05 trust bootstrap and anti-rollback contract.
2. Close A01-RV2-02 with X-08 authority model and A03 split/finality profiles.
3. Close A01-RV2-03 by authoring EVM and Solana proof profiles with test vectors.
4. Close A01-RV2-04 and A01-RV2-05 with a formal key-binding and lineage relationship catalog.
5. Close A01-RV2-06 after R01/X-07 catalogs are approved and transition matrices are reconciled.
6. Run independent cross-audit before schema/code/test authorization.

## 7. Final status

**REVIEW_REQUIRED / INTEGRATION_REVISE / NOT_APPROVED.**

The remediation materially improves the architecture and resolves ambiguity at proposal level, but does not justify declaring the five original HIGH findings fully closed. Three HIGH and three MEDIUM residual findings remain. No canonical requirements, schemas, code, fixtures or tests were changed. No tests were executed for this review. No external assurance or real-network verification is claimed.
