# A02-AR-01 — Adversarial review of trust bootstrap and anti-rollback protocol

**Review ID:** IIE-TRUST-BOOTSTRAP-AR01-REVIEW-001  
**Version:** 0.1.0-draft  
**Date:** 2026-10-31  
**Target:** `a02-ar01-trust-bootstrap-distribution-and-anti-rollback-protocol.md` v0.1.0-draft  
**Review type:** Static adversarial and integration review  
**Status:** REVIEW_REQUIRED / INTEGRATION_REVISE / NOT_APPROVED  
**Independence:** Internal document review; not an external cryptographic/security assessment.

## 1. Determination

The remediation substantially improves the parent A02 text by defining bootstrap ceremony, trust-domain separation, snapshot envelope, approval policy, predecessor transitions, consumer acceptance, stateful/stateless/offline limitations, recovery epochs and historical replay.

The proposal correctly states that a stateless offline consumer cannot prove that a signed snapshot is current without an independent freshness signal. It also avoids prescribing arbitrary quorum, TTL, algorithm or hardware technology.

However, several critical properties remain descriptions rather than formal protocols. In particular, witness consistency, recovery bootstrap, consumer state rollback resistance, trust-policy transition authorization and cross-consumer split-view detection require further precision. The proposal is not approved for implementation.

**Disposition: REVIEW_REQUIRED / INTEGRATION_REVISE / NOT_APPROVED.**

## 2. Findings summary

| ID | Severity | Finding |
|---|---|---|
| AR01-RV-01 | HIGH | Recovery root bootstrap remains circular when both normal root and precommitted recovery authority are compromised or unavailable |
| AR01-RV-02 | HIGH | Witness model does not specify a consistency protocol, quorum/failure assumptions or reliable split-view detection |
| AR01-RV-03 | HIGH | Consumer high-water mark rollback resistance is not technically enforceable without a defined hardware/remote monotonic trust boundary |
| AR01-RV-04 | HIGH | Approval-policy transition and self-amendment resistance need a formal predecessor-policy authorization rule |
| AR01-RV-05 | HIGH | Trust snapshot fork resolution and cross-consumer gossip/reconciliation are incomplete |
| AR01-RV-06 | MEDIUM | Time/freshness policy lacks an explicit trusted-time and uncertainty interval model |
| AR01-RV-07 | MEDIUM | Root/recovery key separation and common-cause compromise analysis are not formalized |
| AR01-RV-08 | MEDIUM | Snapshot lifecycle and transition event semantics are not reconciled to canonical X-07/R01 catalogs |
| AR01-RV-09 | MEDIUM | Recovery impact closure does not define a machine-checkable key/signature/object dependency contract |
| AR01-RV-10 | MEDIUM | The 35 scenarios and 28 invariants lack proof-obligation and executable-test traceability |

**Count:** 5 HIGH, 5 MEDIUM, 0 LOW.

## 3. Detailed findings

### AR01-RV-01 — Recovery root bootstrap remains circular

**Severity:** HIGH

The document requires recovery authority to be precommitted and independent from normal root control. It does not specify how a consumer authenticates the recovery authority if the root and its trust metadata are compromised, nor what happens if recovery keys are lost, unavailable or compromised simultaneously.

**Adversarial case:** The normal root is compromised and the recovery authority's public keys are present only in the compromised trust snapshot. An attacker can replace both the recovery key reference and the recovery policy. The consumer has no independently pinned recovery commitment.

**Required remediation:**
- bind recovery commitments in the independently verified genesis/bootstrap ceremony and preserve them in a separately protected consumer trust record;
- define the recovery authorization path that does not require signatures from the compromised root;
- define recovery-of-recovery and loss-of-recovery-authority handling;
- define whether recovery can proceed with partial consumer state and how identity is re-established;
- explicitly state when no safe recovery path exists and manual re-enrollment is required.

Do not invent an ad hoc alternate root during an incident.

### AR01-RV-02 — Witness consistency protocol is underspecified

**Severity:** HIGH

The document mentions witness checkpoints, append-only logs, inclusion/consistency proofs and multiple independent channels but does not define the consistency protocol, witness failure assumptions, cross-witness agreement or client gossip requirements. A single witness can equivocate; multiple witnesses can share control or collude.

**Adversarial case:** A compromised distribution service presents snapshot A to one consumer and snapshot B to another for the same sequence. Each snapshot has a valid signature and a valid inclusion proof in a different witness view. Neither consumer contacts the other or an independent consistency monitor.

**Required remediation:**
- specify a transparency/consistency protocol profile or keep witness semantics explicitly non-conformant until one is selected;
- define witness identity, independence/failure domains, checkpoint sequencing and consistency verification;
- define gossip/monitoring or another mechanism for cross-client split-view detection;
- define behavior when witnesses disagree, disappear or are suspected compromised;
- do not claim equivocation detection from inclusion proofs alone.

No particular transparency-log implementation is selected by this review.

### AR01-RV-03 — Consumer high-water rollback resistance is not enforceable as written

**Severity:** HIGH

The document says local state should use hardware-backed monotonic facilities where available, or another independently protected mechanism. It does not define the minimum assurance for a consumer to claim rollback resistance. A protected file, encrypted backup or ordinary database can still be restored with the host.

**Adversarial case:** An attacker restores a virtual machine snapshot containing an old high-water mark and a valid old trust chain. The consumer has no TPM/secure element/remote witness state and accepts the old state as current.

**Required remediation:**
- define consumer assurance classes and the anti-rollback guarantees each can claim;
- bind hardware monotonic counter or remote witness state to trust-domain, sequence and digest;
- define clone/restore/failover behavior and device identity changes;
- if no protected monotonic state exists, require a fresh independent checkpoint on every high-impact use or classify consumer as historical-only;
- define recovery after hardware replacement without silently lowering accepted sequence.

### AR01-RV-04 — Approval-policy transition/self-amendment resistance incomplete

**Severity:** HIGH

The acceptance algorithm checks approval policy and transition type, but does not formally state which policy version authorizes a change to the policy itself. An attacker may propose a snapshot that weakens the threshold and then use the weakened policy to approve the same snapshot.

**Adversarial case:** Snapshot n changes approval threshold from a strict threshold to one signer and is signed only under the new threshold. The consumer cannot tell whether predecessor policy or new policy governs acceptance.

**Required remediation:**
- require every trust transition to be authorized under the predecessor snapshot's active policy;
- define policy-change dual binding: predecessor authorization approves the transition; successor policy governs only subsequent transitions after activation;
- define emergency transition exceptions as separately precommitted and narrowly scoped;
- define incompatible policy migrations and consumer capability negotiation;
- add tests for threshold downgrade, role substitution, key-purpose escalation and circular approval.

### AR01-RV-05 — Fork resolution and cross-consumer split-view detection incomplete

**Severity:** HIGH

The proposal says same-sequence different digests are equivocation and both branches are quarantined. It does not specify how consumers discover forks they have not directly observed, reconcile divergent high-water records, or safely recover when one branch is legitimate and another malicious.

**Adversarial case:** Consumer A has accepted sequence 15 / digest X; consumer B has accepted sequence 15 / digest Y. They remain partitioned. Each believes it has a valid current snapshot. Later reconnection has no canonical evidence exchange or adjudication protocol.

**Required remediation:**
- define consumer-to-consumer checkpoint exchange and verification;
- define a fork evidence package binding both branches, approvals, witnesses and observed times;
- define an independent adjudication authority and explicit fork/recovery transition;
- define how to quarantine affected downstream assessments during partition;
- define deterministic reconciliation and preserve both historical branches.

### AR01-RV-06 — Trusted time and freshness uncertainty need a formal model

**Severity:** MEDIUM

The document recognizes clock rollback and time uncertainty but lacks a typed time evidence contract, uncertainty interval arithmetic, trusted timestamp authority requirements and rules for overlapping intervals.

**Required remediation:** align with X-07 temporal semantics; define source-declared time versus trusted timestamp, uncertainty bounds, ordering guarantees and the cases where freshness is UNRESOLVED.

### AR01-RV-07 — Root/recovery common-cause compromise analysis incomplete

**Severity:** MEDIUM

Logical roles are separated, but the document does not require analysis of shared custody providers, cloud/HSM administrators, backup operators, legal control, recovery contacts or common jurisdictional coercion.

**Required remediation:** model root/recovery key custody as failure-domain graph nodes; assess organizational, technical, physical and jurisdictional independence. Multiple keys held by one common administrator are not independent.

### AR01-RV-08 — Lifecycle/state catalog crosswalk incomplete

**Severity:** MEDIUM

Snapshot states DRAFT/PROPOSED/APPROVED/ACTIVE/SUPERSEDED/SUSPENDED/REVOKED/QUARANTINED/REJECTED are candidate labels but no exact object owner, transition guard, terminality, invalidation and legacy crosswalk is supplied.

**Required remediation:** map each state to canonical object/dimension, X-07 lifecycle event, R01 catalog owner, transition authority and compatibility rules before schema or code.

### AR01-RV-09 — Trust impact closure is not machine-checkable

**Severity:** MEDIUM

The protocol requires key-to-signature-to-snapshot-to-claim dependency closure but does not define typed edges, completeness proof, derived artifact propagation or treatment of missing historical signatures.

**Required remediation:** define a typed dependency manifest and closure proof; incomplete closure must remain explicit and prevent claims that all affected objects were identified.

### AR01-RV-10 — Scenario and invariant verification traceability missing

**Severity:** MEDIUM

The 35 scenarios and 28 invariants are not mapped to protocol clauses, proof obligations, fixture classes, test vectors, CI jobs and evidence artifacts.

**Required remediation:** create a traceability matrix and separate documented, fixture-defined, test-implemented, test-executed and independently-reviewed states.

## 4. Cross-document integration findings

- **A02 parent:** Replace the generic bootstrap text only after A02-AR-01 approval; do not create competing trust-domain or snapshot contracts.
- **A01:** Network registry and lineage authority must consume the same bootstrap and recovery model. A01 cannot create its own root.
- **NCP:** Registry/checkpoint trust must pin trust snapshot, source authority and evidence lineage.
- **X-07:** Trust snapshot lifecycle and key events require valid-time/knowledge-time and dependency-aware invalidation.
- **X-08:** Root, approval, witness, recovery and audit roles remain unassigned/unapproved. This review cannot supply real authority.
- **X-09:** Every recovery impact assessment and consumer scope must bind exact scope signature and dependency closure.
- **R01:** Candidate snapshot statuses cannot become canonical without catalog owner approval.

## 5. Remediation sequence

1. AR01-RV-01: recovery root independence, recovery-of-recovery and re-enrollment.
2. AR01-RV-02: witness consistency, monitoring and split-view detection.
3. AR01-RV-03: consumer assurance classes and enforceable anti-rollback.
4. AR01-RV-04: predecessor-policy authorization and safe policy transitions.
5. AR01-RV-05: fork evidence, adjudication and cross-consumer reconciliation.
6. AR01-RV-06..10: time, custody failure domains, state catalogs, impact closure and verification traceability.
7. Independent security review and executable protocol tests before approval.

## 6. Final status

**REVIEW_REQUIRED / INTEGRATION_REVISE / NOT_APPROVED.**

The A02-AR-01 proposal is a meaningful design refinement but not an operational trust protocol. Five HIGH and five MEDIUM findings remain. No root keys, recovery authorities, witnesses, consumers or cryptographic protocols were deployed or tested. No executable tests or external security review were performed.
