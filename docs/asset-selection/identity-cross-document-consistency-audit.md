# Auditoría de consistencia cruzada: identidad, admisibilidad, scopes y lifecycle

**ID:** IIE-IDENTITY-CROSS-AUDIT-001  
**Versión:** 0.1.0-draft  
**Fecha:** 2026-10-31  
**Estado:** REVIEW_REQUIRED / NOT_APPROVED  
**Alcance:** revisión documental estática de la propuesta de grafo de identidad y admisibilidad por finalidad frente al modelo canónico de estados, Scope Algebra, semántica FSA-01 y lifecycle/freshness.  
**Documentos revisados:**
- `docs/asset-selection/identity-graph-and-purpose-admissibility-proposal.md`
- `docs/methodology/03-canonical-decision-state-model.md`
- `docs/methodology/19-x09-scope-algebra-and-atomic-partition.md`
- `docs/methodology/22-x09-scope-algebra-membership-uncertainty-semantics.md`
- `docs/methodology/12-lifecycle-freshness-and-invalidation-framework.md`

## 1. Dictamen ejecutivo

La propuesta de identidad está **bien orientada en su separación de entidad, relación, claim y evaluación por finalidad**, y no se encontró una contradicción que obligue a descartarla. Sin embargo, todavía no está lista para schema freeze ni implementación.

Se identifican **8 hallazgos**: 3 HIGH, 4 MEDIUM y 1 LOW. Los principales riesgos son:
1. Vocabularios de identidad y evidencia no alineados de forma explícita con los dominios canónicos.
2. Scope de PurposeAssessment descrito como lista de campos, pero no ligado formalmente a ScopeDefinition/Scope Algebra.
3. Falta de contrato de evaluación de pertenencia y propagación UNKNOWN/INVALID desde claims y relaciones hacia el scope de finalidad.
4. Semántica temporal parcialmente alineada, pero sin contrato inequívoco de bitemporalidad, point-in-time y disponibilidad.
5. Falta de integración explícita del grafo de dependencias y de la invalidación lifecycle.
6. Falta de una interfaz formal entre PurposeAssessment y los consumidores ANALYTICAL_SELECTION / OPERATIONAL_ROUTE.

La recomendación es **INTEGRATION_REVISE**: conservar la arquitectura de cuatro objetos, corregir estos puntos en una propuesta de integración, y someterla a revisión antes de crear schemas o código.

Esta auditoría es documental. No se ejecutaron tests, validación matemática, consultas on-chain ni pruebas empíricas.

## 2. Hallazgos

### IDCA-01 — Vocabulario de identidad no interoperable con IdentityStatus canónico
**Severidad:** HIGH  
**Evidencia:** La propuesta define `RESOLVED`, `RESOLVED_WITH_LIMITATIONS`, `AMBIGUOUS`, `UNVERIFIED`, `CONFLICTED`, `RETIRED_OR_MIGRATED`. El modelo canónico utiliza `RESOLVED`, `UNRESOLVED`, `CONFLICTED`, `STALE` y estados lifecycle como `INVALIDATED`/`SUPERSEDED`.  
**Riesgo:** Dos módulos pueden interpretar de manera diferente AMBIGUOUS, UNVERIFIED y RESOLVED_WITH_LIMITATIONS. RETIRED_OR_MIGRATED mezcla condición de identidad con transición histórica.  
**Recomendación:** Mantener un estado de resolución de identidad canónico y dimensiones separadas para limitación, freshness y lifecycle. AMBIGUOUS debe mapear como mínimo a UNRESOLVED (con reason code AMBIGUOUS); UNVERIFIED debe distinguir falta de evaluación de evaluación inconclusa; RESOLVED_WITH_LIMITATIONS no debe habilitar consumidores sin evaluar qué limitación afecta a qué claim/purpose. RETIRED/MIGRATED debe expresarse mediante evento/relación de sucesión y estado de lifecycle, no como estado de resolución equivalente a RESOLVED.  
**Criterio de cierre:** Tabla normativa de mapping sin pérdida semántica, ejemplos negativos y aprobación de owner de identidad y Architecture.

### IDCA-02 — EvidenceClaimStatus se confunde potencialmente con EvidenceStatus / admissibility
**Severidad:** HIGH  
**Evidencia:** La propuesta usa CONFIRMED, PARTIALLY_CONFIRMED, DISPUTED, UNVERIFIED, REJECTED, RETRACTED para claims. El modelo canónico separa EvidenceStatus (ADMISSIBLE, ADMISSIBLE_WITH_LIMITATIONS, QUARANTINED, REJECTED, UNKNOWN, STALE, NOT_ASSESSED) de los resultados de otros dominios.  
**Riesgo:** Un claim factual CONFIRMED podría no ser admisible para una finalidad por fuente, licencia, point-in-time, integridad o freshness. Inversamente, ADMISSIBLE no significa que la proposición sea verdadera.  
**Recomendación:** Formalizar ejes ortogonales: (a) claim truth/resolution status; (b) evidence integrity/provenance/quality; (c) source admissibility for purpose; (d) freshness/lifecycle. No usar CONFIRMED como sinónimo de ADMISSIBLE ni REJECTED para significar que el hecho es falso, salvo que se defina el objeto al que se aplica el rechazo.  
**Criterio de cierre:** Modelo de estados multidimensional, reglas de composición y casos donde claim=CONFIRMED pero evidence inadmissible, y claim=UNKNOWN con source admissible.

### IDCA-03 — PurposeAssessment no está ligado formalmente a Scope Algebra
**Severidad:** HIGH  
**Evidencia:** PurposeAssessment incluye scope descrito mediante red, representación, instrumento, venue, jurisdicción, actividad y tiempo. Scope Algebra exige ScopeDefinition tipado, scope_id/version, normalization_version, scope_kind, purpose/activity, dimensiones explícitas, estados CONSTRAINED / UNCONSTRAINED_BY_SCOPE / NOT_APPLICABLE / UNRESOLVED, universo y reglas de comparabilidad.  
**Riesgo:** Implementaciones pueden serializar scopes incompatibles, interpretar campos omitidos como wildcard o combinar assessments de contextos distintos. Se romperían las garantías de partición exacta y aislamiento de decisiones.  
**Recomendación:** PurposeAssessment debe referenciar un ScopeDefinition versionado o un contrato compatible, no redefinir un scope ad hoc. Debe fijar scope_kind y firma de dimensiones; las dimensiones ausentes deben tener estado explícito. Purpose y activity son dimensiones distintas.  
**Criterio de cierre:** Contrato de interoperabilidad PurposeAssessment↔ScopeDefinition, firmas por purpose, reglas de normalización y pruebas de incompatibilidad.

### IDCA-04 — Falta de propagación formal de UNKNOWN / INVALID desde claims al scope
**Severidad:** HIGH  
**Evidencia:** FSA-01 define TRUE/FALSE/UNKNOWN para pertenencia, INVALID estricto para errores de contrato y operaciones UNRESOLVED cuando persiste incertidumbre. La propuesta de identidad usa UNVERIFIED, DISPUTED y missing claims, pero no define cómo cada condición evalúa predicados de scope ni sus operaciones.  
**Riesgo:** Un alias o relación no resuelta podría convertirse accidentalmente en no pertenencia, scope vacío, ausencia de afectación o resultado favorable. También puede confundirse conflicto factual con input estructural inválido.  
**Recomendación:** Definir adaptadores explícitos desde claim/relationship resolution a predicados tipados. Por defecto, evidencia insuficiente o relación no resuelta produce UNKNOWN; payload/tipo/referencia inválida produce INVALID; conflicto no adjudicado produce UNKNOWN o un estado CONFLICTED de dominio que no puede reducirse a FALSE. Aplicar las reglas FSA-01 y preservar diagnostics. No equiparar UNKNOWN a NOT_APPLICABLE.  
**Criterio de cierre:** Tabla de mapping por reason code, tablas de verdad y escenarios adversariales para alias, chain identity, wrapper, bridge y dependencia ausente.

### IDCA-05 — Contrato temporal y point-in-time incompleto
**Severidad:** MEDIUM  
**Evidencia:** La propuesta incluye valid_time, knowledge_time, public_availability_time, authorized_access_time, observed_at, recorded_at, ingested_at, processed_at y evaluated_at. Scope Algebra distingue valid_time de observed_at/recorded_at/evaluated_at; lifecycle también exige occurred_at/effective_at y conserva eventos tardíos/replay.  
**Riesgo:** knowledge_time se define como intervalo/instante en que el sistema conoció y conservó la afirmación, pero puede solaparse con recorded_at y no distingue necesariamente disponibilidad pública de conocimiento interno. Puede producir look-ahead bias en reconstrucciones históricas.  
**Recomendación:** Adoptar un contrato temporal común con definiciones independientes para valid time, source publication/availability, authorized availability, observation, ingestion, recording, processing, evaluation, event occurrence y effective time. Definir precisión, timezone, intervalos, unknown timestamps, causal ordering y el cutoff point-in-time por finalidad. No inferir public availability desde recorded_at ni valid_time.  
**Criterio de cierre:** Glosario temporal común, ejemplos bitemporales, política de intervalos/precisión y pruebas de no-look-ahead/replay.

### IDCA-06 — Dependencias e invalidación de claims/relationships no suficientemente especificadas
**Severidad:** MEDIUM  
**Evidencia:** La propuesta indica que cambios en claims/scope/policy invalidan el PurposeAssessment dependiente y reserva EvidenceDependency/links para una fase posterior. Lifecycle exige dependencias explícitas, tipo, transformación de scope, intended use, propagation rule/version, affected consumers y registro de invalidación.  
**Riesgo:** No puede determinarse qué assessment debe invalidarse cuando se corrige/retracta un claim, cambia una relación o se compromete una fuente. Tampoco se garantiza aislamiento de la invalidación a la finalidad/representación afectada.  
**Recomendación:** Incluir desde el contrato conceptual las referencias de dependencia entre PurposeAssessment, claim versions, relationship versions, source versions, policy versions y scope versions. La invalidación debe crear nueva versión/assessment, conservar el historial y propagarse solo por dependencias y scopes demostrables. Dependencia no resuelta nunca equivale a NOT_AFFECTED.  
**Criterio de cierre:** Dependency graph contract, reason codes, scope transforms y escenarios de eventos tardíos, retractación, fuente comprometida y relación superseded.

### IDCA-07 — Interfaz incompleta hacia ANALYTICAL_SELECTION y OPERATIONAL_ROUTE
**Severidad:** MEDIUM  
**Evidencia:** La propuesta define finalidades ANALYTICAL_SELECTION y OPERATIONAL_ROUTE, y dice que PurposeAssessment no reemplaza elegibilidad, vetos, mandato ni autorización. El modelo canónico y Scope Algebra separan decisiones analíticas (AE/ANALYTICAL_SELECTION) de elegibilidad de ruta operativa (ORE/OPERATIONAL_ROUTE).  
**Riesgo:** La misma evaluación de identidad podría consumirse como permiso operativo o heredarse entre finalidad analítica y ruta sin verificar claims, scope, freshness y controles específicos de venue/custodia.  
**Recomendación:** Definir outputs distintos por finalidad y contratos de consumo separados. ANALYTICAL_SELECTION puede consumir identidad/claims económicos/técnicos pertinentes; OPERATIONAL_ROUTE requiere además representación, instrumento, venue, jurisdicción, actividad, ruta y condiciones operativas vigentes. Ningún resultado de identidad/admisibilidad autoriza orden, custodia ni ejecución.  
**Criterio de cierre:** Matriz purpose→required claims/relationships→scope signature→consumer→blocking behavior y escenarios de no herencia AE↔ORE.

### IDCA-08 — NOT_APPLICABLE y ausencia de claims necesitan semántica de aplicabilidad explícita
**Severidad:** LOW  
**Evidencia:** La propuesta requiere justificación para PurposeAdmissibilityStatus=NOT_APPLICABLE. El modelo canónico separa Applicability (APPLICABLE, NOT_APPLICABLE, UNDETERMINED) del estado de evaluación; Scope Algebra también prohíbe tratar NOT_APPLICABLE como ALL.  
**Riesgo:** Una relación ausente o un claim no requerido puede confundirse con un método/objeto realmente no aplicable.  
**Recomendación:** Registrar applicability assessment separado con regla taxonómica, scope, evidencia/razón y versión. La ausencia de claim requerido debe producir INSUFFICIENT_EVIDENCE/UNKNOWN; solo una regla aprobada puede declarar NOT_APPLICABLE.  
**Criterio de cierre:** Vocabulario y tabla de decisión que separe APPLICABILITY de ASSESSMENT_STATUS y scope dimension state.

## 3. Matriz de compatibilidad

| Área | Compatibilidad actual | Acción requerida |
|---|---|---|
| Separación entidad / claim / decisión | Compatible | Preservar |
| Identity status | Parcial | Canonical mapping |
| Claim status vs evidence admissibility | Parcial | Ejes ortogonales |
| Purpose scope vs Scope Algebra | Incompleta | ScopeDefinition versionado |
| UNKNOWN / INVALID | Incompleta | Adaptador FSA-01 |
| Temporalidad | Parcial | Contrato temporal común |
| Lifecycle / invalidation | Parcial | Dependencias versionadas |
| ANALYTICAL_SELECTION / OPERATIONAL_ROUTE | Parcial | Interfaces separadas |
| NOT_APPLICABLE | Parcial | Applicability independiente |

## 4. Orden recomendado de resolución

1. IDCA-01 e IDCA-02: vocabularios y separación de dominios.
2. IDCA-03 e IDCA-04: scope formal y propagación de incertidumbre.
3. IDCA-05: contrato temporal point-in-time.
4. IDCA-06 e IDCA-07: dependencias, invalidación e interfaces de consumo.
5. IDCA-08: aplicabilidad y NOT_APPLICABLE.
6. Auditoría cruzada de la propuesta revisada; solo después diseñar schemas.

## 5. Restricciones de esta auditoría

- No modifica requisitos IIM, schemas, código, fixtures ni tests.
- No aprueba vocabularios ni decisiones de dominio.
- No demuestra formalmente las propiedades FSA-01.
- No declara IDR-01/02 cerrados.
- No acredita implementación, ejecución de tests ni validación empírica.
- Los documentos comparados son borradores; la compatibilidad aquí descrita no equivale a aprobación normativa.

## 6. Estado y siguiente paso

**Dictamen:** INTEGRATION_REVISE / REVIEW_REQUIRED.

Siguiente artefacto recomendado: `identity-graph-purpose-integration-proposal.md`, que resuelva IDCA-01..08 y presente una matriz de interoperabilidad completa antes de cualquier schema freeze. Los owners de Identity/Data, Architecture, Risk y Operations deberán revisar las decisiones que afecten sus dominios.
