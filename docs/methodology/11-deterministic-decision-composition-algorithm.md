# Algoritmo determinista de composición de decisiones

**ID:** IIE-METH-DEC-COMP-007  
**Versión:** 0.1.0-draft  
**Fecha:** 2026-10-31  
**Estado:** DRAFT / REVIEW_REQUIRED / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED  
**Hallazgo principal:** X-06 de docs/methodology/06-cross-audit-m02-m03-m04.md  
**Dependencias:** M-02, M-03, M-04 y resoluciones X-01 a X-05 (documentos 03, 04, 05, 07, 08, 09 y 10).

## 1. Propósito y límites

Definir un procedimiento reproducible, versionado y auditable para componer evaluaciones independientes en decisiones analíticas y de elegibilidad operativa. El procedimiento conserva todas las causas concurrentes, respeta los scopes, distingue evidencia insuficiente de incumplimiento demostrado y evita que scores compensen vetos.

Este documento es una especificación metodológica candidata. No aprueba vocabularios ni políticas, no modifica requisitos IIM, no crea autorización de inversión y no demuestra implementación, conformidad, seguridad ni validez empírica. Los enums y reglas aquí propuestos requieren revisión de Risk, Architecture, Operations, Security y Legal/Compliance según corresponda.

No se produce una decisión global e indiferenciada del activo. Una misma evaluación puede producir una conclusión analítica y varias conclusiones ORE, cada una ligada a finalidad, actividad, ruta y scope propios.

## 2. Principios obligatorios propuestos

1. **Separación de decisiones:** ANALYTICAL_SELECTION y OPERATIONAL_ROUTE son tipos distintos, con contratos y outcomes distintos.
2. **Fail closed, no fail broad:** un estado desconocido bloquea conclusiones dependientes; no autoriza y tampoco justifica automáticamente un bloqueo universal.
3. **No compensabilidad:** vetos activos y criterios críticos incumplidos no se compensan con puntuaciones, rentabilidad esperada ni ranking.
4. **Scope explícito:** cada input y output declara el scope al que aplica. No se extrapola entre activos, representaciones, redes, venues, cuentas, actividades, jurisdicciones, lados, tamaños u horizontes.
5. **Causas completas:** conservar todas las causas contribuyentes; la causa primaria solo ordena la presentación.
6. **No inferencia favorable:** ausencia de registro, de coincidencia o de veto no equivale a evaluación favorable.
7. **No mutación:** el compositor consume assessments inmutables; no los corrige, degrada, completa ni aprueba.
8. **Reproducibilidad:** mismos inputs, versiones, configuración y timestamp de evaluación semánticamente relevante producen el mismo resultado.
9. **Restricciones ejecutables:** una decisión limitada solo es admisible cuando cada limitación es explícita, verificable y técnicamente exigible en el consumidor.
10. **Abstención específica:** abstenerse únicamente de las conclusiones afectadas, conservando las conclusiones independientes que sí estén sustentadas.

## 3. Tipos de decisión y unidad atómica

### 3.1 ANALYTICAL_SELECTION

Compone la elegibilidad analítica y la conclusión de selección para una combinación explícita de canonical_asset_id y representation_id cuando aplique; purpose (DISCOVERY, RESEARCH, CLASSIFICATION, FUNDAMENTALS, TECHNOLOGY, RISK, MARKET_ANALYSIS, VALUATION o COMPARISON); universo y snapshot; horizonte y jurisdicción cuando sean materiales; requisitos de evidencia y módulos aplicables; policy_version y methodology_version.

La salida no habilita depósitos, retiros, transferencias, custodia ni trading.

### 3.2 OPERATIONAL_ROUTE

Compone elegibilidad para una combinación explícita de activity (MARKET_DATA, TRADE, DEPOSIT, WITHDRAWAL, TRANSFER, CUSTODY o REDEMPTION); asset/representation/network/contract; venue, counterparty, instrument, account y custodian; jurisdicción, tamaño, lado, dirección y ventana temporal cuando sean materiales; ruta concreta y sus dependencias.

La salida ORE no es TradeAuthorization, no sustituye mandato, presupuesto de riesgo, controles pre/post-trade ni enforcement.

### 3.3 Unidad atómica de composición

Antes de agregar, dividir el scope solicitado en unidades atómicas no solapadas para las dimensiones que cambien materialmente la decisión. Una unidad atómica es la combinación mínima sobre la que los inputs aplicables y sus consecuencias son homogéneos.

Ejemplo: dos venues independientes, dos redes o dos cuentas no deben agregarse si los vetos o assessments difieren. La partición debe proceder de identificadores y relaciones explícitas; el compositor no inventa dependencias.

Cada unidad se compone individualmente. La agregación del conjunto se realiza después y no permite que una unidad favorable oculte otra bloqueada.

## 4. Entradas, contrato y precondiciones

### 4.1 Inputs mínimos

El snapshot de composición debe incluir referencias inmutables y versiones de:

- EvaluationRecord y snapshot de identidad;
- AnalyticalEligibilityDecision (solo para ANALYTICAL_SELECTION);
- EvidenceAssessment por dato/módulo/uso;
- ApplicabilityAssessment por dominio de riesgo;
- RiskDomainAssessment y RiskDomainStatus;
- VetoDecision por regla y scope;
- para OPERATIONAL_ROUTE: assessments de venue, contraparte, instrumento, seguridad técnica, custodia, liquidez/costes, legal y salud operacional que sean requeridos por la política;
- matriz de propagación de vetos X-05;
- catálogo de estados, DecisionCompositionPolicy y policy versions;
- scope solicitado y partición atómica;
- timestamp de evaluación suministrado explícitamente.

No se asume que todos los objetos aplican a todos los tipos de decisión. La política declara los inputs obligatorios por finalidad/actividad y tipología de activo.

### 4.2 Validación de integridad

Antes de componer, verificar:

1. identificadores requeridos presentes y no ambiguos;
2. scope de cada input compatible o relacionable con el scope solicitado;
3. referencias de evidencia resolubles y snapshot íntegro;
4. esquema y vocabulario reconocidos por las versiones declaradas;
5. versiones de política/metodología compatibles según matriz explícita de compatibilidad;
6. timestamps, secuencia y TTL verificables;
7. no existe contradicción de identidad/scope entre inputs que impida atribución;
8. inputs obligatorios presentes, salvo exclusión explícita por aplicabilidad justificada.

Un input con versión desconocida, integridad rota, esquema inválido o scope no interpretable no se convierte en estado por defecto. Se registra como error de composición.

### 4.3 Clases de resultado de validación

- **REJECTED_INVALID_INPUT:** payload inválido, integridad rota o referencia imposible de resolver.
- **REVIEW_REQUIRED:** conflicto de versión, identidad, scope o evidencia que requiere adjudicación.
- **INCOMPLETE:** falta input obligatorio o evaluación requerida.
- **VALID:** se puede continuar con las reglas de composición.

REJECTED_INVALID_INPUT no es un DecisionOutcome de inversión; es un resultado de validación del proceso. No debe publicarse una conclusión favorable a partir de un snapshot rechazado.

### 4.4 Freshness

La frescura se evalúa con el timestamp de evaluación suministrado y la política TTL del dominio. No se usa la hora de pared oculta dentro del algoritmo. Un assessment vencido se conserva y marca como STALE para la composición afectada; no se reescribe su registro original. La expiración no levanta un VETO_ACTIVE ni equivale a NO_VETO_IDENTIFIED.

## 5. Catálogos de outcomes propuestos

### 5.1 DecisionOutcome analítico

Catálogo candidato, mutuamente excluyente por unidad atómica y versión de decisión:

| Outcome | Semántica |
|---|---|
| DECISION_NOT_ASSESSED | No se inició la composición requerida. |
| DECISION_IN_PROGRESS | La evaluación requerida está en curso y no existe aún snapshot completo para concluir. |
| DECISION_REVIEW_REQUIRED | Existe conflicto, alerta o discrepancia material adjudicable que impide conclusión automática. |
| DECISION_INELIGIBLE | Evidencia suficiente demuestra incumplimiento de un criterio obligatorio o veto analítico activo aplicable. |
| DECISION_ABSTAIN | No hay evidencia suficiente, vigente o admisible para una conclusión dependiente; no se ha demostrado inelegibilidad. |
| DECISION_ELIGIBLE_WITH_LIMITATIONS | Los criterios se cumplen dentro de restricciones explícitas y exigibles para la finalidad. |
| DECISION_ELIGIBLE | Todos los criterios aplicables están satisfechos sin limitaciones materiales pendientes. |

El resultado de valoración (ESTIMABLE, ESTIMABLE_WITH_LIMITATIONS, NOT_ESTIMABLE, INSUFFICIENT_EVIDENCE, NOT_RUN) permanece en su propio objeto. Si la finalidad requiere valoración y esta no es defendible, la decisión analítica no puede ser DECISION_ELIGIBLE; normalmente será DECISION_ABSTAIN o DECISION_REVIEW_REQUIRED según la causa. No se crea un outcome universal DECISION_NOT_ESTIMABLE.

### 5.2 RouteEligibilityStatus ORE

ORE conserva el catálogo de X-04, sin convertirlo a DecisionOutcome:

- ROUTE_NOT_ASSESSED
- ROUTE_ASSESSMENT_IN_PROGRESS
- ROUTE_ELIGIBLE
- ROUTE_ELIGIBLE_WITH_LIMITATIONS
- ROUTE_INELIGIBLE
- ROUTE_REVIEW_REQUIRED
- ROUTE_INSUFFICIENT_INFORMATION
- ROUTE_STALE
- ROUTE_CONFLICTED
- ROUTE_INVALID

Los estados de validación del proceso pueden impedir producir un RouteEligibilityStatus nuevo; se reportan en el envelope de composición.

### 5.3 Por qué no se fusionan los outcomes

DECISION_ABSTAIN no es ROUTE_INSUFFICIENT_INFORMATION; DECISION_INELIGIBLE no implica que todas las rutas sean ROUTE_INELIGIBLE; ROUTE_ELIGIBLE no convierte una selección analítica en oportunidad respaldada. Los consumidores deben consultar el tipo de decisión y scope, nunca inferirlos por una cadena de estado.

## 6. Clasificación de causas

Cada causa se registra con cause_id estable; source_object_type, source_id y source_version; domain y rule_id; status observado; severity/category definida por catálogo versionado; scope original y scope efectivo; applicability y evidencia de aplicabilidad; propagation result (AFFECTED, NOT_AFFECTED_BY_THIS_VETO, PARTIALLY_AFFECTED, SCOPE_UNRESOLVED); consequence (BLOCK, REVIEW, ABSTAIN, LIMIT, NO_PROPAGATION); reason_code y explicación; evidence references y timestamps; valid_until/review_at si aplica.

### 6.1 Categorías normalizadas

- INVALID_INPUT / INTEGRITY_FAILURE
- VERSION_INCOMPATIBLE
- IDENTITY_UNRESOLVED / IDENTITY_CONFLICT / IDENTITY_STALE
- ELIGIBILITY_FAILURE
- CRITICAL_EVIDENCE_FAILURE
- VETO_ACTIVE
- VETO_REVIEW_REQUIRED
- VETO_NOT_ASSESSED
- RISK_UNKNOWN / RISK_NOT_ASSESSED / RISK_IN_PROGRESS / RISK_STALE / RISK_CONFLICTED / RISK_INVALID
- APPLICABILITY_UNRESOLVED / APPLICABILITY_CONFLICTED
- COMPONENT_ASSESSMENT_FAILURE
- MATERIAL_DATA_INSUFFICIENCY
- MATERIAL_LIMITATION
- SCOPE_UNRESOLVED
- POLICY_REVIEW_REQUIRED

Los códigos son ilustrativos hasta su inclusión en un catálogo canónico aprobado. No son enums implementados.

## 7. Aplicabilidad, riesgo y veto

Para cada dominio de riesgo:

1. Evaluar Applicability antes de interpretar RiskDomainStatus.
2. NOT_APPLICABLE solo se acepta con rationale y evidencia taxonómica registrada.
3. APPLICABILITY_UNRESOLVED o APPLICABILITY_CONFLICTED impide tratar el dominio como cubierto favorablemente.
4. Si APPLICABLE, exigir RiskDomainAssessment vigente y suficiente para la conclusión.
5. ASSESSED_NO_MATERIAL_FINDING significa ausencia de hallazgo material dentro del scope y método, no riesgo cero.
6. ASSESSED_WITH_FINDINGS requiere evaluar hallazgos y reglas de veto; no implica veto automático.
7. NOT_ASSESSED, ASSESSMENT_IN_PROGRESS, UNKNOWN, STALE, CONFLICTED e INVALID nunca se traducen en NO_VETO_IDENTIFIED.

Para VetoDecision:

- VETO_ACTIVE: bloquea las unidades atómicas afectadas por la regla y su propagación aprobada.
- VETO_REVIEW_REQUIRED: impide una conclusión favorable en el scope afectado hasta adjudicación.
- VETO_NOT_ASSESSED: no se puede afirmar ausencia de veto; bloquea la conclusión dependiente cuando la cobertura de veto es obligatoria.
- NO_VETO_IDENTIFIED: solo cuenta como evaluación favorable de esa regla/scope si la cobertura exigida está completa y vigente.

La falta de evaluación no fabrica un VETO_ACTIVE. Puede, no obstante, impedir una conclusión favorable por abstención o revisión. Mantener RiskDomainStatus y VetoDecision como objetos separados.

## 8. Algoritmo de composición

El siguiente procedimiento es normativo solo si se aprueba esta propuesta. Se aplica por unidad atómica y tipo de decisión.

### Paso 1 — Congelar el snapshot

Construir un manifiesto inmutable de inputs, referencias, versiones, timestamps, scope solicitado, partición atómica, policy versions y timestamp de evaluación. Calcular un digest del contenido semántico del snapshot.

### Paso 2 — Validar integridad y compatibilidad

Aplicar §4.2. Si existe INVALID_INPUT o INTEGRITY_FAILURE, rechazar la composición. Si existe conflicto no resoluble de versión, identidad o scope, emitir REVIEW_REQUIRED en el envelope y no producir outcome favorable. Si faltan inputs obligatorios, continuar solo para scopes/conclusiones independientes; para la unidad dependiente, marcar INCOMPLETE.

### Paso 3 — Resolver aplicabilidad

Evaluar aplicabilidad de cada módulo y dominio requerido por la finalidad/actividad y tipología. NOT_APPLICABLE justificado se excluye del conjunto de obligaciones. APPLICABILITY_UNRESOLVED/CONFLICTED crea causa bloqueante para conclusiones dependientes, normalmente REVIEW_REQUIRED si existe contradicción adjudicable y ABSTAIN/INSUFFICIENT_INFORMATION si falta evidencia.

### Paso 4 — Evaluar dependencias

Determinar qué inputs son materiales para la conclusión y unidad atómica. No propagar una causa fuera de la matriz X-05. Si la relación entre scope de la causa y scope solicitado es incierta, clasificar SCOPE_UNRESOLVED y no asumir ni permiso ni propagación universal.

### Paso 5 — Evaluar causas y consecuencias

Para cada causa aplicable:
- BLOCK si existe incumplimiento obligatorio demostrado o VETO_ACTIVE;
- REVIEW si existe VETO_REVIEW_REQUIRED, conflicto material o adjudicación requerida;
- ABSTAIN si falta evidencia material, cobertura obligatoria, frescura o assessment;
- LIMIT si una restricción explícita permite una decisión válida en un subconjunto identificable y el consumidor puede hacerla cumplir;
- NO_PROPAGATION si se demuestra que esa regla no afecta esta unidad. Este resultado no prueba ausencia de otros vetos.

### Paso 6 — Determinar el outcome por unidad

Aplicar la precedencia de §9. Registrar todas las causas, incluso las de categorías de menor precedencia. El outcome no elimina las evaluaciones fuente.

### Paso 7 — Agregar unidades atómicas

Agregar únicamente unidades del mismo decision_type, finalidad/actividad, versión de política y clase de scope compatible. Aplicar §10. No calcular promedios ni scores para mezclar outcomes.

### Paso 8 — Emitir y auditar

Emitir el registro de decisión con causa primaria, causas secundarias, restricciones, causas no coincidentes, referencias completas, versiones, digest, timestamp, vigencia y relación de supersession. La salida se firma o protege según controles de integridad definidos por arquitectura; este documento no prescribe una tecnología criptográfica concreta.

## 9. Precedencia determinista

### 9.1 Precedencia para ANALYTICAL_SELECTION

Para una unidad atómica, aplicar la primera condición verdadera de esta lista, conservando todas las causas:

1. Integridad inválida o snapshot no interpretable: rechazar composición; no emitir outcome favorable.
2. Conflicto material de identidad, versión o scope que requiera adjudicación: DECISION_REVIEW_REQUIRED.
3. Criterio obligatorio explícitamente incumplido o veto analítico VETO_ACTIVE aplicable: DECISION_INELIGIBLE.
4. VETO_REVIEW_REQUIRED aplicable o conflicto material adjudicable: DECISION_REVIEW_REQUIRED.
5. Identidad no resuelta/obsoleta, evidencia crítica no admisible, riesgo material inválido/obsoleto/conflictivo o aplicabilidad conflictiva: DECISION_ABSTAIN o REVIEW_REQUIRED según exista contradicción adjudicable.
6. Dominio material UNKNOWN/NOT_ASSESSED/ASSESSMENT_IN_PROGRESS, VETO_NOT_ASSESSED obligatorio, evidencia material ausente o insuficiente: DECISION_ABSTAIN.
7. Evaluación obligatoria todavía en curso sin causa más restrictiva: DECISION_IN_PROGRESS.
8. Falta de evaluación iniciada: DECISION_NOT_ASSESSED.
9. Una o más limitaciones no bloqueantes, explícitas y exigibles: DECISION_ELIGIBLE_WITH_LIMITATIONS.
10. Todos los criterios y coberturas obligatorios satisfechos: DECISION_ELIGIBLE.

**Importante:** el incumplimiento afirmativo y demostrado se distingue de la ausencia de evidencia. Si una regla de veto no se evaluó, no declarar inelegibilidad por veto; abstenerse de la conclusión dependiente. Si existe un criterio afirmativo incumplido, DECISION_INELIGIBLE prevalece sobre abstención concurrente, pero las causas de abstención siguen registradas.

### 9.2 Precedencia para OPERATIONAL_ROUTE

Para una unidad de ruta, aplicar:

1. Integridad inválida: rechazar composición; no producir ruta favorable.
2. Conflicto de identidad, ruta, versión o scope: ROUTE_CONFLICTED o envelope REVIEW_REQUIRED según catálogo aprobado.
3. VETO_ACTIVE aplicable o control obligatorio con fallo afirmativo: ROUTE_INELIGIBLE.
4. VETO_REVIEW_REQUIRED o assessment conflictivo adjudicable: ROUTE_REVIEW_REQUIRED.
5. Assessment obligatorio inválido u obsoleto: ROUTE_INVALID o ROUTE_STALE, sin conversión a elegible.
6. VETO_NOT_ASSESSED obligatorio, RiskDomainStatus material no resuelto, componente obligatorio no evaluado o información insuficiente: ROUTE_INSUFFICIENT_INFORMATION o ROUTE_NOT_ASSESSED según lifecycle.
7. Assessment obligatorio en curso: ROUTE_ASSESSMENT_IN_PROGRESS.
8. Restricciones explícitas y exigibles: ROUTE_ELIGIBLE_WITH_LIMITATIONS.
9. Todos los controles y assessments requeridos satisfechos: ROUTE_ELIGIBLE.

El catálogo final debe resolver si algunos estados de validación permanecen en envelope o se reflejan en RouteEligibilityStatus; nunca se deben colapsar semánticas distintas solo para simplificar la API.

### 9.3 Desempate y causa primaria

El outcome se determina por precedencia, no por orden de llegada. La causa primaria se elige de forma estable usando: (1) consecuencia más restrictiva según el orden aprobado; (2) categoría de causa según prioridad versionada; (3) severity dentro de la misma categoría; (4) rule_id lexicográfico; (5) source_object_id lexicográfico; (6) cause_id lexicográfico.

Este desempate solo selecciona la causa principal que se muestra primero; nunca descarta causas ni cambia el outcome. No usar timestamps de llegada, orden de consulta o ranking como desempate semántico.

## 10. Composición de scopes parciales y múltiples causas

La formalización propuesta del modelo, las operaciones algebraicas, las dependencias y la prueba de partición está en [19-x09-scope-algebra-and-atomic-partition.md](19-x09-scope-algebra-and-atomic-partition.md) v0.1.0-draft. Hasta aprobación, los conceptos de exact_partition y homogeneous_decision_context siguen siendo precondiciones no implementadas. UNRESOLVED no equivale a EMPTY/DISJOINT; scope omitido no equivale a ALL. La propuesta no crea contratos ni modifica la semántica institucional vigente.

### 10.1 Regla de agregación

La propuesta específica de precedencia agregada A12 se desarrolla en [18-x09-atomic-aggregation-precedence.md](18-x09-atomic-aggregation-precedence.md) v0.1.0-draft. Hasta aprobación, se considera una regla propuesta y no una política institucional vigente.

Para un scope solicitado compuesto por unidades atómicas, una vez que ValidationEnvelope=VALID y la partición sea exacta, exhaustiva, disjunta y homogénea:

1. Una o más unidades DECISION_INELIGIBLE → DECISION_INELIGIBLE agregado.
2. En ausencia de INELIGIBLE, cualquier DECISION_REVIEW_REQUIRED → DECISION_REVIEW_REQUIRED.
3. En ausencia de los anteriores, cualquier DECISION_ABSTAIN → DECISION_ABSTAIN.
4. En ausencia de los anteriores, cualquier DECISION_IN_PROGRESS → DECISION_IN_PROGRESS.
5. En ausencia de los anteriores, cualquier DECISION_NOT_ASSESSED → DECISION_NOT_ASSESSED.
6. Todas las unidades elegibles o elegibles con limitaciones, con al menos una limitada → DECISION_ELIGIBLE_WITH_LIMITATIONS, solo con enforcement.
7. Solo todas las unidades DECISION_ELIGIBLE → DECISION_ELIGIBLE.

La precedencia propuesta es INELIGIBLE > REVIEW_REQUIRED > ABSTAIN > IN_PROGRESS > NOT_ASSESSED > ELIGIBLE_WITH_LIMITATIONS > ELIGIBLE. No es un score. Se conservan todos los outcomes y causas atómicos. Un ValidationEnvelope distinto de VALID impide emitir outcome agregado: REJECTED_INVALID_INPUT rechaza; REVIEW_REQUIRED exige adjudicación; INCOMPLETE impide concluir sobre el requested_scope completo.

El resultado agregado siempre describe el requested_scope original. Un subset permitido requiere scope derivado y decisión independiente; nunca sustituye ni mejora silenciosamente el resultado del scope original.

Para ORE, conservar los resultados por ruta. No resumir un conjunto de rutas heterogéneas en un único ROUTE_ELIGIBLE. Esta propuesta no redefine la precedencia operativa ni crea permiso global implícito.

### 10.2 Restricciones parciales

DECISION_ELIGIBLE_WITH_LIMITATIONS o ROUTE_ELIGIBLE_WITH_LIMITATIONS solo se permite si:

- la unidad permitida está identificada sin ambigüedad;
- el conjunto excluido está identificado;
- límites de venue, red, cuenta, lado, tamaño, jurisdicción, tiempo o actividad son cuantificables cuando corresponda;
- existe enforcement técnico o control humano formalmente asignado;
- se define qué ocurre si el enforcement no está disponible;
- la vigencia y el mecanismo de reevaluación están definidos.

Si no puede garantizarse el enforcement, la salida es REVIEW_REQUIRED, ABSTAIN/INSUFFICIENT_INFORMATION o INELIGIBLE según la causa demostrada. No etiquetar como “limitación” una prohibición que el consumidor no puede hacer cumplir.

### 10.3 Vetos múltiples

Cada veto se evalúa de forma independiente. Levantar o resolver un veto no elimina otros vetos. El conjunto de consecuencias se compone por unidad; prevalece la más restrictiva, pero se conservan todas las reglas y scopes coincidentes.

## 11. Registro de salida obligatorio

Cada decisión debe incluir, como mínimo:

- decision_id;
- decision_type: ANALYTICAL_SELECTION u OPERATIONAL_ROUTE;
- purpose o activity;
- requested_scope y atomic_scope_id;
- outcome o route_eligibility_status;
- primary_reason_code;
- contributing_causes[] completo;
- non_matching_vetoes[] con la etiqueta NOT_AFFECTED_BY_THIS_VETO y evidencia de no coincidencia;
- limitations[] y enforcement_reference cuando aplique;
- input_references[] con object_type, object_id, version, status y scope;
- evidence_snapshot_id/digest;
- state_catalog_version;
- decision_composition_policy_version;
- veto_propagation_policy_version;
- methodology_version y domain_policy_versions;
- evaluated_at (suministrado);
- valid_until o reevaluation_trigger;
- supersedes_decision_id;
- algorithm_version;
- deterministic_result_digest;
- validation_result y warnings.

El registro debe distinguir evaluated_at (metadato del evento) del digest semántico. El digest se calcula sobre inputs normalizados, versiones, scope, configuración y reglas; no debe depender del orden de llegada ni de campos volátiles no semánticos. El diseño exacto de canonicalización/digest requiere especificación técnica posterior.

## 12. Determinismo, replay y versionado

La composición será determinista si se cumplen todos estos requisitos:

1. Orden canónico de arrays y colecciones por identificadores estables antes de evaluar.
2. Normalización de identificadores, unidades, decimales, timestamps y scopes según contratos versionados.
3. Ninguna consulta externa, reloj del sistema, aleatoriedad o dato mutable durante la composición.
4. Todo TTL evaluado contra el timestamp explícito del snapshot.
5. Catálogos, políticas, matrices de aplicabilidad y propagación fijados por versión.
6. Tie-break estable según §9.3.
7. Snapshot inmutable y completo de referencias de entrada.
8. Cambios de código, reglas, vocabularios o normalización generan nueva algorithm_version o policy version.
9. Mismo snapshot semántico + mismas versiones + mismo timestamp de evaluación y configuración → mismo outcome, causas y digest.
10. El replay histórico utiliza las versiones originales; no reinterpreta silenciosamente decisiones antiguas con la política vigente.

Una actualización de política o evidencia crea una nueva decisión que supersede la anterior; no edita el historial.

## 13. Tabla de decisión y escenarios de aceptación

| # | Inputs/condición | Tipo | Resultado esperado |
|---|---|---|---|
| 1 | Todos los módulos obligatorios completos, vigentes y favorables; sin vetos | Analítico | DECISION_ELIGIBLE |
| 2 | Criterio obligatorio incumplido con evidencia suficiente | Analítico | DECISION_INELIGIBLE |
| 3 | Evidencia material ausente; no hay incumplimiento demostrado | Analítico | DECISION_ABSTAIN |
| 4 | VETO_ACTIVE limitado a retiro; finalidad RESEARCH sin dependencia de retiro | Analítico | No propagar ese veto; evaluar el resto de RESEARCH |
| 5 | VETO_ACTIVE aplicable a la finalidad analítica solicitada | Analítico | DECISION_INELIGIBLE |
| 6 | VETO_REVIEW_REQUIRED aplicable | Analítico | DECISION_REVIEW_REQUIRED |
| 7 | VETO_NOT_ASSESSED en cobertura obligatoria | Analítico | DECISION_ABSTAIN, no DECISION_ELIGIBLE |
| 8 | Dominio material APPLICABILITY_UNRESOLVED | Analítico | DECISION_REVIEW_REQUIRED o ABSTAIN según contradicción/evidencia |
| 9 | Dominio NOT_APPLICABLE con justificación válida y resto completo | Analítico | Excluir ese dominio; no penalizar ni asumir riesgo cero |
| 10 | RiskDomainStatus STALE para input material | Analítico | DECISION_ABSTAIN |
| 11 | Dos vetos activos, uno se resuelve y otro permanece | Analítico/ORE | Mantener bloqueo por el segundo veto |
| 12 | Veto de bridge afecta wrapper, sin dependencia nativa demostrada | Analítico/ORE | Propagar solo al wrapper y rutas dependientes |
| 13 | Dos venues: uno bloqueado, otro independiente y apto | ORE | Resultados por ruta separados; no bloquear ni habilitar ambas globalmente |
| 14 | Parte del scope elegible, parte ineligible, subset enforceable | Analítico/ORE | WITH_LIMITATIONS solo si la restricción se puede hacer cumplir y se desglosa |
| 15 | Scope parcial no puede aislarse ni hacerse cumplir | Analítico/ORE | REVIEW/ABSTAIN; nunca favorable global |
| 16 | Ruta ROUTE_ELIGIBLE, AE no evaluada | Ambos | ORE favorable no implica DECISION_ELIGIBLE |
| 17 | AE favorable, ruta con custodia comprometida | Ambos | Mantener AE; ORE bloqueada en el scope afectado |
| 18 | Versión de catálogo desconocida o incompatible | Ambos | Rechazo o REVIEW_REQUIRED; nunca fallback favorable |
| 19 | Input duplicado idéntico por id/version | Ambos | Deduplicación determinista documentada o rechazo por contrato; nunca doble ponderación |
| 20 | Inputs contradictorios para mismo objeto/version/scope | Ambos | REVIEW_REQUIRED/CONFLICTED; no escoger arbitrariamente |
| 21 | Score alto con VETO_ACTIVE aplicable | Analítico | DECISION_INELIGIBLE; score no compensa |
| 22 | Todos los inputs obligatorios en curso | Ambos | IN_PROGRESS correspondiente; no declarar elegibilidad |
| 23 | Ninguna evaluación iniciada | Ambos | NOT_ASSESSED correspondiente |
| 24 | NO_VETO_IDENTIFIED sin cobertura completa de reglas obligatorias | Ambos | Input inválido/no admisible para conclusión favorable; abstenerse/revisar |

## 14. Invariantes de conformidad

Una futura suite de conformidad debe verificar, como mínimo:

- I-01: ningún VETO_ACTIVE aplicable produce outcome favorable en su scope.
- I-02: ningún score puede cambiar un outcome bloqueado por veto o criterio crítico.
- I-03: VETO_NOT_ASSESSED nunca se interpreta como NO_VETO_IDENTIFIED.
- I-04: NOT_APPLICABLE requiere rationale y no equivale a riesgo cero.
- I-05: UNKNOWN/NOT_ASSESSED/STALE/CONFLICTED/INVALID no se convierten en PASS.
- I-06: una causa no se propaga fuera del scope/dependencia definida.
- I-07: resolver un veto no resuelve vetos independientes.
- I-08: ORE no modifica AE; AE no habilita ORE.
- I-09: ORE no crea TradeAuthorization.
- I-10: un scope mixto no se resume como elegible pleno.
- I-11: limitaciones solo se admiten con enforcement identificable.
- I-12: el outcome conserva todas las causas concurrentes.
- I-13: mismo snapshot/versiones/configuración produce mismo resultado.
- I-14: replay histórico usa versiones originales.
- I-15: inputs inválidos o incompatibles nunca producen permiso favorable.
- I-16: ausencia de un veto no es evidencia de ausencia de otros vetos.
- I-17: NOT_AFFECTED_BY_THIS_VETO no equivale a NO_VETO_IDENTIFIED.
- I-18: el orden de llegada de inputs no cambia el resultado.

## 15. Cuestiones abiertas para revisión

1. La precedencia agregada propuesta para coocurrencia INELIGIBLE/REVIEW/ABSTAIN se desarrolla en [X-09 documento 18](18-x09-atomic-aggregation-precedence.md): INELIGIBLE > REVIEW_REQUIRED > ABSTAIN para el resumen del requested_scope, después de validar el envelope. Sigue pendiente aprobación de owners; conservar outcomes y causas atómicos.
2. ¿Cómo mapear exactamente ROUTE_CONFLICTED/ROUTE_INVALID/ROUTE_STALE frente al envelope de validación? Requiere armonización con X-04 y contrato ORE.
3. ¿Qué criterios de evidencia son suficientes para NO_VETO_IDENTIFIED por cada dominio/tipología? Requiere política de riesgo/veto aprobada.
4. ¿Qué restricciones parciales puede aplicar realmente cada consumidor? Requiere contratos de enforcement y pruebas.
5. ¿Qué canonicalización y digest se adoptarán? Requiere especificación técnica y revisión de seguridad.
6. ¿Cómo se resuelven scopes jerárquicos, alias, dependencias compartidas y relaciones temporales? Requiere contrato de identidad/grafo de dependencias.
7. ¿Qué estados se consideran recuperables automáticamente y cuáles requieren aprobación humana? Requiere RACI/SoD de X-08.
8. ¿Cómo se armoniza la precedencia propuesta con los gates legacy y la metodología maestra sin migración silenciosa? Requiere matriz de migración versionada.

## 16. Criterios de aceptación X-06

- [ ] Separación formal ANALYTICAL_SELECTION y OPERATIONAL_ROUTE.
- [ ] Catálogo de outcomes analíticos sin colisión semántica con ORE.
- [ ] Inputs obligatorios definidos por finalidad, actividad y tipología.
- [ ] Validación de integridad, compatibilidad, scope y freshness antes de composición.
- [ ] Aplicabilidad separada de estado de riesgo y decisión de veto.
- [ ] Precedencia completa para decisiones analíticas y operativas.
- [ ] Tratamiento determinista de múltiples causas y selección de causa primaria.
- [ ] Agregación por unidades atómicas y scopes parciales.
- [ ] Restricciones limitadas condicionadas a enforcement verificable.
- [ ] No propagación y no coincidencia no se confunden con ausencia global de vetos.
- [ ] Snapshot, provenance, versionado, replay y digest especificados.
- [ ] Escenarios positivos, negativos, parciales, conflictivos y de incompatibilidad revisados.
- [ ] Revisión especializada de Risk, Architecture, Operations, Security y Legal/Compliance.
- [ ] Aprobación explícita antes de schemas, implementación o actualización de requisitos.

## 17. Estado y limitaciones

X-06 queda especificado como propuesta documental para revisión. No se ha creado schema, código, motor DECISION/ORE ni suite ejecutable a partir de este documento. No se han ejecutado pruebas, backtests ni validaciones empíricas. No se modifica el registro de requisitos IIM. La publicación de este draft no cierra X-06: el cierre requiere resolver cuestiones abiertas, completar criterios de aceptación y obtener aprobación de los responsables designados.
