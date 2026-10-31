# A02-AR-01-RV-01 — Adversarial review: recovery-root independence and re-enrollment

**Review ID:** IIE-TRUST-RECOVERY-ROOT-REVIEW-001  
**Version:** 0.1.0-draft  
**Date:** 2026-10-31  
**Target:** `a02-ar01-rv01-recovery-root-independence-and-reenrollment.md` v0.1.0-draft  
**Review type:** Static adversarial and cross-document review  
**Status:** REVIEW_REQUIRED / INTEGRATION_REVISE / NOT_APPROVED  
**Independence:** Internal design review; not external security, legal or cryptographic assurance.

## 1. Determination

The remediation directly addresses the original circularity: recovery commitments are pinned through an independently verified enrollment ceremony; ordinary root snapshots cannot unilaterally redefine recovery authority; recovery authorization can be checked without the compromised normal root; and exhaustion of all precommitted recovery paths leads to suspension and a new independent enrollment rather than ad hoc key substitution.

The proposal also improves treatment of correlated custody failures, consumers with lost local state, recovery-key lifecycle, bounded recovery-of-recovery and historical trust preservation.

However, it is not ready for approval or implementation. It relies on an independently governed enrollment channel without defining how the human/institutional identity and authority of that channel are established. It also lacks a formal failure-domain closure proof, exact consumer re-enrollment binding, and deterministic migration semantics. The document is therefore a conceptual remediation, not a deployable recovery protocol.

**Disposition: REVIEW_REQUIRED / INTEGRATION_REVISE / NOT_APPROVED.**

## 2. Findings summary

| ID | Severity | Finding |
|---|---|---|
| RR-AR-01 | HIGH | Independent enrollment authority and out-of-band identity verification remain a trust bootstrap dependency |
| RR-AR-02 | HIGH | Recovery independence/failure-domain assessment has no formal graph closure and common-cause proof |
| RR-AR-03 | HIGH | Consumer re-enrollment and device identity binding are not sufficiently specified against cloning, theft and split-brain |
| RR-AR-04 | HIGH | Trust-domain generation/migration and historical artifact acceptance semantics are incomplete |
| RR-AR-05 | MEDIUM | Emergency suspension authority and disputed recovery claims need deterministic precedence and appeal rules |
| RR-AR-06 | MEDIUM | Recovery-key succession and partial quorum loss need lifecycle transition semantics |
| RR-AR-07 | MEDIUM | Re-enrollment evidence retention, privacy and legal hold need policy ownership |
| RR-AR-08 | MEDIUM | 26 scenarios and 22 invariants lack proof-obligation/test traceability |

**Count:** 4 HIGH, 4 MEDIUM, 0 LOW.

## 3. Detailed findings

### RR-AR-01 — Independent enrollment authority remains a trust bootstrap dependency

**Severity:** HIGH

The document requires an independently governed enrollment channel or ceremony, but does not define the authority that authenticates the people/institutions conducting that ceremony, how that authority's credentials are established, or how a consumer verifies the ceremony record if every prior local commitment has been lost.

**Adversarial case:** The normal root and recovery authority are compromised. A malicious party publishes a plausible new enrollment ceremony through an apparently independent channel. Consumers that lost their local commitment have no authenticated reference to distinguish the legitimate ceremony from an attacker-created one.

**Required remediation:**
- define enrollment assurance classes and their externally governed identity/authority roots;
- specify the minimum independent channel combinations and their failure-domain independence;
- define a controlled in-person or legally accountable identity verification route where cryptographic continuity is impossible;
- define consumer-specific re-enrollment challenge/authorization and prevent replay to another consumer;
- state explicitly that a consumer with no authenticated prior commitment may require human/institutional re-verification, not unattended network bootstrap.

### RR-AR-02 — Failure-domain graph lacks formal closure and proof

**Severity:** HIGH

The document lists dimensions and candidate epistemic states but does not define a typed graph contract, required edge set, transitive dependency closure, evidence sufficiency or proof of no common control. Without a formal model, two reviewers can produce incompatible independence conclusions.

**Adversarial case:** Recovery authority uses a different HSM vendor and legal entity but both are administered by a shared identity provider and the same emergency account. The review marks them independent because the listed organizations differ; an attacker compromises the shared identity plane.

**Required remediation:**
- define typed nodes/edges for principals, organizations, custodians, identity providers, hardware, software, facilities, jurisdiction and backup;
- bind edge evidence, valid-time/knowledge-time, confidence and unresolved status;
- define mandatory dimensions by threat model and exact scope;
- define transitive closure and UNKNOWN propagation;
- define common-cause predicates and proof obligations;
- prohibit a categorical INDEPENDENT result when required dependency closure is incomplete.

### RR-AR-03 — Consumer re-enrollment/device binding incomplete

**Severity:** HIGH

The document describes intact, partial and lost state, but not a precise consumer identity lifecycle. It does not define how a device proves it is the same enrolled consumer, how cloned devices are detected, or how simultaneous re-enrollment requests are serialized.

**Adversarial case:** An attacker clones a virtual machine and presents a legitimate recovery package. Both original and clone re-enroll under the same consumer identifier with divergent high-water state. No device attestation, challenge nonce, generation counter or revocation process distinguishes them.

**Required remediation:**
- define ConsumerIdentity and EnrollmentInstance separately;
- define challenge-response and anti-replay binding to consumer identity, enrollment generation, nonce and recovery package;
- define device/secure-element replacement and cloning limitations;
- define concurrent enrollment conflict and consumer revocation;
- classify software-only consumers as lower assurance where hardware identity cannot be verified.

### RR-AR-04 — Trust-domain migration and historical artifact acceptance incomplete

**Severity:** HIGH

The document says a new trust epoch/generation does not retroactively validate old signatures, but does not define whether and how historical records may be accepted for non-current analytical replay, what evidence can be re-adjudicated, and how cross-generation references are typed.

**Adversarial case:** New enrollment succeeds after root compromise. An old signed network registry record is needed to reconstruct a past asset identity. A downstream consumer treats the new trust domain as having inherited old trust, or rejects all historical evidence and destroys reproducibility.

**Required remediation:**
- define TrustDomainGeneration and explicit predecessor/successor relation;
- define historical-only verification under AS_KNOWN_THEN and corrected AS_KNOWN_NOW;
- define re-adjudication status separate from cryptographic signature validity;
- prohibit implicit cross-generation authorization;
- define migration proof and evidence-preserving references.

### RR-AR-05 — Emergency suspension authority and disputed claims need deterministic precedence

**Severity:** MEDIUM

The document allows precautionary suspension under policy but does not specify how conflicting emergency claims, false alarms, malicious suspension requests or appeal/restore authority compose.

**Required remediation:** define temporary suspension trigger, authority, scope, expiry/review, conflicting request precedence, evidence preservation and independent restoration authority consistent with X-08.

### RR-AR-06 — Recovery-key succession/partial quorum loss incomplete

**Severity:** MEDIUM

Rotation is described, but the protocol does not define loss of some but not all recovery signers, quorum degradation, succession when a signer becomes unavailable, or the boundary between routine rotation and recovery authority reconstitution.

**Required remediation:** define threshold continuity, signer retirement, quorum failure, succession approvals and mandatory re-enrollment triggers.

### RR-AR-07 — Evidence retention/privacy/legal hold ownership incomplete

**Severity:** MEDIUM

The document requires preservation of ceremony and incident evidence but does not assign retention authority, privacy controls, legal hold, deletion/retention conflict or public disclosure boundaries.

**Required remediation:** define record classes, access, retention schedule, legal-hold authority, redaction and evidence integrity obligations with legal/privacy review.

### RR-AR-08 — Scenario/invariant verification traceability missing

**Severity:** MEDIUM

The 26 scenarios and 22 invariants are not mapped to protocol clauses, proof obligations, test classes, expected outputs or independent review evidence.

**Required remediation:** produce a traceability matrix distinguishing documented, fixture-defined, test-implemented, test-executed and independently reviewed states.

## 4. Cross-document integration

- **A02-AR-01:** recovery commitments must be anchored once in EnrollmentCommitment; no parallel root registry.
- **A02 parent:** source trust and key authorization must consume the same trust-domain generation.
- **A01/NCP:** network identity lineage and checkpoint evidence must preserve trust-generation references and cannot inherit authorization implicitly.
- **X-07:** root/recovery compromise, suspension, re-enrollment and migration are append-only lifecycle events with valid-time/knowledge-time.
- **X-08:** actual enrollment authorities, custodians, emergency roles, thresholds, delegation and appeal/restore authority remain unassigned/unapproved.
- **X-09:** consumer, authority and migration assessments require exact scope and dependency closure.
- **R01:** candidate recovery states require canonical catalog ownership and crosswalk before schemas or code.

## 5. Remediation sequence

1. RR-AR-01: external enrollment authority and identity verification.
2. RR-AR-02: formal failure-domain graph, closure and common-cause proof.
3. RR-AR-03: consumer identity, enrollment-instance binding and anti-cloning.
4. RR-AR-04: trust-domain generations, migration and historical acceptance.
5. RR-AR-05..08: emergency authority, signer succession, retention and verification traceability.
6. Independent security, legal and governance review before implementation.

## 6. Final status

**REVIEW_REQUIRED / INTEGRATION_REVISE / NOT_APPROVED.**

The remediation improves the design but does not close RR-AR-01. Four HIGH and four MEDIUM findings remain. No external enrollment authority, real custodian, consumer device, cryptographic protocol or legal identity was verified. No code, schemas or executable tests were added; no tests or external reviews were performed.
