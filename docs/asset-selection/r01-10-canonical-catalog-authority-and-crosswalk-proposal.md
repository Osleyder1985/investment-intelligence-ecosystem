# R01.10 — Propuesta de autoridad de catálogos y crosswalks canónicos

**ID:** IIE-IDENTITY-R01-10  
**Versión:** 0.1.0-draft  
**Fecha:** 2026-10-31  
**Estado:** PROPOSED / PENDING_ARCHITECTURE_APPROVAL / NOT_APPROVED  
**Dependencias:** R01.8, R01.9, modelo canónico 03, X-06, X-04 (contrato Route Eligibility), X-07, X-09 y documento 18.  
**Hallazgos abordados:** R018-04, R018-05, R018-07, R018-08; contribuye a R018-09 y R018-11.

## 1. Dictamen ejecutivo

Los documentos actuales no contienen un único vocabulario coherente para Evidence, Risk, SelectionDecision y RouteEligibility. La divergencia no se resuelve con una simple tabla de renombrado: algunos estados describen outcomes, otros workflow, frescura, validez, contención o causas.

Esta propuesta establece una arquitectura de catálogos por dominio y crosswalks conservadores. No sustituye todavía los vocabularios canónicos ni modifica sus documentos fuente.

**Dictamen:** ADDRESSED_IN_DRAFT / INTEGRATION_REVIEW_REQUIRED / NOT_APPROVED.

### Reglas rectoras

1. Un estado tiene un solo dominio semántico propietario.
2. Un consumidor no modifica ni reescribe el estado emitido por el productor.
3. Un crosswalk conserva source status, source version, target assessment, mapping rule/version y rationale.
4. Mapping no demostrado = UNMAPPED/REVIEW_REQUIRED; nunca fallback favorable.
5. Estados de workflow, lifecycle, freshness, contract validity y domain outcome son ortogonales.
6. DecisionOutcome no sustituye DecisionReason ni OpportunityDisposition.
7. RouteEligibility no equivale a TradeAuthorization.
8. Ningún mapping propuesto se considera vigente antes de aprobación de los owners.

## 2. Arquitectura de catálogos propuesta

| Catálogo | Objeto propietario | Producer | Consumer primario | Autoridad propuesta |
|---|---|---|---|---|
| EvidenceAdmissibility | EvidenceUseAssessment | Evidence Assessor | Claim/PA/EA/Risk | Evidence/Data Governance Owner |
| EvidenceSufficiency | EvidenceUseAssessment o ClaimEvidenceAssessment | Evidence Sufficiency Assessor | Claim/PA/EA | Research/Data Owner |
| FreshnessStatus | FreshnessAssessment | Freshness Evaluator | Evidence/Identity/Risk/Route consumers | Data Governance Owner |
| RiskDomainOutcome | RiskDomainAssessment | Risk Assessor | Veto Composer / Decision / Route | Risk Domain Owner |
| RiskAssessmentValidity | Assessment validation envelope/error record | Independent Validator | Risk/Veto/Decision/Route | Architecture + Quality/Security |
| DecisionOutcome | Atomic SelectionDecision | Decision Composer X-06 | Candidate Registry | Decision Method Owner |
| DecisionReason | DecisionCause collection | Decision Composer | Registry/Audit/Users | Decision Method Owner |
| OpportunityDisposition | OpportunityAssessment | Opportunity Evaluator | Ranking/Portfolio Research | Investment Research Owner |
| RouteEligibilityOutcome | Atomic RouteEligibilityAssessment | Route Evaluator X-04 | ORE / Portfolio & Execution | Operational Risk/Route Owner |

Los nombres anteriores son propuestos. Los owners institucionales no están asignados y requieren aprobación X-08. Evidence sufficiency puede ser por claim o por uso; el contrato definitivo debe evitar dos fuentes de verdad.

## 3. Catálogo Evidence

### 3.1 Separación semántica

No conservar un enum único que mezcle:
- admisibilidad de una fuente/evidencia para un uso;
- suficiencia de evidencia para un claim/decisión;
- frescura temporal;
- workflow de evaluación;
- validez contractual del assessment.

Vocabularios candidatos:

**EvidenceAdmissibility**
- ADMISSIBLE
- ADMISSIBLE_WITH_LIMITATIONS
- QUARANTINED
- REJECTED

**EvidenceSufficiency**
- SUFFICIENT
- INSUFFICIENT

**FreshnessStatus** se toma del eje R01.1: FRESH, APPROACHING_EXPIRY, STALE, NOT_APPLICABLE, con policy/use/scope/cutoff explícitos.

Workflow y lifecycle siguen R01.4/R01.7; no se duplican dentro de EvidenceStatus.

### 3.2 Legacy EvidenceStatus crosswalk

El catálogo canónico 03 actualmente incluye ADMISSIBLE, ADMISSIBLE_WITH_LIMITATIONS, QUARANTINED, REJECTED, UNKNOWN, STALE y NOT_ASSESSED.

| Legacy EvidenceStatus | Tratamiento candidato | Mapping automático | Condición / salvaguarda |
|---|---|---|---|
| ADMISSIBLE | EvidenceAdmissibility=ADMISSIBLE | Condicional | Solo conservar el uso, scope, source assessment y periodo originales; no inferir SUFFICIENT |
| ADMISSIBLE_WITH_LIMITATIONS | EvidenceAdmissibility=ADMISSIBLE_WITH_LIMITATIONS | Condicional | Deben existir limitaciones identificables y su scope; si no, UNMAPPED/REVIEW_REQUIRED |
| QUARANTINED | EvidenceAdmissibility=QUARANTINED | Condicional | Mantener reason, autoridad y estado de investigación; no inferir claim falso |
| REJECTED | EvidenceAdmissibility=REJECTED | Condicional | Solo para el uso declarado; no significa falsedad del dato o claim |
| UNKNOWN | Legacy status preservado; nueva evaluación requerida | **No** | No equivale a INSUFFICIENT, NOT_ASSESSED ni CONFLICTED sin causa/assessment |
| STALE | FreshnessStatus=STALE | Condicional | Requiere objeto, use, scope, cutoff y freshness policy identificables; no implica REJECTED ni INVALIDATED |
| NOT_ASSESSED | Workflow=NOT_STARTED o ausencia de assessment | **No** | Solo si se demuestra que el registro existe y el workflow no comenzó; ausencia de registro se representa en registry/envelope |

No se permite convertir UNKNOWN en REJECTED, INSUFFICIENT o NOT_ASSESSED por conveniencia. Para registros legacy sin metadata suficiente, mantener el status original como LEGACY_UNMAPPED y bloquear consumers que requieran una dimensión no demostrada.

### 3.3 Contrato de consumo

Una evidencia puede ser admisible y no suficiente. Una evidencia puede ser suficiente en cantidad pero inadmisible por fuente/uso. STALE se evalúa por uso y cutoff. REJECTED no refuta el claim. QUARANTINED no demuestra falsedad. Una evaluación ausente no es UNKNOWN.

ClaimResolution consume solo EvidenceUseAssessments compatibles y preserva PredicateContract. El consumidor no puede inferir CONFIRMED/REFUTED por el estado de admisibilidad aislado.

## 4. Catálogo Risk

### 4.1 RiskDomainOutcome candidato

Conservar los outcomes factuales/epistemológicos del modelo 03:
- NOT_ASSESSED
- ASSESSMENT_IN_PROGRESS
- ASSESSED_WITH_FINDINGS
- ASSESSED_NO_MATERIAL_FINDING
- UNKNOWN
- STALE
- CONFLICTED

Separar INVALID de RiskDomainOutcome y tratarlo como error/validez de assessment. No se adopta todavía un nuevo enum AssessmentValidity=VALID/INVALID como eje canónico; R018-09 exige definir su semántica. La propuesta preferida es un typed AssessmentError/ValidationEnvelope hasta que Architecture determine si se necesita eje independiente.

### 4.2 Tratamiento de legacy INVALID

El estado RiskDomainStatus.INVALID del modelo 03 se conserva para lectura histórica, pero no debe emitirse en nuevos assessments después de una migración aprobada.

| Causa comprobada de INVALID | Registro destino propuesto | ¿Outcome de riesgo? | Acción consumer |
|---|---|---|---|
| Payload/schema/reference malformed | Contract validation error | No | Rechazar ese input; no componer |
| Method/version not approved or incompatible | Method/Policy compatibility error | No | REVIEW_REQUIRED o reject según policy |
| Execution defect, exception, partial computation | Assessment execution error | No | INCOMPLETE/REVIEW_REQUIRED; no emitir outcome factual |
| Integrity/digest mismatch | Integrity error | No | Rechazar; preservar forensic evidence |
| Source/data quality defect | Evidence/Data quality assessment | No automáticamente | Reevaluar claims/risk; no convertir a UNKNOWN sin ejecución válida |
| Scope/dependency proof failure | Scope/Dependency validation error | No | UNRESOLVED/REVIEW_REQUIRED para unidades dependientes |
| Unknown legacy cause | LEGACY_INVALID_UNCLASSIFIED | No | No mapping; re-evaluación requerida |

**Importante:** UNKNOWN es un resultado epistemológico válido de un assessment ejecutado correctamente. INVALID describe que el assessment no puede considerarse un resultado válido. No son equivalentes.

### 4.3 Risk → Veto contract

RiskDomainOutcome no se convierte directamente en VetoDecision:
- ASSESSED_WITH_FINDINGS requiere aplicar reglas de veto versionadas; no implica VETO_ACTIVE automáticamente.
- ASSESSED_NO_MATERIAL_FINDING puede contribuir a NO_VETO_IDENTIFIED solo si toda cobertura obligatoria de reglas/domains está completa.
- UNKNOWN, NOT_ASSESSED, ASSESSMENT_IN_PROGRESS, STALE y CONFLICTED nunca permiten NO_VETO_IDENTIFIED.
- Error de assessment/contract bloquea la composición del veto dependiente; no fabrica un finding ni ausencia de veto.
- Applicability sigue separada y requiere regla positiva para NOT_APPLICABLE.

VetoDecision mantiene VETO_ACTIVE, VETO_REVIEW_REQUIRED, NO_VETO_IDENTIFIED y VETO_NOT_ASSESSED, con scope y rule coverage explícitos.

## 5. Catálogo SelectionDecision: separar outcome, razón y oportunidad

### 5.1 Problema

Modelo 03 §4.8 mezcla estados de bloqueo por dominio, outcomes de valoración, estados de comparabilidad, candidatura condicional y soporte de oportunidad. X-06 define un outcome de composición más reducido:
- DECISION_NOT_ASSESSED
- DECISION_IN_PROGRESS
- DECISION_REVIEW_REQUIRED
- DECISION_INELIGIBLE
- DECISION_ABSTAIN
- DECISION_ELIGIBLE_WITH_LIMITATIONS
- DECISION_ELIGIBLE

No son enums equivalentes. Por ejemplo, OPPORTUNITY_SUPPORTED no es sinónimo de DECISION_ELIGIBLE: una elegibilidad de finalidad puede existir sin que el protocolo de valoración/benchmark respalde una oportunidad.

### 5.2 Separación propuesta

**A. DecisionOutcome (propietario: X-06)**

Mantener como único outcome de composición:
- DECISION_NOT_ASSESSED
- DECISION_IN_PROGRESS
- DECISION_REVIEW_REQUIRED
- DECISION_INELIGIBLE
- DECISION_ABSTAIN
- DECISION_ELIGIBLE_WITH_LIMITATIONS
- DECISION_ELIGIBLE

Es válido solo por decision_type, purpose, scope atómico, snapshot y policy version. La agregación se rige por X-06/documento 18 después de envelope y partition validation.

**B. DecisionReason (colección, no enum outcome)**

Preservar razones específicas como:
- BLOCKED_IDENTITY
- BLOCKED_ELIGIBILITY
- BLOCKED_EVIDENCE
- VETOED
- REVIEW_REQUIRED
- RISK_INCOMPLETE
- VALUATION_NOT_APPLICABLE
- VALUATION_NOT_ESTIMABLE
- INSUFFICIENT_EVIDENCE
- NOT_COMPARABLE
- MATERIAL_LIMITATION
- OPPORTUNITY_NOT_SUPPORTED

Cada razón tiene cause_code, source_domain, source_ref/version, rule_id/version, scope, severity, primary/secondary, mapping_version y rationale. No se pierde causa por la selección de outcome primario.

**C. OpportunityDisposition (assessment distinto)**

Mantener una evaluación separada de si existe oportunidad respaldada bajo protocolo declarado:
- OPPORTUNITY_SUPPORTED
- NO_OPPORTUNITY_SUPPORTED
- OPPORTUNITY_NOT_ASSESSED
- OPPORTUNITY_REVIEW_REQUIRED
- OPPORTUNITY_INSUFFICIENT_EVIDENCE

Estos nombres son candidatos y no deben adoptarse hasta revisión del owner de Investment Research. El protocolo debe identificar modelo, horizonte, benchmark, costes, universe, point-in-time inputs y validation status.

DECISION_ELIGIBLE no produce OPPORTUNITY_SUPPORTED automáticamente. OPPORTUNITY_SUPPORTED no autoriza ejecución ni sustituye mandato/risk controls.

### 5.3 Crosswalk conservador del modelo 03 a X-06

| Modelo 03 SelectionDecision | DecisionOutcome X-06 candidato | DecisionReason conservada | Condición |
|---|---|---|---|
| BLOCKED_IDENTITY | DECISION_ABSTAIN o DECISION_REVIEW_REQUIRED | BLOCKED_IDENTITY | REVIEW si conflicto adjudicable; ABSTAIN si falta evidencia; nunca DECISION_INELIGIBLE sin criterio sustantivo |
| BLOCKED_ELIGIBILITY | DECISION_INELIGIBLE | BLOCKED_ELIGIBILITY | Solo si EligibilityStatus=INELIGIBLE demostrado para el mismo purpose/scope |
| BLOCKED_EVIDENCE | DECISION_ABSTAIN o DECISION_REVIEW_REQUIRED | BLOCKED_EVIDENCE | Según causa: falta material → abstain; conflicto adjudicable → review; input inválido → envelope reject |
| VETOED | DECISION_INELIGIBLE | VETOED | VETO_ACTIVE aplicable, mismo scope/purpose/activity |
| REVIEW_REQUIRED | DECISION_REVIEW_REQUIRED | REVIEW_REQUIRED | Revisión material requerida y envelope válido |
| RISK_INCOMPLETE | DECISION_ABSTAIN o DECISION_REVIEW_REQUIRED | RISK_INCOMPLETE | UNKNOWN/no evaluado/stale → abstain; conflicto adjudicable → review; INVALID → error/envelope no válido |
| VALUATION_NOT_APPLICABLE | Sin mapping por defecto | VALUATION_NOT_APPLICABLE | Si valuation no requerida, excluir ese módulo; continuar otras obligaciones. Si es requerida, no producir favorable |
| VALUATION_NOT_ESTIMABLE | DECISION_ABSTAIN o DECISION_REVIEW_REQUIRED | VALUATION_NOT_ESTIMABLE | Solo si valuation es obligatoria para el purpose; no confundir con INELIGIBLE |
| INSUFFICIENT_EVIDENCE | DECISION_ABSTAIN | INSUFFICIENT_EVIDENCE | Evidencia material faltante, sin incumplimiento probado |
| NOT_COMPARABLE | DECISION_ABSTAIN o sin mapping | NOT_COMPARABLE | Si comparabilidad es obligatoria, abstener; si no, continuar sin ranking comparable |
| CONDITIONAL_CANDIDATE | DECISION_ELIGIBLE_WITH_LIMITATIONS | CONDITIONAL_CANDIDATE | Solo con límites exactos, enforcement probado y todos los gates válidos |
| OPPORTUNITY_SUPPORTED | No mapear a DecisionOutcome | OpportunityDisposition=OPPORTUNITY_SUPPORTED | Requiere protocolo de oportunidad completo |
| NO_OPPORTUNITY_SUPPORTED | No mapear a DecisionOutcome | OpportunityDisposition=NO_OPPORTUNITY_SUPPORTED | Evaluación completa sin oportunidad; no implica inelegibilidad |

Mappings de BLOCKED_IDENTITY, BLOCKED_EVIDENCE, RISK_INCOMPLETE y VALUATION_* son dependientes de policy y de causa. La tabla no es una conversión automática ni total. Si faltan datos para distinguir causa, el resultado es LEGACY_UNMAPPED/REVIEW_REQUIRED.

### 5.4 Reglas de precedencia y pérdida de información

X-06 DECISION_* es el outcome de composición; el catálogo 03 se conserva como razones/diagnósticos legacy durante migración. Una causa más restrictiva puede determinar el outcome, pero todas las razones y scopes coincidentes se preservan.

La precedencia X-06/documento 18 no debe usarse para colapsar estados de validez, workflow o lifecycle. DECISION_ELIGIBLE solo se permite tras todos los gates requeridos, envelope atómico VALID y ausencia de bloqueos aplicables.

## 6. Catálogo RouteEligibility

### 6.1 Problema

Modelo 03 §4.9 define seis estados:
- ROUTE_ELIGIBLE
- ROUTE_ELIGIBLE_WITH_LIMITATIONS
- ROUTE_INELIGIBLE
- ROUTE_REVIEW_REQUIRED
- ROUTE_UNKNOWN
- ROUTE_STALE

X-04/X-06 documentan un vocabulario operacional más detallado, con:
- ROUTE_NOT_ASSESSED
- ROUTE_ASSESSMENT_IN_PROGRESS
- ROUTE_INSUFFICIENT_INFORMATION
- ROUTE_CONFLICTED
- ROUTE_INVALID
además de estados de elegibilidad y revisión.

El vocabulario detallado separa workflow, insuficiencia, conflicto y validez; pero parte de estas semánticas quizá pertenezca al envelope y no al outcome de ruta.

### 6.2 Separación propuesta

Mantener RouteEligibilityOutcome para resultado de ruta:
- ROUTE_ELIGIBLE
- ROUTE_ELIGIBLE_WITH_LIMITATIONS
- ROUTE_INELIGIBLE
- ROUTE_REVIEW_REQUIRED

Separar:
- RouteAssessmentWorkflow: NOT_ASSESSED / ASSESSMENT_IN_PROGRESS / COMPLETE / REVIEW_REQUIRED;
- RouteAssessmentValidity/error envelope: VALID / INCOMPLETE / REJECTED_INVALID_INPUT / REVIEW_REQUIRED (armonizado con ValidationEnvelope atómico R01.9);
- FreshnessStatus: FRESH / APPROACHING_EXPIRY / STALE / NOT_APPLICABLE;
- RouteEvidence/Dependency conflicts como DecisionReason/route cause, no como outcome elegible.

No adoptar ROUTE_UNKNOWN, ROUTE_STALE, ROUTE_INVALID como outcomes finales nuevos. Preservarlos como estados legacy o diagnósticos con crosswalk contextual.

### 6.3 Crosswalk legacy RouteEligibility

| Estado fuente | Resultado destino propuesto | Condición |
|---|---|---|
| ROUTE_ELIGIBLE | ROUTE_ELIGIBLE | Solo con envelope VALID, scope/route exactos, freshness y controles requeridos |
| ROUTE_ELIGIBLE_WITH_LIMITATIONS | ROUTE_ELIGIBLE_WITH_LIMITATIONS | Limitaciones exactas y enforcement probado |
| ROUTE_INELIGIBLE | ROUTE_INELIGIBLE | Criterio operativo obligatorio incumplido con evidencia válida |
| ROUTE_REVIEW_REQUIRED | ROUTE_REVIEW_REQUIRED | Mantener revisión y razón |
| ROUTE_UNKNOWN | Sin mapping automático | Descomponer por workflow, evidence, applicability y validity; UNKNOWN no distingue no evaluado de insuficiente |
| ROUTE_NOT_ASSESSED | RouteAssessmentWorkflow=NOT_ASSESSED | Solo si assessment record/workflow existe y no se inició |
| ROUTE_ASSESSMENT_IN_PROGRESS | RouteAssessmentWorkflow=ASSESSMENT_IN_PROGRESS | Mantener workflow, sin outcome favorable |
| ROUTE_INSUFFICIENT_INFORMATION | Envelope INCOMPLETE o causa INSUFFICIENT_INFORMATION | Requiere identificar inputs faltantes; no equivale a INELIGIBLE |
| ROUTE_CONFLICTED | Envelope REVIEW_REQUIRED o reason ROUTE_CONFLICTED | Requiere conflicto material, mismo route/scope/version |
| ROUTE_STALE | FreshnessStatus=STALE | Requiere policy/cutoff y route scope; no equivale a INVALIDATED |
| ROUTE_INVALID | REJECTED_INVALID_INPUT o typed assessment error | Depende de causa; no equivale a ROUTE_INELIGIBLE |

X-04 enum exacto y su owner deben ser verificados antes de adoptar esta propuesta. Si X-04 incluye estados no enumerados aquí, registrar cada uno en un crosswalk adicional; no asumir que esta lista es exhaustiva.

### 6.4 Condiciones para ROUTE_ELIGIBLE

Una ruta elegible requiere:
- identidad y representación correctas para la ruta;
- venue, account/custodian, jurisdiction, asset, network, side, size y purpose exactos;
- evidence y assessments obligatorios admisibles, suficientes y frescos;
- envelope VALID por átomo/ruta;
- ausencia de veto/control obligatorio fallido;
- límites técnicamente enforceable;
- mandato, risk budget, authorization y pre-trade controls separados.

ROUTE_ELIGIBLE no es TradeAuthorization, no genera orden y no concede custodia/llaves.

## 7. Crosswalk rules y estados no migrables

Cada mapping debe registrarse como:
- mapping_id/version;
- source_catalog/version/status;
- target_catalog/version/status o UNMAPPED;
- applicability predicate;
- purpose/activity/scope constraints;
- required evidence/metadata;
- transformation rule;
- prohibited interpretations;
- reason code;
- owner/approver;
- effective interval;
- tests/fixtures y replay version.

Reglas:
1. Mapping puede ser 1:1, 1:N, N:1 o no migrable; cardinalidad se declara.
2. N:1 requiere preservar source statuses y razones; no se pierde granularidad.
3. 1:N requiere metadata que permita reconstruir cada dimensión; sin ella, UNMAPPED.
4. Un mapping no puede usar datos posteriores al cutoff para reescribir una decisión histórica.
5. Un mapping de un purpose no se reutiliza para otro purpose.
6. Un mapping de un route no se reutiliza para otra venue/cuenta/jurisdicción.
7. No usar default favorable para estado desconocido o enum desconocido.
8. Unknown enum/version produce VERSION_INCOMPATIBLE/REVIEW_REQUIRED o rechazo contractual.
9. Los estados legacy no migrables permanecen legibles en histórico y no son consumibles por nuevos módulos sin adapter explícito.
10. Cada consumer declara los catálogos/versions aceptados.

## 8. Producer-consumer contracts mínimos

| Producer | Publica | No puede declarar | Consumer |
|---|---|---|---|
| Evidence Assessor | Admissibility, limitations, source/use/scope | Claim truth, eligibility | Claim/PA |
| Sufficiency Assessor | Sufficiency por claim/use | Source admissibility | Claim/PA/EA |
| Risk Assessor | RiskDomainOutcome + findings | Veto absence without coverage | Veto Composer |
| Independent Validator | Contract/integrity/method errors | Factual risk outcome | Risk/Veto/Decision |
| X-06 Decision Composer | DecisionOutcome + DecisionReasons | Opportunity or trade authorization | Candidate Registry |
| Opportunity Evaluator | OpportunityDisposition + protocol refs | Operational route eligibility | Ranking/Research |
| X-04 Route Evaluator | RouteEligibilityOutcome + route causes | Trade authorization | Portfolio & Execution |
| Portfolio Authorization | Mandate/authorization controls | Modify analytical outcome | Execution controls |

Los contratos deben identificar versiones, scope, purpose/activity, snapshot, freshness, lifecycle, workflow, authority y lineage. La asignación de roles reales queda a X-08.

## 9. Hallazgos y resolución

| Hallazgo | Tratamiento R01.10 | Estado |
|---|---|---|
| R018-04 SelectionDecision catalog conflict | DecisionOutcome X-06 + DecisionReason + separate OpportunityDisposition | ADDRESSED_IN_DRAFT; owner approval pending |
| R018-05 Route catalog divergence | Route outcome vs workflow/freshness/validity split | PARTIALLY_ADDRESSED; X-04 exact enum verification pending |
| R018-07 Evidence hybrid enum | Split admissibility/sufficiency/freshness/workflow; conservative legacy crosswalk | ADDRESSED_IN_DRAFT; migration tests pending |
| R018-08 Risk INVALID | Move invalidity to typed validation/error contract, preserve legacy state | PARTIALLY_ADDRESSED; error taxonomy and X-06 binding pending |
| R018-09 AssessmentValidity | Prefer typed error/envelope pending architecture decision | PARTIALLY_ADDRESSED |
| R018-11 Freshness consumer mapping | Freshness by purpose/use/scope/cutoff; crosswalk to be defined per consumer | NOT_CLOSED |

## 10. Acceptance gates

R01.10 no se considera cerrado hasta:
- X-04 enum completo contrastado con su documento fuente y owner;
- todos los catálogos con una autoridad/version propietaria;
- DecisionOutcome, DecisionReason y OpportunityDisposition aprobados como objetos separados;
- Risk INVALID legacy migration y typed error contract definidos;
- Evidence legacy statuses mapeados o declarados no migrables;
- mappings por consumer/purpose/activity/scope con versioning y rationale;
- conflictos y enums desconocidos fail-closed sin falso favorable;
- R01.8 hallazgos HIGH relevantes revisados y aprobados;
- escenarios convertidos a fixtures/tests y CI ejecutada;
- cross-audit independiente antes de schema freeze.

## 11. Verificación futura

Los crosswalks aquí son propuestas, no adaptadores ejecutables. Antes de implementación:
1. crear catálogo machine-readable tras aprobación;
2. definir schemas y validators;
3. crear fixtures de cada legacy status y metadata faltante;
4. probar mappings 1:1, 1:N, N:1 y UNMAPPED;
5. property tests para no aumentar favorabilidad ni ampliar scope;
6. probar replay temporal, version mismatch y unknown enum;
7. ejecutar CI y registrar evidencias.

No se ejecutaron pruebas como parte de R01.10.

## 12. Límites y estado

Revisión documental estática basada en los vocabularios observados en el modelo 03 y X-06. El contrato X-04 requiere confirmación directa de su documento fuente antes de adopción. No se modifican documentos canónicos, requisitos IIM, schemas, código, fixtures o tests. No hay aprobación institucional, implementación, validación empírica ni certificación ISO.

**Estado final:** PROPOSED / PENDING_ARCHITECTURE_APPROVAL / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED.
