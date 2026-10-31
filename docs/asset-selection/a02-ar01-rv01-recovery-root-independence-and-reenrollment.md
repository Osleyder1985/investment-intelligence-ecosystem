# A02-AR-01-RV-01 — Recovery-root independence, recovery-of-recovery and safe re-enrollment

**Document ID:** IIE-TRUST-RECOVERY-ROOT-001  
**Version:** 0.1.0-draft  
**Date:** 2026-10-31  
**Status:** PROPOSED / REVIEW_REQUIRED / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED  
**Parent:** A02-AR-01 trust bootstrap and anti-rollback protocol  
**Finding:** AR01-RV-01 — Recovery root bootstrap remains circular  
**Related:** X-07 lifecycle, X-08 governance, X-09 Scope Algebra, A02 trust model, A01 network identity/lineage.

## 1. Purpose

This document addresses the circular trust problem that arises when a normal root authority is compromised and the recovery authority is authenticated only by metadata controlled by that same root.

It defines a candidate model for:
- independently anchoring recovery commitments at initial enrollment;
- separating normal-root and recovery trust domains;
- authenticating recovery without signatures from the compromised root;
- recovering when recovery keys are lost, unavailable or compromised;
- handling consumers with intact, partial, reset or absent local state;
- defining a safe terminal outcome when no verifiable recovery path remains.

This is a documentation proposal. It does not appoint actual recovery authorities, establish legal powers, create root keys, select cryptographic algorithms, conduct a key ceremony or implement a recovery service.

## 2. Core security rule

**A recovery authority is trustworthy only to the extent that its authority and public-key commitments were independently established before the incident and remain verifiable without relying on the compromised authority.**

A recovery key listed in a trust snapshot signed only by the normal root is not independent recovery protection. A second key, device, cloud account, organization name or geographic location does not prove independence if custody, administration, authorization or recovery dependencies are shared.

The design must avoid an infinite chain of recovery authorities. It terminates in an independently verified enrollment commitment, or in a new external re-enrollment ceremony. It does not claim an unbounded cryptographic regress can be solved internally.

## 3. Trust domains and commitment model

### 3.1 Distinct trust domains

The deployment must model at least:
- **NormalRootDomain:** routine trust policy and snapshot succession.
- **RecoveryAuthorizationDomain:** emergency authority to suspend/replace the normal root under precommitted conditions.
- **EnrollmentCommitmentDomain:** externally verified initial commitment that binds both domains and their transition rules.
- **WitnessDomain:** independent observation/consistency evidence, if selected.
- **ConsumerLocalTrustRecord:** protected local record of accepted enrollment and recovery commitments.

These are logical domains. Their real custodians, legal authority and technical controls remain unassigned until X-08 and security approval.

### 3.2 Enrollment commitment

At initial enrollment, an immutable EnrollmentCommitment MUST bind:
- trust_domain_id and protocol/version;
- normal root public-key identity/fingerprint and allowed purposes;
- recovery authority public-key identities/fingerprints and allowed recovery purposes;
- recovery policy identifier/version and activation conditions;
- recovery threshold and distinct-principal rules, once approved;
- recovery key custody/failure-domain attestations;
- recovery-of-recovery and loss handling policy;
- independent verification channel/ceremony evidence;
- enrollment sequence/epoch and package digest;
- canonicalization/cryptographic profile identifiers;
- consumer enrollment evidence and witness references where required.

The EnrollmentCommitment is accepted only after an out-of-band verification ceremony independent of both normal distribution and the normal root's unilateral control.

### 3.3 Consumer pinning

A consumer MUST retain an independently protected commitment to the EnrollmentCommitment digest and recovery authority identities. The normal root may not replace or remove this recovery commitment through an ordinary snapshot.

A normal trust snapshot may reference the commitment but cannot redefine its trust root. Changes to recovery authorities require the recovery-transition procedure, not an ordinary root-signed policy update.

If a consumer has lost every independently verified enrollment commitment, it cannot safely infer which recovery authority is genuine from an untrusted network response. It must enter REENROLLMENT_REQUIRED or equivalent pending canonical status approval.

## 4. Independence requirements

### 4.1 Failure-domain assessment

Recovery independence MUST be assessed across dimensions, not inferred from different key material alone:

1. beneficial/legal control and organizational authority;
2. key custodians and approval principals;
3. identity provider and administrative directory;
4. hardware security module/secure element and firmware administration;
5. cloud, hosting, backup and network providers;
6. physical sites and disaster recovery locations;
7. privileged administrators, break-glass accounts and support channels;
8. software supply chain, signing infrastructure and build pipeline;
9. jurisdiction, coercion and legal compulsion exposure;
10. witness and publication channels;
11. incident response and recovery contacts;
12. funding, governance and operational dependency.

For each dimension, record ASSERTED, EVIDENCED, UNKNOWN, CONFLICTED or NOT_ASSESSED as candidate epistemic values, with evidence and time. These are not canonical statuses.

### 4.2 No false independence

The following do not establish independence by themselves:
- different key IDs controlled by the same custodian;
- different HSMs administered by the same privileged team;
- different cloud accounts under the same organization-wide administrator;
- different domains resolving to the same provider or shared DNS control;
- multiple witnesses funded, administered or operated by one party;
- separate signers authenticated through the same compromised identity provider;
- geographic separation without custody/control separation.

If a required failure domain is UNKNOWN, recovery independence cannot be declared fully established for a high-impact use.

### 4.3 Common-cause compromise

The threat model MUST explicitly model correlated compromise. A single compromise event may affect normal and recovery domains through shared administrators, software, identity providers, backup systems, coercion or supply chain.

The design must not treat independence as a scalar score unless a separately approved model defines its semantics and validation.

## 5. Recovery authorization without the compromised root

### 5.1 Recovery authorization package

A RecoveryAuthorizationPackage MUST bind:
- trust_domain_id;
- EnrollmentCommitment digest;
- affected normal root/key identifiers;
- incident identifier and classification;
- activation condition and supporting evidence;
- recovery policy version pinned at enrollment;
- recovery signer identities and key purposes;
- approvals, distinct-principal evidence and SoD evaluation;
- proposed recovery epoch and predecessor/high-water references;
- affected scope and intended activities;
- witness/publication requirements;
- consumer reconciliation instructions;
- effective time and time uncertainty;
- package digest and cryptographic profile.

The package MUST be verifiable directly against the consumer's pinned EnrollmentCommitment and recovery policy. It MUST NOT require a signature from a root that is declared compromised.

### 5.2 Activation conditions

The recovery policy must distinguish:
- confirmed normal-root compromise;
- suspected compromise with urgent containment;
- normal-root key loss/unavailability;
- normal-root policy equivocation;
- normal-root authorization failure;
- recovery-authority compromise;
- simultaneous or uncertain compromise.

Precautionary suspension may be permitted under a separately approved emergency policy with narrow scope and expiry/review controls. Permanent root replacement requires the precommitted recovery authorization threshold and evidence rules.

### 5.3 Predecessor-policy pinning

The recovery authority is governed by the recovery policy pinned in the EnrollmentCommitment and any later validly authorized recovery-policy transition. The normal root cannot weaken the recovery policy unilaterally.

A change to recovery authorities or thresholds requires:
1. authorization under the currently pinned recovery policy;
2. evidence of new authority independence;
3. explicit transition and effective interval;
4. independent verification and consumer pin update;
5. witness publication where required.

No transition may authorize itself solely under the policy it introduces.

## 6. Recovery-of-recovery

### 6.1 Recovery authority lifecycle

Recovery keys require their own lifecycle:
- enrollment and custody verification;
- rotation before expiry or upon operational change;
- suspension and revocation;
- suspected/confirmed compromise;
- loss/unavailability;
- succession and replacement;
- consumer pin update;
- audit and independent review.

Recovery keys are not permanent merely because they are offline or rarely used.

### 6.2 Recovery authority compromise

If a recovery key is compromised but the normal root remains trustworthy, the recovery policy may authorize recovery-key rotation using its currently valid transition process. If both normal and recovery authorities are compromised or cannot be trusted, the system MUST NOT choose a new authority from the affected trust metadata.

### 6.3 Recovery-of-recovery commitment

The initial EnrollmentCommitment MUST bind a bounded recovery-of-recovery procedure, including:
- independently verified successor commitment channels;
- eligible external custodians/authorities or a fresh ceremony requirement;
- evidence and activation criteria;
- consumer identity/re-enrollment rules;
- preservation of the previous commitment and incident record;
- how conflicting claims are adjudicated.

This is not a recursive list of infinite keys. It is a finite decision rule for when the existing trust chain is exhausted.

### 6.4 No safe internal recovery

If all precommitted recovery paths are compromised, unavailable, expired without valid successor, or impossible to authenticate, the only permitted outcome is **TRUST_DOMAIN_SUSPENDED / REENROLLMENT_REQUIRED** (candidate labels).

The system MUST:
- stop asserting current trust for affected domain and scopes;
- block high-impact downstream decisions that depend on it;
- preserve all prior snapshots and incident evidence;
- prohibit ad hoc key substitution or operator-selected roots;
- require a new independently governed enrollment ceremony;
- bind the new enrollment to the prior trust domain and incident history without inheriting trust automatically;
- reassess downstream claims and decisions after re-enrollment.

A new ceremony creates a new trust epoch or trust-domain generation under explicit migration semantics. It does not retroactively validate artifacts signed under compromised keys.

## 7. Consumer state classes and recovery

### 7.1 Consumer with intact pinned commitment

The consumer verifies RecoveryAuthorizationPackage directly against the pinned EnrollmentCommitment, checks sequence/epoch and witness requirements, records the new epoch atomically and preserves the old chain.

### 7.2 Consumer with partial local state

If the consumer retains the EnrollmentCommitment digest and required protected recovery references but lacks intermediate snapshots, it may reconstruct the chain from authenticated witnesses and the recovery package. It must verify predecessor and incident references. If required history cannot be reconstructed, it enters REVIEW_REQUIRED and cannot claim complete replay.

### 7.3 Consumer with reset or lost local state

A reset consumer must not accept a recovery package merely because it is signed by a presented recovery key. It must re-establish the original EnrollmentCommitment through an independent enrollment channel or a separately protected recovery record.

If neither exists, the consumer requires a new enrollment ceremony. It cannot trust a remote party to tell it which old commitment it once trusted.

### 7.4 Consumer offline during incident

An offline consumer cannot know that compromise or revocation occurred after its last trusted checkpoint. On reconnection it must obtain fresh authenticated recovery/revocation evidence and reconcile before current-use decisions. Until then, only historical verification at its last known cutoff is allowed.

### 7.5 Device replacement and cloning

Device replacement, virtual-machine cloning, backup restoration or secure-element replacement must not silently transfer or lower the consumer's high-water state. Enrollment/recovery procedures must bind consumer identity and anti-rollback assurance class, and record whether monotonic guarantees were preserved.

## 8. Re-enrollment ceremony

A new enrollment is required when no valid precommitted recovery path remains.

The ceremony MUST:
1. establish the new trust domain/generation identifier and explicit relationship to the suspended predecessor;
2. verify identity and authority of all new root/recovery principals through independent channels;
3. assess common-cause/failure-domain separation;
4. bind new root and recovery commitments, policy, cryptographic profiles and transition rules;
5. record why the former recovery path was unusable;
6. define treatment of historical signatures, snapshots, claims and decisions;
7. publish a signed/witnessed enrollment commitment through independent channels;
8. require consumers to verify and record the new commitment;
9. preserve dissent, failed attempts, unresolved scope and audit records;
10. undergo independent review before re-enabling high-impact uses.

The ceremony MUST NOT represent the new trust domain as a seamless continuation unless a separately approved migration proof establishes the exact continuity claim. New enrollment does not erase old trust failure.

## 9. State and decision semantics

Candidate recovery states:
- RECOVERY_AVAILABLE;
- RECOVERY_AUTHORIZATION_PENDING;
- RECOVERY_AUTHORIZED;
- RECOVERY_IN_PROGRESS;
- RECOVERY_COMPLETED;
- RECOVERY_FAILED;
- RECOVERY_AUTHORITY_COMPROMISED;
- TRUST_DOMAIN_SUSPENDED;
- REENROLLMENT_REQUIRED;
- REENROLLMENT_IN_PROGRESS;
- REENROLLED_WITH_LIMITATIONS.

These labels require R01/X-07 catalog crosswalk and approval. They must not be inserted into canonical catalogs or schemas by this document.

Trust status and consumer workflow status remain separate. For example, a consumer can be technically REENROLLED while downstream historical evidence remains INSUFFICIENT_INFORMATION or REVIEW_REQUIRED.

## 10. Scope and dependency handling

All recovery decisions bind:
- trust_domain_id and generation;
- subject/network/source scopes affected;
- purpose and activity;
- jurisdiction/environment where relevant;
- valid-time and knowledge-time intervals;
- dependency closure and unresolved edges;
- consumers and downstream decisions;
- exact Scope Algebra signature once approved.

A recovery event must not globally invalidate unrelated trust domains or assets by name similarity. Conversely, unresolved dependency closure cannot justify declaring downstream objects unaffected.

## 11. Evidence and audit artifacts

| Artifact | Required content |
|---|---|
| EnrollmentCommitment | Normal/recovery anchors, policy, channels, generation and recovery-of-recovery rule |
| IndependenceAssessment | Failure-domain graph, evidence, uncertainty and common-cause analysis |
| RecoveryAuthorizationPackage | Incident, activation, authority, scope, signatures and approvals |
| RecoveryKeyLifecycleRecord | Key enrollment, custody, rotation, suspension, compromise and succession |
| ConsumerRecoveryRecord | Pinned commitment, local state class, verification and resulting epoch |
| ReEnrollmentCeremonyRecord | Independent identity checks, approvals, channels, dissent and digest |
| TrustDomainMigrationRecord | Old/new generations, continuity claims, scope and proof |
| TrustImpactManifest | Key/signature/object/decision dependency closure and gaps |
| RecoveryReplayManifest | Policies, artifacts, time cutoffs, scopes and verifier versions |

Artifacts must be immutable/versioned and identify producer, method, input/output digest, time provenance, scope and review status. No artifact's presence proves that its process was performed.

## 12. Adversarial scenarios

| ID | Scenario | Required result |
|---|---|---|
| RR-01 | Recovery key exists only in normal-root-signed snapshot | Not independently trusted |
| RR-02 | EnrollmentCommitment pins normal and recovery anchors via independent ceremony | Eligible for enrollment after all gates |
| RR-03 | Normal root attempts to replace recovery key unilaterally | Reject transition |
| RR-04 | Different recovery keys share one custodian | Independence not established |
| RR-05 | Recovery HSM and normal HSM share privileged administrator | Common-cause dependency recorded; no assumed independence |
| RR-06 | Recovery identity provider compromised with normal root | Treat as correlated compromise |
| RR-07 | Root compromised, recovery commitment locally pinned | Verify recovery without compromised-root signature |
| RR-08 | Root and recovery key both compromised | Suspend; no internal ad hoc root |
| RR-09 | Recovery keys lost, normal root still trustworthy | Rotate under pinned recovery policy |
| RR-10 | Recovery keys lost and normal root compromised | New independent enrollment ceremony |
| RR-11 | Recovery key compromised, normal root healthy | Revoke/rotate through approved transition; reassess affected signatures |
| RR-12 | Both normal and recovery authorities unavailable | Trust domain suspended |
| RR-13 | Consumer retains commitment digest but not snapshot chain | Reconstruct only from authenticated witness/history; otherwise review |
| RR-14 | Consumer loses all pinned commitments | Re-enrollment required; do not trust presented recovery package |
| RR-15 | VM restored to stale state | Reconcile external monotonic/witness evidence or quarantine |
| RR-16 | Offline consumer reconnects after root compromise | Historical-only until fresh recovery/revocation reconciliation |
| RR-17 | New enrollment uses same compromised identity provider | Independence gate fails |
| RR-18 | New root is selected by repository maintainer alone | Reject; no authority |
| RR-19 | New enrollment claims seamless continuity without proof | Continuity claim rejected; new generation with limitation |
| RR-20 | Old signatures are automatically accepted under new root | Prohibited; historical trust reassessment required |
| RR-21 | Recovery policy changed by the snapshot it authorizes | Reject self-amendment |
| RR-22 | Recovery quorum uses multiple keys held by one principal | Distinct-principal threshold unsatisfied |
| RR-23 | Scope of compromised recovery key is unknown | Quarantine potentially affected trust scopes |
| RR-24 | One trust domain compromised; unrelated domain has separate commitment | Isolate if independence and no shared dependencies are evidenced |
| RR-25 | Re-enrollment ceremony has unresolved dissent or channel mismatch | Do not activate new trust domain |
| RR-26 | Recovery succeeds but dependency closure incomplete | Restore only proven unaffected scopes; disclose remaining quarantine |

## 13. Invariants

- INV-RR-01: Recovery authority is independently committed at initial enrollment.
- INV-RR-02: Normal root cannot unilaterally redefine recovery authority.
- INV-RR-03: Recovery authorization is verifiable without the compromised normal root.
- INV-RR-04: Different key IDs do not prove independent control.
- INV-RR-05: Recovery independence includes organizational, technical, physical and jurisdictional failure domains.
- INV-RR-06: Unknown required independence dimensions block full recovery assurance.
- INV-RR-07: Recovery policy transitions are authorized by the previously pinned recovery policy.
- INV-RR-08: Recovery-of-recovery terminates in a bounded procedure or new independent enrollment, not recursive trust assumptions.
- INV-RR-09: No ad hoc root substitution is permitted.
- INV-RR-10: If all precommitted recovery paths are unusable, affected trust domain remains suspended.
- INV-RR-11: Loss of consumer commitments cannot be repaired by accepting an untrusted presented key.
- INV-RR-12: New enrollment preserves predecessor and incident history.
- INV-RR-13: New enrollment does not retroactively validate old signatures.
- INV-RR-14: Consumer state classes have explicit recovery assurance and limitations.
- INV-RR-15: Offline consumers cannot claim current trust beyond last authenticated cutoff.
- INV-RR-16: Device replacement/restore cannot silently lower high-water state.
- INV-RR-17: Recovery outcomes bind exact scope and purpose.
- INV-RR-18: Unresolved dependency closure never becomes NOT_AFFECTED.
- INV-RR-19: Recovery events are append-only and replayable.
- INV-RR-20: Recovery authority, normal root, distribution and audit remain segregated.
- INV-RR-21: Trust-domain continuity is an explicit evidence-backed claim, not an inherited assumption.
- INV-RR-22: Re-enrollment requires independent review before high-impact use resumes.

## 14. Cross-document obligations

- **A02 parent:** Recovery authority and trust-domain commitments must reference one EnrollmentCommitment contract; do not maintain competing root registries.
- **A02-AR-01:** Replace the generic recovery authority paragraph only after this proposal is approved and integrated.
- **A01/NCP:** Network registry and checkpoint trust must consume the same trust-domain generation and recovery evidence.
- **X-07:** compromise, suspension, recovery, key lifecycle and re-enrollment are append-only lifecycle events with valid-time/knowledge-time.
- **X-08:** actual custodians, role IDs, authority, thresholds, delegation, SoD and emergency powers must be assigned and approved.
- **X-09:** recovery impact and continuity are bound to exact scope signatures and dependency closure.
- **R01:** candidate states need canonical catalog ownership and crosswalk before schema/code.

## 15. Approval and implementation gates

Before approval:
1. security threat model validates common-cause and simultaneous compromise assumptions;
2. independent specialists review enrollment and recovery ceremony;
3. X-08 assigns real authorities and approves SoD/delegation;
4. legal/compliance reviews authority, jurisdiction, retention and re-enrollment;
5. consumer classes and anti-rollback guarantees are specified;
6. recovery transitions and state catalog are reconciled with X-07/R01;
7. formal proof obligations and executable scenarios are mapped;
8. recovery runbooks and incident exercises are independently reviewed.

No algorithm, key size, HSM vendor, quorum, TTL or named institution is selected.

## 16. Status

**PROPOSED / REVIEW_REQUIRED / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED.**

This proposal addresses the circular recovery-root issue at the conceptual protocol level. It does not establish real authorities or cryptographic trust. If the precommitted recovery chain is exhausted, the safe result is suspension and independent re-enrollment, not an improvised root. The finding remains open pending adversarial review and external security/governance approval.
