# R01.4 — Resolución del lifecycle canónico de assessments

**ID:** IIE-IDENTITY-R01-04  
**Versión:** 0.1.0-draft  
**Fecha:** 2026-10-31  
**Estado:** PROPOSED / PENDING_ARCHITECTURE_APPROVAL / NOT_APPROVED  
**Hallazgo:** ICA-07 y divergencias de lifecycle entre modelo canónico, framework transversal y D-10

## 1. Decisión propuesta

La revisión identifica una colisión semántica: algunos documentos usan una sola lista para describir el progreso del assessment y la vigencia/validez del registro. No son la misma dimensión.

Se proponen **dos máquinas ortogonales**, con una tercera dimensión independiente para freshness:

1. AssessmentWorkflowStatus: progreso y resultado procedimental de la evaluación.
2. RecordLifecycleStatus: condición de vigencia/uso actual de una versión inmutable del registro.
3. FreshnessStatus: vigencia temporal respecto de uso, scope, policy y cutoff (R01.1).

La unificación correcta consiste en definir dos máquinas canónicas compartidas y sus contratos de interacción, no en mezclar estados de naturaleza distinta.

## 2. Máquina A — AssessmentWorkflowStatus

### 2.1 Vocabulario propuesto

| Estado | Definición | ¿Terminal? |
|---|---|---|
| NOT_STARTED | El expediente está creado, pero la evaluación no ha comenzado | No |
| IN_PROGRESS | La evaluación se está ejecutando o reuniendo inputs requeridos | No |
| REVIEW_REQUIRED | La ejecución requiere adjudicación/revisión antes de concluir un resultado definitivo | No |
| COMPLETE | Se cumplieron todas las obligaciones procedimentales del assessment y se emitió el outcome requerido | Sí para esa versión |

Reglas:
- COMPLETE no significa favorable, elegible, fresco, admisible ni aprobado por autoridad.
- REVIEW_REQUIRED no es un outcome de dominio ni un estado de lifecycle del registro.
- Una revisión puede reanudarse mediante REVIEW_REQUIRED → IN_PROGRESS, con evento auditado.
- No se declara COMPLETE si falta una obligación requerida, validación contractual, dependencia material o resultado obligatorio.
- La falta de evaluación se expresa por workflow; no se inventa UNKNOWN como sustituto.
- Una evaluación completada es inmutable. Correcciones materiales generan una nueva versión/assessment enlazado.

### 2.2 Transiciones permitidas

| Desde | Hacia | Condición mínima |
|---|---|---|
| NOT_STARTED | IN_PROGRESS | Inicio autorizado y evento de inicio |
| NOT_STARTED | REVIEW_REQUIRED | Bloqueo previo que exige adjudicación |
| IN_PROGRESS | REVIEW_REQUIRED | Señal, conflicto, dependencia o control pendiente |
| REVIEW_REQUIRED | IN_PROGRESS | Revisión resuelta y autorización para reanudar |
| IN_PROGRESS | COMPLETE | Contrato válido, cobertura completa, dependencias resueltas según policy y outcomes obligatorios emitidos |
| IN_PROGRESS | NOT_STARTED | Prohibido; crear nuevo intento/versión si hubo cancelación y reinicio formal |
| COMPLETE | cualquier estado de workflow | Prohibido; outcome histórico inmutable |

REVIEW_REQUIRED puede reabrirse solo mediante transición explícita y evento; no se resuelve por silencio, TTL ni paso del tiempo.

## 3. Máquina B — RecordLifecycleStatus

### 3.1 Vocabulario propuesto

| Estado | Definición | ¿Terminal? |
|---|---|---|
| ACTIVE | Versión de registro vigente como candidata a consumo actual, sujeto a scope, freshness, policy y autorización | No |
| SUPERSEDED | Otra versión/registro la reemplazó para el uso actual | Sí para esa versión |
| INVALIDATED | Evento/causa demuestra que la versión ya no debe utilizarse dentro del scope afectado | Sí para esa versión |
| WITHDRAWN | Retiro explícito por actor/autoridad autorizada conforme a política | Sí para esa versión |

ACTIVE no significa evaluación completa ni resultado favorable. Un registro puede estar ACTIVE y su workflow NOT_STARTED/IN_PROGRESS/REVIEW_REQUIRED, aunque consumidores que requieren COMPLETE deben bloquearlo.

### 3.2 Transiciones permitidas

| Desde | Hacia | Condición mínima |
|---|---|---|
| ACTIVE | SUPERSEDED | Nueva versión aprobada para reemplazarla, con successor_ref y scope de reemplazo |
| ACTIVE | INVALIDATED | Evento invalidante verificado, alcance, causa, autoridad y dependency impact registrados |
| ACTIVE | WITHDRAWN | Decisión explícita de autoridad facultada, con rationale y alcance |
| SUPERSEDED | cualquier estado | Prohibido; crear nueva versión |
| INVALIDATED | cualquier estado | Prohibido; crear nueva versión y reevaluar |
| WITHDRAWN | cualquier estado | Prohibido; crear nuevo registro si vuelve a presentarse |

No hay transición automática ACTIVE → SUPERSEDED por mera creación de borrador sucesor. El reemplazo se produce cuando la nueva versión cumple el criterio de activación definido por el tipo de objeto y política.

## 4. Relación entre las máquinas y Freshness

| Workflow | Record lifecycle | Freshness | Interpretación |
|---|---|---|---|
| NOT_STARTED | ACTIVE | Sin resultado | Registro vigente como contenedor, aún no evaluado |
| IN_PROGRESS | ACTIVE | FRESH en un input | Trabajo en curso; no hay conclusión completa |
| REVIEW_REQUIRED | ACTIVE | FRESH | Requiere revisión; freshness no resuelve el bloqueo |
| COMPLETE | ACTIVE | FRESH | Assessment completo y registro actualmente utilizable, sujeto a policy |
| COMPLETE | ACTIVE | STALE | Assessment completo históricamente, pero no vigente para el uso/cutoff |
| COMPLETE | SUPERSEDED | FRESH | Assessment reemplazado; no consumible como actual |
| COMPLETE | INVALIDATED | FRESH | Assessment invalidado por evento; freshness no revierte invalidación |
| COMPLETE | WITHDRAWN | FRESH | Retirado explícitamente; no consumible como actual |
| REVIEW_REQUIRED | INVALIDATED | STALE | Revisión detenida y registro invalidado; conservar historia |

La tabla no autoriza uso: el consumidor verifica workflow, lifecycle, freshness, scope, validity, purpose y policy.

Reglas:
1. Workflow COMPLETE no fuerza RecordLifecycle ACTIVE; ACTIVE no fuerza COMPLETE.
2. RecordLifecycle ACTIVE no implica freshness FRESH.
3. Freshness STALE no cambia por sí sola RecordLifecycle a INVALIDATED.
4. Assessment INVALIDATED no se vuelve utilizable por freshness FRESH.
5. SUPERSEDED no equivale a STALE ni INVALIDATED.
6. WITHDRAWN exige acción/autoridad; no inferirlo de fuente ausente, silencio o TTL.
7. REVIEW_REQUIRED no es estado de dominio como CONFLICTED; ambos pueden coexistir.
8. UNKNOWN, NOT_ASSESSED, UNDETERMINED, INVALID y estados de dominio no forman parte de estas máquinas; aplican R01.3.

## 5. Contrato de consumo actual

Para consumo actual de un assessment, el consumidor debe verificar, como mínimo:
- AssessmentWorkflowStatus = COMPLETE;
- RecordLifecycleStatus = ACTIVE;
- ContractValidity = VALID;
- freshness aceptable para el uso/cutoff según policy (o NOT_APPLICABLE positivo y permitido);
- scope exacto/comparable y validation envelope válida;
- PurposeAssessment y EligibilityAssessment requeridos concluidos;
- dependencias sin invalidación material pendiente;
- riesgo/veto/autoridad conforme al purpose/activity;
- policy version compatible y no revocada.

Cualquier excepción debe estar definida por contrato y autorizada. ACTIVE no es bypass de workflow ni COMPLETE bypass de freshness.

Para replay histórico se permite leer versiones SUPERSEDED/INVALIDATED/WITHDRAWN solo en modo histórico explícito, con cutoff y snapshot reproducibles; nunca reutilizarlas como estado actual.

## 6. Invalidation, supersession, withdrawal y recovery

### 6.1 Invalidation

Requiere evento trigger, target record/version, scope afectado, causalidad, evidence refs, autoridad, policy, dependency path y timestamp. Puede propagarse por grafo conforme a reglas de dependencia; no amplía automáticamente a scopes disjuntos.

Una invalidación parcial por scope debe crear un registro de impacto/scope derivado explícito o una versión de estado con partición demostrable. No se permite mutar silenciosamente un lifecycle global cuando solo una parte está afectada.

### 6.2 Supersession

Requiere successor_ref, relación de reemplazo, compatibilidad de scope y política de activación. Un registro antiguo puede seguir siendo válido para replay histórico, pero no para decisión actual.

### 6.3 Withdrawal

Debe indicar quién retiró qué objeto, qué autoridad invocó, alcance, razón, fecha efectiva y si el retiro afecta la fuente, el claim, el assessment o la política. Source withdrawal no prueba falsedad del claim ni elimina evidencia histórica.

### 6.4 Recovery

No se permite reactivar un registro terminal. Se crea nueva versión, se ejecuta workflow nuevo y se conservan relaciones derived_from, supersedes o reassesses, según la causa.

## 7. Resolución de vocabularios divergentes

| Documento/vocabulario previo | Problema | Resolución propuesta |
|---|---|---|
| Modelo canónico: EvaluationLifecycle incluye NOT_STARTED, IN_PROGRESS, COMPLETE, REVIEW_REQUIRED, SUPERSEDED, INVALIDATED | Mezcla workflow con lifecycle terminal | Mantener los cuatro primeros en AssessmentWorkflowStatus; mover SUPERSEDED/INVALIDATED a RecordLifecycleStatus |
| Lifecycle framework: lifecycle ACTIVE, SUPERSEDED, INVALIDATED, WITHDRAWN | Falta distinguir workflow | Mantener como RecordLifecycleStatus |
| D-01/D-02: AssessmentLifecycle mezcla progreso y estados terminales | Mismo eje mezclado | Sustituir en futura versión por ambos campos |
| D-10: workflow y lifecycle separados, pero workflow sin NOT_ASSESSED explícito y lifecycle incluye ACTIVE | Ausencia de assessment no es estado terminal | Alinear con esta propuesta: NOT_STARTED como workflow; ACTIVE como record lifecycle |
| R01.1/R01.3 usan EvaluationLifecycle/workflow_status | Necesitan nombre y contrato uniforme | Adoptar AssessmentWorkflowStatus; actualizar referencias solo tras aprobación |

No se propone NOT_ASSESSED dentro del workflow. NOT_ASSESSED es una conclusión/código de assessment consumidor según R01.3; el estado previo es NOT_STARTED. No son equivalentes semánticos aunque un consumer pueda registrar que el assessment requerido no está disponible.

## 8. Campos mínimos del contrato

Todo AssessmentRecord debe transportar, según versión de contrato:
- assessment_id, record_id y record_version;
- assessment_type/domain y subject_ref;
- scope_id/version/digest;
- assessment_workflow_status;
- record_lifecycle_status;
- contract_validity y validator/version;
- domain_outcome con vocabulario de dominio;
- freshness assessment/status si aplica;
- created_at, started_at, completed_at, lifecycle_effective_at;
- event refs y actor/authority;
- policy, evaluator/engine y schema versions;
- predecessor/successor refs;
- dependency refs, provenance y replay manifest.

Los timestamps no se rellenan por inferencia. Los campos condicionales tienen reglas de obligatoriedad por transición.

## 9. Escenarios de aceptación R01.4

| ID | Escenario | Resultado esperado |
|---|---|---|
| R014-01 | Assessment creado y aún no iniciado | Workflow=NOT_STARTED; lifecycle=ACTIVE si el registro está publicado/activo |
| R014-02 | Evaluación en ejecución | IN_PROGRESS; no emitir COMPLETE |
| R014-03 | Input material requiere adjudicación | REVIEW_REQUIRED; lifecycle puede seguir ACTIVE, pero consumo favorable bloqueado |
| R014-04 | Revisión resuelta y reanudación autorizada | REVIEW_REQUIRED → IN_PROGRESS con evento |
| R014-05 | Obligaciones y outcomes completos | IN_PROGRESS → COMPLETE |
| R014-06 | Assessment COMPLETE con resultado INELIGIBLE | Workflow COMPLETE; domain outcome INELIGIBLE; no inferir fallo de lifecycle |
| R014-07 | Assessment COMPLETE, lifecycle ACTIVE, freshness STALE | No consumir para usos que requieren freshness |
| R014-08 | Assessment COMPLETE, lifecycle SUPERSEDED | No consumir como actual; permitir replay histórico |
| R014-09 | Assessment COMPLETE, lifecycle INVALIDATED | No consumir actual; evento causal obligatorio |
| R014-10 | Assessment COMPLETE, lifecycle WITHDRAWN | No consumir actual; retiro autorizado y auditado |
| R014-11 | Registro ACTIVE, workflow NOT_STARTED | No usar como assessment concluido |
| R014-12 | Se crea borrador sucesor | No supersede automáticamente al registro actual |
| R014-13 | Nueva versión aprobada y activada | Anterior SUPERSEDED con successor_ref |
| R014-14 | TTL vence | Freshness STALE; lifecycle no cambia automáticamente |
| R014-15 | Evento invalidante confirmado | ACTIVE → INVALIDATED en scope demostrado |
| R014-16 | Solo una partición del scope está afectada | Impacto scope-specific; no invalidar todo el universo |
| R014-17 | Fuente retira publicación | Evento SOURCE_WITHDRAWN; no asumir claim falso ni retirar assessment sin policy |
| R014-18 | Assessment terminal recibe evento de reactivación | Rechazar transición; crear nuevo record/version |
| R014-19 | Workflow COMPLETE carece de campo obligatorio | ContractValidity=INVALID o error de completitud según contrato; no aceptar COMPLETE |
| R014-20 | Workflow REVIEW_REQUIRED y dominio CONFLICTED | Mantener ambos ejes; no colapsar |
| R014-21 | Lifecycle ACTIVE y ContractValidity INVALID | Rechazar consumo; ACTIVE no salva el contrato |
| R014-22 | Replay de versión SUPERSEDED | Permitido solo con modo histórico, cutoff y snapshot |
| R014-23 | Evento concurrente supersede e invalida misma versión | Resolver por causalidad/precedencia y adjudicación; no last-write-wins |
| R014-24 | No existe assessment para consumidor requerido | Workflow no existente/NOT_STARTED; consumer puede producir NOT_ASSESSED/INSUFFICIENT_INFORMATION según R01.3 |
| R014-25 | WITHDRAWN por actor sin autoridad | Rechazar evento de lifecycle; conservar intento en audit log |
| R014-26 | Evaluación completa pero dependencia upstream invalidada después | Emitir evento de impacto; invalidar/revisar según dependencia y scope, sin reescribir outcome histórico |

## 10. Invariantes propuestas

- R014-INV-01: AssessmentWorkflowStatus y RecordLifecycleStatus son ejes distintos.
- R014-INV-02: FreshnessStatus es tercer eje independiente.
- R014-INV-03: COMPLETE no implica outcome favorable ni lifecycle ACTIVE.
- R014-INV-04: ACTIVE no implica workflow COMPLETE.
- R014-INV-05: SUPERSEDED, INVALIDATED y WITHDRAWN son terminales para una versión.
- R014-INV-06: ningún registro terminal se reactiva; recuperación crea nueva versión.
- R014-INV-07: REVIEW_REQUIRED no es outcome de dominio ni lifecycle terminal.
- R014-INV-08: STALE no implica INVALIDATED, SUPERSEDED o WITHDRAWN.
- R014-INV-09: SUPERSEDED no implica STALE ni falsedad del contenido histórico.
- R014-INV-10: INVALIDATED requiere evento, causa, scope, actor/authority y provenance.
- R014-INV-11: WITHDRAWN requiere autoridad y decisión explícitas.
- R014-INV-12: workflow COMPLETE exige cobertura y outputs obligatorios completos.
- R014-INV-13: el consumo actual requiere workflow, lifecycle, contract validity, freshness, scope y policy compatibles.
- R014-INV-14: replay histórico es explícito y no autoriza consumo actual.
- R014-INV-15: eventos concurrentes se resuelven por causalidad/policy, nunca por último evento recibido sin adjudicación.
- R014-INV-16: invalidación y propagación respetan scope y dependency closure.
- R014-INV-17: estados de dominio, epistemológicos y de aplicabilidad no se insertan en estas máquinas.
- R014-INV-18: cada transición genera evento auditable e idempotente.

## 11. Decisiones pendientes

1. Architecture y Lifecycle owner aprueban las dos máquinas y sus nombres.
2. Confirmar si ACTIVE se asigna al crear el registro o solo al publicar/validar; esta propuesta recomienda ACTIVE solo cuando el registro está formalmente publicado y habilitado para evaluación/consumo sujeto a workflow.
3. Definir qué tipos de objeto admiten WITHDRAWN y qué autoridad puede emitirlo.
4. Definir semántica y alcance de invalidación parcial; revisar compatibilidad con Scope Algebra.
5. Aprobar transiciones y concurrency/causality policy.
6. Definir compatibilidad/migración para consumidores que hoy esperan EvaluationLifecycle con seis estados.
7. Tras aprobación, actualizar modelo canónico, Lifecycle, D-01/D-02, D-10, R01.1–R01.3, schemas, fixtures, validadores y pruebas de contrato en cambios separados y trazables.

## 12. Estado y limitaciones

**PROPOSED / PENDING_ARCHITECTURE_APPROVAL / NOT_APPROVED.**

Esta resolución propone dos máquinas canónicas y una dimensión Freshness independiente, 26 escenarios y 18 invariantes. La revisión es estática y documental. No se ejecutaron tests, pruebas de concurrencia, validación formal ni empírica. No se modificaron documentos canónicos, requisitos IIM, schemas, código, fixtures ni tests.
