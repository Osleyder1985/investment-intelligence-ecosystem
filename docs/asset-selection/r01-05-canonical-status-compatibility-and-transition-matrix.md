# R01.5 — Matriz canónica de compatibilidad y transición de estados

**ID:** IIE-IDENTITY-R01-05  
**Versión:** 0.1.0-draft  
**Fecha:** 2026-10-31  
**Estado:** PROPOSED / PENDING_ARCHITECTURE_APPROVAL / NOT_APPROVED  
**Dependencias:** R01.1, R01.2, R01.3, R01.4; modelo canónico de estados; Lifecycle/Freshness/Invalidation; Scope Algebra; D-09/D-10  
**Propósito:** cerrar las interfaces entre vocabularios, productores, consumidores, transiciones y propagación antes de modificar contratos ejecutables.

## 1. Dictamen

La revisión cruzada confirma que la normalización no se resuelve con un catálogo plano de strings. Se requiere un contrato de composición que mantenga separados:
- identidad;
- applicability;
- epistemic/claim resolution;
- evidence admissibility;
- purpose admissibility;
- eligibility;
- assessment workflow;
- record lifecycle;
- freshness;
- contract validity;
- scope/dependency validity;
- risk/veto;
- selection decision y operational route.

**No existe un estado global universal del activo.** Cada assessment tiene sujeto, scope, purpose/activity, cutoff, policy version y autoridad productora. Los agregados son decisiones derivadas de un consumer explícito, nunca una compresión destructiva de los estados fuente.

Resultado: matriz propuesta, pendiente de aprobación. No constituye una modificación del modelo canónico ni una especificación de ejecución aprobada.

## 2. Reglas de composición obligatorias

1. Validar primero el contrato y la estructura; un input INVALID no entra en composición factual.
2. Validar identidad del sujeto y correspondencia del scope antes de consumir claims.
3. Determinar applicability por obligación; NOT_APPLICABLE requiere regla positiva versionada y no equivale a ausencia de evidencia.
4. Resolver claims y evidencia preservando UNKNOWN, CONFLICTED, RETRACTED, freshness y provenance.
5. Evaluar PurposeAdmissibility separado de EligibilityStatus.
6. Evaluar lifecycle y workflow como ejes independientes.
7. Comprobar freshness por consumer, purpose, scope y cutoff.
8. Comprobar cierre de dependencias y partición exacta del scope.
9. Aplicar criterios de elegibilidad y risk/veto no compensables.
10. Componer decisión únicamente mediante policy versionada y determinista; ante input material irresuelto, abstenerse o revisar según contrato.
11. No ampliar un veto, invalidación o restricción fuera del scope demostrado.
12. Registrar todas las fuentes, versiones, reglas, transiciones y razones para replay.

## 3. Matriz de dominios, productores y consumidores

| Eje / assessment | Productor autorizado propuesto | Vocabulario propuesto | Consumidores principales |
|---|---|---|---|
| ContractValidity | Schema/contract validator | VALID, INVALID | Todos los módulos |
| IdentityStatus | Identity Resolution | RESOLVED, UNRESOLVED, CONFLICTED | Evidence, Purpose, Eligibility, Selection, route |
| FreshnessStatus | Freshness evaluator por policy | FRESH, APPROACHING_EXPIRY, STALE, NOT_APPLICABLE | Consumer que exige temporalidad |
| ApplicabilityAssessment | Rule/applicability evaluator | APPLICABLE, NOT_APPLICABLE, UNDETERMINED, INVALID | Purpose, Eligibility, otros assessments |
| ClaimResolution | Claim resolver | CONFIRMED, REFUTED, UNKNOWN, CONFLICTED, NOT_ASSESSED, RETRACTED | Purpose, Eligibility, Risk, Selection |
| Evidence assessment | Evidence assessor | ADMISSIBLE, ADMISSIBLE_WITH_LIMITATIONS, QUARANTINED, REJECTED, UNKNOWN, NOT_ASSESSED | Claim resolver y assessments consumidores |
| PurposeAssessment | Purpose admissibility evaluator | ADMISSIBLE, ADMISSIBLE_WITH_LIMITATIONS, INSUFFICIENT_EVIDENCE, CONFLICTED, NOT_ASSESSED, NOT_ADMISSIBLE (propuesto) | Eligibility y decisiones que requieran ese purpose |
| EligibilityAssessment | Eligibility evaluator por purpose/scope | ELIGIBLE, ELIGIBLE_WITH_LIMITATIONS, INELIGIBLE, INSUFFICIENT_INFORMATION, QUARANTINED | SelectionDecision |
| Assessment workflow | Orquestador del assessment | NOT_STARTED, IN_PROGRESS, REVIEW_REQUIRED, COMPLETE | Consumer de ese assessment |
| Record lifecycle | Lifecycle authority/event processor | ACTIVE, SUPERSEDED, INVALIDATED, WITHDRAWN | Todos los consumers de registros |
| Scope validation | Scope validator | VALID, REVIEW_REQUIRED, INCOMPLETE, REJECTED_INVALID_INPUT | Todos los assessments con scope |
| Dependency completeness | Dependency evaluator | COMPLETE, INCOMPLETE, UNRESOLVED, INVALID | Assessments downstream |
| Risk domain | Risk assessor por dominio | Catálogo canónico por dominio; UNKNOWN/NOT_ASSESSED/STALE/CONFLICTED según contrato | Veto y Selection |
| VetoDecision | Risk/veto authority separada | VETO_ACTIVE, VETO_REVIEW_REQUIRED, NO_VETO_IDENTIFIED, VETO_NOT_ASSESSED (catálogo sujeto a modelo canónico) | Selection y route según purpose/activity |
| SelectionDecision | Decision Engine | Catálogo canónico de decisión; no inferirlo desde score | Candidate Registry/portfolio interface |
| RouteEligibility | Operational route evaluator | Catálogo canónico de ruta | Portfolio & Execution, solo para rutas evaluadas |

Un producer no puede emitir el estado de otro dominio por conveniencia. Un consumer no puede reinterpretar el vocabulario fuente sin mapping explícito, versionado y trazable.

## 4. Matriz de compatibilidad por eje

### 4.1 Identidad × freshness × workflow × lifecycle

| Condición | Compatibilidad | Regla de consumo |
|---|---|---|
| Identity=RESOLVED + Freshness=FRESH + Workflow=COMPLETE + Lifecycle=ACTIVE | Compatible | Puede continuar a consumers posteriores, no es aprobación de inversión |
| RESOLVED + STALE + COMPLETE + ACTIVE | Compatible | Bloquear solo conclusiones que exigen freshness para ese uso/cutoff |
| RESOLVED + APPROACHING_EXPIRY + COMPLETE + ACTIVE | Compatible | Continuar con advertencia/monitorización según policy |
| RESOLVED + NOT_APPLICABLE + COMPLETE + ACTIVE | Condicional | Aceptar solo con regla positiva de no aplicabilidad |
| UNRESOLVED + FRESH | Compatible, no favorable | Bloquear conclusiones dependientes de identidad |
| CONFLICTED + FRESH | Compatible, no favorable | REVIEW_REQUIRED/QUARANTINED en scope material; no resolver por freshness |
| RESOLVED + FRESH + NOT_STARTED | Compatible | No consumir como assessment concluido |
| RESOLVED + FRESH + REVIEW_REQUIRED | Compatible | Bloquear conclusión que depende del assessment en revisión |
| RESOLVED + FRESH + COMPLETE + SUPERSEDED | Compatible históricamente | Rechazar consumo actual; replay histórico explícito |
| RESOLVED + FRESH + COMPLETE + INVALIDATED | Compatible como registro histórico | Rechazar consumo actual |
| RESOLVED + FRESH + COMPLETE + WITHDRAWN | Compatible como registro histórico | Rechazar consumo actual |
| Identity=INVALID | Inválido por eje equivocado | INVALID pertenece a ContractValidity; rechazar payload/mapping |

### 4.2 Applicability × claim/evidence × purpose

| Condición | Resultado admisible | Prohibición |
|---|---|---|
| APPLICABLE + claim CONFIRMED + evidencia ADMISSIBLE | Puede contribuir a ADMISSIBLE | No implica por sí solo PurposeAssessment ADMISSIBLE |
| APPLICABLE + claim UNKNOWN | INSUFFICIENT_EVIDENCE o revisión según policy | No convertir UNKNOWN en REFUTED |
| APPLICABLE + claim CONFLICTED | CONFLICTED / revisión | No seleccionar arbitrariamente una fuente |
| APPLICABLE + claim REFUTED | Incumplimiento demostrado de ese predicado | No inferir INELIGIBLE global sin regla de Eligibility |
| NOT_APPLICABLE con regla positiva | Excluir obligación de ese scope | No inferir admisibilidad por vacuidad si todas las obligaciones son N/A |
| UNDETERMINED | No se puede concluir admisibilidad para obligación requerida | No convertir en NOT_APPLICABLE |
| Applicability INVALID | Contract/input error | No emitir resultado factual de obligación |
| Evidence QUARANTINED | Aislar claim dependiente | No consumir como evidencia admisible |
| Evidence REJECTED | No usar para sustentar claim | No inferir automáticamente claim falso |
| Evidence NOT_ASSESSED | Input no evaluado | No tratar como ADMISSIBLE ni REJECTED |
| Purpose NOT_ADMISSIBLE | Obligación de ese purpose demostrada como no satisfecha | No convertir automáticamente en INELIGIBLE global |
| Purpose ADMISSIBLE | Inputs admisibles para ese propósito | No convertir directamente en ELIGIBLE |
| Purpose CONFLICTED / INSUFFICIENT_EVIDENCE / NOT_ASSESSED | Eligibility no puede emitir favorable dependiente de ese input obligatorio | No sustituir por score o datos irrelevantes |

### 4.3 Purpose × Eligibility × Selection

| PurposeAssessment | Condición del criterio de Eligibility | Resultado de Eligibility permitido |
|---|---|---|
| ADMISSIBLE | Todos los criterios obligatorios demostrados | ELIGIBLE o ELIGIBLE_WITH_LIMITATIONS |
| ADMISSIBLE | Criterio obligatorio demostrado incumplido | INELIGIBLE |
| ADMISSIBLE_WITH_LIMITATIONS | Limitaciones compatibles y cuantificadas | ELIGIBLE_WITH_LIMITATIONS si policy lo permite |
| INSUFFICIENT_EVIDENCE | Input material obligatorio sin soporte | INSUFFICIENT_INFORMATION |
| CONFLICTED | Contradicción material que afecta elegibilidad | QUARANTINED o INSUFFICIENT_INFORMATION/REVIEW_REQUIRED según policy |
| NOT_ASSESSED | PA obligatoria ausente | INSUFFICIENT_INFORMATION |
| NOT_ADMISSIBLE | Obligación de purpose incumplida | INELIGIBLE solo si esa obligación está mapeada expresamente como criterio obligatorio de Eligibility |
| Cualquier estado no favorable | PA no obligatoria y existe base alternativa permitida | Continuar solo si policy declara alternativa, equivalencia y suficiencia |

SelectionDecision agrega outputs de eligibility, riesgo/veto, valoración, mercado y otras entradas requeridas. No debe recalcular ni ocultar resultados fuente.

## 5. Matriz de estados que bloquean, revisan o permiten continuar

| Estado/condición | Efecto por defecto | Alcance |
|---|---|---|
| ContractValidity=INVALID | REJECT_INPUT / no composición | Objeto/payload inválido |
| Scope=REJECTED_INVALID_INPUT | Rechazar input | Scope recibido |
| Scope=INCOMPLETE o UNRESOLVED | REVIEW/ABSTAIN; no conclusión favorable dependiente | Scope/partición afectada |
| Identity=UNRESOLVED | Bloquear análisis que depende de identidad inequívoca | Sujeto/representación/scope |
| Identity=CONFLICTED | REVIEW_REQUIRED/QUARANTINED | Dimensión y scope material |
| Freshness=STALE | ABSTAIN o alternativa autorizada | Solo uso/cutoff que exige frescura |
| Workflow=NOT_STARTED/IN_PROGRESS | No consumir como conclusión completa | Assessment concreto |
| Workflow=REVIEW_REQUIRED | Suspender conclusión dependiente | Assessment y dependencias afectadas |
| Lifecycle=SUPERSEDED/INVALIDATED/WITHDRAWN | Rechazar consumo actual | Versión concreta |
| Applicability=UNDETERMINED | No satisfacer obligación requerida | Obligación/purpose |
| Claim=UNKNOWN | No demostrar ni refutar | Predicado y scope |
| Claim=CONFLICTED | Revisión/adjudicación | Predicado y scope |
| Evidence=QUARANTINED/REJECTED | No usar como soporte admisible | Evidence record |
| Purpose=INSUFFICIENT_EVIDENCE/CONFLICTED/NOT_ASSESSED | No emitir favorable dependiente | PurposeAssessment |
| Eligibility=INELIGIBLE | Excluir para ese purpose/scope | No implica exclusión universal |
| Eligibility=QUARANTINED | Aislar decisión | Scope de elegibilidad |
| VETO_ACTIVE aplicable | Veto no compensable | Scope/purpose/activity definidos |
| VETO_REVIEW_REQUIRED aplicable | No favorable hasta adjudicación | Scope plausible según policy |
| VETO_NOT_ASSESSED material | No inferir NO_VETO_IDENTIFIED | Dominio y scope requeridos |
| ELIGIBLE_WITH_LIMITATIONS | Continuar solo con límites explícitos | Mandato/scope compatibles |

Los efectos por defecto no sustituyen la policy aprobada. Cada consumer debe declarar si un estado es bloqueante, revisable o no aplicable a su decisión.

## 6. Matriz de transiciones e invalidación downstream

| Evento upstream | Estado/origen afectado | Efecto downstream propuesto |
|---|---|---|
| Nueva evidencia admisible y material | Claim/Evidence | Reevaluar claim y PA/EA dependientes; no reescribir resultados históricos |
| Evidencia retractada | Claim/Evidence | Marcar RETRACTED o revisar resolución; propagar por dependency graph |
| Contradicción material nueva | Identity/Claim | CONFLICTED o REVIEW_REQUIRED según productor y adjudicación |
| TTL/cutoff excedido | Freshness | STALE por uso; no invalidar identidad factual automáticamente |
| Error contractual o digest/reference mismatch | ContractValidity | INVALID; detener composición del payload afectado |
| Identidad upstream invalidada | Identity assessment | Invalidar/revisar assessments dependientes en scope probado |
| Cambio de applicability policy | Applicability | Nueva evaluación versionada; no cambiar retrospectivamente decisión histórica |
| Cambio de purpose policy | PurposeAssessment | Reevaluar PA y consumidores que dependen de policy version |
| Cambio de eligibility criteria | EligibilityAssessment | Nueva versión de EA y SelectionDecision |
| Cambio de risk policy | Risk/Veto | Reevaluar riesgos y vetos en scopes/purposes afectados |
| Veto confirmado | VetoDecision | Propagar veto por matriz purpose/activity/scope; score no compensa |
| Veto remediado pero no validado | VetoDecision | Mantener REVIEW_REQUIRED/VETO_ACTIVE según policy; no levantar automáticamente |
| Nueva versión sucesora activada | Record lifecycle | Antigua SUPERSEDED; consumers migran solo a sucesora compatible |
| Evento invalidante confirmado | Record lifecycle | INVALIDATED con causalidad y scope; reevaluar dependientes |
| Retiro autorizado | Record lifecycle | WITHDRAWN; no equivale a falsedad ni a source retraction |
| Scope split/partition change | Scope Algebra | Revalidar partición exacta, decisiones independientes por átomo y dependencias |
| Dependency edge unresolved | Dependency completeness | UNRESOLVED; no afirmar NOT_AFFECTED |
| Policy revocada o incompatible | Policy binding | Rechazar mapping/consumo actual y requerir reevaluación |

Propagación nunca se basa solo en proximidad nominal, ticker, símbolo o similitud textual. Requiere dependency edge tipado, dirección, scope y causalidad. UNKNOWN de afectación no equivale a NOT_AFFECTED.

## 7. Reglas de precedencia entre capas

Orden de validación y composición propuesto:

1. **Contract gate:** ContractValidity debe ser VALID. INVALID detiene el procesamiento de ese input.
2. **Scope gate:** scope válido, determinado y particionado conforme al contrato. INCOMPLETE/UNRESOLVED no puede convertirse en scope universal.
3. **Lifecycle gate:** solo ACTIVE es candidato para consumo actual. Estados terminales solo para replay histórico.
4. **Workflow gate:** assessment requerido debe estar COMPLETE; REVIEW_REQUIRED bloquea conclusión dependiente.
5. **Identity gate:** sujeto y relaciones obligatorias resueltos sin conflicto material.
6. **Applicability gate:** cada obligación esperada clasificada; UNDETERMINED no se trata como NOT_APPLICABLE.
7. **Evidence/claim gate:** evidencia admisible y claims resueltos conforme a predicados y temporalidad.
8. **Freshness gate:** estado aceptable para propósito/cutoff.
9. **Purpose gate:** PA admisible o alternativa explícitamente permitida.
10. **Eligibility gate:** aplicar criterios obligatorios no compensables y emitir outcome.
11. **Risk/veto gate:** vetos aplicables prevalecen sobre score/ranking.
12. **Selection composition:** aplicar precedencia canónica únicamente a inputs válidos y scope atómico exacto.
13. **Portfolio/route handoff:** selección analítica no implica elegibilidad operacional, mandato, autorización ni ejecución.

Este orden es una secuencia de gates, no una precedencia total entre todos los estados. Por ejemplo, INVALID de contrato es un error de input, mientras que INELIGIBLE es una conclusión sustantiva; no deben compararse como si fueran niveles de una misma escala.

## 8. Matriz de combinaciones inválidas o que requieren revisión

| Combinación/evento | Clasificación | Acción |
|---|---|---|
| IdentityStatus=INVALID | Vocabulario inválido | Contract rejection |
| FreshnessStatus=UNKNOWN | Vocabulario no aprobado en R01.1 | Rechazar/migrar; ausencia de assessment por workflow |
| NOT_ASSESSED usado como ClaimResolution=UNKNOWN | Mapping inválido | Mantener workflow y resultado separados |
| UNDETERMINED usado como NOT_APPLICABLE | Mapping inválido | Recalcular applicability |
| INVALID convertido en FALSE/INELIGIBLE | Prohibido | Detener composición |
| Purpose ADMISSIBLE convertido directamente en ELIGIBLE | Mapping inválido | Ejecutar EligibilityAssessment |
| Purpose NOT_ADMISSIBLE convertido automáticamente en INELIGIBLE global | Mapping inválido | Aplicar policy explícita por criterio |
| COMPLETE + lifecycle SUPERSEDED consumido como actual | Prohibido | Rechazar consumo actual |
| ACTIVE + workflow NOT_STARTED consumido como assessment completo | Prohibido | Esperar workflow completo |
| STALE → INVALIDATED sin evento invalidante | Prohibido | Mantener freshness; investigar |
| WITHDRAWN por actor no autorizado | Evento inválido | Rechazar transición y auditar intento |
| NOT_APPLICABLE sin regla positiva | Inválido | UNDETERMINED/INCOMPLETE según causa |
| Todas las obligaciones NOT_APPLICABLE → ADMISSIBLE por vacuidad | Prohibido | Resultado insuficiente/no aplicable al purpose según policy |
| UNKNOWN scope → EMPTY/DISJOINT/NOT_AFFECTED | Prohibido | UNRESOLVED y bloquear inferencia |
| Dependency completeness=UNRESOLVED → NOT_AFFECTED | Prohibido | Mantener unresolved |
| Risk NOT_ASSESSED → NO_VETO_IDENTIFIED | Prohibido | RISK_INCOMPLETE |
| VETO_ACTIVE + score alto → ELIGIBLE | Prohibido | VETOED dentro del scope |
| Scope parcial afectado → invalidación global | Prohibido salvo prueba de alcance total | Reparticionar y derivar decisiones |
| Dos eventos concurrentes incompatibles → last-write-wins | Prohibido | Resolver causalidad y adjudicación |
| Policy versions incompatibles combinadas | Revisión obligatoria | No componer hasta compatibilidad aprobada |

## 9. Matriz mínima de productores, autoridad y segregación

La asignación definitiva depende de RACI/SoD X-08 y aprobación organizacional. Como mínimo:
- un módulo productor emite solo el assessment de su dominio;
- un validator independiente valida contrato y estructura;
- un consumer aplica mapping versionado, sin alterar el resultado fuente;
- una autoridad distinta del productor adjudica conflictos materiales y autoriza excepciones cuando SoD lo exija;
- Risk/Veto authority puede bloquear, pero no aprobar unilateralmente una excepción a su propio veto;
- Lifecycle authority procesa invalidación, supersession y withdrawal según policy;
- ningún operador puede editar retrospectivamente un assessment terminal ni levantar un veto fuera del workflow autorizado.

Los nombres de roles, titulares, quorum, delegación y break-glass siguen pendientes de aprobación de X-08. Esta matriz no asigna personas ni crea autoridad.

## 10. Contrato de decisión y trazabilidad

Cada composición debe registrar:
- decision_id, assessment refs y record versions;
- subject, scope_id/digest, purpose, activity, decision_type y cutoff;
- producer, consumer y authority refs;
- vocabularios y schema versions;
- policy versions y mapping rule IDs;
- ContractValidity, workflow, lifecycle y freshness;
- applicability obligations y dispositions;
- claim/evidence refs y admissibility;
- dependency graph snapshot y completeness;
- outcomes fuente y decisión agregada;
- veto refs y scopes;
- limitations, abstention/review reasons;
- transition/event refs, timestamps y idempotency keys;
- replay manifest y canonical input digest.

No se permite guardar únicamente el estado agregado sin preservar sus inputs y razones.

## 11. Escenarios de aceptación de integración

| ID | Escenario | Resultado esperado |
|---|---|---|
| R015-01 | Todos los gates válidos y elegibilidad demostrada | Puede emitirse outcome elegible dentro del scope; no implica ejecución |
| R015-02 | Identity UNRESOLVED para representación requerida | Bloquear conclusión dependiente |
| R015-03 | Identity RESOLVED, freshness STALE para trading | Bloquear solo uso que requiere freshness de trading |
| R015-04 | Purpose ADMISSIBLE, Eligibility no ejecutada | No emitir ELIGIBLE |
| R015-05 | Purpose NOT_ADMISSIBLE por obligación no satisfecha | Solo INELIGIBLE si policy de EA lo declara criterio obligatorio |
| R015-06 | Applicability UNDETERMINED en obligación obligatoria | No emitir favorable; revisión/insuficiencia |
| R015-07 | Claim UNKNOWN y no hay evidencia alternativa | No convertir en FALSE; abstenerse según materialidad |
| R015-08 | ContractValidity INVALID | Detener composición |
| R015-09 | Workflow COMPLETE pero lifecycle SUPERSEDED | Rechazar consumo actual |
| R015-10 | Workflow NOT_STARTED y lifecycle ACTIVE | No consumir como assessment completo |
| R015-11 | VETO_ACTIVE aplicable y score alto | Veto prevalece |
| R015-12 | VETO_NOT_ASSESSED en dominio material | No inferir NO_VETO_IDENTIFIED |
| R015-13 | Freshness NOT_APPLICABLE con regla positiva | Aceptar solo si purpose policy lo permite |
| R015-14 | Freshness ausente en COMPLETE y requerida | Error de completitud/contrato |
| R015-15 | Dependency closure UNRESOLVED | No concluir NOT_AFFECTED |
| R015-16 | Invalidación en subset de scope | Propagar solo a átomos afectados demostrados |
| R015-17 | Scope unknown interpretado como wildcard | Rechazar; no ampliar alcance |
| R015-18 | Dos policies incompatibles | REVIEW_REQUIRED; no mapping silencioso |
| R015-19 | Evidencia source withdrawn | No inferir claim false; reevaluar claim/evidence |
| R015-20 | Lifecycle WITHDRAWN sin autoridad | Rechazar transición |
| R015-21 | Assessment COMPLETE con domain outcome INELIGIBLE | Conservar COMPLETE e INELIGIBLE en ejes separados |
| R015-22 | Assessment COMPLETE, ACTIVE, FRESH, pero PA CONFLICTED | No emitir Eligibility favorable dependiente |
| R015-23 | Nueva evidencia upstream altera claim | Reevaluar downstream sin reescribir histórico |
| R015-24 | Replay de decisión histórica con policy/snapshot originales | Reproducir resultado histórico sin habilitar uso actual |
| R015-25 | Risk domain no aplicable por regla positiva | No crear veto artificial; preservar ApplicabilityAssessment |
| R015-26 | All required obligations NOT_APPLICABLE | No admitir por vacuidad; aplicar regla explícita de propósito vacío |
| R015-27 | Consumer intenta mapear NOT_ASSESSED a UNKNOWN | Rechazar mapping no autorizado |
| R015-28 | Registro ACTIVE, contract INVALID | Rechazar consumo |
| R015-29 | Identity CONFLICTED solo en dimensión no requerida | Continuar únicamente si scope/policy demuestran independencia |
| R015-30 | Estado favorable fuera del scope del consumer | No consumir; scope mismatch |
| R015-31 | Veto remediado sin validación independiente | Mantener veto/review conforme a policy |
| R015-32 | Evento concurrente invalidation/supersession | Resolver por causalidad y autoridad, nunca por orden de llegada |

## 12. Invariantes de integración

- R015-INV-01: cada estado tiene un único dominio productor y un contrato de consumidor explícito.
- R015-INV-02: los estados fuente nunca se sobrescriben con mappings de consumidores.
- R015-INV-03: INVALID es contractual, no factual.
- R015-INV-04: UNKNOWN no equivale a FALSE, EMPTY, NOT_APPLICABLE o NOT_ASSESSED.
- R015-INV-05: NOT_ASSESSED no se traduce automáticamente a UNKNOWN.
- R015-INV-06: UNDETERMINED no se traduce a NOT_APPLICABLE.
- R015-INV-07: PurposeAdmissibility no sustituye EligibilityStatus.
- R015-INV-08: COMPLETE no implica favorable, ACTIVE ni FRESH.
- R015-INV-09: ACTIVE no implica COMPLETE ni ContractValidity=VALID.
- R015-INV-10: STALE no implica INVALIDATED.
- R015-INV-11: SUPERSEDED/INVALIDATED/WITHDRAWN no se consumen como actuales.
- R015-INV-12: NOT_APPLICABLE requiere regla positiva y scope versionados.
- R015-INV-13: la vacuidad no produce admisibilidad ni elegibilidad.
- R015-INV-14: todo bloqueo se aplica al scope y purpose demostrados.
- R015-INV-15: UNKNOWN de scope/dependency no equivale a ausencia de afectación.
- R015-INV-16: los vetos críticos no son compensables por score.
- R015-INV-17: SelectionDecision no implica mandato, route eligibility ni autorización de ejecución.
- R015-INV-18: cada mapping entre productores/consumidores tiene versión, rationale y pruebas.
- R015-INV-19: los eventos concurrentes no se resuelven por last-write-wins.
- R015-INV-20: cada decisión puede reconstruirse con inputs, policies, scopes y eventos versionados.

## 13. Gates para aprobar e implementar

### Gate A — Aprobación semántica
- Owners de Identity, Evidence, Applicability, Purpose, Eligibility, Risk, Lifecycle, Scope y Decision revisan vocabularios y autoridad.
- Resolver explícitamente la adición propuesta de Purpose NOT_ADMISSIBLE.
- Confirmar vocabulario de EvidenceStatus y ClaimResolution, evitando duplicar NOT_ASSESSED.
- Aprobar qué objetos admiten WITHDRAWN y autoridad competente.

### Gate B — Aprobación de interfaces
- Aprobar producer/consumer contracts y mappings.
- Aprobar matriz de combinaciones y efectos por finalidad.
- Aprobar reglas de invalidación y propagación scope-aware.
- Aprobar policy version compatibility y replay.

### Gate C — Diseño ejecutable
- Definir schemas/envelopes versionados.
- Definir catálogo de status y transiciones machine-readable.
- Definir fixtures positivos, negativos, límites, adversariales y property-based.
- Definir validadores de compatibilidad y event processor idempotente.
- Definir CI y cobertura trazable a R01.1–R01.5.

### Gate D — Verificación
- Ejecutar pruebas unitarias, contractuales, integración y property-based.
- Formalizar invariantes seleccionadas y verificar fragmentos finitos/simbólicos.
- Revisar adversarialmente scopes, concurrency, stale/retraction y event ordering.
- Revisión independiente y evidencia reproducible.
- No declarar validación empírica hasta medir desempeño sobre datos reales point-in-time y holdouts sin leakage.

## 14. Hallazgos que permanecen abiertos

1. La propuesta R01.2 de NOT_ADMISSIBLE necesita aprobación y definición exacta de obligations.
2. El vocabulario EvidenceStatus y ClaimResolution aún requiere crosswalk canónico para evitar duplicación de NOT_ASSESSED y UNKNOWN.
3. Los productores/consumidores exactos y sus owners necesitan RACI aprobado.
4. WITHDRAWN requiere catálogo de tipos de objeto y autoridad.
5. La invalidación parcial requiere diseño ejecutable compatible con partición atómica y dependency closure.
6. La política de concurrencia, orden causal, idempotencia y adjudicación requiere contrato técnico.
7. Los vocabularios canónicos de Risk/Veto, SelectionDecision y RouteEligibility deben cotejarse con sus owners antes de fijar mappings normativos.
8. No se ha validado ningún mapping mediante schemas, código o pruebas.

## 15. Estado y limitaciones

**PROPOSED / PENDING_ARCHITECTURE_APPROVAL / NOT_APPROVED.**

Este documento consolida R01.1–R01.4 en una matriz de productores/consumidores, compatibilidad, gates, propagación y precedencia. Incluye 32 escenarios de integración y 20 invariantes. La revisión es estática y documental. No se ejecutaron pruebas automatizadas, property-based, formales, de concurrencia ni empíricas. No se modificaron documentos canónicos, requisitos IIM, schemas, código, fixtures o tests.
