# R01.8 — Auditoría adversarial transversal R01.1–R01.7

**ID:** IIE-IDENTITY-R01-08  
**Versión:** 0.1.0-draft  
**Fecha:** 2026-10-31  
**Estado:** REVIEW_REQUIRED / INTEGRATION_REVISE / NOT_APPROVED  
**Alcance:** R01.1–R01.7, D-01..D-10 y referencias cruzadas al modelo canónico 03, X-06, X-07, X-08 y Scope Algebra/X-09.  
**Método:** revisión documental estática y contraste de vocabularios, transiciones, gates y scopes. No es revisión de código ni prueba formal.

## 1. Dictamen ejecutivo

R01.7 mejora la separación conceptual de ejes y formula mappings prohibidos, pero **no cierra la compatibilidad con los documentos canónicos**. Persisten contradicciones y vacíos que impiden aprobar la integración o congelar schemas.

**Resultado:** 12 hallazgos: 8 HIGH y 4 MEDIUM.  
**Dictamen:** REVIEW_REQUIRED / INTEGRATION_REVISE / NOT_APPROVED.

R01.7 se encuentra en PR #62 como borrador documental. Esta auditoría no lo aprueba ni recomienda merge sin remediación.

## 2. Hallazgos

### R018-01 — HIGH — Invalidación del record padre amplía indebidamente el impacto parcial

**Evidencia:** R01.7 §4 D10 y §7; X-06 §3–4 y §8; Scope Algebra §§5–8; R01.5 R015-16.

**Defecto:** R01.7 propone que cualquier invalidación de una parte material del scope invalide globalmente la versión completa del record padre y obliga a derivar/revaluar regiones restantes. X-06 y R01.5, en cambio, exigen propagación limitada a átomos afectados demostrados y prohíben invalidación global por un subset salvo prueba de alcance total.

**Impacto:** fail-closed se convierte en fail-broad; bloquea regiones independientes sin causalidad demostrada y contradice el principio de no ampliar restricciones.

**Remediación:** separar lifecycle del contenedor/record y admisibilidad de cada claim/assessment atómico; especificar qué record se invalida, qué outputs quedan no consumibles y qué átomos permanecen válidos. Si se conserva invalidación global del contenedor, demostrar que no elimina conclusiones independientes y definir lectura atómica obligatoria. Resolver con X-07/X-09 antes de aprobar.

### R018-02 — HIGH — ValidationEnvelope global contradice composición parcial por unidad independiente

**Evidencia:** R01.7 D09/D11; X-06 §4.3 y §8, especialmente el tratamiento de inputs obligatorios ausentes y scopes independientes; X-09 INV-33/38/48/49.

**Defecto:** R01.7 declara que solo un ValidationEnvelope=VALID habilita X-06, sin definir envelope por requested_scope, unidad atómica o conjunto de dependencias. X-06 permite continuar para scopes/conclusiones independientes cuando falta un input obligatorio que solo afecta otra unidad.

**Impacto:** un input incompleto en un átomo puede bloquear indebidamente todos los demás; o, si se interpreta globalmente, el envelope puede ocultar incompletitud local.

**Remediación:** envelope evaluado por unidad atómica y composición agregada; incluir manifest exacto de inputs/dependencias por átomo, estado de cada gate y regla de cobertura. Solo VALID para el átomo habilita su composición. La agregación del requested_scope conserva outcomes y bloqueos por átomo; no usa un único booleano global.

### R018-03 — HIGH — ValidationEnvelope no tiene contrato canónico ni correspondencia exacta con X-06

**Evidencia:** R01.7 D09; X-06 §4.2–4.3 y §8.

**Defecto:** X-06 define clases de resultado de validación REJECTED_INVALID_INPUT, REVIEW_REQUIRED, INCOMPLETE y VALID, pero no define el objeto ValidationEnvelope, sus campos obligatorios, ownership, canonical ID, binding de proof certificate ni reglas de consumo. R01.7 introduce el nombre y campos propuestos, pero no resuelve el contrato ni las diferencias entre scope validation, dependency completeness y envelope status.

**Impacto:** la precondición crítica no es implementable ni verificable; diferentes módulos podrían construir envelopes incompatibles o afirmar VALID sin demostrar cobertura.

**Remediación:** especificar envelope versionado, productor independiente, consumer contract, atom/scope binding, digest, validator identity/version, proof references, dependency closure semantics, error taxonomy y reglas de replay. Alinear los cuatro outcomes con X-06 y Scope Algebra. No crear schema hasta aprobación semántica.

### R018-04 — HIGH — Catálogo de SelectionDecision no tiene una fuente de autoridad única

**Evidencia:** R01.7 tabla §3 y D11; modelo canónico 03 §4.8; X-06 §5.1 y documento 18 (precedencia agregada).

**Defecto:** R01.7 describe SelectionDecision como DECISION_* “del modelo canónico”, pero el modelo 03 usa BLOCKED_IDENTITY, BLOCKED_ELIGIBILITY, BLOCKED_EVIDENCE, VETOED, REVIEW_REQUIRED, RISK_INCOMPLETE, VALUATION_* y OPPORTUNITY_SUPPORTED, mientras X-06 define DECISION_NOT_ASSESSED, DECISION_IN_PROGRESS, DECISION_REVIEW_REQUIRED, DECISION_INELIGIBLE, DECISION_ABSTAIN, DECISION_ELIGIBLE_WITH_LIMITATIONS y DECISION_ELIGIBLE.

**Impacto:** no existe mapping completo entre decisión específica/causa del modelo 03 y outcome categórico de X-06; puede cambiar el significado o perderse una causa.

**Remediación:** declarar catálogo propietario, separar DecisionOutcome de DecisionReason/DecisionDisposition y definir crosswalk total/versionado para cada estado y combinación, preservando causa primaria y causas secundarias. La precedencia solo se aplica después de envelope válido y partición exacta.

### R018-05 — HIGH — RouteEligibility tiene tres catálogos incompatibles

**Evidencia:** R01.7 §3/D08; modelo 03 §4.9; X-06 §5.2; X-04 referenciado por X-06.

**Defecto:** modelo 03 enumera seis estados (ROUTE_ELIGIBLE, ROUTE_ELIGIBLE_WITH_LIMITATIONS, ROUTE_INELIGIBLE, ROUTE_REVIEW_REQUIRED, ROUTE_UNKNOWN, ROUTE_STALE); X-06/X-04 enumera diez, incluyendo ROUTE_NOT_ASSESSED, ROUTE_ASSESSMENT_IN_PROGRESS, ROUTE_INSUFFICIENT_INFORMATION, ROUTE_CONFLICTED y ROUTE_INVALID. R01.7 remite vagamente a “modelo canónico/X-04” y deja ROUTE_INVALID como asunto legacy sin definir crosswalk para ROUTE_UNKNOWN.

**Impacto:** producer y consumer pueden interpretar estados distintos; la separación analítica/operacional no es determinista.

**Remediación:** elegir catálogo autoritativo por versión, mapear ROUTE_UNKNOWN con reglas explícitas a NOT_ASSESSED o INSUFFICIENT_INFORMATION según evidencia/workflow, y decidir el destino de ROUTE_INVALID. Prohibir mapping por similitud textual.

### R018-06 — HIGH — Lifecycle y workflow duplican estados sin crosswalk ejecutable

**Evidencia:** R01.7 D02/§3/§6; modelo 03 §4.1; X-07 §3.1–3.2; R01.4.

**Defecto:** el modelo canónico y X-07 incluyen EvaluationLifecycle con NOT_STARTED, IN_PROGRESS, COMPLETE, REVIEW_REQUIRED, SUPERSEDED e INVALIDATED. R01.7 lo divide en AssessmentWorkflowStatus y RecordLifecycleStatus, ambos razonables como ejes, pero no especifica de forma exhaustiva qué entidad lleva cada campo, cómo se migra EvaluationLifecycle ni cómo se representan estados simultáneos como workflow COMPLETE + record SUPERSEDED.

**Impacto:** doble fuente de verdad, transiciones imposibles o estados legacy sin migración; un consumer puede aceptar registro terminal por leer solo Workflow=COMPLETE.

**Remediación:** definir objeto AssessmentRecord y RecordVersion, ubicación exacta de ambos ejes, invariantes de combinación, eventos/transiciones y crosswalk legacy exhaustivo. Lifecycle terminal debe bloquear consumo actual aunque Workflow=COMPLETE.

### R018-07 — HIGH — Evidencia conserva un enum híbrido canónico sin migración completa

**Evidencia:** R01.7 D06 y tabla §3; modelo 03 §4.3; X-07 §3.2; R01.5 §§3–4.

**Defecto:** el modelo canónico EvidenceStatus combina admisibilidad, calidad epistemológica, freshness y workflow (ADMISSIBLE, ADMISSIBLE_WITH_LIMITATIONS, QUARANTINED, REJECTED, UNKNOWN, STALE, NOT_ASSESSED). R01.7 propone separar EvidenceAdmissibility, EvidenceSufficiency, Freshness y workflow, pero no especifica mapping individual para todos los estados legacy ni el owner de UNKNOWN en EvidenceStatus.

**Impacto:** estados viejos pueden migrarse de forma arbitraria; evidencia STALE podría confundirse con inadmisible, UNKNOWN con insuficiente, o NOT_ASSESSED con evidencia rechazada.

**Remediación:** crosswalk por objeto/uso/versión para cada estado legacy, con estados no migrables explícitos; definir EvidenceRecord, SourceAssessment, EvidenceAdmissibility, EvidenceSufficiency y ClaimResolution como contratos separados. No borrar EvidenceStatus antes de migración y compatibilidad dual aprobadas.

### R018-08 — HIGH — RiskDomainStatus INVALID se retira sin mapping completo al Decision Engine

**Evidencia:** R01.7 D03/D08; modelo 03 §4.5 y §4.8; X-06 §§4, 6–8.

**Defecto:** el modelo 03 incluye RiskDomainStatus=INVALID y SelectionDecision=RISK_INCOMPLETE cuando riesgo material es INVALID. R01.7 propone retirar INVALID del outcome de riesgo y trasladarlo a ContractValidity/AssessmentValidity, pero no define cómo el consumidor distingue fallo contractual, fallo metodológico, integridad de datos y evaluación no válida, ni cómo se preserva RISK_INCOMPLETE/causa en X-06.

**Impacto:** un assessment de riesgo inválido podría desaparecer del vector de riesgo o convertirse en abstención genérica sin trazabilidad de la causa.

**Remediación:** definir error envelope tipado por assessment y mapping a cause codes/decision reasons; conservar RISK_INCOMPLETE cuando aplique. No sustituir RiskDomainStatus.INVALID hasta aprobar migración y reglas de consumer.

### R018-09 — MEDIUM — AssessmentValidity añade eje nuevo sin semántica verificable

**Evidencia:** R01.7 §3/D03/D09; X-06 §4.2; X-07 §3.

**Defecto:** se propone AssessmentValidity=VALID/INVALID pero no se definen criterios verificables, límites respecto a ContractValidity, producer independiente, tratamiento de solver timeout, método no aprobado, data-quality failure ni invalidación posterior.

**Impacto:** INVALID puede convertirse en contenedor genérico que duplique ContractValidity o absorba outcomes epistemológicos y de dominio.

**Remediación:** antes de adoptarlo, definir taxonomía de defectos, boundary entre contrato, ejecución del método, evidencia y outcome; producer, independence, remediation y replay. Alternativamente no añadir eje y usar error envelope tipado.

### R018-10 — MEDIUM — Transición de veto omite el estado intermedio de revisión

**Evidencia:** R01.7 §6; modelo 03 §6.2; X-07 eventos VETO_REMEDIATION_PROPOSED/VERIFIED; X-08 autoridad de release.

**Defecto:** R01.7 resume VETO_ACTIVE → NO_VETO_IDENTIFIED tras remediación verificada y release authority, pero el modelo 03 exige la secuencia VETO_ACTIVE → VETO_REVIEW_REQUIRED → NO_VETO_IDENTIFIED. R01.7 no declara que el estado intermedio sea obligatorio.

**Impacto:** la implementación podría saltarse el workflow de revisión independiente y liberar veto directamente.

**Remediación:** especificar transición obligatoria a VETO_REVIEW_REQUIRED, adjudicación/review independiente, verificación de todas las reglas y nueva decisión versionada. No liberar por TTL, silencio ni solo evento de remediación.

### R018-11 — MEDIUM — Freshness/Identity migration sigue sin crosswalk de consumidor

**Evidencia:** R01.7 D01; R01.1 §§2–4; modelo 03 §4.2; X-07 §3.2.

**Defecto:** R01.7 repite la separación propuesta pero no define la migración del campo IdentityStatus.STALE del modelo canónico, ni un mapping por consumer para saber si un uso requiere freshness, qué policy/cutoff usar y cómo manejar registros históricos.

**Impacto:** consumer puede seguir esperando STALE en IdentityStatus o tratar Identity=RESOLVED sin freshness como suficiente.

**Remediación:** mapping legacy versionado, FreshnessAssessment explícito por uso/scope/cutoff, regla de obligatoriedad por consumer y dual-read/compatibility window.

### R018-12 — MEDIUM — SoD y authority se expresan como principios, no como controles operables

**Evidencia:** R01.7 D12/§8; X-08 §§5–12.

**Defecto:** R01.7 enumera incompatibilidades, pero no define decision rights, quorum, delegación, autoridad de emergencia, conflict-of-interest handling ni evidencia verificable de independencia. X-08 también permanece propuesta no aprobada.

**Impacto:** no se puede verificar quién puede emitir, aprobar, invalidar, liberar veto o conceder excepciones; el gate de gobernanza no es ejecutable.

**Remediación:** cerrar X-08 con roles/owners autorizados, permisos por acción/scope, quorum, SoD, recusación, delegación, break-glass y audit evidence; luego asociar cada producer/transition con authority_ref.

## 3. Crosswalk de estado

| Área | Resultado R01.8 |
|---|---|
| Identity/Freshness | Conceptualmente mejor, migración y consumer contracts abiertos |
| Workflow/Lifecycle | Separación útil, pero duplicación/crosswalk sin cerrar |
| Evidence/Claims | Taxonomía propuesta, migración legacy incompleta |
| Applicability/Purpose/Eligibility | Separación conceptual adecuada; NOT_ADMISSIBLE requiere policy y crosswalk |
| Risk/Veto | Catálogos parcialmente incompatibles; invalidity mapping pendiente |
| SelectionDecision | Dos catálogos distintos sin crosswalk |
| RouteEligibility | Catálogos 03 y X-04/X-06 divergentes |
| ValidationEnvelope | Necesario, pero no canónico, no atom-scoped y sin proof binding |
| Scope/invalidation | D10 global contradice propagación parcial de X-06/X-09 |
| Governance | Principios SoD, no autoridad operativa aprobada |

## 4. Acciones de remediación recomendadas

1. **R01.9:** resolver primero R018-01..03: invalidación atómica, envelope por unidad y contrato/proof binding.
2. **R01.10:** cerrar catálogos y crosswalks canónicos: Evidence, Risk, SelectionDecision y RouteEligibility, incluyendo estados legacy.
3. **R01.11:** cerrar máquinas de estado, transiciones, lifecycle/workflow y veto release.
4. **R01.12:** cerrar authority/SoD y consumer-specific freshness.
5. **R01.13:** auditoría transversal de remediaciones antes de cualquier aprobación semántica o schema freeze.

No crear schemas/código mientras los catálogos, mappings y owners sigan en disputa.

## 5. Criterios de salida

- Todos los HIGH cerrados con decisiones aprobadas y trazabilidad a fuente.
- No hay catálogo doble para el mismo objeto/versión.
- Cada estado legacy tiene mapping o clasificación explícita como no migrable.
- ValidationEnvelope tiene binding atómico y proof verificable.
- La invalidación parcial no bloquea ni habilita fuera de causalidad probada.
- Las transiciones de veto incluyen revisión y autoridad independiente.
- X-08 asigna autoridades reales y SoD comprobable.
- Escenarios adversariales se convierten en tests ejecutables y se ejecutan en CI.
- Revisión independiente documentada.

## 6. Límites

Auditoría estática de documentos versionados. No se ejecutaron tests, property-based tests, pruebas formales, pruebas de concurrencia, RPC, validación empírica ni revisión independiente externa. No se modificaron documentos canónicos, requisitos IIM, schemas, código, fixtures o tests.

**Estado final:** REVIEW_REQUIRED / INTEGRATION_REVISE / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED.
