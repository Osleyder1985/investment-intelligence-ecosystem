# Auditoría cruzada de coherencia — decisiones de identidad D-01 a D-10

**ID:** IIE-IDENTITY-CROSS-AUDIT-010  
**Versión:** 0.1.0-draft  
**Fecha:** 2026-10-31  
**Estado:** REVIEW_REQUIRED / INTEGRATION_REVISE / NOT_APPROVED  
**Alcance:** D-01..D-10 frente a modelo canónico de estados, Lifecycle, Scope Algebra, FSA-01 y propuesta de grafo de identidad  
**Issue:** #61

## 1. Dictamen ejecutivo

Las diez decisiones cubren los dominios arquitectónicos necesarios para un modelo robusto de identidad y admisibilidad: ejes de scope, identidad/freshness, aplicabilidad, claims, evidencia, proyección lógica, temporalidad, dependencias, identidad por representación y PurposeAssessment.

La revisión estática identifica **9 hallazgos de integración: 4 HIGH, 4 MEDIUM y 1 LOW**. El conjunto no debe aprobarse ni congelarse para implementación hasta resolver los hallazgos HIGH y cerrar las decisiones de vocabulario/contratos. La auditoría no ejecutó código, tests, validación matemática, consultas de redes ni validación empírica.

## 2. Hallazgos

### ICA-01 — IdentityStatus canónico aún contiene STALE mientras D-02 propone retirarlo
**Severidad:** HIGH  
**Evidencia:** D-02 propone IdentityStatus = RESOLVED/UNRESOLVED/CONFLICTED y separa FreshnessStatus. El modelo canónico actual conserva STALE en IdentityStatus. Lifecycle separa freshness, admissibility, lifecycle y domain status. D-09 reutiliza el vocabulario de D-02 sin resolver la migración del documento canónico.  
**Riesgo:** Dos componentes pueden interpretar STALE como resolución de identidad o caducidad temporal; una implementación parcial podría mantener dos fuentes de verdad.  
**Remediación:** Decisión explícita de Architecture sobre la fuente de verdad; inventario de consumidores legacy; mapping y migración versionados; actualización coordinada del modelo canónico y Lifecycle solo tras aprobación. Hasta entonces, D-02/D-09 permanecen propuestas no implementables como contrato normativo.

### ICA-02 — PurposeAdmissibility no está mapeado a EligibilityStatus canónico
**Severidad:** HIGH  
**Evidencia:** D-10 propone ADMISSIBLE, ADMISSIBLE_WITH_LIMITATIONS, INSUFFICIENT_EVIDENCE, CONFLICTED y NOT_ASSESSED. El modelo canónico define EligibilityStatus como ELIGIBLE, ELIGIBLE_WITH_LIMITATIONS, INELIGIBLE, INSUFFICIENT_INFORMATION y QUARANTINED. D-03 ya había definido un PurposeAssessment outcome, pero no se formalizó la relación normativa entre ambos vocabularios.  
**Riesgo:** Duplicación de estados, mapping no determinista y consumidores distintos que traten un mismo expediente de forma incompatible. CONFLICTED no es necesariamente equivalente a QUARANTINED; ADMISSIBLE no es necesariamente ELIGIBLE para toda finalidad.  
**Remediación:** Definir si PurposeAssessment es evidencia/input y EligibilityAssessment es la decisión canónica consumidora, o si uno sustituye al otro. Crear mapping total y explícito, incluyendo estados no equivalentes, límites de uso y ownership. No renombrar ni fusionar vocabularios por similitud textual.

### ICA-03 — FreshnessStatus de D-10 incorpora UNKNOWN sin contrato canónico
**Severidad:** HIGH  
**Evidencia:** D-01 define FreshnessStatus FRESH, APPROACHING_EXPIRY, STALE y NOT_APPLICABLE. Lifecycle utiliza ese vocabulario. D-10 añade UNKNOWN. El modelo canónico usa UNKNOWN dentro de EvidenceStatus, no como estado de freshness.  
**Riesgo:** UNKNOWN puede significar timestamp desconocido, política inexistente, evaluación no ejecutada o freshness no calculable; estas condiciones requieren tratamientos diferentes.  
**Remediación:** Mantener FreshnessStatus separado y definir freshness_assessment_status/validity del cálculo. Diferenciar NOT_ASSESSED, UNDETERMINED y INVALID sin añadir un estado ambiguo al dominio freshness. Determinar cómo se comporta el consumidor cuando freshness no puede calcularse.

### ICA-04 — Contrato de error INVALID se mezcla con vocabularios de dominio
**Severidad:** HIGH  
**Evidencia:** D-04 y D-06 establecen INVALID como error contractual fuera de la conclusión factual; FSA-01 separa validity de TRUE/FALSE/UNKNOWN. D-05 incluye INVALID dentro del vocabulario de admisibilidad de ClaimEvidenceAssessment; D-10 lo lista en Applicability y Dependency completeness.  
**Riesgo:** Un consumidor podría tratar INVALID como resultado de una obligación o dependencia en lugar de invalidar el envelope/assessment afectado.  
**Remediación:** Separar contract_validity/validation_result de domain outcome. INVALID puede ser reason/status de validación del objeto, pero nunca un resultado factual ni una categoría equivalente a UNKNOWN/REJECTED. Definir propagación de error por frontera de contrato y bloqueo de consumo.

### ICA-05 — Falta mapping formal entre PurposeAssessment y firmas/dimensiones de Scope Algebra
**Severidad:** MEDIUM  
**Evidencia:** D-01 separa scope_kind, decision_type, purpose y activity; Scope Algebra requiere firma de dimensiones por scope_kind y declara no comparables scopes con firmas diferentes. D-09/D-10 enumeran dimensiones y campos, pero no proporcionan la tabla normativa de firmas obligatorias por combinación de scope_kind/decision_type/purpose/activity/entity type.  
**Riesgo:** Scopes malformados, comparación accidental de scopes incompatibles o aceptación de una evaluación cuya dimensión obligatoria fue omitida.  
**Remediación:** Producir un ScopeSignatureRegistry versionado: combinaciones permitidas, dimensiones requeridas/opcionales, tipo/namespace, regla de NOT_APPLICABLE, restricciones de comparabilidad y transformaciones. Cada PurposeAssessment debe referenciar una firma exacta.

### ICA-06 — Vocabulario de admisibilidad de ClaimEvidenceAssessment no está alineado con EvidenceStatus
**Severidad:** MEDIUM  
**Evidencia:** D-05 propone ADMISSIBLE, ADMISSIBLE_WITH_LIMITATIONS, INSUFFICIENT, QUARANTINED, REJECTED, NOT_ASSESSED e INVALID. El modelo canónico EvidenceStatus utiliza ADMISSIBLE, ADMISSIBLE_WITH_LIMITATIONS, QUARANTINED, REJECTED, UNKNOWN, STALE y NOT_ASSESSED.  
**Riesgo:** INSUFFICIENT puede confundirse con suficiencia de evidencia agregada; falta UNKNOWN/STALE como dimensiones explícitas; INVALID se mezcla con admissibility.  
**Remediación:** Separar EvidenceAdmissibility, EvidenceSufficiency, Freshness y ContractValidity. Definir mappings y ownership por objeto: EvidenceRecord, SourceAssessment y ClaimEvidenceAssessment. No convertir INSUFFICIENT en sustituto de UNKNOWN o STALE.

### ICA-07 — EvaluationLifecycle y assessment workflow requieren una sola fuente de verdad
**Severidad:** MEDIUM  
**Evidencia:** Modelo canónico y Lifecycle definen EvaluationLifecycle con NOT_STARTED, IN_PROGRESS, COMPLETE, REVIEW_REQUIRED, SUPERSEDED e INVALIDATED. D-10 presenta Assessment workflow con los cuatro primeros y Lifecycle aparte con ACTIVE/SUPERSEDED/INVALIDATED/WITHDRAWN.  
**Riesgo:** COMPLETE podría ser interpretado como lifecycle y no como completitud de proceso; los estados terminales podrían duplicarse entre dos máquinas.  
**Remediación:** Referenciar EvaluationLifecycle canónico como único workflow state machine. Mantener object lifecycle en eje distinto. Documentar transiciones y restricciones de combinación; no crear una segunda máquina incompleta.

### ICA-08 — Contrato de identidad de red/representación requiere registry de perfiles y cardinalidades
**Severidad:** MEDIUM  
**Evidencia:** D-09 enumera claves conceptuales EVM, Solana, UTXO, Cosmos/IBC y Cardano, pero no fija perfiles versionados ejecutables, cardinalidades de relaciones, reglas de canonicalization, resolución de aliases ni política completa para forks/reorgs y native assets.  
**Riesgo:** Identificadores no interoperables, resolución ambigua entre redes y falsos merges/splits. La lista de familias puede interpretarse erróneamente como cobertura exhaustiva.  
**Remediación:** Crear NetworkIdentityProfileRegistry y RepresentationIdentifierProfile por namespace/version; especificar clave compuesta, validadores, cardinalidad, alias, temporalidad, fork/reorg y pruebas de colisión. Etiquetar explícitamente perfiles no soportados.

### ICA-09 — Falta matriz de correspondencia entre PurposeAssessment, EligibilityAssessment y decisiones consumidoras
**Severidad:** LOW  
**Evidencia:** D-10 define contratos de consumo para Identity, Economic Linkage, Analytical Selection, Risk, Veto, ORE y Portfolio; el modelo canónico define EligibilityStatus, SelectionDecision y RouteEligibility por separado. La frontera es descrita conceptualmente pero no existe tabla completa de entradas, autoridad, outputs y prohibiciones por consumidor.  
**Riesgo:** Integración downstream desigual y duplicación de lógica de decisión en múltiples módulos.  
**Remediación:** Consolidar una matriz de interface contracts que identifique producer, consumer, object/version, required fields, allowed states, freshness/cutoff, authority y fail-closed behavior.

## 3. Matriz de coherencia

| Dominio | Resultado | Observación |
|---|---|---|
| D-01 ejes scope/purpose/activity | PARTIALLY_ALIGNED | Falta registry de firmas |
| D-02 identidad/freshness | BLOCKED | Canonical model conserva STALE |
| D-03 applicability | PARTIALLY_ALIGNED | Vacuity rule bien definida; INVALID boundary pendiente |
| D-04 claims | PARTIALLY_ALIGNED | Necesita integración contractual |
| D-05 evidence | REVISE | Vocabularios no alineados |
| D-06 FSA-01 adapter | PARTIALLY_ALIGNED | INVALID boundary debe ser única |
| D-07 temporalidad | PARTIALLY_ALIGNED | Perfiles point-in-time requieren contratos de fuente |
| D-08 dependencias | PARTIALLY_ALIGNED | Falta alineación ejecutable de dependencias/scope signatures |
| D-09 identidad por representación | REVISE | Profiles/registry/cardinalities pendientes |
| D-10 PurposeAssessment | BLOCKED | Mapping con EligibilityStatus y lifecycle pendiente |

## 4. Orden de remediación recomendado

1. **R-01 — Canonical status boundary:** resolver ICA-01, ICA-02, ICA-03, ICA-04 e ICA-07; aprobar fuente de verdad de estados.
2. **R-02 — Scope signatures:** resolver ICA-05 y conectar D-09/D-10 con Scope Algebra.
3. **R-03 — Evidence contract normalization:** resolver ICA-06 y alinear D-05 con EvidenceStatus/FSA-01.
4. **R-04 — Identity profile registry:** resolver ICA-08 con expertos por familia de ledger.
5. **R-05 — Consumer interface contracts:** resolver ICA-09 y completar trazabilidad producer/consumer.
6. Revisión adversarial independiente de la versión integrada resultante.
7. Solo después de aprobar: schemas, fixtures, tests, implementación y CI.

## 5. Límites de la auditoría

Esta es una revisión estática de documentos y vocabularios. No demuestra soundness matemática, seguridad, integridad de datos reales, cobertura de redes, cumplimiento legal ni rendimiento de implementación. No se ejecutaron tests ni se inspeccionaron fuentes externas en tiempo real.

## 6. Estado final

**REVIEW_REQUIRED / INTEGRATION_REVISE / NOT_APPROVED.**

D-01..D-10 están documentadas como propuestas. No están aprobadas institucionalmente, no constituyen contratos ejecutables y no deben etiquetarse como IMPLEMENTED o TESTED. Los hallazgos HIGH bloquean la consolidación normativa y la generación de schemas hasta su resolución.
