# X-09 — Matriz de trazabilidad y catálogo de transiciones

**ID:** IIE-METH-TRACE-STATE-012  
**Versión:** 0.2.0-draft  
**Fecha:** 2026-10-31  
**Estado:** DRAFT / REVIEW_REQUIRED / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED  
**Objeto:** resolver documentalmente A09 y A10 de la auditoría `15-x09-cross-document-consistency-audit.md`.  
**Matriz base:** `14-integrated-conformance-scenario-matrix.md` v0.3.0-draft.

## 1. Dictamen y límites

Este documento crea una primera trazabilidad bidireccional entre escenarios, invariantes, reglas fuente, requisitos IIM candidatos y futuros identificadores de pruebas; además inventaría las máquinas de estados y distingue transiciones documentadas de brechas.

**Dictamen:** A09 y A10 quedan `ADDRESSED_IN_DRAFT`, no cerrados. Los crosswalks son preliminares y los inventarios identifican máquinas sin transiciones completas. No se han creado fixtures ni tests ejecutables. No se ejecutaron pruebas. No se modificaron requisitos normativos, schemas ni código.

Convenciones:
- `MAPPED_DRAFT`: enlace documental propuesto identificable.
- `PARTIAL_DRAFT`: mapeo o expected outcome requiere definición/validación.
- `GAP`: catálogo, owner, transición o relación no definido.
- `TEST_NOT_IMPLEMENTED` y `NOT_EXECUTED`: no existe evidencia de ejecución.

Los futuros test IDs son reservas de trazabilidad, no tests implementados:
- Escenarios individuales: `TC-X09-S-001` … `TC-X09-S-084`.
- Escenarios combinados: `TC-X09-C-01` … `TC-X09-C-16`.
- Invariantes: `TC-X09-INV-01` … `TC-X09-INV-29`.

## 2. A09 — Crosswalk de escenarios individuales

Los rangos de la primera columna expanden a cada escenario individual; por ejemplo, S-001..S-008 asigna un futuro test ID a cada S-001, S-002, …, S-008. Ninguna fila implica que el test esté implementado.

| Escenarios | Reglas fuente | Dimensiones/catálogos | Requisitos IIM candidatos | Test IDs | Estado |
|---|---|---|---|---|---|
| S-001..S-008 | M-02 §4,6; M-03; X-07 §6–9; X-08 §7 | Identidad, evidencia, snapshot, SoD | IIM-001,002,008,018,019,023 | TC-X09-S-001..008 | PARTIAL_DRAFT |
| S-009..S-016 | M-04 §6–8; X-01/X-02; X-06 §7,9 | Aplicabilidad, RiskDomainStatus, cobertura veto | IIM-006,016,017,020,022 | TC-X09-S-009..016 | PARTIAL_DRAFT |
| S-017..S-024 | M-04 §7,9,12; X-05; X-06 §7,9; X-08 §6–10 | Veto, propagación, release, autoridad | IIM-006,017,022,024 | TC-X09-S-017..024 | PARTIAL_DRAFT |
| S-025..S-030 | M-03 §5–9; X-04 §3–8; X-06 §3,5,9 | AE, ORE, autorización y enforcement | IIM-020,022,024; IIM-025,032–035 propuestos | TC-X09-S-025..030 | PARTIAL_DRAFT |
| S-031..S-036 | X-06 §4–10; X-07 §4,8–9 | Versiones, validación, composición | IIM-008,021–024 | TC-X09-S-031..036 | PARTIAL_DRAFT |
| S-037..S-046 | X-07 §6–10; M-04 §9,11; X-06 §4.4 | Lifecycle, dependencia, invalidación, replay | IIM-008,017,018,023,024; IIM-037 propuesto | TC-X09-S-037..046 | PARTIAL_DRAFT |
| S-047..S-060 | X-08 §5–14; X-07 §8–10 | RACI, SoD, delegación, excepción, IAM, auditoría | IIM-023,024; IIM-033–037 propuestos | TC-X09-S-047..060 | PARTIAL_DRAFT |
| S-061..S-070 | X-02; X-05; X-06 §7; X-07 §7–8 | RSK-01..06, RSK-11; CAT-02/04/05/06 | IIM-004,006,011,012,016,020,021 | TC-X09-S-061..070 | PARTIAL_DRAFT |
| S-071..S-080 | X-02; X-04; X-05; X-07 | RSK-05..12, liquidez, legal, oracle, dependencias | IIM-002,006,014,016,018,020; IIM-029,030,032,035 propuestos | TC-X09-S-071..080 | PARTIAL_DRAFT |
| S-081 | X-05 V-01; M-02 identidad; X-06 scope | Identidad/fraude de activo | IIM-001,019,020 | TC-X09-S-081 | PARTIAL_DRAFT |
| S-082 | X-05; X-06 §3,7,9; X-04 | Dependencia WITHDRAWAL↔TRADE | IIM-029,032–034 propuestos | TC-X09-S-082 | PARTIAL_DRAFT |
| S-083..S-084 | X-04 §3,5; X-06 §9.2 | Lifecycle ORE y revisión | IIM-032–034 propuestos | TC-X09-S-083..084 | PARTIAL_DRAFT |

Para pasar de PARTIAL_DRAFT a MAPPED_APPROVED, cada escenario debe tener un registro expandido con regla_id y versión exactos, purpose/activity, atomic scope, input manifest, expected outcome tipado, causa, requisito validado por Requirement Owner y test_id.

## 3. A09 — Crosswalk de escenarios combinados

| ID | Reglas fuente | Invariantes | Futuro test | Estado |
|---|---|---|---|---|
| C-01 | X-05; X-06 §7,9; X-04; X-08 | INV-04,07,11 | TC-X09-C-01 | MAPPED_DRAFT |
| C-02 | X-05; M-03; X-04 | INV-05,06,07 | TC-X09-C-02 | MAPPED_DRAFT |
| C-03 | M-04 §9,11; X-07 §9; X-08 | INV-04,09,18,26 | TC-X09-C-03 | MAPPED_DRAFT |
| C-04 | M-04 §7–8; X-06 §7,9 | INV-01,03,21 | TC-X09-C-04 | MAPPED_DRAFT |
| C-05 | X-04; X-06 §5,9; X-08 | INV-04,07,11 | TC-X09-C-05 | MAPPED_DRAFT |
| C-06 | M-04 §7,9; X-07; X-06 | INV-11,13,26 | TC-X09-C-06 | MAPPED_DRAFT |
| C-07 | X-05 V-04; X-06 §3,7; X-07 §8 | INV-05,11,27 | TC-X09-C-07 | MAPPED_DRAFT |
| C-08 | X-04; X-07; X-08 §10–12 | INV-15,16,19,28 | TC-X09-C-08 | MAPPED_DRAFT |
| C-09 | X-07; X-06 §4,8; X-08 | INV-12,13,23 | TC-X09-C-09 | MAPPED_DRAFT |
| C-10 | M-04 §9; X-08 §6–7 | INV-04,15,16,26 | TC-X09-C-10 | MAPPED_DRAFT |
| C-11 | M-02 identity; X-06 §3–4,9 | INV-01,02,23 | TC-X09-C-11 | MAPPED_DRAFT |
| C-12 | X-08 conflict/recusal; X-07 | INV-15,16,24 | TC-X09-C-12 | MAPPED_DRAFT |
| C-13 | X-06; X-07; portfolio controls proposal | INV-08,12,23 | TC-X09-C-13 | PARTIAL_DRAFT |
| C-14 | X-05 scope; X-06 §3,6–7 | INV-05,11,27 | TC-X09-C-14 | MAPPED_DRAFT |
| C-15 | X-02 applicability; X-07 §7–9 | INV-21,23,27 | TC-X09-C-15 | MAPPED_DRAFT |
| C-16 | X-06; X-08; X-07 | INV-08,15,17,23 | TC-X09-C-16 | MAPPED_DRAFT |

## 4. A09 — Crosswalk de invariantes

| Invariante | Escenarios mínimos | Regla fuente | Test futuro |
|---|---|---|---|
| INV-01 Fail-closed | S-009,026,035,042; C-04 | X-06 §4,8–9; X-07 §8 | TC-X09-INV-01 |
| INV-02 No risk laundering | S-003,004,009,014,022,039; C-11 | M-04 §6,11; X-07 §9 | TC-X09-INV-02 |
| INV-03 No veto laundering | S-009,016,021,040; C-04 | M-04 §7–8; X-06 §7 | TC-X09-INV-03 |
| INV-04 Veto non-compensation | S-018,022,023; C-01,03,05,10 | M-04 §8–9; X-06 §7,9 | TC-X09-INV-04 |
| INV-05 Scope confinement | S-019,020,032,061,072; C-02,07,14 | X-05; X-06 §3–4,7 | TC-X09-INV-05 |
| INV-06 AE/ORE independence | S-025,027,032; C-02 | M-03 §5–9; X-04 | TC-X09-INV-06 |
| INV-07 No trade implication | S-026,028,029,030; C-01,05 | M-03; X-04 §8; X-06 §3.2 | TC-X09-INV-07 |
| INV-08 No stale authorization | S-030,036,052,058; C-13,16 | X-06 §4,8; X-07 §7–10; X-08 | TC-X09-INV-08 |
| INV-09 No TTL release | S-022,039,040; C-03 | M-04 §9,11; X-07 §9 | TC-X09-INV-09 |
| INV-10 Source immutability | S-008,045,060 | X-06 §2,8; X-08 §12 | TC-X09-INV-10 |
| INV-11 Cause preservation | S-010,033,037; C-01,06,14 | X-06 §6,8–10 | TC-X09-INV-11 |
| INV-12 Determinism | S-034,043,052; C-09,13 | X-06 §2,8; X-07 §8 | TC-X09-INV-12 |
| INV-13 Historical reproducibility | S-006,038,041,046; C-09 | X-07 §2,6,9; X-06 §8 | TC-X09-INV-13 |
| INV-14 Idempotency | S-043,044 | X-07 §8,10 | TC-X09-INV-14 |
| INV-15 SoD enforcement | S-008,024,045,047–057,059; C-08,12 | X-08 §7,9–12 | TC-X09-INV-15 |
| INV-16 No self-approval | S-024,048,049,053,057,059; C-10 | X-08 §7,9,11 | TC-X09-INV-16 |
| INV-17 Delegation scope | S-050,051,053; C-16 | X-08 §9,11 | TC-X09-INV-17 |
| INV-18 Exception expiry | S-050,058; C-03 | X-08 §8–10; X-07 | TC-X09-INV-18 |
| INV-19 No break-glass bypass | S-054,055; C-08 | X-08 §10; X-04 §8 | TC-X09-INV-19 |
| INV-20 Audit independence | S-056,060 | X-08 §7,13–14 | TC-X09-INV-20 |
| INV-21 Applicability integrity | S-005,011–013,015,016; C-04,15 | X-02; M-04 §6 | TC-X09-INV-21 |
| INV-22 Partial propagation honesty | S-042–044 | X-07 §8; X-06 §4,7 | TC-X09-INV-22 |
| INV-23 Version coherence | S-007,031,036,046,052; C-09,11,15,16 | X-06 §4,8; X-07 §6–9 | TC-X09-INV-23 |
| INV-24 No hidden override | S-045,049,052,054,058; C-12 | X-08 §8–12; X-07 | TC-X09-INV-24 |
| INV-25 Typed outcomes | S-025–036,082–084 | X-04; X-06 §4–5,9 | TC-X09-INV-25 |
| INV-26 Veto freshness separation | S-022,023,039,040; C-03,06,10 | M-04 §7,9,11; X-07 §6,9 | TC-X09-INV-26 |
| INV-27 Dependency uncertainty | S-019,037,042,044,072,079,082; C-07,14 | X-05; X-07 §8; X-06 §3–4 | TC-X09-INV-27 |
| INV-28 Emergency action confinement | S-054,055; C-08 | X-08 §10; X-09 v0.2 §16.8 | TC-X09-INV-28 |
| INV-29 Service identity least authority | S-053,057 | X-08 §11; X-09 v0.2 §16.9 | TC-X09-INV-29 |

### 4.1 Requisitos IIM y trazabilidad normativa

Los IIM indicados son candidatos de la matriz maestra de trazabilidad, no cambios al registro normativo. IIM-025..037 continúan siendo propuestas. Un escenario no demuestra por sí mismo que un requisito esté satisfecho.

Los requisitos que la matriz maestra mantiene en PARTIAL o GAP conservan ese estado: IIM-004,006,007,012,014–017,020–022,024 y, en Portfolio & Execution, IIM-033,034,036,037. Los owners deben validar cada vínculo exacto antes de aprobar el test mapping.

### 4.2 Criterios de cierre A09

- Cada S-001..S-084 y C-01..C-16 se expande a un registro individual con source_rule_id/version, purpose/activity, atomic scope, input manifest, expected outcome tipado y test_id.
- INV-01..INV-29 tienen pruebas positivas/negativas, límites y mutaciones cuando corresponda.
- Requirement Owner valida cada vínculo IIM; los GAP permanecen visibles.
- Existe verificación automática de escenarios/invariantes huérfanos y referencias rotas.
- Los owners aprueban crosswalk y expected outcomes.

**Estado A09: ADDRESSED_IN_DRAFT / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED.**

## 5. A10 — Inventario de máquinas de estados

**Inventario:** `IIE-X09-STATE-INVENTORY-0.1.0-draft`  
**Regla de evidencia:** solo las transiciones presentes en documentos fuente se marcan SOURCE_DEFINED_DRAFT. No se infiere una transición por la mera existencia de dos estados.

| ID | Máquina | Fuente/catálogo | Transiciones documentadas | Estado |
|---|---|---|---|---|
| SM-01 | Asset Identity / IdentityAssessment | M-02 §4,6 | UNRESOLVED→RESOLVED; CONFLICTED→RESOLVED tras reconciliación; QUARANTINED→ADMISSIBLE/REJECTED tras investigación | SOURCE_DEFINED_DRAFT; guards/roles parciales |
| SM-02 | EvidenceAssessment | M-02 §4; X-07 §6–9 | Eventos EVIDENCE_RECEIVED/CORRECTED/WITHDRAWN/COMPROMISED y reevaluación; mapping a estados de assessment incompleto | GAP |
| SM-03 | ApplicabilityAssessment | M-04 §6; X-02 | APPLICABLE, NOT_APPLICABLE, APPLICABILITY_UNRESOLVED, APPLICABILITY_CONFLICTED; transiciones no enumeradas | GAP |
| SM-04 | RiskDomainAssessment | M-04 §5–6; X-07 §9 | Ocho estados definidos; transición exhaustiva origen/evento/guard/destino no definida | GAP |
| SM-05 | VetoDecision | M-04 §7,9; X-07 §9 | Siete transiciones enumeradas en §6 | SOURCE_DEFINED_DRAFT |
| SM-06 | AnalyticalEligibilityDecision | M-02; M-03 §4–6 | Estados y consecuencias descritos; grafo de transición no completo | GAP |
| SM-07 | OperationalRouteEligibilityDecision | X-04 §3,5–6 | Diez estados, precedencia y migración ROUTE_UNKNOWN; grafo de transición ausente | GAP |
| SM-08 | SelectionDecision / DecisionOutcome | M-02 §4.8,5; X-06 §5,9 | Composición y precedencia; ciclo de vida del decision record no enumerado | GAP |
| SM-09 | ValuationStatus | M-02 §4.7 | Cinco outcomes; transición completa no definida | GAP |
| SM-10 | Lifecycle object status | X-07 §6,9 | STALE, INVALIDATED, SUPERSEDED, REVIEW_REQUIRED/QUARANTINED y eventos; guards/actors no exhaustivos | PARTIAL_DRAFT |
| SM-11 | Governance approval/delegation/exception | X-08 §5–12 | Aprobación, rechazo, recusación, delegación, expiración, break-glass; workflows formales por objeto incompletos | GAP |
| SM-12 | TradeAuthorization/order-control | Portfolio Execution proposal; X-04 §8; X-08 | Separación de autorización y controles; transición order lifecycle no definida | GAP |

## 6. A10 — Transiciones fuente de VetoDecision

| Desde | Hacia | Evento/guard mínimo | Consecuencia |
|---|---|---|---|
| VETO_NOT_ASSESSED | VETO_REVIEW_REQUIRED | Señal material/alerta que requiere investigación | Abrir revisión en scope |
| VETO_NOT_ASSESSED | NO_VETO_IDENTIFIED | Cobertura completa de reglas obligatorias y dominios materiales con evidencia suficiente | Concluir solo para scope revisado |
| NO_VETO_IDENTIFIED | VETO_REVIEW_REQUIRED | Nueva alerta creíble o evidencia contradictoria | Suspender conclusión favorable en scope |
| VETO_REVIEW_REQUIRED | VETO_ACTIVE | Condición crítica verificada y vigente | Bloqueo no compensable |
| VETO_REVIEW_REQUIRED | NO_VETO_IDENTIFIED | Investigación documentada descarta alerta en scope | Cerrar revisión conservando historial |
| VETO_ACTIVE | VETO_REVIEW_REQUIRED | Evidencia inicial de remediación aún no validada | Mantener bloqueo durante revisión |
| VETO_ACTIVE | NO_VETO_IDENTIFIED | Remediación y ausencia de condición bloqueante verificadas con autoridad/revisión exigidas | Nueva decisión versionada; nunca mutar historial |

Prohibiciones explícitas: TTL/silencio/ausencia de incidentes no libera veto; RiskDomainStatus no modifica por sí solo VetoDecision; Lifecycle Engine no libera vetos.

## 7. A10 — Lifecycle: eventos y transiciones definidas parcialmente

Eventos X-07: OBJECT_CREATED, EVIDENCE_RECEIVED, EVIDENCE_CORRECTED, SOURCE_WITHDRAWN, SOURCE_COMPROMISED, FRESHNESS_WARNING, FRESHNESS_EXPIRED, MATERIAL_CHANGE_DETECTED, CONFLICT_DETECTED, INTEGRITY_FAILURE, ASSESSMENT_STARTED, ASSESSMENT_COMPLETED, REVIEW_OPENED/CLOSED, VETO_ISSUED, VETO_REMEDIATION_PROPOSED/VERIFIED, POLICY_CHANGED, DEPENDENCY_CHANGED, OBJECT_SUPERSEDED, OBJECT_INVALIDATED y REASSESSMENT_REQUESTED/DEFERRED.

- STALE: incumplimiento de freshness policy; no demuestra falsedad.
- INVALIDATED: evento/error demuestra que el objeto no debe consumirse para el scope/uso.
- SUPERSEDED: versión nueva reemplaza función actual; versión anterior permanece auditable.
- REVIEW_REQUIRED/QUARANTINED: señal/conflicto material no resuelto, con scope, owner y review_at.
- Recuperación: nueva evidencia admisible, reevaluación, revisión independiente cuando aplique y nueva versión; nunca editar historial.

**GAP:** faltan tablas exhaustivas por tipo de objeto con estado origen, evento, guard, actor/autoridad, destino y side effects.

## 8. A10 — ORE: catálogo sin grafo de transición completo

X-04 define los diez estados, precedencia y migración desde ROUTE_UNKNOWN, pero no define un grafo exhaustivo entre estados. No se inventa aquí. Una reevaluación debe producir nuevo decision record/version; cambios materiales de actividad, venue, cuenta, tamaño, red, custodio, jurisdicción o entorno requieren nueva clave o invalidación explícita. ROUTE_INELIGIBLE no pasa a ROUTE_ELIGIBLE sin nueva evidencia y composición. ROUTE_UNKNOWN no se migra globalmente.

## 9. Transiciones prohibidas transversales

| Operación/transición | Estado | Fuente |
|---|---|---|
| VETO_ACTIVE→NO_VETO_IDENTIFIED por TTL | PROHIBITED | M-04 §9; X-07 §9 |
| VETO_ACTIVE liberado por Lifecycle Owner/engine | PROHIBITED | M-04 §9; X-08 §12 |
| VETO_NOT_ASSESSED→NO_VETO_IDENTIFIED sin cobertura | PROHIBITED | M-04 §7–8 |
| UNKNOWN/STALE/CONFLICTED/INVALID→favorable por renombrado | PROHIBITED | M-04 §6; X-06 §7–9 |
| ROUTE_INELIGIBLE→ROUTE_ELIGIBLE sin reevaluación | PROHIBITED | X-04 §2,7 |
| ORE favorable→TradeAuthorization implícita | PROHIBITED | X-04 §8; X-08 |
| Excepción expirada→autorización activa por caché | PROHIBITED | X-08 §8–11 |
| Service identity→decisión no delegable | PROHIBITED | X-08 §9,11 |
| Break-glass→RELEASE_VETO/ENABLE_TRADING | PROHIBITED | X-08 §10 |
| Decision compositor→mutar assessment fuente | PROHIBITED | X-06 §2,8 |

## 10. Métricas y aceptación A10

No reportar porcentajes hasta aprobar el denominador por state_machine_id/version. Métricas:
- State coverage = estados con al menos un test / estados aprobados.
- Allowed-transition coverage = transiciones permitidas probadas / transiciones permitidas aprobadas.
- Forbidden-transition coverage = transiciones prohibidas probadas negativamente / transiciones prohibidas aprobadas.
- Guard coverage = guards probados / guards inventariados.

Un test que solo menciona un estado no cuenta como transición cubierta. Reportar por máquina y versión, con pass/fail/skipped, entorno y evidencia.

A10 solo se cierra cuando SM-01..SM-12 tengan catálogo y owner aprobado (o exclusión justificada), cada transición tenga evento/guard, autoridad, scope, destino y side effects, existan pruebas negativas, haya versionado/migración y Governance, Risk, Architecture, Operations, Security y Legal/Compliance aprueben lo que les compete.

**Estado A10: ADDRESSED_IN_DRAFT / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED.**

## 11. Registro final de hallazgos

| Hallazgo | Incorporación | Estado |
|---|---|---|
| A09 | Crosswalk escenarios S/C, invariantes, reglas fuente, IIM candidatos y futuros test IDs | ADDRESSED_IN_DRAFT |
| A10 | Inventario SM-01..SM-12, transiciones VetoDecision, lifecycle, ORE gaps y transiciones prohibidas | ADDRESSED_IN_DRAFT |

No se afirma cobertura completa de transiciones, aprobación institucional, implementación ni validación empírica.


## 12. Actualización de trazabilidad A11/A12 — X-09 v0.4.0

La ampliación de valoración wrapped y agregación atómica se especifica en [17-x09-wrapped-valuation-and-atomic-aggregation.md](17-x09-wrapped-valuation-and-atomic-aggregation.md).

### 12.1 Nuevos escenarios individuales

| Escenarios | Regla fuente | Dimensión | Requisito IIM candidato | Test futuro | Estado |
|---|---|---|---|---|---|
| S-085..S-087 | M-02 valuation; X-05 V-04; X-06 §4,9; valuation catalog | Identidad/derechos, evidencia, método, wrapper | IIM-004,006,011,020; mappings exactos requieren owner | TC-X09-S-085..087 | PARTIAL_DRAFT |
| S-088..S-091 | X-05 V-04; X-06 §4,9; X-02 CAT-04 | Modelo alternativo, veto independiente, native/wrapped | IIM-004,006,011,016,020 | TC-X09-S-088..091 | PARTIAL_DRAFT |
| S-092..S-095 | X-06 §3,4,9,10; X-05; M-03 | Partición, scopes, causas múltiples, limitación exigible | IIM-020,022,024; IIM-025,032–035 propuestos | TC-X09-S-092..095 | PARTIAL_DRAFT |
| S-096..S-099 | X-06 §9–10; X-05; X-04 | Enforcement, ORE heterogéneo, dependencias, exhaustividad | IIM-022,024; IIM-029,032–035 propuestos | TC-X09-S-096..099 | PARTIAL_DRAFT |

### 12.2 Nuevas invariantes

| Invariante | Escenarios mínimos | Regla fuente | Test futuro | Estado |
|---|---|---|---|---|
| INV-30 Wrapped valuation separation | S-086..S-091 | X-05 V-04; X-06 §4,9 | TC-X09-INV-30 | PARTIAL_DRAFT |
| INV-31 Representation non-transitivity | S-091, S-092, C-07 | X-05 V-04; X-06 §3,10 | TC-X09-INV-31 | PARTIAL_DRAFT |
| INV-32 Valuation evidence semantics | S-085..S-090 | X-06 §4,9; valuation catalog | TC-X09-INV-32 | PARTIAL_DRAFT |
| INV-33 Atomic partition integrity | S-092..S-099 | X-06 §3,4,10 | TC-X09-INV-33 | PARTIAL_DRAFT |
| INV-34 Cause conservation | S-094, S-098; C-14 | X-06 §6,10 | TC-X09-INV-34 | PARTIAL_DRAFT |
| INV-35 No heterogeneous global permission | S-092, S-093, S-097; C-07 | X-06 §10; X-04 | TC-X09-INV-35 | PARTIAL_DRAFT |

### 12.3 Requisitos de aceptación adicionales

- Los Requirement Owners deben validar la relación de IIM; no se asigna cumplimiento por escenario.
- Valuation/Risk/Legal deben aprobar W-01..W-07, incluida la distinción INSUFFICIENT_EVIDENCE vs NOT_ESTIMABLE.
- Architecture/Operations deben aprobar el algoritmo de partición, scope algebra, dependencias y enforcement.
- La precedencia agregada cuando coexisten outcomes INELIGIBLE, REVIEW y ABSTAIN requiere decisión de owners. Hasta entonces, conservar vector por unidad y ValidationEnvelope.REVIEW_REQUIRED para la agregación ambigua.
- Los escenarios no son tests ejecutables y no han sido ejecutados.

**Estado A11/A12: ADDRESSED_IN_DRAFT / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED.**
