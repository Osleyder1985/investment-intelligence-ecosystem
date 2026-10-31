# Revisión técnica del lote B — IIM-026, IIM-029, IIM-030 e IIM-031

**Versión:** 0.1.0  
**Fecha:** 2026-10-31  
**Estado:** REVIEW_RECOMMENDATIONS_ONLY  
**Baseline:** propuesta IIM-025 a IIM-037 v0.1.0 y matriz de revisión v0.1.0  
**Dependencias de contexto:** revisión del lote A (Issue #35), Issue #34

> Revisión técnica-documental asistida. No es aprobación formal, validación cuantitativa, calibración empírica ni revisión independiente. Los identificadores permanecen provisionales y sus estados formales permanecen PENDING_REVIEW.

## 1. Conclusión ejecutiva

| ID | Recomendación técnica | Motivo principal |
|---|---|---|
| IIM-026 | REVISE | Debe especificar objetivo, factibilidad, restricciones, incertidumbre, costes, robustez y abstención sin imponer un optimizador universal. |
| IIM-029 | REVISE | Debe diferenciar liquidez observada de capacidad ejecutable y modelar mercados CEX, DEX/AMM y costes de red/MEV cuando apliquen. |
| IIM-030 | REVISE | Debe definir escenarios de cartera coherentes, shocks conjuntos, dependencias, liquidez, reverse stress y límites de interpretación probabilística. |
| IIM-031 | REVISE | Debe separar política de rebalanceo, generación de propuesta, autorización y ejecución; definir triggers, bandas, costes y controles contra churn. |

No se recomienda seleccionar aún algoritmo, función objetivo, solver, proveedor de datos ni parámetros numéricos. Tampoco optimizar sobre retornos históricos como si fueran expectativas fiables.

## 2. IIM-026 — Construcción y asignación de cartera

### 2.1. Problemas detectados

La propuesta indica que los pesos no deben derivarse de un score individual, lo cual es correcto, pero no especifica:
- qué variable se optimiza: retorno esperado, riesgo, utilidad, preservación de capital, cumplimiento de pasivos u objetivo multiobjetivo;
- cómo se formalizan las restricciones del mandato y los límites IIM-028;
- cómo se tratan activos no comparables, datos faltantes, distribuciones inestables y parámetros no calibrados;
- cómo se modelan costes, liquidez, capacidad, slippage, turnover y restricciones de venue;
- qué sucede si no existe solución factible o el solver no converge;
- cómo se compara con alternativas simples y cómo se evita sobreajuste de la política;
- cómo se conservan inputs, código, solver, versión, tolerancias y diagnóstico de reproducibilidad.

Un optimizador puede producir pesos matemáticamente válidos y económicamente absurdos si las entradas son débiles o las restricciones incompletas.

### 2.2. Redacción propuesta

**IIM-026 — Generar propuestas reproducibles de construcción y asignación de cartera a partir de un mandato vigente, posiciones existentes, universo elegible, mediciones de exposición y riesgo, estimaciones de liquidez/costes y restricciones aprobadas. Cada propuesta debe declarar objetivo, método, inputs, supuestos, restricciones activas, diagnóstico de factibilidad, sensibilidad, alternativas y comparación con referencias simples. Debe abstenerse o devolver INFEASIBLE/INCONCLUSIVE cuando los datos, restricciones o condiciones numéricas no permitan una solución defendible. La salida es una propuesta informativa y nunca constituye autorización ni orden de ejecución.**

### 2.3. Criterios de aceptación recomendados

1. Consume una versión concreta y vigente del mandato IIM-025, universo, posiciones, exposiciones IIM-027 y política de riesgo IIM-028.
2. Declara el objetivo matemático, sus unidades, horizonte, supuestos y límites de interpretación.
3. Separa inputs OBSERVED, DERIVED, ESTIMATED, CALIBRATED, ASSUMPTION y UNKNOWN.
4. Las restricciones duras son no compensables y se validan antes y después de la asignación; las preferencias blandas tienen prioridad y penalización explícitas.
5. Integra costes de transacción, liquidez, capacidad, límites de concentración y restricciones operativas, evitando asumir ejecución a mid/last price.
6. Trata incertidumbre de parámetros y error de estimación; no presenta retornos históricos como pronósticos sin validación.
7. Expone solución factible, infeasible, no convergente, degenerada o numéricamente inestable como estados diferentes.
8. Registra solver, versión, configuración, tolerancias, semilla si procede, criterio de parada y diagnóstico de convergencia.
9. Produce análisis de sensibilidad y estabilidad de pesos frente a cambios plausibles de inputs y supuestos.
10. Compara con benchmarks simples y políticas de referencia definidas ex ante; no solo con otras variantes optimizadas.
11. No fuerza pesos para activos incomparables, no elegibles, vetados o con evidencia crítica desconocida.
12. Conserva propuesta, razones, alternativas y snapshot reproducible; no envía órdenes.

### 2.4. Decisión recomendada

**REVISE** antes de aceptar para registro. Debe especificar un contrato de propuesta de cartera independiente de cualquier algoritmo particular. La selección de optimizador será una decisión metodológica posterior y validable.

## 3. IIM-029 — Liquidez y coste de ejecución

### 3.1. Problemas detectados

La lista de spread, profundidad, comisiones, slippage e impacto es incompleta sin definir:
- mercado, par, venue, lado, tamaño y horizonte de ejecución;
- distinción entre volumen reportado, volumen negociado y profundidad ejecutable;
- estimación para órdenes de mercado y límites, parcialidad y latencia;
- diferencias entre order books centralizados y pools AMM/DEX;
- gas, prioridad, MEV, price impact, pool utilization y riesgo de sandwich/front-running cuando apliquen;
- impacto de fragmentación entre venues y riesgo de contraparte/custodia;
- sensibilidad a volatilidad, congestión, gaps y retirada de liquidez;
- validación fuera de muestra del modelo de impacto y slippage;
- límites de extrapolación y caducidad de cotizaciones.

Un volumen alto reportado no garantiza que el tamaño requerido pueda ejecutarse sin impacto material.

### 3.2. Redacción propuesta

**IIM-029 — Estimar, por instrumento/representación, mercado, venue, lado, tamaño y horizonte, la liquidez disponible y el coste total esperado de entrada/salida, distinguiendo observaciones de mercado de estimaciones de ejecución. Incluir, cuando apliquen, spread, profundidad ejecutable, comisiones, slippage, impacto de mercado, latencia, gas, price impact de AMM y costes/riesgos de inclusión on-chain. Cada estimación debe declarar fuente, timestamp, cobertura, metodología, supuestos, incertidumbre, tamaño máximo soportado y condiciones de invalidez; no extrapolar fuera del dominio validado ni presentar volumen reportado como capacidad ejecutable.**

### 3.3. Criterios de aceptación recomendados

1. Cada cálculo identifica activo/instrumento/listing, par, venue/pool, red, lado, tamaño, moneda y horizonte.
2. Distingue último precio, mid, bid/ask, profundidad, volumen reportado, operaciones observadas y liquidez ejecutable estimada.
3. Define snapshots y frescura de libro/pool, latencia, timestamp y cobertura; una cotización caducada no se presenta como actual.
4. Incluye comisiones explícitas, tiers y rebates solo si su elegibilidad está demostrada.
5. Para order books, contempla profundidad por niveles, spread, parcialidad, slippage y supuestos de ejecución.
6. Para AMM/DEX, contempla curva de precio, reservas/liquidez del pool, fee tier, price impact, gas y riesgos de transacción cuando sean relevantes.
7. Estima impacto por tamaño y lado; no extrapola linealmente sin evidencia.
8. Declara incertidumbre, intervalo/rango o escenarios; no atribuye precisión que el modelo no tiene.
9. Diferencia capacidad estimada del mercado de la capacidad realmente accesible para la cuenta/usuario.
10. Considera fragmentación y dependencia de venue; no suma liquidez duplicada ni inaccesible.
11. Define límites de dominio y abstención ante profundidad insuficiente, fuentes discrepantes, mercado discontinuo o datos stale.
12. Conserva inputs, metodología, versión y resultados para comparar coste estimado con coste observado posteriormente.

### 3.4. Decisión recomendada

**REVISE** antes de aceptar para registro. Es necesario un contrato de estimación sensible a instrumento, venue, tamaño y tecnología de mercado. IIM-029 estima; IIM-033 registra el ciclo real de órdenes y fills.

## 4. IIM-030 — Escenarios y estrés de cartera

### 4.1. Problemas detectados

La propuesta enumera shocks relevantes, pero necesita:
- distinguir escenarios históricos, hipotéticos, deterministas y probabilísticos calibrados;
- definir shocks conjuntos y dependencias no lineales entre activos/protocolos/venues;
- modelar correlaciones que convergen durante crisis y fallos comunes;
- incluir riesgo de liquidez, gaps, deslistados, depeg, bridge failure, stablecoin impairment y counterparty freeze;
- incluir escenarios inversos (reverse stress): qué combinación hace inviable la cartera o incumple un límite;
- separar pérdida mark-to-market de pérdida realizable bajo restricciones de salida;
- definir horizontes, moneda, unidad, baseline y cobertura;
- impedir que un escenario favorable/central se presente como pronóstico.

### 4.2. Redacción propuesta

**IIM-030 — Evaluar carteras y sus dependencias mediante un catálogo versionado de escenarios históricos, hipotéticos, deterministas y, solo cuando exista calibración validada, probabilísticos. Los escenarios deben especificar horizonte, shocks conjuntos, dependencias, supuestos, liquidez/capacidad, costes y limitaciones, y estimar impactos sobre valor, exposición, liquidez y cumplimiento de límites. Debe incluir escenarios inversos para identificar condiciones que causen breaches o pérdida de viabilidad, sin presentar resultados de estrés como predicciones ni probabilidades no calibradas.**

### 4.3. Criterios de aceptación recomendados

1. Cada escenario tiene ID, versión, propósito, fecha, horizonte, fuente y responsable.
2. Se distingue histórico, hipotético, determinista, reverse stress y probabilístico calibrado.
3. Los shocks son coherentes y conjuntos cuando existen dependencias; no combina extremos incompatibles sin justificación.
4. Incluye riesgos de mercado, liquidez, contraparte, protocolo, red, stablecoin, bridge, custodia y continuidad cuando sean materiales.
5. Evalúa exposición directa e indirecta y dependencias compartidas de cartera.
6. Diferencia pérdida de valoración y pérdida realizable; incorpora costes y límites de salida cuando los datos lo permitan.
7. Evalúa impacto en presupuestos/límites IIM-028 y necesidades de liquidez del mandato.
8. Identifica factores dominantes, supuestos, desconocidos, cobertura y limitaciones del modelo.
9. Incluye reverse stress con definición explícita de breach/fallo, sin afirmar que sea una predicción.
10. No asigna probabilidades a escenarios hipotéticos sin calibración y validación.
11. Registra resultados por posición, factor, dependencia y cartera total, evitando doble conteo.
12. Conserva inputs point-in-time, reglas, código, versiones y resultados reproducibles.

### 4.4. Decisión recomendada

**REVISE** antes de aceptar para registro. El alcance es necesario y coherente con la política existente de escenarios de valoración, pero debe extenderse a riesgos conjuntos y pérdidas realizables de cartera. No duplicar IIM-021: este requisito es portfolio-level risk stress, no valoración de un activo individual.

## 5. IIM-031 — Rebalanceo

### 5.1. Problemas detectados

La propuesta mezcla reglas de rebalanceo y restricciones fiscales/operativas sin definir:
- qué eventos generan revisión: calendario, drift, cambios de mandato, límites, liquidez, riesgo o cambios estructurales;
- si la banda es absoluta, relativa, por riesgo o por exposición;
- cómo se evita churn por oscilaciones de precio y costes;
- cómo priorizar corrección de breaches frente a optimización marginal;
- qué ocurre si no existe liquidez o el mercado está cerrado/degradado;
- tratamiento de depósitos, retiros, staking rewards, forks, airdrops, migraciones y corporate actions equivalentes;
- separación entre propuesta, autorización y ejecución;
- qué límites no pueden ser ignorados por un rebalanceo automático.

### 5.2. Redacción propuesta

**IIM-031 — Detectar condiciones de revisión y generar propuestas versionadas de rebalanceo para una cartera, comparando asignación actual y objetivo bajo el mandato, límites de riesgo, exposiciones, liquidez, costes y restricciones vigentes. La política debe definir triggers, bandas, frecuencia, prioridades, tratamiento de eventos y condiciones de abstención, y cuantificar turnover, costes, impacto y desviaciones antes/después. Toda propuesta requiere autorización independiente antes de cualquier orden; no se ejecuta automáticamente por el mero hecho de activarse un trigger.**

### 5.3. Criterios de aceptación recomendados

1. Triggers explícitos: calendario, drift, breach, cambio de mandato, riesgo, liquidez, elegibilidad o evento material.
2. Cada trigger define ámbito, fuente, timestamp, prioridad, vigencia y regla de desactivación.
3. Bandas y umbrales son parametrizados y aprobados; no se fijan valores universales.
4. Incluye controles anti-churn: hysteresis, cooldown o evaluación neta de costes, cuando estén justificados.
5. Prioriza la remediación de incumplimientos duros frente a mejoras marginales de optimización.
6. Calcula asignación actual/propuesta, turnover, costes, liquidez, exposición y límites antes/después.
7. Expone activos no operables, mercado discontinuo, liquidez insuficiente y restricciones que impidan completar el plan.
8. Trata eventos no discrecionales de cartera (rewards, forks, airdrops, migraciones, depósitos/retiros) con reglas explícitas y trazabilidad.
9. No fuerza operaciones para alcanzar pesos objetivo cuando el coste, riesgo o evidencia hacen inviable el rebalanceo.
10. La propuesta referencia mandato, política, snapshots y versión de cálculo.
11. Requiere autorización separada; la propuesta no crea órdenes ni cambia posiciones.
12. Registra desviaciones aceptadas, decisiones de no actuar y razones.

### 5.4. Decisión recomendada

**REVISE** antes de aceptar para registro. Rebalanceo es una capacidad distinta de construcción inicial de cartera y debe producir propuestas, nunca órdenes implícitas.

## 6. Interfaces y dependencias del lote B

| Requisito | Entradas necesarias | Salidas consumidas por | Límite de responsabilidad |
|---|---|---|---|
| IIM-026 | IIM-025, universo/identidad, IIM-027, IIM-028, IIM-029, IIM-030 | IIM-031, reporting y flujo de autorización | Propuesta de pesos; no autoriza ni ejecuta. |
| IIM-029 | Datos de mercado point-in-time, instrumento/listing, venue/pool, tamaño, lado y cuenta/ruta cuando aplique | IIM-026, IIM-030, IIM-031, IIM-033 | Estimación de capacidad/coste; no garantiza fills. |
| IIM-030 | Posiciones, exposición, riesgos, escenarios y liquidez/costes | IIM-028, IIM-026, IIM-031 | Diagnóstico de estrés; no predice probabilidades no calibradas. |
| IIM-031 | Mandato, cartera actual, propuesta objetivo, límites, exposición, liquidez y costes | Proceso separado de autorización | Plan propuesto; no crea órdenes. |

## 7. Reglas de precedencia y abstención

1. IIM-025 define lo permitido; IIM-028 establece restricciones duras; IIM-017 mantiene vetos no compensables.
2. IIM-029 aporta costes y capacidad estimada; la incertidumbre material puede bloquear una propuesta IIM-026 o IIM-031.
3. IIM-030 puede identificar un breach potencial y alimentar IIM-028, pero no altera por sí mismo la política ni ejecuta mitigación.
4. IIM-026 no puede suavizar o ignorar límites para obtener solución factible.
5. IIM-031 no puede rebalancear hacia un activo no elegible ni generar órdenes sin autorización.
6. UNKNOWN, STALE, INVALID, INSUFFICIENT_COVERAGE e INFEASIBLE no se convierten en PASS ni en cero.
7. Las propuestas se comparan contra referencias simples y se someten a validación temporal y fuera de muestra antes de atribuirles eficacia.

## 8. Validación posterior requerida

Antes de considerar estos requisitos SPECIFIED o implementar componentes productivos, será necesario:
- especificaciones matemáticas y semánticas independientes;
- datasets point-in-time y universos históricos con supervivencia/deslistados;
- simulación de costes y capacidad con comparación contra costes observados;
- pruebas de factibilidad, robustez, sensibilidad y fallos del solver;
- escenarios adversariales, shocks conjuntos y reverse stress;
- backtests walk-forward/out-of-sample sin leakage ni data snooping;
- benchmarks simples y costes realistas;
- pruebas de abstención, datos stale/faltantes, límites duros y mercados discontinuos;
- revisión independiente de riesgo, metodología y seguridad donde corresponda.

La existencia de protocolos o escenarios documentados no demuestra que estas pruebas se hayan ejecutado.

## 9. Estado de la revisión

- IIM-026: **PENDING_REVIEW** — recomendación técnica: REVISE.
- IIM-029: **PENDING_REVIEW** — recomendación técnica: REVISE.
- IIM-030: **PENDING_REVIEW** — recomendación técnica: REVISE.
- IIM-031: **PENDING_REVIEW** — recomendación técnica: REVISE.

La decisión formal corresponde al proceso de aprobación del Issue #34. No modificar el registro oficial hasta que exista decisión autorizada y trazable.
