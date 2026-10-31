# IIE — Matriz maestra de trazabilidad metodológica

**ID:** IIM-TRACE-MASTER-001 | **Versión:** 0.1.0-draft | **Estado:** INCOMPLETE / NOT_APPROVED | **Fecha:** 2026-10-31

Esta matriz enlaza requisitos normativos con secciones de la metodología y políticas de dominio. MAPPED_DOC significa cobertura documental; PARTIAL indica que faltan reglas operativas o criterios de aceptación; GAP indica ausencia de evidencia suficiente. TBD significa que contrato, componente o prueba aún no están aprobados/verificados. No modifica el registro normativo.

## IIM-001 a IIM-024

| ID | Secciones | Referencia de dominio | Cobertura |
|---|---|---|---|
| 001 Identidad | 3, G1, 18 | asset-identity-resolution | MAPPED_DOC |
| 002 Datos/procedencia | G2, 18 | data-source-register; data-quality-policy | MAPPED_DOC |
| 003 Precio vs infravaloración | 4, 6, 10, 14 | undervaluation/decision-framework | MAPPED_DOC |
| 004 Valoración por tipología | 10 | valuation-method-catalog; applicability-matrix | PARTIAL |
| 005 Hipótesis/contraevidencia | 2, 12, 18 | governance/evidence-policy | MAPPED_DOC |
| 006 Riesgo con veto | G5, 11 | risk-taxonomy; veto-policy | PARTIAL |
| 007 Sesgo temporal/OOS | 2, 16 | data-lineage-and-point-in-time; calibration-protocol | PARTIAL |
| 008 Historial versionado | 9, 17, 18 | evaluation-snapshot-and-versioning | MAPPED_DOC |
| 009 Solicitud explícita | G0, 15 | evaluation-workflow | PARTIAL |
| 010 Evidencia/incertidumbre | 2, 12, 18, 19 | evaluation-report-contract | MAPPED_DOC |
| 011 Taxonomía | 4.2, 6–10 | asset-taxonomy | MAPPED_DOC |
| 012 Método por objeto | 10 | valuation-applicability-matrix | PARTIAL |
| 013 Abstención de valor | 2, 10, 12 | valuation-model-contract | MAPPED_DOC |
| 014 Precio ejecutable | G3, G6, 14 | valuation-gap-metrics | PARTIAL |
| 015 Infravaloración extrema | 14, 16 | undervaluation-calibration-protocol | PARTIAL |
| 016 Riesgo por tipología | G5, 11 | risk-assessment-template; risk-taxonomy | PARTIAL |
| 017 Vetos/transiciones | G5, 11, 12 | veto-policy; risk-veto-test-cases | PARTIAL |
| 018 Calidad/linaje/PIT | G2, 18 | data-quality-policy; data-lineage-and-point-in-time | MAPPED_DOC |
| 019 Identidad canónica | 3, G1 | asset-identity-resolution | MAPPED_DOC |
| 020 Universo/screening | 4, G0/G3, 16 | universe-construction; eligibility-and-screening | PARTIAL |
| 021 Modelos reproducibles | 10, 16, 18 | valuation-model-contract; scenario-and-sensitivity | PARTIAL |
| 022 Gates/ranking | G5–G7, 12–13 | decision-gates; ranking-and-dominance | PARTIAL |
| 023 Informe/snapshots | 12, 18 | evaluation-report-contract; snapshot-versioning | MAPPED_DOC |
| 024 Workflow | G0–G9, 17 | evaluation-workflow; failure-recovery-and-idempotency | PARTIAL |

Para todos los requisitos: **contrato, componente y prueba automatizada = TBD** en esta matriz hasta enlazar artefactos aprobados y resultados de ejecución verificables.

## IIM-025 a IIM-037 — Portfolio & Execution propuestos

Estos requisitos permanecen propuestos y fuera del registro normativo aprobado.

| ID | Interfaz con selección | Cobertura |
|---|---|---|
| 025 Mandato | G0, G8 | PARTIAL |
| 026 Construcción/asignación | G8, sección 15 | PARTIAL; responsabilidad L3 |
| 027 Exposición/concentración | G8, sección 15 | PARTIAL; cálculo L3 |
| 028 Presupuesto de riesgo | G8, sección 15 | PARTIAL; política L3 |
| 029 Liquidez/costes | G3, G6, sección 15 | PARTIAL; interfaz por formalizar |
| 030 Escenarios/estrés | 11, 15, 16 | PARTIAL; separar riesgo activo/cartera |
| 031 Rebalanceo | sección 15 | PARTIAL; fuera de selección |
| 032 Venue/contraparte | G3, 11, 15 | PARTIAL; ruta y contraparte |
| 033 Ciclo de órdenes | sección 15 | GAP; contrato L3 |
| 034 Controles pre/post trade | sección 15 | GAP; enforcement L3 |
| 035 Seguridad/custodia | G3, 11, 15 | PARTIAL; análisis vs controles L3 |
| 036 Conciliación | sección 15 | GAP; contrato L3 |
| 037 Promoción de entornos | sección 15 | GAP; gobernanza L3/X |

## Brechas transversales

1. Contrato canónico de evaluación y máquina de estados no aprobados.
2. Falta matriz categoría × módulo × evidencia mínima × motivo de abstención.
3. Vocabularios de screening, veto, decisión y ranking deben reconciliarse.
4. Liquidez ejecutable requiere especificación cuantitativa.
5. Validación científica requiere target, benchmark y horizonte por caso de uso.
6. Enlaces internos y vigencia documental requieren comprobación exhaustiva.
7. La existencia de documentación o casos de prueba no demuestra implementación ni ejecución.

## Criterios de cierre

Cada requisito debe enlazar autoridad, sección, entrada/salida, responsable, contrato, componente, prueba y evidencia de ejecución. Los requisitos provisionales conservarán ese estado hasta aprobación. No declarar IMPLEMENTED, TESTED o EMPIRICALLY_VALIDATED solo por documentación.

**Estado:** INCOMPLETE / NOT_APPROVED / NOT_IMPLEMENTED / NOT_EMPIRICALLY_VALIDATED.
