# Registro de revisión arquitectónica — Issue #33

**Fecha:** 2026-10-31  
**Repositorio:** `Osleyder1985/investment-intelligence-ecosystem`  
**Rama revisada:** `main`  
**Issue:** [#33 — Revisar asignación de requisitos IIM e interfaces](https://github.com/Osleyder1985/investment-intelligence-ecosystem/issues/33)  
**Estado de revisión:** REVISION_REQUIRED  
**Tipo de revisión:** revisión técnica-documental asistida; no independiente.

## 1. Alcance y evidencia consultada

Se revisaron:
- `docs/architecture/strategic-architecture.md`;
- `docs/architecture/requirement-allocation-and-layer-contracts.md`;
- `requirements/requirements-register.md`;
- `docs/traceability/requirements-traceability-matrix.md`;
- `docs/traceability/dependency-and-gap-register.md`;
- `docs/traceability/evidence-and-status-criteria.md`;
- `docs/traceability/issue-19-cross-document-review.md`;
- Issue #19 y su descripción de estado;
- PR #32 y sus declaraciones de verificación.

La revisión se limita al contenido del repositorio y metadatos de GitHub accesibles. No incluye checkout/ejecución local, análisis de seguridad, revisión legal, revisión de proveedores, ni revisión independiente por especialista externo.

## 2. Hallazgos

### AR-33-01 — Responsabilidades transversales asignadas como si fueran exclusivas de una capa

**Severidad:** Media  
**Estado:** Corrección documental propuesta en `requirement-allocation-and-layer-contracts.md`.

IIM-009 (no ejecutar análisis sin solicitud explícita) e IIM-024 (orquestación bajo demanda, estados, gates, idempotencia, recuperación y observabilidad) tienen alcance transversal. Asignarlos a L3 exclusivamente crea una falsa frontera: los controles deben proteger los puntos de entrada y procesos de todas las capas.

IIM-008 (historial versionado) también atraviesa el ciclo completo, aunque VALIDATION & LEARNING deba consumirlo y verificar su integridad.

**Acción:** asignar IIM-008, IIM-009 e IIM-024 a X (transversal), con colaboración de las capas pertinentes.

### AR-33-02 — Responsabilidad de evaluación de riesgo insuficientemente separada de la decisión

**Severidad:** Media  
**Estado:** Corrección documental propuesta.

IIM-016 requiere evaluar riesgos por tipología, evidencia, alcance, controles y riesgo residual. La generación del perfil analítico pertenece principalmente a INTELLIGENCE; DECISION debe consumirlo y aplicar consecuencias, gates y vetos. Asignar todo el requisito a DECISION puede mezclar medición del riesgo con autoridad decisional.

**Acción:** asignar IIM-016 a L1, con L2 como consumidor decisional, y conservar IIM-006/IIM-017 para evaluación de riesgo crítico y aplicación de vetos.

### AR-33-03 — Cobertura insuficiente de PORTFOLIO & EXECUTION en el registro de requisitos

**Severidad:** Alta  
**Estado:** Propuesta creada; requiere aprobación.

El registro IIM-001 a IIM-024 no define de forma atómica construcción de carteras, mandato, asignación, concentración, presupuestos de riesgo, capacidad/liquidez, costes, estrés, rebalanceo, venue/contraparte, ciclo de órdenes, controles pre/post-trade, custodia/secretos, conciliación ni aislamiento simulation/live.

**Acción:** propuesta de requisitos provisionales IIM-025 a IIM-037 en `portfolio-execution-requirements-proposal.md`. No se incorporan al registro oficial hasta revisión y aprobación.

### AR-33-04 — Interfaces aún conceptuales, no contratos de software

**Severidad:** Alta  
**Estado:** Pendiente por diseño.

IF-01 a IF-05 describen propósito, datos e invariantes, pero no contienen esquemas, cardinalidades, claves, serialización, compatibilidad, semántica completa de errores, versionado ni pruebas de conformidad. Además, la transición de una decisión a una cartera y de una cartera a órdenes necesita separar explícitamente propuesta, autorización y ejecución.

**Acción:** no declarar interfaces SPECIFIED. Derivar contratos técnicos después de cerrar los fundamentos y aprobar los requisitos complementarios. Separar lógicamente portfolio management y order execution aunque permanezcan dentro de la misma capa estratégica.

### AR-33-05 — Falta revisión independiente y cierre de dependencias fundacionales

**Severidad:** Alta  
**Estado:** Bloqueante para aprobación formal.

Issue #33 no contiene comentarios de revisión ni decisión registrada de un revisor independiente al momento de esta revisión. Issue #19 permanece abierto. Su descripción conserva IIM-001/IIM-019/IIM-020/IIM-018 como REVISION_REQUIRED e IIM-002 como BLOCKED para aprobar fuentes concretas. PR #32 declara expresamente que las pruebas no fueron ejecutadas y no existe CI configurada para esa suite.

**Acción:** mantener REVISION_REQUIRED; no cerrar Issue #33 ni elevar estados de requisitos. Cerrar los fundamentos de Issue #19 y obtener evidencia de pruebas/revisión según sus criterios.

## 3. Decisión de revisión

**REVISION_REQUIRED**

Razones:
1. Se encontraron asignaciones transversales que requieren corrección y aceptación.
2. El alcance Portfolio & Execution necesita requisitos complementarios revisados.
3. Las interfaces no están listas para declararse contratos técnicos aprobados.
4. No existe evidencia de revisión independiente ni se han cerrado las dependencias fundacionales.

Esta decisión no cuestiona la adopción estratégica de las cuatro capas. Determina que la especificación de asignación e interfaces aún no cumple criterios para aprobación técnica formal.

## 4. Acciones siguientes

| Orden | Acción | Evidencia para cierre |
|---|---|---|
| P0.3-A | Revisar las correcciones de asignación transversal | Aceptación/rechazo documentado por requisito y actualización de matriz. |
| P0.3-B | Revisar IIM-025 a IIM-037 | Decisión por requisito, duplicidades resueltas, prioridad y dependencias. |
| P0.3-C | Separar interfaces de decisión→cartera y cartera→ejecución | Contratos conceptuales revisados y responsabilidades/autoridades inequívocas. |
| P0.3-D | Resolver dependencias de Issue #19 y PR #32 | Revisión fundacional, pruebas ejecutadas y evidencia registrada; sin falsear resultados. |
| P0.3-E | Revisión técnica independiente | Revisor, competencia, versión, alcance, hallazgos y decisión trazables. |

## 5. No-conclusiones

- No se aprueba ningún requisito IIM por esta revisión.
- No se declara ninguna interfaz SPECIFIED.
- No se declara código IMPLEMENTED, pruebas TESTED ni capacidad EMPIRICALLY_VALIDATED por esta revisión.
- No se autoriza activar proveedores, custodiar activos ni ejecutar órdenes reales.
- No se emite certificación ni opinión legal.

**Conclusión:** la arquitectura estratégica de cuatro capas permanece como baseline adoptada. La asignación de requisitos necesita correcciones, la cobertura de Portfolio & Execution debe completarse y las interfaces deben evolucionar de límites conceptuales a contratos revisados antes de implementar componentes integrados.
