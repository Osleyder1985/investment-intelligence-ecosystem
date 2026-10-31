# Resolución arquitectónica propuesta — D-03 y D-04

**ID:** IIE-IDENTITY-ARCH-DECISIONS-006  
**Versión:** 0.1.0-draft  
**Fecha:** 2026-10-31  
**Estado:** PROPOSED / PENDING_ARCHITECTURE_REVIEW / NOT_APPROVED  
**Dependencias:** decisiones D-01/D-02; docs. 03, 12, 19 y 22; propuesta integrada de identidad  
**Issue:** #61

## 1. Propósito y límites

Este documento desarrolla D-03 (aplicabilidad) y D-04 (ClaimEnvelope). Busca impedir que ausencia de aplicabilidad se convierta en admisibilidad favorable, y definir la unidad semántica mínima de una afirmación verificable. Es una propuesta documental; no aprueba vocabularios, no modifica requisitos IIM, documentos metodológicos canónicos, schemas ni código.

## 2. D-03 — Aplicabilidad como evaluación independiente

### 2.1 Decisión propuesta

`ApplicabilityAssessment` será el único objeto que emite el resultado de aplicabilidad de una regla, obligación, criterio o método para un scope concreto.

Vocabulario propuesto:
- `APPLICABLE`: la regla corresponde al objeto/scope según la taxonomía y predicados aprobados.
- `NOT_APPLICABLE`: se ha demostrado que la regla no corresponde al objeto/scope bajo una regla taxonómica versionada.
- `UNDETERMINED`: la aplicabilidad no puede resolverse con la información disponible.
- `INVALID`: la definición, referencia, tipo, versión o estructura de la evaluación incumple el contrato; es error de validez, no un resultado factual.

`NOT_APPLICABLE` no será un estado de `EvidenceAssessmentStatus`, `ClaimResolutionStatus` ni `PurposeAdmissibilityStatus`. Tampoco se inferirá de la ausencia de claim, de una fuente, de una ruta o de una dimensión.

### 2.2 Contrato de ApplicabilityAssessment

Cada assessment futuro debe preservar:
- applicability_assessment_id y versión;
- rule_id / obligation_id / criterion_id y versión;
- subject y ScopeDefinition referenciados;
- taxonomic predicate / applicability policy y versión;
- resultado y reason codes;
- claims/evidence usados para decidir aplicabilidad;
- exclusions justificadas y referencias a evidencia;
- evaluated_at, valid_time y snapshot de universo/contexto;
- actor/engine, provenance y lifecycle references.

Un resultado `NOT_APPLICABLE` exige una justificación positiva y reproducible: regla taxonómica, versión, scope, predicado evaluado y evidencia/referencia que sustenta la exclusión. “No se encontró información” no satisface este requisito.

### 2.3 Agregación de obligaciones en PurposeAssessment

PurposeAssessment debe enumerar el conjunto completo de obligaciones esperadas para el propósito y scope, según un catálogo de obligaciones versionado. Cada obligación se registra como:
- APPLICABLE + resultado de evaluación/admisibilidad;
- NOT_APPLICABLE + justificación;
- UNDETERMINED;
- INVALID.

No se permite eliminar del expediente obligaciones no aplicables: permanecen visibles y auditables.

El envelope agregado mantiene dimensiones separadas:
1. `applicability_coverage`: estado de resolución del conjunto de obligaciones;
2. `applicable_obligation_outcomes`: resultados solo de obligaciones aplicables;
3. `evidence_sufficiency`: suficiencia de la evidencia requerida;
4. `assessment_lifecycle` y freshness;
5. `purpose_assessment_outcome`: resultado del proceso, según política explícita.

**Regla crítica de vacuidad:** si todas las obligaciones son NOT_APPLICABLE, el sistema no puede emitir automáticamente `ADMISSIBLE`, `ELIGIBLE`, `PASS` ni una conclusión favorable. Debe producir un resultado de proceso diferenciado, por ejemplo `NO_APPLICABLE_REQUIREMENTS` o `ASSESSMENT_NOT_REQUIRED`, solo si el catálogo/política de purpose autoriza expresamente ese resultado. Si no existe esa regla, el agregado queda `REVIEW_REQUIRED`/no concluyente. Este resultado de proceso no es un nuevo estado de EvidenceStatus ni de DecisionOutcome.

Si alguna obligación es UNDETERMINED, la cobertura no es completa. Si alguna obligación aplicable tiene evidencia insuficiente, la no aplicabilidad de otras obligaciones no compensa esa insuficiencia. Si aparece INVALID, se bloquea la composición afectada.

### 2.4 Separación entre applicability y evidence/admissibility

| Situación | Applicability | Evidence assessment | Efecto |
|---|---|---|---|
| El método no corresponde a la tipología demostrada | NOT_APPLICABLE | Puede no existir assessment de evidencia para ese método | Excluir solo esa obligación, con justificación |
| Método aplicable pero falta documentación | APPLICABLE | UNKNOWN / NOT_ASSESSED / evidencia insuficiente | No declarar NOT_APPLICABLE |
| No se sabe si la tipología corresponde | UNDETERMINED | Cualquiera | No excluir; revisar/abstenerse |
| Evidencia de fuente rechazada | No cambia por sí sola | REJECTED | No prueba no aplicabilidad ni falsedad del claim |
| Campo o regla malformada | INVALID | No procede composición | Fallo de contrato |

### 2.5 Escenarios D-03

| ID | Escenario | Resultado exigido |
|---|---|---|
| D03-01 | No existe claim para un campo requerido | No inferir NOT_APPLICABLE; aplicar política de evidencia ausente |
| D03-02 | Token sin mecanismo de redención; regla de redención tipológicamente inaplicable, demostrado por contrato del activo | NOT_APPLICABLE solo para esa obligación y scope |
| D03-03 | No se pudo verificar si existe derecho de redención | UNDETERMINED, no NOT_APPLICABLE |
| D03-04 | Todas las obligaciones de un purpose resultan NOT_APPLICABLE | NO_APPLICABLE_REQUIREMENTS solo con política explícita; nunca ADMISSIBLE por vacuidad |
| D03-05 | Una obligación aplicable está UNKNOWN y las demás NOT_APPLICABLE | Resultado no concluyente/insuficiente; las exclusiones no compensan |
| D03-06 | Cambio de taxonomía | Nueva versión de assessment; conservar resultado histórico y policy version |
| D03-07 | Scope de obligación no comparable o incompleto | UNDETERMINED o INVALID según el tipo de fallo; nunca NOT_APPLICABLE por defecto |
| D03-08 | Evidencia de applicability se vuelve STALE | Recalcular freshness; la conclusión histórica se conserva, el uso actual requiere reevaluación |

### 2.6 Resolución propuesta de ADV-03

**ADDRESSED_IN_DRAFT.** El único emisor de NOT_APPLICABLE es ApplicabilityAssessment. Falta aprobación del catálogo de obligaciones, política para el caso de cero obligaciones aplicables y revisión transversal con doc. 03.

## 3. D-04 — ClaimEnvelope como proposición tipada y atómica

### 3.1 Decisión propuesta

Un Claim es una proposición declarativa, tipada, versionada, con sujeto, predicado, objeto/valor y qualifiers suficientes para determinar qué hecho se afirma. No es un documento fuente, una observación bruta, un resultado de admisibilidad ni una decisión.

Un ClaimEnvelope es el contenedor inmutable de:
- proposición canónica;
- contexto de interpretación;
- scope y temporalidad;
- contrato del predicado;
- resultado de resolución;
- referencias a EvidenceRecords y sus assessments;
- provenance, adjudicación y lifecycle.

Un envelope puede agrupar metadatos y un único claim atómico. Si una frase contiene varias proposiciones que pueden ser verdaderas/falsas independientemente, debe descomponerse en claims distintos.

### 3.2 Forma lógica

Representación conceptual:

`Claim = (subject, predicate, object/value, qualifiers, polarity, temporal_scope, context)`

El ClaimEnvelope añade identidad/versionado, contrato, evidencias, resolución, adjudicación y ciclo de vida. Esta notación no es todavía un schema ni impone una serialización.

### 3.3 Campos conceptuales obligatorios

| Campo | Semántica |
|---|---|
| claim_id | Identificador global, estable e inmutable del claim |
| claim_version | Versión inmutable de la proposición/contexto; una modificación material crea nueva versión/claim según política |
| subject_ref + subject_type | Sujeto canónico tipado; no usar ticker/nombre libre como identidad |
| predicate_id + predicate_version | Predicado de catálogo con dominio, rango, cardinalidad y semántica definidos |
| object_ref u object_value | Objeto/target canónico o literal tipado |
| polarity | Afirmación o negación explícita de la proposición; no confundir con status de resolución |
| qualifiers | Condiciones que restringen la afirmación: red, representación, mercado, jurisdicción, estado, método, etc. |
| temporal_context | valid_time del hecho afirmado y precisión/fuente temporal |
| scope_ref | ScopeDefinition y versión; el claim no crea un scope implícito |
| interpretation_contract | Semántica/versiones de predicado, unidades, normalización y reglas de adjudicación |
| resolution_record_ref | Referencia al registro de evaluación de resolución; mutable no, cada reevaluación crea registro nuevo |
| evidence_refs | Referencias a EvidenceRecords y ClaimEvidenceAssessments, no artefactos embebidos |
| provenance | Originador, método de captura, sistema, source refs, timestamps y digest |
| lifecycle_refs | supersedes/retracts/invalidates y causa, sin borrar historial |

Campos condicionales: unit, currency, precision, uncertainty bounds, quantifier, population/universe, closed-world declaration, jurisdiction, environment, method/standard, chain/network ID, block height/hash, transaction/event reference, legal entity/person, confidence/calibration. Su obligatoriedad depende del contrato del predicate.

### 3.4 Atomicidad y tipado

1. Un claim debe ser evaluable independientemente bajo su predicate contract.
2. No debe contener conjunciones de hechos con independencia lógica/material. Ejemplo: “el token está emitido por X, respaldado 1:1 y redimible en cualquier momento” se divide en tres claims.
3. Una relación también es una proposición: subject y object son entidades tipadas y predicate define la relación. Ejemplo `ASSET_A BACKED_BY RESERVE_B` no demuestra solvencia, titularidad, segregación, auditabilidad ni derecho de redención; estos requieren claims separados.
4. El predicate contract declara domain/range, cardinality, inverse/symmetry/transitivity si aplica, temporal semantics, permitted qualifiers y adjudication method.
5. No inferir transitividad, simetría, inversa ni cardinalidad por el nombre del predicate.
6. Valores cuantitativos incluyen unidad, moneda, precisión, inclusividad, fuente de conversión y método de redondeo cuando material.
7. La polaridad negativa es una proposición explícita (por ejemplo, NOT_BACKED_BY), no el estado REFUTED de un claim positivo.
8. Una corrección material no sobrescribe el claim anterior: crea una versión/claim nuevo enlazado mediante supersedes/retracts.

### 3.5 ClaimResolutionStatus

Vocabulario propuesto para un ResolutionRecord, no para EvidenceRecord:
- CONFIRMED: la proposición exacta queda demostrada bajo estándar y scope declarados.
- REFUTED: la proposición exacta queda refutada bajo estándar y scope declarados.
- UNKNOWN: no se demuestra ni refuta; falta información, cobertura o resolución.
- CONFLICTED: existen resultados/evidencias materialmente incompatibles sin adjudicación.
- NOT_ASSESSED: no se ha ejecutado evaluación.
- RETRACTED: claim retirado por originador/autoridad según política; preserva historial y causa.

`PARTIALLY_CONFIRMED` no es un estado permitido del claim atómico. Se divide la proposición en claims atómicos o se emite un conjunto de resultados por subclaim con relaciones explícitas.

`INVALID` no es ClaimResolutionStatus: es error del contrato/envelope/predicate y bloquea la evaluación. `REJECTED` pertenece a la evaluación de evidencia, no declara falso el claim. `STALE` pertenece a freshness; un claim histórico puede seguir siendo verdadero aunque una evaluación actual no sea suficientemente fresca.

### 3.6 Relación con FSA-01: mapping controlado

Un ClaimResolutionStatus no se convierte directamente en TRUE/FALSE para Scope Algebra. La traducción requiere un PredicateContract que defina formalmente cómo el claim determina la pertenencia.

| Claim resolution | Condición del predicate contract | Resultado de pertenencia |
|---|---|---|
| CONFIRMED | Claim cubre exactamente la condición necesaria/suficiente, scope y cutoff; evidencia admisible y estándar satisfecho | TRUE |
| REFUTED | Claim refuta exactamente la condición y el contrato define esa condición como negación suficiente de pertenencia | FALSE |
| REFUTED | Claim no corresponde exactamente al predicate de pertenencia | UNKNOWN o no aplicable a esa derivación; nunca FALSE por analogía |
| UNKNOWN / CONFLICTED / NOT_ASSESSED | La condición depende materialmente del claim | UNKNOWN |
| RETRACTED | No existe resolución vigente sustituta | UNKNOWN para uso actual; registro histórico preservado |
| Cualquier estado | Envelope/predicate/tipos/versiones/referencias inválidos | INVALID fuera del dominio lógico |

Además:
- Evidence inadmissible no aporta soporte para TRUE, pero no demuestra FALSE.
- Claim confirmado con evidencia inadmisible no es suficiente para TRUE.
- La admisibilidad se evalúa por purpose/scope/cutoff; la misma EvidenceRecord puede ser admisible para una finalidad y no para otra.
- Closed-world solo se permite si el predicate contract declara universo, población, cobertura, cutoff y autoridad de completitud. La ausencia de una relación en un grafo abierto no demuestra su falsedad.
- La polaridad del claim, el estado de resolución y la pertenencia al scope son tres conceptos separados.

### 3.7 Temporalidad y point-in-time

Deben preservarse como mínimo:
- valid_time: cuándo el hecho es válido en el dominio;
- observed_at: cuándo fue observado;
- source_published_at: cuándo la fuente lo publicó, si existe;
- recorded_at: cuándo el sistema lo registró;
- evaluated_at: cuándo se evaluó;
- effective_at: cuándo produce efecto una transición, si aplica;
- knowledge_cutoff: límite de información permitido para reconstrucción point-in-time.

No sustituir un timestamp desconocido por recorded_at ni asumir que publicación equivale a observación o conocimiento. Un backtest solo puede consumir claims/evidencias disponibles bajo su knowledge_cutoff, según la política de point-in-time aprobada.

### 3.8 Escenarios D-04

| ID | Escenario | Resultado exigido |
|---|---|---|
| D04-01 | Claim compuesto sobre emisión, backing y redención | Descomponer en tres claims atómicos |
| D04-02 | A BACKED_BY B | No inferir solvencia, titularidad ni redención |
| D04-03 | Claim positivo REFUTED por evidencia suficiente | FALSE solo para predicate idéntico y contrato que lo haga suficiente |
| D04-04 | Claim de un hecho relacionado pero no idéntico | No propagar REFUTED a otro predicate; UNKNOWN si la pertenencia depende de él |
| D04-05 | Fuente inadmisible respalda claim | No aporta TRUE; no genera FALSE |
| D04-06 | Grafo abierto sin relación registrada | UNKNOWN, no FALSE |
| D04-07 | Universo cerrado con cobertura completa y ausencia demostrada | FALSE solo conforme al predicate contract y evidencia de completitud |
| D04-08 | Datos publicados después del knowledge_cutoff | Excluidos de la reconstrucción point-in-time |
| D04-09 | Claim retracted | Preservar versión histórica; uso actual UNKNOWN hasta resolución sustituta |
| D04-10 | Cambio de predicate semantics | Nueva predicate_version y reevaluación; no reinterpretar silenciosamente resultados históricos |
| D04-11 | Claim confirmado, pero scope de la evidencia no contiene el scope solicitado | No heredar CONFIRMED; UNKNOWN o nueva evaluación |
| D04-12 | Falta unit/currency para predicado cuantitativo que las exige | INVALID si incumple estructura; UNKNOWN si el valor existe pero no puede determinarse |

### 3.9 Resolución propuesta de ADV-04

**ADDRESSED_IN_DRAFT.** Se propone ClaimEnvelope tipado, claim atómico, predicate contract versionado y mapping condicionado a FSA-01. La aprobación requiere revisión de dominios, seguridad, data governance, Risk y Architecture, más prueba formal/fixtures.

## 4. Interfaces entre D-03 y D-04

1. ApplicabilityAssessment puede consumir claims como evidencia para decidir si una obligación aplica; no altera el status de esos claims.
2. ClaimResolutionRecord no declara que un propósito sea aplicable ni que la evidencia sea admisible para todos los usos.
3. PurposeAssessment consume el catálogo completo de obligaciones, ApplicabilityAssessments y Evidence/Claim assessments, manteniendo los estados fuente.
4. La decisión de inversión consume el PurposeAssessment solo a través de una interfaz explícita; no se hereda autorización de ejecución.
5. Un evento de lifecycle puede invalidar un assessment o requerir reevaluación, pero no reescribe el hecho histórico ni muta por sí solo ClaimResolutionStatus.

## 5. Criterios de aprobación

- [ ] Se aprueba el vocabulario y el contrato de ApplicabilityAssessment.
- [ ] Se aprueba la política de agregación cuando todas las obligaciones son NOT_APPLICABLE.
- [ ] Se aprueba el catálogo de obligaciones por purpose/scope y su versionado.
- [ ] Se aprueba la atomicidad y tipado de ClaimEnvelope.
- [ ] Se aprueba PredicateContract: domain/range, cardinalidad, qualifiers, lógica, closed/open-world y adjudicación.
- [ ] Se aprueba la separación ClaimResolutionStatus / EvidenceAssessmentStatus / Freshness / Lifecycle.
- [ ] Se aprueba mapping condicionado FSA-01 y se demuestra con pruebas negativas y casos de incertidumbre.
- [ ] Se aprueba temporalidad y knowledge_cutoff point-in-time.
- [ ] Se revisan escenarios D03-01..08 y D04-01..12.
- [ ] Se realiza auditoría cruzada posterior con docs. 03, 12, 19 y 22.

## 6. Estado final

**PROPOSED / PENDING_ARCHITECTURE_REVIEW / NOT_APPROVED.**

D-03 y D-04 quedan desarrolladas en borrador. No se modificaron documentos metodológicos canónicos, requisitos IIM, schemas, código, fixtures ni tests. No se ejecutaron pruebas, pruebas formales ni validación empírica.
