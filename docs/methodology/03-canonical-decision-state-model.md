# Modelo canónico de estados y transiciones de decisión

**ID:** IIE-METH-STATE-001  
**Versión:** 0.1.0-draft  
**Estado:** DRAFT — pendiente de revisión; no normativo ni implementado  
**Hallazgo relacionado:** M-02 de `docs/methodology/02-methodology-consistency-audit.md`  
**Ámbito:** metodología de selección de criptoactivos y sus interfaces con riesgo, ranking y Portfolio & Execution.

## 1. Propósito

Establecer semántica canónica para los estados utilizados por las políticas de identidad, datos, elegibilidad, riesgo, valoración, decisión y ranking. Definir composición, precedencia, transiciones, reevaluación y migración sin colapsar dominios distintos en un único `asset_status`.

Este documento resuelve una inconsistencia documental. No constituye implementación de una máquina de estados, aprobación metodológica, autorización de inversión ni validación empírica.

## 2. Principios normativos propuestos

1. Todo estado pertenece a un dominio y a un objeto/scope explícito. No existe un estado global e indiferenciado del activo.
2. La ausencia de evidencia nunca equivale a evidencia favorable.
3. `UNKNOWN`, `STALE`, `INVALID`, `CONFLICTED` y `NOT_ASSESSED` nunca se convierten implícitamente en `PASS`, `ELIGIBLE` o `NO_VETO_IDENTIFIED`.
4. Los vetos críticos aplicables son no compensables. Ningún score, precio, upside, ranking o ponderación puede anularlos.
5. Un resultado de una finalidad no se hereda automáticamente por otra finalidad, representación, mercado, jurisdicción, tamaño, horizonte o ruta.
6. Los estados derivados deben conservar referencias a los estados fuente, razones, alcance y versión de política.
7. Toda reevaluación produce un nuevo registro inmutable enlazado al anterior.
8. La precedencia determina el resultado agregado; no elimina ni sobrescribe los estados concurrentes de otros dominios.
9. Los estados terminales son terminales únicamente para una versión de evaluación. Un nuevo evento o evidencia abre una nueva evaluación.
10. Los vocabularios de este documento son propuestas hasta aprobación formal y migración controlada.

## 3. Modelo de objetos y alcance

La evaluación se identifica, como mínimo, por:

- `canonical_asset_id`;
- `representation_id` (red, contrato, wrapper u otra representación cuando aplique);
- `market_id` y venue set cuando corresponda;
- `purpose` (por ejemplo: DISCOVERY, RESEARCH, CLASSIFICATION, VALUATION, HISTORICAL_COMPARISON, CUSTODY o EXECUTION);
- `jurisdiction_scope`, cuando sea material;
- `horizon`, cuando sea material;
- `evaluation_timestamp_utc`;
- `universe_snapshot_id`;
- `policy_version`, `methodology_version` y `evidence_manifest_id`.

El alcance de una decisión debe ser explícito. Una restricción a una representación, mercado, custodio o ruta no se generaliza al activo completo sin evidencia que lo justifique.

## 4. Dominios canónicos independientes

### 4.1 Estado del expediente de evaluación — EvaluationLifecycle

| Estado | Significado |
|---|---|
| NOT_STARTED | No existe evaluación iniciada |
| IN_PROGRESS | Evaluación en curso |
| COMPLETE | Los módulos requeridos para el alcance terminaron; no implica resultado favorable |
| REVIEW_REQUIRED | Requiere revisión humana/independiente antes de una conclusión definida |
| SUPERSEDED | Reemplazado por una evaluación nueva |
| INVALIDATED | La evaluación perdió validez por evento, error o evidencia invalidante |

`COMPLETE` describe completitud del proceso, no calidad suficiente ni elegibilidad. `SUPERSEDED` e `INVALIDATED` son terminales para ese registro. No se reescribe el historial.

### 4.2 Identidad — IdentityStatus

| Estado | Significado |
|---|---|
| RESOLVED | Identidad y representación resueltas para el alcance |
| UNRESOLVED | No se pudo resolver |
| CONFLICTED | Existen identificaciones/evidencias incompatibles |
| STALE | La resolución dejó de ser suficientemente actual |

Solo `RESOLVED` permite continuar módulos dependientes de identidad. `UNRESOLVED`, `CONFLICTED` y `STALE` bloquean conclusiones dependientes; no implican necesariamente que el activo sea inválido.

### 4.3 Admisibilidad de evidencia — EvidenceStatus

Estado por dato, fuente, módulo y uso previsto:

| Estado | Significado |
|---|---|
| ADMISSIBLE | Evidencia admisible para el uso y timestamp especificados |
| ADMISSIBLE_WITH_LIMITATIONS | Utilizable con limitaciones explícitas |
| QUARANTINED | Retenida por anomalía, conflicto o investigación pendiente |
| REJECTED | No admisible para el uso declarado |
| UNKNOWN | No se conoce suficientemente su calidad/procedencia |
| STALE | Supera la política de frescura aplicable |
| NOT_ASSESSED | Aún no evaluada |

No agregar evidencia heterogénea a un único estado sin preservar el estado por variable y fuente. Una variable crítica `QUARANTINED`, `REJECTED`, `UNKNOWN` o `STALE` bloquea las conclusiones que dependan materialmente de ella. `ADMISSIBLE_WITH_LIMITATIONS` solo permite continuar si las limitaciones y su efecto están documentados.

### 4.4 Elegibilidad por finalidad — EligibilityStatus

Debe existir una evaluación independiente por finalidad y alcance.

| Estado | Significado |
|---|---|
| ELIGIBLE | Cumple los criterios de la finalidad |
| ELIGIBLE_WITH_LIMITATIONS | Cumple con restricciones explícitas |
| INELIGIBLE | Incumple un criterio de la finalidad |
| INSUFFICIENT_INFORMATION | No existe evidencia suficiente para decidir |
| QUARANTINED | Conflicto material que exige aislamiento/resolución antes de decidir |

Reglas:
- `INELIGIBLE` es una conclusión negativa sustentada respecto de una finalidad concreta.
- `INSUFFICIENT_INFORMATION` expresa falta de evidencia; no es sinónimo de `INELIGIBLE`.
- `QUARANTINED` expresa una condición de contención por conflicto/investigación; no es una conclusión final de inelegibilidad.
- La elegibilidad para RESEARCH no implica elegibilidad para VALUATION, COMPARISON, CUSTODY o EXECUTION.
- La elegibilidad de ejecución se determina en un dominio de ruta separado (sección 4.9).

### 4.5 Riesgo por dominio — RiskDomainStatus

Por cada dominio de riesgo y scope:

| Estado | Significado |
|---|---|
| ASSESSED_WITH_FINDINGS | Evaluado; existen hallazgos que deben conservarse |
| ASSESSED_NO_MATERIAL_FINDING | Evaluado con evidencia suficiente; no se identificó hallazgo material dentro del alcance |
| UNKNOWN | Evidencia insuficiente |
| NOT_ASSESSED | No evaluado |
| STALE | Evaluación fuera de vigencia |
| CONFLICTED | Evidencia material contradictoria |

`ASSESSED_NO_MATERIAL_FINDING` no significa riesgo cero. `NOT_ASSESSED`, `UNKNOWN`, `STALE` y `CONFLICTED` nunca se convierten en ausencia de hallazgos. La aplicabilidad del dominio se registra aparte; no usar `NOT_ASSESSED` para representar no aplicabilidad.

### 4.6 Decisión de veto — VetoDecision

Estados por dominio y alcance:

| Estado | Significado |
|---|---|
| VETO_ACTIVE | Condición crítica verificada y vigente |
| VETO_REVIEW_REQUIRED | Alerta creíble, contradicción o remediación pendiente de validar |
| NO_VETO_IDENTIFIED | No se identificó veto con evidencia suficiente en el alcance revisado |
| RISK_UNKNOWN | No puede determinarse si existe veto por falta de evidencia suficiente |

Precedencia agregada, de mayor a menor restricción:
1. `VETO_ACTIVE`
2. `VETO_REVIEW_REQUIRED`
3. `RISK_UNKNOWN`
4. `NO_VETO_IDENTIFIED`

Solo se agrega `NO_VETO_IDENTIFIED` cuando todos los dominios materiales aplicables están suficientemente evaluados y no hay veto ni revisión pendiente. Dominios no aplicables requieren justificación registrada. No se permite inferir ausencia de veto a partir de dominios no evaluados.

### 4.7 Aplicabilidad y resultado de valoración — ValuationStatus

Separar aplicabilidad del método y suficiencia de evidencia:

**Applicability:** `APPLICABLE`, `NOT_APPLICABLE`, `UNDETERMINED`.

**Valuation outcome:** `ESTIMABLE`, `ESTIMABLE_WITH_LIMITATIONS`, `NOT_ESTIMABLE`, `INSUFFICIENT_EVIDENCE`, `NOT_RUN`.

- `NOT_APPLICABLE`: existe justificación de que el método/objeto no aplica a esa tipología o derecho.
- `NOT_ESTIMABLE`: el método es aplicable, pero no puede producir una estimación defendible bajo las condiciones actuales.
- `INSUFFICIENT_EVIDENCE`: falta evidencia requerida; puede ser causa de `NOT_ESTIMABLE`, pero se conserva como diagnóstico específico.
- `UNDETERMINED`: todavía no se ha establecido aplicabilidad; no equivale a `NOT_APPLICABLE`.

No producir un rango numérico cuando el estado sea `NOT_ESTIMABLE`, `INSUFFICIENT_EVIDENCE` o `NOT_RUN`.

### 4.8 Decisión de selección/oportunidad — SelectionDecision

Resultado único por clave de decisión y versión, derivado de los dominios anteriores:

| Estado canónico | Significado |
|---|---|
| BLOCKED_IDENTITY | Identidad no resuelta, conflictiva u obsoleta |
| BLOCKED_ELIGIBILITY | Inelegible para la finalidad declarada |
| BLOCKED_EVIDENCE | Evidencia crítica no admisible, insuficiente u obsoleta |
| VETOED | Existe VETO_ACTIVE aplicable |
| REVIEW_REQUIRED | Existe VETO_REVIEW_REQUIRED u otra revisión bloqueante |
| RISK_INCOMPLETE | Riesgo material UNKNOWN, NOT_ASSESSED, STALE o CONFLICTED |
| VALUATION_NOT_APPLICABLE | No aplica valoración al objeto/finalidad; no es una oportunidad valorada |
| VALUATION_NOT_ESTIMABLE | Valoración aplicable pero no defendible actualmente |
| INSUFFICIENT_EVIDENCE | Evidencia insuficiente para una conclusión de selección |
| NOT_COMPARABLE | No se puede comparar válidamente con el universo/pares declarados |
| CONDITIONAL_CANDIDATE | Puede continuar con limitaciones/condiciones explícitas y sin bloqueos críticos |
| OPPORTUNITY_SUPPORTED | La evidencia respalda una oportunidad bajo modelo, horizonte, costes y alcance declarados |
| NO_OPPORTUNITY_SUPPORTED | La evaluación se completó, pero no respalda oportunidad bajo el protocolo |

La decisión registra siempre causa primaria y todas las causas/restricciones secundarias. La causa primaria se determina por la precedencia de la sección 5. `NO_OPPORTUNITY_SUPPORTED` no equivale a inelegibilidad ni a veto. `VALUATION_NOT_APPLICABLE` no debe confundirse con una valoración negativa.

### 4.9 Elegibilidad de ruta — RouteEligibility

Evaluación separada por venue, mercado, custodio, contraparte, jurisdicción, tamaño, lado y ruta:

| Estado | Significado |
|---|---|
| ROUTE_ELIGIBLE | Ruta evaluada y habilitada según criterios vigentes |
| ROUTE_ELIGIBLE_WITH_LIMITATIONS | Ruta habilitada con restricciones |
| ROUTE_INELIGIBLE | Ruta no cumple criterios |
| ROUTE_REVIEW_REQUIRED | Revisión pendiente |
| ROUTE_UNKNOWN | Evidencia insuficiente/no evaluada |
| ROUTE_STALE | Evaluación fuera de vigencia |

Este estado no cambia por sí solo la elegibilidad para investigar o valorar el activo. Tampoco autoriza una orden: mandato, presupuesto de riesgo, autorización, controles pre-trade y permisos operativos pertenecen a Portfolio & Execution.

### 4.10 Ranking — RankingDisposition

La política de ranking recibe decisiones vigentes y compatibles:

- `EXCLUDED_BLOCKED`: identidad, elegibilidad, evidencia o veto bloqueante.
- `SEPARATE_REVIEW`: revisión pendiente.
- `SEPARATE_INCOMPLETE`: riesgo o evidencia incompleta.
- `RESEARCH_ONLY`: investigación permitida, pero no candidato comparable/valorado.
- `NOT_COMPARABLE`: no pertenece a un conjunto homogéneo o no hay comparación válida.
- `RANKABLE_CONDITIONAL`: candidato condicional, solo en grupo separado y con limitaciones visibles.
- `RANKABLE_SUPPORTED`: oportunidad respaldada, comparable dentro del mismo universo, finalidad y horizonte.

Los grupos bloqueados, revisión e incompletos no se ordenan como oportunidades. No se calcula un score para compensar estados restrictivos. Ranking parcial y ausencia de dominancia son resultados válidos.

## 5. Precedencia y composición de decisión

La decisión agregada se calcula sin borrar estados de origen. Precedencia propuesta para determinar la causa primaria:

1. Identidad no resuelta/conflictiva/obsoleta → `BLOCKED_IDENTITY`.
2. Inelegibilidad explícita para la finalidad → `BLOCKED_ELIGIBILITY`.
3. Evidencia crítica inadmisible, en cuarentena, desconocida u obsoleta → `BLOCKED_EVIDENCE`.
4. `VETO_ACTIVE` aplicable → `VETOED`.
5. `VETO_REVIEW_REQUIRED` u otra revisión bloqueante → `REVIEW_REQUIRED`.
6. Riesgo material desconocido/no evaluado/obsoleto/conflictivo → `RISK_INCOMPLETE`.
7. Valoración aplicable no estimable o insuficiente → `VALUATION_NOT_ESTIMABLE` / `INSUFFICIENT_EVIDENCE`.
8. Incomparabilidad → `NOT_COMPARABLE`.
9. Valoración no aplicable → `VALUATION_NOT_APPLICABLE`, salvo que la finalidad no requiera valoración y exista otra decisión válida.
10. Limitaciones no bloqueantes → `CONDITIONAL_CANDIDATE`.
11. Evidencia respalda oportunidad → `OPPORTUNITY_SUPPORTED`.
12. Evaluación completa sin oportunidad respaldada → `NO_OPPORTUNITY_SUPPORTED`.

La precedencia ordena la causa primaria de presentación, pero deben registrarse todas las causas simultáneas. Un veto activo se mantiene visible incluso si una condición de mayor precedencia (por ejemplo, identidad conflictiva) determina la causa primaria. Ningún estado favorable puede emitirse si existe una condición bloqueante aplicable.

Los estados de valoración no deben bloquear una finalidad que explícitamente no requiera valoración; la aplicabilidad por finalidad debe estar declarada antes de ejecutar la composición.

## 6. Transiciones

### 6.1 Reglas generales

- Cada transición requiere evento, actor/proceso, timestamp UTC, evidencia, scope, motivo y versión de política.
- Las transiciones se registran como eventos append-only.
- No se permiten cambios directos de estado sin transición auditable.
- La reevaluación crea nuevo assessment/versionado; el anterior pasa a `SUPERSEDED` cuando corresponda.
- El vencimiento por TTL no transforma automáticamente un estado favorable en negativo: invalida su vigencia y exige reevaluación, representada por `STALE` o estado de lifecycle aplicable.
- Un cambio de finalidad/scope crea una evaluación distinta, no una transición artificial del mismo objeto.

### 6.2 Transiciones principales de veto

| Desde | Hacia | Condición mínima |
|---|---|---|
| NO_VETO_IDENTIFIED | VETO_REVIEW_REQUIRED | Nueva alerta creíble o evidencia contradictoria |
| RISK_UNKNOWN | VETO_REVIEW_REQUIRED | Señal material que requiere investigación |
| VETO_REVIEW_REQUIRED | VETO_ACTIVE | Condición crítica verificada y vigente |
| VETO_REVIEW_REQUIRED | NO_VETO_IDENTIFIED | Investigación documentada descarta la alerta en el scope |
| VETO_ACTIVE | VETO_REVIEW_REQUIRED | Evidencia inicial de remediación aún no validada |
| VETO_ACTIVE | NO_VETO_IDENTIFIED | Remediación y ausencia de condición bloqueante verificadas |
| Cualquier estado | RISK_UNKNOWN | Evidencia deja de ser suficiente para sostener el assessment |

No se permite retirar veto por paso del tiempo, anuncio del proyecto o ausencia de incidentes nuevos.

### 6.3 Otras transiciones

- `UNRESOLVED` → `RESOLVED` solo con evidencia de identidad suficiente y reconciliada.
- `CONFLICTED` → `RESOLVED` solo después de documentar resolución de conflicto y fuentes descartadas.
- `QUARANTINED` → `ADMISSIBLE` o `REJECTED` requiere investigación documentada; no hay promoción automática.
- `INSUFFICIENT_INFORMATION` → `ELIGIBLE` requiere evidencia nueva suficiente; no basta con cambiar la etiqueta.
- `NOT_ESTIMABLE` → `ESTIMABLE` requiere método/evidencia nueva y revisión de supuestos.
- `REVIEW_REQUIRED` → estado no bloqueante requiere cierre documentado de la revisión.
- `INVALIDATED` no se revierte en el mismo registro; se crea una evaluación nueva.

## 7. Reglas de abstención

Abstenerse de una conclusión favorable, cuantificada o comparable cuando un estado desconocido, conflictivo, inválido, obsoleto o no evaluado sea material para esa conclusión. La abstención debe indicar:

- qué conclusión se bloquea;
- qué estados/causas lo producen y su alcance;
- qué sí puede afirmarse;
- qué evidencia falta o está en conflicto;
- qué evento/evidencia permitiría reanudar;
- fecha/condición de reevaluación.

Abstención no es un estado sustituto universal. Se representa mediante la decisión específica (por ejemplo, `RISK_INCOMPLETE`, `BLOCKED_EVIDENCE` o `VALUATION_NOT_ESTIMABLE`) y un atributo explícito `abstention=true` para la conclusión afectada, acompañado de razones.

## 8. Tabla de mapeo de vocabularios existentes

El mapeo es semántico y contextual, no un reemplazo mecánico de cadenas.

| Estado existente | Dominio destino | Tratamiento canónico |
|---|---|---|
| RESOLVED / UNRESOLVED / CONFLICTED | IdentityStatus | Mantener; añadir STALE |
| ADMISSIBLE / ADMISSIBLE_WITH_LIMITATIONS / QUARANTINED / REJECTED / UNKNOWN / STALE | EvidenceStatus | Mantener; añadir NOT_ASSESSED |
| ELIGIBLE | EligibilityStatus | ELIGIBLE por finalidad |
| ELIGIBLE_WITH_LIMITATIONS | EligibilityStatus | ELIGIBLE_WITH_LIMITATIONS |
| INELIGIBLE / INELIGIBLE_FOR_SCOPE | EligibilityStatus | INELIGIBLE por finalidad y scope |
| CONDITIONAL | EligibilityStatus | Migrar a ELIGIBLE_WITH_LIMITATIONS solo si condiciones explícitas; de lo contrario INSUFFICIENT_INFORMATION |
| INSUFFICIENT_INFORMATION | EligibilityStatus | Mantener como estado de insuficiencia |
| QUARANTINED (screening) | EligibilityStatus o EvidenceStatus | Determinar si el conflicto afecta decisión de elegibilidad o admisibilidad; conservar origen |
| VETO_ACTIVE | VetoDecision | Mantener |
| VETO_REVIEW_REQUIRED | VetoDecision | Mantener |
| RISK_UNKNOWN | VetoDecision / RiskDomainStatus | Duplicar referencias en ambos dominios solo si ambos assessments lo justifican; no equiparar |
| NO_VETO_IDENTIFIED | VetoDecision | Mantener con cobertura de dominios materiales demostrada |
| ESTIMABLE / CONDITIONAL | ValuationStatus | Separar outcome y limitaciones |
| NOT_APPLICABLE | Applicability | Mantener como aplicabilidad, no outcome |
| INSUFFICIENT_EVIDENCE | EvidenceStatus / SelectionDecision / ValuationStatus | Resolver por dominio; no migrar sin contexto |
| NOT_ESTIMABLE | ValuationStatus | Mantener |
| NOT_ELIGIBLE_DUE_TO_ACTIVE_VETO | SelectionDecision | VETOED |
| REVIEW_REQUIRED | SelectionDecision | Mantener; registrar causa concreta |
| RISK_ASSESSMENT_INCOMPLETE | SelectionDecision | RISK_INCOMPLETE |
| BLOCKED_IDENTITY | SelectionDecision | Mantener |
| INELIGIBLE_FOR_SCOPE | SelectionDecision | BLOCKED_ELIGIBILITY si es salida agregada; conservar estado fuente de elegibilidad |
| BLOCKED_DATA_QUALITY | SelectionDecision | BLOCKED_EVIDENCE |
| NOT_VALUABLE_WITH_AVAILABLE_EVIDENCE | SelectionDecision | VALUATION_NOT_ESTIMABLE, si realmente describe valoración |
| NO_COMPARABLE_OPPORTUNITY | SelectionDecision | NOT_COMPARABLE |
| CONDITIONAL_CANDIDATE | SelectionDecision | Mantener |
| OPPORTUNITY_SUPPORTED_BY_EVIDENCE | SelectionDecision | OPPORTUNITY_SUPPORTED |
| NO_OPPORTUNITY_SUPPORTED | SelectionDecision | Mantener |
| INSUFFICIENT_EVIDENCE (decisión) | SelectionDecision | Mantener con causa y scope |
| VETOED / INELIGIBLE / ABSTAIN / OPPORTUNITY_SUPPORTED (metodología maestra) | SelectionDecision | Sustituir solo en versión futura por estado específico y mapping explícito |
| QUARANTINED / INSUFFICIENT_INFORMATION (screening) | EligibilityStatus/EvidenceStatus | No asumir equivalencia; mapear por causa |
| EXCLUDED / REVIEW / INCOMPLETE / RESEARCH / CONDITIONAL / SUPPORTED (grupos de ranking) | RankingDisposition | Mapear según decisión fuente; no convertir en score |

## 9. Compatibilidad, versionado y migración

1. Mantener los vocabularios actuales en sus documentos hasta que el modelo sea revisado y aprobado.
2. Al aprobar, publicar versión mayor de los contratos afectados si cambia semántica, obligatoriedad o estructura; cambios aditivos compatibles podrán seguir política de versionado acordada.
3. Todo registro migrado conserva estado original, documento/versión de origen, fecha, regla de mapeo y confianza de la migración.
4. Estados ambiguos (por ejemplo `CONDITIONAL`, `INSUFFICIENT_EVIDENCE` sin dominio o `QUARANTINED` sin causa) no se migran automáticamente: requieren revisión o se conservan como legacy no interpretable.
5. Consumidores deben rechazar estados desconocidos de versión incompatible; nunca convertirlos en favorable por defecto.
6. Durante transición, productores pueden emitir formato anterior y nuevo solo si existe mapping determinista y se conserva el payload fuente. No mezclar semánticas en el mismo campo.
7. No modificar el registro normativo IIM ni schemas aprobados sin aprobación explícita y proceso de cambio.

## 10. Requisitos mínimos de registro

Cada assessment/decision debe conservar:
- identificador y tipo de assessment;
- entidad y scope;
- dominio y estado;
- estado anterior y nuevo cuando haya transición;
- timestamp de evaluación y de evidencia;
- fuente, provenance, manifest/hash y freshness;
- política/metodología/schema version;
- razón codificada y explicación;
- limitaciones, desconocidos y contraevidencia;
- referencias a assessments dependientes;
- actor/proceso y revisión/aprobación cuando aplique;
- vigencia/TTL o evento invalidante;
- decisión derivada y causa primaria/secundarias, si corresponde.

## 11. Criterios de aceptación para revisión

M-02 solo podrá marcarse como resuelto después de:

- revisión de consistencia con metodología maestra, gates, veto, screening y ranking;
- validación de que cada estado pertenece a un dominio y scope inequívocos;
- revisión de precedencia con casos simultáneos y casos sin valoración aplicable;
- revisión de transiciones, terminalidad, invalidación, TTL y reevaluación;
- mapeo aprobado de cada estado legacy, con casos ambiguos explícitamente no migrables;
- ejemplos positivos y negativos para UNKNOWN, STALE, CONFLICTED, QUARANTINED, NOT_APPLICABLE, NOT_ESTIMABLE y VETO_REVIEW_REQUIRED;
- aprobación de propietarios de INTELLIGENCE, DECISION, RISK y PORTFOLIO & EXECUTION;
- posterior traducción a contratos y pruebas de conformidad.

## 12. Casos de aceptación ilustrativos

| Caso | Estados fuente | Resultado esperado |
|---|---|---|
| Identidad conflictiva y oportunidad modelada favorable | Identity=CONFLICTED; valuation=ESTIMABLE | BLOCKED_IDENTITY; no ranking favorable |
| Veto activo y score superior | Veto=VETO_ACTIVE; score alto | VETOED; score no compensa |
| Riesgo no evaluado | Risk=NOT_ASSESSED | RISK_INCOMPLETE, no NO_VETO_IDENTIFIED |
| Alerta no corroborada | Veto=VETO_REVIEW_REQUIRED | REVIEW_REQUIRED; no oportunidad favorable |
| Datos críticos obsoletos | Evidence=STALE | BLOCKED_EVIDENCE si son materiales |
| Conflicto de screening | Eligibility=QUARANTINED | Cuarentena; no convertir automáticamente en INELIGIBLE |
| Falta de información | Eligibility=INSUFFICIENT_INFORMATION | No elegible confirmado ni elegible |
| Método no aplica | Applicability=NOT_APPLICABLE | VALUATION_NOT_APPLICABLE; no NOT_ESTIMABLE |
| Método aplica, pero no hay datos | Applicability=APPLICABLE; valuation=INSUFFICIENT_EVIDENCE | VALUATION_NOT_ESTIMABLE/INSUFFICIENT_EVIDENCE según causa primaria |
| Ruta venue no disponible, investigación válida | Research=ELIGIBLE; Route=ROUTE_INELIGIBLE | Investigación permitida; ejecución bloqueada en esa ruta |
| Oportunidad respaldada, pero no comparable | Selection=OPPORTUNITY_SUPPORTED; comparability=false | NOT_COMPARABLE para ese ranking, sin borrar tesis |
| Datos nuevos invalidan assessment | Lifecycle=COMPLETE; invalidating event | Registro previo INVALIDATED; crear reevaluación |

## 13. Estado de esta especificación

**DRAFT / REVIEW_REQUIRED.** Este documento propone una semántica integrada y una migración, pero no sustituye ni modifica todavía las políticas de dominio, contratos JSON Schema o registro de requisitos. No se declara M-02 cerrado, implementado, probado ni validado empíricamente.

## Referencias

- `docs/methodology/01-global-crypto-asset-selection-methodology.md`
- `docs/methodology/02-methodology-consistency-audit.md`
- `docs/decision/decision-gates-and-outcomes.md`
- `docs/decision/ranking-and-dominance-policy.md`
- `docs/risk/veto-policy.md`
- `docs/asset-selection/eligibility-and-screening.md`
- `docs/traceability/methodology-master-traceability-matrix.md`
