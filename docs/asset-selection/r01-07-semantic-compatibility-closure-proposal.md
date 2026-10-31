# R01.7 — Propuesta de cierre de compatibilidad semántica

**ID:** IIE-IDENTITY-R01-07  
**Versión:** 0.1.0-draft  
**Fecha:** 2026-10-31  
**Estado:** PROPOSED / PENDING_ARCHITECTURE_APPROVAL / NOT_APPROVED  
**Dependencias:** R01.1–R01.6; D-01..D-10; modelo canónico 03; X-06; X-07; X-08; Scope Algebra/X-09.

## 1. Propósito y límites

Esta propuesta consolida los hallazgos R016-01..12 en un contrato de compatibilidad. Define ejes semánticos, ownership, vocabularios candidatos, producer/consumer rules, mappings prohibidos, gates, scope y autoridad requerida.

No modifica documentos canónicos, requisitos IIM, schemas, código, fixtures ni tests. No aprueba políticas ni asigna autoridad organizacional. Es una propuesta documental no implementada ni validada.

## 2. Invariantes

1. Workflow, lifecycle, freshness, contract validity, assessment validity y domain outcome son ejes separados.
2. NOT_ASSESSED no es un resultado epistemológico ni un ClaimResolution outcome.
3. UNKNOWN solo es resultado válido de una evaluación ejecutada bajo PredicateContract válido.
4. INVALID es error de contrato o validez metodológica, nunca FALSE, UNKNOWN, CONFLICTED o INELIGIBLE.
5. UNDETERMINED no equivale a NOT_APPLICABLE; N/A exige regla positiva versionada y justificación.
6. NOT_ADMISSIBLE es local al purpose evaluado y no equivale a inelegibilidad global.
7. COMPLETE no significa favorable; ACTIVE no significa COMPLETE; FRESH no significa válido ni elegible.
8. Ausencia de evaluación no se convierte en FRESH, N/A, UNKNOWN o resultado favorable.
9. Ninguna composición favorable se permite si ValidationEnvelope no es VALID.
10. UNKNOWN en scope/dependency no significa EMPTY, DISJOINT, NOT_AFFECTED o independencia.
11. Veto aplicable y criterios críticos no son compensables por score.
12. No se extienden resultados fuera del scope demostrado; decisiones históricas son inmutables.

## 3. Catálogo de dominios y ownership candidato

| Dominio | Producer | Vocabulario candidato | Consumer |
|---|---|---|---|
| ContractValidity | Contract Validator | VALID, INVALID | Todos |
| AssessmentValidity (nuevo, pendiente) | Independent Assessment Validator | VALID, INVALID | Consumers del assessment |
| AssessmentWorkflowStatus | Assessment Orchestrator | NOT_STARTED, IN_PROGRESS, REVIEW_REQUIRED, COMPLETE | Consumers |
| RecordLifecycleStatus | Lifecycle Authority | ACTIVE, SUPERSEDED, INVALIDATED, WITHDRAWN | Todos |
| FreshnessStatus | Freshness Evaluator | FRESH, APPROACHING_EXPIRY, STALE, NOT_APPLICABLE | Consumers con requisito temporal |
| IdentityStatus | Identity Resolver | RESOLVED, UNRESOLVED, CONFLICTED | PA, EA, Selection |
| ApplicabilityOutcome | Applicability Evaluator | APPLICABLE, NOT_APPLICABLE, UNDETERMINED | PA, EA, Risk |
| ClaimResolutionOutcome | Claim Resolver | CONFIRMED, REFUTED, UNKNOWN, CONFLICTED, RETRACTED | PA, EA, Risk |
| EvidenceAdmissibility | Evidence Assessor | ADMISSIBLE, ADMISSIBLE_WITH_LIMITATIONS, QUARANTINED, REJECTED | Claim Resolver, PA |
| EvidenceSufficiency | Claim-Evidence Assessor | SUFFICIENT, INSUFFICIENT | Claim Resolver, PA |
| PurposeAdmissibility | Purpose Evaluator | ADMISSIBLE, ADMISSIBLE_WITH_LIMITATIONS, INSUFFICIENT_EVIDENCE, CONFLICTED, NOT_ADMISSIBLE | EA |
| EligibilityStatus | Eligibility Evaluator | ELIGIBLE, ELIGIBLE_WITH_LIMITATIONS, INELIGIBLE, INSUFFICIENT_INFORMATION, QUARANTINED | Selection |
| RiskDomainStatus | Risk Assessor | NOT_ASSESSED, ASSESSMENT_IN_PROGRESS, ASSESSED_WITH_FINDINGS, ASSESSED_NO_MATERIAL_FINDING, UNKNOWN, STALE, CONFLICTED | Veto, Selection |
| VetoDecision | Veto Composer/Authority | VETO_ACTIVE, VETO_REVIEW_REQUIRED, NO_VETO_IDENTIFIED, VETO_NOT_ASSESSED | Selection, Route |
| ValidationEnvelope | Independent Composition Validator | VALID, INCOMPLETE, REVIEW_REQUIRED, REJECTED_INVALID_INPUT | X-06 |
| SelectionDecision | Decision Engine | DECISION_* del modelo canónico | Candidate Registry |
| RouteEligibility | Route Evaluator | ROUTE_* del modelo canónico/X-04 | Portfolio & Execution |
| ScopeResolutionResult | Scope Algebra Validator | RESOLVED, EMPTY, UNRESOLVED, INVALID | ValidationEnvelope |

EvidenceRecord, SourceAssessment, EvidenceAdmissibility, EvidenceSufficiency y ClaimResolution son objetos distintos; sus vocabularios no se intercambian.

## 4. Resoluciones propuestas

### D01 — Identity y freshness
Adoptar IdentityStatus RESOLVED/UNRESOLVED/CONFLICTED y FreshnessStatus FRESH/APPROACHING_EXPIRY/STALE/NOT_APPLICABLE. Retirar STALE de IdentityStatus canónico solo tras aprobación y migración; no mapear legacy STALE automáticamente. Ausencia de FreshnessAssessment no equivale a ningún estado de freshness.

### D02 — Workflow, lifecycle y ausencia
Adoptar dos máquinas ortogonales: Workflow NOT_STARTED/IN_PROGRESS/REVIEW_REQUIRED/COMPLETE; Lifecycle ACTIVE/SUPERSEDED/INVALIDATED/WITHDRAWN. NOT_ASSESSED se retira de ClaimResolution. Assessment ausente se representa en registry/envelope; assessment existente sin iniciar tiene NOT_STARTED; claim evaluado válidamente sin resolución tiene UNKNOWN. Workflow incompleto no publica outcome final obligatorio.

### D03 — ContractValidity y AssessmentValidity
Separar validez estructural del contrato y validez metodológica/de ejecución. INVALID no pertenece a outcomes de Applicability, ClaimResolution, Risk, Purpose, Eligibility o Selection. AssessmentValidity es eje propuesto pendiente de aprobación; un assessment inválido no publica outcome factual consumible.

### D04 — Applicability
ApplicabilityOutcome = APPLICABLE/NOT_APPLICABLE/UNDETERMINED. INVALID se traslada al eje de validez. NOT_APPLICABLE requiere taxonomía versionada, scope y justificación. Todos los requisitos N/A no producen admisibilidad por vacuidad.

### D05 — ClaimResolution
ClaimResolutionOutcome = CONFIRMED/REFUTED/UNKNOWN/CONFLICTED/RETRACTED. REFUTED exige refutación exacta del PredicateContract; RETRACTED no implica FALSE. Adapter FSA-01 preserva TRUE/FALSE/UNKNOWN; contrato inválido detiene la evaluación.

### D06 — Evidencia
Separar EvidenceRecord, SourceAssessment, EvidenceAdmissibility, EvidenceSufficiency, FreshnessAssessment y ClaimResolution. REJECTED/QUARANTINED impide usar evidencia como soporte admisible para el uso declarado, pero no demuestra falsedad del claim. INSUFFICIENT no equivale a inadmisible. Ausencia de assessment se expresa en workflow, no como resultado final.

### D07 — NOT_ADMISSIBLE
Solo puede emitirse si contrato, catálogo y cobertura están completos; obligaciones materiales aplicables evaluadas; evidencia admisible y suficiente; y una obligación obligatoria del purpose está demostrada como incumplida. No equivale a INELIGIBLE. EA solo emite INELIGIBLE si policy mapea expresamente esa obligación a un criterio obligatorio.

### D08 — Risk, Veto y Route
RiskDomainStatus conserva catálogo canónico salvo INVALID, que se mueve al eje de validez. VetoDecision mantiene VETO_ACTIVE/VETO_REVIEW_REQUIRED/NO_VETO_IDENTIFIED/VETO_NOT_ASSESSED. RouteEligibility mantiene su catálogo propio. ROUTE_INVALID requiere decisión explícita de compatibilidad legacy y no puede tratarse como inelegibilidad factual. UNKNOWN/NOT_ASSESSED/STALE/CONFLICTED en riesgo nunca producen NO_VETO_IDENTIFIED.

### D09 — ValidationEnvelope
Envelope incluye contract/assessment validity, workflow y completitud requerida, scope y partition proof, dependency closure, identity binding, versiones y compatibilidad, freshness requirements, blockers, warnings, lineage, validator/version, evaluated_at y digest.

- VALID: contratos, scope, partición, dependencias y cobertura obligatoria satisfechos.
- INCOMPLETE: faltan inputs, evaluaciones o cobertura.
- REVIEW_REQUIRED: conflicto o incertidumbre material requiere adjudicación.
- REJECTED_INVALID_INPUT: payload, referencias, tipos o integridad inválidos.

Solo VALID habilita X-06. Otros estados permiten diagnósticos, no outcome agregado favorable.

### D10 — Invalidación parcial
Patrón conservador: lifecycle global a una versión inmutable. Si un evento invalida una parte material del scope del record, el record padre pasa a INVALIDATED para consumo actual. Se crean nuevos records derivados con scopes atómicos, lineage y evaluación/decisión independientes. No se muta lifecycle por átomo dentro de la misma versión ni se extiende invalidación a scopes disjuntos sin dependency proof.

### D11 — Gates y precedence
Separar: (1) validación de contrato, identidad referencial, scope, partición, dependencias, compatibilidad y completitud; (2) evaluaciones de dominio; (3) composición X-06. X-06 consume solo snapshots compatibles y Envelope VALID. Gates son ortogonales; precedencia canónica ordena outcomes de decisión únicamente después de validar inputs. Un gate fallido no se transforma en domain outcome.

### D12 — Authority y SoD
X-08 debe asignar producer, independent validator, domain approver, adjudicator, lifecycle authority, veto/release authority, exception authority, decision approver y auditor. Producer no es único validator/approver; veto proposer no tiene release unilateral; requester de excepción no la aprueba; developer no valida independientemente su modelo; operator no audita su propia operación. Sin autoridad/quorum/independencia, transición privilegiada queda REVIEW_REQUIRED o denegada.

## 5. Producer-consumer y mappings prohibidos

| Producer → consumer | Regla | Prohibido |
|---|---|---|
| Identity → PA/EA/Selection | RESOLVED cuando sea criterio obligatorio | STALE → UNRESOLVED automático; RESOLVED → eligible |
| Freshness → assessments | Evaluar por use/scope/cutoff | Ausencia → FRESH/N/A; STALE → INVALIDATED |
| Applicability → PA/EA/Risk | APPLICABLE/N/A/UNDETERMINED | UNDETERMINED → N/A; INVALID como outcome |
| Claim → PA/EA/Risk | Resultado por PredicateContract | NOT_ASSESSED → UNKNOWN/FALSE |
| Evidence → Claim/PA | Admisibilidad y suficiencia separadas | REJECTED → claim FALSE |
| PA → EA | Mapping condicionado por obligación y policy version | ADMISSIBLE → ELIGIBLE; NOT_ADMISSIBLE → INELIGIBLE global |
| EA → Selection | Purpose/scope/policy exactos | INSUFFICIENT_INFORMATION → INELIGIBLE |
| Risk → Veto | Solo assessment válido, aplicable y suficiente | UNKNOWN/NOT_ASSESSED/STALE → NO_VETO |
| Veto → Selection/Route | Propagación por regla y scope probado | VETO_ACTIVE compensado por score |
| Envelope → X-06 | Solo VALID | Otros estados → outcome favorable |
| Selection → Portfolio | Candidatura analítica | DECISION_ELIGIBLE → orden/mandato |
| Route → Execution | Route y scope concretos más controles independientes | ROUTE_ELIGIBLE → TradeAuthorization |

## 6. Transiciones críticas

- Workflow: NOT_STARTED → IN_PROGRESS; IN_PROGRESS → REVIEW_REQUIRED o COMPLETE; REVIEW_REQUIRED → IN_PROGRESS solo tras adjudicación y reanudación autorizadas. COMPLETE no retrocede; nueva versión.
- Lifecycle: ACTIVE → SUPERSEDED/INVALIDATED/WITHDRAWN bajo evento y autoridad; terminales no vuelven a ACTIVE; recovery crea versión nueva.
- Freshness: FRESH → APPROACHING_EXPIRY → STALE por policy/timestamps. Recuperación requiere nuevo assessment.
- Claim: UNKNOWN → CONFIRMED/REFUTED/CONFLICTED con nueva evidencia y reevaluación válida.
- Veto: VETO_ACTIVE → NO_VETO_IDENTIFIED solo tras remediación verificada, cobertura completa y release authority.
- Scope derivado requiere manifest/proof y decisión independiente.
- Envelope no VALID solo pasa a VALID mediante nueva evaluación que satisface todos los gates.

Los nuevos resultados crean records con lineage; no reescriben historia.

## 7. Scope y propagación

DependencyEdge debe ser explícito, tipado, vigente, causal, scoped y respaldado por evidencia. Ausencia de edge no demuestra independencia si el grafo no es cerrado. UNKNOWN no equivale a NOT_AFFECTED. PARTIALLY_AFFECTED exige partición exacta y decisiones por átomo. Residual no es elegible ni libre de veto por defecto. Derived scope no hereda decisión favorable. La partición debe ser exacta, disjunta y con cobertura demostrada antes de Envelope VALID.

## 8. Aprobaciones requeridas

| Área | Revisores | Aprobación |
|---|---|---|
| Vocabularios/ownership | Architecture, Identity, Data Governance | Architecture Authority + Domain Owners |
| Contract/Assessment validity | Architecture, Security, QA | Architecture + Security/Quality |
| Workflow/Lifecycle/Freshness | Lifecycle, Data Governance, Operations | Lifecycle Owner + Architecture |
| Evidence/Claim/PA | Data Governance, Research | Evidence Owner + Purpose Owner |
| Eligibility | Investment Governance, Risk | Eligibility Owner + Governance |
| Risk/Veto | Risk, Legal/Compliance, Security | Risk Authority + release authority independiente |
| Envelope/Scope Algebra | Architecture, Security, Operations, Risk | Architecture + scope owners |
| Selection/Route | Decision, Operations, Risk, Compliance | Owners respectivos |
| SoD | Governance, Security, Audit | Governing Body autorizado |

Cada aprobación conserva rol, autoridad, scope, versión, decisión, condiciones, vigencia, conflicto de interés, dissent y audit event. RACI no concede permisos por sí solo.

## 9. Matriz R016-01..12

| Hallazgo | Tratamiento | Estado |
|---|---|---|
| 01 STALE en IdentityStatus | D01 | ADDRESSED_IN_DRAFT; migración pendiente |
| 02 D-10 divergente | D01–D12 | ADDRESSED_IN_DRAFT; D-10 no modificado |
| 03 NOT_ASSESSED en ClaimResolution | D02/D05 | ADDRESSED_IN_DRAFT |
| 04 INVALID mezclado | D03/D04/D08 | ADDRESSED_IN_DRAFT; AssessmentValidity pendiente |
| 05 NOT_ADMISSIBLE | D07 | ADDRESSED_IN_DRAFT; policy pendiente |
| 06 Evidence/ClaimEvidence | D06 | PARTIALLY_ADDRESSED; revisión Data Governance |
| 07 ausencia/NOT_STARTED | D02 | ADDRESSED_IN_DRAFT; registry contract pendiente |
| 08 Risk/Veto/Route | D08 | PARTIALLY_ADDRESSED; ROUTE_INVALID pendiente |
| 09 ValidationEnvelope | D09 | ADDRESSED_IN_DRAFT; proof/tests pendientes |
| 10 invalidación parcial | D10 | PROVISIONALLY_RESOLVED_IN_DRAFT |
| 11 gates/precedence | D11 | ADDRESSED_IN_DRAFT; reconciliación X-06 |
| 12 authority/SoD | D12 | NOT_CLOSED; aprobación organizacional |

Ningún hallazgo se marca CLOSED en registro normativo antes de aprobación y cross-audit. “Addressed in draft” no significa contradicción corregida.

## 10. Gate R01.8

Auditar R01.1–R01.7, D-01..D-10, modelo canónico 03, X-06, X-07, X-08 y Scope Algebra. Criterios:
- cero contradicciones HIGH abiertas;
- cada enum con owner, producer, consumer y binding;
- mappings versionados con condiciones, scope y reason codes;
- transiciones y combinaciones inválidas especificadas;
- ValidationEnvelope como precondición única verificable de X-06;
- lifecycle parcial aprobado;
- autoridades y SoD aprobados;
- escenarios críticos X-09 convertidos a tests ejecutables y ejecutados en CI;
- revisión independiente documentada.

Si falta un criterio: REVIEW_REQUIRED; no schema freeze.

## 11. Límites

Auditoría documental/propuesta. No se ejecutaron pruebas automatizadas, property-based, formales, de concurrencia o empíricas. No se modifican canónicos 03/11/12/13/19, D-01..D-10, requisitos IIM, schemas, código, fixtures o tests. No se declara conformidad ISO ni certificación.

**Estado:** PROPOSED / PENDING_ARCHITECTURE_APPROVAL / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED.
