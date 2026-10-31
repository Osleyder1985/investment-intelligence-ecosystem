# Propuesta integrada de identidad y admisibilidad por finalidad

**ID:** IIE-IDENTITY-PURPOSE-INTEGRATION-002  
**Versión:** 0.1.0-draft  
**Fecha:** 2026-10-31  
**Estado:** PROPOSAL / PENDING_REVIEW / NOT_APPROVED  
**Hallazgos:** IDCA-01..IDCA-08  
**Issue:** #61

## 1. Principio y alcance

Integrar EntityIdentity, Relationship, EvidenceClaim y PurposeAssessment con los modelos canónicos de estado, Scope Algebra, FSA-01 y lifecycle. Propuesta documental, no normativa. No crea schemas, código, fixtures ni tests y no modifica requisitos IIM.

Identidad, verdad del claim, admisibilidad de evidencia, aplicabilidad, freshness, scope y decisión consumidora son dimensiones independientes. Ningún estado de una capa autoriza una conclusión de otra.

## 2. Contratos semánticos

### 2.1 Identidad

Vocabulario canónico propuesto: RESOLVED, UNRESOLVED, CONFLICTED, STALE. AMBIGUOUS se convierte en reason code de UNRESOLVED. UNVERIFIED debe distinguir NOT_ASSESSED de evaluación inconclusa. RESOLVED_WITH_LIMITATIONS se descompone: la entidad puede estar RESOLVED mientras claims/relaciones concretos conservan sus limitaciones; si la clave de entidad no está demostrada, UNRESOLVED. Migración/retiro se expresan con eventos, relaciones MIGRATES_TO y lifecycle SUPERSEDED/INVALIDATED, no como estado de identidad.

### 2.2 Resolución de claims

ClaimResolutionStatus propuesto:
- CONFIRMED: proposición demostrada bajo estándar declarado.
- REFUTED: proposición refutada con evidencia suficiente.
- UNKNOWN: no se puede determinar verdadero/falso.
- CONFLICTED: evidencia material incompatible no adjudicada.
- NOT_ASSESSED: no evaluada.
- RETRACTED: retirada, con historial preservado.

PARTIALLY_CONFIRMED se descompone en claims atómicos. REJECTED no se usa para significar a la vez proposición falsa y evidencia inadmisible.

### 2.3 Admisibilidad y aplicabilidad

EvidenceAssessmentStatus se alinea con ADMISSIBLE, ADMISSIBLE_WITH_LIMITATIONS, QUARANTINED, REJECTED, UNKNOWN, STALE, NOT_ASSESSED. Evalúa integridad, provenance, independencia, licencia, disponibilidad point-in-time, freshness y método. Una fuente admisible no demuestra el claim.

ApplicabilityStatus: APPLICABLE, NOT_APPLICABLE, UNDETERMINED. NOT_APPLICABLE exige regla taxonómica versionada, razón y scope.

PurposeAdmissibilityStatus: ADMISSIBLE, ADMISSIBLE_WITH_LIMITATIONS, INSUFFICIENT_EVIDENCE, CONFLICTED, NOT_APPLICABLE, NOT_ASSESSED. Freshness/lifecycle permanecen separados. PurposeAssessment nunca produce ELIGIBLE, NO_VETO_IDENTIFIED ni permiso de ejecución.

## 3. Scope y FSA-01

PurposeAssessment referencia ScopeDefinition inmutable/versionada: scope_id, schema/normalization versions, scope_kind, purpose/activity, subject refs, dimension signature/states, universe snapshot, valid_time, predicate digest y dependency graph snapshot.

Estados de dimensión: CONSTRAINED, UNCONSTRAINED_BY_SCOPE, NOT_APPLICABLE, UNRESOLVED. Dimensión omitida no es wildcard; purpose y activity son distintos; scope operacional declara route; comparación exige firmas, universo, unidades y versiones compatibles.

Mapping lógico propuesto:
- Claim confirmado y evidencia admisible: TRUE solo si la regla de pertenencia lo demuestra.
- Claim refutado: FALSE solo si la regla demuestra no pertenencia.
- Claim desconocido/no evaluado, relación no resuelta, alias ambiguo o dependencia ausente: UNKNOWN.
- Campo/tipo/referencia obligatoria malformada: INVALID.
- Evidencia stale para cutoff: UNKNOWN para usos que exigen frescura.
- Evidencia inadmisible: no aporta prueba TRUE; nunca se convierte automáticamente en FALSE.

INVALID es error de contrato, no desconocimiento factual. UNKNOWN no se convierte en FALSE, EMPTY, NOT_APPLICABLE, NOT_AFFECTED ni ausencia de veto. La policy puede bloquear/revisar/abstenerse sin alterar la semántica lógica.

## 4. Temporalidad

Separar valid_time, source_published_at, public_availability_at, authorized_availability_at, occurred_at, observed_at, ingested_at, recorded_at, processed_at, evaluated_at y effective_at. knowledge_time no debe sustituirlos ni permanecer ambiguo.

Historical assessment declara cutoff, finalidad, actor/sistema cuyo conocimiento reconstruye, evidencia disponible antes del cutoff y versiones de policy. recorded_at no sustituye public_availability_at. No inferir valid_time desde block time o captura sin regla aprobada. Eventos tardíos generan replay nuevo, no reescritura histórica.

Se propone intervalo semiabierto [start,end), UTC, precisión declarada y límites desconocidos explícitos; requiere aprobación.

## 5. Lifecycle y dependencias

PurposeAssessment conserva dependencias versionadas hacia identidad, relaciones, claim/evidence assessments, fuentes, aplicabilidad, scope, universo, purpose/policy y dependency graph. Cada edge declara REQUIRED/MATERIAL/OPTIONAL/CONTEXTUAL, intended_use, scope_transform y propagation_rule/version.

Corrección/retractación genera evento; invalidación solo se propaga por dependencias y scopes demostrables. Dependencia no resuelta nunca significa NOT_AFFECTED. STALE bloquea usos que requieren freshness. INVALIDATED/SUPERSEDED no se consumen actualmente salvo replay histórico. Reevaluar crea assessment nuevo. Lifecycle no levanta vetos ni autoriza operaciones.

## 6. Interfaces de consumo

ANALYTICAL_SELECTION requiere identidad de representación, claims técnicos/económicos, evidencia admisible/fresca y scope analítico. Es input del motor, que aún evalúa elegibilidad, riesgo, vetos, fundamentos y valoración.

OPERATIONAL_ROUTE requiere además representación/instrumento, venue/listing, route_id, red/entorno, actividad, jurisdicción y capacidades operativas pertinentes. No hereda de ANALYTICAL_SELECTION y no autoriza órdenes. Mandato, policy, autorización, enforcement y ejecución son capas separadas.

No heredar resultados favorables entre entidades, representaciones, instrumentos, venues, finalidades, actividades, jurisdicciones, tiempos, snapshots o policies.

## 7. Escenarios y decisiones pendientes

Escenarios mínimos: identidad resuelta con claim de control desconocido; fuente admisible con claim desconocido; alias ambiguo; chain ID obligatorio ausente; claim retractado; evidencia publicada después del cutoff; BRIDGES confirmado con BACKED_BY desconocido; route_id ausente en scope operacional; incidente bridge limitado a representación y dependencias probadas; assessment analítico sin ORE; dependency edge ausente; cambio de policy con replay.

Owners: Identity/Architecture (estados), Data (claims), Governance/Legal/Security (admisibilidad), Architecture/Risk/Operations (scope), Formal Methods (FSA-01), Data Engineering (point-in-time), Investment Methodology/Operations (interfaces AE/ORE). Spot/derivatives permanece OPEN.

## 8. Gates y estado

- [ ] Mapping de estados aprobado.
- [ ] Separación claim truth / evidence admissibility / applicability / freshness aprobada.
- [ ] ScopeDefinition interoperable aprobado.
- [ ] Mapping FSA-01 revisado matemáticamente.
- [ ] Contrato temporal y point-in-time aprobado.
- [ ] Dependencias e invalidación revisadas.
- [ ] Interfaces AE/ORE aprobadas.
- [ ] Escenarios convertidos a fixtures/tests después de aprobación.
- [ ] Schemas aprobados antes de implementación.

**INTEGRATION_DRAFT_READY_FOR_REVIEW; NOT_APPROVED.** Intenta resolver IDCA-01..08, pero no los cierra hasta revisión independiente y aprobación. No hay schemas, código, fixtures ni tests; no se ejecutaron pruebas ni validación matemática/empírica. Requisitos IIM conservan sus estados previos.
