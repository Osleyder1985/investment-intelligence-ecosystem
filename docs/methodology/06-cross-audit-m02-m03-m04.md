# Auditoría cruzada de M-02, M-03 y M-04

**ID:** IIE-METH-CROSS-AUDIT-001  
**Versión:** 0.1.0-draft  
**Fecha:** 2026-10-31  
**Estado:** REVIEW_FINDINGS / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED  
**Documentos examinados:**  
- `docs/methodology/03-canonical-decision-state-model.md` (M-02)
- `docs/methodology/04-analytical-vs-operational-eligibility.md` (M-03)
- `docs/methodology/05-risk-assessment-domain-status-and-veto-decision.md` (M-04)
- `docs/decision/decision-gates-and-outcomes.md`
- `docs/risk/veto-policy.md`
- `docs/methodology/02-methodology-consistency-audit.md`

## 1. Objetivo

Evaluar la coherencia semántica, de estados, scope, precedencia, interfaces y gobernanza entre M-02, M-03 y M-04. La auditoría es documental y estática. No ejecuta software, pruebas, backtests ni validación empírica. No constituye revisión independiente externa.

## 2. Conclusión ejecutiva

**Resultado: INTEGRATION_REVISE — no aprobar todavía.**

Los tres documentos comparten principios sólidos: scope explícito, desconocimiento no favorable, vetos no compensables, separación analítica/operativa, versionado e historial inmutable. Sin embargo, aún no forman un contrato semántico único. Se detectan divergencias concretas de vocabulario, estado no evaluado, propagación de vetos, ciclo de vida y composición de ruta.

M-04 mejora la separación conceptual al situar UNKNOWN en el estado del dominio de riesgo y distinguirlo de la decisión de veto. Pero M-02 y las políticas legacy aún no reflejan completamente esa decisión. No debe implementarse una máquina de estados a partir de estos documentos hasta resolver los hallazgos.

## 3. Hallazgos de integración

### X-01 — HIGH — RISK_UNKNOWN tiene dos propietarios semánticos

**Evidencia:** M-02 enumera RISK_UNKNOWN como estado de VetoDecision y lo incluye en su precedencia/transiciones. M-04 declara que RISK_UNKNOWN pertenece a RiskDomainStatus y propone VETO_NOT_ASSESSED cuando la política de veto no se ha evaluado.

**Riesgo:** consumidores distintos pueden interpretar RISK_UNKNOWN como incertidumbre de riesgo o como resultado de la política de veto. Esto impide validación de contratos y composición determinista.

**Resolución propuesta:** adoptar la separación de M-04: RISK_UNKNOWN solo en RiskDomainStatus; VETO_NOT_ASSESSED en VetoDecision. Actualizar M-02, gates y política de vetos mediante migración versionada. No hacer alias silencioso.

**Prioridad:** P0.

### X-02 — HIGH — Los estados de dominio de riesgo no están completamente sincronizados

**Evidencia:** M-02 incluye ASSESSED_WITH_FINDINGS, ASSESSED_NO_MATERIAL_FINDING, UNKNOWN, NOT_ASSESSED, STALE y CONFLICTED. M-04 añade ASSESSMENT_IN_PROGRESS e INVALID, y separa applicability. No hay declaración explícita de si la lista M-04 amplía o reemplaza la lista M-02.

**Riesgo:** productores y consumidores pueden rechazar estados válidos o convertir estados no contemplados en defaults peligrosos.

**Resolución propuesta:** establecer un único catálogo canónico RiskDomainStatus. Aclarar si ASSESSMENT_IN_PROGRESS pertenece al lifecycle del dominio y cómo se representan INVALID, STALE y CONFLICTED. Definir transiciones y precedencia sin mezclar estado, aplicabilidad, severidad o calidad de evidencia.

**Prioridad:** P0.

### X-03 — HIGH — VETO_NOT_ASSESSED no está integrado en los gates ni en las políticas legacy

**Evidencia:** M-04 propone VETO_NOT_ASSESSED; decision-gates y veto-policy aún usan RISK_UNKNOWN como estado agregado de riesgo/veto. M-02 tampoco contiene el nuevo estado.

**Riesgo:** una implementación puede tratar un veto no evaluado como desconocimiento genérico o, peor, permitir NO_VETO_IDENTIFIED por ausencia de registro.

**Resolución propuesta:** definir tabla de transición/migración por dominio: RiskDomainStatus.UNKNOWN/NOT_ASSESSED y VetoDecision.VETO_NOT_ASSESSED son campos independientes. DECISION bloquea conclusiones dependientes de dominios materiales no evaluados; no fabrica una VetoDecision favorable.

**Prioridad:** P0.

### X-04 — HIGH — Vocabulario ORE y RouteStatus no tiene una correspondencia canónica completa

**Evidencia:** M-02 utiliza ROUTE_INELIGIBLE, ROUTE_REVIEW_REQUIRED, ROUTE_UNKNOWN y ROUTE_STALE. M-03 utiliza ROUTE_INSUFFICIENT_INFORMATION, ROUTE_ELIGIBLE_WITH_LIMITATIONS y ROUTE_ELIGIBLE, además de estados de assessments de componentes. No se presenta una tabla exhaustiva de equivalencias, exclusividad y precedencia.

**Riesgo:** un consumidor puede interpretar UNKNOWN e INSUFFICIENT_INFORMATION como estados diferentes, o no reconocer una ruta elegible con limitaciones.

**Resolución propuesta:** definir un RouteEligibilityStatus canónico, distinguirlo de estados de componentes y de control transaccional. Incorporar estados de suficiencia/frescura sin colapsar información. Definir precedencia para veto, revisión, insuficiencia, inelegibilidad y elegibilidad condicionada.

**Prioridad:** P0.

### X-05 — HIGH — La composición de DECISION no define con precisión los bloqueos por finalidad frente a los bloqueos por ruta

**Evidencia:** M-03 separa AE y ORE; M-04 permite que un veto afecte una finalidad/actividad/ruta concreta; M-02 contiene SelectionDecision y estados de ruta, pero no formaliza en un único contrato qué gates bloquean la tesis, cuáles bloquean una actividad y cuáles bloquean ambas.

**Riesgo:** una incidencia de retiro/custodia puede bloquear indebidamente research, o una tesis favorable puede interpretarse como autorización operativa.

**Resolución propuesta:** una matriz única `VetoRule × purpose/activity × scope × consequence`, consumida por DECISION y ORE. Mantener AE, ORE y TradeAuthorization independientes. No inferir propagación entre actividades.

**Prioridad:** P0.

### X-06 — HIGH — Falta un algoritmo determinista de composición con causas múltiples

**Evidencia:** los documentos establecen precedencias por separado, pero no definen un único algoritmo de agregación que conserve causas primarias/secundarias, scopes parciales, dominios no aplicables y bloqueos concurrentes.

**Riesgo:** distintos consumidores pueden producir resultados diferentes para el mismo snapshot de evidencia.

**Resolución propuesta:** especificar un DecisionCompositionPolicy versionado con: conjunto de inputs obligatorios, validación de scope/versión/frescura, matriz de precedencia, reglas de bloqueo por finalidad, causas primarias y secundarias, abstención y registro de dependencias. No usar score compensatorio.

**Prioridad:** P0.

### X-07 — MEDIUM — Lifecycle, freshness e invalidación carecen de un contrato transversal

**Evidencia:** M-02, M-03 y M-04 describen lifecycle y reevaluación, pero no comparten una máquina de eventos ni un dependency map común. M-04 establece que el TTL vencido no levanta un veto activo, principio que debe ser uniforme.

**Riesgo:** estados obsoletos podrían persistir o propagarse de forma inconsistente; un evento podría invalidar un módulo y no otro dependiente.

**Resolución propuesta:** definir un contrato transversal de EvaluationRecord, Event/Invalidation y Supersession; políticas TTL por dominio y finalidad; dependencias explícitas; conservación del veto hasta cierre validado.

**Prioridad:** P1.

### X-08 — MEDIUM — Responsabilidades y segregación de funciones incompletas

**Evidencia:** M-03 y M-04 proponen roles conceptuales; no hay RACI consolidado para productores de evidencia, evaluadores, aprobadores de veto, revisores independientes, DECISION y enforcement ORE.

**Riesgo:** conflicto de interés, autoaprobación o modificación de resultados fuente por consumidores.

**Resolución propuesta:** matriz RACI/SoD que indique quién produce, revisa, aprueba, consume, ejecuta enforcement y puede levantar bloqueos. El motor DECISION no puede editar assessments de Risk.

**Prioridad:** P1.

### X-09 — MEDIUM — No existe matriz de conformidad de escenarios integrada

**Evidencia:** cada documento incluye casos de aceptación, pero no una suite común que pruebe combinaciones AE × ORE × RiskDomainStatus × VetoDecision × DecisionOutcome.

**Riesgo:** contradicciones emergen en combinaciones límite que los casos individuales no cubren.

**Resolución propuesta:** diseñar matriz de escenarios positivos/negativos, tabla de decisión y después fixtures de conformidad. No crear pruebas ejecutables hasta aprobar la semántica.

**Prioridad:** P1.

## 4. Matriz de compatibilidad actual

| Concepto | M-02 | M-03 | M-04 | Resultado |
|---|---|---|---|---|
| AE por finalidad | Elegibilidad por finalidad | Definición explícita | Scope de riesgo | Compatible; contrato de composición pendiente |
| ORE por ruta | Estados Route | Definición explícita | Propagación por actividad | Parcial; X-04 |
| UNKNOWN de riesgo | RiskDomainStatus y VetoDecision | Fail-closed en controles | Solo RiskDomainStatus | Conflicto; X-01/X-03 |
| Veto no evaluado | No separado claramente | UNKNOWN en controles de ruta | VETO_NOT_ASSESSED | Conflicto; X-03 |
| Estado de riesgo por dominio | Catálogo básico | Referencia a riesgos | Catálogo ampliado | Parcial; X-02 |
| Veto activo | VETO_ACTIVE | No compensable y scoped | VETO_ACTIVE | Compatible |
| No veto identificado | Cobertura requerida | Requiere scope | Cobertura suficiente | Compatible en principio |
| Scope | Objeto/finalidad/horizonte | AE/ORE diferenciados | Scope de riesgo/veto | Compatible; composición pendiente |
| Freshness | STALE | TTL y eventos | TTL y no-lifting por tiempo | Parcial; X-07 |
| Segregación | Aprobación por owners | Roles conceptuales | Roles de riesgo/revisión | Parcial; X-08 |

## 5. Modelo integrado objetivo (propuesta)

No crear un estado global de activo. Mantener objetos y estados ortogonales:

1. `EvaluationRecord`: ciclo de vida y snapshot/versiones.
2. `IdentityAssessment`: identidad y representación.
3. `EvidenceAssessment`: admisibilidad/frescura por dato y uso.
4. `ApplicabilityAssessment`: APPLICABLE / NOT_APPLICABLE / APPLICABILITY_UNRESOLVED / APPLICABILITY_CONFLICTED.
5. `AnalyticalEligibilityDecision` (AE): elegibilidad por finalidad.
6. `RiskAssessment`: expediente de riesgo.
7. `RiskDomainAssessment`: hallazgos y RiskDomainStatus por dominio.
8. `VetoDecision`: decisión por regla y scope.
9. `OperationalRouteEligibilityDecision` (ORE): elegibilidad por actividad y ruta.
10. `SelectionDecision`: conclusión analítica y abstención, con referencias a los objetos anteriores.
11. `TradeAuthorization` y controles de Portfolio & Execution: procesos independientes de autorización y enforcement.

El catálogo canónico debe residir en un artefacto de vocabularios/políticas con versionado. Los documentos especializados referencian ese catálogo, no redefinen enums divergentes.

## 6. Reglas integradas propuestas

1. Validar identidad, finalidad, scope, versiones, timestamps y evidence manifest antes de componer resultados.
2. Si identidad no está resuelta, bloquear solo las conclusiones que dependan de esa identidad.
3. Si un dominio material de riesgo está UNKNOWN/NOT_ASSESSED/STALE/CONFLICTED/INVALID, bloquear conclusiones dependientes; no convertirlo en bajo riesgo.
4. VETO_ACTIVE aplicable bloquea el scope declarado y prevalece sobre cualquier score.
5. VETO_REVIEW_REQUIRED aplicable suspende conclusión favorable dentro del scope.
6. VETO_NOT_ASSESSED no equivale a NO_VETO_IDENTIFIED.
7. NO_VETO_IDENTIFIED requiere que todas las reglas/domínios materiales aplicables estén suficientemente evaluados.
8. Un bloqueo ORE no cambia AE; un AE favorable no habilita ORE.
9. ORE favorable no crea TradeAuthorization ni pasa controles pre/post-trade.
10. Ningún consumidor modifica estados fuente; una discrepancia se devuelve al productor para reconciliación.
11. Todas las causas concurrentes se conservan; la causa primaria de presentación no elimina las secundarias.
12. Reevaluaciones generan nuevos registros; expiración/TTL no levanta vetos.

## 7. Secuencia recomendada de resolución

**P0 — Antes de schemas o motor:**
1. Resolver X-01/X-03: separar UNKNOWN de riesgo y VETO_NOT_ASSESSED.
2. Resolver X-02: catálogo único RiskDomainStatus y applicability.
3. Resolver X-04: catálogo RouteEligibilityStatus.
4. Resolver X-05: matriz de alcance/actividad/consecuencia.
5. Resolver X-06: algoritmo de composición DECISION.

**P1 — Antes de conformidad y despliegue:**
6. Resolver X-07: lifecycle, freshness e invalidación transversal.
7. Resolver X-08: RACI/segregación.
8. Resolver X-09: matriz integrada de escenarios y pruebas.

## 8. Decisión de auditoría

**INTEGRATION_REVISE.** Los documentos M-02, M-03 y M-04 son compatibles en sus principios, pero no están listos para consolidarse como contratos de software. Deben corregirse vocabularios, composición y propagación por scope antes de crear schemas de máquina de estados o implementar DECISION/ORE.

## 9. Limitaciones

Revisión documental asistida sobre los archivos indicados en el repositorio y sus versiones recuperadas el 2026-10-31. No se ejecutó código, validación de schemas, pruebas de conformidad, backtesting, revisión legal, auditoría de seguridad ni revisión independiente externa. Los hallazgos requieren revisión humana por los responsables de riesgo, arquitectura, operaciones y compliance. Este documento no aprueba ni modifica requisitos IIM.
