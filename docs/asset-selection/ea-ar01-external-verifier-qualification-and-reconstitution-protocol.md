# EA-AR-01 — External verifier qualification and reconstitution protocol

**Document ID:** IIE-TRUST-VERIFIER-QUALIFICATION-001  
**Version:** 0.1.0-draft | **Date:** 2026-10-31  
**Parent finding:** EA-AR-01  
**Parent proposal:** IIE-TRUST-ENROLLMENT-AUTHORITY-001  
**Status:** PROPOSED / REVIEW_REQUIRED / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED

## 1. Purpose and boundary

This proposal specifies how a verifier or verification organization may be qualified to support exceptional trust-domain enrollment when the previous cryptographic trust chain is unavailable or compromised.

It does not select or endorse a real verifier, accreditation body, legal jurisdiction, identity provider, registry, trust framework, cryptographic suite or manual process. It does not assert that a government, professional association, auditor, notary, certificate authority or commercial provider is automatically trustworthy.

Qualification is claim-, role-, jurisdiction-, action-, scope- and time-specific. Qualification to verify a natural person's identity does not imply qualification to interpret corporate authority, approve a root replacement, attest cryptographic evidence or authorize a trust-domain generation.

The proposal distinguishes verifier identity, legal/organizational status, competence, authorization, independence and the bounded verification result. No verifier qualification creates authority to appoint itself, amend its own qualification policy or unilaterally approve the trust generation it verifies.

## 2. Threat model and non-circular trust bootstrap

Threats include fabricated verifier organizations, impersonated staff, fraudulent accreditation claims, applicant/verifier/issuer collusion, common ownership or infrastructure compromise, coercion, bribery, conflicts of interest, revoked credentials, stale directories, DNS/domain takeover, split-view status information, compromised signing keys and emergency pressure to accept the first available verifier.

A verifier cannot be accepted solely because it presents a key, certificate, website, accreditation logo or signed statement whose trust chain terminates in the compromised trust domain.

The consumer or ceremony authority requires a separately governed **Verifier Qualification Reference (VQR)**. A VQR is a versioned evidence package identifying:
1. verifier legal/person identity;
2. authority that recognizes or appoints it for the stated task;
3. applicable jurisdiction and scope;
4. qualification criteria and supporting evidence;
5. validity interval and current status;
6. revocation, suspension and dispute channels;
7. independent discovery and verification paths;
8. dependencies and known common failure domains;
9. policy and trust-generation context in which it may be relied upon.

A VQR is not self-authenticating. Its issuer, publication channel and authority require verification. If no independent reference can be authenticated, qualification remains UNKNOWN and enrollment stays suspended.

There may be no globally accepted verifier or accreditation hierarchy. The system must permit jurisdiction- and task-specific qualification profiles without converting local recognition into universal authority.

## 3. Typed verifier roles

| Role | Permitted claim domain | Explicit non-authority |
|---|---|---|
| IdentityVerifier | Identity attributes of natural persons or organizations | Does not establish organizational action authority |
| EntityStatusVerifier | Legal entity existence/status under a named regime | Does not interpret every internal delegation |
| AuthorityInstrumentVerifier | Authenticity, status and scope of a named instrument | Does not decide disputed law outside approved competence |
| RoleVerifier | Current appointment/role and effective interval | Does not infer powers beyond the instrument |
| CredentialStatusVerifier | Issuer, signature, validity, suspension/revocation status | Does not establish truth of all credential claims |
| EvidenceCustodian | Chain of custody, acquisition, integrity and retention | Does not establish substantive truth |
| LegalAuthorityReviewer | Jurisdiction-specific legal interpretation within documented mandate | Does not exercise corporate authority for applicant |
| CeremonyWitness | Presence/order/attestations of specified ceremony steps | Does not approve trust policy or root by default |
| CryptographicVerifier | Verification against a specified approved cryptographic profile | Does not establish legal identity or authority |
| IndependentAssuranceReviewer | Review of process, evidence and control design | Does not replace required approval authority |

Combining roles requires explicit competence, independence and segregation-of-duties analysis. Role names are not evidence of qualification.

## 4. VerifierQualificationProfile

Each task requires a versioned VerifierQualificationProfile (VQP) containing:
- profile identifier, version and accountable owner;
- verifier role and precise claim types;
- permitted actions, purpose, scope and jurisdiction;
- required identity and legal-entity evidence;
- competence, training, accreditation or demonstrated capability;
- accepted credential issuers and status-check method;
- evidence provenance and chain-of-custody controls;
- independence and conflict-of-interest criteria;
- accountability, professional liability or insurance where applicable;
- security, privacy, retention and incident-reporting requirements;
- reviewer/witness separation;
- freshness and requalification triggers;
- suspension/revocation conditions;
- assessment procedure, decision authority and appeal path;
- limitations, prohibited uses and residual risks.

A profile MUST specify mandatory predicates, not merely a weighted score. A missing mandatory predicate cannot be offset by reputation, tenure, certificates or other positive attributes. Profiles must be approved before the case is opened or evidence evaluated; they cannot be relaxed retroactively to make a failed case pass.

## 5. Qualification evidence and verification

### 5.1 Verifier identity and accountable entity

For an organization, record legal name and stable registry identifier under a named jurisdiction, current legal status, accountable operating entity, controlling/parent entities where lawfully verifiable, accountable officers, authorized service representatives, service locations and relevant subcontractors.

For an individual, record identity evidence and issuer/status verification, professional status and permitted scope where relevant, engagement relationship, delegation to sign/attest, effective interval and suspension/revocation status.

A trade name, website, domain, logo, professional title or self-issued certificate is not sufficient proof of legal identity or authority.

### 5.2 Recognition, accreditation and competence

Where a task is regulated or a jurisdiction recognizes designated professionals/bodies, verify recognition directly with the competent issuing authority or an independently authenticated registry.

Where no formal accreditation exists, require an approved alternative competence profile, such as demonstrated task-specific experience, documented methods and quality controls, independent competence assessment, verified professional references, controlled demonstration, incident/complaint history and peer review.

Absence of formal accreditation is not automatically disqualifying, but it must be disclosed. The policy owner must explicitly approve the alternative basis and limitations.

### 5.3 Credential and status checks

For each credential or qualification claim:
- validate issuer identity and authority;
- validate signature/seal against a separately trusted profile;
- query current status through an authenticated channel;
- record verification time, response, status source and evidence digest;
- record policy/profile and verifier-software versions;
- retain sufficient evidence to replay the decision;
- distinguish VALID, EXPIRED, REVOKED, SUSPENDED, UNKNOWN, CONFLICTED and NOT_ASSESSED as proposed separate outcomes.

A timeout or unavailable status service is UNKNOWN, not VALID. Cached status is usable only within an approved freshness window with maximum staleness recorded.

### 5.4 Scope and jurisdiction

Qualification MUST identify exact jurisdiction, law/regime, subject types, claim types, actions and limitations. Recognition in one jurisdiction does not automatically transfer to another. Cross-border reliance requires an approved legal recognition basis or remains REVIEW_REQUIRED.

## 6. Independent discovery and authentication

Discovery through a directory, registry, professional body, legal counsel or counterparty only locates a candidate; it does not authenticate or authorize that candidate.

The VQR records discovery source, source operator/issuer, retrieval path, digest/version/time, independent confirmation route and the source's own trust dependencies.

For critical root/recovery ceremonies, verifier identity and authority require at least two evidence paths without a critical shared single point of failure, unless an approved jurisdiction-specific legal mechanism provides equivalent assurance. Potential paths include direct regulator/registry confirmation through separately established contact details, independent entity registry records, confirmation with the appointing entity, relevant professional/court records or pre-approved in-person verification.

Two URLs, certificates, subsidiaries or staff under common control are not independent paths. Independence assessment uses the A02 typed failure-domain graph and transitive dependencies. Unknown critical ownership, control, issuer, infrastructure or revocation dependencies prevent a full-independence result. A missing edge in an incomplete graph is not proof that no dependency exists.

## 7. Independence, conflicts and collusion controls

Assess beneficial ownership/control, shared directors/officers/staff/contractors, identity providers, certificate authorities, cloud, DNS, hosting, communications, common funding, material client concentration, contingent fees, applicant relationships, shared counsel/auditors/subcontractors, political/family/employment/financial relationships and common incident response/recovery/key custody.

Distinguish independent legal entities, operational control, evidence sources, technical infrastructure and decision makers. These are separate dimensions, not a single Boolean based on corporate names.

Conflicts are disclosed and either mitigated by an approved control or disqualify the verifier for that case. A verifier cannot assess its own independence or approve its own conflict waiver.

## 8. Verifier lifecycle

Proposed lifecycle dimensions remain separate:
- QualificationWorkflow: NOT_STARTED, IN_PROGRESS, REVIEW_REQUIRED, COMPLETE.
- QualificationDisposition: QUALIFIED, QUALIFIED_WITH_LIMITATIONS, NOT_QUALIFIED, SUSPENDED, REVOKED, UNKNOWN, NOT_ASSESSED.
- RecordLifecycle: ACTIVE, SUPERSEDED, INVALIDATED, WITHDRAWN.
- Freshness: FRESH, APPROACHING_EXPIRY, STALE, NOT_APPLICABLE.
- ContractValidity: VALID or INVALID under the canonical contract layer.

These vocabularies are candidates only; R01 catalog ownership and crosswalk are required before schemas or code.

Reassessment triggers include credential/accreditation expiry or revocation; changes in legal entity, ownership, control, officers, jurisdiction, service scope, subcontractors or infrastructure; compromise, fraud allegation or material complaint; undisclosed conflict; VQP/policy change; trust-root compromise; and status endpoint unavailability beyond approved tolerance.

Suspension blocks new reliance for affected scopes. Historical attestations are not erased; impact is assessed using valid time, knowledge time, dependencies and exact claims previously supported.

## 9. Case-specific verifier authorization

A qualified verifier is not automatically authorized for every case. Each case authorization binds verifier and individual signers, VQP and qualification evidence versions, trust domain/generation, case ID, purpose, exact scope, claim types, permitted actions, evidence sources, prohibited conflicts/role combinations, reviewer/witness, validity window, policy version and authorization decision.

Authorization is checked at use and recorded in the ceremony transcript. The verifier cannot expand its own scope or delegate beyond its approved profile.

## 10. Bounded attestation

Every attestation states exact subject/claims, evidence and provenance, method/policy/VQP versions, jurisdiction/scope, verification time and evidence interval, mandatory-predicate outcomes, unresolved facts/conflicts/limitations, verifier and signer identity/authority, signature validation evidence, and whether it is factual verification, professional/legal opinion, process witness or cryptographic check.

Generic conclusions such as “fully trusted”, “safe”, “legitimate in all jurisdictions” or “root approved” are prohibited unless every underlying claim is separately established by competent authority and approved process.

The attestation is evidence for the enrollment decision, not the decision itself.

## 11. Revocation, compromise and continuity

- Suspension/revocation blocks future reliance within affected scope.
- Compromise triggers dependency closure across verifier keys, staff, issuer, systems, subcontractors and prior attestations.
- Impact analysis distinguishes attestations before, during and after the possible compromise interval.
- Prior attestations are neither automatically accepted nor erased; they are temporally reassessed.
- A replacement verifier must be independently qualified; a suspended verifier cannot nominate its replacement as the sole authority.
- Key rotation requires authenticated continuity or a new independent qualification event.
- Directory disappearance or endpoint failure is UNKNOWN, not proof of revocation or continued validity.
- Material disputes are preserved and escalated; no automatic winner is selected by recency or source count.

## 12. Manual/legal reconstitution route

A manual route may be used only if an approved external governance policy defines it before the incident.

Minimum requirements:
1. open immutable reconstitution case and declare trust-state loss;
2. identify applicable legal entity, jurisdiction and governing instrument;
3. independently verify identity and authority of the appointing/recognizing body;
4. verify verifier identity, competence and case-specific authorization;
5. obtain independent legal review where authority is legally constituted or disputed;
6. use witnessed ceremony with separate applicant, verifier, approver and reviewer roles;
7. preserve original evidence, dissent, acquisition path and limitations;
8. bind new trust generation and scope; do not presume predecessor continuity;
9. obtain independent publication/witness evidence and consumer acknowledgements;
10. keep the domain suspended if mandatory authority/evidence remains UNKNOWN or CONFLICTED.

Manual does not mean informal or exempt from SoD. A phone call, email, video meeting or paper letter is only an evidence channel; participants and authority still require verification.

If no competent external authority can be independently authenticated, there is no safe reconstitution path. The system remains suspended. A new trust domain may be created only under a separately approved governance process and must not impersonate continuity with the lost domain.

## 13. Adversarial scenarios

| ID | Attack/failure | Required outcome |
|---|---|---|
| VQ-01 | Verifier presents only own website and key | UNKNOWN; reject reliance |
| VQ-02 | Accreditation logo cannot be confirmed with issuer | Not qualified |
| VQ-03 | Directory discovered through compromised root only | Discovery not trusted |
| VQ-04 | Registry contact supplied solely by applicant | Independent confirmation absent |
| VQ-05 | Two channels share identity provider | Common dependency; no independence |
| VQ-06 | Identity verifier signs legal authority opinion | Reject role/scope mismatch |
| VQ-07 | Credential status endpoint times out | UNKNOWN, never VALID |
| VQ-08 | Accreditation expired or revoked | Suspend/reject current reliance |
| VQ-09 | Verifier entity dissolved or merged | Reassess identity/authority |
| VQ-10 | Signer no longer employed or delegated | Reject signer authority |
| VQ-11 | Applicant controls verifier through beneficial ownership | Conflict; disqualify or independently adjudicate |
| VQ-12 | Shared cloud/DNS/CA compromise omitted | Independence result invalid |
| VQ-13 | Verifier paid contingent on successful enrollment | Conflict assessment; possible disqualification |
| VQ-14 | Verifier suppresses adverse evidence | Reject attestation; investigate |
| VQ-15 | Two attestations copied from same source | Count one evidence lineage |
| VQ-16 | Verifier key compromised with uncertain start time | Temporal impact review; suspend affected reliance |
| VQ-17 | Verifier claims universal recognition from one jurisdiction | Limit scope; reject overclaim |
| VQ-18 | VQP weakened after failed case | Reject retroactive policy change |
| VQ-19 | Verifier approves own qualification/conflict waiver | SoD failure |
| VQ-20 | Emergency operator selects first available verifier | Reject without pre-approved route |
| VQ-21 | Manual evidence lacks issuer/authority verification | Insufficient |
| VQ-22 | Legal opinion signed but counsel authority/engagement unknown | Insufficient |
| VQ-23 | Directory split-view shows different status | Suspend; reconcile independently |
| VQ-24 | Critical subcontractor undisclosed | Qualification incomplete |
| VQ-25 | Revocation source unavailable beyond freshness limit | UNKNOWN; suspend new reliance |
| VQ-26 | Qualified verifier not authorized for this case/scope | Reject case authorization |
| VQ-27 | Attestation says “root trusted” without bounded claims | Reject as non-conforming |
| VQ-28 | All external references lost | Remain suspended; no self-bootstrap |
| VQ-29 | Replacement nominated solely by compromised verifier | Reject sole-basis nomination |
| VQ-30 | New verifier claims continuity by old domain name | Reject continuity claim |
| VQ-31 | Same entity name, different registry identity | Distinct/ambiguous; adjudicate |
| VQ-32 | Stale qualification cached as active | UNKNOWN/STALE; no new reliance |
| VQ-33 | Verifier and reviewer share undisclosed control | SoD/independence failure |
| VQ-34 | Different brands share beneficial controller | Common control; no independence |
| VQ-35 | Required dependency has no known owner | Closure incomplete; no full assurance |
| VQ-36 | Manual route bypasses scope/challenge/witness binding | Reject ceremony |
| VQ-37 | Credential authentic but issuer authority disputed | Hold for adjudication |
| VQ-38 | Historical attestation invalidated automatically by later revocation | Temporal reassessment; preserve history |

## 14. Invariants

- INV-VQ-01: Verifier identity, legal status, competence, authorization, independence and result are distinct.
- INV-VQ-02: Verifier cannot establish trustworthiness solely through self-issued artifacts.
- INV-VQ-03: Qualification is role-, claim-, jurisdiction-, action-, scope- and time-specific.
- INV-VQ-04: Discovery never substitutes for authentication or authorization.
- INV-VQ-05: Missing/unavailable mandatory status is UNKNOWN, not VALID.
- INV-VQ-06: Different brands, URLs or keys do not prove independent failure domains.
- INV-VQ-07: Unknown mandatory dependencies prohibit full-independence claims.
- INV-VQ-08: Qualification does not confer case authorization.
- INV-VQ-09: Attestation is bounded evidence, not enrollment decision.
- INV-VQ-10: Mandatory profile predicates cannot be offset by scores or reputation.
- INV-VQ-11: VQP cannot be weakened retroactively to pass a failed case.
- INV-VQ-12: Verifier cannot approve own qualification or conflict waiver.
- INV-VQ-13: Suspension/revocation blocks future reliance within affected scope.
- INV-VQ-14: Historical attestations are temporally reassessed, not silently erased.
- INV-VQ-15: Manual reconstitution remains subject to identity, authority, independence and SoD.
- INV-VQ-16: No safe external reference means continued suspension.
- INV-VQ-17: New trust generation does not inherit continuity by naming or assertion.
- INV-VQ-18: Reliance decision is reproducible from VQR, VQP, case authorization and evidence.
- INV-VQ-19: Real verifier qualification requires external legal/security/governance review.
- INV-VQ-20: Candidate states remain non-canonical until R01/X-07 catalog approval.

## 15. Required integration

- A02 source failure-domain model: verifier, issuer, accreditation body, discovery channel, status endpoint, subcontractors and infrastructure are typed principals/dependencies.
- A02-AR-01: VQR and case authorization bind to immutable trust generation and anti-rollback policy.
- RR-01 external enrollment: qualification supplies evidence to identity/authority records; it does not replace them.
- X-07 lifecycle: qualification, credential, key, legal status and control changes are append-only events with valid-time/knowledge-time.
- X-08 governance: qualification authority, profile owner, reviewer, suspension and appeal authorities must be assigned.
- X-09 Scope Algebra: VQP and case authorization bind exact scopes; unresolved dependency closure prevents PASS.
- R01 catalogs: proposed lifecycle/disposition/freshness labels require canonical crosswalk before schemas or code.
- Privacy/legal: evidence collection and retention require jurisdiction-specific lawful basis and controls.

## 16. Approval and validation gates

Before approval: identify legal jurisdictions and competent authorities; approve verifier roles and VQP owners under X-08; approve independent discovery/publication; define evidence admissibility, freshness and revocation; define source failure-domain graph and closure proof obligations; complete privacy/retention assessment; conduct adversarial tabletop for root/verifier compromise, split-view and total trust-state loss; map scenarios/invariants to proof obligations and executable tests; obtain independent legal, security, cryptographic and governance review; approve a bounded pilot only after evidence and authority exist.

No real verifier, institution, accreditation regime, legal jurisdiction, cryptographic algorithm, key, quorum or trust anchor is selected.

## 17. Status

**PROPOSED / REVIEW_REQUIRED / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED.**

This protocol cannot solve total trust-anchor loss without an independently authenticated external governance basis. If no such basis exists, the correct outcome is suspension, not self-bootstrap.