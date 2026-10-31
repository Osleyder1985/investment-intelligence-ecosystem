# A02-AR-01 — Trust bootstrap, authenticated snapshot distribution and anti-rollback protocol

**Document ID:** IIE-TRUST-BOOTSTRAP-AR01-001  
**Version:** 0.1.0-draft  
**Date:** 2026-10-31  
**Status:** PROPOSED / REVIEW_REQUIRED / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED  
**Parent:** A02 source independence, evidence provenance and cryptographic trust contract  
**Related:** A01 network identity/lineage; NCP registry/checkpoint contract; X-07 lifecycle; X-08 governance; X-09 Scope Algebra; R01 canonical catalogs.

## 1. Purpose and boundaries

This document remediates A02-AR-01 by specifying a candidate protocol for:
1. initial trust enrollment;
2. trust configuration and snapshot structure;
3. authenticated approval and distribution;
4. monotonic version/sequence enforcement and rollback detection;
5. freshness and consistency checks;
6. offline and stateless consumer limitations;
7. root compromise, recovery and re-enrollment;
8. historical replay and audit.

It is a protocol proposal, not a deployed public-key infrastructure, root-of-trust implementation, operational key ceremony, legal mandate or security certification. No real root keys, custodians, trust anchors, cryptographic algorithms, quorum thresholds, snapshot TTLs or recovery authorities are selected here.

The protocol does not assume that Git hosting, a DNS name, TLS, a single cloud account, a repository maintainer, or a source registry is itself the trust root. These may distribute artifacts but cannot independently establish their authority.

## 2. Security objectives and threat assumptions

### 2.1 Objectives

A conforming implementation should be able to establish, for a defined consumer and evaluation cutoff:
- which trust configuration was accepted;
- who approved it and under what authority;
- which predecessor state it extends or explicitly replaces;
- whether its signatures and authorization satisfy the pinned policy;
- whether it is fresh enough for the requested operation;
- whether it is a rollback, fork, equivocation or conflicting branch;
- whether the consumer's anti-rollback state is intact;
- what recovery authority and procedure apply;
- which prior assessments depend on the trust state.

### 2.2 Threats

Assume an adversary may:
- compromise a distribution endpoint, DNS, network path or hosting provider;
- replay an old but correctly signed snapshot;
- suppress newer snapshots or present different snapshots to different consumers;
- compromise one or more signing keys;
- compromise an administrator or attempt collusion;
- reset consumer local storage or restore an old machine image;
- exploit clock rollback or unavailable time services;
- present a valid signature without the required approval/authorization;
- exploit inconsistent trust policy versions or algorithm downgrade;
- disrupt normal distribution during a root recovery event.

No protocol can guarantee detection of every rollback for a fully stateless, offline consumer without an external freshness or monotonicity signal. The system MUST state this limitation and abstain from high-impact use when required anti-rollback evidence is unavailable.

## 3. Trust hierarchy and separation of authority

### 3.1 Logical roles

The proposed hierarchy separates these roles:
- **Root Authority:** controls the trust-policy root and the authorized transition/recovery process.
- **Trust Configuration Authority:** prepares candidate trust snapshots and policy metadata.
- **Approval Authority:** approves a snapshot under the active threshold/role policy.
- **Distribution Service:** transports already approved artifacts; it has no authority to create or approve trust.
- **Consumer:** verifies and records accepted trust state.
- **Independent Witness:** observes or co-signs snapshot sequence/digest commitments to detect equivocation or suppression.
- **Recovery Authority:** activates a pre-established recovery path when normal root authority is unavailable or compromised.
- **Auditor:** independently reviews ceremonies, approvals, transitions and incidents.

These are logical capabilities, not assigned people or institutions. X-08 must approve actual role IDs, delegation, thresholds and segregation of duties.

### 3.2 Separation of duties

The same principal MUST NOT unilaterally:
- propose a trust-root change;
- approve that change;
- distribute it as authoritative;
- attest that consumers accepted it;
- adjudicate an incident caused by that change.

The exact incompatibility matrix and threshold are institutional decisions. A deployment MUST fail closed if the approved role policy cannot be satisfied.

### 3.3 Trust domains

At minimum, distinguish:
1. root authorization keys;
2. snapshot approval keys;
3. distribution transport credentials;
4. witness/log signing keys;
5. recovery keys;
6. source/evidence signing keys.

A key in one domain MUST NOT acquire authority in another domain merely because it can sign a syntactically valid artifact. Cross-domain authorization requires an explicit policy binding.

## 4. Initial trust enrollment (bootstrap ceremony)

### 4.1 Bootstrap package

Initial enrollment MUST use a bootstrap package that binds:
- protocol and trust-policy identifiers/versions;
- root authority identity and root public-key material or key references;
- permitted key purposes and namespaces;
- initial trust sequence and epoch;
- initial snapshot identifier and digest;
- approval policy identifier and version;
- recovery authority commitments and activation conditions;
- cryptographic profile identifiers;
- effective-time semantics;
- provenance and approval records;
- a unique enrollment ceremony identifier.

The bootstrap package MUST be immutable once accepted. Corrections create a new, explicitly linked enrollment/recovery event; they do not silently replace the original package.

### 4.2 Out-of-band verification

A new consumer MUST verify bootstrap authenticity using an independently governed channel or ceremony that is not solely controlled by the distribution channel carrying the package.

Acceptable mechanisms are to be selected by deployment threat model and may include:
- in-person or independently witnessed fingerprint comparison;
- separately controlled authenticated administrative channel;
- pre-provisioned hardware or operating-system trust store under documented custody;
- independently published commitments across separately administered channels;
- controlled offline enrollment with signed ceremony record.

These are candidate patterns, not automatically equivalent assurances. A deployment MUST document which channels are independent, how identities are verified, and what happens when channels disagree.

A QR code, copied fingerprint, repository file, TLS certificate or signed message delivered through the same compromised channel is not independent verification by itself.

### 4.3 Enrollment approvals and record

The ceremony MUST record:
- participants and their approved roles;
- identity/authorization verification evidence;
- devices and key custody controls at an appropriate security level;
- exact package digest and cryptographic profile;
- independent channel comparison results;
- dissent, discrepancy and abort events;
- start/end times with clock provenance and uncertainty;
- approval signatures and policy evaluation;
- consumer enrollment outcome;
- independent witness/audit references.

If a mandatory participant, approval or channel verification is missing or contradictory, enrollment status is NOT_COMPLETED or REVIEW_REQUIRED; no trust state is activated.

### 4.4 No self-bootstrap

A source registry, trust snapshot, evidence publisher or newly introduced key MUST NOT authorize itself as the root of trust. A self-signature may prove possession of a private key; it does not prove that the key is entitled to bootstrap trust.

## 5. Trust snapshot contract

### 5.1 Snapshot envelope

Each immutable TrustSnapshot MUST bind at least:
- trust_domain_id;
- snapshot_id and monotonically increasing sequence;
- epoch/generation identifier;
- protocol_version and trust_policy_version;
- predecessor_snapshot_id and predecessor_digest, except for explicitly defined genesis/recovery cases;
- canonicalization_profile and digest_profile;
- creation, proposed, approved, effective and expiry times where applicable;
- approval_policy_id/version;
- trust-anchor set and authorized key purposes/scopes;
- key lifecycle and delegation references;
- revocation and suspension state references;
- algorithm/profile policy;
- recovery policy reference;
- witness/log commitments;
- source/provenance and approval record references;
- exact payload digest;
- signatures and approval attestations.

The snapshot MUST be content-addressed over an explicitly defined canonical byte representation. The signature envelope MUST bind the snapshot type, trust domain, sequence, epoch, predecessor digest and purpose to prevent cross-domain or cross-protocol substitution.

### 5.2 Snapshot lifecycle

Proposed lifecycle:
- DRAFT: being prepared; no authority.
- PROPOSED: submitted for review; no authority.
- APPROVED: approval policy satisfied; not necessarily active.
- SCHEDULED: approved with a future effective condition/time.
- ACTIVE: accepted for new evaluations by a consumer.
- SUPERSEDED: replaced by a later accepted snapshot.
- SUSPENDED: temporarily unusable under an authorized incident action.
- REVOKED: explicitly withdrawn under policy.
- QUARANTINED: authenticity, authorization, consistency or compromise concern.
- REJECTED: failed validation/approval.

These are candidate lifecycle labels, not canonical approved statuses. A snapshot cannot become ACTIVE merely because it is downloadable or its signature verifies.

### 5.3 Approval policy

An ApprovalPolicy MUST specify:
- eligible approver roles and authority scope;
- minimum distinct principals and any threshold rule;
- conflict-of-interest and SoD constraints;
- key validity and revocation checks;
- required review evidence;
- witness requirements;
- emergency/expedited path, if any;
- policy version and effective interval.

No numerical quorum is prescribed. The threshold is a security/governance decision supported by threat modeling. A policy MUST account for independence of approvers: multiple keys controlled by one principal do not constitute multiple independent approvers.

### 5.4 Transition chain

A normal snapshot MUST extend the consumer's accepted state by referencing the exact predecessor ID and digest. A snapshot that skips, forks from, or replaces a predecessor requires an explicit transition type and separately authorized justification.

Proposed transition types:
- NORMAL_SUCCESSOR;
- KEY_ROTATION;
- POLICY_CHANGE;
- ANCHOR_ADDITION;
- ANCHOR_SUSPENSION;
- ANCHOR_REVOCATION;
- RECOVERY_EPOCH;
- TRUST_DOMAIN_MIGRATION.

A transition MUST state its affected scope, effective time, approvals, evidence and migration semantics. A new snapshot cannot silently reinterpret an old trust domain or rebind an existing key identity.

## 6. Distribution and consistency protocol

### 6.1 Distribution is transport, not authority

Distribution services MAY mirror snapshots across multiple endpoints. Consumers MUST verify snapshot contents, signatures, authorization, predecessor linkage and policy independently of transport security.

Multiple mirrors of one origin are not independent witnesses. Distribution diversity must be assessed by administrative, infrastructure and upstream failure domains.

### 6.2 Witness commitments and equivocation

Where the deployment threat model requires transparency, each approved snapshot SHOULD be committed to one or more independently administered append-only witness/log services.

A witness commitment SHOULD bind:
- trust_domain_id;
- sequence and epoch;
- snapshot digest;
- predecessor digest;
- observation time and clock provenance;
- witness identity and signature;
- inclusion/consistency proof references where a transparency-log design is selected.

Two different digests for the same trust_domain_id, epoch and sequence constitute an equivocation conflict unless an explicitly authorized fork/recovery protocol explains the divergence. Consumers MUST NOT choose whichever branch is newest or easiest to retrieve.

A witness log is not itself a root authority. Witness signatures establish witnessed commitments under their own keys and policies.

### 6.3 Consumer acceptance algorithm

A consumer MUST perform these checks in order, with a versioned deterministic implementation:

1. Parse the envelope under the declared schema/profile; reject malformed or ambiguous encodings.
2. Verify payload digest over exact canonical bytes.
3. Verify signature cryptography under the pinned cryptographic profile.
4. Verify signer identity, key lifecycle, authorization scope and approval roles.
5. Verify the approval threshold and SoD policy for this transition.
6. Verify trust_domain_id, epoch, sequence and predecessor binding.
7. Compare candidate sequence/digest against locally protected monotonic state.
8. Verify witness/log commitments where required.
9. Evaluate freshness and time uncertainty against the approved policy.
10. Verify no unresolved suspension, revocation, compromise or equivocation event affects the candidate.
11. Verify transition type and recovery authorization where applicable.
12. Atomically persist accepted snapshot ID/digest, sequence, epoch, policy versions and acceptance event.
13. Only then make the snapshot available to downstream evaluations.

Any mandatory check UNKNOWN, missing, conflicting or unavailable MUST NOT yield ACTIVE. The result should be a typed non-acceptance outcome (e.g., REVIEW_REQUIRED, QUARANTINED, STALE or INVALID) under an approved catalog; no new canonical vocabulary is approved here.

### 6.4 Atomic persistence and concurrency

Consumer state update MUST be atomic and compare-and-swap against the previously accepted sequence/digest. Concurrent candidates must be serialized. If two valid candidates race from the same predecessor, the consumer must detect a fork and quarantine both pending authorized adjudication; arrival order is not a valid tie-breaker.

A crash between verification and persistence MUST NOT expose the candidate as active. Recovery after crash must re-read durable state and replay acceptance checks.

## 7. Anti-rollback: stateful and stateless consumers

### 7.1 Stateful consumers

A stateful consumer MUST retain a monotonic acceptance record containing:
- trust_domain_id;
- highest accepted epoch/sequence;
- accepted snapshot ID/digest;
- predecessor chain/checkpoint;
- acceptance policy and verifier versions;
- last accepted time with clock provenance;
- witness checkpoint(s), if required;
- integrity protection and backup/recovery metadata.

Storage should use hardware-backed monotonic facilities where available and justified, or another independently protected mechanism. The selected mechanism and residual risks must be documented.

Restoring an old disk image or backup MUST NOT silently reset the high-water mark. If rollback of local state cannot be excluded, consumer trust status becomes REVIEW_REQUIRED/QUARANTINED until reconciled against an independent witness or authorized recovery procedure.

### 7.2 Stateless or ephemeral consumers

A stateless consumer cannot prove that a snapshot is the latest merely by validating its signature and predecessor chain. An attacker can present a complete, correctly signed historical chain.

Therefore, a stateless consumer MUST obtain an approved freshness/consistency signal, such as:
- a fresh authenticated witness checkpoint;
- a trusted online status service independent of the snapshot distributor;
- a challenge-response freshness protocol under a separately governed authority;
- a deployment-specific trusted monotonic hardware service.

The selected mechanism must be pinned in policy and replay manifest. If no approved freshness signal is available, the consumer may perform historical verification but MUST NOT claim current trust or authorize high-impact operations.

### 7.3 Offline consumers

An offline consumer can verify a previously pinned snapshot and its chain but cannot establish that no newer revocation or compromise event exists unless it has a sufficiently fresh authenticated checkpoint under policy.

Offline mode MUST expose the last known trust cutoff and limitations. It MUST NOT convert “no revocation event observed” into “no revocation exists.”

### 7.4 Freshness policy

Snapshot maximum age, witness checkpoint age, clock uncertainty and refresh cadence are deployment-specific. They MUST be explicitly approved for each use class. No universal TTL is defined.

If time is unavailable or uncertainty exceeds the approved bound, the consumer cannot claim freshness for current-use decisions. Historical replay may still be possible using the original pinned cutoff.

## 8. Root compromise and recovery

### 8.1 Precommitted recovery policy

Recovery authority and procedures MUST be established during initial bootstrap, before an incident. The policy MUST identify:
- recovery principals/roles and their independent custody;
- activation conditions and evidence;
- approval threshold and SoD;
- how normal root authority is suspended;
- recovery trust epoch and key material enrollment;
- authenticated distribution and witness requirements;
- consumer state reconciliation;
- affected artifact/assessment reassessment;
- post-incident audit and re-enrollment requirements.

A recovery policy that depends solely on the compromised root key is not a recovery mechanism.

### 8.2 Incident classification

Distinguish at least:
- suspected compromise;
- confirmed compromise with bounded interval;
- confirmed compromise with unknown interval;
- root key loss/unavailability without compromise;
- malicious/erroneous revocation;
- trust snapshot equivocation;
- distribution outage or suppression;
- recovery authority compromise.

Each class requires a policy-specific response. An allegation alone must not silently rewrite history; it may justify precautionary suspension/quarantine under approved authority.

### 8.3 Recovery sequence

Candidate recovery sequence:

1. **Detect and preserve:** record incident evidence, affected trust domain, keys, snapshots, consumers and knowledge time.
2. **Contain:** authorized incident authority suspends affected trust domain/keys and blocks new high-impact use. If authority cannot be established, consumers fail closed rather than self-promote an alternate root.
3. **Determine scope:** construct key→signature→snapshot→source/claim/decision dependency closure; mark incomplete closure explicitly.
4. **Authorize recovery:** recovery authority validates activation conditions, independent approvals and SoD.
5. **Create new epoch:** issue a RECOVERY_EPOCH snapshot with new root material, explicit predecessor/incident references and affected scope.
6. **Witness and distribute:** publish authenticated recovery commitments through independently governed channels.
7. **Reconcile consumers:** consumers verify recovery authorization and reconcile their local high-water mark; no blind reset or downgrade.
8. **Reassess dependencies:** evaluate historical signatures and affected records under AS_KNOWN_THEN and AS_KNOWN_NOW cutoffs.
9. **Restore selectively:** resume only scopes whose trust chain, dependencies and policy checks are complete.
10. **Audit and close:** independent review, incident report, residual-risk acceptance and controlled re-enrollment.

### 8.4 Unknown compromise interval

If the time of compromise is unknown, the affected key's signatures within the potentially compromised interval cannot be presumed valid or invalid solely from the key's later revocation. The affected trust assessments enter review/quarantine according to scope and purpose. A new root does not automatically rehabilitate historical signatures.

### 8.5 Recovery failure

If recovery authority is unavailable, conflicting, or itself compromised, the system remains suspended/quarantined for affected trust domains. No operator may invent a new root or bypass the precommitted recovery policy.

## 9. Trust state and historical replay

The protocol MUST preserve two distinct evaluation views:
- **AS_KNOWN_THEN:** trust state and revocation information available at the historical knowledge cutoff;
- **AS_KNOWN_NOW:** current corrected assessment using later-discovered evidence.

A later recovery, compromise report or root rotation creates new events and assessments. It does not mutate the historical snapshot, signature, approval or original decision record.

Replay MUST pin:
- bootstrap package and enrollment record;
- trust snapshot chain and transition types;
- approval policy and signatures;
- trust-anchor and key lifecycle events;
- witness checkpoints and consistency proofs;
- anti-rollback consumer state;
- freshness/time policy and time evidence;
- recovery policy/incident events;
- verifier and cryptographic profile versions;
- scope, purpose, activity and dependency closure.

If a required trust artifact or historical checkpoint is unavailable, replay is incomplete and must disclose that limitation.

## 10. Required proof and evidence artifacts

| Artifact | Required proof obligation |
|---|---|
| BootstrapPackage | Exact root, policy, initial epoch and recovery commitment binding |
| EnrollmentCeremonyRecord | Independent channel verification, authorized participants and approvals |
| TrustSnapshot | Canonical bytes, digest, signatures, predecessor and transition binding |
| ApprovalManifest | Threshold, distinct principals, role scope and SoD |
| WitnessCheckpoint | Snapshot digest/sequence observation and consistency evidence |
| ConsumerHighWaterRecord | Monotonic accepted sequence/digest and integrity protection |
| FreshnessAttestation | Freshness source, cutoff, clock uncertainty and policy |
| RecoveryAuthorization | Activation condition, independent recovery approvals and scope |
| RecoverySnapshot | New epoch, incident link, new anchors and migration semantics |
| ReconciliationRecord | Consumer old/new state, verification and exception disposition |
| TrustImpactManifest | Key-to-artifact-to-assessment dependency closure and unresolved gaps |
| ReplayManifest | All trust, time, scope, policy and verifier dependencies |

A proof artifact must identify its producer, method, version, input digest, output digest, scope, time and independent review status. A document's existence is not proof that a procedure was performed.

## 11. Failure outcomes and fail-closed behavior

The following are conceptual outcome classes pending canonical catalog approval:

| Condition | Required behavior |
|---|---|
| Bootstrap package not independently verified | Do not enroll |
| Approval threshold/SoD unsatisfied | Do not activate |
| Snapshot digest/signature invalid | Reject and preserve incident evidence |
| Unknown signer authorization | Review/quarantine; no activation |
| Sequence lower than protected high-water mark | Reject rollback |
| Same sequence with different digest | Equivocation; quarantine trust domain |
| Predecessor mismatch | Reject or require explicit authorized transition |
| Stale witness/freshness signal | No current-trust claim |
| Stateless/offline without freshness evidence | Historical verification only |
| Local monotonic state suspected rolled back | Quarantine pending reconciliation |
| Root compromise suspected | Suspend/quarantine according to incident authority |
| Recovery authority unresolved | Remain suspended |
| Dependency closure incomplete | Do not claim all affected artifacts identified |
| Replay dependency unavailable | Replay incomplete |

No fail-closed outcome may be silently converted into a favorable trust assessment by downstream selection or operational routing.

## 12. Adversarial acceptance scenarios

These scenarios are proposed specifications, not executable tests.

| ID | Scenario | Required outcome |
|---|---|---|
| AR01-01 | First snapshot delivered only through the source being trusted | Bootstrap not verified |
| AR01-02 | Correct root fingerprint delivered through two channels with common administrator | Channels not assumed independent |
| AR01-03 | Bootstrap signatures valid but participant role unauthorized | Enrollment rejected |
| AR01-04 | Snapshot is validly signed but approval threshold not met | Not active |
| AR01-05 | One person controls multiple approver keys | Count as one principal for independence/SoD |
| AR01-06 | Distribution mirror substitutes older signed snapshot | Stateful consumer detects sequence rollback |
| AR01-07 | Stateless consumer receives complete old signed chain | Cannot claim latest/current without freshness witness |
| AR01-08 | Consumer disk restored from stale backup | Reconcile high-water mark; quarantine if uncertain |
| AR01-09 | Same sequence and epoch presented with two digests | Equivocation; quarantine both branches |
| AR01-10 | Snapshot skips predecessor without recovery transition | Reject |
| AR01-11 | Distribution endpoint unavailable but independent witness current | Fetch from alternate mirror and verify same digest |
| AR01-12 | Witness checkpoint stale beyond approved policy | No current-trust claim |
| AR01-13 | Clock moves backward | Do not infer freshness from local clock |
| AR01-14 | Snapshot uses deprecated profile | Apply pinned historical/current algorithm policy; no silent downgrade |
| AR01-15 | Valid signature but missing approval manifest | Do not activate |
| AR01-16 | Root compromise suspected but not confirmed | Preserve evidence; precautionary suspension only via authorized policy |
| AR01-17 | Root compromise confirmed, interval bounded | Apply scope/time-specific impact closure |
| AR01-18 | Root compromise confirmed, interval unknown | Quarantine potentially affected signatures |
| AR01-19 | Recovery policy references compromised root only | Recovery invalid; remain suspended |
| AR01-20 | Recovery keys are held by same administrator as root keys | Independence/SoD requirement fails |
| AR01-21 | Recovery snapshot attempts to reset sequence to zero | Reject rollback; use authorized new epoch semantics |
| AR01-22 | Recovery authority quorum unavailable | Remain suspended; no ad hoc root |
| AR01-23 | Recovery snapshot is valid but not witnessed where policy requires | Not active |
| AR01-24 | Consumer accepts recovery snapshot before atomic state persistence | Prohibited; crash-safe acceptance required |
| AR01-25 | Old consumer reconnects after months offline | Reconcile against fresh authenticated checkpoint |
| AR01-26 | Two consumers receive different same-sequence snapshots | Detect equivocation via witness/gossip consistency |
| AR01-27 | Trust snapshot approval policy changed by same snapshot being approved | Evaluate under predecessor-authorized transition rules |
| AR01-28 | New root added but old root not explicitly suspended/revoked | Ambiguous transition; review, not silent overlap |
| AR01-29 | Trust domain migrated to a new namespace | Explicit TRUST_DOMAIN_MIGRATION and consumer re-enrollment |
| AR01-30 | Recovery event arrives late | Preserve AS_KNOWN_THEN; create AS_KNOWN_NOW reassessment |
| AR01-31 | Local monotonic storage unavailable | Apply approved lower-assurance mode or abstain; no assumed anti-rollback |
| AR01-32 | Witness and root authority disagree | Quarantine pending independent adjudication |
| AR01-33 | Same trust package has different canonical byte encodings | Reject canonicalization ambiguity |
| AR01-34 | A trust snapshot is valid but outside consumer's authorized scope | Do not activate for that purpose |
| AR01-35 | Recovery succeeds but key-to-artifact dependency closure is incomplete | Restore only proven unaffected scopes; affected scopes remain review/quarantine |

## 13. Invariants

- INV-AR01-01: No trust anchor self-authorizes its initial enrollment.
- INV-AR01-02: Distribution transport is not trust authority.
- INV-AR01-03: Bootstrap binds root, policy, epoch, recovery and cryptographic profiles.
- INV-AR01-04: Bootstrap authenticity is checked through an independently governed channel/ceremony.
- INV-AR01-05: Trust snapshots are immutable and content-addressed.
- INV-AR01-06: Snapshot signatures bind trust domain, epoch, sequence, predecessor, object type and purpose.
- INV-AR01-07: Snapshot activation requires predecessor-authorized approval policy.
- INV-AR01-08: Approver key count does not equal independent principal count.
- INV-AR01-09: Stateful consumers enforce monotonic sequence/digest acceptance.
- INV-AR01-10: Stateless consumers cannot claim latest state without independent freshness evidence.
- INV-AR01-11: Offline absence of revocation evidence is not proof of no revocation.
- INV-AR01-12: Same sequence/epoch with distinct digests is equivocation absent authorized fork.
- INV-AR01-13: Concurrent acceptance is atomic and serialized.
- INV-AR01-14: Local state rollback cannot silently reset trust high-water mark.
- INV-AR01-15: Freshness is policy-specific and time uncertainty is explicit.
- INV-AR01-16: Recovery authority is precommitted and independent from normal root control.
- INV-AR01-17: Recovery does not rely solely on compromised root material.
- INV-AR01-18: Recovery cannot silently lower sequence or erase history.
- INV-AR01-19: Unknown compromise interval cannot produce automatic favorable signature trust.
- INV-AR01-20: Historical AS_KNOWN_THEN assessments remain immutable.
- INV-AR01-21: Recovery impact closure is explicit; incomplete closure remains visible.
- INV-AR01-22: No trust snapshot is ACTIVE solely because it is signed or downloadable.
- INV-AR01-23: Trust policy changes require explicit versioned transition.
- INV-AR01-24: A trust state cannot be used outside its authorized scope/purpose.
- INV-AR01-25: Missing required witness/freshness evidence blocks current-trust claims.
- INV-AR01-26: Recovery/approval/distribution/audit responsibilities remain segregated.
- INV-AR01-27: A recovery failure leaves affected scope suspended/quarantined.
- INV-AR01-28: Every acceptance and rejection is auditable and replayable.

## 14. Cross-document integration

| Contract | Required binding |
|---|---|
| A02 §5.4 | Replace generic bootstrap paragraph with this protocol by reference only after approval |
| A02 §5.5–5.6 | Key events and revocation are bound to snapshot sequence, valid-time and knowledge-time |
| A02 §10 | Replay pins bootstrap, snapshot chain, witness and consumer high-water records |
| A02 §12 | Real root/recovery roles and thresholds require X-08 approval |
| A01 network identity | Registry trust snapshots and lineage authorities use the same trust-domain bootstrap |
| NCP registry/checkpoint | Registry/checkpoint source trust consumes this snapshot protocol; no independent roots |
| X-07 | Suspension, compromise, recovery and invalidation are append-only lifecycle events |
| X-09 Scope Algebra | Trust assessments and recovery impact are bound to exact subject, purpose, activity and atomic scope |
| R01 catalogs | Lifecycle and trust outcomes need canonical catalog crosswalk before implementation |

This document does not edit parent A02 or any canonical contract. Integration is proposed only.

## 15. Verification obligations before approval

A specialist review must establish:
1. threat model covers state rollback, snapshot equivocation, root compromise, distribution suppression and recovery authority compromise;
2. bootstrap ceremony has independent identity and channel verification;
3. approval policy is resistant to single-principal control and collusion assumptions are explicit;
4. trust snapshot canonicalization/signature binding is unambiguous;
5. stateful anti-rollback storage threat and reset behavior are documented;
6. stateless/offline limitations are correctly enforced;
7. witness model detects same-sequence equivocation and stale views under its assumptions;
8. recovery authority is independent and operable when root is compromised;
9. consumer acceptance is atomic under crash/concurrency;
10. key-to-artifact dependency closure is complete or explicitly incomplete;
11. all 35 scenarios map to proof obligations, fixtures and test classes;
12. independent implementations produce the same accept/reject/quarantine result for the same pinned inputs.

No algorithm choice, numerical threshold, key size, TTL, hardware module or vendor is approved by this document.

## 16. Approval gates and unresolved decisions

Before implementation:
- X-08 must assign real root, approval, witness, recovery and audit authorities and approve SoD/delegation.
- Security must select threat model, cryptographic profiles, trust snapshot encoding, witness/log design and anti-rollback mechanism.
- Operations must define consumer classes (stateful, stateless, offline), availability objectives and recovery runbooks.
- Legal/compliance must review jurisdiction, record retention and authority obligations.
- Architecture must approve trust-domain identifiers, transition semantics, state catalogs and producer/consumer interfaces.
- Independent review must assess the recovery process and test evidence.

Unresolved decisions include initial enrollment channel, root custody technology, number/independence of approvers, witness topology, freshness policy, secure monotonic storage, recovery authority availability, algorithm profiles, and trust-domain migration rules.

## 17. Status

**PROPOSED / REVIEW_REQUIRED / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED.**

This document specifies a candidate trust bootstrap and anti-rollback protocol. It does not create or operate trust anchors, keys, witnesses, recovery authorities or consumer enforcement. No production cryptographic protocol has been selected, no real key ceremony has occurred, and no implementation or executable tests were added. A02-AR-01 remains open pending adversarial review and X-08/security approval.
