# Plan de remediación de la integración de identidad

**ID:** IIE-IDENTITY-REMEDIATION-004  
**Versión:** 0.1.0-draft  
**Fecha:** 2026-10-31  
**Estado:** PROPOSED / PENDING_ARCHITECTURE_REVIEW / NOT_APPROVED  
**Base:** identity-adversarial-review.md (ADV-01..ADV-10)  
**Issue:** #61

## 1. Objetivo

Traducir los hallazgos adversariales en decisiones y cambios acotados, sin alterar documentos normativos ni contratos existentes. Identifica qué puede corregirse en el modelo de identidad y qué exige decisión transversal de Architecture, Risk, Data Governance u Operations.

No es aprobación, schema freeze ni autorización de implementación.

## 2. Clasificación de remediación

| Finding | Remediación propuesta | Alcance | Autoridad requerida |
|---|---|---|---|
| ADV-01 | Separar scope_kind, purpose, decision_type y activity; tratar OPERATIONAL_ROUTE como scope_kind/decision domain | Transversal | Architecture + Methodology + Operations |
| ADV-02 | Resolver STALE como dimensión Freshness; evaluar y migrar su uso en IdentityStatus | Transversal; afecta docs. 03 y 12 | Architecture + Identity + Data Governance |
| ADV-03 | Retirar NOT_APPLICABLE de PurposeAdmissibilityStatus; conservarlo en ApplicabilityAssessment | Identidad | Architecture + Methodology |
| ADV-04 | Crear ClaimEnvelope tipado y reglas de atomización/adjudicación por familia | Identity/Data | Identity + Data + Domain owners |
| ADV-05 | Separar EvidenceRecord, SourceAssessment y ClaimEvidenceAssessment; declarar agregación | Data/evidence | Data Governance + Legal + Security |
| ADV-06 | Proyectar a FSA-01 desde predicados de pertenencia tipados y conjunto de fuentes admisibles | Scope/evidence | Architecture + Formal Methods + Data |
| ADV-07 | Extender contrato temporal con valor original, normalización, precisión, incertidumbre y evidencia | Transversal | Data Engineering + Governance |
| ADV-08 | Reutilizar LifecycleEvent, DependencyEdge e InvalidationRecord completos; añadir manejo provisional | Transversal | Architecture + Operations + Security |
| ADV-09 | Delimitar IdentityAssessment por subject type/key, representation, network/environment, scope y valid_time | Identidad | Identity + Architecture |
| ADV-10 | Definir PurposeAssessment envelope con outcome, workflow, freshness, lifecycle y applicability ortogonales | Integración | Architecture + Data + Operations |

## 3. Decisiones de diseño propuestas

### D-01 — Ejes del scope

Adoptar cuatro ejes explícitos:
1. scope_kind: ANALYTICAL, OPERATIONAL_ROUTE, RISK, VETO, EVIDENCE, VALUATION u otro valor aprobado.
2. decision_type: tipo de decisión consumidora (por ejemplo, ANALYTICAL_SELECTION o ROUTE_ELIGIBILITY), sujeto al catálogo canónico.
3. purpose: propósito de evaluación (DISCOVERY, RESEARCH, VALUATION, COMPARISON, etc.).
4. activity: acción concreta (TRADE, DEPOSIT, WITHDRAWAL, TRANSFER, CUSTODY, REDEMPTION).

Los catálogos no deben permitir que un mismo valor aparezca en varios ejes por conveniencia. La compatibilidad histórica usa mapping versionado.

### D-02 — Freshness e identidad

Propuesta preferida: IdentityStatus contiene RESOLVED, UNRESOLVED y CONFLICTED. Freshness se registra separadamente (FRESH, APPROACHING_EXPIRY, STALE, NOT_APPLICABLE), según Lifecycle.

Esto requiere decisión transversal porque el modelo canónico actual incluye STALE en IdentityStatus. Hasta aprobación:
- no modificar docs. 03 ni 12;
- no introducir un segundo campo STALE con semántica divergente;
- mantener una nota de compatibilidad y bloquear schema freeze.

### D-03 — Aplicabilidad

ApplicabilityAssessment es el único dominio que emite APPLICABLE, NOT_APPLICABLE o UNDETERMINED. PurposeAdmissibilityStatus no incluye NOT_APPLICABLE.

PurposeAssessment conserva obligaciones evaluadas, obligaciones excluidas por NOT_APPLICABLE, regla/versión/scope/justificación de cada exclusión y suficiencia agregada de las obligaciones aplicables. Si ninguna obligación aplica, el resultado agregado requiere decisión explícita de política; no se presume ADMISSIBLE.

### D-04 — ClaimEnvelope

Cada claim es una proposición tipada, atómica y versionada, con campos conceptuales:
- claim_id/version;
- subject_ref y subject_type;
- predicate_id/version;
- object/value o target_ref;
- polarity;
- qualifiers y cardinality;
- unit/currency/precision cuando aplique;
- valid_time;
- jurisdiction/environment cuando aplique;
- quantifier/population y closed-world declaration cuando aplique;
- method/standard_id y adjudication policy;
- evidence references;
- source and knowledge timestamps;
- status/reason codes;
- supersedes/retracts/cause references.

No se presume que un claim refutado niegue pertenencia al scope. REFUTED se interpreta contra la proposición exacta y solo influye en un predicado de pertenencia si la regla aprobada lo conecta formalmente.

### D-05 — Modelo de evidencia

Separar:
1. EvidenceRecord: artefacto/observación e integridad.
2. SourceAssessment: confiabilidad, permisos, licencia, independencia y estado de fuente.
3. ClaimEvidenceAssessment: relación de soporte/refutación entre evidencia y claim para purpose/scope/cutoff.

Cada assessment conserva source lineage. La agregación usa política explícita por tipo de claim; no promedia estados categóricos ni permite que una fuente inadmisible invalide automáticamente fuentes alternativas.

### D-06 — Adaptador FSA-01

La entrada del adaptador es un predicate contract tipado, no un status string aislado. Cada predicado declara claims necesarios/suficientes, policy de admisibilidad, universo cerrado o abierto, reglas TRUE/FALSE, condiciones UNKNOWN/INVALID, reason codes y proof references.

REFUTED solo produce FALSE si refuta exactamente la condición de pertenencia y se cumplen completitud y reglas del dominio. Una fuente REJECTED se excluye del conjunto de soporte, pero no descarta otra evidencia admisible suficiente.

### D-07 — Tiempo y point-in-time

Cada timestamp preserva:
- normalized_value;
- original_value;
- original_timezone_or_offset;
- precision;
- uncertainty_interval o uncertainty_status;
- source/clock identity;
- derivation_method;
- evidence_ref.

public_availability_at y authorized_availability_at solo tienen valor determinado con evidencia; de lo contrario UNKNOWN. El cutoff histórico declara zona, precisión y regla de frontera. No ordenar eventos si sus intervalos de incertidumbre impiden demostrar el orden.

### D-08 — Lifecycle y dependencias

No crear una máquina de dependencias reducida. Reutilizar Lifecycle completo:
- upstream/downstream object IDs y versions;
- edge ID/type;
- scope transform;
- intended use;
- propagation rule/version;
- owner;
- creation/validity;
- causation/correlation;
- event verification/confidence;
- idempotency;
- affected consumers;
- invalidation record y audit trail.

Distinguir trigger provisional (REVIEW_REQUIRED/QUARANTINED) de invalidación confirmada. Una señal creíble no verificada puede abrir revisión en scope plausible; no convierte automáticamente el objeto en INVALIDATED.

### D-09 — Resolución de identidad acotada

IdentityAssessment identifica subject type/key, canonical entity reference, representation_id cuando aplique, network/chain ID, environment, scope_id/version, valid_time y evaluación point-in-time, además de evidencias y dependencias.

Canonical entity resolution no prueba network binding ni representación. Cada una necesita evaluación explícita y no hereda estados favorables de otra representación.

### D-10 — PurposeAssessment envelope

Mantener dominios ortogonales:
- assessment workflow: NOT_STARTED, IN_PROGRESS, COMPLETE, REVIEW_REQUIRED, SUPERSEDED, INVALIDATED;
- purpose admissibility outcome: ADMISSIBLE, ADMISSIBLE_WITH_LIMITATIONS, INSUFFICIENT_EVIDENCE, CONFLICTED, NOT_ASSESSED;
- freshness: FRESH, APPROACHING_EXPIRY, STALE, NOT_APPLICABLE;
- lifecycle: ACTIVE, SUPERSEDED, INVALIDATED, WITHDRAWN según tipo;
- applicability: APPLICABLE, NOT_APPLICABLE, UNDETERMINED;
- scope resolution / validation envelope;
- dependency completeness and reasons.

La matriz de combinaciones debe prohibir estados imposibles y definir qué consumidores pueden usar cada combinación.

## 4. Orden de remediación

1. D-01/D-02: catálogos transversales y compatibilidad con docs. 03/12/19.
2. D-03/D-04: aplicabilidad y ClaimEnvelope.
3. D-05/D-06: evidencia y adaptador FSA-01.
4. D-07/D-08: tiempo, eventos y dependencias.
5. D-09/D-10: identidad por representación y PurposeAssessment envelope.
6. Redactar nueva versión integrada de identidad conservando la propuesta anterior como historial.
7. Segunda auditoría de consistencia y adversarial.
8. Solo después schemas, fixtures, tests y CI.

## 5. Gates de aprobación

- [ ] Architecture aprueba ejes scope_kind / decision_type / purpose / activity.
- [ ] Architecture resuelve conflicto STALE con docs. 03 y 12.
- [ ] Applicability y PurposeAdmissibility quedan sin duplicación.
- [ ] ClaimEnvelope y atomización aprobados por Data/Domain owners.
- [ ] Evidence model y source admissibility aprobados por Governance/Legal/Security.
- [ ] Predicate adapter y mapping FSA-01 revisados por Formal Methods.
- [ ] Point-in-time contract aprobado por Data Governance.
- [ ] Lifecycle/Dependency contract alineado sin pérdida de campos.
- [ ] Identity resolution por representación y network binding aprobado.
- [ ] PurposeAssessment envelope y transiciones revisados por Architecture/Operations.
- [ ] Auditoría independiente sin hallazgos críticos abiertos.

## 6. Estado y límites

**PROPOSED / PENDING_ARCHITECTURE_REVIEW / NOT_APPROVED.**

Este plan convierte ADV-01..ADV-10 en decisiones y remediaciones concretas. No cierra hallazgos, no aprueba el modelo y no autoriza schema freeze o implementación. No se modificaron documentos normativos, requisitos IIM, schemas, código, fixtures o tests. No se ejecutaron pruebas ni validación matemática/empírica.
