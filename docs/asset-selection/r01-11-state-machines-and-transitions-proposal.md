# R01.11 — Máquinas de estados y transiciones

**ID:** IIE-IDENTITY-R01-11  
**Versión:** 0.1.0-draft  
**Fecha:** 2026-10-31  
**Estado:** PROPOSED / PENDING_ARCHITECTURE_APPROVAL / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED  
**Dependencias:** modelo 03; X-04, X-06, X-07, X-08, X-09; R01.1–R01.10.

## 1. Dictamen

IIE requiere máquinas por objeto/dominio y ejes ortogonales para workflow, lifecycle, freshness, validez contractual y outcome. Una máquina global fusionaría conceptos distintos y permitiría mappings inseguros.

R01.11 propone:
1. AssessmentWorkflowStatus y RecordLifecycleStatus independientes.
2. EvidenceAdmissibility/Sufficiency separadas de ClaimResolution.
3. RiskDomainAssessment separado de VetoDecision.
4. RouteEligibilityOutcome separado de workflow, freshness y ValidationEnvelope.
5. DecisionOutcome separado de OpportunityDisposition.
6. Contratos de eventos, concurrencia, idempotencia, invalidación parcial y replay.

No es una aprobación institucional, implementación, prueba ni certificación.

## 2. Revisión de fuentes

| Fuente | Resultado |
|---|---|
| Modelo 03 | Mezcla workflow/lifecycle y conserva vocabularios previos |
| X-04 / documento 09 | Confirma diez estados RouteEligibility |
| X-06 / documento 11 | Separa ANALYTICAL_SELECTION y OPERATIONAL_ROUTE; composición atómica |
| X-07 / documento 12 | Define eventos, causalidad, idempotencia y prohíbe last-write-wins |
| X-08 / documento 13 | Roles funcionales/SoD propuestos; sin asignaciones reales |
| X-09 / documento 14 | Escenarios e invariantes son especificaciones, no tests ejecutables |
| R01.4–R01.5 | Workflow/lifecycle y matrices de compatibilidad aún pendientes |
| R01.9–R01.10 | Envelope atómico y autoridad de catálogos propuestos, no aprobados |

### 2.1 Catálogo X-04 verificado

El documento fuente 09 define: ROUTE_NOT_ASSESSED, ROUTE_ASSESSMENT_IN_PROGRESS, ROUTE_ELIGIBLE, ROUTE_ELIGIBLE_WITH_LIMITATIONS, ROUTE_INELIGIBLE, ROUTE_REVIEW_REQUIRED, ROUTE_INSUFFICIENT_INFORMATION, ROUTE_STALE, ROUTE_CONFLICTED y ROUTE_INVALID.

R01.10 propone separar workflow, freshness y validez del outcome de ruta. Se confirma el catálogo fuente, pero no se autoriza migración automática.

## 3. Ejes ortogonales

| Eje | Estados/campos candidatos |
|---|---|
| ContractValidity / ValidationEnvelope | VALID, INCOMPLETE, REVIEW_REQUIRED, REJECTED_INVALID_INPUT |
| AssessmentWorkflowStatus | NOT_STARTED, IN_PROGRESS, REVIEW_REQUIRED, COMPLETE |
| RecordLifecycleStatus | ACTIVE, SUPERSEDED, INVALIDATED, WITHDRAWN |
| FreshnessStatus | FRESH, APPROACHING_EXPIRY, STALE, NOT_APPLICABLE |
| DomainOutcome | ClaimResolution, RiskDomainOutcome, RouteEligibilityOutcome, DecisionOutcome |
| Authority | actor, role, delegation, approval |
| Scope | atom_scope_id/digest, purpose, activity, route_id |

COMPLETE no implica outcome favorable, ACTIVE ni FRESH. ACTIVE no implica COMPLETE ni VALID. STALE no implica INVALIDATED. INVALID es contractual/técnico, no factual. Un mismo nombre de estado en ejes distintos no autoriza equivalencia semántica.

## 4. Máquina A — AssessmentWorkflowStatus

Estados: NOT_STARTED, IN_PROGRESS, REVIEW_REQUIRED, COMPLETE.

| Desde | Hacia | Guard |
|---|---|---|
| NOT_STARTED | IN_PROGRESS | Inicio autorizado; scope e inputs identificados |
| NOT_STARTED | REVIEW_REQUIRED | Bloqueo material previo |
| IN_PROGRESS | REVIEW_REQUIRED | Conflicto/dependencia/revisión obligatoria |
| REVIEW_REQUIRED | IN_PROGRESS | Causa resuelta y reanudación autorizada |
| IN_PROGRESS | COMPLETE | RequiredInputManifest satisfecho; outputs presentes; envelope válido; dependencias resueltas |

Prohibido: NOT_STARTED→COMPLETE sin ejecución; IN_PROGRESS→NOT_STARTED; COMPLETE→cualquier estado; REVIEW_REQUIRED→COMPLETE sin cierre documentado. COMPLETE es terminal para esa versión y no significa favorable.

## 5. Máquina B — RecordLifecycleStatus

Estados: ACTIVE, SUPERSEDED, INVALIDATED, WITHDRAWN.

| Desde | Hacia | Guard |
|---|---|---|
| ACTIVE | SUPERSEDED | Sucesora validada/activada, successor_ref y scope compatible |
| ACTIVE | INVALIDATED | Evento causal verificado, scope, evidencia, dependency path y autoridad |
| ACTIVE | WITHDRAWN | Decisión explícita de autoridad facultada |

Los estados terminales no se reactivan. Un borrador sucesor no supersede al registro actual. TTL/STALE no causa INVALIDATED. Invalidación parcial no puede invalidar globalmente sin prueba. Recovery crea una nueva versión enlazada; no reescribe historia.

## 6. Evidence y ClaimResolution

EvidenceAdmissibility propuesta: ADMISSIBLE, ADMISSIBLE_WITH_LIMITATIONS, QUARANTINED, REJECTED. EvidenceSufficiency, FreshnessStatus, workflow y ContractValidity son dimensiones separadas. UNKNOWN/STALE/NOT_ASSESSED legacy no se migran por renombrado.

ClaimResolutionStatus propuesto: CONFIRMED, REFUTED, UNKNOWN, CONFLICTED, RETRACTED. NOT_ASSESSED pertenece a workflow; INVALID a ContractValidity; REJECTED a admisibilidad.

Transiciones: sin resolución→CONFIRMED/REFUTED solo con PredicateContract y evidencia admisible/suficiente; sin resolución→UNKNOWN tras evaluación sin demostración; CONFIRMED/REFUTED→CONFLICTED ante evidencia contradictoria comparable; UNKNOWN→CONFIRMED/REFUTED con evidencia nueva; CONFLICTED→resolución adjudicada en nueva versión; cualquier resolución activa→RETRACTED mediante procedimiento válido. No overwrite CONFIRMED↔REFUTED.

## 7. Risk y VetoDecision

RiskDomainOutcome no equivale a VetoDecision. ASSESSED_WITH_FINDINGS no crea automáticamente VETO_ACTIVE. ASSESSED_NO_MATERIAL_FINDING solo contribuye a NO_VETO_IDENTIFIED con cobertura completa y vigente. UNKNOWN, NOT_ASSESSED, ASSESSMENT_IN_PROGRESS, STALE y CONFLICTED no prueban ausencia de veto. INVALID se trata como error tipado, no outcome factual.

VetoDecision: VETO_NOT_ASSESSED, VETO_REVIEW_REQUIRED, VETO_ACTIVE, NO_VETO_IDENTIFIED.

Transiciones:
- VETO_NOT_ASSESSED→VETO_REVIEW_REQUIRED ante alerta material.
- VETO_NOT_ASSESSED→NO_VETO_IDENTIFIED solo con cobertura completa, evidencia suficiente y vigente.
- VETO_REVIEW_REQUIRED→VETO_ACTIVE con condición crítica verificada y decisión autorizada.
- VETO_REVIEW_REQUIRED→NO_VETO_IDENTIFIED tras investigación que descarte la alerta y satisfaga cobertura.
- NO_VETO_IDENTIFIED→VETO_REVIEW_REQUIRED ante nueva alerta.
- VETO_ACTIVE→VETO_REVIEW_REQUIRED al presentarse remediación para validación.

**Prohibido VETO_ACTIVE→NO_VETO_IDENTIFIED directamente.** La ruta exige remediación, revisión independiente de evidencia/scope/eficacia y autorización de Veto Release Authority distinta del proponente original. Si falla, el veto permanece activo. TTL, silencio o anuncio del proyecto no lo levantan. Esto aborda R018-10 en propuesta, pendiente de aprobación Risk/SoD.

## 8. RouteEligibility

X-04 legacy enum se conserva como vocabulario fuente. R01.10 propone:
- RouteAssessmentWorkflow: NOT_ASSESSED, ASSESSMENT_IN_PROGRESS, REVIEW_REQUIRED, COMPLETE.
- ValidationEnvelope: VALID, INCOMPLETE, REVIEW_REQUIRED, REJECTED_INVALID_INPUT.
- FreshnessStatus independiente.
- RouteEligibilityOutcome: ROUTE_ELIGIBLE, ROUTE_ELIGIBLE_WITH_LIMITATIONS, ROUTE_INELIGIBLE, ROUTE_REVIEW_REQUIRED.

El outcome ROUTE_REVIEW_REQUIRED no es el workflow REVIEW_REQUIRED; el contrato debe distinguirlos.

Transiciones: sin outcome→ELIGIBLE solo con envelope VALID, scope exacto, workflow COMPLETE, freshness aceptable y controles satisfechos; →ELIGIBLE_WITH_LIMITATIONS con límites enforceable; →INELIGIBLE por incumplimiento afirmativo probado; →REVIEW_REQUIRED ante alerta/conflicto adjudicable. Outcomes favorables pueden pasar a REVIEW_REQUIRED por evento material. REVIEW_REQUIRED puede resolverse a eligible/limited/ineligible solo tras revisión y recomposición. Cambios materiales de route/account/venue/side/size/jurisdiction requieren nueva evaluación.

Crosswalk X-04: NOT_ASSESSED/ASSESSMENT_IN_PROGRESS se mapean a workflow; STALE a freshness; INVALID a error tipado; INSUFFICIENT_INFORMATION a INCOMPLETE/causa; CONFLICTED a revisión/conflicto; REVIEW_REQUIRED queda sin mapping automático si falta contexto. Estados elegibles requieren revalidación de todos los gates.

## 9. SelectionDecision y OpportunityDisposition

DecisionOutcome X-06: DECISION_NOT_ASSESSED, DECISION_IN_PROGRESS, DECISION_REVIEW_REQUIRED, DECISION_INELIGIBLE, DECISION_ABSTAIN, DECISION_ELIGIBLE_WITH_LIMITATIONS, DECISION_ELIGIBLE.

Transiciones: NOT_ASSESSED→IN_PROGRESS; IN_PROGRESS→REVIEW_REQUIRED/ABSTAIN/INELIGIBLE/ELIGIBLE_WITH_LIMITATIONS/ELIGIBLE conforme a guards X-06; REVIEW_REQUIRED→IN_PROGRESS tras adjudicación o a outcome final mediante nueva composición. Outcome final no se sobrescribe en la misma versión. ValidationEnvelope VALID del mismo átomo es precondición para favorable.

OpportunityDisposition es separado; no existe catálogo completo aprobado en las fuentes revisadas. No se inventan valores ni transiciones.

## 10. Eventos, concurrencia e idempotencia

Cada transición aceptada produce evento append-only con event_id, event_type/schema_version, aggregate_id/version, from/to, catalog_version, scope/digest, actor/role/authority, occurred_at/observed_at/recorded_at/effective_at, source_sequence, payload_digest, evidence_refs, reason_code, causal parents, idempotency_key, causation/correlation IDs, policy/schema/algorithm versions y approval refs.

Guards: catálogo reconocido; estado origen coincidente; contrato válido; actor/autoridad vigente; scope exacto; evidencia y SoD; policy compatible; transición permitida; no duplicidad; dependency closure suficiente; control de concurrencia.

Orden: secuencia autenticada de fuente cuando proceda, causalidad explícita, aggregate version/CAS y adjudicación. recorded_at o llegada al bus no bastan. Evento duplicado exacto es no-op; mismo event_id con digest distinto es integrity failure. Eventos tardíos generan evaluación de impacto sin reescribir log. Eventos concurrentes incompatibles se conservan y adjudican; nunca last-write-wins. Retry no duplica efectos.

## 11. Invalidación atómica y replay

Invalidación se aplica al AtomicAssessmentRecord/atom_scope exacto y se propaga solo por dependencias tipadas demostradas. Dependency closure UNKNOWN no equivale a NOT_AFFECTED. La incertidumbre bloquea la conclusión dependiente, no autoriza invalidación universal. Legacy records no atómicos siguen R01.9: no consumir subsets hasta reconstrucción.

Historical replay reconstruye decisión con inputs, policies, schemas, mappings, algoritmo, catálogo, cutoff y eventos originales. Current reassessment crea nueva conclusión con versiones actuales. Replay exige log íntegro, snapshots, digest reproducible, scope/partition exactos, versiones y autoridad histórica. Replay de terminales es solo auditoría; no reactiva ni autoriza uso actual. Faltantes producen resultado incompleto, no aproximación silenciosa.

## 12. Productores, consumidores y SoD

| Objeto | Producer | Reviewer/Authority | Consumer |
|---|---|---|---|
| Workflow | Assessment Producer | Function Owner / Independent Reviewer | Domain consumers |
| Lifecycle | Lifecycle/Incident Owner | Lifecycle Authority | Consumers |
| Evidence | Evidence/Sufficiency Assessor | Data Quality Owner | Claim/PA/EA/Risk |
| Claim | Claim Assessor | Claim Adjudicator | PA/EA/Risk |
| Risk | Risk Analyst | Risk Approver / Independent Reviewer | Veto/Decision/Route |
| Veto | Veto Proposer/Composer | Veto Approver/Release Authority | X-06/ORE |
| Route | Route Assessment Owner | Route Eligibility Authority | Portfolio & Execution gate |
| Decision | X-06 Composer | Decision Approver if mandated | Candidate Registry |
| Opportunity | Owner pendiente | Authority pendiente | Candidate Registry |

Producer no debe ser único validador/aprobador de un assessment crítico. Veto Proposer no puede ser Veto Release Authority. Lifecycle operator no adjudica mérito del dominio. Decision Composer no modifica inputs. Route assessor no autoriza operaciones. Model Owner no valida su propio modelo. IAM admin no adquiere autoridad de negocio. Auditor no produce ni aprueba lo auditado.

X-08 sigue sin titulares, grupos RBAC/ABAC, quorum, delegaciones o independencia probada. Los roles son funcionales, no permisos reales.

## 13. Escenarios e invariantes

Escenarios propuestos:
1. COMPLETE + INELIGIBLE conserva ambos ejes.
2. ACTIVE + NOT_STARTED no se consume como completo.
3. COMPLETE + SUPERSEDED solo replay histórico.
4. STALE no cambia lifecycle.
5. COMPLETE sin inputs obligatorios se rechaza.
6. VETO_ACTIVE→NO_VETO directo se rechaza.
7. Remediación no validada mantiene veto.
8. Risk findings sin regla no fabrican veto.
9. ROUTE_INVALID por schema error no equivale a INELIGIBLE.
10. ROUTE_REVIEW_REQUIRED ambiguo queda UNMAPPED/REVIEW.
11. DECISION_ELIGIBLE con envelope no VALID se rechaza.
12. Duplicado exacto es no-op.
13. Mismo event_id con digest distinto es integrity failure.
14. Eventos concurrentes incompatibles requieren adjudicación.
15. Invalidación de átomo A no afecta B disjunto probado.
16. Dependency closure desconocida no demuestra independencia.
17. Replay histórico no habilita uso actual.
18. Withdrawal sin autoridad se rechaza.

Invariantes:
- Workflow, lifecycle, freshness, validity y domain outcome son independientes.
- COMPLETE no implica favorable, ACTIVE ni FRESH.
- Lifecycle terminal no se reactiva en la misma versión.
- STALE no implica INVALIDATED.
- INVALID no es outcome factual.
- EvidenceAdmissibility no implica EvidenceSufficiency.
- UNKNOWN no se convierte en FALSE/EMPTY/NOT_APPLICABLE.
- RiskDomainOutcome no equivale a VetoDecision.
- VETO_ACTIVE no se levanta directamente a NO_VETO_IDENTIFIED.
- RouteEligibility no implica TradeAuthorization.
- DecisionOutcome no implica OpportunityDisposition ni ejecución.
- Outcome favorable exige envelope VALID del mismo átomo.
- Eventos duplicados no duplican efectos.
- Eventos concurrentes no usan last-write-wins.
- Invalidación se propaga solo por scope/dependencias demostrados.
- Replay histórico no reactiva objetos.
- SoD exige asignaciones y controles verificables.

## 14. Hallazgos y cuestiones abiertas

| Hallazgo | Estado propuesto |
|---|---|
| R018-06 workflow/lifecycle | ADDRESSED_IN_DRAFT; canónicos/migración pendientes |
| R018-10 transición intermedia veto | ADDRESSED_IN_DRAFT; Risk/SoD approval pendiente |
| R018-11 Identity/Freshness | PARTIALLY_ADDRESSED; consumer crosswalk pendiente |
| R018-12 authority/SoD | NOT_RESOLVED; asignaciones/aprobación pendientes |
| R018-04 Decision catalog | ADDRESSED_IN_DRAFT; OpportunityDisposition pendiente |
| R018-05 Route catalog | PARTIALLY_ADDRESSED; owner/migración pendientes |
| R018-07 Evidence enum | ADDRESSED_IN_DRAFT; migration tests pendientes |
| R018-08 Risk INVALID | PARTIALLY_ADDRESSED; taxonomy/canónico pendientes |

Riesgos restantes: Route REVIEW_REQUIRED dual; RiskDomainOutcome y error codes sin ratificación; OpportunityDisposition sin catálogo; Identity/Freshness consumer matrix incompleta; terminality e invalidación parcial por átomo por formalizar; veto quorum/excepciones sin política; X-08 sin titulares; orden temporal por tipo de fuente pendiente; no hay inventario de legacy ni pruebas.

## 15. Gates

**Gate A — Semántica:** aprobar catálogos, transiciones, mappings prohibidos, Route REVIEW_REQUIRED dual, OpportunityDisposition, Risk outcomes y crosswalk Identity/Freshness.

**Gate B — Gobierno:** aprobar X-08, RACI, SoD, quorum, delegaciones, autoridad independiente de veto/release y lifecycle.

**Gate C — Contratos:** versionar catálogos machine-readable; definir schemas de TransitionEvent, AssessmentRecord, VetoDecision, RouteEligibility, DecisionOutcome y replay; enlazar envelope/proofs por átomo; implementar event processor, idempotency store y optimistic concurrency.

**Gate D — Verificación:** fixtures válidos/ilegales y legacy; contract/unit/integration/property/concurrency tests; verificar scope, veto, terminality, replay y no-favorability amplification; revisión independiente y evidencia reproducible.

## 16. Estado y límites

**PROPOSED / PENDING_ARCHITECTURE_APPROVAL / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED.**

Revisión documental estática. X-04 se confirmó en documento 09 y se cotejó con X-06/R01.10. No se modificaron canónicos, requisitos IIM, schemas, código, fixtures o tests. No se ejecutaron pruebas automatizadas, concurrencia, formales o empíricas. No hay aprobación institucional ni certificación ISO. R01.11 no autoriza inversión, custodia, trading ni ejecución real.
