# Registro de requisitos

**Corte de revisión:** 2026-10-30  
**Matriz de trazabilidad:** [requirements-traceability-matrix](../docs/traceability/requirements-traceability-matrix.md)  
**Criterios de estado:** [evidence-and-status-criteria](../docs/traceability/evidence-and-status-criteria.md)

| ID | Requisito | Prioridad | Estado documental | Evidencia principal |
|---|---|---|---|---|
| IIM-001 | Identificar inequívocamente cada criptoactivo y su red/contrato | Crítica | DOCUMENTED / SPECIFICATION_DRAFTED; no aprobado | [Identidad canónica](../docs/asset-selection/asset-identity-resolution.md); PR #12 |
| IIM-002 | Registrar procedencia, licencia, cobertura y calidad de los datos | Crítica | DOCUMENTED / SPECIFICATION_DRAFTED; no aprobado | [Registro de fuentes](../docs/data/data-source-register.md); [calidad](../docs/data/data-quality-policy.md); PR #10 |
| IIM-003 | Diferenciar precio bajo de infravaloración | Crítica | DOCUMENTED / SPECIFICATION_DRAFTED; no aprobado | [Marco de infravaloración](../docs/undervaluation/decision-framework.md); PR #6 |
| IIM-004 | Estimar valor mediante métodos adecuados a la tipología | Alta | DOCUMENTED / SPECIFICATION_DRAFTED; no aprobado | [Catálogo de métodos](../docs/valuation/valuation-method-catalog.md); [contrato de modelos](../docs/valuation/valuation-model-contract.md); PR #4 y #14 |
| IIM-005 | Registrar hipótesis, alternativas y contraevidencia | Alta | DOCUMENTED / SPECIFICATION_DRAFTED; no aprobado | [Política de evidencia](../docs/governance/evidence-policy.md); [contrato de informe](../docs/reporting/evaluation-report-contract.md); PR #2 y #16 |
| IIM-006 | Evaluar riesgos críticos con capacidad de veto | Crítica | DOCUMENTED / SPECIFICATION_DRAFTED; no aprobado | [Taxonomía de riesgos](../docs/risk/risk-taxonomy.md); [política de vetos](../docs/risk/veto-policy.md); PR #8 |
| IIM-007 | Evitar sesgos temporales y validar out-of-sample | Crítica | DOCUMENTED / SPECIFICATION_DRAFTED; no aprobado; validación no ejecutada | [Protocolo de calibración](../docs/validation/undervaluation-calibration-protocol.md); [point-in-time](../docs/data/data-lineage-and-point-in-time.md); PR #6 y #10 |
| IIM-008 | Mantener historial versionado de evaluaciones | Alta | DOCUMENTED / SPECIFICATION_DRAFTED; no aprobado | [Snapshots y versionado](../docs/history/evaluation-snapshot-and-versioning.md); PR #16 |
| IIM-009 | No ejecutar análisis sin solicitud explícita del usuario | Alta | DOCUMENTED / SPECIFICATION_DRAFTED; control ejecutable no evidenciado | [Workflow bajo demanda](../docs/execution/evaluation-workflow.md); PR #17 |
| IIM-010 | Producir conclusión con nivel de evidencia e incertidumbre | Crítica | DOCUMENTED / SPECIFICATION_DRAFTED; no aprobado | [Contrato de informe](../docs/reporting/evaluation-report-contract.md); [schema](../docs/reporting/structured-evaluation-schema.md); PR #16 |
| IIM-011 | Clasificar criptoactivos por derechos, función y captura de valor, admitiendo tipologías híbridas | Crítica | DOCUMENTED / SPECIFICATION_DRAFTED; no aprobado | [Taxonomía de activos](../docs/asset-selection/asset-taxonomy.md); PR #4 |
| IIM-012 | Seleccionar métodos de valoración según el objeto y la tipología económica | Crítica | DOCUMENTED / SPECIFICATION_DRAFTED; no aprobado | [Matriz de aplicabilidad](../docs/valuation/valuation-applicability-matrix.md); PR #4 |
| IIM-013 | Abstenerse de estimar valor razonable cuando falten derechos o evidencia suficientes | Crítica | DOCUMENTED / SPECIFICATION_DRAFTED; no aprobado | [Contrato de modelos](../docs/valuation/valuation-model-contract.md); [especificaciones por tipología](../docs/valuation/model-specifications-by-typology.md); PR #14 |
| IIM-014 | Clasificar la brecha entre precio ejecutable y rango de valor razonable con incertidumbre y costes explícitos | Crítica | DOCUMENTED / SPECIFICATION_DRAFTED; no aprobado | [Métricas de brecha](../docs/undervaluation/valuation-gap-metrics.md); PR #6 |
| IIM-015 | Habilitar la categoría extrema solo tras calibración y validación temporal independiente | Crítica | DOCUMENTED / SPECIFICATION_DRAFTED; NOT_CALIBRATED | [Protocolo de calibración](../docs/validation/undervaluation-calibration-protocol.md); PR #6 |
| IIM-016 | Evaluar riesgos por tipología, evidencia, alcance, controles y riesgo residual | Crítica | DOCUMENTED / SPECIFICATION_DRAFTED; no aprobado | [Taxonomía de riesgos](../docs/risk/risk-taxonomy.md); [plantilla](../docs/risk/risk-assessment-template.md); PR #8 |
| IIM-017 | Aplicar vetos críticos independientes de la valoración y con transiciones verificables | Crítica | DOCUMENTED / SPECIFICATION_DRAFTED; no aprobado | [Política de vetos](../docs/risk/veto-policy.md); [casos de prueba](../docs/risk/risk-veto-test-cases.md); PR #8 |
| IIM-018 | Asegurar calidad, admisibilidad, linaje y disponibilidad point-in-time de cada dato utilizado | Crítica | DOCUMENTED / SPECIFICATION_DRAFTED; no aprobado | [Calidad de datos](../docs/data/data-quality-policy.md); [linaje](../docs/data/data-lineage-and-point-in-time.md); PR #10 |
| IIM-019 | Resolver identidad canónica de activos, redes, contratos y representaciones sin depender del ticker | Crítica | DOCUMENTED / SPECIFICATION_DRAFTED; no aprobado | [Resolución de identidad](../docs/asset-selection/asset-identity-resolution.md); PR #12 |
| IIM-020 | Construir universos versionados y screening por finalidad sin sesgo de supervivencia ni precio nominal | Crítica | DOCUMENTED / SPECIFICATION_DRAFTED; no aprobado | [Universo](../docs/asset-selection/universe-construction-policy.md); [elegibilidad y screening](../docs/asset-selection/eligibility-and-screening.md); PR #12 |
| IIM-021 | Especificar modelos de valoración reproducibles con atribución demostrada, escenarios, sensibilidad y abstención | Crítica | DOCUMENTED / SPECIFICATION_DRAFTED; no aprobado | [Contrato de modelos](../docs/valuation/valuation-model-contract.md); [escenarios y sensibilidad](../docs/valuation/scenario-and-sensitivity-policy.md); PR #14 |
| IIM-022 | Emitir decisiones informativas y rankings parciales reproducibles por horizonte, con gates no compensables, dominancia y abstención | Crítica | DOCUMENTED / SPECIFICATION_DRAFTED; no aprobado | [Gates y resultados](../docs/decision/decision-gates-and-outcomes.md); [ranking y dominancia](../docs/decision/ranking-and-dominance-policy.md); PR #15 |
| IIM-023 | Producir informes de evaluación trazables y snapshots inmutables comparables longitudinalmente con descomposición de cambios | Crítica | DOCUMENTED / SPECIFICATION_DRAFTED; no aprobado | [Informe](../docs/reporting/evaluation-report-contract.md); [snapshots](../docs/history/evaluation-snapshot-and-versioning.md); PR #16 |
| IIM-024 | Orquestar evaluaciones reproducibles bajo demanda con estados, gates, idempotencia, recuperación y observabilidad | Crítica | DOCUMENTED / SPECIFICATION_DRAFTED; no aprobado | [Workflow](../docs/execution/evaluation-workflow.md); [recuperación e idempotencia](../docs/execution/failure-recovery-and-idempotency.md); PR #17 |

## Interpretación del estado

- Los 24 requisitos tienen evidencia documental relacionada en `main`.
- Los documentos existentes son borradores metodológicos; la matriz no declara ninguno formalmente aprobado como SPECIFIED.
- La presencia de casos de prueba escritos no significa que se hayan ejecutado.
- No se declara ningún requisito IMPLEMENTED, TESTED ni EMPIRICALLY_VALIDATED con la evidencia disponible en este repositorio.
- La categoría de infravaloración extrema permanece NOT_CALIBRATED y deshabilitada.
- Los requisitos no se consideran aprobados hasta su análisis, trazabilidad y aceptación formal.

La revisión completa, las dependencias, las brechas y los límites de esta afirmación están descritos en [la matriz de trazabilidad](../docs/traceability/requirements-traceability-matrix.md).