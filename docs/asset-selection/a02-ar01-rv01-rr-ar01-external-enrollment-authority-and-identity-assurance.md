# RR-AR-01 — External enrollment authority and identity assurance

**Document ID:** IIE-TRUST-ENROLLMENT-AUTHORITY-001  
**Version:** 0.1.0-draft | **Date:** 2026-10-31  
**Parent:** IIE-TRUST-RECOVERY-ROOT-001  
**Status:** PROPOSED / REVIEW_REQUIRED / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED

## 1. Purpose and boundary

This proposal defines how a trust-domain enrollment or re-enrollment ceremony establishes the identity, legal/organizational authority and action-specific authorization of participants when the previous cryptographic trust chain is unavailable or compromised.

It does not appoint an institution, select a jurisdiction, validate any real identity, create keys, select cryptographic algorithms or implement a ceremony. It does not assume that a government registry, PKI, notary, auditor, custodian, repository owner or service provider is inherently trustworthy.

A valid identity credential alone does not establish organizational authority, permission for a particular action, technical validity of a ceremony, or independence from a compromised trust domain.

## 2. Core rule: non-circular bootstrap

A new enrollment MUST rely on evidence whose acceptance does not depend solely on the trust domain being enrolled, the compromised root, or an authority whose identity and authority are asserted only by that root.

There is no internal cryptographic solution if every trust anchor and every independent identity/authority reference has been lost or compromised. The system must then rely on a separately governed external assurance process or remain suspended. The external process itself has declared evidence, jurisdiction, limits, expiry, conflicts and review requirements; it is not an assumed root of truth.

## 3. Typed principal and authority model

Distinguish:
- **NaturalPerson:** identified human participant.
- **LegalEntity:** organization under a specified legal regime.
- **OrganizationalRole:** role held by a person in an entity, with effective interval and scope.
- **AuthorityInstrument:** resolution, delegation, mandate, appointment, contract or other evidence granting authority.
- **CredentialIssuer:** issuer of an identity credential.
- **EvidenceAttestor:** party attesting a specific fact or artifact.
- **CeremonyOperator:** conducts procedural steps; has no implied approval power.
- **IndependentReviewer:** reviewer independent of preparation and approval.
- **ConsumerPrincipal:** accountable human or service identity enrolling a consumer.

Every authority assertion MUST bind principal, verified identity reference, entity/jurisdiction, role, authority instrument, permitted actions, trust-domain generation, scope, effective interval, delegation chain, expiry, revocation check, evidence provenance and reviewer.

Repository ownership, cloud administration, domain registration, code contribution, key possession, seniority or infrastructure funding MUST NOT independently grant authority to establish or replace trust roots.

## 4. Candidate enrollment assurance classes

These classes are proposed labels, not a claim of compliance with an identity standard or a universal ranking.

| Class | Evidence basis | Permitted interpretation |
|---|---|---|
| EA-0 Unverified | Self-assertion or unchecked document/key | No trust enrollment |
| EA-1 Credential-verified | Identity credential checked with issuer/status source | Identity claim supported; authority not established |
| EA-2 Authority-verified | Identity plus current entity/role and action-specific authority instrument | Candidate authority for stated actions/scope |
| EA-3 Independently corroborated | EA-2 plus independent evidence channels, conflict checks, SoD and witnessed ceremony | Candidate high-assurance enrollment, subject to threat-specific gates |
| EA-4 Exceptional reconstitution | External identity/authority re-verification after trust-chain exhaustion, multi-party review | New trust generation may be established; continuity not presumed |

The ceremony MUST define minimum assurance by role and action. Root and recovery-authority enrollment MUST NOT rely on EA-0 or EA-1 alone. No numerical score may compensate for a missing mandatory element.

## 5. Evidence verification

### 5.1 Identity and authority

Accepted identity evidence may include issuer-verifiable credentials, approved in-person verification or credentials anchored in a pre-existing independent framework. Legal/entity authority evidence may include official registry extracts, current board/committee resolutions, signed delegations, powers of attorney, contracts or legally relevant appointments.

The verifier MUST check issuer, authenticity, current status, revocation, effective interval, jurisdiction, exact action and scope. A registry entry showing a director does not necessarily empower that person to perform every cryptographic or financial act. A copied document or image without issuer/status verification is insufficient by itself.

### 5.2 Integrity is not authenticity

For each artifact record issuer/attestor, source, digest, acquisition method, signature/seal validation, status checks, issue/effective/observed/verification times, jurisdiction, transformations, limitations and conflicts.

A digest establishes byte integrity relative to the digest, not issuer authenticity or truth. A valid signature establishes a key relationship, not legal authority or factual truth.

### 5.3 Privacy

Collect only identity data necessary to support required claims. Prefer protected evidence references and verification attestations over unnecessary document copies. Define access, retention, disclosure, deletion, legal hold and breach response before live enrollment. Unresolved conflict between privacy and audit obligations requires legal review, not silent evidence destruction.

## 6. Independent channel and failure-domain requirements

Candidate channels include issuer-verified digital credentials, official legal/entity registries, in-person verification, independent professional attestation, precommitted witnesses and direct entity confirmation through a separately established contact path.

Different URLs, domains, apps, providers or signer keys do not prove independence. Record ownership, administrators, identity providers, infrastructure, funding, jurisdiction, revocation and recovery dependencies.

For critical enrollment:
- identity and authority claims each require appropriate verification;
- no single compromised channel may unilaterally establish all critical claims;
- use at least two materially independent evidence paths unless an explicitly approved legal process provides an alternative;
- shared dependencies count as a common failure domain;
- unknown mandatory dependencies prevent a full-independence assertion.

This is not a rule that “two sources equal trust”. Two sources controlled by one principal or infrastructure remain one failure domain. Out-of-band contact details must be established independently of the applicant and compromised trust domain; contact information supplied solely in the request is not independent confirmation.

## 7. Roles and segregation of duties

Distinguish applicant, identity verifier, authority verifier, ceremony operator, root/recovery approvers, independent witness, independent reviewer and consumer enrollment verifier.

For critical enrollment, the applicant cannot approve their own identity or authority evidence; the ceremony operator cannot be the sole approver; and the person controlling evidence collection cannot be its sole reviewer. Actual role assignment, quorum, distinct-principal rules, delegation and emergency powers require X-08 approval. No actual authority is implied by this proposal.

## 8. Consumer-specific re-enrollment

Distinguish:
- **ConsumerIdentity:** stable logical identity of the consuming system or accountable owner.
- **EnrollmentInstance:** one device, installation, runtime or protected execution instance.
- **EnrollmentGeneration:** advancing generation under the approved anti-rollback mechanism.
- **ConsumerBindingRecord:** evidence linking consumer, instance, trust-domain generation and accepted commitment.

A consumer may have multiple instances only under explicit policy. A cloned instance is not automatically a legitimate second instance.

Every re-enrollment challenge MUST bind trust-domain ID/generation, ConsumerIdentity, EnrollmentInstance, fresh unpredictable nonce, requested action/scope, incident case, previous commitment reference if available, assurance class, anti-rollback capability and expiry. The response MUST bind the exact challenge and commitment. Replay across consumer, instance, generation or scope MUST fail.

If all local commitments are lost:
- unattended bootstrap from a presented remote key or website is prohibited;
- consumer ownership/control must be re-established through an independent channel;
- requester authority over the ConsumerIdentity must be verified;
- a new case and ConsumerBindingRecord are created;
- the old instance is marked revoked only with evidence; otherwise it is unreachable/unknown;
- no claim is made that the new instance is cryptographically identical to the lost one.

Software-only consumers must declare lower anti-rollback assurance. They cannot claim hardware-backed protection against local rollback, cloning or key extraction. High-impact use may be prohibited or require independent witness/high-water services.

## 9. Fail-closed enrollment procedure

1. Create immutable EnrollmentCase with unique ID, purpose and scope.
2. Validate protocol/evidence contract versions.
3. Verify applicant identity and exact authority.
4. Verify entity, role, authority instrument, jurisdiction and effective interval.
5. Verify approver and witness identities and authority.
6. Evaluate conflicts, SoD, delegation and common control.
7. Validate evidence authenticity, freshness, revocation and provenance.
8. Evaluate channel independence and shared failure domains.
9. Verify root/recovery commitments and recovery-of-recovery procedure.
10. Verify policy predecessor; reject self-authorizing transitions.
11. Bind generation, consumer, scope and challenge.
12. Obtain required approvals and independent witness attestations.
13. Produce canonical EnrollmentCommitment and ceremony evidence.
14. Independently verify package against approved policy.
15. Publish through approved channels and obtain consumer acknowledgements.
16. Activate only after all mandatory gates pass; otherwise remain suspended or activate only an explicitly authorized bounded scope.

Invalid contract, unresolved critical identity/authority conflict, missing mandatory evidence, SoD failure or unverified channel results in rejection, review or suspension—not implicit approval.

## 10. Disputes, revocation and appeals

Conflicting identity/authority claims create a disputed case; repository or technical ownership cannot select the winner. Preserve claims, evidence, timing, reviewers and dissent. Revocation of a credential, authority instrument or role triggers scoped dependency reassessment. New instruments do not rewrite past approvals. Appeals and restoration are handled by an independent authority defined under X-08, not by the applicant or sole approver.

## 11. Required artifacts

| Artifact | Minimum content |
|---|---|
| EnrollmentCase | ID, initiator, scope, purpose, protocol and status |
| PrincipalIdentityRecord | principal type, identity claims, issuer, evidence and validity |
| AuthorityVerificationRecord | entity, role, instrument, actions, scope, delegation and validity |
| ChannelIndependenceAssessment | channel graph, shared dependencies, evidence and unresolved edges |
| SoDAndConflictAssessment | roles, principal linkage, conflicts, recusals and controls |
| ConsumerBindingRecord | consumer, instance, generation, challenge and assurance |
| CeremonyTranscript | ordered events, participants, times, approvals, dissent and digests |
| EnrollmentCommitment | anchors, policies, generation, scope and recovery commitments |
| IndependentReviewRecord | reviewer independence, scope, findings and disposition |
| ReEnrollmentCase | predecessor incident, lost-state class, new binding and revocation references |

Artifact existence alone does not prove verification occurred. Outcomes and supporting evidence must be inspectable subject to privacy controls.

## 12. Adversarial scenarios

| ID | Attack/failure | Required outcome |
|---|---|---|
| EA-01 | Repository owner declares new root | Reject; no authority |
| EA-02 | Self-asserted identity and unchecked documents | EA-0; no enrollment |
| EA-03 | Valid identity credential, no authority instrument | Identity supported; authority unproven |
| EA-04 | Authority instrument excludes requested action | Reject out-of-scope action |
| EA-05 | Two channels share compromised identity provider | Independence not established |
| EA-06 | Contact number supplied only by applicant | Not independent |
| EA-07 | Required official registry unavailable | Review/suspend |
| EA-08 | Authority instrument expired/revoked | Reject current authorization |
| EA-09 | Person removed from organizational role | Reject role authority |
| EA-10 | Multiple keys controlled by one principal | SoD/quorum failure |
| EA-11 | Ceremony operator is sole approver | Reject critical ceremony |
| EA-12 | Witness shares undisclosed applicant control | Independence unresolved |
| EA-13 | Ceremony record replayed for another trust domain | Reject binding mismatch |
| EA-14 | Challenge replayed for another consumer instance | Reject nonce/instance mismatch |
| EA-15 | Consumer VM cloned | Detect duplicate or quarantine |
| EA-16 | Total local commitment loss; unattended bootstrap requested | Independent owner re-verification |
| EA-17 | Service consumer has no accountable owner | Remain suspended |
| EA-18 | Software-only consumer claims hardware anti-rollback | Reject assurance claim |
| EA-19 | Authority outside jurisdiction/scope | Reject or review |
| EA-20 | Evidence issuer key revoked after ceremony | Scoped impact assessment; preserve history |
| EA-21 | Official sources conflict | Dispute; no automatic winner |
| EA-22 | Applicant suppresses dissenting evidence | Reject incomplete package |
| EA-23 | Recovery policy authorizes itself | Reject self-authorization |
| EA-24 | Previous consumer instance unreachable | Mark unknown; do not infer safe/revoked |
| EA-25 | Privacy process destroys required audit evidence | Legal/retention review |
| EA-26 | Temporary suspension power used for permanent root replacement | Reject action/scope mismatch |
| EA-27 | Credential outside effective interval | Reject current authority |
| EA-28 | Reviewer participated in evidence preparation | Conflict; replace reviewer |
| EA-29 | New generation claims continuity by naming convention | Reject continuity claim |
| EA-30 | Shared channel dependency is unknown | No full-independence assertion |
| EA-31 | Request expands beyond approved consumer scope | Reject expansion |
| EA-32 | Authority instrument authentic, issuer authority disputed | Hold for adjudication |

## 13. Invariants

- INV-EA-01: Identity, authority, ceremony authorization, technical validity and independence are distinct.
- INV-EA-02: Repository, infrastructure or key ownership never implies enrollment authority.
- INV-EA-03: Authority is action-, scope-, jurisdiction- and time-bound.
- INV-EA-04: Identity credential alone never proves organizational authority.
- INV-EA-05: Signature/digest alone never proves legal authority or truth.
- INV-EA-06: No single channel unilaterally establishes all critical enrollment claims.
- INV-EA-07: Shared failure domains prohibit unsupported independence claims.
- INV-EA-08: Unknown mandatory dependencies prohibit full-assurance status.
- INV-EA-09: Critical enrollment roles obey approved SoD.
- INV-EA-10: Applicant cannot approve own identity/authority evidence.
- INV-EA-11: Enrollment binds trust generation, purpose and exact scope.
- INV-EA-12: Re-enrollment challenge is consumer/instance-specific and replay-resistant.
- INV-EA-13: Total local commitment loss requires independent owner/identity verification.
- INV-EA-14: No unattended bootstrap after total trust-state loss.
- INV-EA-15: Software-only consumers cannot claim hardware-backed anti-rollback.
- INV-EA-16: Disputed authority never becomes approved by silence.
- INV-EA-17: Revocation triggers scoped reassessment without rewriting history.
- INV-EA-18: Privacy minimization preserves legally required audit evidence.
- INV-EA-19: New generation does not inherit predecessor authorization without proof.
- INV-EA-20: Enrollment is replayable from versioned policy, evidence and ceremony artifacts.
- INV-EA-21: Mandatory gate failure yields reject/review/suspension, never PASS.
- INV-EA-22: Actual authorities and thresholds require governance approval.

## 14. Cross-document obligations

- **RV-01:** supplies external identity/authority and consumer re-enrollment procedure; does not replace EnrollmentCommitment.
- **A02-AR-01:** distribution binds ceremony evidence and snapshots to same trust-domain generation.
- **A02:** issuer/attestor trust is purpose-scoped and failure-domain assessed.
- **A01/NCP:** network registry/checkpoint trust consumes only approved enrollment generations and scopes.
- **X-07:** identity/authority changes, revocations and consumer resets are append-only lifecycle events with valid-time/knowledge-time.
- **X-08:** real authority assignments, SoD, emergency roles, appeals and restoration remain institutional decisions.
- **X-09:** ceremony and consumer assessments require exact scope and dependency closure.
- **R01:** candidate statuses require catalog ownership/crosswalk before schemas or implementation.

## 15. Approval gates

Before approval: jurisdiction-specific legal review; security review of channel independence/coercion/insider threats; X-08 assignment of accountable authorities/verifiers/witnesses/reviewers; privacy and retention assessment; approved assurance requirements per role/action; consumer anti-rollback model; executable proof/test mapping; independent tabletop exercise for root compromise and total local-state loss.

No real identity provider, institution, legal regime, cryptographic algorithm, key or quorum is selected.

## 16. Status

**PROPOSED / REVIEW_REQUIRED / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED.**

This proposal cannot manufacture trust when every independent reference has been lost. In that condition the system remains suspended until a new, independently governed identity/authority ceremony is completed. External adversarial, legal and security review remains required.
