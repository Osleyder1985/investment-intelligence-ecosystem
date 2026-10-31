# Auditoría de consistencia metodológica — Global Crypto Asset Selection Methodology

**ID:** IIM-AUDIT-METHOD-001  
**Fecha:** 2026-10-31  
**Objeto:** `docs/methodology/01-global-crypto-asset-selection-methodology.md` v0.1.0-draft y sus políticas de dominio relacionadas.  
**Estado:** REVIEW_FINDINGS — no constituye aprobación independiente ni validación empírica.

## 1. Alcance y limitaciones

Se realizó una revisión documental de la metodología maestra, registro IIM-001 a IIM-024, asignación arquitectónica, política de elegibilidad/screening, política de vetos, gates/estados de decisión y política de ranking/dominancia disponibles en el repositorio. No se ejecutó software, backtesting ni pruebas cuantitativas. No se verificó de forma exhaustiva cada URL externa o enlace relativo del repositorio. Esta revisión no es una auditoría legal, de seguridad ni una revisión independiente externa.

## 2. Resumen ejecutivo

La metodología maestra cubre el ciclo conceptual de selección, pero todavía no es normativa ni ejecutable. Se detectaron problemas de consistencia entre la especificación maestra y políticas especializadas, así como brechas de formalización. Se corrigió en la metodología la contradicción más clara en el orden de los gates: valoración quedaba incluida en G4 y simultáneamente aparecía en G6, después de riesgos.

**Conclusión:** `REVISE`. La metodología puede continuar como borrador de integración, pero no debe aprobarse ni utilizarse como base de recomendaciones automatizadas hasta cerrar los hallazgos críticos y altos.

## 3. Hallazgos

### M-01 — CRITICAL — Orden de gates permitía valoración antes de resolver riesgos

**Evidencia:** G4 remitía originalmente a las secciones 6–10, incluyendo valoración, mientras G6 volvía a valorar después de G5 (riesgos y vetos).

**Riesgo:** ejecución ambigua del flujo; una valoración o señal favorable podía calcularse antes de completar los vetos.

**Corrección aplicada:** G4 ahora cubre secciones 6–9 y excluye expresamente la conclusión de valoración. G5 precede a G6; G6 exige que G1–G5 permitan continuar.

**Estado:** ADDRESSED_IN_DRAFT — requiere revisión de consistencia posterior y pruebas del workflow.

### M-02 — HIGH — Vocabularios de estados divergentes entre documentos

**Evidencia:** la metodología maestra enumera `VETOED`, `INELIGIBLE`, `ABSTAIN`, `OPPORTUNITY_SUPPORTED`; la política de gates utiliza estados más específicos como `NOT_ELIGIBLE_DUE_TO_ACTIVE_VETO`, `RISK_ASSESSMENT_INCOMPLETE`, `OPPORTUNITY_SUPPORTED_BY_EVIDENCE`, `INSUFFICIENT_EVIDENCE`. Screening usa `QUARANTINED` y `INSUFFICIENT_INFORMATION`.

**Riesgo:** consumidores y operadores pueden interpretar estados distintos como equivalentes, perdiendo causa, alcance o severidad.

**Acción requerida:** establecer un vocabulario canónico por dominio y un modelo de decisión agregado; definir equivalencias explícitas, precedencia, exclusividad, transiciones y compatibilidad. No renombrar estados sin migración/versionado.

**Estado:** OPEN.

### M-03 — HIGH — Gate G3 mezcla elegibilidad de activo y disponibilidad de ruta

**Evidencia:** G3 agrupa elegibilidad para investigación/valoración/comparación con custodia y ruta de ejecución.

**Riesgo:** una limitación de venue/custodia puede contaminar indebidamente la elegibilidad de investigación o una ruta concreta puede interpretarse como propiedad global del activo.

**Acción requerida:** modelar elegibilidad por finalidad y objeto de alcance: activo, representación, mercado, venue, custodio, jurisdicción, tamaño y ruta. Separar elegibilidad analítica de route eligibility.

**Estado:** OPEN.

### M-04 — HIGH — Separación entre análisis de riesgo y gate de riesgo no está formalizada

**Evidencia:** sección 11 describe evaluación integral de riesgos; G5 aplica vetos; la política de vetos exige dominio/alcance, precedencia y transiciones.

**Riesgo:** duplicación o divergencia entre perfil analítico de riesgo, estado de veto y decisión final.

**Acción requerida:** especificar RiskAssessment, RiskDomainStatus y VetoDecision como resultados diferenciados; el motor de decisión consume estos resultados sin recalcularlos ni rebajar UNKNOWN.

**Estado:** OPEN.

### M-05 — HIGH — Taxonomía no está conectada a una matriz operativa de aplicabilidad

**Evidencia:** sección 4 lista familias y secciones 6–10 condicionan módulos por aplicabilidad, pero no existe una matriz completa categoría × módulo × evidencia mínima × abstención.

**Riesgo:** selección oportunista de modelos, omisión silenciosa de análisis o comparación de objetos no equivalentes.

**Acción requerida:** crear matriz versionada de aplicabilidad para fundamentales, tokenomics, tecnología, mercado, riesgos, valoración y validación; incluir tipologías híbridas y reglas para módulos NOT_APPLICABLE frente a DATA_INSUFFICIENT.

**Estado:** OPEN.

### M-06 — HIGH — Criterios de invertibilidad no tienen especificación de medición

**Evidencia:** se mencionan profundidad, spread, slippage, impacto y tamaño, pero no se formalizan ventanas, fuentes, lados, venues, agregación, escenarios de estrés ni incertidumbre.

**Riesgo:** resultados no reproducibles y falsa liquidez basada en volumen reportado.

**Acción requerida:** especificar contrato de LiquidityAssessment con timestamp, order size, side, venue set, order-book depth, spread, fees, slippage/impact model, transfer constraints, stress assumptions y confidence/limitations. No fijar thresholds universales sin validación.

**Estado:** OPEN.

### M-07 — HIGH — Ranking parcial y estados de decisión no están reconciliados

**Evidencia:** la política de ranking define grupos y dominancia; la metodología define estados propios y menciona grupos similares, sin mapeo normativo.

**Riesgo:** un activo podría aparecer en ranking favorable pese a estar en revisión o sin valoración defendible.

**Acción requerida:** definir reglas de inclusión por grupo, compatibilidad, exclusión, no comparabilidad y precedencia; preservar ranking parcial sin ordenar grupos bloqueados.

**Estado:** OPEN.

### M-08 — HIGH — Protocolo científico no define todavía experimento por objetivo de selección

**Evidencia:** sección 16 enumera controles y métricas, pero no define target, unidad de predicción, etiqueta, horizonte, universo de prueba, costes y benchmark por familia.

**Riesgo:** optimizar retrospectivamente un proxy irrelevante o declarar habilidad de selección con target mal especificado.

**Acción requerida:** preregistrar protocolo por caso de uso (clasificación, ranking, valoración, timing o cartera), target económico, horizon, benchmark, tratamiento de delistings, costes, métricas primarias/secundarias y holdout.

**Estado:** OPEN.

### M-09 — MEDIUM — Límites entre selección y Portfolio & Execution requieren contrato explícito

**Evidencia:** la metodología incluye G8 y sección 15, mientras IIM-025–037 permanecen propuestas de requisitos separadas.

**Riesgo:** confundir activo seleccionado con activo aprobado para una cartera, venue o ejecución.

**Acción requerida:** mantener interfaz explícita entre candidate decision y portfolio proposal; Portfolio & Execution conserva mandato, presupuestos, autorización, controles, órdenes y conciliación.

**Estado:** OPEN.

### M-10 — MEDIUM — Responsables, aprobaciones y segregación de funciones no están asignados

**Evidencia:** se mencionan revisión, aprobaciones y responsables, sin matriz RACI ni separación entre productor, revisor y aprobador.

**Riesgo:** autocertificación de modelos y controles.

**Acción requerida:** definir RACI, independencia mínima, conflictos de interés, evidencia de aprobación y escalamiento.

**Estado:** OPEN.

### M-11 — MEDIUM — Referencias documentales y enlaces no auditados exhaustivamente

**Evidencia:** la sección 20 enumera documentos relacionados; esta revisión no verificó todos los destinos en el árbol actual.

**Riesgo:** trazabilidad rota o referencia a documento inexistente/desactualizado.

**Acción requerida:** comprobar todos los enlaces relativos, existencia, vigencia, versión y autoridad; automatizar link-check para documentación interna.

**Estado:** OPEN.

### M-12 — MEDIUM — La metodología no define aún caducidad y reevaluación por tipo de evidencia

**Evidencia:** se exige fecha de caducidad y monitorización, pero no políticas de freshness/TTL diferenciadas por dominio.

**Riesgo:** decisión aparentemente vigente con datos obsoletos o evento material no incorporado.

**Acción requerida:** establecer freshness policy por variable/fuente/dominio, eventos invalidantes y SLA de reevaluación, sin imponer una cadencia única a todo el universo.

**Estado:** OPEN.

## 4. Corrección aplicada

Se modificó G4/G5/G6 en la metodología maestra para:
- excluir valoración del gate de análisis de inteligencia;
- situar riesgos y vetos antes de la valoración;
- condicionar G6 a que G1–G5 permitan continuar;
- exigir abstención cuando no exista un modelo defendible.

La corrección está en el commit `a0a7a9ab7809e60826fd71b5af6c0d73e9383303`.

## 5. Orden de resolución recomendado

1. M-02: vocabulario canónico y máquina de estados.
2. M-03/M-04: separación elegibilidad analítica/ruta y RiskAssessment/VetoDecision.
3. M-05: matriz de aplicabilidad por tipología.
4. M-06: contrato de liquidez ejecutable.
5. M-07: compatibilidad entre decisión y ranking parcial.
6. M-08: protocolo científico por objetivo de selección.
7. M-10/M-11/M-12: gobernanza, enlaces y freshness.
8. M-09: contrato formal de salida hacia Portfolio & Execution.

## 6. Decisión

**REVISE — no aprobado.** Los hallazgos M-02 a M-08 deben cerrarse o recibir una disposición formal justificada antes de solicitar aprobación metodológica. La corrección de M-01 solo está addressed in draft. No se declara implementación, pruebas ejecutadas ni validación empírica.
