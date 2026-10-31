# R01.9 — Propuesta de validación atómica y propagación de invalidación por scope

**ID:** IIE-IDENTITY-R01-09  
**Versión:** 0.1.0-draft  
**Fecha:** 2026-10-31  
**Estado:** PROPOSED / PENDING_ARCHITECTURE_APPROVAL / NOT_APPROVED  
**Dependencias:** R01.7, R01.8, X-06, X-07, X-09 Scope Algebra, FSA-01 y FSA-02..05.  
**Hallazgos abordados:** R018-01, R018-02, R018-03.

## 1. Propósito y límites

Resolver el exceso de propagación por invalidación parcial y la falta de un contrato de validación vinculado a unidades atómicas. La propuesta define:
- unidad de evaluación y persistencia atómica;
- separación entre registro agregado y registros atómicos;
- invalidación causal limitada por scope;
- ValidationEnvelope por unidad atómica;
- manifest, dependency closure y proof binding;
- agregación de resultados sin borrar outcomes locales.

No se adopta todavía ningún schema ni enum canónico. No modifica X-06, X-07, X-09, requisitos IIM, código, fixtures o tests. Requiere aprobación de Architecture, Risk, Security, Data Governance y Operations.

## 2. Dictamen y principios

**Dictamen:** ADDRESSED_IN_DRAFT / PENDING_ARCHITECTURE_APPROVAL / NOT_APPROVED.

1. Una decisión solo es válida para un scope exacto, una versión de policy y un snapshot determinado.
2. La unidad mínima persistida y consumible debe ser suficientemente atómica para que su lifecycle e invalidación no afecten regiones independientes.
3. Un agregado que representa varias unidades no sustituye los registros atómicos.
4. La invalidación de una unidad no invalida por defecto las unidades disjuntas.
5. La ausencia de una dependencia no demuestra independencia si el universo de dependencias no está cerrado.
6. UNKNOWN/UNRESOLVED no se convierte en NOT_AFFECTED, EMPTY, DISJOINT ni VALID.
7. Una unidad con ValidationEnvelope distinto de VALID no produce una decisión favorable para esa unidad.
8. La validez de una unidad no se hereda a una unidad derivada ni a un agregado distinto.
9. El resultado agregado conserva el vector completo de resultados atómicos; no promedia ni compensa estados restrictivos.
10. El historial es inmutable; cambios producen nuevos records, envelopes y decisiones con lineage.

## 3. Modelo de objetos propuesto

### 3.1 AtomicAssessmentRecord

Registro inmutable que contiene un assessment y su scope atómico exacto.

Campos conceptuales:
- atomic_record_id, record_version, record_type;
- subject_ref y canonical identity binding;
- scope_id, scope_digest, scope_schema_version;
- decision_type, purpose/activity y route_id cuando aplique;
- producer_ref, authority_ref y consumer_contract_ref;
- assessment_workflow_status y record_lifecycle_status como ejes separados;
- contract/method validity references;
- evidence, claim, policy, methodology y dependency references;
- valid_time, observed_at, recorded_at, evaluated_at;
- supersedes_ref, invalidated_by_event_ref y lineage;
- content_digest.

Invariante: un AtomicAssessmentRecord no contiene decisiones para dos átomos que requieran lifecycle o outcomes independientes. Si el scope cambia materialmente, se crea un record nuevo.

### 3.2 AggregateAssessmentRecord

Registro derivado que agrupa referencias a AtomicAssessmentRecord compatibles para un requested_scope.

Contiene:
- aggregate_id/version;
- requested_scope_ref/digest;
- ordered atom manifest;
- partition proof reference;
- atomic record references y sus versiones;
- aggregation policy/version;
- summary derivado;
- causes y limitations conservadas;
- digest y lineage.

El agregado no reescribe el lifecycle de los registros atómicos. Una nueva composición crea una nueva versión del AggregateAssessmentRecord.

### 3.3 Record lifecycle

El lifecycle es local al objeto/version:
- INVALIDATED de un AtomicAssessmentRecord afecta ese registro y los consumidores dependientes en scope probado.
- INVALIDATED de un AggregateAssessmentRecord invalida ese agregado para consumo actual, pero no invalida automáticamente sus AtomicAssessmentRecord referenciados.
- SUPERSEDED y WITHDRAWN se aplican al record/version exacto y autoridad correspondiente.
- Una nueva versión no reactiva el registro terminal anterior.
- Un agregado no puede seguir siendo consumido como actual si cambió o se invalidó una referencia material; debe recomponerse.

Este modelo evita el falso dilema entre invalidar parcialmente un record indivisible y bloquear globalmente todo el universo. La regla es persistir por separado las unidades que deban conservar independencia.

## 4. Propagación de eventos y causalidad

### 4.1 DependencyEdge requerido

Cada arista causal debe incluir:
- dependency_edge_id y version;
- source_object_ref/version;
- dependent_object_ref/version;
- relation_type y causal direction;
- source_scope_ref/digest y dependent_scope_ref/digest;
- propagation_rule_id/version;
- evidence/provenance refs;
- valid_time y recorded_at;
- completeness/closure context;
- status y adjudication cuando exista conflicto.

No inferir edges por similitud de nombre, ticker, proyecto, proveedor, bridge o correlación estadística.

### 4.2 Algoritmo de impacto

Para un evento invalidante E y cada record dependiente R:

1. Validar el contrato del evento, identidad de objetos, versión y provenance.
2. Resolver el scope causal del evento.
3. Obtener dependencias directas relevantes para R con versión/cutoff compatibles.
4. Comparar causal scope y R.scope mediante Scope Algebra.
5. Clasificar relación como EQUAL, CONTAINS, WITHIN, DISJOINT, OVERLAPS, PARTIAL_ORDER, UNRESOLVED o INVALID.
6. Aplicar la regla de propagación aprobada para relation_type, purpose/activity y scope.
7. Crear una invalidation assessment auditable para el record afectado; no modificar el historial.
8. Reevaluar solo los AtomicAssessmentRecord afectados demostrados.
9. Recomponer agregados que referencien records afectados.
10. Conservar los records disjuntos sin degradarlos, sujeto a su propia freshness, lifecycle, dependencies y policy.

### 4.3 Resultados de propagación

| Resultado | Significado | Consecuencia |
|---|---|---|
| AFFECTED | La relación causal y el scope de impacto están demostrados | Invalidar/revisar el átomo dependiente conforme a policy |
| NOT_AFFECTED_BY_THIS_EVENT | Se demuestra disjunción o ausencia de dependencia bajo universo cerrado y regla aplicable | No propagar este evento; mantener controles propios |
| PARTIALLY_AFFECTED | El scope de un objeto contiene subregiones afectadas y no afectadas, particionadas exactamente | Crear/usar records atómicos separados; reevaluar afectados |
| SCOPE_UNRESOLVED | No se puede demostrar alcance o independencia | No afirmar NOT_AFFECTED; REVIEW_REQUIRED en el átomo dependiente cuya relación no se resuelve |
| INVALID | Contrato de evento/edge/scope inválido | Rechazar el evento/edge y auditar |

SCOPE_UNRESOLVED no justifica invalidación universal. Bloquea una conclusión favorable solo para la unidad cuya dependencia material no puede resolverse. No se amplía a unidades demostrablemente disjuntas.

### 4.4 Records agregados no atómicos heredados

Un record legacy que mezcla múltiples scopes y no permite identificar de forma fiable sus partes no puede tratarse como si fuera atómico. Ante un evento que puede afectar una parte:
- el aggregate legacy se marca no consumible para conclusiones actuales dependientes hasta su partición/reconstrucción;
- no se declara que cada subregión sea inválida;
- no se declara que cada subregión sea independiente;
- la reconstrucción produce records atómicos nuevos con evidence lineage y ValidationEnvelope propios;
- las regiones no reconstruidas permanecen sin decisión actual, no elegibles por herencia.

Este tratamiento es REVIEW/ABSTAIN por falta de granularidad, no propagación factual universal.

## 5. Atomic ValidationEnvelope

### 5.1 Una envelope por unidad atómica

El ValidationEnvelope se vincula obligatoriamente a:
- atomic_record_id/version;
- atom_scope_id/digest;
- requested_scope_ref/digest;
- partition_manifest_id/digest;
- decision_type, purpose/activity, route_id si aplica;
- snapshot_id/digest;
- policy, methodology, schema y mapping versions;
- required_input_manifest;
- dependency_closure_certificate;
- contract_validation result;
- method/assessment validation result cuando exista;
- lifecycle/workflow/freshness requirements and results;
- identity binding;
- errors, blockers, warnings y reason codes;
- validator_ref/version, evaluated_at y deterministic digest;
- proof_certificate_ref y checker_ref/version cuando la operación requiera demostración formal.

Estados candidatos por átomo:
- VALID
- INCOMPLETE
- REVIEW_REQUIRED
- REJECTED_INVALID_INPUT

Estos resultados mantienen semántica compatible con X-06 §4.3. No son DecisionOutcome ni RouteEligibilityStatus.

### 5.2 Gate de composición

X-06 consume el envelope correspondiente al mismo átomo, snapshot, policy, scope y decision_type.

- VALID: habilita composición de ese átomo.
- INCOMPLETE: no compone una conclusión favorable para ese átomo; puede coexistir con otros átomos VALID.
- REVIEW_REQUIRED: adjudicación obligatoria para ese átomo.
- REJECTED_INVALID_INPUT: rechaza el input afectado y conserva error/lineage.

Un envelope de otro átomo, otra versión, otro cutoff, otro propósito o policy no es reutilizable.

### 5.3 Manifiesto de inputs obligatorios

Cada decision policy version debe producir un RequiredInputManifest:
- lista de módulos/assessments obligatorios por tipología, purpose/activity, scope y jurisdiction;
- regla de applicability por input;
- evidence/freshness thresholds;
- dependencies y dependency policy;
- reglas para inputs opcionales/alternativos;
- exclusiones justificadas;
- version, owner y effective interval.

Un input no se vuelve opcional por ausencia. NOT_APPLICABLE exige regla positiva en el manifest y evidence de aplicabilidad.

### 5.4 Dependency closure

La declaración de closure solo es válida respecto de:
- universo de dependencias declarado;
- tipos de edge incluidos;
- fuentes/registries y sus snapshots;
- cutoff temporal;
- política de cobertura;
- método de detección de edges;
- límites y exclusiones documentados.

Si no existe universo cerrado o cobertura demostrable, el resultado es UNRESOLVED; no se emite certificado de cierre ni VALID para una conclusión que dependa de exhaustividad.

### 5.5 Proof binding y checker

Cuando se exige prueba de partición, contención, disjunción o cierre:
- el certificado referencia atom_scope_digest, requested_scope_digest, universe/version, normalization_version, algorithm_version, input_manifest_digest y dependency_snapshot_digest;
- incluye property proven, assumptions, solver/checker identity, version y result;
- un checker independiente verifica el certificado sin confiar ciegamente en el producer;
- timeout, unknown, resource exhaustion o proof mismatch no se consideran proof success;
- proof de una versión/scope no se reutiliza para otro digest.

La semántica detallada debe armonizarse con X-09 documentos 20–22 y FSA-02..05.

## 6. Agregación del requested_scope

1. El requested_scope se particiona exactamente en átomos disjuntos antes de agregar.
2. Cada átomo tiene su propio AtomicAssessmentRecord, ValidationEnvelope y DecisionRecord.
3. El agregado conserva la lista ordenada de atom IDs, outcomes, causes, envelope states y proof refs.
4. Solo átomos con envelope VALID pueden tener DecisionOutcome calculado.
5. Un átomo no válido no recibe outcome favorable por herencia.
6. La agregación aplica la policy aprobada de X-06/documento 18; conserva vector y causas.
7. Si cualquier región del requested_scope carece de resultado válido, el resumen global no puede afirmar elegibilidad irrestricta para todo el requested_scope.
8. Se pueden publicar resultados positivos para subconjuntos exactos y explícitos, cada uno con su propio scope y decisión.
9. No se permite que el resumen del agregado sustituya o borre outcomes atómicos.
10. La precedencia categórica solo se aplica tras validar la partición y envelope de cada átomo; no resuelve conflictos de validez.

No se introduce aquí un nuevo enum de aggregate status. Se conservan estados por átomo y un summary derivado con cobertura, conteos y restricciones. Los owners deben decidir si se necesita un catálogo explícito para aggregate status.

## 7. Matriz de escenarios adversariales

| ID | Situación | Resultado exigido |
|---|---|---|
| R019-01 | Evento afecta átomo A; B es demostrablemente disjunto | A reevaluado; B conserva su propio estado |
| R019-02 | Evento afecta solo parte de record legacy no atómico | Aggregate legacy no consumible; no afirmar inválidos ni independientes los subsets hasta reconstrucción |
| R019-03 | Edge de dependencia desconocido para átomo A | A SCOPE_UNRESOLVED/REVIEW; no propagar a B disjunto probado |
| R019-04 | Envelope A=VALID, B=INCOMPLETE | Decisión solo para A; no elegibilidad global A∪B |
| R019-05 | Envelope de A aplicado a B | Rechazo por scope/digest mismatch |
| R019-06 | Partition proof no cubre residual | Envelope del requested_scope no VALID |
| R019-07 | Dependency closure con universo abierto | UNRESOLVED; no VALID para conclusiones dependientes de exhaustividad |
| R019-08 | Proof certificate de versión antigua | Rechazo por digest/version mismatch |
| R019-09 | Solver timeout o UNKNOWN | No proof success; REVIEW/INCOMPLETE según policy |
| R019-10 | Agregado contiene un átomo INVALIDATED | Recomponer agregado; no invalidar automáticamente átomos independientes |
| R019-11 | Evidencia stale en una dimensión de un átomo | Bloquear solo consumers/claims que dependen de esa freshness |
| R019-12 | Dos eventos concurrentes sobre el mismo átomo | Resolver por causalidad, idempotency key y autoridad; no last-write-wins |
| R019-13 | Un átomo no tiene assessment obligatorio | Envelope INCOMPLETE; no NOT_APPLICABLE por omisión |
| R019-14 | Todos los inputs declarados N/A por regla | Aplicar explicit empty-purpose policy; no admisibilidad por vacuidad |

## 8. Invariantes propuestos

- R019-INV-01: un envelope valida exactamente un atom scope y snapshot.
- R019-INV-02: un resultado VALID no se hereda entre átomos.
- R019-INV-03: invalidación se propaga solo a records dependientes dentro del scope causal demostrado.
- R019-INV-04: ausencia de DependencyEdge no demuestra independencia en universo abierto.
- R019-INV-05: SCOPE_UNRESOLVED bloquea conclusión dependiente del átomo afectado, no autoriza propagación universal.
- R019-INV-06: un aggregate no cambia lifecycle de sus records atómicos.
- R019-INV-07: aggregate legacy no atómico no se interpreta como colección de átomos independientes sin reconstrucción.
- R019-INV-08: requested_scope partition debe ser exacta y pairwise disjoint antes de agregación.
- R019-INV-09: todo residual se conserva y requiere outcome independiente.
- R019-INV-10: proof certificate está ligado a todos los inputs semánticos y versiones.
- R019-INV-11: solver UNKNOWN/timeout/resource exhaustion no equivale a proof success.
- R019-INV-12: evento concurrente no se resuelve por last-write-wins.
- R019-INV-13: aggregate summary no oculta ningún átomo no validado o bloqueado.
- R019-INV-14: ningún átomo con envelope no VALID produce decisión favorable.

## 9. Crosswalk R018-01..03

| Hallazgo | Resolución propuesta | Estado |
|---|---|---|
| R018-01 invalidación parcial demasiado amplia | AtomicAssessmentRecord + AggregateAssessmentRecord; propagación por atom/dependency scope | ADDRESSED_IN_DRAFT; X-07/X-09 approval pending |
| R018-02 envelope global | ValidationEnvelope por átomo; X-06 por unidad; aggregate conserva vector | ADDRESSED_IN_DRAFT; decision policy pending |
| R018-03 contrato/proof no definido | Envelope fields, RequiredInputManifest, DependencyClosureCertificate, ProofBinding y checker | PARTIALLY_ADDRESSED; schema/formal proof pending |

## 10. Aprobaciones y gates

- Architecture: objetos, claves, atomización, versionado y compatibilidad con X-06.
- Scope/Methods: operaciones, partición exacta, proof semantics y residual.
- Data Governance: dependency registries, provenance, temporalidad y closure.
- Risk: impacto de incertidumbre y reglas de propagación de vetos.
- Security: integridad de manifests, digest, proof checker, replay y concurrencia.
- Operations: granularidad de registros, invalidation workflow y re-composition.
- Governance: authority para invalidar, adjudicar y aprobar excepciones.

Cada decisión requiere owner, authority_ref, scope, version, effective_at, rationale, dissent y audit event.

## 11. Plan de verificación futuro

Los escenarios R019-01..14 e invariantes R019-INV-01..14 son especificaciones, no tests ejecutables. Antes de implementar:
1. traducir a fixtures y tests deterministas;
2. agregar property-based tests de partición y scope algebra;
3. validar proof certificate con checker independiente;
4. probar differential checker y solver unknown/timeout;
5. probar eventos tardíos, duplicados y concurrentes;
6. verificar replay y digest stability;
7. ejecutar CI y documentar resultados.

No se ejecutaron pruebas como parte de R01.9.

## 12. Límites y estado

Propuesta documental. No se modifican canónicos X-06/X-07/X-09, requisitos IIM, schemas, código, fixtures o tests. No se aprueba el nuevo modelo de objetos ni el ValidationEnvelope. No se declara validación formal, empírica o ISO.

**Estado final:** PROPOSED / PENDING_ARCHITECTURE_APPROVAL / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED.
