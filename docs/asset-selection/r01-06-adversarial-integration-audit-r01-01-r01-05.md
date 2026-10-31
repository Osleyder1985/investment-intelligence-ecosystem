# R01.6 — Auditoría adversarial de integración R01.1–R01.5

**ID:** IIE-IDENTITY-R01-06  
**Versión:** 0.1.0-draft  
**Fecha:** 2026-10-31  
**Estado:** REVIEW_REQUIRED / INTEGRATION_REVISE / NOT_APPROVED  
**Alcance:** auditoría estática de R01.1–R01.5 contra el modelo canónico de estados, Lifecycle, Scope Algebra, X-06, D-01..D-10 y el inventario X-09.  
**Issue:** #61

## 1. Dictamen ejecutivo

R01.1–R01.5 mejoran la separación conceptual entre identidad, freshness, admisibilidad, elegibilidad, workflow, lifecycle y validez contractual. Sin embargo, **no existe todavía una especificación integrada suficientemente consistente para aprobación o schema freeze**.

La revisión identifica 12 hallazgos: 8 HIGH y 4 MEDIUM. Algunos son contradicciones documentales confirmadas; otros son interfaces aún incompletas que impiden demostrar interoperabilidad. No se afirma que el sistema permita actualmente una decisión favorable indebida: los documentos son propuestas y no existe un compositor implementado que pueda ser probado. El riesgo es que una implementación basada en una selección parcial de documentos interprete estados incompatibles.

**Dictamen:** REVIEW_REQUIRED / INTEGRATION_REVISE / NOT_APPROVED.

## 2. Hallazgos

### R016-01 — IdentityStatus canónico todavía contiene STALE
**Severidad:** HIGH  
**Tipo:** contradicción documental confirmada; pendiente de migración canónica.

- **Evidencia:** modelo canónico 03-canonical-decision-state-model.md, §4.2, conserva RESOLVED, UNRESOLVED, CONFLICTED y STALE; también §§5 y 8 tratan identidad obsoleta. R01.1 §§1–3 y D-02 proponen retirar STALE de IdentityStatus y asignarlo exclusivamente a FreshnessStatus.
- **Contradicción:** los documentos de resolución exigen un vocabulario de identidad de tres estados mientras que el documento canónico aún autoriza cuatro.
- **Impacto:** dos productores o consumers pueden interpretar STALE como identidad no resuelta o como freshness vencida. Un mapping mecánico puede bloquear o permitir usos incorrectos.
- **Remediación:** aprobar formalmente la separación, actualizar el modelo canónico y su tabla de mapping, inventariar todos los usos legacy y definir migración por evidencia; no convertir STALE automáticamente.
- **Estado:** OPEN; no cerrado por R01.1 porque el documento canónico no se modificó.

### R016-02 — D-10 conserva vocabularios incompatibles con R01.1–R01.4
**Severidad:** HIGH  
**Tipo:** contradicción documental confirmada.

- **Evidencia:** D-10 §3.4 incluye Freshness UNKNOWN; su workflow incluye NOT_ASSESSED y no incluye NOT_STARTED; su PurposeAdmissibility no incluye NOT_ADMISSIBLE. R01.1 §§1–4 prohíbe UNKNOWN como FreshnessStatus; R01.3 §3.2 sitúa NOT_ASSESSED en proceso; R01.4 §2 define NOT_STARTED/IN_PROGRESS/REVIEW_REQUIRED/COMPLETE; R01.2 §3.1 propone NOT_ADMISSIBLE.
- **Contradicción:** D-10 es una de las decisiones de integración base, pero no refleja las resoluciones posteriores. R01.5 incluye NOT_ADMISSIBLE como propuesto, pero no resuelve la versión divergente de D-10.
- **Impacto:** los productores y consumers pueden aceptar/rechazar payloads diferentes según el documento de referencia. Un enum implementado desde D-10 admitiría Freshness UNKNOWN y podría no reconocer estados necesarios.
- **Remediación:** emitir una versión consolidada de D-10 con historial de decisiones y mappings explícitos; retirar los enums obsoletos solo después de aprobación.
- **Estado:** OPEN; la precedencia temporal de documentos no constituye aprobación.

### R016-03 — NOT_ASSESSED sigue dentro de ClaimResolutionStatus
**Severidad:** HIGH  
**Tipo:** contradicción semántica confirmada.

- **Evidencia:** D-04 §3.5 enumera NOT_ASSESSED dentro de ClaimResolutionStatus; R01.3 §§1, 2.2 y 3.2 define NOT_ASSESSED como estado de proceso y distingue UNKNOWN como resultado de evaluación válida. R01.3 §5.5 vuelve a incluir NOT_ASSESSED en la lista de resultados de ClaimResolution.
- **Contradicción:** el documento define que NOT_ASSESSED no es una conclusión epistemológica, pero lo mantiene como miembro del vocabulario de resolución del claim.
- **Impacto:** consumers pueden tratar la ausencia de evaluación como un resultado de claim, o convertirla en UNKNOWN sin un contrato de adaptación. Se pierde la distinción entre “no se ejecutó” y “se ejecutó, pero no se pudo resolver”.
- **Remediación:** separar ResolutionOutcome de AssessmentWorkflowStatus. Definir cómo se representa un claim sin ResolutionRecord; si un envelope de resolución debe existir antes de evaluación, mantener su workflow separado y sin domain outcome. Retirar NOT_ASSESSED del enum de resolución solo mediante decisión aprobada y migración.
- **Estado:** OPEN.

### R016-04 — INVALID aparece simultáneamente como validez contractual y estado de dominio
**Severidad:** HIGH  
**Tipo:** contradicción documental confirmada.

- **Evidencia:** R01.3 §§1, 2 y 3.4 establece INVALID como error contractual, separado de los dominios factual/lógico. Sin embargo, D-10 §3.4 y R01.5 §3 incluyen INVALID en ApplicabilityAssessment; el modelo canónico §4.5 incluye INVALID en RiskDomainStatus; X-06 §§5.2 y 6.1 usa RISK_INVALID.
- **Contradicción:** INVALID se declara transversalmente fuera de los outcomes de dominio, pero algunos catálogos de dominio lo incluyen como estado.
- **Impacto:** no queda claro si INVALID rechaza el objeto completo, invalida solo el resultado de un dominio o permite continuar con otros inputs. Una implementación puede degradar un error de contrato a un hallazgo de riesgo o a una aplicabilidad no resuelta.
- **Remediación:** separar ContractValidity de DomainAssessmentValidity/MethodValidity, si esta última se necesita. Definir objeto, productor, alcance y efecto de cada error. El término INVALID no debe aparecer como outcome factual de Applicability/Risk sin un contrato de validez distinto.
- **Estado:** OPEN; requiere decisión transversal y revisión de Architecture.

### R016-05 — NOT_ADMISSIBLE no está integrado en D-10 ni completamente en reglas de composición
**Severidad:** HIGH  
**Tipo:** divergencia de vocabulario y contrato consumidor incompleto.

- **Evidencia:** R01.2 §3.1 propone NOT_ADMISSIBLE para una obligación de propósito demostrada como incumplida con evidencia suficiente; R01.5 §3 lo incluye expresamente como propuesto. D-10 §3.4 omite ese estado y §3.6 describe composición sin fijar el outcome para incumplimiento probado de obligación.
- **Contradicción:** el productor PA y sus consumidores no tienen un vocabulario y mapping único aprobado.
- **Impacto:** una obligación incumplida podría representarse como INSUFFICIENT_EVIDENCE, CONFLICTED o ausencia genérica de ADMISSIBLE, perdiendo la diferencia entre desconocimiento y incumplimiento demostrado. El mapping hacia INELIGIBLE podría ser demasiado permisivo o demasiado amplio.
- **Remediación:** aprobar NOT_ADMISSIBLE o alternativa inequívoca; definir el mapping condicionado a criterio de EA obligatorio, preservando scope, evidence y policy. Actualizar D-10 y todos los consumer contracts.
- **Estado:** OPEN.

### R016-06 — EvidenceStatus y ClaimEvidenceAssessment no tienen un contrato de vocabulario común
**Severidad:** HIGH  
**Tipo:** interfaz productora/consumidora incompleta.

- **Evidencia:** modelo canónico §4.3 define EvidenceStatus con ADMISSIBLE, ADMISSIBLE_WITH_LIMITATIONS, QUARANTINED, REJECTED, UNKNOWN, STALE y NOT_ASSESSED. D-05 §2.5 define ClaimEvidenceAssessment con ADMISSIBLE, ADMISSIBLE_WITH_LIMITATIONS, INSUFFICIENT, QUARANTINED, REJECTED, NOT_ASSESSED e INVALID contractual. R01.5 §3 presenta un “Evidence assessment” que mezcla/adopta solo parte de estos valores y no identifica si habla de EvidenceRecord, SourceAssessment o ClaimEvidenceAssessment.
- **Contradicción/interfaz:** no hay un contrato explícito que establezca si STALE se elimina de EvidenceStatus porque ahora es Freshness, ni cómo se separan insuficiencia probatoria, admisibilidad, validez contractual y freshness en ClaimEvidenceAssessment.
- **Impacto:** una evidencia rechazada, insuficiente, no evaluada o stale puede ser consumida bajo semánticas distintas; además, el consumidor puede confundir evidencia no admisible con claim falso.
- **Remediación:** publicar una matriz de objetos y estados: EvidenceRecord lifecycle/integrity; SourceAssessment; ClaimEvidenceAssessment admissibility/sufficiency; FreshnessAssessment; ClaimResolution. Definir mappings por purpose y no un enum agregado “Evidence assessment”.
- **Estado:** OPEN.

### R016-07 — Workflow NOT_ASSESSED y AssessmentWorkflowStatus no están cerrados entre sí
**Severidad:** HIGH  
**Tipo:** ambigüedad de modelado con impacto en completitud.

- **Evidencia:** R01.3 §3.2 llama NOT_ASSESSED estado de proceso; R01.4 §2 define AssessmentWorkflowStatus con NOT_STARTED, IN_PROGRESS, REVIEW_REQUIRED y COMPLETE, y §7 dice que NOT_ASSESSED no forma parte del workflow; R01.1 §2.3 usa EvaluationLifecycle para progreso/completitud.
- **Contradicción/ambigüedad:** R01.3 presenta NOT_ASSESSED como “no ejecutado/no concluido”, mientras R01.4 usa NOT_STARTED para un assessment creado pero aún no iniciado y afirma que NOT_ASSESSED puede ser un outcome/código del consumer. No se define un único envelope que explique cómo conviven ausencia de assessment, assessment creado sin iniciar y evaluación ejecutada sin outcome.
- **Impacto:** riesgo de duplicar NOT_ASSESSED en workflow y domain outcome, o de fabricar un outcome de claim inexistente. También puede declararse COMPLETE con campos de outcome ausentes.
- **Remediación:** definir estados de existencia/creación del AssessmentRecord, workflow de un assessment existente y outcome del dominio como tres conceptos explícitos; especificar obligatoriedad condicional por fase. Prohibir domain outcome cuando no hay ResolutionRecord.
- **Estado:** OPEN.

### R016-08 — Vocabularios de riesgo, veto y ruta siguen dependiendo de catálogos no reconciliados
**Severidad:** HIGH  
**Tipo:** interfaz incompleta.

- **Evidencia:** modelo canónico §4.5 define RiskDomainStatus; §4.6 define VetoDecision y su agregación. X-06 §§5.2, 6.1 y 7 define RouteEligibilityStatus, RISK_* y reglas de VetoDecision. R01.5 §3 solo remite a “catálogo canónico por dominio” y declara el catálogo de veto sujeto al modelo canónico; la matriz no fija el vocabulario exacto de RouteEligibility ni todos los estados de riesgo.
- **Contradicción/interfaz:** R01.5 se presenta como matriz canónica de productores/consumidores, pero deja sin cerrar los enums exactos y su correspondencia con X-06. Además, INVALID aparece en RiskDomainStatus aunque R01.3 lo reserva a validez contractual.
- **Impacto:** el motor puede mapear estados de riesgo y ruta con traducciones no deterministas o incompatibles; un riesgo no evaluado podría no propagarse correctamente a veto/selección.
- **Remediación:** anexar tabla exacta producer → schema/domain enum → consumer mapping → consequence para RiskDomainStatus, VetoDecision, SelectionDecision y RouteEligibilityStatus, incluyendo no mappings y reason codes.
- **Estado:** OPEN.

### R016-09 — Falta un binding obligatorio y verificable entre ValidationEnvelope, Scope Algebra y composición
**Severidad:** HIGH  
**Tipo:** interfaz de control incompleta.

- **Evidencia:** X-06 §§4.3 y 8 exige resultado de validación VALID/INCOMPLETE/REVIEW_REQUIRED/REJECTED_INVALID_INPUT antes de componer; Scope Algebra §11 establece que solo ValidationEnvelope VALID permite agregación y que la precedencia X-06 aplica después de VALID; R01.5 §§2 y 7 exige scope válido/particionado y dice que su secuencia no es una precedencia total, pero no especifica un objeto/envelope normativo común ni enlaza cada gate a su estado de validación y proof artifact.
- **Contradicción/omisión:** los documentos describen gates similares, pero no establecen el contrato único que impide invocar X-06 si la partición exacta o cierre de dependencias no ha sido demostrada.
- **Impacto:** un compositor podría tratar “scope validado” como booleano local y ejecutar precedencia sobre un scope incompleto o una partición no probada.
- **Remediación:** definir ValidationEnvelope canónico con ContractValidity, ScopeResolutionResult, exact partition proof, dependency closure, completeness, versions/digests y estados permitidos. Hacer obligatorio VALID como precondición de la composición X-06; prohibir DecisionOutcome en cualquier otro estado.
- **Estado:** OPEN.

### R016-10 — Invalidación parcial y lifecycle terminal no tienen representación integrada por átomo
**Severidad:** HIGH  
**Tipo:** ambigüedad de alcance con impacto de propagación.

- **Evidencia:** R01.4 §§3 y 6.1 declara SUPERSEDED/INVALIDATED terminales por versión y exige que invalidaciones parciales creen un registro de impacto/scope derivado o una versión de estado particionada. Scope Algebra §§7–9 exige partición exacta y decisiones separadas por scope derivado. R01.5 §§4.1 y 6 exige propagación limitada al scope probado, pero su matriz de lifecycle presenta un RecordLifecycleStatus por registro y no define el binding entre record version, atom scopes e impacto parcial.
- **Ambigüedad:** no está decidido si la invalidación parcial invalida el registro completo y crea registros sustitutos para regiones no afectadas, o si un mismo record/version puede tener lifecycle segmentado por átomo.
- **Impacto:** puede producirse invalidación global innecesaria o, en el extremo opuesto, consumo de una región afectada bajo un record marcado ACTIVE.
- **Remediación:** decidir un patrón único: (A) lifecycle terminal del registro completo más nuevos records derivados para regiones no afectadas/afectadas, o (B) estados de impacto por atom scope separados del lifecycle global. Definir claves, lineage, consumers y replay.
- **Estado:** OPEN; no debe resolverse mediante un enum adicional improvisado.

### R016-11 — Orden de gates R01.5 no está formalmente conectado a la precedencia de X-06
**Severidad:** MEDIUM  
**Tipo:** ambigüedad de composición.

- **Evidencia:** R01.5 §7 enumera gates de elegibilidad antes de risk/veto y aclara que no es precedencia total; el modelo canónico §5 y X-06 §§7–8 definen precedencia de outcomes y reglas específicas para veto/riesgo. Scope Algebra §11 limita la precedencia a ValidationEnvelope VALID.
- **Impacto:** un implementador puede confundir orden de evaluación, bloqueo temprano y selección de causa primaria. Esto afecta especialmente coexistencia de INELIGIBLE, VETO_ACTIVE, RISK_INCOMPLETE y REVIEW_REQUIRED.
- **Remediación:** publicar un diagrama/tabla de tres fases distintas: (1) validación de inputs y scope; (2) evaluación de dominios; (3) agregación/precedencia X-06. Especificar que todas las causas aplicables se conservan y que la precedencia solo selecciona causa primaria después del envelope VALID.
- **Estado:** OPEN.

### R016-12 — Autoridad y SoD para lifecycle y mappings siguen sin asignación aprobada
**Severidad:** MEDIUM  
**Tipo:** dependencia de gobernanza no cerrada.

- **Evidencia:** R01.5 §9 reserva autoridades, adjudicación y excepciones a X-08; R01.4 §§3 y 6 requiere autoridad para invalidation/withdrawal y transiciones terminales; X-08 (13-governance-raci-and-segregation-of-duties.md) continúa como propuesta sin roles reales ni aprobaciones.
- **Impacto:** el contrato define quién debería producir/consumir conceptualmente, pero no quién puede emitir, validar, adjudicar, invalidar, retirar, aprobar excepciones o reabrir revisiones. No puede probarse segregación de funciones.
- **Remediación:** resolver autoridad y SoD antes de habilitar transiciones sensibles; asociar cada transición y mapping crítico a un role/permission, independencia requerida, delegación y audit event. No asignar autoridad por inferencia.
- **Estado:** OPEN.

## 3. Comprobaciones de seguridad semántica

La auditoría confirma que las propuestas contienen protecciones correctas que deben conservarse:

1. UNKNOWN no es FALSE, EMPTY, NOT_APPLICABLE ni NOT_ASSESSED.
2. NOT_ASSESSED no debe convertirse en UNKNOWN en el registro productor.
3. INVALID contractual bloquea la composición dependiente.
4. NOT_ADMISSIBLE no es inelegibilidad global; solo puede contribuir a INELIGIBLE si EA lo mapea a un criterio obligatorio.
5. STALE no invalida automáticamente identidad factual ni levanta/crea un veto.
6. COMPLETE no implica outcome favorable, ACTIVE ni FRESH.
7. ACTIVE no implica COMPLETE ni ContractValidity=VALID.
8. SUPERSEDED, INVALIDATED y WITHDRAWN no son consumibles como actuales.
9. UNKNOWN en scope/dependency no equivale a NOT_AFFECTED; falta de edge no demuestra independencia.
10. La partición debe cubrir exactamente requested_scope y ser pairwise disjoint; residual no significa elegible ni libre de veto.
11. VETO_ACTIVE aplicable no es compensable por score.
12. ANALYTICAL_SELECTION no habilita OPERATIONAL_ROUTE ni ejecución.

Estas salvaguardas son reglas documentales propuestas, no controles ejecutables verificados.

## 4. Compatibilidad con Scope Algebra, X-06 y X-08

- **Scope Algebra:** compatible en principios de no wildcard, pertenencia demostrada, incertidumbre conservadora, scopes derivados y partición exacta. Sigue abierto el binding del ValidationEnvelope y la representación de invalidación parcial.
- **X-06:** compatible en separación de decisiones, composición determinista y no compensabilidad. Falta cerrar el contrato de precondición y separar con precisión gate order, domain evaluation y outcome precedence.
- **X-08:** R01.5 no crea autoridad por sí sola, correctamente; sin embargo, la falta de RACI/SoD aprobado impide habilitar transiciones de invalidación, withdrawal, adjudicación y excepciones.
- **X-09:** los escenarios documentales no son pruebas ejecutables. Las invariantes de partición, dependencias, concurrencia y replay deben convertirse en fixtures/tests antes de declarar conformidad.

## 5. Secuencia de remediación recomendada

### R01.7 — Cierre de compatibilidad semántica

Resolver, en este orden:

1. Aprobar matriz de dominios y propietarios: ContractValidity, AssessmentWorkflowStatus, RecordLifecycleStatus, Freshness, IdentityStatus, Applicability, ClaimResolution, Evidence/ClaimEvidence, PurposeAdmissibility, Eligibility, Risk, Veto, Selection y Route.
2. Resolver NOT_ASSESSED: ausencia de assessment vs workflow NOT_STARTED vs outcome ausente.
3. Resolver INVALID: contractual vs method/domain validity.
4. Cerrar vocabularios exactos de EvidenceRecord, SourceAssessment, ClaimEvidenceAssessment y ClaimResolution.
5. Aprobar NOT_ADMISSIBLE y sus mappings condicionados.
6. Reconciliar D-10 y el modelo canónico 03 con R01.1–R01.5.
7. Definir ValidationEnvelope único y su binding obligatorio con X-06.
8. Resolver invalidación parcial por atom scope.
9. Cerrar matrices Risk/Veto/Selection/Route y precedencia de composición.
10. Obtener aprobación de autoridades X-08 y SoD para transiciones sensibles.

### R01.8 — Auditoría adversarial posterior

Después de R01.7, ejecutar una nueva auditoría transversal R01.1–R01.7 y verificar que cada hallazgo tiene:
- decisión arquitectónica explícita;
- productor y consumer definidos;
- mapping versionado;
- transición y combinación compatible;
- scope/temporalidad/dependency contract;
- autoridad y SoD;
- escenarios negativos y positivos;
- trazabilidad con X-09.

Solo después de ese cierre se recomienda congelar contratos y preparar schemas/fixtures/tests. La aprobación documental no equivale a implementación ni validación.

## 6. Límites de la auditoría

- Método: inspección estática de documentos del repositorio y referencias cruzadas.
- No se ejecutaron tests automatizados, property-based, formales, de concurrencia ni empíricos.
- No se modificaron requisitos IIM, documentos canónicos, schemas, código, fixtures ni tests.
- No se aprobaron estados, políticas, autoridades ni excepciones.
- No se declara conformidad ISO ni certificación.
