# Matriz de resolución de hallazgos INT-01 a INT-10

**Versión:** 0.1.0  
**Fecha:** 2026-10-31  
**Estado:** DRAFT_CROSSWALK / PENDING_HUMAN_REVIEW  
**Documento relacionado:** portfolio-execution-integrated-requirements-draft.md

Esta matriz indica dónde el borrador integrado aborda cada hallazgo. "Abordado en borrador" no significa que la decisión haya sido aprobada, implementada o probada. La aceptación final requiere revisión humana competente.

| Hallazgo | Tratamiento en el borrador integrado | Estado de resolución documental | Evidencia/sección |
|---|---|---|---|
| INT-01 Mandato, política, autorización y enforcement | Se separan InvestmentMandate, RiskPolicy, TradeAuthorization y TradeControlDecision. Se prohíbe inferir autorización desde mandato/propuesta. | ADDRESSED_IN_DRAFT; VALIDATION_PENDING | §§1, 4, 10, 14–15 |
| INT-02 Exposición, riesgo, estrés y enforcement | Se asignan funciones distintas a ExposureSnapshot, perfil IIM-016, escenarios IIM-030, política IIM-028 y controles IIM-034. | ADDRESSED_IN_DRAFT; VALIDATION_PENDING | §§3, 4, 6, 10, 14 |
| INT-03 Asignación inicial y rebalanceo | Se establece PortfolioProposal como contrato común; se mantiene lógica específica de rebalanceo. | PARTIALLY_ADDRESSED; SCHEMA_DETAIL_PENDING | §§2, 7, 14 |
| INT-04 Coste estimado y realizado | Se distingue estimación de coste realizado y se exige vinculación con órdenes/fills. La medición operativa detallada de slippage queda para especificación técnica. | PARTIALLY_ADDRESSED; SCHEMA_DETAIL_PENDING | §§5, 7, 9, 14 |
| INT-05 Venue eligibility y seguridad/custodia | Se mantienen VenueEligibilityDecision y controles de seguridad/custodia como dominios independientes. | ADDRESSED_IN_DRAFT; VALIDATION_PENDING | §§8, 11, 14 |
| INT-06 Orden, control y conciliación | Se separan OrderLifecycleEvent, TradeControlDecision y ReconciliationCase; workflow IIM-024 no sustituye estados externos. | ADDRESSED_IN_DRAFT; VALIDATION_PENDING | §§9–12, 14, 16 |
| INT-07 Kill switch, cancelación y liquidación | Se distinguen detener nuevas órdenes, cancelar órdenes abiertas y reducir/cerrar posiciones; reanudación requiere autorización. | ADDRESSED_IN_DRAFT; SEMANTICS_AND_TESTS_PENDING | §10, §15 |
| INT-08 Conciliación y finality | Se exige fuente de verdad por dato, snapshots/deltas, finality y discrepancias. Política específica por proveedor/red y tolerancias siguen pendientes. | PARTIALLY_ADDRESSED; POLICY_DETAIL_PENDING | §12, §14 |
| INT-09 Vocabulario de estados y UNKNOWN | Se prohíbe equiparar UNKNOWN/STALE/INVALID/CONFLICT con PASS o cero y se evita un enum global. Catálogos y transiciones por dominio aún deben especificarse. | PARTIALLY_ADDRESSED; CATALOG_PENDING | §§3, 4, 8–13, 15 |
| INT-10 Trazabilidad y temporalidad | Se define AuditEvent y referencias versionadas; falta un envelope formal con esquema, semántica temporal y reglas de correlación. | PARTIALLY_ADDRESSED; ENVELOPE_SCHEMA_PENDING | §§1, 14–16 |

## Acciones necesarias para cerrar la integración

- [ ] Especificar schema y validaciones de PortfolioProposal.
- [ ] Definir estructura de coste estimado/realizado y métricas de slippage.
- [ ] Definir fuentes de verdad, finality y tolerancias por clase de fuente.
- [ ] Publicar catálogos de estados y transiciones por dominio.
- [ ] Definir envelope de evidencia con IDs, actor, event time, observed time, processing time, versión y lineage.
- [ ] Revisar las redacciones con arquitectura, riesgo y operaciones.
- [ ] Obtener revisión independiente de seguridad y revisión legal/compliance aplicable.
- [ ] Registrar decisiones individuales en Issue #34 con versión y evidencia.

## Criterio de cierre

INT-01 a INT-10 solo pueden considerarse cerrados cuando sus acciones pendientes estén resueltas o aceptadas formalmente por autoridad competente, con evidencia revisable. El borrador integrado no cierra por sí mismo estos hallazgos.
