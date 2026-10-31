# R01.1 — Resolución de IdentityStatus y FreshnessStatus

**ID:** IIE-IDENTITY-R01-01  
**Versión:** 0.1.0-draft  
**Fecha:** 2026-10-31  
**Estado:** PROPOSED / PENDING_ARCHITECTURE_APPROVAL / NOT_APPROVED  
**Backlog:** R-01 — Normalización del modelo canónico de estados  
**Hallazgo:** ICA-01  
**Documentos relacionados:** D-01/D-02, modelo canónico de estados, Lifecycle/Freshness, Scope Algebra, FSA-01

## 1. Decisión propuesta

Separar de forma estricta la resolución factual de identidad de la vigencia temporal de una evaluación o evidencia.

**IdentityStatus** tendrá exclusivamente:
- RESOLVED
- UNRESOLVED
- CONFLICTED

**FreshnessStatus** conservará el vocabulario canónico ya definido por Lifecycle:
- FRESH
- APPROACHING_EXPIRY
- STALE
- NOT_APPLICABLE

No se añade UNKNOWN ni NOT_ASSESSED a FreshnessStatus. La ausencia de evaluación de freshness se representa mediante el estado del proceso de evaluación (EvaluationLifecycle) y/o el resultado de validación/completitud del assessment, no inventando un cuarto significado dentro del dominio temporal.

La propuesta requiere aprobación de Architecture, owner de Lifecycle y owner de Identity antes de modificar el modelo canónico. Este documento no modifica ese modelo.

## 2. Semántica normativa propuesta

### 2.1 IdentityStatus

| Estado | Definición | No significa |
|---|---|---|
| RESOLVED | La identidad del sujeto exacto está determinada mediante reglas y evidencia admisibles para el scope de identidad declarado | Que la evidencia esté fresca para cualquier uso; que el activo sea seguro, solvente, invertible o elegible |
| UNRESOLVED | No hay evidencia suficiente para determinar la identidad dentro del scope declarado | Que la identidad sea falsa o que exista necesariamente contradicción |
| CONFLICTED | Existen afirmaciones/evidencias materiales, admisibles y contradictorias sobre la identidad dentro del mismo scope y contexto temporal, sin adjudicación válida | Que la información esté simplemente caducada |

IdentityStatus es una conclusión sobre la identidad de un IdentitySubject tipado. Debe estar asociada a subject_ref, scope/version, temporal context, policy/predicate versions y evidence/claim lineage. No existe un IdentityStatus universal descontextualizado para todo uso.

### 2.2 FreshnessStatus

| Estado | Definición | Tratamiento |
|---|---|---|
| FRESH | Satisface la política temporal/contextual de vigencia para el uso y cutoff declarados | Puede ser consumido sujeto a los demás controles |
| APPROACHING_EXPIRY | Sigue dentro de vigencia, pero se aproxima al límite definido por la política | Señal preventiva; no equivale a STALE |
| STALE | No satisface la política de vigencia para el uso/contexto declarado | Bloquea o limita solo las conclusiones que requieren esa vigencia |
| NOT_APPLICABLE | Una regla explícita y versionada determina que la dimensión de freshness no aplica a ese objeto/uso | No puede usarse por falta de timestamps, falta de política o falta de evaluación |

Freshness es contextual: se calcula respecto de un objeto/assessment/evidence, intended use, scope, policy version y temporal cutoff. No existe un único freshness booleano global del activo.

### 2.3 Estado de evaluación y validez contractual

- EvaluationLifecycle describe el progreso/estado del expediente: NOT_STARTED, IN_PROGRESS, COMPLETE, REVIEW_REQUIRED, SUPERSEDED, INVALIDATED, conforme a la máquina canónica.
- La validación contractual determina si el objeto/inputs cumplen schema, invariantes y referencias. INVALID es un resultado de validación contractual, no un IdentityStatus ni FreshnessStatus.
- Una evaluación NOT_STARTED o IN_PROGRESS no tiene por defecto FreshnessStatus=UNKNOWN; no se ha producido una conclusión de freshness. El campo/result record de freshness puede estar ausente solo si el contrato del workflow permite esa fase. En un assessment COMPLETE que exige freshness, su ausencia es un defecto de completitud y bloquea el consumo.
- NOT_APPLICABLE requiere una decisión explícita de aplicabilidad; no representa ausencia de datos.

## 3. Reglas de independencia y composición

1. RESOLVED + FRESH: identidad determinada y evaluación vigente para el uso/cutoff declarado.
2. RESOLVED + APPROACHING_EXPIRY: identidad determinada; vigencia aún válida, con alerta preventiva.
3. RESOLVED + STALE: identidad factual permanece resuelta; la evaluación/evidencia no es suficientemente actual para los usos que exigen freshness.
4. UNRESOLVED + FRESH: la información puede estar temporalmente actual, pero no determina la identidad.
5. CONFLICTED + FRESH: evidencia contradictoria actual; la frescura no resuelve el conflicto.
6. UNRESOLVED/CONFLICTED no se convierten en STALE para ocultar su semántica.
7. STALE no cambia por sí mismo IdentityStatus a UNRESOLVED, CONFLICTED ni INVALIDATED.
8. INVALIDATED requiere un evento/causa que invalida el registro; vencimiento temporal por sí solo produce STALE, no INVALIDATED.
9. SUPERSEDED significa reemplazo por otra versión/registro; no es sinónimo de STALE.
10. Freshness se evalúa por consumidor y política. Un dato STALE para trading en tiempo real podría ser válido para reconstrucción histórica si el snapshot/cutoff y las reglas históricas lo permiten.
11. No inferir FRESH por ausencia de error, ni NOT_APPLICABLE por ausencia de obligación, ni STALE por timestamp faltante.
12. No calcular freshness sobre una identidad ambigua/conflictiva como si la fecha resolviera la ambigüedad. Puede calcularse freshness de sus evidencias individuales, pero el resultado de identidad sigue siendo UNRESOLVED/CONFLICTED.

## 4. Reglas de determinación de freshness

Un FreshnessAssessment debe identificar al menos:
- target_object_ref y version;
- intended_use, scope_id/version y temporal cutoff;
- freshness_policy_id/version;
- timestamps disponibles y su semántica (valid_time, observed_at, source_published_at, recorded_at, evaluated_at, effective_at, según aplique);
- source/time authority y timezone cuando corresponda;
- resultado y reason codes;
- dependency/evidence refs;
- evaluated_at y evaluator/engine version.

La política define qué timestamp gobierna cada tipo de objeto, tolerancias, límites, excepciones, eventos invalidantes y tratamiento de datos tardíos. No se define un TTL universal en esta resolución.

Si falta un timestamp requerido, la política no existe, la fuente temporal no es confiable o no se puede calcular el cutoff, el sistema no debe emitir FRESH ni STALE por conjetura. El workflow debe permanecer incompleto/revisión requerida o producir un error contractual cuando la ausencia viole un campo obligatorio. Esta condición se registra como freshness assessment not completed/undetermined en el expediente, no como un nuevo valor de FreshnessStatus.

## 5. Migración de IdentityStatus=STALE legado

No se permite un mapping automático global. Cada registro legacy requiere clasificación según evidencia de origen:

| Condición comprobada | Migración propuesta |
|---|---|
| El significado documentado era expiración temporal de evaluación/evidencia | Preservar IdentityStatus factual previo si se conoce; derivar FreshnessStatus=STALE con política y scope identificados |
| Existe evidencia suficiente de identidad y no hay contradicción, pero no hay freshness assessment | IdentityStatus=RESOLVED solo si la evidencia satisface la regla de resolución; freshness sin resultado hasta evaluar |
| El significado era falta de evidencia de identidad | IdentityStatus=UNRESOLVED |
| El significado era contradicción material de identidad | IdentityStatus=CONFLICTED |
| El significado no puede reconstruirse | Mantener legacy_status=STALE, marcar MIGRATION_REVIEW_REQUIRED y bloquear consumo que dependa de clasificación inequívoca |

La migración conserva source document/version, original value, record ID/version, migration rule/version, evidence, reviewer, timestamp y decision rationale. Nunca reescribe el historial original. No derivar un estado factual a partir del mero nombre legacy.

## 6. Tabla de combinaciones y consumo

| IdentityStatus | FreshnessStatus | Interpretación | Consumo |
|---|---|---|---|
| RESOLVED | FRESH | Identidad resuelta y vigente | Permitido sujeto a purpose, applicability, risk y governance |
| RESOLVED | APPROACHING_EXPIRY | Identidad resuelta, cerca del límite | Permitido si policy lo admite; alerta/revalidación |
| RESOLVED | STALE | Identidad resuelta, evidencia/assessment temporalmente vencida | Bloquear usos que exigen vigencia; permitir replay histórico explícito si corresponde |
| UNRESOLVED | FRESH | Evidencia actual pero identidad no determinada | No consumir como identidad resuelta |
| UNRESOLVED | STALE | Identidad no determinada y evidencia fuera de vigencia | No consumir |
| CONFLICTED | FRESH | Conflicto material actual | Review/quarantine según policy; no seleccionar identidad |
| CONFLICTED | STALE | Conflicto material y evidencia temporalmente vencida | No ocultar conflicto; revisar/reconstruir |
| Cualquier identidad | NOT_APPLICABLE | Freshness declarada no aplicable por regla explícita | Consumir solo si el purpose contract permite esa inaplicabilidad |

La tabla no es una autorización general: cada consumidor aplica sus dimensiones obligatorias y políticas específicas.

## 7. Escenarios de aceptación R01.1

| ID | Escenario | Resultado esperado |
|---|---|---|
| R011-01 | Identidad resuelta; assessment excede TTL para trading | RESOLVED + STALE; bloquear solo consumo que exige vigencia |
| R011-02 | Identidad resuelta; snapshot histórico cumple cutoff | RESOLVED + FRESH para el perfil histórico aplicable |
| R011-03 | Identidad desconocida, datos recientes | UNRESOLVED + FRESH; no inferir identidad |
| R011-04 | Dos claims de identidad admisibles se contradicen | CONFLICTED; freshness se evalúa aparte |
| R011-05 | Contrato no resuelto por falta de evidencia | UNRESOLVED; no usar STALE como sustituto |
| R011-06 | Timestamp requerido ausente | No emitir FRESH/STALE; assessment incompleto o error contractual según contrato |
| R011-07 | Política de freshness inexistente | No emitir FRESH/STALE; REVIEW_REQUIRED/incompleto |
| R011-08 | Política declara explícitamente que freshness no aplica | NOT_APPLICABLE con regla/justificación versionadas |
| R011-09 | Vence TTL sin evento invalidante | STALE; no INVALIDATED |
| R011-10 | Error material demuestra que identidad anterior era incorrecta | INVALIDATED al assessment previo y nueva resolución; identidad se reevalúa |
| R011-11 | Assessment reemplazado por nueva versión | Versión anterior SUPERSEDED, no STALE por definición |
| R011-12 | Legacy IdentityStatus=STALE sin provenance suficiente | MIGRATION_REVIEW_REQUIRED; no mapping automático |
| R011-13 | Legacy STALE documentado como expiración de TTL | Derivar freshness STALE conservando identidad factual solo si puede demostrarse |
| R011-14 | Evaluador no ejecutó freshness assessment | No fabricar UNKNOWN como FreshnessStatus; lifecycle/completitud refleja no evaluado |
| R011-15 | Consumer acepta APPROACHING_EXPIRY por policy | Permitir con alerta/limitación explícita |
| R011-16 | Consumer exige FRESH y recibe NOT_APPLICABLE | Rechazar salvo excepción explícita del contrato consumidor |

## 8. Invariantes propuestas

- R011-INV-01: IdentityStatus ∈ {RESOLVED, UNRESOLVED, CONFLICTED}.
- R011-INV-02: FreshnessStatus ∈ {FRESH, APPROACHING_EXPIRY, STALE, NOT_APPLICABLE}.
- R011-INV-03: IdentityStatus y FreshnessStatus no se derivan uno del otro.
- R011-INV-04: ausencia de freshness assessment nunca implica FRESH, STALE ni NOT_APPLICABLE.
- R011-INV-05: STALE no invalida por sí solo la verdad factual de identidad.
- R011-INV-06: NOT_APPLICABLE requiere regla positiva, scope y justificación.
- R011-INV-07: INVALID contractual no pertenece a IdentityStatus ni FreshnessStatus.
- R011-INV-08: SUPERSEDED/INVALIDATED pertenecen al lifecycle del registro, no a IdentityStatus.
- R011-INV-09: todo resultado freshness referencia policy version, target, use, scope y cutoff.
- R011-INV-10: la migración legacy conserva el valor original y no inventa semántica.

## 9. Decisiones aún requeridas

1. Architecture aprueba la separación y los dos vocabularios.
2. Identity owner aprueba el alcance exacto de IdentitySubject y el criterio de CONFLICTED.
3. Lifecycle owner aprueba el FreshnessAssessment contract y el tratamiento de evaluación incompleta.
4. Data Governance aprueba autoridad temporal y procedencia de timestamps.
5. Owners de consumidores aprueban freshness requirements por purpose/activity.
6. Se aprueba política de migración y compatibilidad legacy.
7. Tras aprobación, se actualiza el modelo canónico y se construye inventario de referencias legacy; después schemas/fixtures/tests.

## 10. Estado

**PROPOSED / PENDING_ARCHITECTURE_APPROVAL / NOT_APPROVED.**

R01.1 queda especificada documentalmente con 16 escenarios y 10 invariantes. No se modificó el modelo canónico, Lifecycle, requisitos IIM, schemas, código, fixtures ni tests. No se ejecutaron pruebas automatizadas, validación formal ni empírica.
