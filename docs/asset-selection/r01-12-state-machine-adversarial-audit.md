# R01.12 — Auditoría adversarial de máquinas de estados R01.11

**ID:** IIE-IDENTITY-R01-12  
**Versión:** 0.1.0-draft  
**Fecha:** 2026-10-31  
**Estado:** REVIEW_REQUIRED / INTEGRATION_REVISE / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED  
**Objeto auditado:** R01.11 — Máquinas de estados y transiciones (IIE-IDENTITY-R01-11).  
**Fuentes de contraste:** modelo 03; X-04/documento 09; X-06/documento 11; X-07/documento 12; X-08/documento 13; X-09/documento 14; R01.1–R01.10.

---

## 1. Dictamen ejecutivo

R01.11 mejora sustancialmente la separación conceptual entre workflow, lifecycle, freshness, validez contractual y outcomes de dominio. También introduce una salvaguarda necesaria para el levantamiento de vetos: VETO_ACTIVE no debe pasar directamente a NO_VETO_IDENTIFIED.

Sin embargo, la revisión adversarial detecta vacíos que impiden aprobar la propuesta como catálogo ejecutable de máquinas de estados. En particular, algunos dominios tienen outcomes sin una máquina completa, varios nombres de estado se repiten entre ejes, la semántica de revisión de veto puede interpretarse como una reducción de la restricción, y las reglas de concurrencia no especifican un protocolo completo de commit/adjudicación.

**Dictamen:** REVIEW_REQUIRED / INTEGRATION_REVISE / NOT_APPROVED.

### 1.1 Resumen de hallazgos

| ID | Severidad | Hallazgo |
|---|---|---|
| R012-01 | HIGH | Route REVIEW_REQUIRED duplicado entre outcome y workflow sin discriminador contractual |
| R012-02 | HIGH | RiskDomainOutcome no tiene catálogo cerrado ni máquina de transiciones |
| R012-03 | HIGH | VETO_ACTIVE → VETO_REVIEW_REQUIRED puede interpretarse como reducción de enforcement |
| R012-04 | HIGH | Terminalidad por versión e invalidación parcial por scope no tienen un modelo de transición integrado |
| R012-05 | HIGH | ValidationEnvelope mezcla REVIEW_REQUIRED con estados procedimentales sin semántica única |
| R012-06 | HIGH | EvidenceSufficiency carece de catálogo y máquina; el flujo de evidencia queda incompleto |
| R012-07 | HIGH | Claim RETRACTED no distingue claramente retirada de afirmación, retiro de fuente y lifecycle del record |
| R012-08 | HIGH | La transición de DECISION_REVIEW_REQUIRED a outcomes finales no fija identidad/version del nuevo DecisionRecord |
| R012-09 | MEDIUM | No se define la transición de veto cuando falla la remediación ni la reapertura tras adjudicación |
| R012-10 | MEDIUM | FreshnessStatus aparece como eje, pero no tiene transiciones ni evento/guard formalizado |
| R012-11 | MEDIUM | El protocolo temporal/concurrente carece de regla de commit atómico y tratamiento de split-brain |
| R012-12 | MEDIUM | Producer/authority y SoD siguen siendo roles nominales no operables |
| R012-13 | MEDIUM | Crosswalk de estados legacy no es exhaustivo por producer, consumer, purpose y scope |
| R012-14 | MEDIUM | TransitionEvent e idempotencia no definen unicidad, orden ni deduplicación en un ámbito contractual preciso |

Los hallazgos son defectos de especificación/propuesta, no evidencia de defectos en un motor ejecutable: no existe implementación R01.11 que se haya probado.

## 2. Método y límites de auditoría

Se realizó revisión documental estática de R01.11 contra las fuentes listadas. Se verificaron vocabularios, ejes, transiciones, guards, autoridad, scope, temporalidad, concurrencia, replay y crosswalks.

No se ejecutaron:
- pruebas unitarias, contractuales, integración o property-based;
- pruebas de concurrencia, orden de eventos o recuperación;
- validación formal/SMT/BDD;
- replay contra event logs reales;
- revisión independiente institucional de Risk, Architecture, Operations, Legal/Compliance o Security.

Por tanto, los hallazgos expresan brechas de diseño documental. No se afirma que haya comportamiento de software defectuoso ni que una transición esté implementada.

## 3. Hallazgos detallados

### R012-01 — HIGH: Route REVIEW_REQUIRED duplicado sin discriminador contractual

**Evidencia:** R01.11 §8 distingue RouteEligibilityOutcome=ROUTE_REVIEW_REQUIRED y RouteAssessmentWorkflow=REVIEW_REQUIRED, pero reconoce que el contrato debe resolver su coexistencia.

**Riesgo:** un consumer puede leer el mismo token en el campo equivocado, sobrescribir el outcome con workflow o interpretar que cerrar el workflow elimina la revisión de ruta. El problema se extiende a validadores, logs, métricas y migraciones.

**Resolución requerida:**
1. Usar campos y tipos distintos, no un campo status genérico.
2. Definir nombres inequívocos, por ejemplo route_outcome y assessment_workflow_status.
3. Establecer combinaciones válidas e inválidas entre ambos.
4. Prohibir que una transición de workflow modifique route_outcome sin una recomposición explícita.
5. Añadir schema validation y fixtures de colisión.

**Criterio de cierre:** matriz exhaustiva de combinaciones y transiciones entre ambos ejes, aprobada por Route Eligibility Owner.

### R012-02 — HIGH: RiskDomainOutcome carece de catálogo cerrado y máquina

**Evidencia:** R01.11 §7.1 remite a R01.10 y deja la lista exacta pendiente del Risk Domain Owner. Sin embargo, la sección de máquinas presenta transiciones de veto como si sus inputs de riesgo estuvieran suficientemente especificados.

**Riesgo:** no se puede definir una transición determinista desde RiskDomainAssessment, probar la cobertura ni asegurar que un producer no emita un estado fuera de catálogo. Los errores de método, ejecución, evidencia insuficiente y outcomes factuales podrían seguir mezclándose.

**Resolución requerida:**
1. Cerrar el catálogo RiskDomainOutcome y separar Contract/Execution/Method errors.
2. Definir la máquina de workflow del RiskAssessment y su relación con el outcome.
3. Definir transiciones de findings, no-material-finding, unknown, conflict y stale.
4. Definir reglas para nueva evidencia, corrección y reevaluación.
5. Aprobar el mapping RiskDomainOutcome → VetoRuleEvaluation.

**Criterio de cierre:** catálogo versionado, transition table y matriz producer/consumer aprobados por Risk y Architecture.

### R012-03 — HIGH: VETO_ACTIVE → VETO_REVIEW_REQUIRED puede debilitar la restricción

**Evidencia:** R01.11 §7.3 permite pasar de VETO_ACTIVE a VETO_REVIEW_REQUIRED al presentar remediación. §7.4 dice que la restricción no se libera, pero no define un control de enforcement independiente del status.

**Riesgo:** un consumer que interprete VETO_REVIEW_REQUIRED como estado menos restrictivo puede permitir actividad mientras se valida la remediación. La semántica de transición podría convertirse en un bypass operacional.

**Resolución requerida:**
1. Separar VetoLifecycle/ReviewWorkflow de VetoEffect/EnforcementDisposition.
2. Mientras la remediación no esté aprobada, conservar explícitamente el efecto BLOCK_ACTIVE.
3. Definir que VETO_REVIEW_REQUIRED tras VETO_ACTIVE es una fase de revisión con bloqueo heredado, no una liberación.
4. Solo la autoridad de liberación puede cambiar el efecto de bloqueo después de evidencia independiente.
5. Prohibir cualquier consumer mapping de VETO_REVIEW_REQUIRED a NO_VETO_IDENTIFIED o permiso de operar.

**Criterio de cierre:** contrato con dos ejes independientes y tests que prueben que ninguna transición de revisión reduce el bloqueo.

### R012-04 — HIGH: Terminalidad por versión e invalidación parcial no integradas

**Evidencia:** R01.11 §5 declara INVALIDATED terminal para una versión; §12 permite invalidación por átomo y scope. R01.9 distingue AtomicAssessmentRecord y AggregateAssessmentRecord, pero R01.11 no formaliza cómo se representa una versión parcialmente afectada cuando el registro legado era agregado.

**Riesgo:** puede terminarse invalidando todo el record padre o, en el extremo opuesto, seguir consumiendo subconjuntos no demostrablemente independientes. También falta una regla clara para cuando la partición cambia después de una invalidación.

**Resolución requerida:**
1. Definir lifecycle de AtomicAssessmentRecord separado del lifecycle del AggregateAssessmentRecord.
2. Especificar que un aggregate es una proyección/vector y no muta lifecycle de átomos.
3. Definir cómo un aggregate parcial se marca no consumible sin declarar inválidos los átomos no reconstruidos.
4. Definir split/merge de átomos y conservación de lineage.
5. Vincular toda invalidación con partition manifest y dependency closure proof.

**Criterio de cierre:** máquina lifecycle de record atómico y reglas de proyección aggregate, consistentes con R01.9, X-07 y Scope Algebra.

### R012-05 — HIGH: ValidationEnvelope mezcla revisión procedimental y validación de contrato

**Evidencia:** R01.11 §3 y §8 incluyen REVIEW_REQUIRED dentro de ContractValidity/ValidationEnvelope, a la vez que REVIEW_REQUIRED existe en AssessmentWorkflowStatus y RouteEligibilityOutcome.

**Riesgo:** una revisión humana puede confundirse con un error de contrato, o un input inválido puede tratarse como una revisión que puede reanudarse. Esto rompe la precedencia de X-06 y la separación propuesta por R01.3/R01.10.

**Resolución requerida:**
1. Separar ContractValidity (VALID/INVALID u outcomes de validación tipados) de CompletenessStatus y ReviewDisposition.
2. Mantener workflow como eje de proceso, no como estado del envelope contractual.
3. Definir envelope como objeto compuesto con campos independientes, no un enum plano.
4. Especificar cuál gate detiene el parseo, cuál bloquea composición y cuál requiere adjudicación.

**Criterio de cierre:** ValidationEnvelope schema con campos tipados y matriz de precedencia aprobada.

### R012-06 — HIGH: EvidenceSufficiency sin catálogo ni máquina

**Evidencia:** R01.11 §6 separa EvidenceAdmissibility de EvidenceSufficiency, pero no define outcomes de suficiencia ni transiciones.

**Riesgo:** la afirmación “admisible no significa suficiente” queda correcta pero no ejecutable. El consumer no puede determinar si un claim tiene evidencia suficiente, insuficiente o contradictoria ni qué evento cambia ese resultado.

**Resolución requerida:**
1. Definir EvidenceSufficiencyStatus con owner y unidad (claim/use/scope).
2. Definir cómo se calcula suficiencia sin confundir cantidad con calidad.
3. Definir transiciones ante nueva evidencia, retractación, conflicto y cambio de PredicateContract.
4. Definir la relación entre suficiencia y ClaimResolution.
5. Mantener UNKNOWN separado de NOT_ASSESSED y de error contractual.

**Criterio de cierre:** catálogo, state machine y escenarios para suficiencia por claim/use.

### R012-07 — HIGH: Claim RETRACTED mezcla objetos y autoridades

**Evidencia:** R01.11 §6 permite cualquier resolución activa → RETRACTED, pero no especifica si se retira la afirmación, el registro de claim, la evidencia fuente o la conclusión evaluada. X-07 separa SOURCE_WITHDRAWN, record WITHDRAWN y claim/evidence events.

**Riesgo:** source withdrawal podría confundirse con retractación del claim; un actor productor podría retirar una conclusión ya consumida; consumers podrían borrar soporte histórico o inferir falsedad.

**Resolución requerida:**
1. Definir qué objeto puede ser RETRACTED y quién tiene autoridad.
2. Separar ClaimAssertionStatus, ClaimResolution y RecordLifecycle.
3. Definir el evento de retractación y su efecto downstream.
4. Prohibir que SOURCE_WITHDRAWN implique ClaimResolution=REFUTED o RETRACTED automáticamente.
5. Conservar el resultado anterior para replay.

**Criterio de cierre:** objeto, autoridad, evento y crosswalk temporal aprobados.

### R012-08 — HIGH: Nueva versión de DecisionRecord no está definida en transición de revisión

**Evidencia:** R01.11 §9 permite DECISION_REVIEW_REQUIRED → DECISION_IN_PROGRESS o outcome final mediante nueva composición, y §9.2 prohíbe sobrescribir outcomes finales, pero no define de forma completa el versionado del DecisionRecord ni el vínculo con la decisión revisada.

**Riesgo:** el sistema podría modificar la misma decisión, duplicar decision_id o perder el lineage entre revisión y resultado posterior. También podría consumir el outcome anterior mientras la nueva composición está en progreso.

**Resolución requerida:**
1. Definir decision_id estable y decision_version inmutable.
2. Definir supersedes/reassesses y review_case_id.
3. Establecer que una decisión en revisión no es consumible como favorable actual.
4. Definir activación atómica de la nueva versión y lifecycle de la anterior.
5. Mantener replay histórico de la decisión anterior.

**Criterio de cierre:** transición/versioning contract para DecisionRecord, alineado con X-06 y R01.4.

### R012-09 — MEDIUM: Falta transición explícita ante remediación fallida de veto

**Evidencia:** R01.11 §7.4 afirma que el veto permanece activo o vuelve a activo si falla la revisión, pero la tabla §7.3 no especifica la transición VETO_REVIEW_REQUIRED → VETO_ACTIVE ni sus guards.

**Riesgo:** el cierre fallido podría quedar indefinido o un workflow de revisión podría terminar sin restaurar el bloqueo de forma verificable.

**Resolución requerida:** añadir transición explícita VETO_REVIEW_REQUIRED → VETO_ACTIVE por remediación rechazada, evidencia insuficiente, regresión, incumplimiento de guard o vencimiento de excepción; conservar causalidad y autoridad.

**Criterio de cierre:** transición y escenarios positivos/negativos definidos; bloqueo efectivo sin intervalo permisivo.

### R012-10 — MEDIUM: FreshnessStatus no tiene máquina ni triggers

**Evidencia:** R01.11 utiliza FRESH, APPROACHING_EXPIRY, STALE y NOT_APPLICABLE, pero no define transiciones, reloj, triggers ni condiciones de recuperación.

**Riesgo:** distintos producers pueden cambiar freshness de manera inconsistente, o un registro STALE puede volver a FRESH por actualización de timestamp sin reevaluación de fuente/uso.

**Resolución requerida:**
1. Definir transiciones por freshness policy versionada.
2. Separar refresh de fuente y reevaluación de freshness.
3. Definir FRESH → APPROACHING_EXPIRY → STALE y recuperación solo con nueva observación/verificación.
4. NOT_APPLICABLE exige regla positiva.
5. Un cambio de freshness no altera lifecycle ni verdad factual.

**Criterio de cierre:** máquina FreshnessAssessment y escenarios por policy/uso/cutoff.

### R012-11 — MEDIUM: Protocolo concurrente sin commit atómico ni split-brain

**Evidencia:** R01.11 §11 propone secuencia fuente, causalidad, aggregate version y adjudicación, pero no define el mecanismo de commit ni el comportamiento ante dos processors que aceptan transiciones desde la misma versión.

**Riesgo:** dos workers pueden aceptar transiciones incompatibles, publicar outcomes distintos o aplicar side effects duplicados aunque el documento prohíba last-write-wins.

**Resolución requerida:**
1. Definir optimistic concurrency/CAS sobre aggregate_version.
2. Definir transaction boundary entre event append, state projection y outbox/side effects.
3. Definir deduplication store y retry semantics.
4. Definir fencing/lease o mecanismo equivalente si existe ownership temporal de partición.
5. Ante commit conflict, re-read, re-evaluate guards y adjudicar; nunca sobrescribir.
6. Probar crash entre append y projection, y entre projection y side effect.

**Criterio de cierre:** protocolo de event processor y pruebas de fallos/concurrencia.

### R012-12 — MEDIUM: Producer/authority y SoD no son operables

**Evidencia:** R01.11 §14–15 hereda roles de X-08, que sigue sin asignaciones reales, delegaciones, quorum ni controles RBAC/ABAC.

**Riesgo:** una tabla de roles no demuestra independencia. El mismo actor puede producir, revisar y liberar un veto en la práctica.

**Resolución requerida:** no promover la propuesta a APPROVED/IMPLEMENTED hasta asignar titulares, suplentes, delegaciones, conflicto de interés, quorum y controles de acceso verificables; definir tratamiento de organizaciones pequeñas sin independencia suficiente.

**Criterio de cierre:** X-08 aprobado, matriz de incompatibilidades por identidad humana/servicio y evidencia de control.

### R012-13 — MEDIUM: Legacy crosswalk no es exhaustivo

**Evidencia:** R01.11 §6 y §8 presentan tratamientos conservadores, pero no un inventario completo de productores/consumidores, scope, finalidad, policy version y metadata requerida para cada valor legacy.

**Riesgo:** valores con igual nombre pueden proceder de objetos distintos; migración masiva puede cambiar semántica o aumentar favorabilidad.

**Resolución requerida:** crosswalk por source catalog/version, source object, consumer, purpose/activity, scope, mapping rule, evidence required, prohibited interpretation, fallback UNMAPPED y migration test.

**Criterio de cierre:** matriz exhaustiva con cobertura 100% del inventario real o declaración explícita de valores no migrables.

### R012-14 — MEDIUM: TransitionEvent/idempotencia sin ámbito contractual preciso

**Evidencia:** R01.11 §10–11 enumera event_id, idempotency_key, aggregate_version, source_sequence y digest, pero no fija sus namespaces, retención, deduplication window ni regla de reutilización entre producers.

**Riesgo:** colisiones entre productores, deduplicación excesiva o insuficiente, replays no deterministas y side effects duplicados.

**Resolución requerida:** definir uniqueness scope para event_id/idempotency_key, canonical payload digest, retención, producer namespace, source sequence reset/epoch, replay semantics y detección de event_id reutilizado.

**Criterio de cierre:** contrato de evento versionado y pruebas de duplicación, colisión, retry y replay.

## 4. Matriz de cobertura y prioridad

| Prioridad | Hallazgos | Trabajo requerido |
|---|---|---|
| P0 — antes de aprobar máquinas | R012-01 a R012-08 | Resolver catálogos/ejes, efecto de veto, atomicidad/versionado y semántica de evidencia/claim |
| P1 — antes de implementar | R012-09 a R012-11, R012-14 | Completar transiciones, freshness, event processor y protocolo concurrente |
| P2 — antes de producción | R012-12 y R012-13 | Autoridad real, SoD, inventario y migración legacy |

No debe comenzar la implementación integral de state machines hasta resolver P0 y aprobar los contratos de dominio. Se pueden diseñar fixtures no normativos, pero no fijar enums canónicos por inferencia.

## 5. Crosswalk R01.11 → R01.12

| Área R01.11 | Resultado adversarial |
|---|---|
| AssessmentWorkflow / RecordLifecycle | Conceptualmente consistente; requiere integración con atomic/aggregate records |
| EvidenceAdmissibility / Sufficiency | Separación correcta; incompleta por ausencia de catálogo de suficiencia |
| ClaimResolution | Requiere separar retractación de claim, retiro de fuente y lifecycle |
| Risk / Veto | Requiere catálogo de RiskDomainOutcome y preservar enforcement durante review |
| RouteEligibility | X-04 confirmado; REVIEW_REQUIRED dual requiere schema discriminado |
| SelectionDecision | Requiere versionado explícito del DecisionRecord y review case |
| Freshness | Eje correcto; máquina/triggers faltantes |
| Concurrency / replay | Principios correctos; protocolo transaccional y de deduplicación incompleto |
| Authority / SoD | Roles correctos como propuesta; no operables sin asignación institucional |

## 6. Recomendación

No aprobar R01.11 todavía. Crear R01.13 para resolver, en orden:

1. Contrato de envelope con ejes tipados y Route REVIEW_REQUIRED inequívoco.
2. Modelo de enforcement del veto durante revisión y transición fallida.
3. Catálogos/máquinas de RiskDomainOutcome y EvidenceSufficiency.
4. Claim assertion/retraction y versionado de DecisionRecord.
5. FreshnessAssessment state machine.
6. Atomic/Aggregate lifecycle y protocolo transaccional de eventos.
7. Matriz completa de authority/SoD y legacy crosswalk.

La implementación de código y schemas permanece bloqueada hasta que los owners aprueben semántica, contratos y autoridad.

## 7. Estado y límites

**REVIEW_REQUIRED / INTEGRATION_REVISE / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED.**

Auditoría estática documental. Se identifican 8 hallazgos HIGH y 6 MEDIUM. No se ejecutaron tests, análisis formal, pruebas de concurrencia ni validación empírica. No se modificaron documentos canónicos, requisitos IIM, schemas, código, fixtures o tests. Este informe no constituye certificación ISO ni autorización operativa.
