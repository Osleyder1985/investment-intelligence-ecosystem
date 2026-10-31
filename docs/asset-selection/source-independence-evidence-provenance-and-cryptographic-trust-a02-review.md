# A02 — Adversarial and cross-document review

**Review ID:** IIE-EVIDENCE-TRUST-A02-REVIEW-001  
**Version:** 0.1.0-draft  
**Date:** 2026-10-31  
**Target:** `source-independence-evidence-provenance-and-cryptographic-trust-a02.md` v0.1.0-draft  
**Review type:** Static adversarial and cross-document consistency review  
**Status:** REVIEW_REQUIRED / INTEGRATION_REVISE / NOT_APPROVED  
**Independence:** Internal design review; not external security, legal or cryptographic assurance.

## 1. Determination

The A02 proposal establishes important separations: source principal versus endpoint, source lineage versus independence assessment, evidence bytes versus capture/provenance, digest integrity versus signature authenticity, signature validity versus key authorization, and evidentiary admissibility versus sufficiency and permission.

The document is not ready for approval or implementation. It defines a useful conceptual architecture but leaves important contracts intentionally open. Some of these are unavoidable dependencies on governance, security and legal review; others are specification gaps that must be resolved before an executable implementation can be unambiguous.

**Disposition: REVIEW_REQUIRED / INTEGRATION_REVISE / NOT_APPROVED.**

No real source, key, trust root, evidence artifact, license or cryptographic implementation was evaluated.

## 2. Findings summary

| ID | Severity | Finding |
|---|---|---|
| A02-AR-01 | HIGH | Trust bootstrap and anti-rollback still lack a complete authenticated distribution/recovery protocol |
| A02-AR-02 | HIGH | Independence graph lacks a formal model for uncertainty, evidence, temporal closure and claim-specific failure impact |
| A02-AR-03 | HIGH | Cryptographic profile and signed-object canonicalization/domain-separation contract is incomplete |
| A02-AR-04 | HIGH | Key compromise/revocation impact analysis lacks deterministic affected-record closure and adjudication semantics |
| A02-AR-05 | HIGH | Evidence sufficiency and conflict adjudication remain policy interfaces without formal proof obligations |
| A02-AR-06 | MEDIUM | SourcePrincipal identity, legal entity, control and endpoint ownership cardinalities are underspecified |
| A02-AR-07 | MEDIUM | Time and timestamp trust model lacks clock authority, uncertainty and ordering semantics |
| A02-AR-08 | MEDIUM | Permission/retention model lacks explicit state ownership and deletion/hold conflict semantics |
| A02-AR-09 | MEDIUM | New evidence/independence statuses need canonical R01 catalog crosswalk |
| A02-AR-10 | MEDIUM | 45 scenarios and 32 invariants are not mapped to proof obligations, test classes or coverage evidence |

**Count:** 5 HIGH, 5 MEDIUM, 0 LOW.

## 3. Detailed findings

### A02-AR-01 — Trust bootstrap and anti-rollback protocol incomplete

**Severity:** HIGH

The proposal requires an independently governed trust configuration, sequence, authentication, anti-rollback and recovery. It does not define the initial enrollment protocol, the trust configuration's own signature/approval envelope, authenticated distribution channel, monotonic state storage, offline recovery, root compromise recovery or behavior when consumers have lost trusted sequence state.

**Adversarial case:** A new consumer receives a validly signed but obsolete trust snapshot. Its local state has been reset, so it cannot detect rollback. A revoked key remains trusted under the old snapshot.

**Required remediation:**
- define bootstrap enrollment and out-of-band verification;
- define snapshot signature/approval envelope and trust transition chain;
- define monotonic sequence and rollback protection for stateful and stateless consumers;
- define offline/air-gapped recovery and root compromise;
- define maximum snapshot age and fail-closed behavior through approved policy, not arbitrary defaults;
- bind all trust configuration artifacts and approvals in replay.

### A02-AR-02 — Independence graph lacks formal uncertainty and failure-impact semantics

**Severity:** HIGH

The proposal correctly rejects URL counting and models multiple dependency dimensions. However, it does not define a canonical typed graph contract, edge assertion/adjudication lifecycle, negative/unknown edge semantics, graph closure, or how dependency evidence maps to a claim-specific common-mode failure. Independence cannot be calculated reproducibly from a list of candidate edges alone.

**Adversarial case:** Provider A and B use different cloud accounts but the same upstream node fleet and parser library. One reviewer marks them independent based on ownership; another marks them dependent based on data origin. The document lacks a formal per-claim failure model and composition rule to force consistent results.

**Required remediation:**
- define edge object, endpoint types, direction, valid-time/knowledge-time, evidence and confidence/epistemic state;
- define explicit UNKNOWN and NOT_ASSESSED semantics for missing lineage;
- define failure-domain graph completeness and dependency closure proof;
- define policy composition for required dimensions and partial independence;
- specify conflict adjudication and evidence for ownership/control changes;
- create deterministic examples and conformance vectors.

### A02-AR-03 — Cryptographic profile and signed-object canonicalization incomplete

**Severity:** HIGH

The proposal requires algorithm identifiers, canonicalization, domain separation and verifier versions but does not define approved profiles, exact byte serialization, signature envelope encoding, algorithm parameters, certificate path semantics, trusted timestamp rules, or downgrade/deprecation handling. Without a pinned profile registry, implementations can differ while claiming conformance.

**Adversarial case:** Two implementations serialize the same logical JSON evidence envelope differently, verify signatures over different byte sequences, and both report signature valid under a vague canonicalization label.

**Required remediation:**
- define versioned cryptographic profile registry and allowed algorithm/parameter sets;
- specify exact canonical serialization and domain-separation construction;
- define signature envelope byte format and key/certificate reference semantics;
- define timestamp authority/verification semantics and signing-time uncertainty;
- define deprecation, downgrade, algorithm agility and historical verification policy;
- provide cross-implementation test vectors.

The review does not select algorithms or key sizes.

### A02-AR-04 — Key compromise/revocation impact closure is not deterministic

**Severity:** HIGH

The proposal distinguishes planned rotation, revocation and compromise and preserves valid-time/knowledge-time. It does not define the mandatory dependency graph from a compromised key to all signed artifacts, source/authority assessments, claims, decisions and downstream uses. It also lacks adjudication when compromise interval is uncertain or evidence is contradictory.

**Adversarial case:** A root signing key is compromised. Some signed trust snapshots and source records were copied into derived datasets; the system cannot prove which artifacts depend on the key due to missing signature references. It cannot determine the affected scope.

**Required remediation:**
- require key-to-signature-to-object dependency edges;
- define dependency closure proof and incomplete-closure state;
- define compromise interval bounds and uncertainty semantics;
- define quarantine, suspension, reassessment, release and appeal transitions;
- define authority/SoD for compromise adjudication;
- preserve historical AS_KNOWN_THEN and corrected AS_KNOWN_NOW results.

### A02-AR-05 — Evidence sufficiency and conflict adjudication lack formal proof obligations

**Severity:** HIGH

The proposal correctly says sufficiency is claim-specific and must not be reduced to a universal score. It does not define how PredicateContract conditions, source authority, independence, coverage and contradictions compose into a deterministic proof obligation. The policy interface is not yet executable or formally reviewable.

**Adversarial case:** One policy accepts one primary official artifact; another requires independent replication. Both claim to implement the same claim type because the claim-specific sufficiency contract is not versioned at predicate granularity.

**Required remediation:**
- define typed PredicateContract and SufficiencyPolicy schema semantics;
- enumerate mandatory claims, evidence classes, independence dimensions and coverage rules;
- define whether one source can satisfy each proof obligation and under what authority evidence;
- define conflict and adjudication state transitions;
- map all outcomes to FSA-01 TRUE/FALSE/UNKNOWN/INVALID without conflating admissibility, sufficiency or claim truth;
- produce proof bundles and independent replay test vectors.

### A02-AR-06 — SourcePrincipal identity and ownership cardinalities underspecified

**Severity:** MEDIUM

SourcePrincipal may represent organizations, institutions, protocol actors or individuals. The document does not define legal-entity identity, brand/product separation, subsidiaries, joint ventures, beneficial ownership evidence thresholds, source principal succession or endpoint-to-principal cardinality. Privacy constraints for individual principals are also open.

**Required remediation:** define entity taxonomy and relation catalog with temporal cardinality, identity evidence, legal review and privacy constraints. Do not infer common control from name similarity or domain ownership alone.

### A02-AR-07 — Time and timestamp trust model incomplete

**Severity:** MEDIUM

The proposal lists many time fields but does not define clock provenance, precision, uncertainty intervals, ordering guarantees, leap-second/time-zone handling, source timestamp trust or conflict between trusted timestamp and source-declared time. A signed timestamp is not necessarily a trusted time assertion.

**Required remediation:** define timestamp type, clock authority, uncertainty, ordering and canonical representation. Preserve unknown time and prohibit ordering claims where uncertainty intervals overlap.

### A02-AR-08 — Permission and retention conflict semantics incomplete

**Severity:** MEDIUM

The permission model identifies access, storage, retention, deletion and redistribution, but does not define conflict between deletion obligations and legal hold/audit retention, evidence preservation requirements, data minimization, redaction, backup expiry or derived artifact deletion propagation.

**Required remediation:** define purpose-specific permission states, retention schedule, legal-hold authority, deletion event/proof, derivative lineage propagation and what metadata may remain after deletion. Requires legal/privacy review.

### A02-AR-09 — Canonical state catalog crosswalk missing

**Severity:** MEDIUM

The proposal introduces independence outcomes and evidence axes, while R01/X-07 distinguish identity, evidence, freshness, workflow, lifecycle and contract validity. It explicitly defers catalog approval but does not yet provide a full producer/consumer and transition crosswalk. Implementers could introduce divergent vocabularies.

**Required remediation:** map each proposed value to an owning object/dimension, transition owner, invalidation behavior, canonical catalog version and legacy migration rule. No implementation until catalog authority approves.

### A02-AR-10 — Scenarios and invariants lack verification mapping

**Severity:** MEDIUM

The 45 scenarios and 32 invariants are useful review material, but are not mapped to proof obligations, test classes, expected machine-readable outputs, coverage criteria or independent reviewer responsibilities.

**Required remediation:** create a traceability matrix mapping each scenario/invariant to contract clause, proof obligation, fixture, test type, expected result, CI job and evidence artifact. Distinguish documented scenario, fixture defined, test implemented, test executed and independently reviewed.

## 4. Cross-document integration assessment

### A01 network identity and lineage

A02 supplies the source/trust evidence dimensions A01 depends on. However, A01 trust bootstrap and A02 trust bootstrap must use one authority trust contract; they must not evolve separate roots or snapshot formats. A01 split adjudication evidence must bind source independence and authority assessments for the exact split scope and cutoff.

### X-07 lifecycle

A02 needs explicit lifecycle events for source ownership change, evidence withdrawal, key rotation, compromise, revocation, trust snapshot update and permission change. Valid-time, knowledge-time and dependency invalidation must be reconciled. No historical record may be rewritten.

### X-08 governance

Trust-anchor administration, source admission, key revocation, conflict adjudication, legal permission approval and emergency quarantine require approved role IDs, delegation and segregation of duties. X-08 remains unapproved; A02 cannot create actual authority.

### X-09 Scope Algebra and FSA-01

Independence, sufficiency and permission are always evaluated for exact scope/purpose/activity and time. Unknown source lineage cannot be treated as independence or as NOT_AFFECTED. PredicateContract and proof bundle must use the approved scope signature and dependency closure.

### Identity D-05/D-06 and R01

A02 should align with the existing conceptual separation SourceRecord, EvidenceRecord, SourceAssessment and ClaimEvidenceAssessment. It must not create competing statuses for EvidenceStatus, EvidenceAdmissibility, EvidenceSufficiency, Freshness, ClaimResolution or contract validity. A canonical crosswalk is required before schemas.

## 5. Closure sequence

1. A02-AR-01: trust bootstrap, snapshot distribution and anti-rollback.
2. A02-AR-02: formal source lineage/failure-domain graph and independence composition.
3. A02-AR-03: cryptographic profile and signed-object canonicalization.
4. A02-AR-04: key compromise dependency closure and adjudication.
5. A02-AR-05: PredicateContract/SufficiencyPolicy proof obligations and FSA-01 adapter.
6. A02-AR-06..10: identity/time/permission/catalog/verification crosswalk.
7. Independent security, legal and mathematical review before implementation.

## 6. Final status

**REVIEW_REQUIRED / INTEGRATION_REVISE / NOT_APPROVED.**

A02 is a substantial specification proposal, not an implemented trust system. Five HIGH and five MEDIUM findings remain. No sources, keys, licenses or claims were verified. No schemas, code, fixtures or tests were added. No tests or external legal/security/cryptographic reviews were performed.
