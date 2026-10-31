# Modelo formal de RiskAssessment, RiskDomainStatus y VetoDecision

**ID:** IIE-METH-RISK-003  
**Versión:** 0.1.0-draft  
**Estado:** DRAFT / REVIEW_REQUIRED / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED  
**Fecha:** 2026-10-31  
**Hallazgo:** M-04 (HIGH), auditoría `docs/methodology/02-methodology-consistency-audit.md`

## 1. Propósito

Separar tres objetos que responden preguntas diferentes:

1. **RiskAssessment:** expediente versionado que coordina evaluaciones de riesgo dentro de un scope y finalidad definidos.
2. **RiskDomainStatus:** estado de evaluación de cada dominio material, sin ocultar desconocimiento, aplicabilidad, hallazgos o frescura.
3. **VetoDecision:** determinación explícita de si una condición crítica bloquea una finalidad, actividad, ruta o alcance concreto.

DECISION consume estos resultados y aplica la política de gates; no vuelve a evaluar evidencia de riesgo, no recalcula severidades y no puede rebajar un veto.

Este documento es una propuesta metodológica. No modifica el registro normativo IIM ni convierte los contratos existentes en contratos aprobados.

## 2. Principios de diseño

- **Separación semántica:** evaluación de riesgo, estado del dominio y decisión de veto no son sinónimos ni deben compartir un enum.
- **No scalarización destructiva:** una puntuación global no reemplaza el perfil por dominios, los hallazgos, la incertidumbre o los vetos.
- **Desconocido explícito:** UNKNOWN, NOT_ASSESSED, STALE, INVALID y CONFLICTED no significan riesgo bajo, ausencia de hallazgo ni PASS.
- **Veto no compensable:** ningún score, descuento, upside, liquidez, ranking o diversificación neutraliza un veto aplicable.
- **Scope obligatorio:** cada assessment y veto identifica objeto, finalidad, actividad y contexto relevantes.
- **Evidencia reproducible:** toda conclusión enlaza fuentes, timestamps, procedencia, calidad, versión de política y razonamiento.
- **Independencia de decisión:** DECISION proyecta el resultado; no cambia el juicio especializado ni la evidencia fuente.
- **Historial inmutable:** correcciones y reevaluaciones crean nuevas versiones relacionadas.
- **Fail-closed:** si un control crítico obligatorio no puede evaluarse, la acción dependiente no recibe PASS.

## 3. Modelo de objetos y relaciones

### 3.1 RiskAssessment

Expediente que agrupa los resultados de riesgo para un activo/representación y un scope determinados. No es un número único ni una conclusión de inversión.

Un RiskAssessment contiene:
- metadatos de scope y finalidad;
- catálogo de dominios aplicables y justificación de no aplicabilidad;
- referencias a uno o más RiskDomainAssessment;
- hallazgos, escenarios, severidad, exposición e incertidumbre;
- referencias a VetoDecision;
- cobertura, limitaciones, contraevidencia y asuntos abiertos;
- estado de completitud, frescura y revisión del expediente.

### 3.2 RiskDomainAssessment y RiskDomainStatus

El assessment es el registro detallado de un dominio; el status es su estado de proceso/evidencia. El status no sustituye la evaluación de severidad ni el contenido de hallazgos.

Dominios iniciales propuestos, ajustables mediante taxonomía versionada:
- protocol_and_smart_contract;
- cybersecurity_and_key_management;
- consensus_and_network;
- economic_and_tokenomics;
- market_and_liquidity;
- financial_and_counterparty;
- custody_and_operational;
- governance_and_control;
- legal_and_regulatory;
- data_integrity_and_market_abuse;
- bridge_and_interoperability, cuando aplique;
- environmental_or_other_material_domain, cuando la tesis lo requiera.

No todos los dominios aplican a todos los activos. La aplicabilidad se determina antes de evaluar y se registra explícitamente.

### 3.3 VetoDecision

Registro separado que aplica una regla de veto a una condición y scope. Debe identificar la regla/política que lo origina, evidencia mínima, condición observada, autoridad/rol de revisión, alcance bloqueado y condición de levantamiento.

Una alerta no corroborada no se representa como veto confirmado. Un veto activo no se reduce a una etiqueta de riesgo alto: es una decisión de control con consecuencia explícita.

### 3.4 Relaciones

- Un RiskAssessment contiene N RiskDomainAssessment.
- Cada RiskDomainAssessment pertenece a un RiskAssessment y una versión de taxonomía.
- Un RiskDomainAssessment puede originar cero o más VetoDecision.
- Una VetoDecision puede referenciar varios dominios/evidencias.
- DECISION consume RiskAssessment y VetoDecision mediante IDs y versiones, no copiando o recalculando sus juicios.
- ORE puede consumir los dominios pertinentes al route scope; no convierte automáticamente un veto analítico en bloqueo universal de todas las actividades.

## 4. Scope obligatorio

Cada objeto declara, según aplicabilidad:
- canonical_asset_id y representation_id (chain, contract, wrapper);
- market_id, venue_id, counterparty_id y custodian_id;
- purpose y activity;
- jurisdiction_scope;
- portfolio/account scope cuando aplique;
- size/notional, side, instrument y environment para ruta operativa;
- horizon y timestamp de corte;
- universe_snapshot_id y policy/taxonomy/methodology versions.

Un veto debe bloquear exactamente el alcance demostrado. Si el alcance no puede delimitarse de forma fiable y existe riesgo material, se aplica revisión/bloqueo provisional al conjunto mínimo seguro hasta resolverlo; no se generaliza indefinidamente sin evidencia.

## 5. RiskAssessment: ciclo de vida y estados

Estados de expediente:
- `NOT_STARTED`: expediente creado sin evaluación.
- `IN_PROGRESS`: evaluaciones de dominio en curso.
- `COMPLETE`: todos los dominios requeridos para el scope han concluido su proceso; puede incluir UNKNOWN, hallazgos o limitaciones.
- `REVIEW_REQUIRED`: revisión especializada/independiente pendiente.
- `SUPERSEDED`: reemplazado por una versión posterior.
- `INVALIDATED`: evento o error invalidó su uso actual.

**COMPLETE no significa riesgo bajo, ausencia de veto ni aprobación.** Completitud y resultado son ejes distintos. Un expediente COMPLETE puede contener dominios UNKNOWN cuando la limitación está declarada; no puede producir un resultado favorable que dependa de ellos.

Atributos resumen que no son estados:
- coverage_ratio por dominio/cobertura, con denominador explícito;
- findings_count por severidad y estado;
- unresolved_critical_items;
- overall_uncertainty;
- assessment_confidence, solo si hay método calibrado y definido;
- aggregate_risk_rating, únicamente si existe metodología aprobada y validada, nunca como sustituto de dominios ni vetos.

No definir un promedio simple de riesgo como resultado canónico. Cualquier agregación futura requiere justificación, pesos, tratamiento de correlación, incertidumbre, datos faltantes, sensibilidad y validación.

## 6. RiskDomainStatus

Estados propuestos, por dominio y scope:

| Estado | Definición | Interpretación |
|---|---|---|
| NOT_ASSESSED | No se inició evaluación suficiente | No hay conclusión |
| ASSESSMENT_IN_PROGRESS | Trabajo activo | No usar como resultado final |
| ASSESSED_WITH_FINDINGS | Evaluación completada con uno o más hallazgos | Interpretar severidad/exposición por separado |
| ASSESSED_NO_MATERIAL_FINDING | Evaluación suficiente sin hallazgo material identificado en el scope | No significa riesgo cero |
| UNKNOWN | Evidencia insuficiente para concluir | No favorable |
| STALE | Evaluación fuera de la vigencia definida | No usar como estado actual |
| CONFLICTED | Evidencia material incompatible no resuelta | Revisión requerida |
| INVALID | Assessment inválido por error de método, datos o integridad | No consumir |

La aplicabilidad se registra en un objeto/atributo separado:
- `APPLICABLE`
- `NOT_APPLICABLE`
- `APPLICABILITY_UNRESOLVED`
- `APPLICABILITY_CONFLICTED`

Si un dominio es NOT_APPLICABLE, debe incluir regla taxonómica, justificación y versión. No usar NOT_APPLICABLE para representar datos faltantes, dificultad de análisis o resultado favorable.

### 6.1 Hallazgos de riesgo

Cada finding debe declarar:
- finding_id, domain, rule_id y scope;
- condición observada y descripción factual separada de inferencias;
- severity y rationale;
- likelihood/probability solo si hay método defendible;
- exposure/impact y horizonte;
- evidence_refs y contraevidencia;
- confidence/uncertainty con semántica definida;
- first_observed_at, last_verified_at y status;
- remediation_requirements y closure_criteria;
- owner/reviewer y version.

No asignar probabilidad numérica sin calibración. No equiparar severidad, probabilidad, exposición y confianza.

## 7. VetoDecision

### 7.1 Estados

- `VETO_ACTIVE`: condición de veto verificada y vigente.
- `VETO_REVIEW_REQUIRED`: alerta creíble, evidencia contradictoria, incidente en investigación o remediación no validada.
- `NO_VETO_IDENTIFIED`: no se identificó veto en el scope revisado con cobertura suficiente.
- `VETO_NOT_ASSESSED`: no se evaluó la política de veto para ese scope.

**RISK_UNKNOWN pertenece a RiskDomainStatus, no al enum de VetoDecision.** Cuando la falta de evidencia impide determinar si se cumple una regla de veto, el dominio se marca UNKNOWN/NOT_ASSESSED y la decisión de veto no puede ser NO_VETO_IDENTIFIED: debe permanecer VETO_NOT_ASSESSED o pasar a VETO_REVIEW_REQUIRED si existe una señal concreta. DECISION debe bloquear una conclusión favorable que dependa de ese riesgo.

### 7.2 Campos obligatorios

- veto_decision_id, rule_id, policy_version;
- subject_id y scope estructurado;
- status y consequence;
- trigger/finding references y evidence manifest;
- rationale, counterevidence y unresolved questions;
- effective_at, first_observed_at, last_verified_at;
- review_due_at (no es expiración automática);
- closure_criteria y remediation_evidence_refs;
- decision_actor, reviewer, approval/separation-of-duties metadata;
- supersedes_id y transition_event_id.

### 7.3 Catálogo de reglas de veto

Cada regla debe definir antes de producción:
- propósito y tipo de riesgo;
- sujetos y actividades cubiertos;
- criterios verificables y evidencia mínima;
- severidad/condición que activa;
- alcance por defecto y cómo limitarlo;
- consecuencia exacta (bloquear research conclusion, valuation, comparison, trade, deposit, withdrawal, custody, etc.);
- posibilidad o imposibilidad de excepción;
- revisión, remediación y cierre;
- autoridad responsable y fuente de política.

La lista actual de condiciones candidatas en `docs/risk/veto-policy.md` es punto de partida, no catálogo exhaustivo ni automáticamente aprobado.

## 8. Precedencia y agregación

### 8.1 Agregación por dominio

No promediar estados. Para cada dominio, conservar aplicabilidad, status, findings y evidencia. El resumen del RiskAssessment muestra cobertura y dominios críticos desconocidos; no borra detalles.

### 8.2 Agregación de veto

Para un mismo scope y conjunto de reglas:
1. VETO_ACTIVE prevalece.
2. En ausencia de veto activo, VETO_REVIEW_REQUIRED prevalece.
3. En ausencia de ambos, si alguna regla obligatoria no se evaluó, el resultado es VETO_NOT_ASSESSED.
4. NO_VETO_IDENTIFIED solo cuando todas las reglas aplicables y dominios materiales requeridos fueron revisados con evidencia suficiente.

Una decisión por scope más amplio no se deriva de una más estrecha, ni viceversa, sin regla de propagación explícita. Un veto sobre retiro no bloquea automáticamente investigación; un veto sobre contrato específico no se generaliza a otra representación sin evidencia.

### 8.3 Múltiples bloqueos

Preservar todas las decisiones activas y revisiones pendientes. Puede existir una causa primaria para presentación, pero no eliminar causas secundarias ni sus owners. La resolución de una causa no libera el scope si persisten otros bloqueos.

## 9. Transiciones y levantamiento de veto

Transiciones permitidas:
- VETO_NOT_ASSESSED → VETO_REVIEW_REQUIRED: aparece alerta o indicio material que exige investigación.
- VETO_NOT_ASSESSED → NO_VETO_IDENTIFIED: evaluación completa y evidencia suficiente para el scope.
- NO_VETO_IDENTIFIED → VETO_REVIEW_REQUIRED: aparece alerta creíble o contraevidencia material.
- VETO_REVIEW_REQUIRED → VETO_ACTIVE: condición crítica verificada.
- VETO_REVIEW_REQUIRED → NO_VETO_IDENTIFIED: investigación documentada descarta la condición dentro del scope.
- VETO_ACTIVE → VETO_REVIEW_REQUIRED: remediación alegada o evidencia nueva requiere reevaluación; el bloqueo permanece durante la revisión.
- VETO_ACTIVE → NO_VETO_IDENTIFIED: remediación verificada, criterios de cierre satisfechos y revisión independiente cuando el riesgo lo exige.

No se permite transición directa de VETO_ACTIVE a NO_VETO_IDENTIFIED sin evidencia de cierre y evaluación nueva. El tiempo, un anuncio del emisor, auditoría genérica o ausencia de incidentes reportados no constituyen remediación suficiente.

Los cambios de RiskDomainStatus (por ejemplo UNKNOWN a ASSESSED_WITH_FINDINGS) no eliminan por sí solos una VetoDecision. La política de veto debe reevaluarse explícitamente.

## 10. Interfaz con DECISION y ORE

### 10.1 Responsabilidad de Risk

Risk produce:
- RiskAssessment versionado;
- RiskDomainAssessment y status por dominio;
- findings y evidence references;
- VetoDecision por regla y scope;
- limitaciones, incertidumbre y fechas de vigencia.

### 10.2 Responsabilidad de DECISION

DECISION:
- verifica identidad de IDs, scopes, versiones y vigencia;
- consume los estados y decisiones publicados;
- aplica gates y precedencia de decisión aprobados;
- registra qué estados fuente originaron la salida;
- se abstiene o bloquea cuando faltan assessments requeridos;
- no cambia severity, likelihood, finding status o veto status;
- no convierte UNKNOWN en bajo riesgo ni VETO_REVIEW_REQUIRED en NO_VETO_IDENTIFIED.

Si existe discrepancia entre RiskAssessment y VetoDecision, DECISION no resuelve el conflicto por heurística: emite REVIEW_REQUIRED y solicita reconciliación al propietario de Risk.

### 10.3 Responsabilidad de ORE

ORE consume solo los dominios relevantes para actividad/ruta: venue, contraparte, seguridad, custodia, liquidez, red, instrumento, jurisdicción y salud operacional. Una VetoDecision debe llevar scope/actividad para determinar si afecta esa ruta.

Un veto analítico puede bloquear la tesis o candidatura sin necesariamente bloquear MARKET_DATA o RESEARCH. Un veto de custodia puede bloquear CUSTODY/TRADE/DEPOSIT/WITHDRAWAL en la ruta concreta. La propagación se define por regla, no por inferencia genérica.

## 11. Freshness, invalidación y reevaluación

- TTL se define por dominio, fuente, riesgo y finalidad; no hay TTL universal.
- Registrar observed_at, evaluated_at, last_verified_at, valid_until/TTL, fuente, snapshot y versión.
- Un evento material puede invalidar una evaluación antes del TTL.
- La expiración de una evaluación no borra un veto activo ni lo levanta: marca el assessment como STALE y exige reevaluación; mientras no se resuelva, el bloqueo no se relaja.
- Cambios de contrato, red, custodia, tokenomics, gobernanza, jurisdicción, venue, contraparte, exposición o tamaño invalidan los dominios afectados conforme a un dependency map.
- Reevaluar genera IDs nuevos y enlaces supersedes/superseded; conservar snapshots históricos.

## 12. Segregación de funciones y gobernanza

Roles conceptuales:
- Risk Analyst/Assessor: produce evaluación y evidencia.
- Domain Specialist: valida hallazgos técnicos/regulatorios específicos.
- Veto Approver: confirma veto según autoridad delegada.
- Independent Reviewer: revisa decisiones críticas, levantamiento de veto y excepciones.
- Decision Engine: consume resultados, sin poder modificarlos.
- Portfolio/Execution Control: aplica bloqueos operativos, sin editar la evaluación fuente.

En casos de riesgo material, quien propone el levantamiento no debe ser el único que lo aprueba. Las excepciones requieren autoridad, alcance, razón, duración, controles compensatorios y registro; nunca dispensan vetos legalmente no dispensables ni reglas definidas como no excepcionables.

## 13. Registro de auditoría mínimo

Cada creación/cambio de assessment o veto registra:
- actor/servicio y rol;
- acción y transition_event_id;
- IDs de objetos, scope y versiones;
- estado anterior/nuevo;
- evidencia y hashes/manifiestos;
- timestamps UTC;
- rationale y reason codes;
- aprobación/revisión y segregación;
- correlación con evaluación, decisión, cartera o ruta;
- resultado de controles de integridad.

Los logs no deben almacenar secretos, claves privadas, credenciales ni datos personales innecesarios. Las políticas de retención, acceso y redacción deben definirse separadamente.

## 14. Casos de aceptación

| Caso | RiskDomainStatus | VetoDecision | Resultado DECISION/ORE |
|---|---|---|---|
| Dominio material no investigado | NOT_ASSESSED | VETO_NOT_ASSESSED | No emitir conclusión favorable dependiente |
| Evidencia insuficiente | UNKNOWN | VETO_NOT_ASSESSED o REVIEW_REQUIRED si hay señal | No inferir riesgo bajo |
| Alerta no corroborada | CONFLICTED/ASSESSMENT_IN_PROGRESS | VETO_REVIEW_REQUIRED | Suspender conclusión favorable en scope |
| Exploit crítico confirmado | ASSESSED_WITH_FINDINGS | VETO_ACTIVE | Bloquear scope definido, no compensable |
| Evaluación suficiente sin hallazgo material | ASSESSED_NO_MATERIAL_FINDING | NO_VETO_IDENTIFIED | Continuar gates; no declarar riesgo cero |
| Dominio no aplicable con taxonomía justificada | No se evalúa riesgo | Regla de veto no aplicable | Conservar justificación; no contar como cobertura evaluada |
| Veto activo y score excelente | Cualquier estado | VETO_ACTIVE | Bloqueo prevalece |
| Remediación anunciada pero no verificada | ASSESSED_WITH_FINDINGS/UNKNOWN | VETO_REVIEW_REQUIRED o VETO_ACTIVE según evidencia | No levantar veto |
| Remediación verificada por revisión independiente | Nuevo assessment | Nueva VetoDecision NO_VETO_IDENTIFIED | Reabrir solo gates afectados |
| Riesgo de custodia en una ruta | Custody finding | Veto scoped a CUSTODY/TRADE | No generalizar a research sin regla |
| DECISION recibe estados contradictorios | CONFLICTED | Inconsistente | REVIEW_REQUIRED; no resolver por score |
| Assessment expirado con veto activo previo | STALE | VETO_ACTIVE se conserva hasta reevaluación/cierre | No levantar por TTL |

## 15. Criterios de aceptación para M-04

- [ ] RiskAssessment, RiskDomainAssessment/Status y VetoDecision son objetos separados.
- [ ] Completitud del expediente no implica evaluación favorable.
- [ ] Aplicabilidad se separa de evaluación y de suficiencia de evidencia.
- [ ] UNKNOWN, NOT_ASSESSED, STALE, INVALID y CONFLICTED no son PASS.
- [ ] Hallazgos separan severidad, probabilidad, exposición, confianza e incertidumbre.
- [ ] Veto tiene regla, evidencia, scope, consecuencia, vigencia y cierre.
- [ ] NO_VETO_IDENTIFIED exige cobertura suficiente de todos los dominios/reglas materiales.
- [ ] DECISION consume resultados sin recalcularlos ni degradarlos.
- [ ] ORE propaga vetos por scope/actividad, sin contaminación indebida entre finalidades.
- [ ] Levantamiento de veto requiere evidencia nueva, transición auditable y revisión apropiada.
- [ ] Se definen TTL, eventos invalidantes, segregación y audit trail.
- [ ] Contratos, validadores y pruebas se desarrollan después de revisión y aprobación metodológica.

## 16. Relación con documentos existentes

- `docs/risk/veto-policy.md`
- `docs/decision/decision-gates-and-outcomes.md`
- `docs/methodology/02-methodology-consistency-audit.md`
- `docs/methodology/03-canonical-decision-state-model.md`
- `docs/methodology/04-analytical-vs-operational-eligibility.md`
- `docs/architecture/portfolio-execution-integrated-requirements-draft.md`

## 17. Estado y limitaciones

**DRAFT / REVIEW_REQUIRED / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED.** Este documento propone un modelo semántico y de gobernanza. No es un contrato JSON Schema, motor de riesgo implementado, catálogo de vetos aprobado, dictamen legal ni validación empírica. M-04 permanece abierto hasta revisión y aceptación por responsables de riesgo, arquitectura, operaciones y compliance según jurisdicción.
