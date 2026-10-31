# EA-AR-01 — Adversarial review of external verifier qualification protocol

**Review ID:** IIE-TRUST-VERIFIER-QUALIFICATION-REVIEW-001  
**Version:** 0.1.0-draft | **Date:** 2026-10-31  
**Target:** ea-ar01-external-verifier-qualification-and-reconstitution-protocol.md  
**Review type:** Static adversarial and cross-document review  
**Status:** REVIEW_REQUIRED / INTEGRATION_REVISE / NOT_APPROVED

## 1. Determination

The protocol materially improves the definition of external verifier qualification. It separates identity, legal status, competence, authorization, independence and attestation; makes qualification task- and scope-specific; rejects self-authentication; and preserves suspension when external trust references cannot be authenticated.

It is not a deployable verifier accreditation or trust bootstrap mechanism. It depends on external authorities whose own trust basis remains unspecified. It does not formally prove source-independence closure, define authoritative status publication against split-view attacks, establish legally recognized competence profiles, or provide machine-enforceable consumer verification of VQRs. Its candidate status vocabulary is not reconciled with canonical catalogs.

**Disposition: REVIEW_REQUIRED / INTEGRATION_REVISE / NOT_APPROVED.**

## 2. Findings summary

| ID | Severity | Finding |
|---|---|---|
| VQ-AR-01 | HIGH | Trust basis and qualification of the qualification authority remain circular/underspecified |
| VQ-AR-02 | HIGH | VQR publication, authenticity, consistency and anti-rollback are not operationally specified |
| VQ-AR-03 | HIGH | Independence graph closure and common-cause reasoning are not formally decidable |
| VQ-AR-04 | HIGH | Verifier competence/recognition profiles lack legally grounded acceptance rules |
| VQ-AR-05 | HIGH | Consumer verification after total trust-state loss is not technically bounded |
| VQ-AR-06 | MEDIUM | Status/freshness/revocation semantics and historical impact are not fully crosswalked |
| VQ-AR-07 | MEDIUM | Case authorization, multi-role conflicts and quorum are not composed deterministically |
| VQ-AR-08 | MEDIUM | 38 scenarios and 20 invariants lack executable proof/test traceability |
| VQ-AR-09 | MEDIUM | Privacy, liability, complaints and redress remain jurisdiction-specific and unassigned |

**Count: 5 HIGH, 4 MEDIUM, 0 LOW.**

## 3. Detailed findings

### VQ-AR-01 — Qualification authority bootstrap remains underspecified

The protocol requires a separately governed VQR issuer or recognition authority but does not establish how that authority's identity, competence and legal mandate are authenticated after total trust-state loss. A registry or regulator name alone is not a cryptographic or legal proof.

**Attack:** attacker fabricates an accreditation authority and a plausible VQR hierarchy; a consumer with no surviving trust reference has no deterministic basis to distinguish it from the legitimate authority.

**Required remediation:** define jurisdiction-specific roots of recognition, independently verifiable legal instruments, external witness/confirmation paths and the exact residual manual adjudication route. If no competent authority can be independently authenticated, maintain suspension.

### VQ-AR-02 — VQR publication and consistency protocol incomplete

The VQR is versioned and content-addressed conceptually, but no signed object format, canonical serialization, witness consistency protocol, split-view detection, anti-rollback state or recovery behavior is specified.

**Attack:** two consumers receive different VQR versions, both signed by a compromised or equivocating publisher; each accepts a different verifier set.

**Required remediation:** integrate the approved A02 trust snapshot protocol, define canonical signed-object profile, predecessor linkage, independent witnesses, consistency proofs, consumer high-water state and stateless/offline limitations. No algorithm or quorum should be selected before cryptographic review.

### VQ-AR-03 — Failure-domain closure is not formally decidable

The document lists dependency dimensions but does not define the graph contract, required node/edge types, transitive closure, unknown-edge handling, common-cause predicates or proof certificate.

**Attack:** verifier and regulator appear independent but share beneficial ownership, identity provider, cloud administrator and recovery custodian; incomplete graph is treated as independence.

**Required remediation:** integrate A02 source principal/failure-domain graph and Scope Algebra. Define mandatory closure, dependency completeness, scope-bound proof obligations and fail-closed UNKNOWN propagation. Absence of an edge in an incomplete graph must never prove independence.

### VQ-AR-04 — Competence and recognition profiles lack legal acceptance rules

The alternative competence path (experience, references, demonstrations, peer review) is not tied to a legally accountable qualification authority or minimum task-specific standard. This could allow self-selected peers to confer authority.

**Attack:** a newly created association recognizes its own members as qualified legal authority reviewers, with no external recognition or accountability.

**Required remediation:** define role-specific legal/technical competence requirements, accepted recognition basis, assessor independence, accountability and appeal. Where no legally or independently recognized basis exists, limit claims to technical witness or factual attestation; do not imply legal authority.

### VQ-AR-05 — Consumer verification after total trust loss is not technically bounded

The VQR cannot authenticate itself to a consumer that has lost all pinned references. Manual process is described, but no consumer security tier, trusted human interface, anti-substitution protocol or local state protection is specified.

**Attack:** malware replaces the verifier's directory and user interface; operator approves a fabricated VQR during emergency manual reconstitution.

**Required remediation:** define consumer assurance tiers, trusted display/confirmation channel, independent challenge binding, anti-rollback limitations and explicit human-in-the-loop controls. If the consumer cannot authenticate the independent basis, it must remain suspended.

### VQ-AR-06 — Status and historical impact crosswalk incomplete

The proposed qualification dispositions, freshness, workflow and record lifecycle are descriptive and not aligned with X-07/R01 canonical status models. It is unclear how suspension, expiry, revocation, compromise and unknown status compose for a given case and historical attestation.

**Required remediation:** define separate contract validity, qualification disposition, freshness, workflow and record lifecycle mappings; define temporal impact and scope invalidation without rewriting historical facts.

### VQ-AR-07 — Case authorization composition ambiguous

The document lists case-bound attributes but does not define deterministic authorization composition for multiple roles, delegated signers, conflicts, recusals, quorum, partial scope and emergency replacement.

**Attack:** a qualified verifier is authorized for identity and a second for authority, but the same controlling person sits behind both services; approvals are counted separately.

**Required remediation:** bind principal/control identity and delegation graph to X-08 SoD; define case authorization envelope, atomic scope, quorum and deny precedence. Do not count linked principals as independent approvers.

### VQ-AR-08 — No executable traceability

Thirty-eight scenarios and twenty invariants are not mapped to proof obligations, test classes, fixtures, expected results or review evidence.

**Required remediation:** create a traceability matrix separating DOCUMENTED, PROOF_SPECIFIED, FIXTURE_DEFINED, TEST_IMPLEMENTED, TEST_EXECUTED and INDEPENDENTLY_REVIEWED. Map every scenario/invariant to the relevant state, scope and adversarial property.

### VQ-AR-09 — Accountability, liability and redress incomplete

The protocol mentions professional liability, complaints and appeal but does not define accountable parties, compensation/remediation, evidence preservation, notification, dispute timelines or protection against retaliation.

**Required remediation:** jurisdiction-specific legal/contractual review and explicit accountability/complaint/appeal procedure before live reliance.

## 4. Cross-document integration

- **RR-AR-01:** VQR provides bounded verifier evidence; it does not replace identity/authority verification or EnrollmentCommitment.
- **A02/AR-01:** VQR publication must inherit signed snapshot, witness, anti-rollback and recovery semantics; no parallel trust root.
- **A02 failure-domain model:** verifier, accreditation authority, registry, status endpoint, subcontractor and publication infrastructure require typed dependency closure.
- **X-07:** qualification, credential, key, legal status and control changes require append-only temporal events.
- **X-08:** actual profile owner, qualification authority, approver, suspension, reviewer and appeal authority remain unassigned.
- **X-09:** case scope and dependency closure require exact Scope Algebra signatures and proof binding.
- **R01:** proposed states remain non-canonical until catalog ownership and crosswalk are approved.
- **Privacy/legal:** evidence collection, retention, disclosure and liability require jurisdiction-specific controls.

## 5. Required next sequence

1. VQ-AR-01: qualification authority root and external recognition basis.
2. VQ-AR-02: signed VQR publication, witness consistency and anti-rollback.
3. VQ-AR-03: formal failure-domain closure and proof certificate.
4. VQ-AR-04: role-specific legal/technical competence profiles.
5. VQ-AR-05: consumer verification after total trust-state loss.
6. VQ-AR-06..09: canonical lifecycle crosswalk, case authorization, executable traceability and accountability.
7. Independent legal, cryptographic, security and governance review.

## 6. Scope and status

Static internal adversarial review only. No real verifier, accreditation authority, jurisdiction, legal instrument, consumer, trust anchor, signature scheme or status directory was tested. No code, schema, fixture or automated test was added or executed.

**REVIEW_REQUIRED / INTEGRATION_REVISE / NOT_APPROVED.**