# Requisitos complementarios propuestos — PORTFOLIO & EXECUTION

**Versión:** 0.1.0  
**Fecha:** 2026-10-31  
**Estado:** PROPUESTA PARA REVISIÓN; NO APROBADA  
**Arquitectura de referencia:** `docs/architecture/strategic-architecture.md`  
**Asignación de referencia:** `docs/architecture/requirement-allocation-and-layer-contracts.md`

> Los identificadores IIM-025 a IIM-037 son provisionales. No forman parte del registro oficial de requisitos hasta que se aprueben formalmente. No cambian el estado de los IIM-001 a IIM-024 ni autorizan operación real.

## 1. Motivo

El registro vigente de 24 requisitos cubre identidad, datos, análisis, valoración, riesgo, decisión, reporting, historial y orquestación de evaluaciones. No define con suficiente precisión las capacidades necesarias para construir carteras, administrar exposiciones y ejecutar operaciones.

La capa estratégica PORTFOLIO & EXECUTION necesita requisitos explícitos. Se propone el siguiente backlog para revisión, depuración y aprobación.

## 2. Requisitos complementarios propuestos

| ID provisional | Prioridad | Requisito propuesto | Resultado verificable |
|---|---|---|---|
| IIM-025 | Crítica | Definir el mandato de inversión, horizonte, moneda base, restricciones, universo elegible, benchmark y límites operativos de cada cartera. | Mandato versionado, validado y asociado a cada cartera/decisión. |
| IIM-026 | Crítica | Construir propuestas de cartera mediante políticas de asignación explícitas, reproducibles y adecuadas al mandato, sin asumir que una puntuación individual determina el peso. | Pesos propuestos, método, inputs, restricciones, alternativas y razones reproducibles. |
| IIM-027 | Crítica | Agregar exposición y concentración por activo, representación, red, protocolo, sector/factor, venue y contraparte cuando los datos permitan hacerlo. | Exposición consolidada, cobertura, límites, concentraciones y tratamiento explícito de relaciones/solapamientos desconocidos. |
| IIM-028 | Crítica | Aplicar presupuestos de riesgo, límites duros y controles de exposición a nivel de cartera y subcartera, independientes de la valoración y ranking. | Evaluación pre y post asignación; incumplimientos bloqueantes y razones trazables. |
| IIM-029 | Crítica | Estimar liquidez, capacidad, spread, slippage, comisiones, impacto de mercado y coste total de entrada/salida por instrumento, tamaño y horizonte. | Curvas o estimaciones de coste con fuente, timestamp, supuestos, incertidumbre y límites de capacidad. |
| IIM-030 | Alta | Evaluar carteras bajo escenarios, shocks y estrés, incluyendo correlaciones inestables, gaps, iliquidez, fallos de venue y eventos de protocolo. | Resultados por escenario, pérdidas potenciales, factores dominantes y limitaciones. |
| IIM-031 | Alta | Definir reglas de rebalanceo, bandas, frecuencia, triggers, restricciones fiscales/operativas cuando apliquen y costes de rotación. | Plan de rebalanceo reproducible con impacto estimado, autorizaciones y registro de desviaciones. |
| IIM-032 | Crítica | Evaluar elegibilidad y riesgo de venues, mercados, custodios y contrapartes antes de permitir su uso. | Registro de aprobación/restricción por jurisdicción, instrumento, operación, exposición y fecha. |
| IIM-033 | Crítica | Gestionar el ciclo de vida de órdenes desde propuesta y autorización hasta envío, aceptación, parcialidad, fill, cancelación, rechazo y expiración. | Estado de orden inequívoco, historial temporal, correlación con decisión y reconciliación de eventos. |
| IIM-034 | Crítica | Aplicar controles pre-trade y post-trade, límites de pérdida/exposición, validación de órdenes y mecanismos de parada de emergencia. | Controles probados, motivos de bloqueo, kill switch, escalado y evidencia de activación. |
| IIM-035 | Crítica | Proteger credenciales, claves y permisos mediante mínimo privilegio, segregación de funciones, rotación, almacenamiento seguro y trazabilidad. | Evidencia de controles de acceso, gestión de secretos, revisión y respuesta a incidentes; sin secretos en logs o repositorio. |
| IIM-036 | Crítica | Conciliar órdenes, fills, balances, posiciones, comisiones y movimientos con fuentes de venue/custodia, conservando discrepancias. | Conciliación reproducible, diferencias detectadas, estado de resolución y audit trail. |
| IIM-037 | Crítica | Separar de forma técnica y operativa los entornos de simulación, paper trading y operación real, exigiendo gates y autorizaciones específicas para promoción. | Aislamiento verificable, credenciales y endpoints separados, promoción controlada y ausencia de rutas accidentales a ejecución real. |

## 2.1. Revisión individual y decisiones

La revisión individual de los 13 requisitos se gestiona mediante [`portfolio-execution-requirements-review-matrix.md`](portfolio-execution-requirements-review-matrix.md) y el Issue de seguimiento dedicado. El estado global permanece PENDING_REVIEW; cada identificador mantiene carácter provisional hasta una decisión formal. La aceptación para registro no equivale a especificación, implementación, pruebas ni autorización de operación.

## 3. Reglas de diseño comunes

Todos los requisitos anteriores deben cumplir:
- trazabilidad desde el mandato hasta la decisión, cartera, orden, fill y resultado;
- datos y precios point-in-time, con provenance y permisos de uso;
- estados UNKNOWN/BLOCKED/REJECTED/INCONCLUSIVE explícitos y no aprobatorios;
- límites duros que no puedan compensarse con un score;
- costes, liquidez, restricciones y capacidad considerados antes de atribuir rendimiento;
- separación de propuesta, autorización y ejecución;
- registro de actor, instante, motivo, versión de política y evidencia;
- controles de idempotencia, reintentos, duplicados, recuperación y conciliación;
- simulación como modo inicial y operación real deshabilitada por defecto.

## 4. Límites y exclusiones

Esta propuesta no define:
- fórmula universal de optimización o asignación;
- pesos, umbrales, stop-loss o límites cuantitativos;
- exchanges, custodios, proveedores o jurisdicciones aprobados;
- infraestructura de custodia o firma de transacciones;
- trading autónomo, apalancamiento o derivados habilitados;
- retorno objetivo ni garantía de preservación de capital.

Esas decisiones requieren análisis de riesgo, evidencia, requisitos no funcionales, revisión de seguridad y revisión legal aplicable.

## 5. Dependencias

Los requisitos propuestos dependen, como mínimo, de:
- identidad de activo/instrumento/listing y universo (IIM-001, IIM-019, IIM-020);
- fuentes, licencias, calidad y point-in-time (IIM-002, IIM-018);
- tesis, decisión, vetos y abstención (IIM-005, IIM-006, IIM-013, IIM-017, IIM-022);
- workflow, historial y auditoría (IIM-008, IIM-009, IIM-023, IIM-024);
- validación y controles temporales (IIM-007, IIM-015).

## 6. Secuencia sugerida

1. Aprobar mandato, restricciones y taxonomía de exposición (IIM-025, 027).
2. Aprobar políticas de riesgo, asignación y escenarios (IIM-026, 028, 030).
3. Especificar liquidez, costes, venues y contrapartes (IIM-029, 032).
4. Especificar workflow de órdenes, controles, custodia y conciliación (IIM-033 a IIM-036).
5. Aprobar aislamiento de simulación/operación real y gates de promoción (IIM-037).
6. Implementar primero simulación; operación real permanece fuera de alcance hasta superar gates técnicos, de seguridad, legales y de gobernanza.

## 7. Criterios de aceptación de la propuesta

- Cada requisito tiene alcance atómico y resultado verificable.
- Las dependencias con los IIM existentes están identificadas.
- No se duplican intencionalmente requisitos existentes; cualquier solapamiento debe resolverse durante la revisión.
- Se definen por separado portfolio management y order execution cuando sus riesgos y controles difieran.
- No se introduce ningún umbral cuantitativo sin justificación y validación.
- Los identificadores y estados provisionales no se incorporan al registro oficial sin aprobación.

**Decisión solicitada:** revisar, fusionar/dividir o rechazar cada requisito propuesto y registrar decisión, revisor, fecha, versión y evidencia.
