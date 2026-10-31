# RR-AR-01 — Adversarial review of external enrollment authority

**Review ID:** IIE-TRUST-ENROLLMENT-AUTHORITY-REVIEW-001  
**Version:** 0.1.0-draft | **Date:** 2026-10-31  
**Target:** `a02-ar01-rv01-rr-ar01-external-enrollment-authority-and-identity-assurance.md`  
**Type:** Static internal adversarial/cross-document review  
**Status:** REVIEW_REQUIRED / INTEGRATION_REVISE / NOT_APPROVED

## 1. Determination

The proposal separates identity, authority, ceremony authorization, technical validity and independence. It rejects authority by repository/technical ownership, defines consumer-specific challenges and prohibits unattended bootstrap after total local trust-state loss.

It is not deployable. Assurance classes are descriptive; no jurisdictional evidence policy, qualified verifier scheme, legal recognition rules, channel-independence proof or consumer anti-cloning mechanism has been approved. Partial activation and disputed evidence also lack deterministic decision semantics.

## 2. Findings

| ID | Severity | Finding |
|---|---|---|
| EA-AR-01 | HIGH | External assurance roots and verifier qualification unspecified |
| EA-AR-02 | HIGH | Legal authority validation and cross-jurisdiction conflict resolution undefined |
| EA-AR-03 | HIGH | Channel independence is qualitative; no formal dependency closure |
| EA-AR-04 | HIGH | Consumer identity, cloning detection and anti-rollback not technically enforceable |
| EA-AR-05 | MEDIUM | Assurance classes lack measurable role-specific profiles |
| EA-AR-06 | MEDIUM | Partial activation, quorum and consumer acknowledgement semantics ambiguous |
| EA-AR-07 | MEDIUM | Privacy, retention and disclosure need jurisdiction-specific rules |
| EA-AR-08 | MEDIUM | Scenario/invariant verification and status crosswalk are not executable |

**Count: 4 HIGH, 4 MEDIUM, 0 LOW.**

## 3. Detailed findings

### EA-AR-01 — External assurance roots and verifier qualification

The proposal does not specify how external verifier identities/authority are qualified, how verifiers are held accountable, or how a consumer validates their credentials after total loss of its previous trust state.

**Required:** verifier qualification profiles, accepted authority evidence, independent discovery/verification channels, revocation and complaint mechanisms, and a bounded manual/legal reconstitution route. If no independently corroborated basis exists, enrollment remains suspended.

### EA-AR-02 — Legal authority and cross-jurisdiction conflicts

Governing-law selection, conflicts of law, entity status changes, contested resolutions, competing delegates, insolvency, court orders and foreign-instrument recognition are undefined.

**Required:** jurisdiction-specific legal review; governing law and entity domicile; instrument hierarchy, effective dates and revocation; dispute escalation and suspension. A technical engine must not adjudicate legal conflicts.

### EA-AR-03 — Channel independence lacks formal closure

“Two materially independent evidence paths” is qualitative. Typed dependency graph, transitive closure, shared-control predicates, required dimensions and proof obligations are missing.

**Required:** integrate A02 failure-domain graph and Scope Algebra; define mandatory nodes/edges, closure completeness, UNKNOWN propagation and common-cause conditions. Incomplete closure prohibits a full-independence result.

### EA-AR-04 — Consumer identity and anti-rollback are not enforceable

ConsumerIdentity, EnrollmentInstance and nonce binding are defined conceptually, but protected identity, clone detection, device replacement, generation monotonicity, restore and split-brain behavior are not.

**Required:** hardware-backed/software-only assurance tiers, key lifecycle, instance revocation, concurrency/duplicate detection, external monotonic/witness options and explicit residual limitations. Do not claim clone prevention without platform evidence.

### EA-AR-05 — Assurance classes lack measurable profiles

EA-0..EA-4 have no mandatory evidence profile per principal/action, issuer assurance, evidence freshness or reviewer competence.

**Required:** role/action-specific profiles and explicit NOT_ASSESSED/INSUFFICIENT outcomes. No score may conceal a missing mandatory control.

### EA-AR-06 — Partial activation and quorum ambiguous

Bounded-scope activation lacks atomicity, required consumer population, quorum, scope partitioning and stale-consumer restrictions.

**Required:** all-or-nothing default; approved atomic partitions; consumer restrictions, acknowledgements and stale-consumer quarantine; quorum/SoD under approved X-08 policy.

### EA-AR-07 — Privacy controls incomplete

Lawful basis, access roles, retention, cross-border transfers, redaction, subject access and legal hold are not specified.

**Required:** jurisdiction-specific privacy assessment, controller/processor allocation, retention/deletion schedule and audit disclosure rules before live identity collection.

### EA-AR-08 — Scenario traceability missing

Thirty-two scenarios and twenty-two invariants are not mapped to proof obligations, tests, fixtures, expected outputs or independent review. Candidate labels are not reconciled with R01/X-07 catalogs.

**Required:** traceability matrix distinguishing DOCUMENTED, FIXTURE_DEFINED, TEST_IMPLEMENTED, TEST_EXECUTED and INDEPENDENTLY_REVIEWED.

## 4. Cross-document obligations

- **RV-01:** ConsumerBindingRecord and EnrollmentCommitment need a single normative owner.
- **A02:** verifier/attestor provenance and failure domains must themselves be assessed.
- **A02-AR-01:** ceremony and snapshot publication use the same generation/predecessor rules.
- **A01/NCP:** registry authority cannot be inferred from domain name, chain ID or endpoint.
- **X-07:** credential/authority revocation and consumer reset are append-only temporal events.
- **X-08:** actual verifier, authority, reviewer, quorum and appeal roles remain unassigned.
- **X-09:** dependency closure uses exact scopes and unresolved-state propagation.
- **R01:** candidate assurance/workflow states need catalog mapping before schemas/code.

## 5. Required sequence

1. EA-AR-01: verifier qualification and bounded manual/legal reconstitution.
2. EA-AR-02: jurisdictional authority conflict model.
3. EA-AR-03: formal channel/failure-domain graph closure.
4. EA-AR-04: consumer instance security and anti-rollback profiles.
5. EA-AR-05..08: assurance profiles, activation atomicity, privacy and test traceability.
6. Independent legal, security and governance review.

## 6. Status

**REVIEW_REQUIRED / INTEGRATION_REVISE / NOT_APPROVED.**

Static internal review only. No real verifier, authority, identity, jurisdiction, device, cryptographic mechanism or legal instrument was tested. No code, schema, fixture or automated test was added or executed. This remains documentation-only and is not an approved enrollment procedure.
